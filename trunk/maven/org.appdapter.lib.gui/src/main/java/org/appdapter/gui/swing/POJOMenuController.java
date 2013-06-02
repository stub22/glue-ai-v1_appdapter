package org.appdapter.gui.swing;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.appdapter.gui.box.POJOApp;
import org.appdapter.gui.pojo.POJOCollectionListener;

/**
 * The controller class for a object menu (JMenu or JPopupMenu), showing the list
 * of available actions for a given object. <p>
 *
 * The list of available actions is fetched from the
 * given POJOCollectionContext. <p>
 *
 * If the object or context is null the menu will be empty.
 *
 * 
 */
class POJOMenuController implements POJOCollectionListener {
  POJOApp context;

  Object object;

  JPopupMenu popup = null;
  JMenu menu = null;

  public POJOMenuController(POJOApp context, Object object, JPopupMenu popup) {
    this.context = context;
    if (context != null) {
      context.addListener(this);
    }
    this.object = object;
    this.popup = popup;

    if (object != null) {
      if (context == null) {
        popup.setLabel("" + object);
      } else {
        popup.setLabel(context.getBoxName(object));
      }
      initMenu();
    }
  }

  public POJOMenuController(POJOApp context, Object object, JMenu menu) {
    this.context = context;
    if (context != null) {
      context.addListener(this);
    }

    this.object = object;
    this.menu = menu;

    if (object != null) {
      if (context == null) {
        menu.setText("" + object);
      } else {
        menu.setText(context.getBoxName(object));
      }
      initMenu();
    }
  }

  void updateMenu() {
    if (popup != null)
      popup.removeAll();
    else
      menu.removeAll();
    initMenu();
  }

  private void initMenu() {
    if (context != null) {
      Collection actions = context.getCollectionUtilAppActions(object);
      Iterator it = actions.iterator();
      while(it.hasNext()) {
        Action action = (Action) it.next();
        addAction(action);
      }
    }
  }

  void addAction(Action a) {
    if (popup != null) {
      popup.add(a);
    } else {
      menu.add(a);
    }
  }

/*  void removeAction(Action a) {
    if (popup != null) {
      popup.remove(a);
    } else {
      menu.remove(a);
    }
  }
*/

  @Override
public void pojoAdded(Object obj) {
    if (obj == object) {
      updateMenu();
    }
  }

  @Override
public void pojoRemoved(Object obj) {
    if (obj == object) {
      updateMenu();
    }
  }
}
