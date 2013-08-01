package org.appdapter.gui.swing;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.UIManager;
import javax.swing.plaf.MenuItemUI;

public class SafeJCheckBoxMenuItem extends JCheckBoxMenuItem {

	private Object savedContext;

	/**
	 * Resets the UI property with a value from the current look and feel.
	 *
	 * @see JComponent#updateUI
	 */
	public void updateUI() {
		MenuItemUI itemUI = null;
		try {
			itemUI = (MenuItemUI) UIManager.getUI(this);
		} catch (Throwable t) {

		}
		if (itemUI != null) {
			setUI(itemUI);
		}
	}

	protected void fireActionPerformed(ActionEvent event) {
		super.fireActionPerformed(event);
	}

	/**
	 * Creates a menu item whose properties are taken from the 
	 * specified <code>Action</code>.
	 *
	 * @param a the action of the <code>JCheckBoxMenuItem</code>
	 * @since 1.3
	 */
	public SafeJCheckBoxMenuItem(Object ctx, boolean iamObject, Action a) {
		super(a);
		this.savedContext = ctx;
	}

	/**
	 * Creates a <code>JCheckBoxMenuItem</code> with the specified text and icon.
	 *
	 * @param text the text of the <code>JCheckBoxMenuItem</code>
	 * @param icon the icon of the <code>JCheckBoxMenuItem</code>
	 */
	public SafeJCheckBoxMenuItem(Object ctx, boolean iamObject, String text, Icon icon, boolean isChecked) {
		super(text, isChecked);
		this.savedContext = ctx;
	}

}
