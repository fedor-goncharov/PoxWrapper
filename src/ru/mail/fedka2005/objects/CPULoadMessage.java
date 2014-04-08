package ru.mail.fedka2005.objects;
import java.io.Serializable;
import java.lang.management.ManagementFactory;

import com.sun.management.OperatingSystemMXBean;

/**
 * Class intended for monitoring purposes of the master-controller.
 * Sends only cpu-usage on the node
 * @author fedor
 *
 */
public class CPULoadMessage implements Serializable {
	public double cpuLoad;
	
	public CPULoadMessage() {
		OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(
				OperatingSystemMXBean.class);
		cpuLoad = osBean.getSystemCpuLoad();
	}
}
