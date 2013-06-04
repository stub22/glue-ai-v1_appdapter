package org.appdapter.gui.box;

import java.awt.Component;
import java.awt.Container;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.SimpleBeanInfo;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.appdapter.api.trigger.AnyOper.UIHidden;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxImpl;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.core.name.Ident;
import org.appdapter.gui.box.ScreenBoxPanel.Kind;
import org.appdapter.gui.browse.DisplayContext;
import org.appdapter.gui.pojo.Convertable;
import org.appdapter.gui.pojo.DisplayType;
import org.appdapter.gui.pojo.GetSetObject;
import org.appdapter.gui.pojo.NamedObjectCollection;
import org.appdapter.gui.pojo.POJOBox;
import org.appdapter.gui.pojo.TriggerForInstance;
import org.appdapter.gui.pojo.Utility;

/**
 * 
 *  Plain Old Java Object in a "Box"
 * 
 * A wrapper for objects used in the ScreenBox system. It holds an object,
 * a name, and info about whether it is selected or not. The "name" and
 * "selected" properties are bound and constrained, i.e. you can listen to
 * changes using addPropertyChangeListener, and you can also prevent changes in
 * some cases if you use addVetoableChangeListener.
 * 
 * 
 */
@UIHidden
public abstract class POJOBoxImpl<TrigType extends Trigger<? extends POJOBoxImpl<TrigType>>> extends BoxImpl<TrigType>

implements java.io.Serializable, GetSetObject, Convertable, POJOBox<TrigType>, DisplayContextProvider, UIProvider {

	@UIHidden
	public class NoObject {

	}

	public static String getDefaultName(Object object) {
		if (object == null)
			return "<null>";
		Class type = object.getClass();
		if (type == Class.class)
			return ((Class) object).getName();
		else
			return "a " + Utility.getShortClassName(object.getClass());
	}

	static public DisplayType getDisplayType(Class expected) {
		if (expected.isPrimitive()) {
			return DisplayType.TOSTRING;
		}
		if (Number.class.isAssignableFrom(expected)) {
			return DisplayType.TOSTRING;
		}
		if (expected == String.class || CharSequence.class.isAssignableFrom(expected)) {
			return DisplayType.TOSTRING;
		}
		return DisplayType.PANEL;
	}

	protected String _uname = null;
	public Box m_box;

	DisplayType m_displayType = DisplayType.PANEL;

	public boolean m_is_added;
	public Object m_obj = new NullPointerException("pojoObject" + System.currentTimeMillis()).fillInStackTrace();
	public Container m_parent_component;

	public String m_title;

	public BoxPanelSwitchableView m_toplevel;

	public Component m_view;

	// ==== Transient instance variables =============
	transient PropertyChangeSupport propSupport = new PropertyChangeSupport(this);

	public String registeredWithName;

	transient VetoableChangeSupport vetoSupport = new VetoableChangeSupport(this);

	// ==== Constructors ==================================
	/**
	 * Creates a new Plain Old Java Object Box for the given object and assigns it a default
	 * name.
	 */
	public POJOBoxImpl() {
	}

	public POJOBoxImpl(String label) {
		// TODO Auto-generated constructor stub
	}

	public POJOBoxImpl(String title, Object boxOrObj) {
		setShortLabel(title);
		setObject(boxOrObj);
	}

	public POJOBoxImpl(String title, Object boxOrObj, Component vis, DisplayType displayType, Container parent, BoxPanelSwitchableView bpsv) {
		m_view = vis;
		m_parent_component = parent;
		if (boxOrObj instanceof Box) {
			m_box = (Box) boxOrObj;
		} else {
			m_obj = boxOrObj;
		}
		m_toplevel = bpsv;
		m_displayType = displayType;
		((BoxPanelSwitchableViewImpl) m_toplevel).registerPair(this, false);
	}

	abstract protected Component findOrCreateBoxPanel(Kind objectProperties);

	/**
	 * PropertyChangeListeners will find out when the name or selection state
	 * changes.
	 */
	public void addPropertyChangeListener(PropertyChangeListener p) {
		checkTransient();
		propSupport.addPropertyChangeListener(p);
	}

	/**
	 * VetoableChangeListeners will find out when the name or selection state is
	 * about to change, and can prevent such changes if desired.
	 */
	public void addVetoableChangeListener(VetoableChangeListener v) {
		checkTransient();
		vetoSupport.addVetoableChangeListener(v);
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

	/**
	 * Updates transient instance variables if necessary
	 */
	private void checkTransient() {
		if (propSupport == null) {
			propSupport = new PropertyChangeSupport(this);
			vetoSupport = new VetoableChangeSupport(this);
		}
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

	@Override public DisplayContext findDisplayContext(Box b) {
		// TODO Auto-generated method stub
		return null;
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
				return new SimpleBeanInfo();
			}
		}
	}

	public BoxPanelSwitchableView getBoxPanelTabPane() {
		return m_toplevel;
	}

	// ==== Event listener registration =============

	@Override public Component getComponent() {
		if (m_view != null)
			return m_view;
		for (Object o : getObjects()) {
			if (Component.class.isInstance(o))
				return (Component) o;
		}
		return findOrCreateBoxPanel(Kind.OBJECT_PROPERTIES);
	}

	@Override public Component getComponent(DisplayType attachType) {
		return getDisplayTarget(getDisplayType());
	}

	public String getDebugName() {
		try {
			return getValue().toString();
		} catch (Exception e) {
			return super.toString();
		}
	}

	@Override public Container getDisplayTarget(DisplayType attachType) {
		return getPropertiesPanel();
	}

	// ===== Property getters and setters ========================

	@Override public DisplayType getDisplayType() {
		return m_displayType;
	}

	@Override public Object getObject() {
		return m_obj;
	}

	/** 
	 * This returns the decomposed Mixins
	 * @return
	 */
	public Object[] getObjects() {
		Object o = getValue();
		if (o != null && o != this)
			return new Object[] { o, this, getUniqueName(), getIdent() };
		return new Object[] { this, getUniqueName(), getIdent() };
	}

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

	public Class<? extends Object> getPOJOClass() {
		Object obj = getValue();
		if (obj != null)
			return obj.getClass();
		return getClass();
	}

	final public ScreenBoxPanel getPropertiesPanel() {
		Object obj = getValue();
		if (obj instanceof ScreenBoxPanel) {
			return (ScreenBoxPanel) obj;
		}
		if (obj == this) {
			ScreenBoxPanel pnl = Utility.getPropertiesPanel(obj);
			pnl.setName(getShortLabel());
			return pnl;
		}
		ScreenBoxPanel pnl = Utility.getPropertiesPanel(obj);
		pnl.setName(getShortLabel());
		return pnl;
	}

	@Override public List<TrigType> getTriggers() {
		List<TrigType> tgs = super.getTriggers();
		for (Class cls : getTypes()) {
			TriggerForInstance.addClassLevelTriggers(cls, tgs, this);
		}
		return tgs;
	}

	/**
	 * Returns the Class[]s that this object wrapper represents
	 */
	@Override public List<Class> getTypes() {
		java.util.HashSet al = new java.util.HashSet<Class>();
		Class pojoClass = getPOJOClass();
		if (pojoClass != null)
			al.add(pojoClass);
		else {
			al.add(getClass());
		}
		return new ArrayList<Class>(al);
	}

	/**
	 * Returns the name of this object
	 */
	final public String getUniqueName() {
		if (_uname == null) {
			Ident ident = getIdent();
			if (ident != null) {
				_uname = ident.getAbsUriString();
			} else {
				Object object = getValue();
				if (object != null) {
					_uname = Utility.generateUniqueName(object);
				} else {
					_uname = Utility.generateUniqueName(this);
				}
			}
		}
		return _uname;
	}

	/**
	 * Returns the object that this object wrapper represents
	 */
	@Override public Object getValue() {
		if (m_obj == null) {
			getLogger().warn("Default implementation of getObject() for NULL is returning 'this'", getShortLabel());
			return this;
		}
		if (m_obj != this)
			return m_obj;

		getLogger().warn("Default implementation of getObject() for {} is returning 'this'", getShortLabel());
		return this;
	}

	public boolean isNamed(String test) {
		if (Utility.stringsEqual(test, getUniqueName()))
			return true;
		if (Utility.stringsEqual(test, getShortLabel()))
			return true;
		return false;
	}

	public boolean isTypeOf(Class type) {
		for (Class c : getTypes()) {
			if (type.isAssignableFrom(c))
				return true;
		}
		return type.isInstance(getValue());
	}

	@Override public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub

	}

	// ========= Utility methods =================

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
	public void removeVetoableChangeListener(VetoableChangeListener v) {
		checkTransient();
		vetoSupport.removeVetoableChangeListener(v);
	}

	public boolean representsObject(Object test) {
		return getValue().equals(test);
	}

	public void setNameValue(String uniqueName, Object obj) {
		if (uniqueName == null) {
			if (obj != null) {
				uniqueName = "sihc-" + System.identityHashCode(obj) + "-" + System.currentTimeMillis();
			} else {
				uniqueName = "snul-" + System.identityHashCode(this) + "-" + System.currentTimeMillis();
			}
		}
		_uname = uniqueName;
		if (obj == null)
			obj = new NullPointerException(uniqueName).fillInStackTrace();
		setShortLabel(uniqueName);
		setObject(obj);
	}

	@Override public void setObject(Object obj) {
		m_obj = obj;
		String ds = getDescription();
		if (ds == null) {
			setDescription("" + obj + " " + obj.getClass());
		}
	}

	/**
	 * Changes the name of this object. The name should never be null.
	 * 
	 * @throws PropertyVetoException
	 *             if someone refused to allow the name to change
	 */
	public void setUniqueName(String newName) throws PropertyVetoException {
		String name = getUniqueName();
		if (!(newName.equals(name))) {
			checkTransient();
			String oldName = name;
			vetoSupport.fireVetoableChange("name", oldName, newName);
			this._uname = newName;
			propSupport.firePropertyChange("name", oldName, newName);
		}
		String os = getShortLabel();
		if (os == null) {
			setShortLabel(newName);
		}
	}

	// the jtree label uses this .. so supply someting good!
	@Override public String toString() {
		String sl = getShortLabel();
		if (sl != null)
			return sl;
		return getUniqueName() + " -> " + getDebugName();

	}

	public NamedObjectCollection getNamedObjectCollection() {
		// TODO Auto-generated method stub
		return (NamedObjectCollection) m_toplevel;
	}

	public void showError(String msg, Throwable err) {
		m_toplevel.showError(msg, err);

	}

	public ScreenBoxPanel showScreenBox(Object value) {
		return m_toplevel.showScreenBox(value);
	}

	public void setSelectedComponent(Object object) throws PropertyVetoException {
		// TODO Auto-generated method stub

	}

	public void showError(String string, Object object) {
		// TODO Auto-generated method stub

	}

	@Override public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
		// TODO Auto-generated method stub

	}

}