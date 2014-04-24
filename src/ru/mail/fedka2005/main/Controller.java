package ru.mail.fedka2005.main;

import org.jgroups.Message;

import ru.mail.fedka2005.exceptions.ClientConstructorException;
import ru.mail.fedka2005.exceptions.MalformedInputException;
import ru.mail.fedka2005.gui.ControllerWrapperGUI;
import ru.mail.fedka2005.objects.ControllerWrapper;
/**
 * Part of MVC(Model-View-Controller Application) -- binds graphical user
 * interface(ControllerWrapperGUI) and business logic(ControllerWrapper).
 * It's a singleton class.
 * @author fedor
 *
 */
public class Controller {

	private ControllerWrapperGUI gui = null;	//gui
	private ControllerWrapper instance = null;	//business-logic
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
					1,	//TODO - generate identifier from nodeName
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
		//TODO
		//add action on event of connect from client Listener
	}
	/**
	 * Button generated event
	 */
	public void stopClient() {
		instance.stopClient();
	}
	/**
	 * Prints a new message from the cluster to gui table
	 */
	public void printMessage(Message msg) {
		gui.addRecord(msg);
	}
	/**
	 * binding gui and controller object
	 */
	public void setGUI(ControllerWrapperGUI gui) {
		this.gui = gui;		
	}
}
