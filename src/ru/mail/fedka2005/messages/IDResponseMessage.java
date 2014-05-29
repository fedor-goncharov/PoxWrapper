package ru.mail.fedka2005.messages;

import java.io.Serializable;
/**
 * Response message for node ID request
 * @author fedor.goncharov.ol@gmail.com
 *
 */
@SuppressWarnings("serial")
public class IDResponseMessage implements Serializable {
	/**
	 * 
	 */
	public Integer id = null;
	public IDResponseMessage(Integer id) {
		this.id = new Integer(id);
	}
}
