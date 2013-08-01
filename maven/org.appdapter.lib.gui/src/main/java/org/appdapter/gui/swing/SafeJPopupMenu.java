package org.appdapter.gui.swing;

import javax.swing.JPopupMenu;

public class SafeJPopupMenu extends JPopupMenu implements UISwingReplacement {

	@Override public void addSeparator() {
		// TODO Auto-generated method stub
		super.addSeparator();
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
