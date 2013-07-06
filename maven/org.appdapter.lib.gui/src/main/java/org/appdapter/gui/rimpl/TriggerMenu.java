package org.appdapter.gui.rimpl;

import javax.swing.JMenu;

import org.appdapter.api.trigger.BT;
import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.api.trigger.NamedObjectCollection;

/**
 * A menu showing the available triggers for a boxed Pojo.
 * The valid menu actions are fetched from the boxed Pojo context
 *
 * 
 */
public class TriggerMenu extends JMenu {
	TriggerMenuController controller;

	public TriggerMenu(String title, DisplayContext context, NamedObjectCollection noc, BT box, Object object) {
		super(title);
		controller = new TriggerMenuController(context, noc, object, box, this);
	}
}