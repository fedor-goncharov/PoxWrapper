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
import ru.mail.fedka2005.exceptions.GenerateMappingException;
import ru.mail.fedka2005.exceptions.JGroupsException;
import ru.mail.fedka2005.exceptions.MasterReplaceException;
import ru.mail.fedka2005.exceptions.RefreshException;
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
			
			this.mNotifications = new Stack<CPULoadRecord>();
			ControllerWrapper.cpuThreshold = cpuThreshold;
			
			
			channel = new JChannel(false);	//	create own protocol stack
			ProtocolStack stack = new ProtocolStack();
			channel.setProtocolStack(stack);
			
			stack.addProtocol(new UDP().setValue("bind_addr", InetAddress.getByName(groupAddress))
									   .setValue("ip_ttl", 8)
									   .setValue("mcast_send_buf_size", 32000)
									   .setValue("ucast_recv_buf_size", 64000))
						.addProtocol(new PING())	//broadcast udp	
						.addProtocol(new MERGE2())	//merge protocol
						.addProtocol(new MERGE3())	//another merging layer for safety
						.addProtocol(new FD_SOCK())
						.addProtocol(new FD_ALL().setValue("timeout",12000)
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
						.addProtocol(new COUNTER())
						.addProtocol(new CENTRAL_LOCK());
			stack.init();
			
			logger.info("Protocol stack initialized, bind address:" + groupAddress);
			
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
							System.out.println("[INFO]: Unknown message type, " +
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
						if (firstConnect) {
							id = newView.size();
							firstConnect = false;
						}
						cl_mapping_update = true;
					}
				
					@Override
					public void unblock() {
						//empty method
						//maybe useful in future
					}
				
					@Override
						public void suspect(Address addr) {
						logger.info("Node:" + addr.toString() + " may have crushed.");
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
						if (msg.getObject() instanceof IDRequestMessage)
							return new IDResponseMessage(ControllerWrapper.this.id);	//buggy line
						if (msg.getObject() instanceof NodeInfoRequest)
							return new NodeInfoResponse(ControllerWrapper.this.id, 
														ControllerWrapper.this.pName, 
														ControllerWrapper.this.groupAddress, 
														masterID.get() == ControllerWrapper.this.id ? true : false);
						return null;	//should never get here
					}
			});
			logger.info("JGroups message dispatcher and counter initialized");
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
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
		logger.info("Client process started.");
		try {
			channel.connect(groupName);
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			channel.close();
			throw new ClientStartException("Exception: controller start method failed, " +
						"message:" + e.getMessage());
		}
		try {
			masterLock = lock_service.getLock(master_lock); //init lock - for atomic best master selection
			masterID = syncService.getOrCreateCounter(master_counter, id);
			if (cl_mapping_update) {
				cluster_mapping = generateMapping();			//Map<id,Address>	//private method
				info_mapping = refreshInfo();					//Map<Address, General info>
				cl_mapping_update = false;
			}
			eventLoop();
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
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
									GenerateMappingException,
									RefreshException,
									JGroupsException {
		boolean rewrite = false;			//to switch: ovs-vsctl set-controller (me)
		while (isActive) {
			rewrite = false;
			info_mapping = refreshInfo();
			while (isActive && masterID.get() == id) {	//cpu-load notification for the cluster
				if (cl_mapping_update) {
					cluster_mapping = generateMapping();
					info_mapping = refreshInfo();
					cl_mapping_update = false;
				}
				if (!rewrite) {
					try {						//handling deadlocking-exception event
						masterLock.lock();		//concurrent rewriting excluded
							if (masterID.get() == id) {
								controller.startPOX();	//master starts pox controller in a seperate process
								rewrite = true;
							}
					} finally {	masterLock.unlock(); }
				}
				try {
					channel.send(new Message(null, new CPULoadMessage()));
				} catch (Exception e) {
					logger.error(ExceptionUtils.getStackTrace(e));
					throw new JGroupsException("Exception: channel send message failed, " +
							"message" + e.getMessage());
				}
				TimeUnit.SECONDS.sleep(SEND_DELAY);
			}
			controller.stopPOXController();		//stop controller if it was running
			info_mapping = refreshInfo();
			logger.info("Not a master any more, controller stopped.");
			boolean wait_stack = true;			//wait one iteration to get notification from master controller
			int local_master;
			while (isActive && (local_master = (int)masterID.get()) != id) {
				if (cl_mapping_update) {
					cluster_mapping = generateMapping();
					info_mapping = refreshInfo();
					cl_mapping_update = false;
				}
				if (!mNotifications.empty()) {
					CPULoadRecord record = mNotifications.pop();
					if (cluster_mapping.get(record.getAddress()) != null &&
							cluster_mapping.get(record.getAddress()) == local_master && 
							record.getCPULoad() > cpuThreshold) {	//cpu-load exceeds the threshold -> replace master
						logger.info("invoking master replacement process");
						replaceMaster(CPU_LOAD, local_master);
						wait_stack = true;
					} else {
						mNotifications.clear();
						TimeUnit.SECONDS.sleep(RECV_DELAY);
					}
				} else {
					if (wait_stack) {
						TimeUnit.SECONDS.sleep(RECV_DELAY);
						wait_stack = false;
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
	private void replaceMaster(int code, int master_id) throws MasterReplaceException {
		try {
			masterLock.lock();	//lock access, write controller addr - synchronized event
			if (master_id == masterID.get()) {	//synchronized event
				Address master = null;
				for (Map.Entry<Address, Integer> entry : cluster_mapping.entrySet()) {
					if (master_id == entry.getValue()) {
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
				rsp_list.addRsp(channel.getAddress(), new CPULoadMessage());
				Address new_master = chooseMaster(rsp_list);
				masterID.set(cluster_mapping.get(new_master));
				logger.info("New master selected:" + cluster_mapping.get(new_master));
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw new MasterReplaceException("Exception: master replacement failed, " +
					"message:\n" + e.getMessage());	//failed to send cpu-load request 
		} finally {
			masterLock.unlock();
		}
	}
	/**
	 * blocks current thread and asks other members for their ID and JGroups.Address
	 * @return HashMap<Integer,Address> 
	 * @throws GenerateMappingException
	 */
	private Map<Address, Integer> generateMapping() throws GenerateMappingException {
		try {
			RspList<IDResponseMessage> id_rsp = msg_disp.castMessage(null, //request all for id's
					new Message(null, new IDRequestMessage()), 
					new RequestOptions(ResponseMode.GET_ALL, 0));
			Map<Address, Integer> output = new HashMap<Address, Integer>();
			for (Address address : id_rsp.keySet()) {
				output.put(
						address,
						((IDResponseMessage)id_rsp.getValue(address)).id
						);
			}
			output.put(channel.getAddress(), id);	//put own address
			return output;
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw new GenerateMappingException("Exception : generateMapping() : failed to generate mapping:\n" +
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
	private Counter masterID = null;			//atomic service for managing master-id
	private Integer id;							//node id
	private Map<Address, Integer> cluster_mapping = null;
	@SuppressWarnings("unused")
	private Map<Address, NodeInfoResponse> info_mapping = null;
	private Stack<CPULoadRecord> mNotifications = null;
	private LockService lock_service = null;
	private Lock masterLock = null;				//lock when change controller
	private MessageDispatcher msg_disp = null;	//synchrounous req-response cpu-load
	private boolean cl_mapping_update = true;	//cluster mapping shoudl be updated(yes/no?)
	
	//config
	private String groupAddress;				//cluster absolute address
	private String groupName;					//cluster unique idendifier
	private String pName;						//personal name
	private boolean isActive = true;			//node works
	private boolean firstConnect = true;
	public Logger logger = null;				//logger
	private static final String master_lock = "MASTER_LOCK";
	private static final String master_counter = "MASTER";
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
		for (Address address : cluster_mapping.keySet()) {
			Integer client_id = cluster_mapping.get(address);
			if (id == client_id) {
				try {
					channel.send(new Message(null, new ClientStopMessage(id)));
				} catch (Exception e) {
					logger.error(ExceptionUtils.getStackTrace(e));
					throw new DetachNodeException("Exception: detaching client failed, message" +
							e.getMessage());
				}
			}
		}
	}
	/**
	 * Send a broad cast message to all members, to update their personal info. Called when view 
	 * has changed to update the data;
	 * 
	 * @throws RefreshException - thrown when node failed to send broadcast request or
	 * handle some answers
	 */
	public Map<Address, NodeInfoResponse> refreshInfo() throws RefreshException {
		try {
			RspList<NodeInfoResponse> info_rsp = msg_disp.castMessage(null, //request all for id's
					new Message(null, new NodeInfoRequest()), 
					new RequestOptions(ResponseMode.GET_ALL, 0));
			
			Map<Address, NodeInfoResponse> output = new HashMap<Address, NodeInfoResponse>();
			for (Address address : info_rsp.keySet()) {
				output.put(
						address,
						((NodeInfoResponse)(info_rsp.getValue(address)))
						);
			}
			controller.printConnectedNodes(output);	
			return output;
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw new RefreshException("Exception : refreshInfo() : failed to refresh cluster state," +
					"message:\n" + e.toString());
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
