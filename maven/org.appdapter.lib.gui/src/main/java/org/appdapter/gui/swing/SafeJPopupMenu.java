package org.appdapter.gui.swing;

import javax.swing.JPopupMenu;

public class SafeJPopupMenu extends JPopupMenu implements UISwingReplacement {

	@Override public void addSeparator() {
		try {
			super.addSeparator();
		} catch (Throwable t) {
		}
	}

	@UISalient
	public Object userObject;

	@Override public void updateUI() {
		try {
			super.updateUI();
		} catch (Throwable t) {
		}
	}
}
