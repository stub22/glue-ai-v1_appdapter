package org.appdapter.gui.box;

/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.org).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.appdapter.api.trigger.ABoxImpl;
import org.appdapter.api.trigger.AnyOper.UIHidden;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxContext;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.core.component.MutableKnownComponent;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.name.Ident;
import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.DisplayContextProvider;
import org.appdapter.gui.api.DisplayType;
import org.appdapter.gui.api.FocusOnBox;
import org.appdapter.gui.api.GetDisplayContext;
import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.api.POJOCollectionListener;
import org.appdapter.gui.api.ScreenBox;
import org.appdapter.gui.api.SetObject;
import org.appdapter.gui.api.WrapperValue;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.editors.UseEditor;
import org.appdapter.gui.repo.DatabaseManagerPanel;
import org.appdapter.gui.repo.ModelMatrixPanel;
import org.appdapter.gui.repo.RepoManagerPanel;
import org.appdapter.gui.swing.ComponentHost;
import org.appdapter.gui.trigger.TriggerMenuFactory;
import org.appdapter.gui.util.PromiscuousClassUtilsA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.appdapter.core.convert.ReflectUtils.*;
/**
/**  Base implementation of our demo Swing Panel boxes. 
 * The default implementation can own one swing panel of each "Kind".
 * This owner does not actually create any kind of GUI resource until it is asked to
 * findBoxPanel(kind).  A strongheaded purpose-specific box might ignore "Kind",
 * and always return whatever panel it thinks is "best".  
 * <br/> 
 * @author Stu B. <www.texpedient.com>
 */

@UIHidden
public class ScreenBoxImpl<TrigType extends Trigger<? extends ScreenBoxImpl<TrigType>>> //
		extends ABoxImpl<TrigType> //
		implements BT<TrigType> {
	public class SBWrapperValue implements WrapperValue {

		public Class getObjectClass() {
			return ScreenBoxImpl.this.getObjectClass();
		}

		public Object reallyGetValue() {
			return ScreenBoxImpl.this.getValueOrThis();
		}

		public void reallySetValue(Object newObject) throws UnsupportedOperationException {
			if (newObject == ScreenBoxImpl.this)
				return;
			if (newObject == ScreenBoxImpl.this.getValueOrThis())
				return;
			throw new UnsupportedOperationException("value");
		}

	}

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

	static Logger theLogger = LoggerFactory.getLogger(ScreenBoxImpl.class);

	public static List<ScreenBox> boxctxGetOpenChildBoxesNarrowed(BoxContext oh, Object parent, Class boxClass, Class trigClass) {
		return oh.getOpenChildBoxesNarrowed((Box) parent, boxClass, trigClass);
	}

	public static void doAttachTrigger(Object box, Object bt) {
		((MutableBox) box).attachTrigger((Trigger) bt);
	}

	/*
	public POJOBoxImpl(NamedObjectCollection noc, String label) {
	this(noc, label, null);
	}*/

	public static void doSetShortLabel(Object box, Object nym) {
		((MutableKnownComponent) box).setShortLabel((String) nym);
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

	public Class<?> clz;

	///public String name = null;
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

	protected boolean madeElsewhere;

	// Because it's a "provider", we have an extra layer of indirection between
	// box and display, enabling independence.
	private DisplayContextProvider myDCP;

	// A box may have up to one panel for any kind.
	protected Map<Object, JPanel> myPanelMap = new HashMap<Object, JPanel>();

	//public Object value;
	private NamedObjectCollection noc;

	// ==== Transient instance variables =============
	transient PropertyChangeSupport propSupport = new PropertyChangeSupport(this);

	SBWrapperValue sbWrapperValue;

	boolean selected = false;

	public Object valueSetAs = null;

	//==== Serializable instance variables ===============
	//public Object value = null;//this;//;//new NoObject();
	protected transient VetoableChangeSupport vetoSupport = new VetoableChangeSupport(this);

	//==== Constructors ==================================

	/**
	 * Creates a new Box
	 * 
	 */
	public ScreenBoxImpl() {
		madeElsewhere = true;
		Utility.recordCreated(this);
	}

	public ScreenBoxImpl(boolean isSelfTheValue) {
		this.madeElsewhere = isSelfTheValue;
		Utility.recordCreated(this);
	}

	/**
	 * Creates a new ScreenBox for the given value
	 * and assigns it a default name.
	 */
	public ScreenBoxImpl(NamedObjectCollection noc, String title, Object value) {
		this.noc = noc;
		setNameValue(title, value);
	}

	/**
	 * PropertyChangeListeners will find out when the name or selection state
	 * changes.
	 */
	public void addPropertyChangeListener(PropertyChangeListener p) {
		checkTransient();
		propSupport.addPropertyChangeListener(p);
	}

	protected void addToNoc(NamedObjectCollection noc, String title) {
		if (this instanceof POJOCollectionListener) {
			noc.addListener((POJOCollectionListener) this, true);
		}
		col2Name.put(noc, title);
		noc.addBoxed(title.toString(), (BT) this);
	}

	/**
	 * VetoableChangeListeners will find out when the name or selection state is
	 * about to change, and can prevent such changes if desired.
	 */
	public void addVetoableChangeListener(VetoableChangeListener v) {
		checkTransient();
		vetoSupport.addVetoableChangeListener(v);
	}

	public Box asBox() {
		return this;
	}

	@Override public <T> boolean canConvert(Class<T> c) {
		return ReflectUtils.canConvert(c, getObjects());
	}

	@Override public <T> T convertTo(Class<T> c) {
		return ReflectUtils.convertTo(c, getObjects());
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

	public void dump() {
		theLogger.info("DUMP-DUMP-DE-DUMP");
	}

	public DisplayContext findDisplayContext(Box b) {
		if (b instanceof GetDisplayContext) {
			return ((GetDisplayContext) b).getDisplayContext();
		}
		Debuggable.notImplemented();
		return null;
	}

	public JPanel findExistingBoxPanel(Kind kind) {
		return myPanelMap.get(toKey(kind));
	}

	public JPanel findExistingBoxPanel(Object kind) {
		return myPanelMap.get(toKey(kind));
	}

	/**
	 * The box panel returned might be one that we "made" earlier, 
	 * or it might be one that someone "put" onto me.
	 * @param kind
	 * @return 
	 */
	public JPanel findOrCreateBoxPanel(Object kind) {
		JPanel bp = findExistingBoxPanel(kind);
		if (bp == null) {
			bp = makeBoxPanelCustomized(kind);
		}
		return bp;
	}

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

	public BT getBox() {
		return (BT) this;
	}

	public Component getComponent() {
		//if (m_view != null) 			return m_view;		
		for (Object o : getObjects()) {
			if (Component.class.isInstance(o)) {
				return (Component) o;
			}
		}
		return findOrCreateBoxPanel(getDisplayType());
	}

	public Component getComponent(DisplayType attachType) {
		return getDisplayTarget(getDisplayType());
	}

	public String getDebugName() {
		try {
			Object o = getValue();
			if (o == null) {
				return getShortLabel();
			}
			return o.toString();
		} catch (Exception e) {
			return super.toString();
		}
	}

	public DisplayContext getDisplayContext() {
		DisplayContext dc = m_displayContext;
		if (dc == null) {
			if (myDCP != null) {
				dc = myDCP.findDisplayContext(this);
			}
			if (dc != null)
				return dc;
		}
		return Utility.browserPanel.getDisplayContext();
	}

	public Container getDisplayTarget(DisplayType attachType) {
		return findOrCreateBoxPanel(attachType);
	}

	public DisplayType getDisplayType() {
		return m_displayType;
	}

	public NamedObjectCollection getNoc() {
		return noc;
	}

	public Class<? extends Object> getObjectClass() {
		if (clz != null) {
			return this.clz;
		}
		Object obj = getValueOrThis();
		if (obj != null) {
			return obj.getClass();
		}
		return Object.class;
	}

	/** 
	 * This returns the decomposed Mixins
	 * @return
	 */

	public Iterable<Object> getObjects() {
		if (objects.size() == 0) {
			addIfNew(objects, valueSetAs);
			addIfNew(objects, getValue());
			addIfNew(objects, this);
			addIfNew(objects, getIdent());
			addIfNew(objects, getShortLabel());
		}
		return objects;
	}

	List<Object> objects = new ArrayList<Object>();

	static <T> boolean addIfNew(List<T> objects2, T valueSetAs2) {
		if (valueSetAs2 != null) {
			if (!objects2.contains(valueSetAs2)) {
				objects2.add(valueSetAs2);
				return true;
			}
		}
		return false;
	}

	static <T> boolean removeIfOld(List<T> objects2, T valueSetAs2) {
		if (valueSetAs2 != null) {
			if (objects2.contains(valueSetAs2)) {
				objects2.remove(valueSetAs2);
				return true;
			}
		}
		return false;
	}

	public <T, E extends T> Iterable<E> getObjects(Class<T> type) {
		HashSet<E> objs = new HashSet<E>();
		if (this.canConvert(type)) {
			T one = convertTo(type);
			objs.add((E) one);
		}
		for (Object o : getObjects()) {
			if (type.isInstance(o)) {
				objs.add((E) o);
			}
		}
		return objs;
	}

	final public JPanel getPropertiesPanel() {
		Object m_largeview = myPanelMap.get(Kind.OBJECT_PROPERTIES);
		if (m_largeview instanceof JPanel) {
			return (JPanel) m_largeview;
		}
		JPanel pnl = makePropertiesPanel();
		if (m_largeview == null) {
			m_largeview = pnl;
		}
		return pnl;
	}

	public List<TrigType> getTriggers() {
		List<TrigType> tgs = super.getTriggers();
		DisplayContext dc = getDisplayContext();
		for (Class cls : getTypes()) {
			Boolean was = Utility.canMakeInstanceTriggers.get();
			try {
				Utility.canMakeInstanceTriggers.set(true);
				TriggerMenuFactory.addTriggersForInstance(dc, cls, tgs, this);
			} finally {
				Utility.canMakeInstanceTriggers.set(was);
			}
		}
		return tgs;
	}

	/**
	 * Returns the Class[]s that this object wrapper represents
	 */
	public Iterable<Class> getTypes() {
		java.util.HashSet al = new java.util.HashSet<Class>();
		Class pojoClass = getObjectClass();
		if (pojoClass != null) {
			al.add(pojoClass);
		} else {
			al.add(getClass());
		}
		return al;
	}

	//abstract public <T> T convertTo(Class<T> c) throws ClassCastException;

	/**
	 * True if this value is selected
	 */
	public boolean getUISelected() {
		return selected;
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

		 public Class<? extends Annotation> annotationType() {
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

	/**
	 * Returns the name of this value
	 */
	public String getKey() {
		String sl = super_getShortLabel();
		if (sl != null)
			return sl;
		if (noc != null) {
			return getUniqueName(noc.getNameToBoxIndex());
		}
		return getUniqueName(null);
	}

	/**
	 * Returns the name of this object
	 */
	final public String getUniqueName(Map checkAgainst) {
		//String _uname = null;
		String name = super_getShortLabel();
		if (name == null) {
			Ident ident = getIdent();
			if (ident != null) {
				name = ident.getLocalName();
			} else {
				Object object = getValue();
				if (object != null) {
					name = Utility.generateUniqueName(object, checkAgainst);
				} else {
					name = Utility.generateUniqueName(this, checkAgainst);
				}
			}
		}
		setShortLabel(name);
		return name.toString();
	}

	private String super_getShortLabel() {
		return super.getShortLabel();
	}

	public Object getValue() {
		WrapperValue wv = getWrapperValue();
		if (wv != null && wv != this && sbWrapperValue != wv) {
			return wv.reallyGetValue();
		}
		if (valueSetAs != null)
			return valueSetAs;
		return this;
	}

	/**
	 * Returns the object that this object wrapper represents
	 */
	public Object getValueOrThis() {
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

	public WrapperValue getWrapperValue() {
		if (this instanceof WrapperValue) {
			return (WrapperValue) this;
		}
		if (sbWrapperValue == null) {
			sbWrapperValue = new SBWrapperValue();
		}
		return sbWrapperValue;
	}

	public boolean isNamed(String test) {
		for (String name : getNames()) {
			if (Utility.stringsEqual(test, name)) {
				return true;
			}
		}
		return false;
	}

	/*public BoxPanelSwitchableView getBoxPanelTabPane() {
		return getDisplayContextNoLoop();
	}*/

	// ==== Event listener registration =============
	List<String> names;

	Collection<String> getNames() {
		if (names == null) {
			names = new ArrayList<String>();
			names.add(getShortLabel());
			names.add(super_getShortLabel());
		}
		return names;
	}

	public boolean isTypeOf(Class type) {
		for (Class c : getTypes()) {
			if (type.isAssignableFrom(c)) {
				return true;
			}
		}
		return type.isInstance(getValue());
	}

	/**
	 * This whole "kind" thing is a ruse allowing us to make some hardwired basic panel types
	 * without the conceptual bloat of yet another registry of named things.  The real generality
	 * named things. The real generality comes when you override this
	 * comes when you override this ScreenBoxImpl class, and provide your own OTHER kind of panel.
	 * When these mechanisms mature, we will expand to a proper GUI component type registry.
	 * @param kind
	 * @return 
	 */
	protected JPanel makeBoxPanel(Kind kind) {
		JPanel bp = makeBoxPanelForKind(kind);
		if (bp != null) {
			// Subclasses might do something fancier to share panels among
			// instances.
			putBoxPanel(kind, bp);
		}
		return bp;
	}

	protected JPanel makeBoxPanelCustomized(Object customizer) {
		JPanel bp = makeBoxPanelForCustomizer(customizer);
		if (bp == null) {
			bp = makeOtherPanel();
		}
		if (bp != null) {
			// Subclasses might do something fancier to share panels among
			// instances.
			putBoxPanel(customizer, bp);
		}
		return bp;
	}

	public JPanel makeBoxPanelForCustomizer(Object customizer) {
		if (customizer instanceof Kind) {
			JPanel sbp = makeBoxPanel((Kind) customizer);
			if (sbp != null)
				return sbp;
		}
		return makeBoxPanelForCustomizer2(customizer);
	}

	public JPanel makeBoxPanelForCustomizer2(Object customizer) {
		JPanel pnl = myPanelMap.get(toKey(customizer));
		if (pnl != null) {
			return pnl;
		}

		Object val = getValueOrThis();

		if (customizer instanceof Customizer) {
			Customizer cust = (Customizer) customizer;
			cust.setObject(val);
			if (this instanceof PropertyChangeListener) {
				cust.addPropertyChangeListener((PropertyChangeListener) this);
			}
		}

		Class objClass = val.getClass();

		if (customizer instanceof Class) {
			Class cust = (Class) customizer;
			if (Customizer.class == customizer) {
				cust = Utility.findCustomizerClass(objClass);
			}
			if (PromiscuousClassUtilsA.isCreateable(cust)) {
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
			customizer = new UseEditor(editor, objClass, this);
		}

		if (customizer instanceof Component) {
			customizer = pnl = ComponentHost.asPanel((Component) customizer, val);
			((ComponentHost) pnl).focusOnBox(this);
			return pnl;

		}
		return null;
	}

	// ===== Property getters and setters ========================

	protected JPanel makeBoxPanelForKind(Kind kind) {
		if (kind == Kind.MATRIX)
			return new ModelMatrixPanel();
		if (kind == Kind.REPO_MANAGER)
			return new RepoManagerPanel();
		if (kind == Kind.DB_MANAGER)
			return new DatabaseManagerPanel();
		if (kind == Kind.OBJECT_PROPERTIES)
			return Utility.getPropertiesPanel(this);
		if (kind == Kind.OTHER)
			return makeOtherPanel();
		throw new RuntimeException("Found unexpected ScreenBoxPanelKind: " + kind);
	}

	/**
	/** Override this to create an app-specific ScreenBoxPanel kind, and configure
	 * your app to request a panel of kind "OTHER", using BrowseTabFuncs.openBoxPanelAndFocus,
	 * BrowseTabFuncs.openBoxPanelAndFocus, PanelTriggers.OpenTrigger, or your
	 * PanelTriggers.OpenTrigger, or your own mechanism.  Note that your ScreenBoxPanel
	 * may be able to display any number of boxes, by responding to the focusOnBox method.
	 * If those boxes are screen boxes, you may want to tell them to 
	 * putBoxPanel() the one currently displaying them, in case they are later asked
	 * to findBoxPanel themselves.
	 * 
	 * @return
	 */
	protected JPanel makeOtherPanel() {
		//theLogger.warn("Default implementation of makeOtherPanel() for {} is returning null", getShortLabel());
		return getPropertiesPanel();// Utility.getPropertiesPanel(this);
	}

	protected JPanel makePropertiesPanel() {
		Object m_largeview = myPanelMap.get(Kind.OBJECT_PROPERTIES);
		if (m_largeview instanceof JPanel) {
			return (JPanel) m_largeview;
		}
		Object obj = getValue();
		if (obj instanceof JPanel) {
			return (JPanel) obj;
		}
		if (obj == null) {
			obj = this;
		}
		JPanel pnl = Utility.getPropertiesPanel(obj);
		if (pnl == null) {
			Utility.gpp.remove(obj);
			pnl = Utility.getPropertiesPanel(obj);
		}
		pnl.setName(getShortLabel());
		return pnl;
	}

	public void propertyChange(PropertyChangeEvent evt) {
		propSupport.firePropertyChange(evt);
	}

	protected void putBoxPanel(Object kind, JPanel bp) {
		JPanel oldBP = findExistingBoxPanel(kind);
		if (oldBP != null) {
			theLogger.warn("Replacing old ScreenBoxPanel link for " + getShortLabel() + " to {} with {} ", oldBP, bp);
		}
		setPanelBox(bp);
		myPanelMap.put(toKey(kind), bp);
	}

	private void setPanelBox(JPanel bp) {
		boolean needSet = true;
		if (needSet && bp instanceof FocusOnBox) {
			try {
				((FocusOnBox) bp).focusOnBox(this);
				needSet = false;
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		if (needSet && bp instanceof SetObject) {
			try {
				((SetObject) bp).setObject(this);
				needSet = false;
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	public Object reallyGetValue() {
		return valueSetAs;
	}

	public void reallySetValue(Object newObject) {
		if (removeIfOld(objects, valueSetAs)) {
			noc.removeObject(valueSetAs);
		}
		valueSetAs = newObject;
		addValue(valueSetAs);
	}

	/**
	 * PropertyChangeListeners will find out when the name or selection state
	 * changes.
	 */
	public void removePropertyChangeListener(PropertyChangeListener p) {
		checkTransient();
		propSupport.removePropertyChangeListener(p);
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
	 * VetoableChangeListeners will find out when the name or selection state is
	 * about to change, and can prevent such changes if desired.
	 */
	public void removeVetoableChangeListener(VetoableChangeListener v) {
		checkTransient();
		vetoSupport.removeVetoableChangeListener(v);
	}

	public boolean representsObject(Object test) {
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
		return getKey() == test;
	}

	public void setDisplayContextProvider(DisplayContextProvider dcp) {
		myDCP = dcp;
	}

	public void setNameValue(String uniqueName, Object value) {

		if (uniqueName == null) {
			uniqueName = Utility.generateUniqueName(value, uniqueName, noc.getNameToBoxIndex());
		}
		setShortLabel(uniqueName);
		if (value == null) {
			value = new NullPointerException(uniqueName).fillInStackTrace();
		}
		if (clz == null)
			clz = value.getClass();

		try {
			setObject(value);
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setObject(Object value) throws InvocationTargetException {
		String ds = getDescription();
		if (ds == null) {
			setDescription("" + value + " " + value.getClass());
		}
		Object oldObject = getValue();
		if (oldObject == value) {
			return;
		}
		if (value == null) {
			value = new NullPointerException(getShortLabel()).fillInStackTrace();
		}
		if (clz == null)
			clz = value.getClass();

		reallySetValue(value);
		try {
			valueChanged(oldObject, value);
			getWrapperValue().reallySetValue(value);
		} catch (PropertyVetoException e) {
			throw Debuggable.reThrowable(e);
		}

	}

	//===== Property getters and setters ========================

	public void setSelectedComponent(Object object) throws PropertyVetoException {

	}

	@Override public String getShortLabel() {
		return super_getShortLabel();
	}

	public void setShortLabel(String shortLabel) {
		super.setShortLabel(shortLabel);
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

	/**
	 * Changes the name of this value. The name should never be null.
	 *
	 * @throws PropertyVetoException if someone refused to allow the name to change
	 */
	public void setUniqueName(String newName) throws PropertyVetoException {
		setUniqueName(newName, Utility.uiObjects.getNameToBoxIndex());
	}

	//========= Utility methods =================

	/**
	 * Changes the name of this object. The name should never be null.
	 * 
	 * @throws PropertyVetoException
	 *             if someone refused to allow the name to change
	 */
	public void setUniqueName(String newName, Map checkAgainst) throws PropertyVetoException {
		final String name = getUniqueName(checkAgainst);
		if (!newName.equals(name)) {
			checkTransient();
			String oldName = name;
			vetoSupport.fireVetoableChange("name", oldName, newName);
			setShortLabel(newName);
			propSupport.firePropertyChange("name", oldName, newName);
		}
	}

	public Object setValue(Object obj) {
		try {
			setObject(obj);
		} catch (Throwable e) {
			throw Debuggable.reThrowable(e);
		}
		return obj;
	}

	protected Object toKey(Object kind) {
		if (kind == null) {
			return null;
		}
		if (kind instanceof Enum) {
			return kind;
		}
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
	public String toString() {
		String sl = getShortLabel();
		if (sl != null) {
			return sl;
		}
		String name = getKey();
		if (name != null) {
			return name;
		}
		return getUniqueName(null) + " -> " + getDebugName();

	}

	public void valueChanged(Object oldObject, Object newObject) throws PropertyVetoException {
		checkTransient();
		String oldName = getShortLabel();
		vetoSupport.fireVetoableChange("value", oldObject, newObject);
		this.reallySetValue(newObject);
		propSupport.firePropertyChange("value", oldObject, newObject);
	}

	public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
		vetoSupport.fireVetoableChange(evt);

	}

	@Override public void addValue(Object val) {
		BT prev = noc.findBoxByObject(val);
		if (prev != null && prev != this) {
			Debuggable.notImplemented("Already existing value: " + prev);
		}

		if (addIfNew(objects, val))
			noc.addValueBoxed(val, this);

	}

	@Override public void addTitle(String nym) {
		if (noc.addBoxed(nym, this))
			getNames().add(nym);
	}
}