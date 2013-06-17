package org.appdapter.gui.rimpl;

import javax.swing.JMenu;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.gui.api.Utility;

/**
 * A menu showing the available triggers for a boxed Pojo.
 * The valid menu actions are fetched from the boxed Pojo context
 *
 * 
 */
public class TriggerMenu extends JMenu {
	TriggerMenuController controller;

	public TriggerMenu(String title, Box box, Object object) {
		this(title, Utility.getCurrentContext(), box, object);
	}

	public TriggerMenu(String title, DisplayContext context, Box box, Object object) {
		super(title);
		controller = new TriggerMenuController(context, object, box, this);
	}
}