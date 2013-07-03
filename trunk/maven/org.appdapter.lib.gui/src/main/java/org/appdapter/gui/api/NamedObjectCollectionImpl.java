package org.appdapter.gui.api;

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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import org.appdapter.api.trigger.*;
import org.appdapter.api.trigger.*;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.box.*;
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
public class NamedObjectCollectionImpl implements NamedObjectCollection, VetoableChangeListener, PropertyChangeListener, Serializable {
	// ==== Static variables ===================
	private static Logger theLogger = LoggerFactory.getLogger(NamedObjectCollectionImpl.class);

	static public NamedObjectCollection load(File source) throws IOException, ClassNotFoundException {
		FileInputStream fileIn = new FileInputStream(source);
		ObjectInputStream objectIn = new ObjectInputStream(fileIn);
		NamedObjectCollection b = (NamedObjectCollection) objectIn.readObject();
		fileIn.close();

		b.initAfterLoading();
		return b;
	}

	// private Map boxsToPOJOCollection = new HashMap();

	// An ordered list of objects
	//private List objectList = new LinkedList();
	protected LinkedList<BT> boxList = new LinkedList<BT>();
	//private LinkedList objectList = new LinkedList();

	// ============ Constructors
	// ==================================================

	transient private Set colListeners = new HashSet();

	private DisplayContext displayContext;

	//Maps value wrapper name to value wrapper
	private Map<String, BT> nameIndex = new Hashtable<String, BT>();

	private LinkedList<Object> objectList = new LinkedList<Object>();

	// ===== Serializable instance variables ================
	// Maps objects to their boxs
	private Map<Object, BT> objectsToWrappers = new HashMap();

	// ==== Queries ========================

	// ==== Transient instance variables ===================
	transient private PropertyChangeSupport propSupport = new PropertyChangeSupport(this);

	// The currently selected object
	private Object selected = null;

	String toStringText;

	public NamedObjectCollectionImpl() {
	}

	public NamedObjectCollectionImpl(String named, DisplayContext displayedAt) {
		toStringText = named;
		this.displayContext = displayedAt;
	}

	public boolean addBoxed(String title, BT box) {
		synchronized (boxList) {
			BT prev = findBoxByName(title);
			if (prev == box)
				return true;
			if (prev != null) {
				Debuggable.notImplemented("Already existing name: " + title);
			}

			// Add it
			// objectsToBoxs.put(obj, box);
			boxList.add(box);
			objectList.add(box.getValueOrThis());

			if (objectsToWrappers != null) {
				synchronized (objectsToWrappers) {
					objectsToWrappers.put(box.getValue(), box);
				}
			}
			if (title == null) {
				title = getTitleOf(box);
			}
			if (nameIndex != null) {
				synchronized (nameIndex) {
					nameIndex.put(title, box);
				}
			}

			// Add myself as listener
			box.addVetoableChangeListener(this);
			box.addPropertyChangeListener(this);

			// Update the name index
			// nameIndex.put(box.getName(), box);

			// notify collectionListeners
			Iterator it = colListeners.iterator();
			while (it.hasNext()) {
				// @temp
				((POJOCollectionListener) it.next()).pojoAdded(box.getValueOrThis());
			}
			return true;
		}

	}

	/**
	 * Listeners will find out when objects are added or removed
	 */
	public void addListener(POJOCollectionListener l) {
		colListeners.add(l);
	}

	public BT addObjectMaybe(Object obj) throws PropertyVetoException {
		synchronized (boxList) {
			return addObjectMaybe(null, obj);
		}
	}

	public BT addObjectMaybe(String label, Object obj) throws PropertyVetoException {

		BT box = findBoxByObject(obj);
		if (box == null) {
			box = findBoxByName(label);
		}

		// Create the object box, with a unique name
		String title = label;
		if (title == null) {
			title = generateUniqueName(obj);
		}
		if (box == null)
			box = new ObjectWrapper(this, title, obj);
		return box;

	}

	static class ObjectWrapper extends POJOBoxImpl implements BT, IGetBox {
		@Override public BT getBT() {
			return this;
		}

		public Object value;
		private NamedObjectCollection noc;

		public void setNameValue(String uniqueName, Object obj) {
			valueSetAs = obj;
			if (uniqueName == null) {
				uniqueName = Utility.generateUniqueName(obj, uniqueName, noc.getNameToBoxIndex());
			}
			name = uniqueName;
			if (obj == null) {

				obj = new NullPointerException(uniqueName).fillInStackTrace();
			}
			if (clz == null)
				clz = obj.getClass();

			setShortLabel(uniqueName);
			setObject(obj);
		}

		/*
		public POJOBoxImpl(NamedObjectCollection noc, String label) {
		this(noc, label, null);
		}*/

		/**
		 * Creates a new ScreenBox for the given object
		 * and assigns it a default name.
		 */
		public ObjectWrapper(NamedObjectCollection noc, String title, Object val) {
			this.noc = noc;
			setNameValue(title, val);
		}

		@Override public Object reallyGetValue() {
			return value;
		}

		@Override public void reallySetValue(Object newObject) {
			value = newObject;
		}

		@Override public Object getValue() {
			if (value != null)
				return value;
			return this;
		}

		@Override public Object getValueOrThis() {
			if (value != null)
				return value;
			return this;
		}
	}

	/**
	 * Listeners will be notifed when the currently value selection
	 * is changed.
	 */
	public void addPropertyChangeListener(PropertyChangeListener p) {
		propSupport.addPropertyChangeListener(p);
	}

	/**
	 * Checks if this namedObjects contains the given value
	 */
	@Override public boolean containsObject(Object object) {

		object = Utility.dref(object);

		if (objectsToWrappers != null) {
			synchronized (objectsToWrappers) {
				return objectsToWrappers.containsKey(object);
			}
		}
		if (objectList != null)
			return objectList.contains(object);

		return findBoxByObject(object) != null;
	}

	@Override public String getName() {
		return toStringText;
	}

	// ==== Event listener registration ======================

	/**
	 * Checks if this namedObjects contains the given value wrapper
	 */
	private boolean containsWrapper(BT wrapper) {
		return boxList.contains(wrapper);
		//    return objectsToWrappers.containsValue(value);
	}

	/**
	 * Creates a new object of the given class and adds to this namedObjects.
	 * The given class must have an empty constructor.
	 *
	 * @throws InstantiationException if the given Class represents an abstract class, an interface, an array class, a primitive type, or void; or if the instantiation fails for some other reason
	 * @throws IllegalAccessException if the given class or initializer is not accessible.
	 *
	 * @returns the newly created ScreenBox
	 */
	public synchronized Object createAndAddObject(Class cl) throws InstantiationException, IllegalAccessException {
		//Create the object
		Object obj = cl.newInstance();

		//Add it
		findOrCreateBox(obj);
		return obj;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public BT findBoxByName(String name) {
		if (name == null)
			return null;
		if (nameIndex != null) {
			synchronized (nameIndex) {
				BT boxl = nameIndex.get(name);
				if (boxl instanceof BT)
					return (BT) boxl;
			}
			return null;
		}
		for (BT box : getScreenBoxes()) {
			if (box.isNamed(name)) {
				return box;
			}
		}
		return null;
	}

	/**
	 * Returns the wrapper corresponding to the given object, i.e
	 * the ScreenBox who's object corresponds to the given one.
	 * Returns null if the POJOCollection does not contain the given object.
	 */
	public BT findBoxByObject(Object object) {
		if (object == null)
			return null;

		object = Utility.dref(object, object);

		int i = objectList.indexOf(object);
		if (i != -1) {
			BT wrapper = (BT) boxList.get(i);
			if (wrapper != null)
				return wrapper;
		}

		for (BT box : getScreenBoxes()) {
			if (box.representsObject(object))
				return box;
		}
		return null;
	}

	// ======== Utility methods ==================================

	/**
	 * Returns the value with the given name, or null if none.
	 */
	public Object findObjectByName(String name) {
		BT wrapper = findBoxByName(name);
		if (wrapper == null) {
			return null;
		} else {
			return wrapper.getValue();
		}
	}

	/**
	 * Returns all objects representing objects that are an instance of the given class
	 * or interface, either directly or indirectly.
	 */
	public Collection findObjectsByType(Class type) {
		Set result = new HashSet();
		Iterator it = getObjects();
		while (it.hasNext()) {
			Object obj = it.next();
			if (type.isInstance(obj)) {
				result.add(obj);
			}
		}
		for (BT obj : getScreenBoxes()) {
			if (obj.isTypeOf(type)) {
				result.add(obj.convertTo(type));
			}
		}
		return result;
	}

	@Override public BT findOrCreateBox(Object value) {
		try {
			return findOrCreateBox(null, value);
		} catch (PropertyVetoException e) {
			throw Debuggable.reThrowable(e);
		}
	}

	/**
	 * Adds the given object to the POJOCollection, if it does not already exist.
	 *
	 * @returns true if the object was added, i.e. if it didn't already exist.
	 */
	public synchronized BT findOrCreateBox(String title, Object obj) throws PropertyVetoException {
		BT wrapper = findBoxByObject(obj);
		if (wrapper != null) {
			{
				if (title != null) {
					wrapper.setUniqueName(title);
				}
				if (!boxList.contains(wrapper)) {
					addBoxed(title, wrapper);
				}
				return wrapper;
			}
		} else {
			if (title == null) {
				title = generateUniqueName(obj);
			}
			//Create the value wrapper, with a unique name
			wrapper = (BT) new ObjectWrapper(this, title, obj);

			//Add it
			//objectsToWrappers.put(obj, wrapper);
			objectList.add((Object) obj);
			boxList.add(wrapper);
			objectsToWrappers.put(obj, wrapper);

			//Add myself as listener
			wrapper.addVetoableChangeListener(this);
			wrapper.addPropertyChangeListener(this);

			//Update the name index
			nameIndex.put(title, wrapper);

			//notify namedObjectsListeners
			Iterator it = colListeners.iterator();
			while (it.hasNext()) {
				//@temp
				((POJOCollectionListener) it.next()).pojoAdded(obj);
			}

			return wrapper;
		}
	}

	/**
	 * Generates a default name for the given object, while will be something
	 * like "Button1", "Button2", etc.
	 */
	public String generateUniqueName(Object object) {
		return Utility.generateUniqueName(object, getNameToBoxIndex());
	}

	/**
	 * Returns an iterator over all the value wrappers.
	 * NOTE - this could be a bit slow! Avoid whenever possible.
	 * The code can be optimized for this, but it isn't right now.
	 */
	@Override public Iterator getBoxes() {
		//LinkedList boxList = getBoxListFrom(DisplayType.TOSTRING);
		LinkedList list = new LinkedList();
		synchronized (boxList) {
			Iterator it = boxList.iterator();
			while (it.hasNext()) {
				list.add(it.next());
			}
		}
		if (true)
			return list.iterator();
		else {
			Iterator it = getObjects();
			while (it.hasNext()) {
				list.add(findOrCreateBox(it.next()));
			}
			return list.iterator();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int getBoxesCount() {
		synchronized (boxList) {
			return boxList.size();
		}
	}

	public DisplayContext getDisplayContext() {
		if (displayContext != null)
			return displayContext;
		return Utility.getDisplayContext();
	}

	//==== Manipulating the collection of objects ==================

	private BT getEventBox(PropertyChangeEvent evt) {
		BT box = (BT) evt.getSource();
		return box;
	}

	@Override public Map<String, BT> getNameToBoxIndex() {
		return nameIndex;
	}

	//==== Queries ========================

	/**
	 * Returns the value at the given index
	 */
	private Object getObjectAt(int index) {
		return objectList.get(index);
	}

	/**
	 * Returns the current number of objects in the namedObjects
	 */
	private int getObjectCount() {
		return objectList.size();
	}

	/**
	 * Returns an iterator over all the objects
	 */
	public Iterator getObjects() {
		return objectList.iterator();
	}

	public Iterable<BT> getScreenBoxes() {
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

	public Iterable<BT> getScreenBoxes(DisplayType attachType) {
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
	 * Returns the currently selected value,
	 * or null if none.
	 */
	public Object getSelectedObject() {
		return selected;
	}

	private String getTitleOf(Box box) {
		if (box == null)
			return MISSING_COMPONENT;
		if (box instanceof BT)
			return ((BT) box).getUniqueName(getNameToBoxIndex());
		String lbl = ((BoxImpl) box).getShortLabel();
		if (lbl != null)
			return lbl;
		return Utility.generateUniqueName(box.getValue(), this.nameIndex);
	}

	public String getTitleOf(Object box) {
		if (box == null)
			return "<null>";
		if (box instanceof Box) {
			return getTitleOf((Box) box);
		}
		if (box instanceof String) {
			return getTitleOf(findBoxByName("" + box));
		}
		BT pojo = findBoxByObject(box);
		if (pojo == null)
			return MISSING_COMPONENT;
		return getTitleOf(pojo);

	}

	//===== Manipulating the selected value ===============

	//==== Event listener registration ======================

	/**
	 * Tells the namedObjects that it has just been loaded from a file
	 * and needs to initialize itself. For example update
	 * transient instance variables that were "lost" during the
	 * serialization, and add itself as listener to all the objects.
	 */
	public void initAfterLoading() {
		if (colListeners == null) {
			colListeners = new HashSet();
		}

		if (propSupport == null) {
			propSupport = new PropertyChangeSupport(this);
		}

		Iterator it = getBoxes();
		while (it.hasNext()) {
			BT b = (BT) it.next();
			b.addVetoableChangeListener(this);
			b.addPropertyChangeListener(this);
		}
	}

	private BT makeWrapper(Object object) throws PropertyVetoException {
		BT box = new ObjectWrapper(this, generateUniqueName(object), object);
		return box;
	}

	//===== Property notifications (i.e. others notifying me) =========

	/**
	 * This is used for Boxes to tell their NamedObjectCollection that a property
	 * such as "name" or "selected" has changed. The NamedObjectCollection will update
	 * its state as necessary.
	 */
	public synchronized void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("name")) {
			//Name has changed - so update the name index
			BT value = (BT) (evt.getSource());
			if (containsObject(value.getValue())) {
				String newName = (String) evt.getNewValue();
				String oldName = (String) evt.getOldValue();
				if (oldName != null) {
					nameIndex.remove(oldName);
				}
				nameIndex.put(newName, value);
			}

		} else if (evt.getPropertyName().equals("selected")) {
			//Selection has changed. Call notifySelected or notifyDeselected
			//to update my internal state
			Boolean newValue = (Boolean) evt.getNewValue();
			BT wrapper = (BT) evt.getSource();
			Object object = wrapper.getValue();
			if (containsObject(object)) {
				if (newValue.equals(new Boolean(true))) {
					try {
						setSelectedObject(object);
					} catch (PropertyVetoException err) {
						theLogger.warn("The NamedObjectCollection was notified that a value has been selected, and when trying to update the internal state a PropertyVetoException occurred", err);
					}
				} else if (newValue.equals(new Boolean(false))) {
					try {
						setSelectedObject(null);
					} catch (PropertyVetoException err) {
						theLogger.warn("The NamedObjectCollection was notified that a value has been deselected, and when trying to update the internal state a PropertyVetoException occurred", err);
					}
				}
			}
		}
	}

	//====== Save and load operations ================================

	@Override public void reload() {
		Debuggable.notImplemented();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void removeListener(POJOCollectionListener l) {
		colListeners.remove(l);
	}

	//======== Utility methods ==================================

	/**
	 * Removes the given value, if it is inside this namedObjects.
	 * If not, nothing happens. <p>
	 *
	 * ObjectListeners will be notified. <p>
	 *
	 * If the value was selected, the current selection
	 * will change to null and property change listeners
	 * will be notified. <p>
	 *
	 */
	public synchronized boolean removeObject(Object obj) {
		//Find the wrapper
		BT wrapper = findBoxByObject(obj);
		if (wrapper == null)
			return false;
		Object realObj = wrapper.getValue();
		if (realObj != null && realObj != obj) {
			theLogger.warn("This box is for a differnt object " + wrapper + " not " + obj);
			obj = realObj;
		}

		String title = wrapper.getUniqueName();

		//Remove it
		//objectsToWrappers.remove(obj);
		objectList.remove(obj);
		boxList.remove(wrapper);
		objectsToWrappers.remove(obj);

		//Update the name index			
		nameIndex.remove(title);

		//Deselect it if necessary
		if (selected == obj) {
			try {
				//The value will fire a PropertyChangeEvent which I will
				//catch, so I don't need to do setSelectedObject(null)
				wrapper.setUISelected(false);
			} catch (PropertyVetoException err) {
				theLogger.warn("In NamedObjectCollection.removeObject(...) I was unable to deselect the removed value. I'll ignore the problem, i.e. leave it selected and remove it anyway.", err);
			}
		}

		//notify namedObjectsListeners
		Iterator it = colListeners.iterator();
		while (it.hasNext()) {
			((POJOCollectionListener) it.next()).pojoRemoved(obj);
		}
		return true;

	}

	/**
	 * Listeners will be notifed when the currently value selection
	 * is changed.
	 */
	public void removePropertyChangeListener(PropertyChangeListener p) {
		propSupport.removePropertyChangeListener(p);
	}

	@Override public void renameObject(String oldName, String newName) throws PropertyVetoException {
		BT value = findOrCreateBox(newName, findObjectByName(oldName));
		value.setUniqueName(newName);
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
			BT oldBox = findOrCreateBox(oldSelected);

			// Update my "selected" instance variable
			selected = object;

			// Fire a property change
			propSupport.firePropertyChange("selected", oldSelected, selected);

		}
	}

	/**
	 * Makes the given value the currently selected one.
	 * The previously selected value (if any) will be deselected,
	 * and a property change event will be fired.
	 *
	 * @throws PropertyVetoException if someone refused to let the selected value change
	 */
	public synchronized void setSelectedObject(Object object) throws PropertyVetoException {
		if (selected != object && containsObject(object)) {

			//Deselect the old wrapper (if any)
			Object oldSelected = selected;
			BT oldWrapper = findOrCreateBox(oldSelected);
			if (oldWrapper != null) {
				oldWrapper.setUISelected(false);
			}

			//Update my "selected" instance variable
			selected = object;

			//Make sure the corresponding wrapper knows its selected
			findOrCreateBox(selected).setUISelected(true);

			//Fire a property change
			propSupport.firePropertyChange("selected", oldSelected, selected);

		}
	}

	@Override public String toString() {
		return toStringText;
	}

	/**
	 * This is used for Boxes to tell their NamedObjectCollection that a property
	 * such as "name" or "selected" is about to change, allowing
	 * the NamedObjectCollection to fire a PropertyVetoException to stop the change if it likes. <p>
	 *
	 * This would happen, for example, if someone is trying to rename a value to a
	 * name that another value within this namedObjects already has.
	 */
	public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
		if (evt.getPropertyName().equals("name")) {
			//The name of a wrapper has changed. Make sure there are no name collisions
			BT wrapper = (BT) (evt.getSource());
			Object object = wrapper.getValue();
			if (containsObject(object)) {
				String name = (String) evt.getNewValue();
				BT otherWrapper = findBoxByName(name);
				if (otherWrapper != null && otherWrapper != wrapper) {
					throw new PropertyVetoException("Another value already has the name '" + name + "'", evt);
				}
			}
		}
	}

	public BrowserPanelGUI getLocalTreeAPI() {
		Debuggable.notImplemented();
		return null;
	}
}