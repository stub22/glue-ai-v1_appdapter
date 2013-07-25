package org.appdapter.gui.trigger;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.appdapter.api.trigger.Box;
import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.api.POJOCollectionListener;
import org.appdapter.gui.browse.Utility;

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
public class TriggerMenuController implements POJOCollectionListener {
	NamedObjectCollection localCollection;
	final DisplayContext context;
	final TriggerMenuFactory triggerFactory;

	Object object;
	BT boxed;

	JPopupMenu popup = null;
	JMenu menu = null;

	private TriggerMenuController(DisplayContext context0, NamedObjectCollection noc, Object object0, BT box) {
		this.boxed = box;
		this.object = object0;
		if (context0 == null)
			context0 = Utility.getCurrentContext();
		this.context = context0;
		if (noc == null)
			noc = context0.getLocalBoxedChildren();
		this.localCollection = noc;

		triggerFactory = TriggerMenuFactory.getInstance(context);
		syncBoxedObject();
		if (localCollection != null) {
			localCollection.addListener(this);
		}
	}

	public TriggerMenuController(DisplayContext context0, NamedObjectCollection noc, Object object0, BT box, JPopupMenu popup0) {
		this(context0, noc, object0, box);
		this.popup = popup0;
		initMenu();
	}

	public TriggerMenuController(DisplayContext context0, NamedObjectCollection noc, Object object0, BT box, JMenu menu0) {
		this(context0, noc, object0, box);
		this.menu = menu0;
		initMenu();
	}

	void updateMenu() {
		if (popup != null)
			popup.removeAll();
		else
			menu.removeAll();
		initMenu();
	}

	private void initMenu() {
		initLabelText();
		if (context != null) {
			Collection actions = context.getTriggersFromUI((BT) boxed, object);
			Iterator it = actions.iterator();
			while (it.hasNext()) {
				Action action = (Action) it.next();
				addAction(action);
			}
		}
		if (popup != null)
			triggerFactory.addTriggersToPopup(asBox(), popup);
		if (menu != null)
			triggerFactory.addTriggersToPopup(asBox(), menu);

	}

	private void syncBoxedObject() {
		if (boxed == null && object != null) {
			if (object instanceof BT) {
				boxed = (BT) object;
			} else {
				if (localCollection != null) {
					boxed = localCollection.findOrCreateBox(object);
				} else {
					boxed = Utility.asWrapped(object);
				}
			}
		}
		if (object == null && this.boxed != null)
			object = boxed.getValueOrThis();

		if (object == null) {
			object = Utility.dref(boxed);
		}
	}

	private void initLabelText() {
		final String label = Utility.getUniqueName(object, asBox(), localCollection);
		if (menu != null) {
			menu.setText(label);
		} else {
			popup.setLabel(label);
		}
	}

	Box asBox() {
		return boxed.asBox();
	}

	void addAction(Action a) {
		if (popup != null) {
			triggerFactory.addMenuItem(a, asBox(), popup);
		} else {
			triggerFactory.addMenuItem(a, asBox(), menu);
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

	@Override public void pojoAdded(Object obj, BT box) {
		if (obj == object) {
			updateMenu();
		}
	}

	@Override public void pojoRemoved(Object obj, BT box) {
		if (obj == object) {
			updateMenu();
		}
	}
}
