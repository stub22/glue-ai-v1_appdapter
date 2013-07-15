package org.appdapter.gui.trigger;

import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.plaf.MenuItemUI;

import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.swing.SafeJMenuItem;

/**
 * A Popup menu for a object. The valid actions are
 * fetched using the objects context.
 */
public class TriggerPopupMenu extends JPopupMenu {
	final TriggerMenuController controller;

	public TriggerPopupMenu(DisplayContext context, NamedObjectCollection noc, BT box, Object object) {
		controller = new TriggerMenuController(context, noc, object, box, this);
	}

	/**
	 * Appends a new menu item to the end of the menu which 
	 * dispatches the specified <code>Action</code> object.
	 *
	 * @param a the <code>Action</code> to add to the menu
	 * @return the new menu item
	 * @see Action
	 */
	public JMenuItem add(Action a) {
		JMenuItem mi = createActionComponent(a);
		mi.setAction(a);
		add(mi);
		String ttt = "" + a.getValue("tooltip");
		mi.setToolTipText(ttt);
		setToolTipText(ttt);
		return mi;
	}

	/**
	 * Factory method which creates the <code>JMenuItem</code> for
	 * <code>Actions</code> added to the <code>JPopupMenu</code>.
	 *
	 * @param a the <code>Action</code> for the menu item to be added
	 * @return the new menu item
	 * @see Action
	 *
	 * @since 1.3
	 */
	protected JMenuItem createActionComponent(Action a) {
		JMenuItem mi = new SafeJMenuItem() {
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

}