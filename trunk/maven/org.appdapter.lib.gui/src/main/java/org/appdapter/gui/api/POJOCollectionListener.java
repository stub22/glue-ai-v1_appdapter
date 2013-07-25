package org.appdapter.gui.api;

import org.appdapter.api.trigger.AnyOper.UIProvider;

public interface POJOCollectionListener extends UIProvider {
	public void pojoAdded(Object obj, BT box);

	public void pojoRemoved(Object obj, BT box);
}