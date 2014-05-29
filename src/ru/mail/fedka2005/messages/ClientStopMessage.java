package ru.mail.fedka2005.messages;

import java.io.Serializable;

/**
 * Empty class, notification that the dst-node must terminate 
 * it's work
 * @author fedor
 *
 */
@SuppressWarnings("serial")
public class ClientStopMessage implements Serializable {
	public int id;
	
	public ClientStopMessage(int id) {
		this.id = id;
	}
}
