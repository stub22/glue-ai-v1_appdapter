package org.appdapter.gui.trigger;

import javax.swing.JMenu;

import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.NamedObjectCollection;

/**
 * A menu showing the available triggers for a boxed Pojo.
 * The valid menu actions are fetched from the boxed Pojo context
 *
 * 
 */
public class TriggerMenu extends SafeJMenu {
	TriggerMenuController controller;

	public TriggerMenu(String title, DisplayContext context, NamedObjectCollection noc, BT box, Object object) {
		super(title);
		controller = new TriggerMenuController(context, noc, object, box, this);
	}
}