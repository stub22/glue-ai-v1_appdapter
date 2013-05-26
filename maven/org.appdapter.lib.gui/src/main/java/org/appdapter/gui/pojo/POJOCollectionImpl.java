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

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxImpl;
import org.appdapter.api.trigger.ScreenBox;
import org.appdapter.gui.box.ScreenBoxImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A POJOCollection is a container of objects and corresponding POJOBoxs,
 * which add the concept of "name" and "selected".
 * <p>
 * 
 * Each object inside the POJOCollection has a corresponding Box. A
 * Box has a reference to the object in memory it respresents. Given an
 * object the only way to find the corresponding Box is to use
 * getBox(Object pojo)
 * <p>
 * 
 * PropertyChangeListeners can register to find out when the selected object is
 * changed, in which case the property "selectedPOJO" will change.
 * <p>
 * 
 * POJOCollectionContextListeners can register to find out when objects are
 * added or removed.
 * 
 * @see Box
 * 
 */
@SuppressWarnings("serial")
public class POJOCollectionImpl implements VetoableChangeListener, PropertyChangeListener, Serializable, NamedObjectCollection {
	// ==== Static variables ===================
	private static Logger theLogger = LoggerFactory.getLogger(POJOCollectionImpl.class);

	// ==== Transient instance variables ===================
	transient private PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
	transient private Set collectionListeners = new HashSet();

	// ===== Serializable instance variables ================
	// Maps objects to their boxs
	// private Map objectsToBoxs = new HashMap();

	// private Map boxsToPOJOCollection = new HashMap();

	// An ordered list of objects
	// private List objectList = new LinkedList();
	private LinkedList<POJOBox> boxList = new LinkedList<POJOBox>();
	// private LinkedList objectList = new LinkedList();

	// The currently selected object
	private Object selected = null;

	// Maps object box name to object box
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
			// Create the object box, with a unique name
			POJOBox box = new ScreenBoxImpl(generateUniqueName(obj), obj);

			// Add it
			// objectsToBoxs.put(obj, box);
			// objectList.add(obj);
			boxList.add(box);

			// Add myself as listener
			box.addVetoableChangeListener(this);
			box.addPropertyChangeListener(this);

			// Update the name index
			// nameIndex.put(box.getName(), box);

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
			// Find the box
			Box box = getBox(obj);

			// Remove it
			// objectsToBoxs.remove(obj);
			// objectList.remove(obj);
			boxList.remove(box);

			// Update the name index
			// nameIndex.remove(box.getName());

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
	@Override public Iterable<Box> getBoxes() {
		LinkedList list = new LinkedList();
		synchronized (boxList) {
			Iterator it = boxList.iterator();
			while (it.hasNext()) {
				list.add(it.next());
			}
		}
		return list;
	}

	public Iterable<POJOBox> getScreenBoxes() {
		LinkedList list = new LinkedList();
		synchronized (boxList) {
			Iterator it = boxList.iterator();
			while (it.hasNext()) {
				list.add(it.next());
			}
		}
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public int getPOJOCount() {
		synchronized (boxList) {
			return boxList.size();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public boolean containsPOJO(Object object) {
		return containsBox(getBox(object));// containsKey(object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public boolean containsBox(Box box) {
		synchronized (boxList) {
			return boxList.contains(box) || containsPOJO(((POJOBox) box).getObject());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public Object findPOJO(String name) {
		POJOBox box = findBox(name);
		if (box == null) {
			return null;
		} else {
			return box.getObject();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public POJOBox findBox(String name) {
		for (POJOBox box : getScreenBoxes()) {
			if (box.isNamed(name))
				return box;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public POJOBox getBox(Object object) {
		if (object == null)
			return null;
		if (object instanceof POJOBox)
			return (POJOBox) object;

		synchronized (boxList) {
			for (POJOBox box : boxList) {
				if (box.representsObject(object))
					return box;
			}
			POJOBox box;
			try {
				box = makeWrapper(object);
			} catch (PropertyVetoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw Utility.reThrowable(e);
			}
			boxList.add(box);

			// Add myself as listener
			box.addVetoableChangeListener(this);
			box.addPropertyChangeListener(this);

			// Update the name index
			// nameIndex.put(box.getName(), box);

			// notify collectionListeners
			Iterator it = collectionListeners.iterator();
			while (it.hasNext()) {
				// @temp
				((POJOCollectionListener) it.next()).pojoAdded(object);
			}
			return box;
		}
	}

	private POJOBox makeWrapper(Object object) throws PropertyVetoException {
		Class gc = object.getClass();
		POJOBox box = new ScreenBoxImpl(generateUniqueName(object), object);
		return box;
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
			POJOBox box = (POJOBox) evt.getSource();
			Object object = box.getObject();
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

			// Deselect the old box (if any)
			Object oldSelected = selected;
			Box oldBox = getBox(oldSelected);

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
			// The name of a box has changed. Make sure there are no name
			// collisions
			POJOBox box = getEventBox(evt);
			Object object = box.getObject();
			if (containsPOJO(object)) {
				String name = (String) evt.getNewValue();
				Box otherBox = findBox(name);
				if (otherBox != null && otherBox != box) {
					throw new PropertyVetoException("Another object already has the name '" + name + "'", evt);
				}
			}
		}
	}

	private POJOBox getEventBox(PropertyChangeEvent evt) {
		POJOBox box = (POJOBox) evt.getSource();
		return box;
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

	static public NamedObjectCollection load(File source) throws IOException, ClassNotFoundException {
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

		for (POJOBox b : getScreenBoxes()) {
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

	@Override public NamedObjectCollection getPOJOSession() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override public <T> Collection<T> getPOJOCollectionOfType(Class<T> type) {
		Set result = new HashSet();
		for (POJOBox obj : getScreenBoxes()) {
			if (obj.isInstance(type)) {
				result.add(obj.convertTo(type));
			}
		}
		return result;
	}

	@Override public BoxImpl findBoxByName(String name) {
		return (BoxImpl) findBox(name);
	}

	@Override public POJOBox findOrCreateBox(Object object) {
		return (POJOBox) getBox(object);
	}

	@Override public POJOApp getPOJOApp() {
		return Utility.getCurrentContext();
	}
}