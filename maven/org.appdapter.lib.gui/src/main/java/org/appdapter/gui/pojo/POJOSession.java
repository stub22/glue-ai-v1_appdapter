package org.appdapter.gui.pojo;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.gui.swing.EmptyPOJOCollectionContext;

public interface POJOSession {

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
	 * Returns the current number of objects in the collection
	 */
	//public abstract int getPOJOCount();

	/**
	 * Checks if this collection contains the given object box
	 */
	public abstract boolean containsBox(Box box);

	/**
	 * Returns the box with the given name, or null if none.
	 */
	public abstract POJOBox findBoxByName(String name);

	/**
	 * Returns the box corresponding to the given object, i.e the
	 * ScreenBoxImpl who's object corresponds to the given one. Returns null if
	 * the ObjectNavigator does not contain the given object.
	 * @throws PropertyVetoException 
	 */
	public abstract POJOBox findOrCreateBox(Object object) throws PropertyVetoException;

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
	//	@Override 
	public abstract void addListener(POJOCollectionListener l);

	/**
	 * Listeners will find out when objects are added or removed
	 */
	//@Override 
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

	public abstract Iterable<POJOBox> getBoxes();

	Object findObjectByName(String name);

	public abstract String getPOJOName(Object object);

	static interface POJOApp {
		/**
		 * Opens up a GUI to show the details of the given object
		 */
		public void showScreenBox(Object object) throws Exception;

		//public String getPOJOName(Object object);

		/**
		 * Returns all actions that can be carried out on the given object
		 */
		//public Collection getActions(Object object);

		/**
		 * Displays the given error message somehow
		 */
		public void showError(String msg, Throwable err);

		public void reload();

		POJOCollection getCollection();

		public POJOSession getPOJOSession();

		public POJOSession getPOJOApp();

	}

	public abstract POJOApp getPOJOApp();

	public abstract POJOCollection getCollection();

	public java.util.List<Trigger> getActions(Object object);
}