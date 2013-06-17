package org.appdapter.api.trigger;



public interface POJOCollectionListener extends UIProvider {
	public void pojoAdded(Object obj);

	public void pojoRemoved(Object obj);
}