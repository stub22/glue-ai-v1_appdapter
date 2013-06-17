package org.appdapter.gui.rimpl;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.gui.api.Utility;

import com.jidesoft.swing.JidePopupMenu;

/**
 * A Popup menu for a object. The valid actions are
 * fetched using the objects context.
 */
public class TriggerPopupMenu extends JidePopupMenu {
	final TriggerMenuController controller;

	public TriggerPopupMenu(Box box, Object object) {
		this(Utility.getCurrentContext(), box, object);
	}

	public TriggerPopupMenu(DisplayContext context, Box box, Object object) {
		controller = new TriggerMenuController(context, object, box, this);
	}

}