package ru.mail.fedka2005.objects;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.jgroups.*;
import org.jgroups.blocks.locking.LockService;
import org.jgroups.blocks.*;
import org.jgroups.blocks.atomic.*;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.*;

import org.jgroups.stack.ProtocolStack;
import org.jgroups.util.RspList;

import ru.mail.fedka2005.exceptions.ClientConstructorException;
import ru.mail.fedka2005.exceptions.ClientStartException;
import ru.mail.fedka2005.exceptions.DetachNodeException;
import ru.mail.fedka2005.exceptions.UpdateInfoException;
import ru.mail.fedka2005.exceptions.JGroupsException;
import ru.mail.fedka2005.exceptions.MasterReplaceException;
import ru.mail.fedka2005.main.Controller;
import ru.mail.fedka2005.messages.*;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
/**
 * Class represents a node in the cluster of controllers
 * A group of ControllerWrapper instances is a cluster.
 * Target - synchronize a number of pox-controllers, choose
 * the master, and return address of the master controller.
 * When controller is chosen, others must monitor it's state.
 * @author fedor.goncharov.ol@gmail.com
 *
 */

//TODO
//fix bug with updating the cluster state
//after master is changed not everything being updated correctly

//TODO
//after new master has been chosen one must send message to all members, that they should
//update their mappings -> after that everything is clear

//TODO
//this class can not to know the path to pox controller

//TODO
//add License under which this program being distributed(fully open license)

public class ControllerWrapper implements Runnable {
	/**
	 * method creates the channel and connect process to the cluster
	 * performs monitoring of the master-node, and changes it's state
	 * if cpu-load there exceeds the specified limit.
	 * 
	 * @param controller - reference to controller instance
	 * @param groupName - cluster name
	 * @param groupAddress - cluster absolute address
	 * @param pName	- node unique name
	 * @param id - node unique identifier
	 * @param poxPath - path to pox-binary
	 * @param poxPort - port on which controller starts
	 * @throws Exception - //TODO generate good exception handling
	 */
	public ControllerWrapper(Controller controller,
			String groupName, String groupAddress, 
			String pName, 
			double cpuThreshold) throws ClientConstructorException {
		try {
			this.controller = controller;	//bound to controller
			this.groupName = groupName;
			this.groupAddress = groupAddress;
			this.pName = pName;
			this.logger = Logger.getLogger(ControllerWrapper.class);	//create logger
			cl_update = true;	//update mapping in the beginning
			this.mNotifications = new Stack<CPULoadRecord>();
			ControllerWrapper.cpuThreshold = cpuThreshold;
			
			
			channel = new JChannel(false);	//	create own protocol stack
			ProtocolStack stack = new ProtocolStack();
			channel.setProtocolStack(stack);
			
			stack.addProtocol(new UDP().setValue("bind_addr", InetAddress.getByName(groupAddress)))
						.addProtocol(new PING())	//broadcast udp	
						.addProtocol(new MERGE2())	//merge protocol
						.addProtocol(new MERGE3())	//another merging layer for safety
						.addProtocol(new FD_SOCK())
						.addProtocol(new FD_ALL().setValue("timeout",10000)
												 .setValue("interval",3000))	//heartbeat protocol, failure detection
						.addProtocol(new VERIFY_SUSPECT())
						.addProtocol(new BARRIER())
						.addProtocol(new NAKACK())
						.addProtocol(new UNICAST2())
						.addProtocol(new STABLE())
						.addProtocol(new GMS())
						.addProtocol(new UFC())
						.addProtocol(new MFC())
						.addProtocol(new FRAG2())
						.addProtocol(new CENTRAL_LOCK())
						.addProtocol(new COUNTER());
			stack.init();
			
			logger.info(pName + ":protocol stack initialized, bind address:" + groupAddress);
			
			channel.setName(pName);
			syncService = new CounterService(channel);	//master id atomic service
			lock_service = new LockService(channel);
			msg_disp = new MessageDispatcher(channel, 
				new MessageListener() {
				
				@Override
				public void setState(InputStream arg0) throws Exception {
					//empty method - initial state when connect
					//will be used in future versions
				}
				
				@Override
				public void receive(Message msg) {
					switch (RecvMessageHandler.getMessageType(msg)) {
					case RecvMessageHandler.CPU_NOTIFICATION : {
							mNotifications.push(RecvMessageHandler.getCPULoad(msg));
							ControllerWrapper.this.controller.printMessage(msg.copy());
							break;
					}
					case RecvMessageHandler.STOP : {
							int message_id = ((ClientStopMessage)msg.getObject()).id;
							if (message_id == ControllerWrapper.this.id) {
								stopClient();	//stop client
								sendStopGUI();
							}
							break;
					}
					case RecvMessageHandler.UNKNOWN : {
							logger.info(ControllerWrapper.this.pName + ": Unknown message type, " +
									"message:" + msg.toString());
							break;
						}
					}
				}
				
				@Override
				public void getState(OutputStream arg0) throws Exception {
					//empty method	- must read doc
					//will be used in future versions
				}
			}, new MembershipListener() {
					@Override
					public void viewAccepted(View newView) {
						clView = newView;
						//TODO
						//buggy place here
						if (firstConnect) {
							id = newView.size();
							master_id = syncService.getOrCreateCounter(master_counter_name, id);
							firstConnect = false;
						}
						cl_update = true;
					}
				
					@Override
					public void unblock() {
						//empty method
						//maybe useful in future
					}
				
					@Override
						public void suspect(Address addr) {
						logger.info(ControllerWrapper.this.pName + "Node:" + addr + " may have crushed.");
						//do nothing here
					}
				
					@Override
						public void block() {
						//empty method
						//maybe useful in future
					}
			}, new RequestHandler() {
					/**
					 * request type - RequestCPULoadMessage -> new CPULoadMessage
					 * request type - NodeInforRequest -> new NodeInfoResponse
					 * request type - unknown -> null
					 */
					@Override	//called after cpu-load request
					public Object handle(Message msg) throws Exception {
						if (msg.getObject() instanceof RequestCPULoadMessage)
							return new CPULoadMessage();
						if (msg.getObject() instanceof NodeInfoRequest) {
							//TODO
							//buggy place here, master id may be undefined or id undefined
							//nullpointer exception somewhere here
							return new NodeInfoResponse(ControllerWrapper.this.id, 
														ControllerWrapper.this.pName, 
														ControllerWrapper.this.groupAddress, 
														master_id.get() == ControllerWrapper.this.id ? true : false);
						}
						return null;	//should never get here
					}
			});
			logger.info(pName + ":JGroups message dispatcher and counter initialized");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(pName + ExceptionUtils.getStackTrace(e));
			throw new ClientConstructorException("Exception:client constructor failed, " +
					"message: " + e.getMessage());
		}
	}
	/**
	 * Dives current thread into loop, selecting monitoring and managing cluster model for
	 * a set of hosts with controllers activated on them.
	 * @throws Exception
	 */
	public void start() throws ClientStartException, JGroupsException {
		logger.info(pName + ":process started.");
		try {
			channel.connect(groupName);
		} catch (Exception e) {
			logger.error(pName + ":" + ExceptionUtils.getStackTrace(e));
			channel.close();
			e.printStackTrace();
			throw new ClientStartException("Exception: channel connection failed, " +
						"message:" + e.getMessage());
		}
		try {
			master_lock = lock_service.getLock(master_lock_name); //init lock - for atomic best master selection
			master_id = syncService.getOrCreateCounter(master_counter_name, id);
			if (cl_update) {
				updateNodeInfo();					//Map<Address, General info>
				cl_update = false;
			}
			eventLoop();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(pName + ExceptionUtils.getStackTrace(e));
			throw new ClientStartException("Exception: controller start method failed, " +
					"message:\n" + e.getMessage());
		} finally {
			channel.close();
		}
	}
	/**
	 * Node main loop: monitoring messages, replace master actions
	 */
	private void eventLoop() throws MasterReplaceException, 
									InterruptedException, 
									UpdateInfoException,
									JGroupsException {
		boolean controller_on = false;			//should we start controller, maybe the old one is still connected
		while (isActive) {
			controller_on = false;
			while (isActive && id == master_id.get()) {	//cpu-load notification for the cluster
				if (cl_update) {
					updateNodeInfo();
					cl_update = false;
				}
				if (!controller_on) {
					try {						//handling deadlocking-exception event
						master_lock.lock();		//concurrent rewriting excluded
						if (master_id.get() == id) {
							controller.startPOX();	//master starts pox controller in a seperate process
							controller_on = true;
						}
					} finally {	master_lock.unlock(); }
				}
				try {
					channel.send(new Message(null, new CPULoadMessage()));
				} catch (Exception e) {
					e.printStackTrace();
					logger.error(pName + ExceptionUtils.getStackTrace(e));
					throw new JGroupsException("Exception: channel send message failed, " +
							"message" + e.getMessage());
				}
				TimeUnit.SECONDS.sleep(SEND_DELAY);	//interval between message sendings
			}
			logger.info(pName + ":not a master.");
			controller.stopPOXController();		//stop controller if it was running
			boolean wait_stack = true;			//wait one iteration to get notification from master controller
			int local_master;
			while (isActive && (local_master = (int)master_id.get()) != id) {
				if (cl_update) {
					updateNodeInfo();
					cl_update = false;
				}
				if (!mNotifications.empty()) {
					CPULoadRecord record = mNotifications.pop();
					if (map_id.get(record.getAddress()) != null &&	
														map_id.get(record.getAddress()) == local_master && 
														record.getCPULoad() > cpuThreshold) {
						logger.info(pName + ":invoking master replacement process");
						replaceMaster(CPU_LOAD, local_master);
						wait_stack = true;
					} else {
						mNotifications.clear();
						wait_stack = true;
						TimeUnit.SECONDS.sleep(RECV_DELAY);
					}
				} else {
					if (wait_stack) {		//wait for message once more time
						wait_stack = false;
						TimeUnit.SECONDS.sleep(RECV_DELAY);
					} else {
						replaceMaster(EXCEEDTIME, local_master);
					}
				}
			}
			
		}
	}
	/**
	 * Replaces the master controller, call depends on reasons:
	 * exceeded notification await time, suspected for crush, high cpu-load on the node.
	 * @param code - reason of replacement(low perfomance, loss of connection)
	 * @param master_id who is the master at the moment, when called this method
	 * @throws MasterReplaceException
	 */
	private void replaceMaster(int code, int tmp_master_id) throws MasterReplaceException {
		try {
			master_lock.lock();	//lock access, write controller addr - synchronized event
			if (tmp_master_id == master_id.get()) {	//synchronized event
				Address master = null;
				for (Map.Entry<Address, Integer> entry : map_id.entrySet()) {
					if (tmp_master_id == entry.getValue()) {
						master = entry.getKey();
						break;
					}
				}
				RspList<CPULoadMessage> rsp_list = null;
				if (master != null) {
					rsp_list = msg_disp.castMessage(null,	//request cpu-load from nodes
							new Message(null, null, new RequestCPULoadMessage()),
							new RequestOptions(ResponseMode.GET_ALL, 0).setExclusionList(master)
					);
				} else {
					rsp_list = msg_disp.castMessage(null,	//request cpu-load from nodes
							new Message(null, null, new RequestCPULoadMessage()),
							new RequestOptions(ResponseMode.GET_ALL, 0)
					);
				}
				Address new_master = chooseMaster(rsp_list);
				master_id.set(map_id.get(new_master));
				//TODO
				//add notification that new master selected
				logger.info(pName + ":new master selected:" + map_id.get(new_master));
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(pName + ":" + ExceptionUtils.getStackTrace(e));
			throw new MasterReplaceException("Exception: master replacement failed, " +
					"message:\n" + e.getMessage());	//failed to send cpu-load request 
		} finally {
			master_lock.unlock();	//always unlock
		}
	}
	/**
	 * blocks current thread and asks other members for their ID, name and other data.
	 * @return HashMap<Integer,Address> 
	 * @throws UpdateInfoException
	 */
	public void updateNodeInfo() throws UpdateInfoException {
		try {
			RspList<NodeInfoResponse> id_rsp = msg_disp.castMessage(null, //request all for id's
					new Message(null, new NodeInfoRequest()), 
					new RequestOptions(ResponseMode.GET_ALL, 0));
			if (id_rsp != null) {
				Map<Address, NodeInfoResponse> info_output = new HashMap<Address, NodeInfoResponse>();
				Map<Address, Integer> map_output = new HashMap<Address, Integer>();
				for (Address address : id_rsp.keySet()) {
					map_output.put(
							address,
							((NodeInfoResponse)id_rsp.getValue(address)).id
							);
					info_output.put(
							address, 
							((NodeInfoResponse)id_rsp.getValue(address))
							);
				}
				info_mapping = info_output;
				map_id = map_output;
				controller.printConnectedNodes(info_mapping);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(pName + ExceptionUtils.getStackTrace(e));
			throw new UpdateInfoException("Exception : generateMapping() : failed to generate mapping:\n" +
					e.toString());
		}
	}
	
	/**
	 * Method sorts cpu-load from cluster nodes, the least loaded
	 * node becomes a new master.
	 * @param rsp_list - list of responses from the nodes
	 * @return	Address - Address class of the new master
	 */
	private Address chooseMaster(RspList<CPULoadMessage> rsp_list) {
		Address next_master = channel.getAddress();
		double cpu_load = ((CPULoadMessage)rsp_list.getValue(next_master)).cpuLoad;
		for (Address address : rsp_list.keySet()) {
			if (((CPULoadMessage)rsp_list.getValue(address)).cpuLoad < cpu_load) {
				next_master = address;
				cpu_load = ((CPULoadMessage)rsp_list.getValue(address)).cpuLoad;
			}
		}
		return next_master;
	}
	//static error codes
	private static final int EXCEEDTIME = 200;
	private static final int CPU_LOAD = 201;
	//private static final int CRASH_SUSPECT = 203;	//now unused but maybe in future will be helpful
	private static double cpuThreshold = 0.9;		//cpu-load threshold for node
	private Controller controller = null;
	
	//dynamic
	private JChannel channel = null;
	@SuppressWarnings("unused")
	private View clView;						//(currentView) will be required later
	
	private CounterService syncService = null;
	private Counter master_id = null;			//atomic service for managing master-id
	private Integer id = null;							//node id
	private Map<Address, Integer> map_id = null;
	private Map<Address, NodeInfoResponse> info_mapping = null;
	private Stack<CPULoadRecord> mNotifications = null;
	private LockService lock_service = null;
	private Lock master_lock = null;				//lock when change controller
	private MessageDispatcher msg_disp = null;	//synchrounous req-response cpu-load
	private boolean cl_update = true;	//cluster mapping shoudl be updated(yes/no?)
	
	//config
	private String groupAddress;				//cluster absolute address
	private String groupName;					//cluster unique idendifier
	private String pName;						//personal name
	private boolean isActive = true;			//node works
	private boolean firstConnect = true;
	public Logger logger = null;				//logger
	private static final String master_lock_name = "MASTER_LOCK";
	private static final String master_counter_name = "MASTER_COUNTER";
	public static final int SEND_DELAY = 1;	//send delay in SECONDS between CPU-LOAD notifications
	public static final int RECV_DELAY = 2; //recieve delay SECONDS between CPU-LOAD notifications
	
	/**
	 * Internal method to stop the node on the next iteration of the life-loop
	 */
	public void stopClient() {
		isActive = false;	//exit loops stop client
	}
	/**
	 * Recieved a stop message, client invokes stop GUI method
	 */
	public void sendStopGUI() {
		controller.stopGUI();
	}
	
	/**
	 * Sends message broadcastly to detach the specified client. Specified by unique id-value;
	 * @param id
	 */
	public void detachClient(int id) throws DetachNodeException {
		for (Address address : map_id.keySet()) {
			Integer client_id = map_id.get(address);
			if (id == client_id) {
				try {
					channel.send(new Message(null, new ClientStopMessage(id)));
				} catch (Exception e) {
					e.printStackTrace();
					logger.error(pName + ExceptionUtils.getStackTrace(e));
					throw new DetachNodeException("Exception: detaching client failed, message" +
							e.getMessage());
				}
			}
		}
	}
	@Override
	public void run() {
		try {
			this.start();
		} catch (Exception e) {
			e.printStackTrace();
			channel.close();	//close channel, free resources - closing channel doesn't throw any exceptions
			controller.forwardException(e);
			Thread.currentThread().interrupt();	//kill current thread
		}
	}
}
