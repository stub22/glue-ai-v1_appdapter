package org.appdapter.gui.demo;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.appdapter.gui.box.POJOApp;
import org.appdapter.gui.pojo.Utility;
import org.appdapter.gui.swing.impl.JJPanel;
import org.appdapter.gui.util.PromiscuousClassUtils;

import com.jidesoft.swing.AutoCompletion;
import com.jidesoft.swing.JideButton;
import com.jidesoft.swing.JideComboBox;

public class ClassChooserPanel extends JJPanel implements ActionListener, DocumentListener {
	Class selectedClass = null;

	//JLayeredPane desk;
	//JSplitPane split;
	JideButton classBrowserButton;
	JideComboBox classField;
	POJOApp context;
	AutoCompletion autoCompletion;

	public ClassChooserPanel(POJOApp context) {
		this.context = context;
		Utility.registerEditors();
		Utility.setBeanInfoSearchPath();
		initGUI();
		new Thread("Class groveler") {
			public void run() {
				resetAutoComplete();
			};
		}.start();
	}

	private void resetAutoComplete() {
		autoCompletion = new AutoCompletion(classField);
		autoCompletion.setStrict(false);
		autoCompletion.setStrictCompletion(false);
		for (Class c : PromiscuousClassUtils.getInstalledClasses()) {
			classField.addItem(c.getName());
		}
	}

	@Override public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == classField || evt.getSource() == classBrowserButton) {
			openClassBrowser();
		}
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

	void initGUI() {
		removeAll();

		adjustSize();

		classBrowserButton = new JideButton("Examine...");
		classBrowserButton.setToolTipText("Opens a new window that lets you examine classes and create new object instances");
		classBrowserButton.addActionListener(this);
		classBrowserButton.setEnabled(false);
		//classBrowserButton.setActionCommand(COMMAND_CREATE_BEAN);

		classField = new JideComboBox();
		classField.setSize(400, (int) classField.getSize().getHeight());
		classField.addActionListener(this);
		classField.setEnabled(true);
		final JTextComponent tc = (JTextComponent) classField.getEditor().getEditorComponent();
		tc.getDocument().addDocumentListener(this);

		setBorder(new TitledBorder("Class browser"));
		setLayout(new BorderLayout());
		add("North", new JLabel("Full class name:"));
		add("Center", classField);
		add("East", classBrowserButton);
	}

	private void adjustSize() {
		Container p = getParent();
		if (p != null) {
			setSize(p.getSize());
		}
	}

	private void classFieldChanged() {
		try {
			final JTextComponent tc = (JTextComponent) classField.getEditor().getEditorComponent();
			selectedClass = PromiscuousClassUtils.forName(tc.getText());
		} catch (Exception err) {
			selectedClass = null;
		}
		classBrowserButton.setEnabled(selectedClass != null || true);
	}

	@Override public void insertUpdate(DocumentEvent e) {
		classFieldChanged();
	}

	@Override public void removeUpdate(DocumentEvent e) {
		classFieldChanged();
	}

	@Override public void changedUpdate(DocumentEvent e) {
		classFieldChanged();
	}

}
