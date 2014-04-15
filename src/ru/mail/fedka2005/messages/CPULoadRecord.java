package ru.mail.fedka2005.messages;
import org.jgroups.Address;
/**
 * Record generated when a notificaton from master-controller came.
 * Slave-controllers monitor master throuhg these messages and decide when master
 * must be replaced.
 * @author fedor
 *
 */
public class CPULoadRecord {
	private Address src;
	private double cpuLoad;
	
	public CPULoadRecord(Address addr, double load) {
		src = addr; cpuLoad = load;
	}
	
	public Address getAddress() { return src;}
	public double getCPULoad() { return cpuLoad;}
}
