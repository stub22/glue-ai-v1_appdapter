package org.appdapter.gui.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.appdapter.api.trigger.AnyOper.UISalient;
import org.appdapter.core.convert.NoSuchConversionException;
import org.appdapter.gui.api.BrowserPanelGUI;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.editors.LargeObjectView;
import org.appdapter.gui.util.PromiscuousClassUtilsA;

/**
 * A GUI component that lets you select any class and browse
 * its constructors, static methods, etc.
 *
 * @author Henrk Kniberg
 */

@UISalient(Description = "Show subclasses and implementors of %t")
public class ClassBrowserPanel extends JJPanel implements ActionListener {
	DisplayContext context;

	JTextField text;
	JButton okButton;
	JPanel topPanel = new JPanel();
	JPanel classPanel = new JPanel();

	public ClassBrowserPanel(BrowserPanelGUI ctx) {
		this.context = ctx;
		initGUI();
	}

	public ClassBrowserPanel() {
		this(Utility.controlApp);
	}

	private void initGUI() {
		Container content = this;
		content.setLayout(new BorderLayout());

		text = new JTextField(10);
		text.addActionListener(this);
		okButton = new JButton("Browse this class");
		okButton.addActionListener(this);

		JPanel textPanel = new JPanel();
		text.setText(org.appdapter.gui.test.TestObject.class.getName());
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

	public void actionPerformed(ActionEvent evt) {
		showClass();
	}

	/**
	 * Shows the currently select class now,
	 * or shows an error if something went wrong.
	 */
	private void showClass() {
		classPanel.removeAll();
		try {
			Class cl = PromiscuousClassUtilsA.forName(text.getText());
			LargeObjectView view = new LargeObjectView();
			view.setObject(cl); //context, cl);
			classPanel.add("Center", view);
		} catch (Exception err) {
			classPanel.add("Center", new JLabel(err.toString()));
		}
	}

	public void setObject(Object object) throws java.lang.reflect.InvocationTargetException, NoSuchConversionException {
		Class clazz = Utility.recast(object, Class.class);
		text.setText(clazz.getCanonicalName());
		if (context == null) {
			Utility.ensureRunning();
			context = Utility.getCurrentContext();
		}
		try {
			Utility.browserPanel.showScreenBox(clazz);
		} catch (Exception e) {
			Utility.showError(context, null, e);
		}
	}
}
