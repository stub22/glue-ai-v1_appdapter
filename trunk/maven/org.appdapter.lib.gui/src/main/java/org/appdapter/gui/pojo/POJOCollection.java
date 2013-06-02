package org.appdapter.gui.pojo;

import java.util.Collection;

public interface POJOCollection {


	public POJOCollection getCollection();

	public NamedObjectCollection getNamedObjectCollection();
	
	/**
	 * Adds the given object to the ObjectNavigator, if it does not already
	 * exist.
	 * 
	 * @returns true if the object was added, i.e. if it didn't already exist.
	 */
	public abstract boolean addPOJO(Object obj);

	/**
	 * Removes the given object, if it is inside this collection. If not,
	 * nothing happens.
	 * <p>
	 * 
	 * POJOListeners will be notified.
	 * <p>
	 * 
	 * If the object was selected, the current selection will change to null and
	 * property change listeners will be notified.
	 * <p>
	 * 
	 */
	public abstract boolean removePOJO(Object obj);

	/**
	 * Returns the current number of objects in the collection
	 */
	public abstract int getPOJOCount();
	
	/**
	 * Checks if this collection contains the given object
	 */
	public abstract boolean containsPOJO(Object object);


	/**
	 * Returns all objects representing objects that are an instance of the
	 * given class or interface, either directly or indirectly.
	 */
	public abstract <T> Collection<T> getPOJOCollectionOfType(Class<T> type);

	/**
	 * Returns the object with the given name, or null if none.
	 */
	public abstract Object findObjectByName(String name);

	/**
	 * Listeners will be notifed when the currently object selection is changed.
	 */
	//public abstract void addPropertyChangeListener(PropertyChangeListener p);

	/**
	 * Listeners will be notifed when the currently object selection is changed.
	 */
	//public abstract void removePropertyChangeListener(PropertyChangeListener p);

	/**
	 * Listeners will find out when objects are added or removed
	 */
	public abstract void addListener(POJOCollectionListener l);

	/**
	 * Listeners will find out when objects are added or removed
	 */
	public abstract void removeListener(POJOCollectionListener l);



}