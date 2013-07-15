package org.appdapter.gui.swing;

import javax.swing.Action;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.UIManager;
import javax.swing.plaf.MenuItemUI;
import javax.swing.plaf.PopupMenuUI;

public class SafeJMenuItem extends JMenuItem {

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

	/**
	* Creates a <code>JMenuItem</code> with no set text or icon.
	*/
	public SafeJMenuItem() {
		this(null, (Icon) null);
	}

	/**
	 * Creates a <code>JMenuItem</code> with the specified icon.
	 *
	 * @param icon the icon of the <code>JMenuItem</code>
	 */
	public SafeJMenuItem(Icon icon) {
		this(null, icon);
	}

	/**
	 * Creates a <code>JMenuItem</code> with the specified text.
	 *
	 * @param text the text of the <code>JMenuItem</code>
	 */
	public SafeJMenuItem(String text) {
		this(text, (Icon) null);
	}

	/**
	 * Creates a menu item whose properties are taken from the 
	 * specified <code>Action</code>.
	 *
	 * @param a the action of the <code>JMenuItem</code>
	 * @since 1.3
	 */
	public SafeJMenuItem(Action a) {
		super(a);
	}

	/**
	 * Creates a <code>JMenuItem</code> with the specified text and icon.
	 *
	 * @param text the text of the <code>JMenuItem</code>
	 * @param icon the icon of the <code>JMenuItem</code>
	 */
	public SafeJMenuItem(String text, Icon icon) {
		super(text, icon);
	}

	/**
	 * Creates a <code>JMenuItem</code> with the specified text and
	 * keyboard mnemonic.
	 *
	 * @param text the text of the <code>JMenuItem</code>
	 * @param mnemonic the keyboard mnemonic for the <code>JMenuItem</code>
	 */
	public SafeJMenuItem(String text, int mnemonic) {
		super(text, mnemonic);
	}

}
