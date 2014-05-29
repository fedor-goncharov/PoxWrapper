package ru.mail.fedka2005.messages;

import java.io.Serializable;

/**
 * Class as a message, contains channel name, address, id of the node.
 * No methods, pure data only
 * @author fedor.goncharov.ol@gmail.com
 *
 */
@SuppressWarnings("serial")
public class NodeInfoResponse implements Serializable {
	public int id;
	public String name;
	public String address;
	public boolean master;
	
	/**
	 * Constructor
	 * @param id
	 * @param name
	 * @param clusterName
	 * @param address
	 */
	public NodeInfoResponse(int id, String name, String address, boolean master) {
		this.id = id;
		this.name = name;
		this.address = address;
		this.master = master;
	}

}
