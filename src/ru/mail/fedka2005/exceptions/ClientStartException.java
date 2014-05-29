package ru.mail.fedka2005.exceptions;

@SuppressWarnings("serial")
public class ClientStartException extends Exception{

public ClientStartException() {}
	
	public ClientStartException(String message) {
		super(message);
	}
}
