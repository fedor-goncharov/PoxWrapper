package ru.mail.fedka2005.gui;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;

import org.jgroups.Address;
import org.jgroups.Message;
import org.math.plot.Plot2DPanel;

import ru.mail.fedka2005.main.Controller;
import ru.mail.fedka2005.messages.NodeInfoResponse;
import ru.mail.fedka2005.messages.RecvMessageHandler;
import ru.mail.fedka2005.exceptions.ClientConstructorException;
import ru.mail.fedka2005.exceptions.MalformedInputException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

/**
 * Graphical user interface, includes cpu-load plot of the master, list of cluster-members
 * @author fedor.goncharov.ol@gmail.com
 *
 */
@SuppressWarnings("serial")
public class ControllerWrapperGUI extends JFrame {
	
	private Controller controller = null; //reference to controller object
	private JTable messageTable = null;
	private JTable membersTable = null;	  //list of users in cluster, and who is the master
	private JTextField nodeNameTextField;
	private JTextField groupNameTextField;
	private JTextField poxPathTextField;
	private JTextField addressTextField;
	
	private JButton btnStartClient = null;	//buttons for actions
	private JButton btnStopClient = null;
	private JButton btnPOXConfiguration = null;
	private JTextField cpuThresholdTextField;
	
	private final int message_buffer_size = 17;	//max messages displayed
	private Plot2DPanel plot = new Plot2DPanel();	//plot, master cpu-load
	private LinkedList<Double> masterCPUUsage = new LinkedList<Double>();	//plotting staff'
	public HashSet<String> poxComponentsSelected = null;
	private int selected_id;
	
	/**
	 * Empty constructor, adds all components on the frame
	 */
	public ControllerWrapperGUI() {
		setResizable(false);
		this.setSize(656, 489);
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
					if (nodeName.length() == 0 || groupName.length() == 0 || 
							poxPath.length() == 0 || address.length() == 0 ||
							cpuThreshold.length() == 0) {
						JOptionPane.showMessageDialog(ControllerWrapperGUI.this,
								"Some fields where not defined",
								"EmptyInput Error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					btnStartClient.setEnabled(false);
					textFieldsEnable(false);
					btnPOXConfiguration.setEnabled(false);
					
					controller.startClient(nodeName, groupName, 
										   poxPath, address, 
										   Double.valueOf(cpuThreshold));
					btnStopClient.setEnabled(true);
				} catch (MalformedInputException ex) {
					JOptionPane.showMessageDialog(ControllerWrapperGUI.this, 
							"Malformed input, check if you entered correct data",
							"MalformedInput Error",
							JOptionPane.WARNING_MESSAGE);
					
					stopGUI();
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(ControllerWrapperGUI.this, 
							"Malformed input, check if you entered correct data",
							"MalformedInput Error",
							JOptionPane.WARNING_MESSAGE);
					
					stopGUI();
				} catch (ClientConstructorException ex) {
					JOptionPane.showMessageDialog(ControllerWrapperGUI.this, 
							"Initialization of constructor failed, program will now exit." +
							" See log file for details.",
							"Initialization Error",
							JOptionPane.ERROR_MESSAGE);
					System.exit(1);
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
				stopGUI();
			}
		});
		btnStopClient.setToolTipText("Disconnects client from the cluster");
		btnPOXConfiguration = new JButton("Configure POX");
		btnPOXConfiguration.setEnabled(true);
		btnPOXConfiguration.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JFrame frame = new POXComponentsGUI(ControllerWrapperGUI.this);
				frame.setVisible(true);
			}
		});
		
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
		
		JLabel lblPoxPath = new JLabel("POX path:");
		
		JLabel lblAddress = new JLabel("Address:");
		
		cpuThresholdTextField = new JTextField();
		cpuThresholdTextField.setColumns(10);
		
		JLabel lblCpuThreshold = new JLabel("CPU Threshold:");
		

		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 626, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(btnStopClient, GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
								.addComponent(btnPOXConfiguration, GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
								.addComponent(btnStartClient, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE))
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
										.addComponent(lblAddress))
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
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
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblGroupName)
						.addComponent(addressTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblAddress)
						.addComponent(groupNameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnPOXConfiguration))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(cpuThresholdTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblCpuThreshold)
						.addComponent(btnStopClient))
					.addGap(12))
		);
		
		messageTable = new JTable(new DefaultTableModel(new Object[]{"Source","Destination","Message"}, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		});
		messageTable.setRowSelectionAllowed(true);
		messageTable.setColumnSelectionAllowed(false);
		membersTable = new JTable(new DefaultTableModel(new Object[]{"Name","Address","ID","Master"},0) {	//add table of cluster members
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		});
		membersTable.setRowSelectionAllowed(true);
		membersTable.setColumnSelectionAllowed(false);
		
		
		final ActionListener menuListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {			
				if (event.getActionCommand() == "Refresh") {
					controller.refreshNodes();
				}
				if (event.getActionCommand() == "Detach") {
					int detached_id = (int)membersTable.getModel().getValueAt(selected_id, 2);
					controller.detachSelectedNode(detached_id);
				}
			}
		};
		membersTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int r = membersTable.rowAtPoint(e.getPoint());
				if (r >= 0 && r < membersTable.getRowCount()) {
					membersTable.setRowSelectionInterval(r,r);
				} else {
					membersTable.clearSelection();
				}
				if (e.isPopupTrigger() && e.getComponent() instanceof JTable) {
					
					if (r >= 0 && r < membersTable.getRowCount()) {
						membersTable.setRowSelectionInterval(r,r);
						ControllerWrapperGUI.this.selected_id = membersTable.getSelectedRow();
					}
					JPopupMenu popup = new JPopupMenu();
					JMenuItem menuItem;
					menuItem = new JMenuItem("Refresh");
					menuItem.addActionListener(menuListener);
					popup.add(menuItem);
					menuItem = new JMenuItem("Detach");
					menuItem.addActionListener(menuListener);
					popup.add(menuItem);
					
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		
		
		JScrollPane messageScrollPane = new JScrollPane(messageTable);
		JScrollPane membersScrollPane = new JScrollPane(membersTable);
		
		tabbedPane.addTab("Cluster Messages", null, messageScrollPane, null);	//table of messages to view the history
		tabbedPane.addTab("Cluster Info", null, membersScrollPane, null);		//talbe of cluster members to remember all the members
		plot.setAxisLabel(0, "Time");
		plot.setAxisLabel(1,"CPU-usage");
		plot.setFixedBounds(0, 0, 100);
		plot.setFixedBounds(1,0,1);
		plot.removePlotToolBar();
		tabbedPane.addTab("Master Load",null, plot, null);
		
		getContentPane().setLayout(groupLayout);
		this.setVisible(true);
	}
	private void textFieldsEnable(boolean bool) {
		nodeNameTextField.setEnabled(bool);
		groupNameTextField.setEnabled(bool);
		poxPathTextField.setEnabled(bool);
		addressTextField.setEnabled(bool);
		cpuThresholdTextField.setEnabled(bool);
	}
	
	public void setController(Controller controller) {
		this.controller = controller;
	}
	/**
	 * Update data on message table.
	 * @param msg JGroups message class
	 */
	public void addMessageRecord(Message msg) {
		if (messageTable.getModel().getRowCount() > message_buffer_size) {
			clearTable(messageTable);
		}
		DefaultTableModel model = (DefaultTableModel)messageTable.getModel();
		model.addRow(new Object[]{msg.getSrc(),
				(msg.getDest() == null ? "all" : msg.getDest()),
				msg.getObject()});
		double cpu_usage = RecvMessageHandler.getCPULoad(msg).getCPULoad();
		masterCPUUsage.add(Double.valueOf(cpu_usage));
		if (masterCPUUsage.size() > 100) {
			masterCPUUsage.poll();
		}

		double[] x = new double[100];
		double[] y = new double[100];
		for (int i = 0; i < 100; ++i) {
			y[i] = 0; x[i] = 0;
		}
		Double[] doubleArray = new Double[masterCPUUsage.size()];
		masterCPUUsage.toArray(doubleArray);
		for (int i = 0; i < 100; ++i) {
			x[i] = i;
			if (i < doubleArray.length) {
				y[i] = doubleArray[doubleArray.length - i - 1].doubleValue();
			} else {
				y[i] = 0;
			}
		}
		plot.removeAllPlots();
		plot.addLinePlot("master-cpu", x, y);
	}
	/**
	 * Update list of connected nodes.
	 * @param content  Map<Address, NodeInfoResponse>, Address - JGroups address,
	 * NodeInfoResponse - message class, containing id, name, master
	 */
	public void updateNodeInfo(Map<Address, NodeInfoResponse> content) {
		clearTable(membersTable);
		
		DefaultTableModel model = (DefaultTableModel)membersTable.getModel();
		for (NodeInfoResponse node_info : content.values()) {
			model.addRow(new Object[]{node_info.name,
									  node_info.address,
									  node_info.id,
									  node_info.master
			});
		}
	}
	/**
	 * Pop-up exceptions, generated from business-logic
	 * @param e internal exception that should be displayer
	 */
	public void handleInternalException(Exception e) {
		JOptionPane.showMessageDialog(ControllerWrapperGUI.this, 
				"Exception occured, program will now exit. " +
				"See log file(poxwrapper.log) for details:\n" + e.getMessage(),
				"Unexpected exception",
				JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}
	
	/**
	 * Unblocks buttons, cleares message,members - tables
	 */
	public void stopGUI() {
		textFieldsEnable(true);
		btnStartClient.setEnabled(true);
		btnPOXConfiguration.setEnabled(true);
		btnStopClient.setEnabled(false);
		
		clearTable(messageTable);
		clearTable(membersTable);
	}
	
	private void clearTable(JTable table) {
		try {
			DefaultTableModel model = (DefaultTableModel)table.getModel();
			int rowCount = model.getColumnCount();
			for (int i = 0; i < rowCount; ++i) {
				model.removeRow(i);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			//nothing to do
		}
	}
}
