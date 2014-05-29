package ru.mail.fedka2005.exceptions;

/**
 * Thrown, when client entered wrong path to the POX controller.
 * @author fedor
 *
 */
@SuppressWarnings("serial")
public class POXInitException extends Exception {
	public POXInitException() {}
	
	public POXInitException(String message) {
		super(message);
	}

}
