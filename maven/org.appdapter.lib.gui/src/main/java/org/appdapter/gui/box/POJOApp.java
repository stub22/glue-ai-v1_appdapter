package org.appdapter.gui.box;

import java.util.Collection;

import org.appdapter.gui.demo.Reloadable;
import org.appdapter.gui.pojo.NamedObjectCollection;
import org.appdapter.gui.pojo.POJOCollectionListener;

/**
 * Represents any kind of object-container environment with a Session
 * 
 * 
 */
public interface POJOApp extends Reloadable {

	/**
	 * Opens up a GUI to show the details of the given object
	 */
	public void showScreenBox(Object object) throws Exception;

	public String getBoxName(Object object);

	/**
	 * Returns all actions that can be carried out on the given object
	 */
	public Collection getCollectionUtilAppActions(Object object);

	/**
	 * Displays the given error message somehow
	 */
	public void showError(String msg, Throwable err);

	public Object findObjectByName(String n);

	public NamedObjectCollection getNamedObjectCollection();

	public void addListener(POJOCollectionListener cl);

	public void removeListener(POJOCollectionListener cl);

	public ScreenBoxPanel findOrCreateComponent(Object object, boolean b);

}
