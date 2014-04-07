package ru.mail.fedka2005.objects;
import java.net.InetAddress;
import org.jgroups.JChannel;
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
	public ControllerWrapper(String groupName, String address, String pName,
			String poxPath, int poxPort) 
			throws Exception {
		try {
			this.groupName = groupName;
			this.groupAddress = InetAddress.getByName(address);
			this.pName = pName;
			this.poxPath = poxPath;
			this.poxPort = poxPort;
		} catch (Exception e) {
			throw new Exception("ControllerWrapper constructor");
		}
	}
	
	public void start() throws Exception {
		
		try {
			channel = new JChannel();
			//TODO
			//add url props when initializong JChannel
			channel.setName(pName);
			channel.connect(groupName);	//handle exception
			isActive = true;
		} catch (Exception e) {
			throw new Exception("ControllerWrapper.Start(), message:" + e.toString());
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
	private InetAddress groupAddress;
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
