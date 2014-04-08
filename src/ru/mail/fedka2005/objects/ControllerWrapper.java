package ru.mail.fedka2005.objects;

import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.Address;
/**
 * Class represents a domain in the cluster of controllers
 * A group of ControllerWrapper instances gives a cluster.
 * Target - synchronize a number of pox-controllers, choose
 * the master, and return address of the master controller.
 * Another task, when controller is chosen, one must monitor it's
 * state.
 * @author fedor
 *
 */

public class ControllerWrapper implements Runnable {
	public ControllerWrapper(String groupName, String groupAddress, String pName,
			String poxPath, int poxPort) throws Exception {
		try {
			this.groupName = groupName;
			this.groupAddress = groupAddress;
			this.pName = pName;
			this.poxPath = poxPath;
			this.poxPort = poxPort;
		} catch (Exception e) {
			throw new Exception("ControllerWrapper constructor");
		}
	}
	
	public void start() throws Exception {
		
		try {
			channel = new JChannel(groupAddress);
			//TODO
			//add url props when initializong JChannel
			
			channel.setName(pName);
			channel.connect(groupName);
			isActive = true;
			channel.setReceiver(new ReceiverAdapter() {
				public void receive(Message mesg) {
					System.out.println("NodeID:" + channel.getAddressAsUUID() + 
							"\nMessage:" + mesg.toString());
					//TODO
					//process message
				}
				public void viewAccepted(View new_view) {
					clView = new_view;
				}
				public void suspect(Address addr) {
					System.out.println("Member:" + addr.toString() + " may have crushed.");
					//TODO
					//process this suspicious event
				}
			});
			while (true) {
				channel.send(new Message(null, null, "HelloWorld"));
			}
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
	
	private JChannel channel = null;
	//private UUID uuid;	//unique identifier of the node
	private View clView;
	private String groupAddress;
	private String groupName;
	private String pName;
	private boolean isActive = false;
	private boolean isMaster = false;	//master-controller node node
	private int poxPort;
	private String poxPath;
	
	public String getpName() {
		return pName;
	}

	public boolean isActive() {
		return isActive;
	}

	public boolean isMaster() {
		return isMaster;
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
