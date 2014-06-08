package ru.mail.fedka2005.main;

import org.apache.log4j.PropertyConfigurator;

import ru.mail.fedka2005.gui.ControllerWrapperGUI;


public class WrapperApplication {

	/**
	 * Application provides clustering service for POX controller.
	 * Application implements master/slave model, by monitoring 
	 * master for failures and replacing one if required.
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.setProperty("java.net.preferIPv4Stack", "true");	//connect node via IPv4, no IPv6
		PropertyConfigurator.configure("properties/log4j.properties");	//enable logging settings
		
		ControllerWrapperGUI gui = new ControllerWrapperGUI();	//create GUI
		Controller controller = new Controller();				//create Controller from MVC-pattern
		gui.setController(controller);							//bound objects
		controller.setGUI(gui);
	}
}
