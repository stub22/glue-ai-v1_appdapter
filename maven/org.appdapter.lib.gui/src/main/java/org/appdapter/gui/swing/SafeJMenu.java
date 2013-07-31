package org.appdapter.gui.swing;

import java.awt.event.ActionEvent;

import javax.swing.JComponent;
import javax.swing.JMenu;

public class SafeJMenu extends JMenu {

	final Object targetMaybe;

	public SafeJMenu(boolean iamObject, String text, Object target) {
		super(text);
		targetMaybe = target;		
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

	protected void fireActionPerformed(ActionEvent event) {
		super.fireActionPerformed(event);
	}

}
