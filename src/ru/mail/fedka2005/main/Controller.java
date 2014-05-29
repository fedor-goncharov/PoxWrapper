package ru.mail.fedka2005.main;

import java.io.File;
import java.util.Map;
import java.util.Scanner;

import org.jgroups.Address;
import org.jgroups.Message;


import ru.mail.fedka2005.exceptions.ClientConstructorException;
import ru.mail.fedka2005.exceptions.MalformedInputException;
import ru.mail.fedka2005.exceptions.POXInitException;
import ru.mail.fedka2005.exceptions.RefreshException;
import ru.mail.fedka2005.gui.ControllerWrapperGUI;
import ru.mail.fedka2005.messages.NodeInfoResponse;
import ru.mail.fedka2005.objects.ControllerWrapper;
/**
 * Part of MVC(Model-View-Controller Application) -- binds graphical user
 * interface(ControllerWrapperGUI) and business logic(ControllerWrapper).
 * Executes POX process and also kills controller, when required.
 * It's a singleton class.
 * @author fedor
 */
public class Controller {

	private ControllerWrapperGUI gui = null;	//gui
	private ControllerWrapper instance = null;	//business-logic
	public Process poxProcess = null;
	/**
	 * Button generated event
	 */
	public void startClient(String nodeName, String groupName, 
			String poxPath, String groupAddress, 
			String cpuThreshold, String port) throws MalformedInputException, ClientConstructorException {
		try {
			//Read id
			//TODO
			//generate id from name, or give id when connecting(maybe get state)
			//generate id after connecting to the cluster and obtaining id's of all clients
			Scanner in = new Scanner(System.in);
			int id = in.nextInt();
			in.close();
			
			instance = new ControllerWrapper(this,
					groupName, 
					groupAddress, 
					nodeName, 
					id,	//TODO - generate identifier from nodeName, or generate identifier from cluster
					poxPath, 
					Integer.parseInt(port), 
					Double.parseDouble(cpuThreshold));
			Thread appProcess = new Thread(instance);
			appProcess.start();		//process started in another thread
		} catch (NumberFormatException ex) {
			throw new MalformedInputException("malformed input, message: " + ex.getMessage());
		}
	}
	/**
	 * Button generated event
	 */
	public void stopClient() {
		instance.stopClient();
		stopPOX();
	}
	/**
	 * Prints a new message from the cluster to gui table
	 * @param Message msg - JGroups class, containing destination, source, serialized object
	 */
	public void printMessage(Message msg) {
		gui.addRecord(msg);
	}
	/**
	 * binding gui and controller object
	 * @param ControllerWrapperGUI - class instance
	 */
	public void setGUI(ControllerWrapperGUI gui) {
		this.gui = gui;		
	}
	/**
	 * forward exception from ControllerWrapper to gui
	 * @param Exception to be forwarded to some GUI methods
	 */
	public void forwardException(Exception e) {
		gui.handleInternalException(e);
	}
	/**
	 * start POX controller(invoked when this client is a master)
	 * @param String poxPath - path to pox.py
	 * @param int poxPort - number of port, to which POX is attached 
	 */
	public void startPOX(String path, int port) {
		//TODO
		//start the pox controller, recieve a reference to it
		//catch exception if pox execution failed
		ProcessBuilder pBuilder = new ProcessBuilder("python","pox.py","openflow.of_01",
				"--port=" + String.valueOf(port));
		try { 
			pBuilder.directory(new File(path));
			poxProcess = pBuilder.start();
			
		} catch (Exception e) {
			forwardException(new POXInitException("Error invoking pox-controller:\n"
						+ e.getMessage()));
		}
	}
	/**
	 * ControllerWrapper class calls this method to send information about all the nodes
	 * to gui class.
	 * Parameters - list of specialized classes(or messages)
	 */
	public void printConnectedNodes(Map<Address, NodeInfoResponse> content) {
		System.out.println("gui called");
		gui.updateNodeInfo(content);
		System.out.println("gui called");
	}
	
	
	/**
	 * Method called from GUI to detach selected node from cluster. After calling thread
	 * sends a special message to the target node - as an order to terminate. No response is sent,
	 * message delivery is unreliable(because all messaging is handled by UDP)
	 */
	public void detachSelectedNode(int id) {
		instance.detachClient(id);
	}
	/**
	 * After user calls this method, target client broadcastly requires info about all the nodes(name, cluster, address, master - yes,no) 
	 * Target node is blocked untill all the messages are recieved.  
	 */
	public void refreshNodes() {
		try {
			instance.refreshInfo();
		} catch (RefreshException e) {
			forwardException(e);			
		}
	}
	
	/**
	 * Stop POX controller(invoked when client is shifted from the master position)
	 */
	public void stopPOX() {
		if (poxProcess != null) {
			poxProcess.destroy();	//kill the controller process
			poxProcess = null;
		}
	}
	/**
	 * Controller calls gui to stop
	 */
	public void stopGUI() {
		gui.stopGUI();
	}
	
}
