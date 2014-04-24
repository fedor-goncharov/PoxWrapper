package ru.mail.fedka2005.gui;

import javax.swing.JFrame;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JLabel;

import org.jgroups.Message;

import ru.mail.fedka2005.main.Controller;
import ru.mail.fedka2005.exceptions.ClientConstructorException;
import ru.mail.fedka2005.exceptions.MalformedInputException;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ControllerWrapperGUI extends JFrame {
	
	private Controller controller = null; //reference to controller object
	private JTable messageTable = null;
	private JTextField nodeNameTextField;
	private JTextField groupNameTextField;
	private JTextField poxPathTextField;
	private JTextField addressTextField;
	
	private JButton btnStartClient = null;	//buttons for actions
	private JButton btnStopClient = null;
	private JTextField cpuThresholdTextField;
	private JTextField portTextField;
	public ControllerWrapperGUI() {
		setResizable(false);
		this.setSize(640, 480);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		setTitle("Cluster Monitoring Client");
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setToolTipText("Cluster Messages");
		tabbedPane.setBorder(new LineBorder(new Color(0, 0, 0)));
		
		btnStartClient = new JButton("Start Client");
		btnStartClient.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//block input while invoking connection
				try {
					String nodeName = nodeNameTextField.getText();
					String groupName = groupNameTextField.getText();
					String poxPath = poxPathTextField.getText();
					String address = addressTextField.getText();
					String cpuThreshold = cpuThresholdTextField.getText();
					String port = portTextField.getText();
					if (nodeName.length() == 0 || groupName.length() == 0 || 
							poxPath.length() == 0 || address.length() == 0 ||
							cpuThreshold.length() == 0 || port.length() == 0) {
						JOptionPane.showMessageDialog(ControllerWrapperGUI.this,
								"Some fields where not defined",
								"EmptyInput Error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					btnStartClient.setEnabled(false);
					textFieldsEnable(false);
					
					controller.startClient(nodeName, groupName, poxPath, address, cpuThreshold, port);	//start controller-client
					//in a seperate thread
					btnStopClient.setEnabled(true);
				} catch (NullPointerException ex) {
					//TODO - throw exception
					//happens when problems with gui components occure
				} catch (MalformedInputException ex) {
					JOptionPane.showMessageDialog(ControllerWrapperGUI.this, 
							"Malformed input, check if you entered correct data",
							"MalformedInput Error",
							JOptionPane.ERROR_MESSAGE);
					textFieldsEnable(true);
					btnStartClient.setEnabled(true);
				} catch (ClientConstructorException ex) {
					JOptionPane.showMessageDialog(ControllerWrapperGUI.this, 
							"Initialization of constructor failed, see log file for details.",
							"Initialization Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnStartClient.setToolTipText("Connect to cluster with specified address and unique" +
				" identifier name");
		
		btnStopClient = new JButton("Stop Client");
		btnStopClient.setEnabled(false);
		btnStopClient.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				controller.stopClient();
				textFieldsEnable(true);
				btnStartClient.setEnabled(true);
				btnStopClient.setEnabled(false);
			}
		});
		btnStopClient.setToolTipText("Disconnects client from the cluster");
		
		nodeNameTextField = new JTextField();
		nodeNameTextField.setColumns(10);
		
		JLabel lblNodeName = new JLabel("Node Name:");
		
		groupNameTextField = new JTextField();
		groupNameTextField.setColumns(10);
		
		JLabel lblGroupName = new JLabel("Group Name:");
		
		poxPathTextField = new JTextField();
		poxPathTextField.setColumns(10);
		
		addressTextField = new JTextField();
		addressTextField.setColumns(10);
		
		JLabel lblPoxPath = new JLabel("POX Path:");
		
		JLabel lblAddress = new JLabel("Address:");
		
		cpuThresholdTextField = new JTextField();
		cpuThresholdTextField.setColumns(10);
		
		JLabel lblCpuThreshold = new JLabel("CPU Threshold:");
		
		JLabel lblNewLabel = new JLabel("Port:");
		
		portTextField = new JTextField();
		portTextField.setColumns(10);
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addComponent(btnStopClient, GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
								.addComponent(btnStartClient, GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE))
							.addGap(14)
							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(lblNodeName)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(nodeNameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(lblGroupName)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(groupNameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED))
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(lblCpuThreshold)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(cpuThresholdTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)))
							.addGap(38)
							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(lblPoxPath)
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addComponent(poxPathTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGroup(groupLayout.createSequentialGroup()
									.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
										.addComponent(lblNewLabel)
										.addComponent(lblAddress))
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
										.addComponent(portTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(addressTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 348, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnStartClient)
						.addComponent(nodeNameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNodeName)
						.addComponent(poxPathTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblPoxPath))
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
							.addComponent(btnStopClient)
							.addPreferredGap(ComponentPlacement.UNRELATED))
						.addGroup(groupLayout.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblGroupName)
								.addComponent(addressTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblAddress)
								.addComponent(groupNameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblNewLabel)
								.addComponent(portTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(cpuThresholdTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblCpuThreshold))
							.addPreferredGap(ComponentPlacement.RELATED)))
					.addGap(12))
		);
		
		messageTable = new JTable(new DefaultTableModel(new Object[]{"Source","Destination","Message"}, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		});
		tabbedPane.addTab("Cluster Messages", null, new JScrollPane(messageTable), null);		
		JPanel clusterInfoPanel = new JPanel();
		tabbedPane.addTab("Cluster Info", null, clusterInfoPanel, null);
	
		getContentPane().setLayout(groupLayout);
		this.setVisible(true);
	}
	private void textFieldsEnable(boolean bool) {
		nodeNameTextField.setEnabled(bool);
		groupNameTextField.setEnabled(bool);
		poxPathTextField.setEnabled(bool);
		addressTextField.setEnabled(bool);
		cpuThresholdTextField.setEnabled(bool);
		portTextField.setEnabled(bool);
	}
	
	public void setController(Controller controller) {
		this.controller = controller;
	}
	/**
	 * update data on 
	 */
	public void addRecord(Message msg) {
		DefaultTableModel model = (DefaultTableModel)messageTable.getModel();
		model.addRow(new Object[]{msg.getSrc(),
				(msg.getDest() == null ? "all" : msg.getDest()),
				msg.getObject()});
	}
}
