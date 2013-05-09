package org.appdapter.gui.pojo;

import java.util.Collection;

/**
 * Represents any kind of object-container environment with a Session
 * 
 * 
 */
public interface POJOCollectionWithBoxContext extends POJOCollection, Reloadable {

	/**
	 * Opens up a GUI to show the details of the given object
	 */
	public void showScreenBox(Object object) throws Exception;

	public String getPOJOName(Object object);

	/**
	 * Returns all actions that can be carried out on the given object
	 */
	public Collection getActions(Object object);

	/**
	 * Displays the given error message somehow
	 */
	public void showError(String msg, Throwable err);

	// public POJOCollectionWithSwizzler getWithSwizzler();
}
