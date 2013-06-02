package org.appdapter.gui.demo;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.appdapter.gui.box.POJOApp;
import org.appdapter.gui.demo.CollectionEditorUtil.Icons;
import org.appdapter.gui.pojo.POJOBox;
import org.appdapter.gui.swing.impl.JBox;

public class RenameDialog extends JFrame {
	public JTextField nameField = new JTextField(10);
	public JButton okButton = new JButton("OK");
	public JButton cancelButton = new JButton("Cancel");
	POJOBox object;
	POJOApp context;

	public RenameDialog(POJOApp context, POJOBox object) {
		super("Rename");
		this.context = context;
		setIconImage(Icons.loadImage("mainFrame.gif"));
		this.object = object;

		JPanel top = new JPanel(new FlowLayout());
		top.add(new JLabel("Rename " + object.getUniqueName() + " to: "));
		top.add(nameField);
		nameField.setText(object.getUniqueName());
		nameField.selectAll();

		JPanel bottom = new JPanel(new FlowLayout());
		bottom.add(cancelButton);
		bottom.add(okButton);

		getContentPane().setLayout(new BorderLayout());

		JBox box = new JBox(BoxLayout.Y_AXIS);
		box.add(top);
		box.add(bottom);
		getContentPane().add("Center", box);
		pack();
		org.appdapter.gui.pojo.Utility.centerWindow(this);

		nameField.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void insertUpdate(DocumentEvent evt) {
				checkControls();
			}

			@Override public void changedUpdate(DocumentEvent evt) {
				checkControls();
			}

			@Override public void removeUpdate(DocumentEvent evt) {
				checkControls();
			}
		});

		nameField.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent evt) {
				okPressed();
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent evt) {
				dispose();
			}
		});

		okButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent evt) {
				okPressed();
			}
		});

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	private void okPressed() {
		String name = nameField.getText();
		if (isNameValid(name)) {
			try {
				object.setUniqueName(name);
				context.reload();
			} catch (Exception err) {
				context.showError(null, err);
			}
			dispose();
		} else {
			context.showError("Invalid name - there is already another object named '" + name + "'", null);
		}
	}

	private void checkControls() {
		String newName = nameField.getText();
		okButton.setEnabled(isNameValid(newName));
	}

	private synchronized boolean isNameValid(String n) {
		if (n == null || n.equals("")) {
			return false;
		} else {
			return context.findObjectByName(n) == null;
		}
	}
}
