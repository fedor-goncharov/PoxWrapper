package ru.mail.fedka2005.gui;

import javax.swing.JFrame;
import javax.swing.SpringLayout;
import javax.swing.JButton;

/**
 * Frame, where client can choose a number of components to be invoked when
 * controller starts.
 * @author fedor.goncharov.ol@gmail.com
 *
 */

//TODO
//implement choosing number of components
@SuppressWarnings("serial")
public class POXComponentsGUI extends JFrame {
	public POXComponentsGUI() {
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		JButton btnApply = new JButton("Apply");
		springLayout.putConstraint(SpringLayout.SOUTH, btnApply, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, btnApply, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(btnApply);
		
	}
	
	//number of avalable pox components 
	private static final String[] components = { 
		"py", "forwarding.hub", "forwarding.l2_learning", "forwarding.l2_pairs",
		"forwarding.l3_learning", "forwarding.l2_multi", "forwarding.l2_nx", "forwarding.topo_proactive",
		"openflow.spanning_tree", "openflow.webservice", "web.webcore",	"messenger",
		"openflow.of_01", "openflow.discovery", "openflow.debug", "openflow.keepalive",
		"proto.pong", "proto.arp_responder", "info.packet_dump", "proto.dns_spy",
		"proto.dhcp_client", "proto.dhcpd", "misc.of_tutorial", "misc.full_payload", 
		"misc.mac_blocker", "misc.nat", "misc.ip_loadbalancer", "misc.gephi_topo",
		"log", "log.color", "log.level", "samples.pretty_log", "tk",
		"host_tracker", "datnapaths.pcap_switch"
	};
}
