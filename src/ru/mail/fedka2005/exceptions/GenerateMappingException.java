package ru.mail.fedka2005.exceptions;

/**
 * Thrown when request-response in cluster failed
 * @author fedor
 *
 */
public class GenerateMappingException extends Exception {
	public GenerateMappingException() {}
	
	public GenerateMappingException(String message) {
		super(message);
	}
}
