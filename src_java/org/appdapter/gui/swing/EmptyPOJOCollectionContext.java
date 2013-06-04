package org.appdapter.gui.swing;

import java.util.Collection;
import java.util.Vector;

import org.appdapter.gui.box.BoxPanelSwitchableViewImpl;
import org.appdapter.gui.box.ScreenBoxImpl;
import org.appdapter.gui.pojo.POJOCollection;
import org.appdapter.gui.pojo.POJOCollectionListener;

public class EmptyPOJOCollectionContext extends BoxPanelSwitchableViewImpl implements POJOCollection {
	/**
	 * Adds a new object, if it wasn't already there
	 * 
	 * @returns true if the object was added, false if the object was already
	 *          there
	 */
	@Override public ScreenBoxImpl addObject(Object object) {
		return null;
	}

	/**
	 * Removes a object, if it is there
	 * 
	 * @returns true if the object was removed, false if that object wasn't in
	 *          this context
	 */
	@Override public boolean removeObject(Object object) {
		return false;
	}

	@Override public <T> Collection findBoxByType(Class<T> type) {
		return new Vector();
	}

	@Override public boolean containsObject(Object object) {
		return false;
	}

	@Override public void addListener(POJOCollectionListener o) {
	}

	@Override public void removeListener(POJOCollectionListener o) {
	}

	@Override public Collection getTriggersFromUI(Object object) {
		return new Vector();
	}

	public String getTitleOf(Object object) {
		return "" + object;
	}

	@Override public void showError(String msg, Throwable err) {
		new ErrorDialog(msg, err).show();
	}

}