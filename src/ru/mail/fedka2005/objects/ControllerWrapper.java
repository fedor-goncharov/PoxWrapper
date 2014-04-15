package ru.mail.fedka2005.objects;
import java.util.EmptyStackException;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.Address;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.atomic.Counter;
import org.jgroups.blocks.atomic.CounterService;
import org.jgroups.blocks.locking.LockService;
import org.jgroups.util.RspList;

import ru.mail.fedka2005.messages.*;
/**
 * Class represents a node in the cluster of controllers
 * ghjA group of ControllerWrapper instances is a cluster.
 * Target - synchronize a number of pox-controllers, choose
 * the master, and return address of the master controller.
 * When controller is chosen, others must monitor it's state.
 * @author fedor.goncharov.ol@gmail.com
 *
 */

//TODO
//add log4j for logging all events
//replace sleeping with time-scheduler
//generate mapping between addresses and id's - Map<id, Address>

public class ControllerWrapper implements Runnable {
	/**
	 * method creates the channel and connect process to the cluster
	 * performs monitoring of the master-node, and changes it's state
	 * if cpu-load there exceeds the specified limit.
	 * 
	 * @param groupName - cluster name
	 * @param groupAddress - cluster absolute address
	 * @param pName	- node unique name
	 * @param id - node unique identifier
	 * @param poxPath - path to pox-binary
	 * @param poxPort
	 * @throws Exception
	 */
	public ControllerWrapper(String groupName, String groupAddress, String pName, long id,
			String poxPath, int poxPort, double cpuThreshold) throws Exception {
		try {
			this.groupName = groupName;
			this.groupAddress = groupAddress;
			this.pName = pName; this.id = id;
			this.poxPath = poxPath; this.poxPort = poxPort;
			this.mNotifications = new Stack<CPULoadRecord>();
			ControllerWrapper.cpuThreshold = cpuThreshold;
			
			channel = new JChannel(groupAddress);
			channel.setName(pName);
			id_service = new CounterService(channel);	//master id atomic service
			lock_service = new LockService(channel);
			msg_disp = new MessageDispatcher(channel, null, null, new RequestHandler() {

				/**
				 * request type - RequestCPULoadMessage -> new CPULoadMessage
				 * request type - unknown -> null
				 */
				@Override	//called after cpu-load request
				public Object handle(Message msg) throws Exception {
					if (msg.getObject() instanceof RequestCPULoadMessage) {
						return new CPULoadMessage();
					} else {
						return null;
					}
				}
			});
			channel.setReceiver(new ReceiverAdapter() {
				public void receive(Message msg) {
					switch (RecvMessageHandler.getMessageType(msg)) {
						case RecvMessageHandler.CPU_NOTIFICATION : {
							if (mNotifications.size() > 0) mNotifications.clear();
							mNotifications.push(RecvMessageHandler.getCPULoad(msg));
							break;
						}
						case RecvMessageHandler.UNKNOWN : {
							//TODO
							//print message - UNKNOWN type of message
							//throw Exception
						}
					}
					
					System.out.println("NodeID:" + channel.getAddressAsUUID() + 
							"\nMessage:" + msg.toString());
					//TODO
					//process message
				}
				public void viewAccepted(View newView) {
					clView = newView;
					//update mapping Map<address, id>
				}
				public void suspect(Address addr) {
					System.out.println("Member:" + addr.toString() + " may have crushed.");
					//TODO
					//process this suspicious event
						//check if it was master and replace if required
				}
			});
		} catch (Exception e) {
			throw new Exception("Client constructor exception.");
		}
	}
	/**
	 * Creates 
	 * @throws Exception
	 */
	public void start() throws Exception {
		try {
			channel.connect(groupName); 
			isActive = true;	//init connection 
			masterLock = lock_service.getLock(master_lock); //init lock - for atomic best master selection
			
			eventLoop(channel, id_service.getOrCreateCounter("master", id), masterLock);
		} catch (Exception e) {
			throw new Exception("ControllerWrapper.start(), message:" + e.toString());
		} finally {
			try {
				if (channel != null) {channel.close();}
			} catch (Exception e) {};
		}
	}
	/**
	 * Node main loop, monitoring messages, and switching master between nodes
	 * appropirately.
	 * @param channel
	 * @param masterID
	 * @param masterLock
	 * @throws Exception
	 */
	private void eventLoop(JChannel channel, Counter masterID, Lock masterLock) 
			throws Exception {
		boolean rewrite = false;	//to switch: ovs-vsctl set-controller (me)
		while (isActive) {
			while (masterID.get() == id) {	//cpu-load notification for the cluster
				if (!rewrite) {
					masterLock.lock();		//concurrent rewriting excluded
						if (masterID.get() == id) {
							//TODO
							//for all ovs's set-controller id(me)
							//send message to 
							rewrite = true;
						}
					masterLock.unlock();
				}
				channel.send(new Message(null, new CPULoadMessage()));
				TimeUnit.SECONDS.sleep(SEND_DELAY);
			}
			rewrite = false;
			boolean wait_stack = true;	//wait one iteration to get notification from master controller
			while (masterID.get() != id) {
				try {
					CPULoadRecord record = mNotifications.pop();
					if (record.getCPULoad() > cpuThreshold) {	//if cpu-load is too-high -> replace master
						replaceMaster(masterID, masterLock, CPU_LOAD);
						wait_stack = true;
						continue;
					}
				} catch (EmptyStackException e) {
					if (wait_stack) {
						TimeUnit.SECONDS.sleep(RECV_DELAY);
						wait_stack = false;
						continue;
					} else {
						replaceMaster(masterID, masterLock, CPU_LOAD);
						continue;
					}
				}
			}
		}
	}
	/**
	 * replaces the master controller, depends on the reason of replacement:
	 * exeeded notification await time, suspected for crush, high cpu-load on the node.
	 * @param masterID - service for atomic master-controller change
	 * @param masterLock - 
	 * @param code - reason of 
	 */
	private void replaceMaster(Counter masterID, Lock masterLock, int code) throws Exception {
		try {
			masterLock.lock();	//lock access, write controller addr - synchronized
			RspList<CPULoadMessage> rsp_list = msg_disp.castMessage(null,	//list of responses
					new Message(null, null, new RequestCPULoadMessage()),
					new RequestOptions(ResponseMode.GET_ALL, 0).setExclusionList(/*exclude master*/));
		} catch (Exception e) {
			throw new Exception();	//failed to send cpu-load request 
		} finally {
			masterLock.unlock();
		}
		switch (code) {
			case EXCEEDTIME :	
				break;
			case CPU_LOAD : 
				break;
			case CRASH_SUSPECT : 
				break;
			default : //throw UnknowTypeofReplacement
		masterLock.unlock();
		}
	}
			
	//static
	private static final int EXCEEDTIME = 200;
	private static final int CPU_LOAD = 201;
	private static final int CRASH_SUSPECT = 203;
	private static double cpuThreshold = 90;	//maximum cpu load% for master
	
	//dynamic
	private JChannel channel = null;
	private View clView;						//will be required later
	private CounterService id_service = null;
	private long id;							//node id
	private Stack<CPULoadRecord> mNotifications = null;
	private LockService lock_service = null;
	private Lock masterLock = null;				//lock when change controller
	private MessageDispatcher msg_disp = null;	//synchrounous req-response cpu-load
	
	
	//config
	private String groupAddress;			//cluster absolute address
	private String groupName;				//cluster unique idendifier
	private String pName;					//personal name
	private boolean isActive = false;
	private int poxPort;
	private String poxPath;
	private static final String master_lock = "MASTER_LOCK"; 
	public static final int SEND_DELAY = 2;	//send delay in seconds between cpu-load notifications
	public static final int RECV_DELAY = 3; //recieve delay in seconds between cpu-load notifications
	
	//TODO
	//add getters and setters, later
	
	@Deprecated
	@Override
	public void run() {
		try {
			this.start();
		} catch (Exception e) {
			System.out.println(e.toString());
			System.exit(1);
		}
	}
}
