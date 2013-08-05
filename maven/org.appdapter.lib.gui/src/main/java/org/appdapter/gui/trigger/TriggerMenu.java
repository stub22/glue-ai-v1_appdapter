package org.appdapter.gui.trigger;

import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.swing.SafeJMenu;

/**
 * A menu showing the available triggers for a boxed Pojo.
 * The valid menu actions are fetched from the boxed Pojo context
 *
 * 
 */
public class TriggerMenu extends SafeJMenu {
	TriggerMenuController controller;

	public TriggerMenu(String title, DisplayContext context, NamedObjectCollection noc, Object box) {
		super(true, title, box);
		controller = new TriggerMenuController(context, noc, box, this);
	}
}