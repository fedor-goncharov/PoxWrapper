package ru.mail.fedka2005.exceptions;

/**
 * Thrown when request-response in cluster failed
 * @author fedor
 *
 */
@SuppressWarnings("serial")
public class UpdateInfoException extends Exception {
	public UpdateInfoException() {}
	
	public UpdateInfoException(String message) {
		super(message);
	}
}
