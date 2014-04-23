package ru.mail.fedka2005.main;

import org.jgroups.Message;

import ru.mail.fedka2005.exceptions.MalformedInputException;
/**
 * Part of MVC(Model-View-Controller Application) -- binds graphical user
 * interface(ControllerWrapperGUI) and business logic(ControllerWrapper).
 * It's a singleton class.
 * @author fedor
 *
 */
public class Controller {

	/**
	 * Button generated event
	 */
	public void startClient(String nodeName, String groupName, 
			String poxPath, String address) throws MalformedInputException {
		//TODO
		//add action on event of connect from client Listener
	}
	/**
	 * Button generated event
	 */
	public void stopClient() {
		//TODO
		//add action on event of disconnect from cluster
	}
	/**
	 * Print a new message from the cluster to gui
	 */
	public void printMessage(Message msg) {
		//TODO
		//call a method, invoking table data update and redrawing gui
	}
}
