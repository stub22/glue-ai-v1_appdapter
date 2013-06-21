package org.appdapter.gui.rimpl;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.appdapter.api.trigger.BT;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.api.trigger.NamedObjectCollection;
import org.appdapter.api.trigger.POJOCollectionListener;

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
class TriggerMenuController implements POJOCollectionListener {
	NamedObjectCollection context;
	DisplayContext appcontext;

	Object object;
	Box boxed;

	JPopupMenu popup = null;
	JMenu menu = null;

	public TriggerMenuController(DisplayContext context0, Object object, Box box, JPopupMenu popup0) {
		this.boxed = box;
		if (object == null && this.boxed != null)
			object = null;//boxed.getValue();
		appcontext = context0;
		this.context = context0.getLocalBoxedChildren();
		if (context != null) {
			context.addListener(this);
		}
		this.object = object;

		this.popup = popup0;

		if (object != null) {
			if (context == null) {
				popup.setLabel("" + object);
			} else {
				popup.setLabel(context.getTitleOf(object));
			}
		} else {
			if (context == null) {
				popup.setLabel("" + boxed);
			} else {
				popup.setLabel(context.getTitleOf(boxed));
			}
		}
		initMenu();
	}

	public TriggerMenuController(DisplayContext context0, Object object, Box box, JMenu menu0) {
		this.boxed = box;
		this.context = context0.getLocalBoxedChildren();
		if (context != null) {
			context.addListener(this);
		}
		this.object = object;
		this.menu = menu0;

		if (object != null) {
			if (context == null) {
				menu.setText("" + object);
			} else {
				menu.setText(context.getTitleOf(object));
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
			if (boxed == null)
				boxed = context.findOrCreateBox(object);
			if (object == null) {
				object = boxed;
			}
		}
		if (appcontext != null) {
			Collection actions = appcontext.getTriggersFromUI((BT) boxed, object);
			Iterator it = actions.iterator();
			while (it.hasNext()) {
				Action action = (Action) it.next();
				addAction(action);
			}
			return;
		}
		TriggerMenuFactory factor = TriggerMenuFactory.getInstance(object);
		if (popup != null)
			factor.addTriggersToPopup(boxed, popup);
		if (menu != null)
			factor.addTriggersToPopup(boxed, menu);

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

	@Override public void pojoAdded(Object obj) {
		if (obj == object) {
			updateMenu();
		}
	}

	@Override public void pojoRemoved(Object obj) {
		if (obj == object) {
			updateMenu();
		}
	}
}
