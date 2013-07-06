package org.appdapter.api.trigger;

public interface POJOCollectionListener extends UIProvider {
	public void pojoAdded(Object obj, BT box);

	public void pojoRemoved(Object obj, BT box);
}