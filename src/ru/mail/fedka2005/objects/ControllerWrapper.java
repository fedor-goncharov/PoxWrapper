package ru.mail.fedka2005.objects;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.Address;
import org.jgroups.blocks.atomic.Counter;
import org.jgroups.blocks.atomic.CounterService;
import org.jgroups.blocks.locking.LockService;
/**
 * Class represents a domain in the cluster of controllers
 * A group of ControllerWrapper instances gives a cluster.
 * Target - synchronize a number of pox-controllers, choose
 * the master, and return address of the master controller.
 * Another task, when controller is chosen, one must monitor it's
 * state.
 * @author fedor.goncharov.ol@gmail.com
 *
 */
public class ControllerWrapper implements Runnable {
	public ControllerWrapper(String groupName, String groupAddress, String pName, long id,
			String poxPath, int poxPort) throws Exception {
		try {
			this.groupName = groupName;
			this.groupAddress = groupAddress;
			this.pName = pName; this.id = id;
			this.poxPath = poxPath; this.poxPort = poxPort;
		} catch (Exception e) {
			throw new Exception("ControllerWrapper constructor");
		}
	}
	
	public void start() throws Exception {
		try {
			channel = new JChannel(groupAddress);
			channel.setName(pName);
			lock_service = new LockService(channel);
			id_service = new CounterService(channel);
			channel.connect(groupName); isActive = true;
			/*
			 * process received messages, new connections
			 */
			channel.setReceiver(new ReceiverAdapter() {
				public void receive(Message mesg) {
					//TODO - logging
					System.out.println("NodeID:" + channel.getAddressAsUUID() + 
							"\nMessage:" + mesg.toString());
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
						//check if it was master
				}
			});
			masterIDCounter = id_service.getOrCreateCounter("master_id", id);	//try node as master
			//cpu-load notification for the whole cluster
			while (masterIDCounter.get() == id) {
				channel.send(new Message(null, new CPULoadMessage()));
				TimeUnit.SECONDS.sleep(DELAY);
			}
			//
			
			
			//TODO
			//choose master
		} catch (Exception e) {
			throw new Exception("ControllerWrapper.start(), message:" + e.toString());
		} finally {
			try {
				if (channel != null) {channel.close();}
			} catch (Exception e) {};
		}
		//TODO
		//add listeners
		
	}
	
	private void replaceMaster() {
		Lock master_lock = lock_service.getLock("master");
		try {
			master_lock.lock();
			//ask cpu load for all cluster members
			//swap current master with best selection and notify others
		} finally {
			master_lock.unlock();
		}
		//TODO
		//implement replace the master
	}
	//dynamic variables
	private JChannel channel = null;
	private View clView;
	private LockService lock_service = null;
	private CounterService id_service = null;
	private Counter  masterIDCounter = null;
	private long masterID = 0;	
	private long id;							//node id
	
	//config, static variables
	private String groupAddress;
	private String groupName;
	private String pName;
	private boolean isActive = false;
	private int poxPort;
	private String poxPath;
	/**
	 * DELAY - each DELAY seconds master broadcasts cpu load
	 */
	public static int DELAY = 2;
	
	public String getpName() {
		return pName;
	}

	public boolean isActive() {
		return isActive;
	}

	public int getPoxPort() {
		return poxPort;
	}

	public void setPoxPort(int poxPort) {
		this.poxPort = poxPort;
	}

	public String getPoxPath() {
		return poxPath;
	}

	public void setPoxPath(String poxPath) {
		this.poxPath = poxPath;
	}
	
	public long getId() {
		return id;
	}
	
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
