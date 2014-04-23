package ru.mail.fedka2005.exceptions;

/**
 * thrown when, when ControllerWrapper initialization fails
 * @author fedor
 *
 */
public class ClientConstructorException extends Exception {
	public ClientConstructorException() {}
	
	public ClientConstructorException(String message) {
		super(message);
	}
}
