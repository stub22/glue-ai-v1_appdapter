package org.appdapter.gui.swing;

import javax.swing.JMenu;
import javax.swing.UIManager;
import javax.swing.plaf.MenuItemUI;
import javax.swing.plaf.PopupMenuUI;


public class SafeJMenu extends JMenu {

	public SafeJMenu(String text) {
		super(text);
	}

	/**
	 * Resets the UI property with a value from the current look and feel.
	 *
	 * @see JComponent#updateUI
	 */
	public void updateUI() {
		try {
			super.updateUI();
		} catch (Throwable t) {

		}
	}

}
