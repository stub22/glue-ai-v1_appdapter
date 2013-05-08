package org.appdapter.gui.swing;

import javax.swing.JPopupMenu;

import org.appdapter.gui.objbrowser.model.POJOCollectionWithBoxContext;
import org.appdapter.gui.objbrowser.model.Utility;

/**
 * A Popup menu for a object. The valid actions are
 * fetched using the objects context.
 */
public class POJOPopupMenu extends JPopupMenu{
  POJOMenuController controller;

  public POJOPopupMenu(Object object) {
    this(Utility.getCurrentInstances(), object);
  }

  public POJOPopupMenu(POJOCollectionWithBoxContext context, Object object) {
    controller = new POJOMenuController(context, object, this);
  }

}