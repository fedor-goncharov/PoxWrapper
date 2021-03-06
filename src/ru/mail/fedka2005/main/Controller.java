package ru.mail.fedka2005.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import org.jgroups.Address;
import org.jgroups.Message;


import ru.mail.fedka2005.exceptions.ClientConstructorException;
import ru.mail.fedka2005.exceptions.DetachNodeException;
import ru.mail.fedka2005.exceptions.UpdateInfoException;
import ru.mail.fedka2005.exceptions.MalformedInputException;
import ru.mail.fedka2005.exceptions.POXInitException;
import ru.mail.fedka2005.gui.ControllerWrapperGUI;
import ru.mail.fedka2005.messages.NodeInfoResponse;
import ru.mail.fedka2005.objects.ControllerWrapper;

/**
 * Part of MVC(Model-View-Controller Application) -- binds graphical user
 * interface(ControllerWrapperGUI) and business logic(ControllerWrapper).
 * Executes POX process and also kills controller, when required. Singleton class.
 * @author fedor.goncharov.ol@gmail.com
 */
public class Controller {

	private ControllerWrapperGUI gui = null;	//gui
	public ControllerWrapper instance = null;	//business-logic
	
	private Process poxProcess = null;
	private String poxPath = null;
	
	/**
	 * Button generated event, creates another thread running all logic and synchronization.
	 * @param nodeName logical name of the node(not unique among cluster)
	 * @param groupName name of the target cluster, connect to
	 * @param poxPath path to POX folder
	 * @param groupAddress ip-address to which the client will be binded
	 * @param cpuThreshold highest cpu-load master can have without being replaced
	 */
	public void startClient(String nodeName, String groupName, 
			String poxPath, String groupAddress, 
			double cpuThreshold) throws MalformedInputException, ClientConstructorException {
		this.poxPath = poxPath;
		try {
			instance = new ControllerWrapper(this,
					groupName, 
					groupAddress, 
					nodeName, 
					cpuThreshold);
			Thread appProcess = new Thread(instance);
			appProcess.start();		//process started in another thread
		} catch (NumberFormatException e) {
			forwardException(new MalformedInputException("Exception: malformed input, " +
					"message: " + e.getMessage()));
		}
	}
	/**
	 * Invokes stop the bussiness logic, stop POX controller
	 */
	public void stopClient() {
		stopPOXController();
		long time_m = System.currentTimeMillis();
		long hours  = ((time_m / (1000*60*60)) % 24);
		long minutes = ((time_m / (1000*60)) % 60);
		long seconds = ((time_m / (1000)) % 60);
		long dec_seconds = ((time_m / 10) % 100);
		instance.logger.info("POX stopped : " + hours + ":" + minutes + 
				":" + seconds + ":" + dec_seconds);	//write time to log-file
		instance.logger.info("Full time:" + time_m);
		instance.stopClient();
	}
	/**
	 * Prints a new message from the cluster to GUI messageTable
	 * @param Message msg - JGroups class, containing destination, source, serialized object
	 */
	public void printMessage(Message msg) {
		gui.addMessageRecord(msg);
	}
	/**
	 * binding gui and controller object
	 * @param ControllerWrapperGUI - class instance
	 */
	public void setGUI(ControllerWrapperGUI gui) {
		this.gui = gui;		
	}
	/**
	 * Forward exception from ControllerWrapper to gui.
	 * @param e to be forwarded to some GUI methods
	 */
	public void forwardException(Exception e) {
		gui.handleInternalException(e);
	}
	/**
	 * Start POX controller(invoked when this client is a master).
	 * Starts with components selected in POX configuration
	 */
	public void startPOX() {
		ArrayList<String> cmdList = new ArrayList<String>();
		cmdList.add("python"); cmdList.add("pox.py");
		cmdList.add("timer");	//for tests only
		if (gui.poxComponentsSelected != null) {
			cmdList.addAll(gui.poxComponentsSelected);
		}
		ProcessBuilder pBuilder = new ProcessBuilder(cmdList);
		try { 
			pBuilder.directory(new File(poxPath));
			poxProcess = pBuilder.start();
			
		} catch (Exception e) {
			forwardException(new POXInitException("Exception:failed to invoke POX-Controller:\n"
						+ e.getMessage()));
		}
	}
	/**
	 * ControllerWrapper class calls this method to send information about all the nodes
	 * to gui class.
	 * @param content map, containing info about id, name, address
	 */
	public void printConnectedNodes(Map<Address, NodeInfoResponse> content) {
		gui.updateNodeInfo(content);
	}
	
	
	/**
	 * Method called from GUI to detach selected node from cluster. After calling thread
	 * sends a special message to the target node - as an order to terminate. No response is sent,
	 * message delivery is unreliable(because all messaging is handled by UDP)
	 * @param id - identificator of the node to be detached
	 */
	public void detachSelectedNode(int id) {
		try {
			instance.detachClient(id);
		} catch (DetachNodeException e) {
			forwardException(e);
		}
	}
	/**
	 * Target client broadcastly requires info about all the nodes(name, cluster, address, master - yes,no) 
	 * Client is blocked until all the messages are recieved.  
	 */
	public void refreshNodes() {
		try {
			instance.updateNodeInfo();
		} catch (UpdateInfoException e) {
			forwardException(e);			
		}
	}
	
	/**
	 * Stop POX controller(invoked when client is shifted from the master position)
	 */
	public void stopPOXController() {
		if (poxProcess != null) {
			poxProcess.destroy();	//kill the controller process
		}
	}
	/**
	 * Stop GUI, unblock buttons and fields. Clean the fields and tables.
	 */
	public void stopGUI() {
		gui.stopGUI();
	}
	
}
