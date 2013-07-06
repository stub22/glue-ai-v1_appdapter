package org.appdapter.gui.rimpl;

import javax.swing.JPopupMenu;

import org.appdapter.api.trigger.BT;
import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.api.trigger.NamedObjectCollection;

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