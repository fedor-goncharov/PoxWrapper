package ru.mail.fedka2005.exceptions;

/**
 * Thrown when replace algorithm fails: could be due lock fails, dispatcher
 * failures, customization classes, when working with RSP lists
 * @author fedor
 *
 */
public class MasterReplaceException extends Exception {
	public MasterReplaceException() {}
	
	public MasterReplaceException(String message) {
		super(message);
	}
}
