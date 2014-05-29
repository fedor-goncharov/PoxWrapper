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
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JLabel;

import org.jgroups.Message;
import org.math.plot.Plot2DPanel;

import ru.mail.fedka2005.main.Controller;
import ru.mail.fedka2005.messages.RecvMessageHandler;
import ru.mail.fedka2005.exceptions.ClientConstructorException;
import ru.mail.fedka2005.exceptions.MalformedInputException;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

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
	private JTextField cpuThresholdTextField;
	private JTextField portTextField;
	
	private final int message_buffer_size = 17;	//max messages displayed
	private Plot2DPanel plot = new Plot2DPanel();	//plot, master cpu-load
	private LinkedList<Double> masterCPUUsage = new LinkedList<Double>();	//plotting staff
	
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
				stopGUI();
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
		
		JLabel lblPoxPath = new JLabel("POX path:");
		
		JLabel lblAddress = new JLabel("Address:");
		
		cpuThresholdTextField = new JTextField();
		cpuThresholdTextField.setColumns(10);
		
		JLabel lblCpuThreshold = new JLabel("CPU Threshold:");
		
		JLabel lblNewLabel = new JLabel("POX port:");
		
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
		
		//TODO
		//add event handler for clicking right mose on the row - detach, refresh
		
		JScrollPane messageScrollPane = new JScrollPane(messageTable);
		JScrollPane membersScrollPane = new JScrollPane(membersTable);
		
		tabbedPane.addTab("Cluster Messages", null, messageScrollPane, null);	//table of messages to view the history
		tabbedPane.addTab("Cluster Info", null, membersScrollPane, null);	//talbe of cluster members to remember all the members
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
		portTextField.setEnabled(bool);
	}
	
	public void setController(Controller controller) {
		this.controller = controller;
	}
	/**
	 * update data on message table, draw in gui
	 * @parame Message
	 */
	public void addRecord(Message msg) {
		DefaultTableModel model = (DefaultTableModel)messageTable.getModel();
		if (model.getRowCount() > message_buffer_size) {	//clear table sometimes, add flush messages to log_files
			int size = model.getRowCount();
			for (int i = 0; i < size; ++i) {
				model.removeRow(size-i-1);
			}
		}
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
	 * Pop-up exceptions, generated from business-logic
	 * @param e
	 */
	public void handleInternalException(Exception e) {
		JOptionPane.showMessageDialog(ControllerWrapperGUI.this, 
		"Exception occured, see log file for details:\n" + 
		e.getMessage(),
		"Unexpected exception",
		JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}
	
	/**
	 * Stop client, display in GUI:
	 */
	public void stopGUI() {
		textFieldsEnable(true);
		btnStartClient.setEnabled(true);
		btnStopClient.setEnabled(false);
	}
}
