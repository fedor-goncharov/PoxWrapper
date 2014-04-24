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

	private ControllerWrapperGUI gui = null;
	private ControllerWrapper instance = null;
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
	 * Print a new message from the cluster to gui
	 */
	public void printMessage(Message msg) {
		gui.addRecord(msg);
		//TODO
		//call a method, invoking table data update and redrawing gui
	}
	//gui setter for controller
	public void setGUI(ControllerWrapperGUI gui) {
		this.gui = gui;		
	}
}
