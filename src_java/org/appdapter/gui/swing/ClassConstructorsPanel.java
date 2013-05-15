package org.appdapter.gui.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import org.appdapter.gui.pojo.POJOApp;
import org.appdapter.gui.pojo.Utility;
import org.appdapter.gui.swing.impl.JJPanel;

/**
 * A GUI component that shows all the constructors provided by a given class
 */
public class ClassConstructorsPanel extends JJPanel implements ActionListener {
	//==== Instance variables ==========================

	Class cls;
	POJOApp context;

	//Maps Button -> Constructor
	Hashtable buttons = new Hashtable();

	//Maps Button -> parameters panels
	Hashtable panels = new Hashtable();

	//==== Constructors =============================

	public ClassConstructorsPanel(POJOApp context, Class cls) throws Exception {
		this.context = context;
		this.cls = cls;
		initGUI();
	}

	public ClassConstructorsPanel(Class cls) throws Exception {
		this(Utility.getCurrentContext(), cls);
	}

	//==== Event handlers =============================

	@Override public void actionPerformed(ActionEvent evt) {
		Constructor c = (Constructor) buttons.get(evt.getSource());
		if (c != null) {
			ConstructorParametersPanel p;
			p = (ConstructorParametersPanel) panels.get(evt.getSource());
			try {
				executeConstructor(c, p.getValues());
			} catch (Throwable err) {
				if (context == null) {
					new ErrorDialog(err).show();
				} else {
					context.showError(null, err);
				}
			}
		}
	}

	//==== Private methods =======================

	/**
	 * Executes the given constructor with the given parameters
	 */
	private void executeConstructor(Constructor constructor, Object[] params) throws Exception {
		if (constructor != null) {
			Object newObject = constructor.newInstance(params);
			if (context != null) {
				context.getCollectionWithSwizzler().addPOJO(newObject);
			}
		}
	}

	/**
	 * Creates the GUI
	 */
	private void initGUI() throws Exception {
		setLayout(new VerticalLayout(VerticalLayout.LEFT, true));

		Constructor[] array = cls.getConstructors();
		for (int i = 0; i < array.length; ++i) {
			Constructor c = array[i];

			JButton button = new JButton("Create");
			button.addActionListener(this);

			JPanel pbutton = new JPanel();
			pbutton.setLayout(new FlowLayout(FlowLayout.RIGHT));
			pbutton.add(button);

			ConstructorParametersPanel pparams;
			pparams = new ConstructorParametersPanel(context, c);

			JPanel pmain = new JPanel();
			pmain.setLayout(new BorderLayout());
			pmain.add("Center", pparams);
			pmain.add("West", pbutton);

			EtchedBorder border = new EtchedBorder(EtchedBorder.LOWERED);
			pmain.setBorder(border);

			buttons.put(button, c);
			panels.put(button, pparams);
			add(pmain);
		}
	}

}
