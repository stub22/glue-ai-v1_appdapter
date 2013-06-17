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
		extends BoxImpl<TrigType> //

		implements java.io.Serializable, GetSetObject, Convertable, BT<TrigType>, GetDisplayContext, DisplayContextProvider, UIProvider {

	// A box may have up to one panel for any kind.
	protected Map<Object, JPanel> myPanelMap = new HashMap<Object, JPanel>();

	//===== Inner classes ==========================
	/**
	 * A rather ugly but workable default icon used in cases
	 * where there is no known icon for the value.
	 */
	static class UnknownIcon implements Icon, java.io.Serializable {
		public int getIconHeight() {
			return 16;
		}

		public int getIconWidth() {
			return 16;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(Color.blue);
			g.setFont(new Font("serif", Font.BOLD, 12));
			g.drawString("@", x, y + 12);
		}
	}

	public static BeanInfo getBeanInfo(Class beanClass) throws IntrospectionException {
		return Introspector.getBeanInfo(beanClass, Introspector.USE_ALL_BEANINFO);
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

	public static String getDefaultName(Object object) {
		Class type = object.getClass();
		if (type == Class.class)
			return ((Class) object).getName();
		else
			return "a " + Utility.getShortClassName(object.getClass());
	}

	//	public String m_title;

	//	public BoxPanelSwitchableView m_toplvl;

	/**
	 * Returns an Icon for this value, determined using BeanInfo.
	 * If no icon was found a default icon will be returned.
	 */
	static public Icon getIcon(BeanInfo info) {
		Icon icon;
		try {
			Image image;
			image = info.getIcon(BeanInfo.ICON_COLOR_16x16);
			if (image == null)
				image = info.getIcon(BeanInfo.ICON_MONO_16x16);

			if (image == null)
				icon = new UnknownIcon();
			else
				icon = new ImageIcon(image);
		} catch (Exception err) {
			icon = new UnknownIcon();
		}
		return icon;
	}

	//	public Component m_view;

	public String _uname;

	//public String registeredWithName;

	Class<TrigType> clz;

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

	public DisplayType m_displayType;

	//	public boolean m_is_added;
	public Object m_obj = null;//this;//;//new NoObject();
	//	public Container m_parent_component;

	public JPanel m_view;

	String name = null;

	// ==== Transient instance variables =============
	transient PropertyChangeSupport propSupport = new PropertyChangeSupport(this);

	boolean selected = false;

	//==== Serializable instance variables ===============
	Object value;

	protected transient VetoableChangeSupport vetoSupport = new VetoableChangeSupport(this);

	// ==== Constructors ==================================
	/**
	 * Creates a new Plain Old Java Object Box for the given object and assigns it a default
	 * name.
	 */
	public POJOBoxImpl() {
	}

	public POJOBoxImpl(NamedObjectCollection noc, String label) {
		this(noc, label, null);
	}

	public POJOBoxImpl(NamedObjectCollection noc, String title, Object boxOrObj) {
		setShortLabel(title);
		setObject(boxOrObj);
		addToNoc(noc, title);
	}

	/**
	 * Creates a new ScreenBox for the given object
	 * and assigns it a default name.
	 */
	public POJOBoxImpl(Object val) {
		this.value = val;
		this.clz = (Class<TrigType>) val.getClass();
		this.name = getDefaultName(val);
	}

	/**
	 * Creates a new ScreenBox for the given object, with the given name.
	 */
	public POJOBoxImpl(String title, Object val) {
		this.value = val;
		this.clz = (Class<TrigType>) value.getClass();
		this.name = title;
	}

	/*public BoxPanelSwitchableView getBoxPanelTabPane() {
		return getDisplayContextNoLoop();
	}*/

	// ==== Event listener registration =============

	/**
	 * PropertyChangeListeners will find out when the name or selection state
	 * changes.
	 */
	public void addPropertyChangeListener(PropertyChangeListener p) {
		checkTransient();
		propSupport.addPropertyChangeListener(p);
	}

	private void addToNoc(NamedObjectCollection noc, String title) {
		if (this instanceof POJOCollectionListener)
			noc.addListener((POJOCollectionListener) this);
		col2Name.put(noc, title);
		noc.addBoxed(title.toString(), this);
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

	// ===== Property getters and setters ========================

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
		if (b instanceof GetDisplayContext) {
			return ((GetDisplayContext) b).getDisplayContext();
		}
		Debuggable.notImplemented();
		return null;
	}

	public abstract JPanel findOrCreateBoxPanel(Object panelKind);

	/**
	 * Gets a BeanInfo object for this object, using the Introspector class
	 */
	public BeanInfo getBeanInfo() throws IntrospectionException {
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

	@Override public Component getComponent() {
		//if (m_view != null) 			return m_view;		
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

	@Override public DisplayContext getDisplayContext() {
		return m_displayContext;
	}

	@Override public Container getDisplayTarget(DisplayType attachType) {
		return getPropertiesPanel();
	}

	@Override public DisplayType getDisplayType() {
		return m_displayType;
	}

	/**
	 * Returns the object that this value wrapper represents
	 */
	public TrigType getObject() {
		return (TrigType) value;
	}

	// ========= Utility methods =================

	/** 
	 * This returns the decomposed Mixins
	 * @return
	 */
	public Object[] getObjects() {
		Object o = getValue();
		if (o != null && o != this)
			return CollectionSetUtils.arrayOf(o, this, getShortLabel(), getIdent());
		return CollectionSetUtils.arrayOf(this, getShortLabel(), getIdent());
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

	public Class<? extends Object> getObjectClass() {
		if (clz != null)
			return this.clz;
		Object obj = getValue();
		if (obj != null)
			return obj.getClass();
		return getClass();
	}

	final public JPanel getPropertiesPanel() {
		if (m_view instanceof JPanel)
			return (JPanel) m_view;
		JPanel pnl = getPropertiesPanel0();
		if (m_view == null) {
			m_view = pnl;
		}
		return pnl;
	}

	private JPanel getPropertiesPanel0() {
		if (m_view instanceof JPanel)
			return (JPanel) m_view;
		Object obj = getValue();
		if (obj instanceof JPanel) {
			return (JPanel) obj;
		}
		if (obj == this) {
			JPanel pnl = Utility.getPropertiesPanel(obj);
			pnl.setName(getShortLabel());
			return pnl;
		}
		JPanel pnl = Utility.getPropertiesPanel(obj);
		pnl.setName(getShortLabel());
		return pnl;
	}

	@Override public List<TrigType> getTriggers() {
		List<TrigType> tgs = super.getTriggers();
		for (Class cls : getTypes()) {
			TriggerForInstance.addClassLevelTriggers(m_displayContext, cls, tgs, this);
		}
		return tgs;
	}

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
	 * Returns the Class[]s that this object wrapper represents
	 */
	@Override public List<Class> getTypes() {
		java.util.HashSet al = new java.util.HashSet<Class>();
		Class pojoClass = getObjectClass();
		if (pojoClass != null)
			al.add(pojoClass);
		else {
			al.add(getClass());
		}
		return new ArrayList<Class>(al);
	}

	/**
	 * True if this value is selected
	 */
	public boolean getUISelected() {
		return selected;
	}

	/**
	 * Returns the name of this value
	 */
	public String getUniqueName() {
		return name;
	}

	/**
	 * Returns the name of this object
	 */
	final public String getUniqueName(Map checkAgainst) {
		//String _uname = null;
		if (_uname == null) {
			Ident ident = getIdent();
			if (ident != null) {
				_uname = ident.getAbsUriString();
			} else {
				Object object = getValue();
				if (object != null) {
					_uname = Utility.generateUniqueName(object, checkAgainst);
				} else {
					_uname = Utility.generateUniqueName(this, checkAgainst);
				}
			}
		}
		return _uname.toString();
	}

	@Override public Object getValue() {
		return value;
	}

	/**
	 * Returns the object that this object wrapper represents
	 */
	public Object getValueOrThis() {
		if (value != null)
			return value;
		if (m_obj == null) {
			//getLogger().warn("Default implementation of getObject() for NULL is returning 'this'", getShortLabel());
			return this;
		}
		if (m_obj != this)
			return m_obj;

		//	getLogger().warn("Default implementation of getObject() for {} is returning 'this'", getShortLabel());
		return this;
	}

	public boolean isNamed(String test) {
		if (Utility.stringsEqual(test, _uname))
			return true;
		if (Utility.stringsEqual(test, getShortLabel()))
			return true;
		return false;
	}

	//==== Constructors ==================================

	public boolean isTypeOf(Class type) {
		for (Class c : getTypes()) {
			if (type.isAssignableFrom(c))
				return true;
		}
		return type.isInstance(getValue());
	}

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
	public JPanel makeBoxPanelForCustomizer(Object customizer) {
		JPanel pnl = myPanelMap.get(toKey(customizer));
		if (pnl != null)
			return pnl;

		Object val = getValueOrThis();

		if (customizer instanceof Customizer) {
			Customizer cust = ((Customizer) customizer);
			cust.setObject(val);
			if (this instanceof PropertyChangeListener) {
				cust.addPropertyChangeListener(((PropertyChangeListener) this));
			}
		}

		Class objClass = getObjectClass();

		if (customizer instanceof Class) {
			Class cust = (Class) customizer;
			if (Customizer.class == customizer) {
				cust = Utility.findCustomizerClass(objClass);
			}
			if (PromiscuousClassUtils.isCreateable(cust)) {
				try {
					return ComponentHost.asPanel(findOrCreateBoxPanel(Utility.newInstance(cust)), val);
				} catch (InstantiationException e) {
				} catch (IllegalAccessException e) {
				}
			}

		}
		Class clazz = Utility.getCustomizerClassForClass(objClass);
		if (PropertyEditor.class == customizer) {
			customizer = Utility.findEditor(objClass);
		}
		if (customizer instanceof PropertyEditor) {
			PropertyEditor editor = (PropertyEditor) customizer;
			customizer = new UseEditor(editor, objClass, (GetSetObject) this);
		}

		if (customizer instanceof Component) {
			customizer = pnl = ComponentHost.asPanel((Component) customizer, val);
			((ComponentHost) pnl).focusOnBox(this);
			return pnl;

		}
		return null;
	}

	//===== Property getters and setters ========================

	@Override public void propertyChange(PropertyChangeEvent evt) {
		Debuggable.notImplemented();

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
	public void removeVetoableChangeListener(VetoableChangeListener v) {
		checkTransient();
		vetoSupport.removeVetoableChangeListener(v);
	}

	public boolean representsObject(Object test) {
		Object myObj = getValue();
		if (this == test)
			return true;
		if (this == test)
			return true;
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
		if (this.value == obj)
			return;
		try {
			valueChanged(this.value, obj);
		} catch (PropertyVetoException e) {
			throw Debuggable.reThrowable(e);
		}
	}

	public void setSelectedComponent(Object object) throws PropertyVetoException {
		Debuggable.notImplemented();
		Debuggable.notImplemented();

	}

	/**
	 * Changes the selection state.
	 *
	 * @throws PropertyVetoException if someone refused to allow selection state change
	 */
	public void setUISelected(boolean newSelected) throws PropertyVetoException {
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
	public void setUniqueName(String newName) throws PropertyVetoException {
		if (!(newName.equals(name))) {
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
	public void setUniqueName(String newName, Map checkAgainst) throws PropertyVetoException {
		String name = getUniqueName(checkAgainst);
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

	public void setValue(Object obj) {
		try {
			setObject(obj);
		} catch (Throwable e) {
			throw Debuggable.reThrowable(e);
		}
	}

	protected Object toKey(Object kind) {
		if (kind == null)
			return null;
		if (kind instanceof Enum)
			return kind;
		if (kind instanceof Class) {
			Class cind = (Class) kind;
			if (cind.isArray()) {
				kind = Iterable.class;
			} else if (Iterable.class.isAssignableFrom(cind)) {
				kind = Iterable.class;
			}
			return kind;
		}
		return toKey(kind.getClass());
	}

	// the jtree label uses this .. so supply someting good!
	@Override public String toString() {
		String sl = getShortLabel();
		if (sl != null)
			return sl;
		return getUniqueName(null) + " -> " + getDebugName();

	}

	public void valueChanged(Object oldObject, Object newObject) throws PropertyVetoException {
		checkTransient();
		String oldName = name;
		vetoSupport.fireVetoableChange("value", oldObject, newObject);
		this.value = newObject;
		propSupport.firePropertyChange("value", oldObject, newObject);
	}
}