package org.appdapter.gui.trigger;

import javax.swing.JPopupMenu;

import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.NamedObjectCollection;

/**
 * A Popup menu for a object. The valid actions are
 * fetched using the objects context.
 */
public class TriggerPopupMenu extends JPopupMenu {
	final TriggerMenuController controller;

	public TriggerPopupMenu(DisplayContext context, NamedObjectCollection noc, BT box, Object object) {
		controller = new TriggerMenuController(context, noc, object, box, this);
	}

}