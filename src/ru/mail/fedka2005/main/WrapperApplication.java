package ru.mail.fedka2005.main;

import ru.mail.fedka2005.gui.ControllerWrapperGUI;


public class WrapperApplication {

	/**
	 * Application provides clustering service for POX controller.
	 * Application implements master/slave model, by monitoring 
	 * master for failures and replacing one if required.
	 * @param args
	 */
	public static void main(String[] args) {
		
		ControllerWrapperGUI gui = new ControllerWrapperGUI();	//create GUI
		Controller controller = new Controller();				//create Controller from MVC-pattern
		gui.setController(controller);							//bound objects
		controller.setGUI(gui);
	}
}
