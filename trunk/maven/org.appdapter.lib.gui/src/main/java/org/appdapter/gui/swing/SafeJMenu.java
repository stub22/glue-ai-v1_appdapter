package org.appdapter.gui.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.GetSetObject;

public class SafeJMenu extends JMenu implements UISwingReplacement, GetSetObject {

	@UISalient
	public Object userObject;

	public SafeJMenu(boolean iamObject, String text, Object target) {
		super(text);
		userObject = target;
	}

	@Override public void setPopupMenuVisible(boolean b) {
		super.setPopupMenuVisible(b);
	}

	@Override public boolean isPopupMenuVisible() {
		return super.isPopupMenuVisible();
	}

	/**
	 * Resets the UI property with a value from the current look and feel.
	 *
	 * @see JComponent#updateUI
	 */
	public void updateUI() {
		ensureSafePopupMenuCreated();
		try {
			super.updateUI();
		} catch (Throwable t) {

		}
	}

	protected void fireActionPerformed(ActionEvent event) {
		super.fireActionPerformed(event);
	}

	/**
	 * Creates a new menu item attached to the specified 
	 * <code>Action</code> object and appends it to the end of this menu.
	 *
	 * @param a the <code>Action</code> for the menu item to be added
	 * @see Action
	 */
	public JMenuItem add(Action a) {
		JMenuItem mi = createActionComponent(a);
		mi.setAction(a);
		add(mi);
		return mi;
	}

	protected JMenuItem createActionComponent(Action a) {
		JMenuItem mi = new SafeJMenuItem(userObject, true) {
			protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
				PropertyChangeListener pcl = createActionChangeListener(this);
				if (pcl == null) {
					pcl = super.createActionPropertyChangeListener(a);
				}
				return pcl;
			}
		};
		mi.setHorizontalTextPosition(JButton.TRAILING);
		mi.setVerticalTextPosition(JButton.CENTER);
		return mi;
	}

	public Component getMenuComponent(int n) {
		ensureSafePopupMenuCreated();
		return super.getMenuComponent(n);
	}

	public Object getValue() {
		return userObject;
	}

	/**
	 * Initializes the menu item with the specified text and icon.
	 *
	 * @param text the text of the <code>JMenuItem</code>
	 * @param icon the icon of the <code>JMenuItem</code>
	 */
	protected void init(String text, Icon icon) {
		ensureSafePopupMenuCreated();
		super.init(text, icon);
	}

	public void setObject(Object object) {
		userObject = object;
	}

	@Override public void addSeparator() {
		ensureSafePopupMenuCreated();
		//super.addSeparator();
	}

	private void ensureSafePopupMenuCreated() {
		try {
			Field f = JMenu.class.getDeclaredField("popupMenu");
			f.setAccessible(true);
			JPopupMenu popupMenu = (JPopupMenu) f.get(this);
			if (!(popupMenu instanceof UISwingReplacement)) {
				SafeJPopupMenu safe;
				popupMenu = safe = new SafeJPopupMenu();
				safe.userObject = this;//.userObject our user object may not be populated yet 
				f.set(this, popupMenu);
				popupMenu.setInvoker(this);
				popupListener = createWinListener(popupMenu);
			}
		} catch (NoSuchFieldException t) {
			Debuggable.warn("Fields = " + Debuggable.toInfoStringA(JMenu.class.getDeclaredFields(), ",", 3));
		} catch (Throwable t) {
			throw Debuggable.reThrowable(t);
		}

	}

	/**
	 * Creates a window-closing listener for the popup.
	 *
	 * @param p the <code>JPopupMenu</code>
	 * @return the new window-closing listener
	 *
	 * @see WinListener
	 */
	protected WinListener createWinListener(JPopupMenu p) {
		if (!(p instanceof UISwingReplacement)) {
			// complain complain
		}
		return new WinListener(p);
	}
}
