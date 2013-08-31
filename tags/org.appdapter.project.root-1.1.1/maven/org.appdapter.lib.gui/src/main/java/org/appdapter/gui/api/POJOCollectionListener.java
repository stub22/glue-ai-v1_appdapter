package org.appdapter.gui.api;

import java.util.Collection;

import org.appdapter.api.trigger.AnyOper.UIProvider;

public interface POJOCollectionListener extends UIProvider {
	public void pojoAdded(Object obj, BT box, Object senderCollection);

	public void pojoRemoved(Object obj, BT box, Object senderCollection);
}