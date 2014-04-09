package ru.mail.fedka2005.messages;

import org.jgroups.Message;

public class RecvMessageHandler {
	public static final RecvMessageHandler instance = new RecvMessageHandler();
	public static final int CPU_NOTIFICATION = 100;
	public static final int CPU_REQUEST = 101;
	public static final int UNKNOWN = 0;
	
	public int getMessageType(Message msg) {
		Object obj = msg.getObject();
		if (obj instanceof CPULoadMessage) {
			return CPU_NOTIFICATION;
		}
		if (obj instanceof RequestCPULoadMessage) {
			return CPU_REQUEST;
		}
		return UNKNOWN;
	}
	public double getCPULoad(Message msg) throws ClassCastException {
		return ((CPULoadMessage)msg.getObject()).cpuLoad;
	}
}
