package org.appdapter.gui.pojo;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.util.List;

import org.appdapter.api.trigger.BoxImpl;
import org.appdapter.api.trigger.Trigger;

/**
 * A wrapper for objects used in the ObjectNavigator system. It holds an object,
 * a name, and info about whether it is selected or not. The "name" and
 * "selected" properties are bound and constrained, i.e. you can listen to
 * changes using addPropertyChangeListener, and you can also prevent changes in
 * some cases if you use addVetoableChangeListener.
 * 
 * 
 */
abstract public class POJOSwizzler<TrigType extends Trigger<? extends BoxImpl<TrigType>>> extends BoxImpl<TrigType> implements java.io.Serializable {
	// ==== Transient instance variables =============
	transient PropertyChangeSupport propSupport = new PropertyChangeSupport(
			this);
	transient VetoableChangeSupport vetoSupport = new VetoableChangeSupport(
			this);

	protected String name = null;

	// ==== Constructors ==================================
	/**
	 * Creates a new Swizzler for the given object and assigns it a default
	 * name.
	 */
	public POJOSwizzler() {
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
	 * Returns the object that this object wrapper represents
	 */
	abstract public Object getObject();

	/**
	 * Returns the Class[]s that this object wrapper represents
	 */
	abstract public List<Class> getTypes();
	
	/**
	 * Returns the name of this object
	 */
	public String getName() {
		if (name==null) return getIdent().toString();
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
	@Override
	public String toString() {
		return super.toString();
		//return getName();
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
		return getObject().equals(test);
	}

	public boolean isNamed(String test) {
		return name.equals(test);
	}

}