package ru.mail.fedka2005.gui;


import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SpringLayout;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JList;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Frame, where client can choose a number of components to be invoked when
 * controller starts.
 * @author fedor.goncharov.ol@gmail.com
 *
 */

//TODO
//implement choosing number of components
//TODO
//actions on close, apply buttons

@SuppressWarnings("serial")
public class POXComponentsGUI extends JFrame {
	
	JButton btnApply = null;
	JButton btnAddComponent = null;
	JButton btnRemoveComponent = null;
	JButton btnClose = null;
	JList<String> availableComponentsListGUI = new JList<String>();
	JList<String> selectedComponentsListGUI = new JList<String>();
	HashSet<String> selectedComponents = new HashSet<String>();
	
	ControllerWrapperGUI masterInstance = null;
		

	public POXComponentsGUI(ControllerWrapperGUI controllerWrapperGUI) {
		
		masterInstance = controllerWrapperGUI;
		masterInstance.setEnabled(false); 	//disable until choose number of components
		setAlwaysOnTop(true);
		setResizable(false);
		setSize(500, 400);
		setTitle("POX Components");
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		btnApply = new JButton("Apply");
		btnApply.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				masterInstance.poxComponentsSelected = selectedComponents;
				POXComponentsGUI.this.dispose();
			}
		});
		springLayout.putConstraint(SpringLayout.WEST, btnApply, -89, SpringLayout.EAST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, btnApply, -10, SpringLayout.SOUTH, getContentPane());
		getContentPane().add(btnApply);
		
		JScrollPane componentsAvailable = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, componentsAvailable, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, componentsAvailable, 15, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, componentsAvailable, -61, SpringLayout.SOUTH, getContentPane());
		getContentPane().add(componentsAvailable);
		
		availableComponentsListGUI = new JList<String>();
		availableComponentsListGUI.setListData(components);
		componentsAvailable.setViewportView(availableComponentsListGUI);
		
		JScrollPane componentSelected = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, componentSelected, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, componentSelected, -26, SpringLayout.NORTH, btnApply);
		springLayout.putConstraint(SpringLayout.EAST, btnApply, 0, SpringLayout.EAST, componentSelected);
		springLayout.putConstraint(SpringLayout.WEST, componentSelected, 298, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, componentSelected, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(componentSelected);
		
		selectedComponentsListGUI = new JList<String>();
		if (selectedComponents.size() > 0) {
			selectedComponentsListGUI.setListData(
					selectedComponents.toArray(new String[selectedComponents.size()]));
		}
		componentSelected.setViewportView(selectedComponentsListGUI);
		
		btnAddComponent = new JButton(">");
		btnAddComponent.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				List<String> strList;
				if ((strList = POXComponentsGUI.this.availableComponentsListGUI.getSelectedValuesList()) != null) {
					selectedComponents.addAll(strList);
					selectedComponentsListGUI.setListData(selectedComponents.toArray(
							new String[selectedComponents.size()]));	//convert to array
				}
			}
		});
		springLayout.putConstraint(SpringLayout.WEST, btnAddComponent, 217, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, btnAddComponent, -236, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, componentsAvailable, -23, SpringLayout.WEST, btnAddComponent);
		springLayout.putConstraint(SpringLayout.EAST, btnAddComponent, -22, SpringLayout.WEST, componentSelected);
		getContentPane().add(btnAddComponent);
		
		btnRemoveComponent = new JButton("<");
		btnRemoveComponent.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				List<String> strList;
				if ((strList = POXComponentsGUI.this.selectedComponentsListGUI.getSelectedValuesList()) != null)  {
					selectedComponents.removeAll(strList);
					selectedComponentsListGUI.setListData(selectedComponents.toArray(
							new String[selectedComponents.size()]));	//convert to array
				}
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, btnRemoveComponent, 6, SpringLayout.SOUTH, btnAddComponent);
		springLayout.putConstraint(SpringLayout.WEST, btnRemoveComponent, 217, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, btnRemoveComponent, 0, SpringLayout.EAST, btnAddComponent);
		getContentPane().add(btnRemoveComponent);
		
		btnClose = new JButton("Close");
		springLayout.putConstraint(SpringLayout.WEST, btnClose, 0, SpringLayout.WEST, componentSelected);
		springLayout.putConstraint(SpringLayout.SOUTH, btnClose, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, btnClose, -23, SpringLayout.WEST, btnApply);
		getContentPane().add(btnClose);
		btnClose.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				selectedComponents.clear();
				masterInstance.setEnabled(true);
				POXComponentsGUI.this.dispose();
			}
		});
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				selectedComponents.clear();
				masterInstance.setEnabled(true);
				
			}
		});
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

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
