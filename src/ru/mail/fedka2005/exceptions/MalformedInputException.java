package ru.mail.fedka2005.exceptions;

/**
 * Thrown, when user invokes connection to cluster, having not entered all required
 * data into the fields.
 * @author fedor
 *
 */
public class MalformedInputException extends Exception {
	public MalformedInputException() {}

	public MalformedInputException(String message) {
		super(message);
	}

}
