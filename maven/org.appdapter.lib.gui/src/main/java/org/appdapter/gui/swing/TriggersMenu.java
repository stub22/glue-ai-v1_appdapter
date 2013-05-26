package org.appdapter.gui.swing;

import javax.swing.JMenu;

import org.appdapter.gui.pojo.POJOApp;
import org.appdapter.gui.pojo.Utility;

/**
 * A menu showing the available triggers for a boxed Pojo.
 * The valid menu actions are fetched from the boxed Pojo context
 *
 * 
 */
public class TriggersMenu extends JMenu {
	POJOMenuController controller;

	public TriggersMenu(String title, Object object) {
		this(title, Utility.getCurrentContext(), object);
	}

	public TriggersMenu(String title, POJOApp context, Object object) {
		super(title);
		controller = new POJOMenuController(context, object, this);
	}
}