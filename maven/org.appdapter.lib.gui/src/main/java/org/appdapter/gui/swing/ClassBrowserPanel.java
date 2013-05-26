package org.appdapter.gui.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.appdapter.gui.pojo.POJOApp;
import org.appdapter.gui.pojo.Utility;
import org.appdapter.gui.swing.impl.JVPanel;
import org.appdapter.gui.util.PromiscuousClassUtils;

/**
 * A GUI component that lets you select any class and browse
 * its constructors, static methods, etc.
 *
 * @author Henrk Kniberg
 */
public class ClassBrowserPanel extends JVPanel implements ActionListener {
	POJOApp context;

	JTextField text;
	JButton okButton;
	JPanel topPanel = new JPanel();
	JPanel classPanel = new JPanel();

	public ClassBrowserPanel(POJOApp context) {
		this.context = context;
		initGUI();
	}

	public ClassBrowserPanel() {
		this(Utility.getCurrentContext());
	}

	private void initGUI() {
		Container content = this;
		content.setLayout(new BorderLayout());

		text = new JTextField(10);
		text.addActionListener(this);
		okButton = new JButton("Browse this class");
		okButton.addActionListener(this);

		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BorderLayout());
		textPanel.add("Center", text);
		textPanel.add("East", okButton);

		topPanel.setLayout(new VerticalLayout());
		topPanel.add(new JLabel("Enter the complete classname:"));
		topPanel.add(textPanel);

		classPanel.setLayout(new BorderLayout());

		content.add("North", topPanel);
		content.add("Center", classPanel);

		//setSize(300, 200);
	}

	@Override public void actionPerformed(ActionEvent evt) {
		showClass();
	}

	/**
	 * Shows the currently select class now,
	 * or shows an error if something went wrong.
	 */
	private void showClass() {
		//classPanel.removeAll();
		try {
			Class cl = PromiscuousClassUtils.forName(text.getText());
			context.showScreenBox(cl);
			//ScreenBoxPanel view = new BasicObjectCustomizer(context, cl);
			//classPanel.add("Center", view);
		} catch (Exception err) {
			classPanel.add("Center", new JLabel(err.toString()));
		}
	}
}
