package org.appdapter.gui.api;

import org.appdapter.gui.api.Ontologized.UIProvider;

public interface POJOCollectionListener extends UIProvider {
	public void pojoAdded(Object obj, BT box);

	public void pojoRemoved(Object obj, BT box);
}