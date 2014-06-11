package ru.mail.fedka2005.gui;


import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.SpringLayout;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JList;

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
	
	JButton btnApply = null;
	JButton btnAddComponent = null;
	JButton btnRemoveComponent = null;
	JList<String> availableComponentsList = new JList<String>();
	JList<String> selectedComponentsList = new JList<String>();
	
	ControllerWrapperGUI masterInstance = null;
		

	public POXComponentsGUI(ControllerWrapperGUI controllerWrapperGUI) {
		
		masterInstance = controllerWrapperGUI;
		setAlwaysOnTop(true);
		setResizable(false);
		setSize(500, 400);
		setTitle("POX Components");
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		btnApply = new JButton("Apply");
		springLayout.putConstraint(SpringLayout.WEST, btnApply, -89, SpringLayout.EAST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, btnApply, -10, SpringLayout.SOUTH, getContentPane());
		getContentPane().add(btnApply);
		
		JScrollPane componentsAvailable = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, componentsAvailable, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, componentsAvailable, 15, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, componentsAvailable, -61, SpringLayout.SOUTH, getContentPane());
		getContentPane().add(componentsAvailable);
		
		availableComponentsList = new JList<String>();
		availableComponentsList.setListData(components);
		componentsAvailable.setViewportView(availableComponentsList);
		
		JScrollPane componentSelected = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, componentSelected, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, componentSelected, -26, SpringLayout.NORTH, btnApply);
		springLayout.putConstraint(SpringLayout.EAST, btnApply, 0, SpringLayout.EAST, componentSelected);
		springLayout.putConstraint(SpringLayout.WEST, componentSelected, 298, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, componentSelected, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(componentSelected);
		
		selectedComponentsList = new JList<String>();
		componentSelected.setViewportView(selectedComponentsList);
		
		btnAddComponent = new JButton(">");
		btnAddComponent.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//TODO
				//get selected row, add it to selected items
			}
		});
		springLayout.putConstraint(SpringLayout.WEST, btnAddComponent, 217, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, btnAddComponent, -236, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, componentsAvailable, -23, SpringLayout.WEST, btnAddComponent);
		springLayout.putConstraint(SpringLayout.EAST, btnAddComponent, -22, SpringLayout.WEST, componentSelected);
		getContentPane().add(btnAddComponent);
		
		btnRemoveComponent = new JButton("<");
		springLayout.putConstraint(SpringLayout.NORTH, btnRemoveComponent, 6, SpringLayout.SOUTH, btnAddComponent);
		springLayout.putConstraint(SpringLayout.WEST, btnRemoveComponent, 217, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, btnRemoveComponent, 0, SpringLayout.EAST, btnAddComponent);
		getContentPane().add(btnRemoveComponent);
		
		JButton btnNewButton = new JButton("Close");
		springLayout.putConstraint(SpringLayout.WEST, btnNewButton, 0, SpringLayout.WEST, componentSelected);
		springLayout.putConstraint(SpringLayout.SOUTH, btnNewButton, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, btnNewButton, -23, SpringLayout.WEST, btnApply);
		getContentPane().add(btnNewButton);

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
