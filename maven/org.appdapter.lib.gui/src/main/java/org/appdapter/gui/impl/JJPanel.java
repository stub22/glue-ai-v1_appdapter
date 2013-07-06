package org.appdapter.gui.impl;

import java.awt.LayoutManager;

import javax.swing.JPanel;

import org.appdapter.gui.api.Utility;

public class JJPanel extends JPanel {
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

}
