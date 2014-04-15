package ru.mail.fedka2005.messages;
import java.io.Serializable;
import java.lang.management.ManagementFactory;

import com.sun.management.OperatingSystemMXBean;

/**
 * Class intended for monitoring purposes of the master-controller.
 * Sends only cpu-load on the node.
 * @author fedor
 *
 */
public class CPULoadMessage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7828421234707335812L;
	public double cpuLoad;
	
	public CPULoadMessage() {
		OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(
				OperatingSystemMXBean.class);
		cpuLoad = osBean.getSystemCpuLoad();
	}
}
