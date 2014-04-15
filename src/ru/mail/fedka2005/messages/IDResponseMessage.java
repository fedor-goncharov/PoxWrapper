package ru.mail.fedka2005.messages;

import java.io.Serializable;
/**
 * Response message for node ID request
 * @author fedor
 *
 */
public class IDResponseMessage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Integer id = null;
	public IDResponseMessage(Integer id) {
		this.id = new Integer(id);
	}
}
