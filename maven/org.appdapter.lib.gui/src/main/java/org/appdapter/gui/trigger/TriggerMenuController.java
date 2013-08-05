package org.appdapter.gui.trigger;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

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

	JPopupMenu popup = null;
	JMenu menu = null;

	private TriggerMenuController(DisplayContext context0, NamedObjectCollection noc, Object object0) {

		this.object = object0;
		if (context0 == null)
			context0 = Utility.getCurrentContext();
		this.context = context0;
		if (noc == null)
			noc = context0.getLocalBoxedChildren();
		this.localCollection = noc;
		syncBoxedObject();

		triggerFactory = TriggerMenuFactory.getInstance(context);
		if (localCollection != null) {
		//	localCollection.addListener(this, false);
		}
	}

	public TriggerMenuController(DisplayContext context0, NamedObjectCollection noc, Object object0, JPopupMenu popup0) {
		this(context0, noc, object0);
		this.popup = popup0;
		initMenu();
	}

	public TriggerMenuController(DisplayContext context0, NamedObjectCollection noc, Object object0, JMenu menu0) {
		this(context0, noc, object0);
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
			Collection actions = context.getTriggersFromUI(object);
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

	private Object asBox() {
		return object;
	}

	private void syncBoxedObject() {

	}

	private void initLabelText() {
		final String label = Utility.getUniqueName(object, localCollection);
		if (menu != null) {
			menu.setText(label);
		} else {
			popup.setLabel(label);
		}
	}

	/*Box asBox() {
		return boxed.asBox();
	}*/

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

	@Override public void pojoAdded(Object obj, BT wrapper, Object senderCollection) {
		if (obj == object) {
			updateMenu();
		}
	}

	@Override public void pojoRemoved(Object obj, BT wrapper, Object senderCollection) {
		if (obj == object) {
			updateMenu();
		}
	}
}
