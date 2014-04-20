package ru.mail.fedka2005.main;


import java.util.concurrent.TimeUnit;

import ru.mail.fedka2005.objects.ControllerWrapper;

public class WrapperApplication {

	/**
	 * Application provides clustering service for POX
	 * controller. Application implements master/slave model,
	 * by monitoring master for failures and replacing one if required.
	 * @param args
	 */
	public static void main(String[] args) {
		Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
			
			@Override
			public void uncaughtException(Thread t, Throwable ex) {
				System.out.println("Uncaught exception: " + ex);
			}
		};
		try {
			ControllerWrapper clientOne = new ControllerWrapper("test", "", "clientOne", 1, 
					"", 0, 0.99);
			ControllerWrapper clientTwo = new ControllerWrapper("test", "", "clientTwo", 2,
					"", 0, 0.99);
			ControllerWrapper clientThree = new ControllerWrapper("test", "", "clientThree", 3,
					"", 0, 0.99);
			Thread threadOne = new Thread(clientOne);
			Thread threadTwo = new Thread(clientTwo);
			Thread threadThree = new Thread(clientThree);
			threadOne.setUncaughtExceptionHandler(handler);	//thread exception handling
			threadTwo.setUncaughtExceptionHandler(handler);
			threadThree.setUncaughtExceptionHandler(handler);
			
			threadOne.start();
			threadTwo.start();
			threadThree.start();
		} catch (Exception e) {
			System.out.println("Main caught exception");
			e.printStackTrace();
		}
	}

}
