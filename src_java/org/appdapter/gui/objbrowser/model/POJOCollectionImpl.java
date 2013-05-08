package org.appdapter.gui.objbrowser.model;

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

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
 * @see POJOSwizzler
 * 
 */
@SuppressWarnings("serial")
public class POJOCollectionImpl implements VetoableChangeListener,
		PropertyChangeListener, Serializable, POJOCollection {
	// ==== Static variables ===================
	private static Logger theLogger = LoggerFactory.getLogger(POJOCollectionImpl.class);

	// ==== Transient instance variables ===================
	transient private PropertyChangeSupport propSupport = new PropertyChangeSupport(
			this);
	transient private Set collectionListeners = new HashSet();

	// ===== Serializable instance variables ================
	// Maps objects to their swizzlers
	// private Map objectsToSwizzlers = new HashMap();

	// private Map swizzlersToPOJOCollection = new HashMap();

	// An ordered list of objects
	// private List objectList = new LinkedList();
	private LinkedList<POJOSwizzler> swizzlerList = new LinkedList<POJOSwizzler>();
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

	/* (non-Javadoc)
	 * @see org.appdapter.gui.objbrowser.model.IPOJOCollection#createAndAddPOJO(java.lang.Class)
	 */
	@Override
	public synchronized Object createAndAddPOJO(Class cl)
			throws InstantiationException, IllegalAccessException {
		// Create the object
		Object obj = cl.newInstance();

		// Add it
		addPOJO(obj);

		return obj;
	}

	/* (non-Javadoc)
	 * @see org.appdapter.gui.objbrowser.model.IPOJOCollection#addPOJO(java.lang.Object)
	 */
	@Override
	public synchronized boolean addPOJO(Object obj) {
		if (containsPOJO(obj)) {
			return false;
		} else {
			// Create the object swizzler, with a unique name
			POJOSwizzler swizzler = new POJOSwizzler(generateUniqueName(obj),
					obj);

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

	/* (non-Javadoc)
	 * @see org.appdapter.gui.objbrowser.model.IPOJOCollection#removePOJO(java.lang.Object)
	 */
	@Override
	public synchronized boolean removePOJO(Object obj) {
		if (containsPOJO(obj)) {
			// Find the swizzler
			POJOSwizzler swizzler = getSwizzler(obj);

			// Remove it
			// objectsToSwizzlers.remove(obj);
			// objectList.remove(obj);
			swizzlerList.remove(swizzler);

			// Update the name index
			// nameIndex.remove(swizzler.getName());

			// Deselect it if necessary
			if (selected == obj) {
				try {
					// The object will fire a PropertyChangeEvent which I will
					// catch, so I don't need to do setSelectedPOJO(null)
					swizzler.setSelected(false);
				} catch (PropertyVetoException err) {
					theLogger.warn(
							"In POJOCollection.removePOJO(...) I was unable to deselect the removed object. I'll ignore the problem, i.e. leave it selected and remove it anyway.",
							err);
				}
			}

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

	/* (non-Javadoc)
	 * @see org.appdapter.gui.objbrowser.model.IPOJOCollection#getSwizzlers()
	 */
	@Override
	public Iterable<POJOSwizzler> getSwizzlers() {
		LinkedList list = new LinkedList();
		Iterator it = swizzlerList.iterator();
		while (it.hasNext()) {
			list.add(it.next());
		}
		return list;
	}

	/* (non-Javadoc)
	 * @see org.appdapter.gui.objbrowser.model.IPOJOCollection#getPOJOCount()
	 */
	@Override
	public int getPOJOCount() {
		return swizzlerList.size();
	}

	/* (non-Javadoc)
	 * @see org.appdapter.gui.objbrowser.model.IPOJOCollection#getPOJOAt(int)
	 */
	@Override
	public Object getPOJOAt(int index) {
		return swizzlerList.get(index);
	}

	/* (non-Javadoc)
	 * @see org.appdapter.gui.objbrowser.model.IPOJOCollection#containsPOJO(java.lang.Object)
	 */
	@Override
	public boolean containsPOJO(Object object) {
		// return objectList.contains(object);
		return containsSwizzler(getSwizzler(object));// containsKey(object);
	}

	/* (non-Javadoc)
	 * @see org.appdapter.gui.objbrowser.model.IPOJOCollection#containsSwizzler(org.appdapter.gui.objbrowser.model.POJOSwizzler)
	 */
	@Override
	public boolean containsSwizzler(POJOSwizzler swizzler) {
		return swizzlerList.contains(swizzler);
		// return objectsToSwizzlers.containsValue(object);
	}

	/* (non-Javadoc)
	 * @see org.appdapter.gui.objbrowser.model.IPOJOCollection#getPOJOCollectionOfType(java.lang.Class)
	 */
	@Override
	public Set getPOJOCollectionOfType(Class type) {
		Set result = new HashSet();
		Iterator it = swizzlerList.iterator();
		while (it.hasNext()) {
			POJOSwizzler obj = (POJOSwizzler) it.next();
			if (obj.isInstance(type)) {
				result.add(obj);
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.appdapter.gui.objbrowser.model.IPOJOCollection#findPOJO(java.lang.String)
	 */
	@Override
	public Object findPOJO(String name) {
		POJOSwizzler swizzler = findSwizzler(name);
		if (swizzler == null) {
			return null;
		} else {
			return swizzler.getObject();
		}
	}

	/* (non-Javadoc)
	 * @see org.appdapter.gui.objbrowser.model.IPOJOCollection#findSwizzler(java.lang.String)
	 */
	@Override
	public POJOSwizzler findSwizzler(String name) {
		for (POJOSwizzler swizzler : getSwizzlers()) {
			if (swizzler.isNamed(name))
				return swizzler;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.appdapter.gui.objbrowser.model.IPOJOCollection#getSwizzler(java.lang.Object)
	 */
	@Override
	public POJOSwizzler getSwizzler(Object object) {
		if (object == null)
			return null;
		for (POJOSwizzler swizzler : swizzlerList) {
			if (swizzler.representsObject(object))
				return swizzler;
		}
		return null;
	}

	// ===== Manipulating the selected object ===============

	/* (non-Javadoc)
	 * @see org.appdapter.gui.objbrowser.model.IPOJOCollection#getSelectedPOJO()
	 */
	@Override
	public Object getSelectedPOJO() {
		return selected;
	}

	// ==== Event listener registration ======================

	/* (non-Javadoc)
	 * @see org.appdapter.gui.objbrowser.model.IPOJOCollection#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	@Override
	public void addPropertyChangeListener(PropertyChangeListener p) {
		propSupport.addPropertyChangeListener(p);
	}

	/* (non-Javadoc)
	 * @see org.appdapter.gui.objbrowser.model.IPOJOCollection#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	@Override
	public void removePropertyChangeListener(PropertyChangeListener p) {
		propSupport.removePropertyChangeListener(p);
	}

	/* (non-Javadoc)
	 * @see org.appdapter.gui.objbrowser.model.IPOJOCollection#addListener(org.appdapter.gui.objbrowser.model.POJOCollectionListener)
	 */
	@Override
	public void addListener(POJOCollectionListener l) {
		collectionListeners.add(l);
	}

	/* (non-Javadoc)
	 * @see org.appdapter.gui.objbrowser.model.IPOJOCollection#removeListener(org.appdapter.gui.objbrowser.model.POJOCollectionListener)
	 */
	@Override
	public void removeListener(POJOCollectionListener l) {
		collectionListeners.remove(l);
	}

	// ===== Property notifications (i.e. others notifying me) =========

	/* (non-Javadoc)
	 * @see org.appdapter.gui.objbrowser.model.IPOJOCollection#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public synchronized void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("name")) {
			// Name has changed - so update the name index
			POJOSwizzler object = (POJOSwizzler) (evt.getSource());
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
			POJOSwizzler swizzler = (POJOSwizzler) evt.getSource();
			Object object = swizzler.getObject();
			if (containsPOJO(object)) {
				if (newValue.equals(new Boolean(true))) {
					try {
						setSelectedPOJO(object);
					} catch (PropertyVetoException err) {
						theLogger.warn(
								"The POJOCollection was notified that a object has been selected, and when trying to update the internal state a PropertyVetoException occurred",
								err);
					}
				} else if (newValue.equals(new Boolean(false))) {
					try {
						setSelectedPOJO(null);
					} catch (PropertyVetoException err) {
						theLogger.warn(
								"The POJOCollection was notified that a object has been deselected, and when trying to update the internal state a PropertyVetoException occurred",
								err);
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.appdapter.gui.objbrowser.model.IPOJOCollection#setSelectedPOJO(java.lang.Object)
	 */
	@Override
	public synchronized void setSelectedPOJO(Object object)
			throws PropertyVetoException {
		if (selected != object && containsPOJO(object)) {

			// Deselect the old swizzler (if any)
			Object oldSelected = selected;
			POJOSwizzler oldSwizzler = getSwizzler(oldSelected);
			if (oldSwizzler != null) {
				oldSwizzler.setSelected(false);
			}

			// Update my "selected" instance variable
			selected = object;

			// Make sure the corresponding swizzler knows its selected
			getSwizzler(selected).setSelected(true);

			// Fire a property change
			propSupport.firePropertyChange("selected", oldSelected, selected);

		}
	}

	/* (non-Javadoc)
	 * @see org.appdapter.gui.objbrowser.model.IPOJOCollection#vetoableChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void vetoableChange(PropertyChangeEvent evt)
			throws PropertyVetoException {
		if (evt.getPropertyName().equals("name")) {
			// The name of a swizzler has changed. Make sure there are no name
			// collisions
			POJOSwizzler swizzler = (POJOSwizzler) (evt.getSource());
			Object object = swizzler.getObject();
			if (containsPOJO(object)) {
				String name = (String) evt.getNewValue();
				POJOSwizzler otherSwizzler = findSwizzler(name);
				if (otherSwizzler != null && otherSwizzler != swizzler) {
					throw new PropertyVetoException(
							"Another object already has the name '" + name
									+ "'", evt);
				}
			}
		}
	}

	// ====== Save and load operations ================================

	/* (non-Javadoc)
	 * @see org.appdapter.gui.objbrowser.model.IPOJOCollection#save(java.io.File)
	 */
	@Override
	public void save(File destination) throws IOException {
		theLogger.debug("Saving collection to " + destination);
		FileOutputStream fileOut = new FileOutputStream(destination);
		ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
		objectOut.writeObject(this);
		fileOut.close();
		theLogger.debug("Successfully saved!");
	}

	static public POJOCollection load(File source) throws IOException,
			ClassNotFoundException {
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

		for (POJOSwizzler b : getSwizzlers()) {
			b.addVetoableChangeListener(this);
			b.addPropertyChangeListener(this);
		}
	}

	// ======== Utility methods ==================================

	/**
	 * Generates a default name for the given object, while will be something
	 * like "Button1", "Button2", etc.
	 */
	private String generateUniqueName(Object object) {
		String className = Utility.getShortClassName(object.getClass());

		int counter = 1;
		boolean done = false;
		String name = "???";
		while (!done) {
			name = className + counter;
			Object otherPOJO = findPOJO(name);
			if (otherPOJO == null) {
				done = true;
			} else {
				++counter;
			}
		}
		return name;
	}

	/* (non-Javadoc)
	 * @see org.appdapter.gui.objbrowser.model.IPOJOCollection#getSelectedBean()
	 */
	@Override
	public Object getSelectedBean() {
		// TODO Auto-generated method stub
		return selected;
	}
}