package ru.mail.fedka2005.main;

import org.jgroups.Message;

import ru.mail.fedka2005.exceptions.ClientConstructorException;
import ru.mail.fedka2005.exceptions.MalformedInputException;
import ru.mail.fedka2005.gui.ControllerWrapperGUI;
import ru.mail.fedka2005.objects.ControllerWrapper;
/**
 * Part of MVC(Model-View-Controller Application) -- binds graphical user
 * interface(ControllerWrapperGUI) and business logic(ControllerWrapper).
 * Executes POX process and also kills controller, when required.
 * It's a singleton class.
 * @author fedor
 *
 */
public class Controller {

	private ControllerWrapperGUI gui = null;	//gui
	private ControllerWrapper instance = null;	//business-logic
	Process poxProcess = null;
	/**
	 * Button generated event
	 */
	public void startClient(String nodeName, String groupName, 
			String poxPath, String groupAddress, 
			String cpuThreshold, String port) throws MalformedInputException, ClientConstructorException {
		try {
			instance = new ControllerWrapper(this,
					groupName, 
					groupAddress, 
					nodeName, 
					1,	//TODO - generate identifier from nodeName, or generate identifier from cluster
					poxPath, 
					Integer.parseInt(port), 
					Double.parseDouble(cpuThreshold));
			Thread appProcess = new Thread(instance);
			appProcess.start();		//process started in another thread
		//TODO catch true exception
		//from cluster
		} catch (NumberFormatException ex) {
			throw new MalformedInputException("malformed input, message: " + ex.getMessage());
		}
	}
	/**
	 * Button generated event
	 */
	public void stopClient() {
		instance.stopClient();
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
		Runtime rt = Runtime.getRuntime();
		poxProcess = rt.exec("//TODO - add invoking pox");
		//handle exceptions
	}
	/**
	 * stop POX controller(invoked when client is shifted from the master position)
	 */
	public void stopPOX() {
		//TODO
		//stop the controller(kill the process)
	}
	
}
