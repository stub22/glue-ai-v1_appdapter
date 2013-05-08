package org.appdapter.gui.swing;

import javax.swing.JMenu;

import org.appdapter.gui.objbrowser.model.POJOCollectionWithBoxContext;
import org.appdapter.gui.objbrowser.model.Utility;

/**
 * A menu showing the available triggers for a boxed Pojo.
 * The valid menu actions are fetched from the boxed Pojo context
 *
 * 
 */
public class TriggersMenu extends JMenu{
  POJOMenuController controller;

  public TriggersMenu(Object object) {
    this(Utility.getCurrentInstances(), object);
  }
  public TriggersMenu(POJOCollectionWithBoxContext context, Object object) {
    controller = new POJOMenuController(context, object, this);
  }
}