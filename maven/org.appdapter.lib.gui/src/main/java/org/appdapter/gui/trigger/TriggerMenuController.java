package org.appdapter.gui.trigger;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.appdapter.api.trigger.GetObject;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.Convertable;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.api.POJOCollectionListener;
import org.appdapter.gui.browse.Utility;
import static org.appdapter.core.convert.ReflectUtils.*;

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
public class TriggerMenuController implements POJOCollectionListener, Convertable {
	NamedObjectCollection localCollection;
	final DisplayContext context;
	final TriggerMenuFactory triggerFactory;

	Collection objects;

	JPopupMenu popup = null;
	JMenu menu = null;
	public MouseEvent originalMouseEvent;

	private TriggerMenuController(DisplayContext context0, MouseEvent e, NamedObjectCollection noc, Collection object0) {
		this.originalMouseEvent = e;
		this.objects = object0;
		if (context0 == null)
			context0 = Utility.getCurrentContext();
		this.context = context0;
		if (noc == null)
			noc = context0.getLocalBoxedChildren();
		this.localCollection = noc;
		triggerFactory = TriggerMenuFactory.getInstance(context);
		if (localCollection != null) {
			//	localCollection.addListener(this, false);
		}
	}

	public TriggerMenuController(DisplayContext context0, MouseEvent e, NamedObjectCollection noc, Collection object0, JPopupMenu popup0) {
		this(context0, e, noc, object0);
		this.popup = popup0;
		addObjectsMenu();
	}

	public TriggerMenuController(DisplayContext context0, MouseEvent e, NamedObjectCollection noc, Collection object0, JMenu menu0) {
		this(context0, e, noc, object0);
		this.menu = menu0;
		addObjectsMenu();
	}

	void updateMenu() {
		if (popup != null)
			popup.removeAll();
		else
			menu.removeAll();
		addObjectsMenu();
	}

	private void addObjectsMenu() {
		if (objects == null) {
			objects = new ArrayList();
			return;
		}
		for (Object o : objects) {
			addObjectMenu(o);
		}

	}

	public void addObjectMenu(Object o) {
		if (o == null)
			return;
		if (!objects.contains(o))
			objects.add(o);
		initLabelText(o);
		if (context != null) {
			Collection actions = context.getTriggersFromUI(o);
			Iterator it = actions.iterator();
			while (it.hasNext()) {
				Action action = (Action) it.next();
				addAction(action);
			}
		}
		if (popup != null)
			triggerFactory.addTriggersToPopup(o, popup);
		if (menu != null)
			triggerFactory.addTriggersToPopup(o, menu);

	}

	private Object asBox() {
		return this;
	}

	private void syncBoxedObject() {

	}

	private void initLabelText(Object object) {
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
		if (objects.contains(obj)) {
			updateMenu();
		}
	}

	@Override public void pojoRemoved(Object obj, BT wrapper, Object senderCollection) {
		if (objects.contains(obj)) {
			updateMenu();
		}
	}

	@Override public <T> boolean canConvert(Class<T> c) {
		return ReflectUtils.canConvert(c, getObjects());
	}

	@Override public <T> T convertTo(Class<T> c) {
		return ReflectUtils.convertTo(c, getObjects());
	}

	private Iterable<?> getObjects() {
		return objects;
	}

}
