package org.appdapter.gui.pojo;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.List;

import org.appdapter.api.trigger.BoxImpl;
import org.appdapter.api.trigger.ScreenBoxPanel;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.core.name.Ident;
import org.appdapter.gui.util.PromiscuousClassUtils;

/**
 * A wrapper for objects used in the ScreenBox system. It holds an object,
 * a name, and info about whether it is selected or not. The "name" and
 * "selected" properties are bound and constrained, i.e. you can listen to
 * changes using addPropertyChangeListener, and you can also prevent changes in
 * some cases if you use addVetoableChangeListener.
 * 
 * 
 */
abstract public class POJOBox<TrigType extends Trigger<? extends BoxImpl<TrigType>>> extends

BoxImpl<TrigType> implements java.io.Serializable, GetSetObject, Convertable {
	// ==== Transient instance variables =============
	transient PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
	transient VetoableChangeSupport vetoSupport = new VetoableChangeSupport(this);

	@Override public <T> T[] getObjects(Class<T> type) {
		HashSet<Object> objs = new HashSet<Object>();
		if (this.canConvert(type)) {
			T one = convertTo(type);
			objs.add(one);
		}
		for (Object o : getObjects()) {
			if (type.isInstance(o)) {
				objs.add(o);
			}
		}
		return objs.toArray((T[]) Array.newInstance(type, objs.size()));
	}

	@Override public <T> boolean canConvert(Class<T> c) {
		for (Object o : getObjects()) {
			if (o == null)
				continue;
			if (!c.isInstance(o))
				continue;
			try {
				final T madeIT = (T) o;
				if (madeIT != null)
					return true;
			} catch (Exception e) {
				getLogger().error("JVM Issue (canConvert)", e);
			}
			return true;
		}
		return false;
	}

	@Override public <T> T convertTo(Class<T> c) {
		for (Object o : getObjects()) {
			if (o == null)
				continue;
			if (!c.isInstance(o))
				continue;
			try {
				return c.cast(o);
			} catch (Exception e) {
				getLogger().error("JVM Issue (canConvert)", e);
				return (T) o;
			}
		}
		throw new ClassCastException("Cannot convert " + getDebugName() + " to " + c);
	}

	public String getDebugName() {
		return getObject().toString();
	}

	/** 
	 * This returns the decomposed Mixins
	 * @return
	 */
	public Object[] getObjects() {
		Object o = getObject();
		if (o != null && o != this)
			return new Object[] { o, this, getName(), getIdent() };
		return new Object[] { this, getName(), getIdent() };
	}

	@Override public List<TrigType> getTriggers() {
		List<TrigType> tgs = super.getTriggers();
		for (Class cls : getTypes()) {
			Utility.addClassLevelTriggers(cls, tgs, this);
		}
		return tgs;
	}

	protected String _name = null;

	// ==== Constructors ==================================
	/**
	 * Creates a new Swizzler for the given object and assigns it a default
	 * name.
	 */
	public POJOBox() {
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
		if (_name == null) {
			Ident ident = getIdent();
			if (ident != null) {
				_name = ident.getAbsUriString();
			} else {
				Object object = getObject();
				if (object != null) {
					_name = Utility.generateUniqueName(object);
				} else {
					_name = Utility.generateUniqueName(this);
				}
			}
		}
		return _name;
	}

	/**
	 * Changes the name of this object. The name should never be null.
	 * 
	 * @throws PropertyVetoException
	 *             if someone refused to allow the name to change
	 */
	public void setName(String newName) throws PropertyVetoException {
		String name = getName();
		if (!(newName.equals(name))) {
			checkTransient();
			String oldName = name;
			vetoSupport.fireVetoableChange("name", oldName, newName);
			this._name = newName;
			propSupport.firePropertyChange("name", oldName, newName);
		}
		String os = getShortLabel();
		if (os == null) {
			setShortLabel(newName);
		}
	}

	/**
	 * Gets a BeanInfo object for this object, using the Introspector class
	 */
	public BeanInfo getBeanInfo() {
		try {
			return Utility.getPOJOInfo(getPOJOClass(), Introspector.USE_ALL_BEANINFO);
		} catch (IntrospectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				return Utility.getPOJOInfo(Object.class, Introspector.USE_ALL_BEANINFO);
			} catch (IntrospectionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return new SimplePOJOInfo();
			}
		}
	}

	public Class<? extends Object> getPOJOClass() {
		Object obj = getObject();
		if (obj != null)
			return obj.getClass();
		return getClass();
	}

	/**
	 * Returns the name of this object
	 */
	@Override public String toString() {
		return super.toString();
		//return getName();
	}

	// ========= Utility methods =================

	public static String getDefaultName(Object object) {
		if (object == null)
			return "<null>";
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
		String name = getName();
		return name.equals(test);
	}

	static public DisplayType getDisplayType(Class expected) {
		expected = PromiscuousClassUtils.nonPrimitiveTypeFor(expected);
		if (Number.class.isAssignableFrom(expected)) {
			return DisplayType.TOSTRING;
		}
		if (expected == String.class) {
			return DisplayType.TOSTRING;
		}
		return DisplayType.PANEL;
	}

	final public ScreenBoxPanel getPropertiesPanel() {
		Object obj = getObject();
		if (obj instanceof ScreenBoxPanel) {
			return (ScreenBoxPanel) obj;
		}
		if (obj == this) {
			ScreenBoxPanel pnl = new BasicObjectCustomizer(Utility.getCurrentContext(), obj);
			pnl.setName(getShortLabel());
			return pnl;
		}
		ScreenBoxPanel pnl = new BasicObjectCustomizer(Utility.getCurrentContext(), obj);
		pnl.setName(getShortLabel());
		return pnl;
	}

}