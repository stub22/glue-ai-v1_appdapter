package org.appdapter.gui.demo;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.appdapter.demo.ObjectNavigatorGUI;
import org.appdapter.gui.pojo.POJOCollection;
import org.appdapter.gui.pojo.POJOCollectionWithBoxContext;
import org.appdapter.gui.pojo.POJOCollectionWithSwizzler;
import org.appdapter.gui.pojo.Utility;
import org.appdapter.gui.swing.impl.JJPanel;

public class POJOCollectionViewPanelWithContext extends JJPanel implements ActionListener, DocumentListener, ObjectNavigatorGUI {
	Class selectedClass = null;

	JLayeredPane desk;
	JSplitPane split;
	POJOCollectionViewPanel list;
	JButton classBrowserButton;
	JTextField classField;
	POJOCollectionWithBoxContext context;

	public POJOCollectionViewPanelWithContext(POJOCollectionWithBoxContext context) {
		this.context = context;
		Utility.registerEditors();
		Utility.setBeanInfoSearchPath();
		initGUI();
	}

	@Override
	public JLayeredPane getDesk() {
		return desk;
	}

	public POJOCollection getCollection() {
		return context.getCollection();
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == classField || evt.getSource() == classBrowserButton) {
			openClassBrowser();
		}
	}

	public POJOCollectionViewPanel getPOJOList() {
		return list;
	}

	private synchronized void openClassBrowser() {
		if (selectedClass != null) {
			try {
				context.showScreenBox(selectedClass);
			} catch (Throwable err) {
				context.showError(null, err);
			}
		}
	}

	private void initGUI() {
		desk = new JDesktopPane();
		list = new POJOCollectionViewPanel(context);

		classBrowserButton = new JButton("Examine...");
		classBrowserButton.setToolTipText("Opens a new window that lets you examine classes and create new object instances");
		classBrowserButton.addActionListener(this);
		classBrowserButton.setEnabled(false);
		// classBrowserButton.setActionCommand(COMMAND_CREATE_BEAN);

		classField = new JTextField(10);
		classField.addActionListener(this);
		classField.getDocument().addDocumentListener(this);

		JPanel classPanel = new JPanel();
		classPanel.setBorder(new TitledBorder("Class browser"));
		classPanel.setLayout(new BorderLayout());
		classPanel.add("North", new JLabel("Full class name:"));
		classPanel.add("Center", classField);
		classPanel.add("East", classBrowserButton);

		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout());
		// objectsPanel.setBorder(new TitledBorder("Object browser"));

		// list.setTitle("Object browser");
		list.setBorder(new TitledBorder("Object browser"));

		/*
		 * JPanel listPanel = new JPanel(); listPanel.setLayout(new
		 * BorderLayout()); listPanel.add("North", tempPanel);
		 * listPanel.add("Center", list);
		 */

		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, leftPanel, desk);
		split.setOneTouchExpandable(true);

		setLayout(new BorderLayout());
		add("Center", split);

		leftPanel.add("North", classPanel);
		leftPanel.add("Center", list);
	}

	private void classFieldChanged() {
		try {
			selectedClass = Class.forName(classField.getText());
		} catch (Exception err) {
			selectedClass = null;
		}
		classBrowserButton.setEnabled(selectedClass != null);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		classFieldChanged();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		classFieldChanged();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		classFieldChanged();
	}

	@Override
	public POJOCollectionWithSwizzler getCollectionWithSwizzler() {
		return context.getCollectionWithSwizzler();
	}

}
