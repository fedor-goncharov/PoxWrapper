package ru.mail.fedka2005.gui;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

public class ControllerWrapperGUI extends JFrame {
	public ControllerWrapperGUI() {
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGap(0, 444, Short.MAX_VALUE)
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGap(0, 273, Short.MAX_VALUE)
		);
		getContentPane().setLayout(groupLayout);
	}

}
