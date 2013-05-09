package org.appdapter.gui.pojo;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;

import org.appdapter.api.trigger.BoxImpl;
import org.appdapter.gui.box.ScreenBoxImpl;

public interface POJOCollectionWithSwizzler extends POJOCollection {

	/**
	 * Creates a new object of the given class and adds to this collection. The
	 * given class must have an empty constructor.
	 * 
	 * @throws InstantiationException
	 *             if the given Class represents an abstract class, an
	 *             interface, an array class, a primitive type, or void; or if
	 *             the instantiation fails for some other reason
	 * @throws IllegalAccessException
	 *             if the given class or initializer is not accessible.
	 * 
	 * @returns the newly created ScreenBoxImpl
	 */
	public abstract Object createAndAddPOJO(Class cl) throws InstantiationException, IllegalAccessException;

	/**
	 * Returns an iterator over all the object swizzlers. NOTE - this could be a
	 * bit slow! Avoid whenever possible. The code can be optimized for this,
	 * but it isn't right now.
	 */
	public abstract Iterable<POJOSwizzler> getSwizzlers();

	/**
	 * Returns the current number of objects in the collection
	 */
	//public abstract int getPOJOCount();

	/**
	 * Checks if this collection contains the given object swizzler
	 */
	public abstract boolean containsSwizzler(ScreenBoxImpl swizzler);

	/**
	 * Returns the swizzler with the given name, or null if none.
	 */
	public abstract BoxImpl findBoxByName(String name);

	/**
	 * Returns the swizzler corresponding to the given object, i.e the
	 * ScreenBoxImpl who's object corresponds to the given one. Returns null if
	 * the ObjectNavigator does not contain the given object.
	 */
	public abstract ScreenBoxImpl getBoxForObject(Object object);

	/**
	 * Returns the currently selected object, or null if none.
	 */
	public abstract Object getSelectedPOJO();

	/**
	 * Listeners will be notifed when the currently object selection is changed.
	 */
	public abstract void addPropertyChangeListener(PropertyChangeListener p);

	/**
	 * Listeners will be notifed when the currently object selection is changed.
	 */
	public abstract void removePropertyChangeListener(PropertyChangeListener p);

	/**
	 * Listeners will find out when objects are added or removed
	 */
	@Override
	public abstract void addListener(POJOCollectionListener l);

	/**
	 * Listeners will find out when objects are added or removed
	 */
	@Override
	public abstract void removeListener(POJOCollectionListener l);

	/**
	 * This is used for ScreenBoxImpls to tell their POJOCollection that a
	 * property such as "name" or "selected" has changed. The POJOCollection
	 * will update its state as necessary.
	 */
	public abstract void propertyChange(PropertyChangeEvent evt);

	/**
	 * Makes the given object the currently selected one. The previously
	 * selected object (if any) will be deselected, and a property change event
	 * will be fired.
	 * 
	 * @throws PropertyVetoException
	 *             if someone refused to let the selected object change
	 */
	public abstract void setSelectedPOJO(Object object) throws PropertyVetoException;

	/**
	 * This is used for ScreenBoxImpls to tell their POJOCollection that a
	 * property such as "name" or "selected" is about to change, allowing the
	 * POJOCollection to fire a PropertyVetoException to stop the change if it
	 * likes.
	 * <p>
	 * 
	 * This would happen, for example, if someone is trying to rename a object
	 * to a name that another object within this collection already has.
	 */
	public abstract void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException;

	public abstract void save(File destination) throws IOException;

	public abstract Object getSelectedBean();

	POJOSwizzler findSwizzler(String name);

	POJOSwizzler getSwizzler(Object object);

	boolean containsSwizzler(POJOSwizzler swizzler);

}