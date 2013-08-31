package org.appdapter.gui.swing;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JPanel;

import org.appdapter.core.convert.NoSuchConversionException;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.trigger.TriggerMouseAdapter;

public class JJPanel extends JPanel implements UISwingReplacement, IsReference {
	public JJPanel() {
		this(new FlowLayout());
	}

	public JJPanel(boolean predecorate) {
		this(new FlowLayout());
	}

	public JJPanel(LayoutManager layout) {
		super(layout);
	}

	@Override public String getName() {
		boolean was = Debuggable.isNotShowingExceptions();
		Debuggable.setDoNotShowExceptions(true);
		try {
			String sgn = super.getName();
			if (sgn != null)
				return sgn;
			try {
				Object val = getValue();
				if (val != null) {
					return Utility.getUniqueNameForKey(val);
				}
			} catch (Throwable t) {

			}
			return sgn;
		} finally {
			Debuggable.setDoNotShowExceptions(was);
		}

	}

	public Object getValue() {
		if (getClass() != JJPanel.class && !Debuggable.isNotShowingExceptions())
			Debuggable.warn("getValue not Implemented (returning null)");
		return null;
	}

	@Override public void updateUI() {
		try {
			super.updateUI();
		} catch (Throwable t) {

		}
	}

}
