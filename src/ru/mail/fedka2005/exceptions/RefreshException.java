package ru.mail.fedka2005.exceptions;

/**
 * Thrown when failed to refresh node state.
 * @author fedor.goncharov.ol@gmail.com
 *
 */
@SuppressWarnings("serial")
public class RefreshException extends Exception {
	public RefreshException() {};
	
	public RefreshException(String message) {
		super(message);
	}

}
