package org.appdapter.gui.rimpl;

import javax.swing.JPopupMenu;

import org.appdapter.api.trigger.BT;
import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.api.trigger.GetObject;
import org.appdapter.gui.api.Utility;

/**
 * A Popup menu for a object. The valid actions are
 * fetched using the objects context.
 */
public class TriggerPopupMenu extends JPopupMenu {
	final TriggerMenuController controller;

	public TriggerPopupMenu(BT box, Object object) {
		this(Utility.getCurrentContext(), box, object);
	}

	public TriggerPopupMenu(DisplayContext context, BT box, Object object) {
		controller = new TriggerMenuController(context, object, box, this);
	}

}