package org.appdapter.gui.swing;

import java.awt.LayoutManager;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JPanel;

import org.appdapter.core.convert.NoSuchConversionException;
import org.appdapter.gui.browse.Utility;

public class JJPanel extends JPanel implements UISwingReplacement {
	public JJPanel() {
		super();
	}

	public JJPanel(boolean predecorate) {
		super();
	}

	public JJPanel(LayoutManager layout) {
		super(layout);
	}

	@Override public String getName() {
		try {
			Object val = getValue();
			if (val != null) {
				return Utility.getUniqueName(val);
			}
		} catch (Throwable t) {

		}
		return super.getName();
	}

	public Object getValue() throws Throwable {
		return null;
	}

	@Override public void updateUI() {
		try {
			super.updateUI();
		} catch (Throwable t) {

		}
	}

}
