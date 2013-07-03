package org.appdapter.api.trigger;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.Customizer;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.appdapter.api.trigger.AnyOper.UIHidden;
import org.appdapter.api.trigger.ScreenBox.Kind;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.name.Ident;
import org.appdapter.gui.api.ComponentHost;
import org.appdapter.gui.api.GetSetObject;
import org.appdapter.gui.api.UseEditor;
import org.appdapter.gui.api.Utility;
import org.appdapter.gui.box.ScreenBoxImpl;
import org.appdapter.gui.box.WrapperValue;
import org.appdapter.gui.rimpl.TriggerForInstance;
import org.appdapter.gui.util.CollectionSetUtils;
import org.appdapter.gui.util.PromiscuousClassUtils;

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
public abstract class POJOBoxImpl<TrigType extends Trigger<? extends POJOBoxImpl<TrigType>>> //
		extends ScreenBoxImpl<TrigType> //

		implements java.io.Serializable, GetSetObject, Convertable, GetDisplayContext, DisplayContextProvider, UIProvider, WrapperValue, BT {

	abstract public void reallySetValue(Object newObject);

	//===== Inner classes ==========================
	/**
	 * A rather ugly but workable default icon used in cases
	 * where there is no known icon for the value.
	 */
	static class UnknownIcon implements Icon, java.io.Serializable {
		@Override public int getIconHeight() {
			return 16;
		}

		@Override public int getIconWidth() {
			return 16;
		}

		@Override public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(Color.blue);
			g.setFont(new Font("serif", Font.BOLD, 12));
			g.drawString("@", x, y + 12);
		}
	}

	public static BeanInfo getBeanInfo(Class beanClass) throws IntrospectionException {
		return Introspector.getBeanInfo(beanClass, Introspector.USE_ALL_BEANINFO);
	}

	/**
	 * Returns an Icon for this value, determined using BeanInfo.
	 * If no icon was found a default icon will be returned.
	 */
	static public Icon getIcon(BeanInfo info) {
		Icon icon;
		try {
			Image image;
			image = info.getIcon(BeanInfo.ICON_COLOR_16x16);
			if (image == null) {
				image = info.getIcon(BeanInfo.ICON_MONO_16x16);
			}

			if (image == null) {
				icon = new UnknownIcon();
			} else {
				icon = new ImageIcon(image);
			}
		} catch (Exception err) {
			icon = new UnknownIcon();
		}
		return icon;
	}

	/*@UIHidden
	public class NoObject implements UIHidden {
		private Throwable myCreation;
		private String myName;

		public String toString() {
			return myName;
		}

		NoObject() {
			this.myName = "NO_pojoObject" + System.currentTimeMillis();
			this.myCreation = new NullPointerException(myName).fillInStackTrace();
		}

		@Override public Class<? extends Annotation> annotationType() {
			if (true)
				return UIHidden.class;
			return (Class<? extends Annotation>) getClass().getInterfaces()[0];
		}
	}*/

	//	protected String _uname = null;
	//	public Box m_box = this;

	//	DisplayType m_displayType = DisplayType.PANEL;
	/*
		public static String getDefaultName(Object object) {
			Class type = object.getClass();
			if (type == Class.class)
				return ((Class) object).getName();
			else
				return "a " + Utility.getShortClassName(object.getClass());
		}*/

	//	public String m_title;

	//	public BoxPanelSwitchableView m_toplvl;

	//public String registeredWithName;

	/*
		public POJOBoxImpl(NamedObjectCollection noc, String title, Object boxOrObj) {
			m_view = vis;
			m_parent_component = parent;
			if (boxOrObj instanceof Box) {
				m_box = (Box) boxOrObj;
				m_obj = boxOrObj;
			} else {
				m_obj = boxOrObj;
			}
			m_displayType = displayType;
			if (bpsv != null) {
				m_toplvl = bpsv;
				m_toplvl.registerPair(this, false);
			}
			addToNoc(noc, title);
		}
	*/
	Map<NamedObjectCollection, String> col2Name = new HashMap<NamedObjectCollection, String>();
	public DisplayContext m_displayContext;
	public DisplayType m_displayType = DisplayType.PANEL;
	//	public boolean m_is_added;
	//	public Container m_parent_component;

	public Class<TrigType> clz;
	public String name = null;

	// ==== Transient instance variables =============
	transient PropertyChangeSupport propSupport = new PropertyChangeSupport(this);

	boolean selected = false;

	//==== Serializable instance variables ===============
	//public Object value = null;//this;//;//new NoObject();
	protected transient VetoableChangeSupport vetoSupport = new VetoableChangeSupport(this);

	// ==== Constructors ==================================
	/**
	 * Creates a new Plain Old Java Object Box for the given object and assigns it a default
	 * name.
	 */

	public POJOBoxImpl() {
		super(false);
	}

	/**
	 * PropertyChangeListeners will find out when the name or selection state
	 * changes.
	 */
	@Override public void addPropertyChangeListener(PropertyChangeListener p) {
		checkTransient();
		propSupport.addPropertyChangeListener(p);
	}

	/*public BoxPanelSwitchableView getBoxPanelTabPane() {
		return getDisplayContextNoLoop();
	}*/

	// ==== Event listener registration =============

	protected void addToNoc(NamedObjectCollection noc, String title) {
		if (this instanceof POJOCollectionListener) {
			noc.addListener((POJOCollectionListener) this);
		}
		col2Name.put(noc, title);
		noc.addBoxed(title.toString(), this);
	}

	/**
	 * Returns the object that this object wrapper represents
	 */
	@Override public Object getValueOrThis() {
		Object value = getValue();
		if (value != null) {
			return value;
		}
		if (value == null) {
			//getLogger().warn("Default implementation of getObject() for NULL is returning 'this'", getShortLabel());
			return this;
		}
		if (value != this) {
			return value;
		}

		//	getLogger().warn("Default implementation of getObject() for {} is returning 'this'", getShortLabel());
		return this;
	}

	@Override public void setObject(Object obj) {
		String ds = getDescription();
		if (ds == null) {
			setDescription("" + obj + " " + obj.getClass());
		}
		Object value = getValue();
		if (value == obj) {
			return;
		}
		try {
			valueChanged(value, obj);
		} catch (PropertyVetoException e) {
			throw Debuggable.reThrowable(e);
		}
	}

	public void valueChanged(Object oldObject, Object newObject) throws PropertyVetoException {
		checkTransient();
		String oldName = name;
		vetoSupport.fireVetoableChange("value", oldObject, newObject);
		this.reallySetValue(newObject);
		propSupport.firePropertyChange("value", oldObject, newObject);
	}

	@Override public Box asBox() {
		return this;
	}

	/**
	 * VetoableChangeListeners will find out when the name or selection state is
	 * about to change, and can prevent such changes if desired.
	 */
	@Override public void addVetoableChangeListener(VetoableChangeListener v) {
		checkTransient();
		vetoSupport.addVetoableChangeListener(v);
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

	// ===== Property getters and setters ========================

	/**
	 * Gets a BeanInfo object for this object, using the Introspector class
	 */
	@Override public BeanInfo getBeanInfo() throws IntrospectionException {
		try {
			return Utility.getPOJOInfo(getObjectClass(), Introspector.USE_ALL_BEANINFO);
		} catch (IntrospectionException e) {
			try {
				return Utility.getPOJOInfo(Object.class, Introspector.USE_ALL_BEANINFO);
			} catch (IntrospectionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return getBeanInfo(this.getObjectClass());
			}
		}
	}

	public Component getComponent() {
		//if (m_view != null) 			return m_view;		
		for (Object o : getObjects()) {
			if (Component.class.isInstance(o)) {
				return (Component) o;
			}
		}
		return findOrCreateBoxPanel(Kind.OBJECT_PROPERTIES);
	}

	@Override public Component getComponent(DisplayType attachType) {
		return getDisplayTarget(getDisplayType());
	}

	@Override public String getDebugName() {
		try {
			Object o = getValue();
			if (o == null) {
				return getUniqueName();
			}
			return o.toString();
		} catch (Exception e) {
			return super.toString();
		}
	}

	@Override public DisplayContext getDisplayContext() {
		return m_displayContext;
	}

	@Override public Container getDisplayTarget(DisplayType attachType) {
		return getPropertiesPanel();
	}

	@Override public DisplayType getDisplayType() {
		return m_displayType;
	}

	@Override public Class<? extends Object> getObjectClass() {
		if (clz != null) {
			return this.clz;
		}
		Object obj = getValue();
		if (obj != null) {
			return obj.getClass();
		}
		return Object.class;
	}

	// ========= Utility methods =================

	/*
		public NamedObjectCollection getNamedObjectCollection() {
			BoxPanelSwitchableView m_toplevel = getDisplayContextNoLoop();
			if (m_toplevel != null) {
				NamedObjectCollection m_collection = m_toplevel.getNamedObjectCollection();
				if (m_collection != null)
					return m_collection;
			}
			Debuggable.notImplemented();
			Debuggable.notImplemented();
			return (NamedObjectCollection) m_toplevel.getNamedObjectCollection();
		}
	*/

	/**
	 * True if this value is selected
	 */
	@Override public boolean getUISelected() {
		return selected;
	}

	/**
	 * Returns the name of this value
	 */
	@Override public String getUniqueName() {
		return name;
	}

	/**
	 * Returns the name of this object
	 */
	@Override final public String getUniqueName(Map checkAgainst) {
		//String _uname = null;
		if (name == null) {
			Ident ident = getIdent();
			if (ident != null) {
				name = ident.getAbsUriString();
			} else {
				Object object = getValue();
				if (object != null) {
					name = Utility.generateUniqueName(object, checkAgainst);
				} else {
					name = Utility.generateUniqueName(this, checkAgainst);
				}
			}
		}
		return name.toString();
	}

	@Override public boolean isNamed(String test) {
		if (Utility.stringsEqual(test, name)) {
			return true;
		}
		if (Utility.stringsEqual(test, getShortLabel())) {
			return true;
		}
		return false;
	}

	@Override public boolean isTypeOf(Class type) {
		for (Class c : getTypes()) {
			if (type.isAssignableFrom(c)) {
				return true;
			}
		}
		return type.isInstance(getValue());
	}

	//==== Constructors ==================================

	/*

	private BoxPanelSwitchableView getDisplayContextNoLoop() {
		return m_toplvl;
	}

	public ScreenBoxPanel showScreenBox(Object value) {
		BoxPanelSwitchableView m_toplevel = getDisplayContextNoLoop();
		return m_toplevel.showScreenBox(value);
	}

	public Collection getTriggersFromUI(Object object) {
		BoxPanelSwitchableView m_toplevel = getDisplayContextNoLoop();
		return m_toplevel.getTriggersFromUI(object);
	}

	public ScreenBoxPanel showError(String msg, Throwable error) {
		BoxPanelSwitchableView m_toplevel = getDisplayContextNoLoop();
		return m_toplevel.showError(msg, error);
	}

	@Override public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
		Debuggable.notImplemented();
		Debuggable.notImplemented();
	}

	public ScreenBoxPanel showMessage(String msg) {
		BoxPanelSwitchableView m_toplevel = getDisplayContextNoLoop();
		return m_toplevel.showMessage(msg);
	}
	*/

	@Override public void propertyChange(PropertyChangeEvent evt) {
		propSupport.firePropertyChange(evt);
	}

	//===== Property getters and setters ========================

	/**
	 * PropertyChangeListeners will find out when the name or selection state
	 * changes.
	 */
	@Override public void removePropertyChangeListener(PropertyChangeListener p) {
		checkTransient();
		propSupport.removePropertyChangeListener(p);
	}

	/**
	 * VetoableChangeListeners will find out when the name or selection state is
	 * about to change, and can prevent such changes if desired.
	 */
	@Override public void removeVetoableChangeListener(VetoableChangeListener v) {
		checkTransient();
		vetoSupport.removeVetoableChangeListener(v);
	}

	@Override public boolean representsObject(Object test) {
		if (test == null) {
			return false;
		}
		Object myObj = getValue();
		if (myObj == test) {
			return true;
		}
		if (this == test) {
			return true;
		}
		if (test == m_displayContext) {
			return true;
		}
		for (Object p : myPanelMap.values()) {
			if (p == test) {
				return true;
			}
		}
		return name == test;
	}

	public void setSelectedComponent(Object object) throws PropertyVetoException {

	}

	@Override public void setShortLabel(String shortLabel) {
		super.setShortLabel(shortLabel);
	}

	/**
	 * Changes the selection state.
	 *
	 * @throws PropertyVetoException if someone refused to allow selection state change
	 */
	@Override public void setUISelected(boolean newSelected) throws PropertyVetoException {
		if (newSelected != selected) {
			checkTransient();
			boolean oldSelected = selected;
			vetoSupport.fireVetoableChange("selected", new Boolean(oldSelected), new Boolean(newSelected));
			this.selected = newSelected;
			propSupport.firePropertyChange("selected", new Boolean(oldSelected), new Boolean(newSelected));
		}
	}

	//========= Utility methods =================

	/**
	 * Changes the name of this value. The name should never be null.
	 *
	 * @throws PropertyVetoException if someone refused to allow the name to change
	 */
	@Override public void setUniqueName(String newName) throws PropertyVetoException {
		if (!newName.equals(name)) {
			checkTransient();
			String oldName = name;
			vetoSupport.fireVetoableChange("name", oldName, newName);
			this.name = newName;
			propSupport.firePropertyChange("name", oldName, newName);
		}
	}

	/**
	 * Changes the name of this object. The name should never be null.
	 * 
	 * @throws PropertyVetoException
	 *             if someone refused to allow the name to change
	 */
	@Override public void setUniqueName(String newName, Map checkAgainst) throws PropertyVetoException {
		String name = getUniqueName(checkAgainst);
		if (!newName.equals(name)) {
			checkTransient();
			String oldName = name;
			vetoSupport.fireVetoableChange("name", oldName, newName);
			this.name = newName;
			propSupport.firePropertyChange("name", oldName, newName);
		}
		String os = getShortLabel();
		if (os == null) {
			setShortLabel(newName);
		}
	}

	@Override public void setValue(Object obj) {
		try {
			setObject(obj);
		} catch (Throwable e) {
			throw Debuggable.reThrowable(e);
		}
	}

	// the jtree label uses this .. so supply someting good!
	@Override public String toString() {
		String sl = getShortLabel();
		if (sl != null) {
			return sl;
		}
		if (name != null) {
			return name;
		}
		return getUniqueName(null) + " -> " + getDebugName();

	}

	public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
		vetoSupport.fireVetoableChange(evt);

	}
}