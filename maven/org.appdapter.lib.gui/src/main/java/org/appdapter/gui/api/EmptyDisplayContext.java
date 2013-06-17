package org.appdapter.gui.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.appdapter.api.trigger.BT;
import org.appdapter.api.trigger.BoxPanelSwitchableView;
import org.appdapter.api.trigger.BrowserPanelGUI;
import org.appdapter.api.trigger.ITabUI;
import org.appdapter.api.trigger.NamedObjectCollection;
import org.appdapter.api.trigger.POJOCollectionListener;
import org.appdapter.api.trigger.UserResult;
import org.appdapter.core.log.Debuggable;

public class EmptyDisplayContext extends NamedObjectCollectionImpl implements BrowserPanelGUI, NamedObjectCollection {
	/**
	 * Adds a new value, if it wasn't already there
	 *
	 * @returns true if the value was added, false if the value was already there
	 */
	public BT addObject(Object value) {
		return null;
	}

	/**
	 * Removes a value, if it is there
	 *
	 * @returns true if the value was removed, false if that value wasn't in this context
	 */
	public boolean removeObject(Object value) {
		return false;
	}

	public Collection findObjectsByType(Class type) {
		return new Vector();
	}

	public boolean containsObject(Object value) {
		return false;
	}

	public void addListener(POJOCollectionListener o) {
	}

	public void removeListener(POJOCollectionListener o) {
	}

	public Collection getTriggersFromUI(Object value) {
		return new Vector();
	}

	public Object findObject(String name) {
		return null;
	}

	public String getTitleOf(Object value) {
		return "" + value;
	}

	public UserResult showError(String msg, Throwable e) {
		return Utility.showError(null, msg, e);
	}

	@Override public void renameObject(String oldName, String newName) throws PropertyVetoException {
		BT value = findOrCreateBox(newName, findObjectByName(oldName));
		value.setUniqueName(newName);
	}

	public BT findOrCreateBox(String newName, Object obj) throws PropertyVetoException {
		throw new PropertyVetoException("Cant create objects in " + this, new PropertyChangeEvent(this, "objects", null, obj));
	}

	@Override public BrowserPanelGUI getDisplayContext() {
		return Utility.getDisplayContext();
	}

	@Override public BT findOrCreateBox(Object value) {
		Debuggable.notImplemented();
		return null;
	}

	@Override public Iterator<Object> getObjects() {
		Debuggable.notImplemented();
		return null;
	}

	@Override public Object findObjectByName(String n) {
		Debuggable.notImplemented();
		return null;
	}

	@Override public NamedObjectCollection getLocalBoxedChildren() {
		Debuggable.notImplemented();
		return this;
	}

	@Override public BoxPanelSwitchableView getBoxPanelTabPane() {
		return getDisplayContext().getBoxPanelTabPane();
	}

	@Override public UserResult showScreenBox(Object value) throws Exception {
		return getDisplayContext().showScreenBox(value);
	}

	@Override public UserResult showMessage(String string) {
		return getDisplayContext().showMessage(string);
	}

	public ITabUI getLocalCollectionUI() {
		Debuggable.notImplemented();
		return null;
	}

	@Override public UserResult attachChildUI(String title, Object value) throws Exception {
		Debuggable.notImplemented();
		return null;
	}

}