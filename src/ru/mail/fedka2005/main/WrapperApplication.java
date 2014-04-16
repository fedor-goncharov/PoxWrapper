package ru.mail.fedka2005.main;

import ru.mail.fedka2005.objects.ControllerWrapper;

public class WrapperApplication {

	/**
	 * Application provides clustering service for POX
	 * controller. Application implements master/slave model,
	 * by monitoring master for failures and replacing one if required.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ControllerWrapper clientOne = new ControllerWrapper("test", "", "clientOne", 1, 
					"", 0, 0.99);
			ControllerWrapper clientTwo = new ControllerWrapper("test", "", "clientTwo", 2,
					"", 0, 0.99);
			Thread threadOne = new Thread(clientOne);
			Thread threadTwo = new Thread(clientTwo);
			threadOne.start();
			threadTwo.start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
