package org.appdapter.gui.pojo;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.management.RuntimeErrorException;

import org.appdapter.api.trigger.BoxImpl;
import org.appdapter.gui.box.ScreenBoxImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A POJOCollection is a container of objects and corresponding POJOSwizzlers,
 * which add the concept of "name" and "selected".
 * <p>
 * 
 * Each object inside the POJOCollection has a corresponding POJOSwizzler. A
 * POJOSwizzler has a reference to the object in memory it respresents. Given an
 * object the only way to find the corresponding POJOSwizzler is to use
 * getSwizzler(Object pojo)
 * <p>
 * 
 * PropertyChangeListeners can register to find out when the selected object is
 * changed, in which case the property "selectedPOJO" will change.
 * <p>
 * 
 * POJOCollectionContextListeners can register to find out when objects are
 * added or removed.
 * 
 * @see POJOBox
 * 
 */
@SuppressWarnings("serial")
public class POJOCollectionImpl implements VetoableChangeListener, PropertyChangeListener, Serializable, POJOCollectionWithSwizzler {
	// ==== Static variables ===================
	private static Logger theLogger = LoggerFactory.getLogger(POJOCollectionImpl.class);

	// ==== Transient instance variables ===================
	transient private PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
	transient private Set collectionListeners = new HashSet();

	// ===== Serializable instance variables ================
	// Maps objects to their swizzlers
	// private Map objectsToSwizzlers = new HashMap();

	// private Map swizzlersToPOJOCollection = new HashMap();

	// An ordered list of objects
	// private List objectList = new LinkedList();
	private LinkedList<POJOBox> swizzlerList = new LinkedList<POJOBox>();
	// private LinkedList objectList = new LinkedList();

	// The currently selected object
	private Object selected = null;

	// Maps object swizzler name to object swizzler
	// private Map nameIndex = new Hashtable();

	// ============ Constructors
	// ==================================================

	public POJOCollectionImpl() {
	}

	// ==== Manipulating the collection of objects ==================

	/**
	 * {@inheritDoc}
	 */
	@Override public synchronized Object createAndAddPOJO(Class cl) throws InstantiationException, IllegalAccessException {
		// Create the object
		Object obj = cl.newInstance();

		// Add it
		addPOJO(obj);

		return obj;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	@Override public synchronized boolean addPOJO(Object obj) {
		try {
			return addPOJOMaybe(obj);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
			return containsPOJO(obj);
		}
	}

	public boolean addPOJOMaybe(Object obj) throws PropertyVetoException {
		if (containsPOJO(obj)) {
			return false;
		} else {
			// Create the object swizzler, with a unique name
			POJOBox swizzler = new ScreenBoxImpl(generateUniqueName(obj), obj);

			// Add it
			// objectsToSwizzlers.put(obj, swizzler);
			// objectList.add(obj);
			swizzlerList.add(swizzler);

			// Add myself as listener
			swizzler.addVetoableChangeListener(this);
			swizzler.addPropertyChangeListener(this);

			// Update the name index
			// nameIndex.put(swizzler.getName(), swizzler);

			// notify collectionListeners
			Iterator it = collectionListeners.iterator();
			while (it.hasNext()) {
				// @temp
				((POJOCollectionListener) it.next()).pojoAdded(obj);
			}

			return true;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public synchronized boolean removePOJO(Object obj) {
		if (containsPOJO(obj)) {
			// Find the swizzler
			POJOBox swizzler = getSwizzler(obj);

			// Remove it
			// objectsToSwizzlers.remove(obj);
			// objectList.remove(obj);
			swizzlerList.remove(swizzler);

			// Update the name index
			// nameIndex.remove(swizzler.getName());

			// notify collectionListeners
			Iterator it = collectionListeners.iterator();
			while (it.hasNext()) {
				((POJOCollectionListener) it.next()).pojoRemoved(obj);
			}
			return true;
		} else {
			return false;
		}
	}

	// ==== Queries ========================

	/**
	 * {@inheritDoc}
	 */
	@Override public Iterable<POJOBox> getSwizzlers() {
		LinkedList list = new LinkedList();
		Iterator it = swizzlerList.iterator();
		while (it.hasNext()) {
			list.add(it.next());
		}
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public int getPOJOCount() {
		return swizzlerList.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public boolean containsPOJO(Object object) {
		return containsSwizzler(getSwizzler(object));// containsKey(object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public boolean containsSwizzler(POJOBox swizzler) {
		return swizzlerList.contains(swizzler);
		// return objectsToSwizzlers.containsValue(object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public Object findPOJO(String name) {
		POJOBox swizzler = findSwizzler(name);
		if (swizzler == null) {
			return null;
		} else {
			return swizzler.getObject();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public POJOBox findSwizzler(String name) {
		for (POJOBox swizzler : getSwizzlers()) {
			if (swizzler.isNamed(name))
				return swizzler;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public POJOBox getSwizzler(Object object) {
		if (object == null)
			return null;
		if (object instanceof POJOBox)
			return (POJOBox) object;

		synchronized (swizzlerList) {
			for (POJOBox swizzler : swizzlerList) {
				if (swizzler.representsObject(object))
					return swizzler;
			}
			POJOBox swizzler;
			try {
				swizzler = makeWrapper(object);
			} catch (PropertyVetoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			swizzlerList.add(swizzler);

			// Add myself as listener
			swizzler.addVetoableChangeListener(this);
			swizzler.addPropertyChangeListener(this);

			// Update the name index
			// nameIndex.put(swizzler.getName(), swizzler);

			// notify collectionListeners
			Iterator it = collectionListeners.iterator();
			while (it.hasNext()) {
				// @temp
				((POJOCollectionListener) it.next()).pojoAdded(object);
			}
			return swizzler;
		}
	}

	private POJOBox makeWrapper(Object object) throws PropertyVetoException {
		Class gc = object.getClass();
		POJOBox swizzler = new ScreenBoxImpl(generateUniqueName(object), object);
		return swizzler;
	}

	// ===== Manipulating the selected object ===============

	/**
	 * {@inheritDoc}
	 */
	@Override public Object getSelectedPOJO() {
		return selected;
	}

	// ==== Event listener registration ======================

	/**
	 * {@inheritDoc}
	 */
	@Override public void addPropertyChangeListener(PropertyChangeListener p) {
		propSupport.addPropertyChangeListener(p);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void removePropertyChangeListener(PropertyChangeListener p) {
		propSupport.removePropertyChangeListener(p);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void addListener(POJOCollectionListener l) {
		collectionListeners.add(l);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void removeListener(POJOCollectionListener l) {
		collectionListeners.remove(l);
	}

	// ===== Property notifications (i.e. others notifying me) =========

	/**
	 * {@inheritDoc}
	 */
	@Override public synchronized void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("name")) {
			// Name has changed - so update the name index
			POJOBox object = (POJOBox) (evt.getSource());
			if (containsPOJO(object.getObject())) {
				String newName = (String) evt.getNewValue();
				String oldName = (String) evt.getOldValue();
				if (oldName != null) {
					// nameIndex.remove(oldName);
				}
				// nameIndex.put(newName, object);
			}

		} else if (evt.getPropertyName().equals("selected")) {
			// Selection has changed. Call notifySelected or notifyDeselected
			// to update my internal state
			Boolean newValue = (Boolean) evt.getNewValue();
			POJOBox swizzler = (POJOBox) evt.getSource();
			Object object = swizzler.getObject();
			if (containsPOJO(object)) {
				if (newValue.equals(new Boolean(true))) {
					try {
						setSelectedPOJO(object);
					} catch (PropertyVetoException err) {
						theLogger.warn("The POJOCollection was notified that a object has been selected, and when trying to update the internal state a PropertyVetoException occurred", err);
					}
				} else if (newValue.equals(new Boolean(false))) {
					try {
						setSelectedPOJO(null);
					} catch (PropertyVetoException err) {
						theLogger.warn("The POJOCollection was notified that a object has been deselected, and when trying to update the internal state a PropertyVetoException occurred", err);
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public synchronized void setSelectedPOJO(Object object) throws PropertyVetoException {
		object = Utility.asPOJO(object);
		if (selected != object && containsPOJO(object)) {

			// Deselect the old swizzler (if any)
			Object oldSelected = selected;
			POJOBox oldSwizzler = getSwizzler(oldSelected);

			// Update my "selected" instance variable
			selected = object;

			// Fire a property change
			propSupport.firePropertyChange("selected", oldSelected, selected);

		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
		if (evt.getPropertyName().equals("name")) {
			// The name of a swizzler has changed. Make sure there are no name
			// collisions
			POJOBox swizzler = (POJOBox) (evt.getSource());
			Object object = swizzler.getObject();
			if (containsPOJO(object)) {
				String name = (String) evt.getNewValue();
				POJOBox otherSwizzler = findSwizzler(name);
				if (otherSwizzler != null && otherSwizzler != swizzler) {
					throw new PropertyVetoException("Another object already has the name '" + name + "'", evt);
				}
			}
		}
	}

	// ====== Save and load operations ================================

	/**
	 * {@inheritDoc}
	 */
	@Override public void save(File destination) throws IOException {
		theLogger.debug("Saving collection to " + destination);
		FileOutputStream fileOut = new FileOutputStream(destination);
		ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
		objectOut.writeObject(this);
		fileOut.close();
		theLogger.debug("Successfully saved!");
	}

	static public POJOCollectionWithSwizzler load(File source) throws IOException, ClassNotFoundException {
		FileInputStream fileIn = new FileInputStream(source);
		ObjectInputStream objectIn = new ObjectInputStream(fileIn);
		POJOCollectionImpl b = (POJOCollectionImpl) objectIn.readObject();
		fileIn.close();

		b.initAfterLoading();
		return b;
	}

	/**
	 * Tells the collection that it has just been loaded from a file and needs
	 * to initialize itself. For example update transient instance variables
	 * that were "lost" during the serialization, and add itself as listener to
	 * all the objects.
	 */
	void initAfterLoading() {
		if (collectionListeners == null) {
			collectionListeners = new HashSet();
		}

		if (propSupport == null) {
			propSupport = new PropertyChangeSupport(this);
		}

		for (POJOBox b : getSwizzlers()) {
			b.addVetoableChangeListener(this);
			b.addPropertyChangeListener(this);
		}
	}

	// ======== Utility methods ==================================

	/**
	 * Generates a default name for the given object, while will be something
	 * like "Button1", "Button2", etc.
	 */
	public String generateUniqueName(Object object) {
		if (object instanceof Class) {
			return ((Class) object).getCanonicalName();
		}
		return Utility.generateUniqueName(object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public Object getSelectedBean() {
		// TODO Auto-generated method stub
		return selected;
	}

	@Override public POJOCollection getCollection() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override public POJOCollectionWithSwizzler getCollectionWithSwizzler() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override public <T> Collection<T> getPOJOCollectionOfType(Class<T> type) {
		Set result = new HashSet();
		Iterator it = swizzlerList.iterator();
		while (it.hasNext()) {
			POJOBox obj = (POJOBox) it.next();
			if (obj.isInstance(type)) {
				result.add(obj);
			}
		}
		return result;
	}

	@Override public boolean containsSwizzler(ScreenBoxImpl swizzler) {
		return containsPOJO(swizzler.getObject());
	}

	@Override public BoxImpl findBoxByName(String name) {
		return findSwizzler(name);
	}

	@Override public ScreenBoxImpl findOrCreateBox(Object object) {
		return (ScreenBoxImpl) getSwizzler(object);
	}

	@Override public POJOApp getPOJOApp() {
		return Utility.getCurrentContext();
	}
}