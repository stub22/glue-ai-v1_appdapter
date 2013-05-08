package org.appdapter.gui.objbrowser.model;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;


/**
 * A wrapper for objects used in the ObjectNavigator system. It holds an object,
 * a name, and info about whether it is selected or not. The "name" and
 * "selected" properties are bound and constrained, i.e. you can listen to
 * changes using addPropertyChangeListener, and you can also prevent changes in
 * some cases if you use addVetoableChangeListener.
 * 
 * 
 */
public class POJOSwizzler implements java.io.Serializable {
	// ==== Transient instance variables =============
	transient PropertyChangeSupport propSupport = new PropertyChangeSupport(
			this);
	transient VetoableChangeSupport vetoSupport = new VetoableChangeSupport(
			this);

	// ==== Serializable instance variables ===============
	Object object;
	String name = null;
	boolean selected = false;

	// ==== Constructors ==================================

	/**
	 * Creates a new Swizzler for the given object and assigns it a default
	 * name.
	 */
	public POJOSwizzler(Object object) {
		this.object = object;
		this.name = getDefaultName(object);
	}

	/**
	 * Creates a new Swizzler for the given object, with the given name.
	 */
	public POJOSwizzler(String name, Object object) {
		this.object = object;
		this.name = name;
	}

	// ==== Event listener registration =============

	/**
	 * PropertyChangeListeners will find out when the name or selection state
	 * changes.
	 */
	public void addPropertyChangeListener(PropertyChangeListener p) {
		checkTransient();
		propSupport.addPropertyChangeListener(p);
	}

	/**
	 * PropertyChangeListeners will find out when the name or selection state
	 * changes.
	 */
	public void removePropertyChangeListener(PropertyChangeListener p) {
		checkTransient();
		propSupport.removePropertyChangeListener(p);
	}

	/**
	 * VetoableChangeListeners will find out when the name or selection state is
	 * about to change, and can prevent such changes if desired.
	 */
	public void addVetoableChangeListener(VetoableChangeListener v) {
		checkTransient();
		vetoSupport.addVetoableChangeListener(v);
	}

	/**
	 * VetoableChangeListeners will find out when the name or selection state is
	 * about to change, and can prevent such changes if desired.
	 */
	public void removeVetoableChangeListener(VetoableChangeListener v) {
		checkTransient();
		vetoSupport.removeVetoableChangeListener(v);
	}

	// ===== Property getters and setters ========================

	/**
	 * Changes the selection state.
	 * 
	 * @throws PropertyVetoException
	 *             if someone refused to allow selection state change
	 */
	public void setSelected(boolean newSelected) throws PropertyVetoException {
		if (newSelected != selected) {
			checkTransient();
			boolean oldSelected = selected;
			vetoSupport.fireVetoableChange("selected",
					new Boolean(oldSelected), new Boolean(newSelected));
			this.selected = newSelected;
			propSupport.firePropertyChange("selected",
					new Boolean(oldSelected), new Boolean(newSelected));
		}
	}

	/**
	 * True if this object is selected
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Returns the object that this object wrapper represents
	 */
	public Object getObject() {
		return object;
	}

	/**
	 * Returns the name of this object
	 */
	public String getName() {
		return name;
	}

	/**
	 * Changes the name of this object. The name should never be null.
	 * 
	 * @throws PropertyVetoException
	 *             if someone refused to allow the name to change
	 */
	public void setName(String newName) throws PropertyVetoException {
		if (!(newName.equals(name))) {
			checkTransient();
			String oldName = name;
			vetoSupport.fireVetoableChange("name", oldName, newName);
			this.name = newName;
			propSupport.firePropertyChange("name", oldName, newName);
		}
	}

	/**
	 * Gets a BeanInfo object for this object, using the Introspector class
	 */
	public BeanInfo getBeanInfo() {
		try {
			return Utility.getPOJOInfo(getPojoClass(),
					Introspector.USE_ALL_BEANINFO);
		} catch (IntrospectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				return Utility.getPOJOInfo(Object.class,
						Introspector.USE_ALL_BEANINFO);
			} catch (IntrospectionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return new SimplePOJOInfo();
			}
		}
	}

	public Class<? extends Object> getPojoClass() {
		return getObject().getClass();
	}

	/**
	 * Returns the name of this object
	 */
	public String toString() {
		return getName();
	}

	// ========= Utility methods =================

	public static String getDefaultName(Object object) {
		Class type = object.getClass();
		if (type == Class.class)
			return ((Class) object).getName();
		else
			return "a " + Utility.getShortClassName(object.getClass());
	}

	/**
	 * Updates transient instance variables if necessary
	 */
	private void checkTransient() {
		if (propSupport == null) {
			propSupport = new PropertyChangeSupport(this);
			vetoSupport = new VetoableChangeSupport(this);
		}
	}

	public boolean isInstance(Class type) {
		return type.isInstance(getObject());
	}

	public boolean representsObject(Object test) {
		return object.equals(test);
	}

	public boolean isNamed(String test) {
		return name.equals(test);
	}
}