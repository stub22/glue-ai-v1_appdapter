package org.appdapter.gui.box;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.appdapter.api.trigger.Box;
import org.appdapter.core.component.KnownComponent;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.BrowserPanelGUI;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.DisplayType;
import org.appdapter.gui.api.IGetBox;
import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.api.POJOBoxImpl;
import org.appdapter.gui.api.POJOCollectionListener;
import org.appdapter.gui.browse.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A BoxedCollection is a container of objects and corresponding POJOBoxs,
 * which add the concept of "name" and "selected".
 * <p>
 * 
 * Each value inside the BoxedCollection has a corresponding Box. A
 * Box has a reference to the value in memory it represents. Given an
 * value the only way to find the corresponding Box is to use
 * getBox(Object wrapper)
 * <p>
 * 
 * PropertyChangeListeners can register to find out when the selected value is
 * changed, in which case the property "selectedBoxed" will change.
 * <p>
 * 
 * POJOCollectionListeners can register to find out when objects are
 * added or removed.
 * 
 * @see Box
 * 
 */
@SuppressWarnings("serial")
public class BoxedCollectionImpl implements NamedObjectCollection, VetoableChangeListener, PropertyChangeListener, Serializable {
	// ==== Static variables ===================
	private static Logger theLogger = LoggerFactory.getLogger(BoxedCollectionImpl.class);

	static public NamedObjectCollection load(File source) throws IOException, ClassNotFoundException {

		FileInputStream fileIn = new FileInputStream(source);
		ObjectInputStream objectIn = new ObjectInputStream(fileIn);
		NamedObjectCollection b = (NamedObjectCollection) objectIn.readObject();
		fileIn.close();

		b.initAfterLoading();
		return b;
	}

	// private Map boxsToBoxedCollection = new HashMap();

	// An ordered list of objects
	//private List objectList = new LinkedList();
	protected LinkedList<BT> boxList = new LinkedList<BT>();
	//private LinkedList objectList = new LinkedList();
	transient public Object syncObject = boxList;

	// ============ Constructors
	// ==================================================

	transient private Set colListeners = new HashSet();

	private DisplayContext displayContext;

	//Maps value wrapper name to value wrapper
	private Map<String, BT> nameIndex = new Hashtable<String, BT>();

	private LinkedList<Object> objectList = new LinkedList<Object>();

	// ===== Serializable instance variables ================
	// Maps objects to their boxes
	private Map<Object, BT> objectsToWrappers = new HashMap();

	// ==== Queries ========================

	// ==== Transient instance variables ===================
	transient private PropertyChangeSupport propSupport = new PropertyChangeSupport(this);

	// The currently selected value
	private Object selected = null;

	String toStringText;

	public BoxedCollectionImpl() {
	}

	public BoxedCollectionImpl(String named, DisplayContext displayedAt) {
		toStringText = named;
		this.displayContext = displayedAt;
	}

	public boolean addBoxed(String title, BT wrapper) {
		synchronized (syncObject) {
			BT prev = findBoxByName(title);
			if (prev == wrapper)
				return true;
			if (prev != null) {
				Debuggable.notImplemented("Already existing name: " + title);
			}

			// Add it
			// objectsToBoxs.put(value, wrapper);
			Object value = wrapper.getValueOrThis();
			boxList.add(wrapper);
			objectList.add(value);

			if (objectsToWrappers != null) {
				synchronized (objectsToWrappers) {
					objectsToWrappers.put(value, wrapper);
				}
			}
			if (title == null) {
				title = getTitleOf(wrapper);
			}
			if (nameIndex != null) {
				synchronized (nameIndex) {
					nameIndex.put(title, wrapper);
				}
			}

			// Add myself as listener
			wrapper.addVetoableChangeListener(this);
			wrapper.addPropertyChangeListener(this);

			// Update the name index
			// nameIndex.put(wrapper.getName(), wrapper);

			// notify collectionListeners
			Iterator it = colListeners.iterator();
			while (it.hasNext()) {
				// @temp
				((POJOCollectionListener) it.next()).pojoAdded(value, (BT) wrapper);
			}
			return true;
		}

	}

	/**
	 * Listeners will find out when objects are added or removed
	 */
	public void addListener(POJOCollectionListener l) {
		synchronized (syncObject) {
			colListeners.add(l);
		}
	}

	public BT addObjectMaybe(Object value) throws PropertyVetoException {
		return addObjectMaybe(null, value);
	}

	public BT addObjectMaybe(String label, Object value) throws PropertyVetoException {
		synchronized (syncObject) {
			BT wrapper = findBoxByObject(value);
			if (wrapper == null) {
				wrapper = findBoxByName(label);
			}

			// Create the value wrapper, with a unique name
			String title = label;
			if (title == null) {
				title = generateUniqueName(value);
			}
			if (wrapper == null)
				wrapper = new ObjectWrapper(this, title, value);
			return wrapper;
		}

	}

	static class ObjectWrapper extends POJOBoxImpl implements BT, IGetBox {
		@Override public BT getBT() {
			return this;
		}

		//public Object value;
		private NamedObjectCollection noc;

		public void setNameValue(String uniqueName, Object value) {
			valueSetAs = value;

			if (uniqueName == null) {
				uniqueName = Utility.generateUniqueName(value, uniqueName, noc.getNameToBoxIndex());
			}
			name = uniqueName;
			if (value == null) {

				value = new NullPointerException(uniqueName).fillInStackTrace();
			}
			if (clz == null)
				clz = value.getClass();

			setShortLabel(uniqueName);
			setObject(value);
		}

		/*
		public POJOBoxImpl(NamedObjectCollection noc, String label) {
		this(noc, label, null);
		}*/

		/**
		 * Creates a new ScreenBox for the given value
		 * and assigns it a default name.
		 */
		public ObjectWrapper(NamedObjectCollection noc, String title, Object value) {
			this.noc = noc;
			setNameValue(title, value);
		}

		@Override public Object reallyGetValue() {
			return valueSetAs;
		}

		@Override public void reallySetValue(Object newObject) {
			valueSetAs = newObject;
		}

		@Override public Object getValue() {
			if (valueSetAs != null)
				return valueSetAs;
			return this;
		}

		@Override public Object getValueOrThis() {
			if (valueSetAs != null)
				return valueSetAs;
			return this;
		}
	}

	/**
	 * Listeners will be notifed when the currently value selection
	 * is changed.
	 */
	public void addPropertyChangeListener(PropertyChangeListener p) {
		synchronized (syncObject) {
			propSupport.addPropertyChangeListener(p);
		}
	}

	/**
	 * Checks if this namedObjects contains the given value
	 */
	@Override public boolean containsObject(Object value) {
		synchronized (syncObject) {
			value = Utility.dref(value);

			if (objectsToWrappers != null) {
				synchronized (objectsToWrappers) {
					return objectsToWrappers.containsKey(value);
				}
			}
			if (objectList != null)
				return objectList.contains(value);

			return findBoxByObject(value) != null;
		}
	}

	@Override public String getName() {
		return toStringText;
	}

	// ==== Event listener registration ======================

	/**
	 * Checks if this namedObjects contains the given value wrapper
	 */
	private boolean containsWrapper(BT wrapper) {
		synchronized (syncObject) {
			return boxList.contains(wrapper);
		}
		//    return objectsToWrappers.containsValue(value);
	}

	/**
	 * Creates a new value of the given class and adds to this namedObjects.
	 * The given class must have an empty constructor.
	 *
	 * @throws InstantiationException if the given Class represents an abstract class, an interface, an array class, a primitive type, or void; or if the instantiation fails for some other reason
	 * @throws IllegalAccessException if the given class or initializer is not accessible.
	 *
	 * @returns the newly created ScreenBox
	 */
	public synchronized Object createAndAddObject(Class cl) throws InstantiationException, IllegalAccessException {
		//Create the value
		Object value = cl.newInstance();

		//Add it
		findOrCreateBox(value);
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public BT findBoxByName(String name) {
		if (name == null)
			return null;
		synchronized (syncObject) {
			if (nameIndex != null) {
				synchronized (nameIndex) {
					BT boxl = nameIndex.get(name);
					if (boxl instanceof BT)
						return (BT) boxl;
				}
				return null;
			}
		}
		for (BT wrapper : getScreenBoxes()) {
			if (wrapper.isNamed(name)) {
				return wrapper;
			}
		}
		return null;
	}

	/**
	 * Returns the wrapper corresponding to the given value, i.e
	 * the ScreenBox who's value corresponds to the given one.
	 * Returns null if the BoxedCollection does not contain the given value.
	 */
	public BT findBoxByObject(Object value) {

		if (value == null)
			return null;

		BT utilityBT = Utility.asBTNoCreate(value);
		if (utilityBT != null) {
			return utilityBT;
		}

		value = Utility.dref(value, value);

		if (value instanceof String) {
			return findBoxByName((String) value);
		}
		int i = objectList.indexOf(value);
		if (i != -1) {
			BT wrapper = (BT) boxList.get(i);
			if (wrapper != null)
				return wrapper;
		}

		for (BT wrapper : getScreenBoxes()) {
			if (wrapper.representsObject(value))
				return wrapper;
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
			Object value = it.next();
			if (type.isInstance(value)) {
				result.add(value);
			}
		}
		for (BT value : getScreenBoxes()) {
			if (value.isTypeOf(type)) {
				result.add(value.convertTo(type));
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
	 * Adds the given value to the BoxedCollection, if it does not already exist.
	 *
	 * @returns true if the value was added, i.e. if it didn't already exist.
	 */
	public BT findOrCreateBox(String title, Object value) throws PropertyVetoException {
		synchronized (syncObject) {
			return findOrCreateBox0(title, value);
		}
	}

	private BT findOrCreateBox0(String title, Object value) throws PropertyVetoException {
		BT utilityBT = Utility.asBTNoCreate(value);
		if (utilityBT != null) {
			return utilityBT;
		}
		BT wrapper = findBoxByObject(value);
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
				title = generateUniqueName(value);
			}
			//Create the value wrapper, with a unique name
			wrapper = (BT) new ObjectWrapper(this, title, value);

			//Add it
			//objectsToWrappers.put(value, wrapper);
			objectList.add((Object) value);
			boxList.add(wrapper);
			objectsToWrappers.put(value, wrapper);

			//Add myself as listener
			wrapper.addVetoableChangeListener(this);
			wrapper.addPropertyChangeListener(this);

			//Update the name index
			nameIndex.put(title, wrapper);

			//notify namedObjectsListeners			
			Iterator it = colListeners.iterator();
			while (it.hasNext()) {
				//@temp
				((POJOCollectionListener) it.next()).pojoAdded(value, wrapper);
			}

			return wrapper;
		}
	}

	/**
	 * Generates a default name for the given value, while will be something
	 * like "Button1", "Button2", etc.
	 */
	public String generateUniqueName(Object value) {
		synchronized (syncObject) {
			return Utility.generateUniqueName(value, getNameToBoxIndex());
		}
	}

	/**
	 * Returns an iterator over all the value wrappers.
	 * NOTE - this could be a bit slow! Avoid whenever possible.
	 * The code can be optimized for this, but it isn't right now.
	 */
	@Override public Iterator<BT> getBoxes() {
		//LinkedList boxList = getBoxListFrom(DisplayType.TOSTRING);
		LinkedList list = new LinkedList();
		synchronized (syncObject) {
			list.addAll(boxList);
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
	}

	/**
	 * {@inheritDoc}
	 */
	public int getBoxesCount() {
		synchronized (syncObject) {
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
		BT wrapper = (BT) evt.getSource();
		return wrapper;
	}

	@Override public Map<String, BT> getNameToBoxIndex() {
		synchronized (syncObject) {
			return nameIndex;
		}
	}

	//==== Queries ========================

	/**
	 * Returns the value at the given index
	 */
	private Object getObjectAt(int index) {
		synchronized (syncObject) {
			return objectList.get(index);
		}
	}

	/**
	 * Returns the current number of objects in the namedObjects
	 */
	private int getObjectCount() {
		synchronized (syncObject) {
			return objectList.size();
		}
	}

	/**
	 * Returns an iterator over all the objects
	 */
	public Iterator getObjects() {
		synchronized (syncObject) {
			return new ArrayList(objectList).iterator();
		}
	}

	public Iterable<BT> getScreenBoxes() {
		synchronized (syncObject) {
			return new ArrayList<BT>(boxList);
		}
	}

	public Iterable<BT> getScreenBoxes(DisplayType attachType) {
		//	LinkedList boxList = getBoxListFrom(attachType);
		LinkedList list = new LinkedList();
		synchronized (syncObject) {
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

	private String getTitleOf(Box wrapper) {
		if (wrapper == null)
			return MISSING_COMPONENT;
		if (wrapper instanceof BT)
			return ((BT) wrapper).getUniqueName(getNameToBoxIndex());
		String lbl = ((KnownComponent) wrapper).getShortLabel();
		if (lbl != null)
			return lbl;
		return Utility.generateUniqueName(wrapper.getValue(), this.nameIndex);
	}

	public String getTitleOf(Object value) {
		if (value == null)
			return "<null>";
		synchronized (syncObject) {

			if (value instanceof Box) {
				return getTitleOf((Box) value);
			}
			if (value instanceof String) {
				return getTitleOf(findBoxByName("" + value));
			}
			BT wrapper = findBoxByObject(value);
			if (wrapper == null)
				return MISSING_COMPONENT;
			return getTitleOf(wrapper);
		}
	}

	@Override public BT asWrapped(Object d) {
		if (d == null || d instanceof BT) {
			return (BT) d;
		}
		return findOrCreateBox(d);
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
		synchronized (syncObject) {
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
	}

	private BT makeWrapper(Object value) throws PropertyVetoException {
		BT wrapper = new ObjectWrapper(this, generateUniqueName(value), value);
		return wrapper;
	}

	//===== Property notifications (i.e. others notifying me) =========

	/**
	 * This is used for Boxes to tell their NamedObjectCollection that a property
	 * such as "name" or "selected" has changed. The NamedObjectCollection will update
	 * its state as necessary.
	 */
	public synchronized void propertyChange(PropertyChangeEvent evt) {
		synchronized (syncObject) {
			propertyChange0(evt);
		}
	}

	private void propertyChange0(PropertyChangeEvent evt) {
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
			Object value = wrapper.getValue();
			if (containsObject(value)) {
				if (newValue.equals(new Boolean(true))) {
					try {
						setSelectedObject(value);
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
		synchronized (syncObject) {
			colListeners.remove(l);
		}
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
	public synchronized boolean removeObject(Object value) {
		synchronized (syncObject) {
			return removeObject0(value);
		}
	}

	private boolean removeObject0(Object value) {
		//Find the wrapper
		BT wrapper = findBoxByObject(value);
		if (wrapper == null)
			return false;
		Object realObj = wrapper.getValue();
		if (realObj != null && realObj != value) {
			theLogger.warn("This wrapper is for a differnt value " + wrapper + " not " + value);
			value = realObj;
		}

		String title = wrapper.getUniqueName();

		//Remove it
		//objectsToWrappers.remove(value);
		objectList.remove(value);
		boxList.remove(wrapper);
		objectsToWrappers.remove(value);

		//Update the name index			
		nameIndex.remove(title);

		//Deselect it if necessary
		if (selected == value) {
			try {
				//The value will fire a PropertyChangeEvent which I will
				//catch, so I don't need to do setSelectedObject(null)
				wrapper.setUISelected(false);
			} catch (PropertyVetoException err) {
				theLogger.warn("In NamedObjectCollection.removeObject(...) I was unable to deselect the removed value. I'll ignore the problem, i.e. leave it selected and remove it anyway.", err);
			}
		}

		//notify namedObjectsListeners
		synchronized (colListeners) {
			Iterator it = colListeners.iterator();
			while (it.hasNext()) {
				((POJOCollectionListener) it.next()).pojoRemoved(value, wrapper);
			}
		}
		return true;

	}

	/**
	 * Listeners will be notifed when the currently value selection
	 * is changed.
	 */
	public void removePropertyChangeListener(PropertyChangeListener p) {
		synchronized (syncObject) {
			propSupport.removePropertyChangeListener(p);
		}
	}

	@Override public void renameObject(String oldName, String newName) throws PropertyVetoException {
		BT wrapper = findOrCreateBox(newName, findObjectByName(oldName));
		wrapper.setUniqueName(newName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void save(File destination) throws IOException {
		theLogger.debug("Saving collection to " + destination);
		synchronized (syncObject) {
			FileOutputStream fileOut = new FileOutputStream(destination);
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(this);
			fileOut.close();
		}
		theLogger.debug("Successfully saved!");
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized void setSelectedComponent(Object value) throws PropertyVetoException {
		synchronized (syncObject) {
			value = Utility.dref(value);
			if (selected != value && containsObject(value)) {

				// Deselect the old wrapper (if any)
				Object oldSelected = selected;
				BT oldBox = findOrCreateBox(oldSelected);

				// Update my "selected" instance variable
				selected = value;

				// Fire a property change
				propSupport.firePropertyChange("selected", oldSelected, selected);

			}
		}
	}

	/**
	 * Makes the given value the currently selected one.
	 * The previously selected value (if any) will be deselected,
	 * and a property change event will be fired.
	 *
	 * @throws PropertyVetoException if someone refused to let the selected value change
	 */
	public synchronized void setSelectedObject(Object value) throws PropertyVetoException {
		if (selected != value && containsObject(value)) {
			synchronized (syncObject) {
				//Deselect the old wrapper (if any)
				Object oldSelected = selected;
				BT oldWrapper = findOrCreateBox(oldSelected);
				if (oldWrapper != null) {
					oldWrapper.setUISelected(false);
				}

				//Update my "selected" instance variable
				selected = value;

				//Make sure the corresponding wrapper knows its selected
				findOrCreateBox(selected).setUISelected(true);

				//Fire a property change
				propSupport.firePropertyChange("selected", oldSelected, selected);
			}
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
			Object value = wrapper.getValue();
			if (containsObject(value)) {
				String name = (String) evt.getNewValue();
				BT otherWrapper = findBoxByName(name);
				if (otherWrapper != null && otherWrapper != wrapper) {
					throw new PropertyVetoException("Another value already has the name '" + name + "'", evt);
				}
			}
		}
	}

	public BrowserPanelGUI getCurrentContext() {
		return Utility.getCurrentContext();
	}
}
