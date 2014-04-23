package ru.mail.fedka2005.gui;

import javax.swing.JFrame;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JLabel;

import ru.mail.fedka2005.main.Controller;
import ru.mail.fedka2005.exceptions.MalformedInputException;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ControllerWrapperGUI extends JFrame {
	
	private Controller controller = null; //reference to controller object
	private Object[][] data = null;		  //data printed in the table windows
	private JTable messageTable;
	private JTextField nodeNameTextField;
	private JTextField groupNameTextField;
	private JTextField poxPathTextField;
	private JTextField addressTextField;
	
	private JButton btnStartClient = null;	//buttons for actions
	private JButton btnStopClient = null;
	public ControllerWrapperGUI() {
		setResizable(false);
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
					btnStartClient.setEnabled(false);
					String nodeName = nodeNameTextField.getText();
					nodeNameTextField.setEnabled(false);
					String groupName = groupNameTextField.getText();
					groupNameTextField.setEnabled(false);
					String poxPath = poxPathTextField.getText();
					poxPathTextField.setEnabled(false);
					String address = addressTextField.getText();
					addressTextField.setEnabled(false);
					controller.startClient(nodeName, groupName, poxPath, address);	//start controller-client
					//in a seperate thread
					btnStopClient.setEnabled(true);
				} catch (NullPointerException ex) {
					JOptionPane.showMessageDialog(new JFrame(),
							"Some fields where not defined",
							"EmptyInput Error",
							JOptionPane.ERROR_MESSAGE);
				} catch (MalformedInputException ex) {
					JOptionPane.showMessageDialog(new JFrame(), 
							"Malformed input, check if you entered correct data",
							"MalformedInput Error",
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
				nodeNameTextField.setEnabled(true);
				groupNameTextField.setEnabled(true);
				poxPathTextField.setEnabled(true);
				addressTextField.setEnabled(true);

				btnStartClient.setEnabled(true);
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
		
		JLabel lblPoxPath = new JLabel("POX Path:port:");
		
		JLabel lblAddress = new JLabel("Address:");
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 602, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
								.addComponent(btnStopClient, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(btnStartClient, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
							.addGap(20)
							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addComponent(lblNodeName)
								.addComponent(lblGroupName))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(groupNameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED, 52, Short.MAX_VALUE)
									.addComponent(lblAddress)
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addComponent(addressTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(nodeNameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED, 52, Short.MAX_VALUE)
									.addComponent(lblPoxPath)
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addComponent(poxPathTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnStartClient)
						.addComponent(nodeNameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNodeName)
						.addComponent(poxPathTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblPoxPath))
					.addGap(3)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnStopClient)
						.addComponent(groupNameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblGroupName)
						.addComponent(addressTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblAddress))
					.addContainerGap())
		);
		
		class MessageTableModel extends AbstractTableModel {
			String[] columnNames = {"NodeID", 
					"Source", 
					"Adress",
					"Message String"};
			private Object[][] data = null;
			
			@Override
			public int getColumnCount() {
				return columnNames.length;
			}

			@Override
			public int getRowCount() {
				return data.length;
			}

			@Override
			public Object getValueAt(int row, int col) {
				return data[row][col];
			}
		}
		messageTable = new JTable(new MessageTableModel());
		tabbedPane.addTab("Cluster Messages", null, messageTable, null);		
		JPanel clusterInfoPanel = new JPanel();
		tabbedPane.addTab("Cluster Info", null, clusterInfoPanel, null);
	
		getContentPane().setLayout(groupLayout);
	}
	public void setController(Controller controller) {
		this.controller = controller;
	}
	/**
	 * update data on 
	 */
	public void addRecord() {
		
	}
}
