package org.appdapter.gui.swing;

import java.util.Collection;
import java.util.Vector;

import org.appdapter.gui.box.POJOApp;
import org.appdapter.gui.box.ScreenBoxPanel;
import org.appdapter.gui.pojo.NamedObjectCollection;
import org.appdapter.gui.pojo.POJOCollection;
import org.appdapter.gui.pojo.POJOCollectionListener;
import org.appdapter.gui.pojo.Utility;

public class EmptyPOJOCollectionContext implements POJOApp, POJOCollection {
	/**
	 * Adds a new object, if it wasn't already there
	 * 
	 * @returns true if the object was added, false if the object was already
	 *          there
	 */
	@Override public boolean addPOJO(Object object) {
		return false;
	}

	/**
	 * Removes a object, if it is there
	 * 
	 * @returns true if the object was removed, false if that object wasn't in
	 *          this context
	 */
	@Override public boolean removePOJO(Object object) {
		return false;
	}

	@Override public <T> Collection<T> getPOJOCollectionOfType(Class<T> type) {
		return new Vector();
	}

	@Override public boolean containsPOJO(Object object) {
		return false;
	}

	@Override public void showScreenBox(Object object) {
	}

	@Override public void addListener(POJOCollectionListener o) {
	}

	@Override public void removeListener(POJOCollectionListener o) {
	}

	@Override public Collection getCollectionUtilAppActions(Object object) {
		return new Vector();
	}

	@Override public Object findObjectByName(String name) {
		return null;
	}

	@Override public String getBoxName(Object object) {
		return "" + object;
	}

	@Override public void showError(String msg, Throwable err) {
		new ErrorDialog(msg, err).show();
	}

	@Override public int getPOJOCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override public POJOCollection getCollection() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override public NamedObjectCollection getNamedObjectCollection() {
		// TODO Auto-generated method stub
		return Utility.getCurrentContext2();
	}

	@Override public void reload() {
		// TODO Auto-generated method stub

	}

	@Override public ScreenBoxPanel findOrCreateComponent(Object object, boolean b) {
		// TODO Auto-generated method stub
		return null;
	}
}