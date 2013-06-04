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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.gui.box.POJOBoxImpl;
import org.appdapter.gui.box.ScreenBoxImpl;
import org.appdapter.gui.box.ScreenBoxTreeNode;
import org.appdapter.gui.browse.AbstractScreenBoxTreeNodeImpl;
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
abstract public class POJOCollectionImpl extends AbstractScreenBoxTreeNodeImpl implements VetoableChangeListener, PropertyChangeListener, Serializable, NamedObjectCollection {
	// ==== Static variables ===================
	private static Logger theLogger = LoggerFactory.getLogger(POJOCollectionImpl.class);

	static public NamedObjectCollection load(File source) throws IOException, ClassNotFoundException {
		FileInputStream fileIn = new FileInputStream(source);
		ObjectInputStream objectIn = new ObjectInputStream(fileIn);
		POJOCollectionImpl b = (POJOCollectionImpl) objectIn.readObject();
		fileIn.close();

		b.initAfterLoading();
		return b;
	}

	// An ordered list of objects
	// private List objectList = new LinkedList();
	private LinkedList<POJOBox> boxList = new LinkedList<POJOBox>();
	// private LinkedList objectList = new LinkedList();

	// ===== Serializable instance variables ================
	// Maps objects to their boxs
	// private Map objectsToBoxs = new HashMap();

	// private Map boxsToPOJOCollection = new HashMap();

	transient private Set collectionListeners = new HashSet();

	// Maps object box name to object box
	// private Map nameIndex = new Hashtable();

	// ============ Constructors
	// ==================================================

	// ==== Transient instance variables ===================
	transient private PropertyChangeSupport propSupport = new PropertyChangeSupport(this);

	// ==== Manipulating the collection of objects ==================

	// The currently selected object
	private Object selected = null;

	public POJOCollectionImpl() {
	}

	public POJOCollectionImpl(MutableBox rootBox) {
		// TODO Auto-generated constructor stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void addListener(POJOCollectionListener l) {
		collectionListeners.add(l);
	}

	// ==== Queries ========================

	/**
	 * {@inheritDoc}
	 */
	@Override public POJOBox addObject(Object object) {
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

	public boolean addObjectMaybe(Object obj) throws PropertyVetoException {
		if (containsObject(obj)) {
			return false;
		} else {
			// Create the object box, with a unique name
			String named = generateUniqueName(obj);
			POJOBoxImpl box = (POJOBoxImpl) addObject(generateUniqueName(obj), obj);

			if (box.registeredWithName != null) {
				return false;
			}
			box.registeredWithName = named;
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
	@Override public void addPropertyChangeListener(PropertyChangeListener p) {
		propSupport.addPropertyChangeListener(p);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean containsObject(Box box) {
		synchronized (boxList) {
			return boxList.contains(box) || containsObject(((POJOBox) box).getValue());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public boolean containsObject(Object object) {
		return containsObject(addObject(object));// containsKey(object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public synchronized Object createAndAddBox(Class cl) throws InstantiationException, IllegalAccessException {
		// Create the object
		Object obj = cl.newInstance();

		// Add it
		addObject(obj);

		return obj;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public ScreenBoxImpl findBoxByName(String name) {
		for (ScreenBoxImpl box : getScreenBoxes()) {
			if (box.isNamed(name))
				return box;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public POJOBox findBoxByObject(Object obj) {
		for (POJOBox box : getScreenBoxes()) {
			if (box.representsObject(obj))
				return box;
		}
		return null;
	}

	@Override public <T> Collection findBoxByType(Class<T> type) {
		Set result = new HashSet();
		for (POJOBox obj : getScreenBoxes()) {
			if (obj.isTypeOf(type)) {
				result.add(obj.convertTo(type));
			}
		}
		return result;
	}

	/**
	 * Generates a default name for the given object, while will be something
	 * like "Button1", "Button2", etc.
	 */
	public String generateUniqueName(Object object) {
		return Utility.generateUniqueName(object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public Iterable<Box> getBoxes() {
		//LinkedList boxList = getBoxListFrom(DisplayType.TOSTRING);
		LinkedList list = new LinkedList();
		synchronized (boxList) {
			Iterator it = boxList.iterator();
			while (it.hasNext()) {
				list.add(it.next());
			}
		}
		return list;
	}

	// ===== Manipulating the selected object ===============

	/**
	 * {@inheritDoc}
	 */
	@Override public int getBoxesCount() {
		synchronized (boxList) {
			return boxList.size();
		}
	}

	// ==== Event listener registration ======================

	private POJOBox getEventBox(PropertyChangeEvent evt) {
		POJOBox box = (POJOBox) evt.getSource();
		return box;
	}

	public NamedObjectCollection getNamedObjectCollection() {
		return this;
	}

	// ===== Property notifications (i.e. others notifying me) =========

	public Iterable<ScreenBoxImpl> getScreenBoxes(DisplayType attachType) {
		//	LinkedList boxList = getBoxListFrom(attachType);
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
	@Override public Object getSelectedComponent() {
		// TODO Auto-generated method stub
		return selected;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public Object getSelectedPOJO() {
		return selected;
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

	// ====== Save and load operations ================================

	private POJOBox makeWrapper(Object object) throws PropertyVetoException {
		Class gc = object.getClass();
		POJOBox box = new ScreenBoxImpl(generateUniqueName(object), object);
		return box;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public synchronized void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("name")) {
			// Name has changed - so update the name index
			POJOBox object = (POJOBox) (evt.getSource());
			if (containsObject(object.getValue())) {
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
			Object object = box.getValue();
			if (containsObject(object)) {
				if (newValue.equals(new Boolean(true))) {
					try {
						setSelectedComponent(object);
					} catch (PropertyVetoException err) {
						theLogger.warn("The POJOCollection was notified that a object has been selected, and when trying to update the internal state a PropertyVetoException occurred", err);
					}
				} else if (newValue.equals(new Boolean(false))) {
					try {
						setSelectedComponent(null);
					} catch (Exception err) {
						theLogger.warn("The POJOCollection was notified that a object has been deselected, and when trying to update the internal state a PropertyVetoException occurred", err);
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void removeListener(POJOCollectionListener l) {
		collectionListeners.remove(l);
	}

	// ======== Utility methods ==================================

	/**
	 * {@inheritDoc}
	 */
	@Override public synchronized boolean removeObject(Object obj) {
		if (containsObject(obj)) {
			// Find the box
			Box box = addObject(obj);

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

	/**
	 * {@inheritDoc}
	 */
	@Override public void removePropertyChangeListener(PropertyChangeListener p) {
		propSupport.removePropertyChangeListener(p);
	}

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

	/**
	 * {@inheritDoc}
	 */
	public synchronized void setSelectedComponent(Object object) throws PropertyVetoException {
		object = Utility.asPOJO(object);
		if (selected != object && containsObject(object)) {

			// Deselect the old box (if any)
			Object oldSelected = selected;
			Box oldBox = addObject(oldSelected);

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
			Object object = box.getValue();
			if (containsObject(object)) {
				String name = (String) evt.getNewValue();
				Box otherBox = findBoxByName(name);
				if (otherBox != null && otherBox != box) {
					throw new PropertyVetoException("Another object already has the name '" + name + "'", evt);
				}
			}
		}
	}

	public ScreenBoxTreeNode findDescendantNodeForBox(Box b) {
		// TODO Auto-generated method stub
		return super.findDescendantNodeForBox(b);
	}

	@Override public Collection getTriggersFromUI(Object object) {
		return null;
	}

}