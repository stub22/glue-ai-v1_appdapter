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

import java.awt.Component;
import java.beans.Customizer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.appdapter.api.trigger.ABoxImpl;
import org.appdapter.api.trigger.AnyOper.UIHidden;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxContext;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.api.trigger.UserResult;
import org.appdapter.core.component.MutableKnownComponent;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.Convertable;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.DisplayContextProvider;
import org.appdapter.gui.api.GetDisplayContext;
import org.appdapter.gui.api.GetSetObject;
import org.appdapter.gui.api.ScreenBox;
import org.appdapter.gui.api.WrapperValue;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.editors.UseEditor;
import org.appdapter.gui.repo.DatabaseManagerPanel;
import org.appdapter.gui.repo.ModelMatrixPanel;
import org.appdapter.gui.repo.RepoManagerPanel;
import org.appdapter.gui.swing.ComponentHost;
import org.appdapter.gui.trigger.TriggerMenuFactory;
import org.appdapter.gui.util.PromiscuousClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class ScreenBoxImpl<TrigType extends Trigger<? extends ScreenBoxImpl<TrigType>>> extends ABoxImpl<TrigType>

implements ScreenBox<TrigType>, GetSetObject, UserResult, Convertable, DisplayContextProvider {

	public Object valueSetAs = null;

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

	protected boolean madeElsewhere;
	// A box may have up to one panel for any kind.
	protected Map<Object, JPanel> myPanelMap = new HashMap<Object, JPanel>();

	public Box asBox() {
		return this;
	}

	public static List<ScreenBox> boxctxGetOpenChildBoxesNarrowed(BoxContext oh, Object parent, Class boxClass, Class trigClass) {
		return oh.getOpenChildBoxesNarrowed((Box) parent, boxClass, trigClass);
	}

	public static void doAttachTrigger(Object box, Object bt) {
		((MutableBox) box).attachTrigger((Trigger) bt);
	}

	public static void doSetShortLabel(Object box, Object nym) {
		((MutableKnownComponent) box).setShortLabel((String) nym);
	}

	static Logger theLogger = LoggerFactory.getLogger(ScreenBoxImpl.class);
	// Because it's a "provider", we have an extra layer of indirection between
	// box and display, enabling independence.
	private DisplayContextProvider myDCP;

	public ScreenBoxImpl() {
		madeElsewhere = true;
		Utility.recordCreated(this);
	}

	public ScreenBoxImpl(boolean isSelfTheValue) {
		this.madeElsewhere = isSelfTheValue;
		Utility.recordCreated(this);
	}

	/*
		public ScreenBoxImpl(NamedObjectCollection noc, String label, Object obj) {
			super(noc, label, obj);
		}

		public ScreenBoxImpl(String label, Object obj) {
			this(Utility.getTreeBoxCollection(), label, obj);
		}
	*/
	/*
		public ScreenBoxImpl(NamedObjectCollection noc, String title, Object boxOrObj, Component vis, DisplayType displayType, Container parent, BoxPanelSwitchableView bpsv) {
			super(noc, title, boxOrObj, vis, displayType, parent, bpsv);
		}
	*/
	/**
	 * The box panel returned might be one that we "made" earlier, 
	 * or it might be one that someone "put" onto me.
	 * @param kind
	 * @return 
	 */
	@Override public JPanel findOrCreateBoxPanel(Object kind) {
		JPanel bp = findExistingBoxPanel(kind);
		if (bp == null) {
			bp = makeBoxPanelCustomized(kind);
		}
		return bp;
	}

	public void dump() {
		theLogger.info("DUMP-DUMP-DE-DUMP");
	}

	public JPanel findExistingBoxPanel(Kind kind) {
		return myPanelMap.get(toKey(kind));
	}

	public JPanel findExistingBoxPanel(Object kind) {
		return myPanelMap.get(toKey(kind));
	}

	@Override public DisplayContext getDisplayContext() {
		if (myDCP != null) {
			DisplayContext dc = myDCP.findDisplayContext(this);
			if (dc != null)
				return dc;
		}
		return Utility.browserPanel.getDisplayContext();
	}

	@Override public <T> boolean canConvert(Class<T> c) {
		try {
			for (Object o : getObjects()) {
				if (o == null) {
					continue;
				}
				if (!c.isInstance(o)) {
					continue;
				}
				try {
					final T madeIT = (T) o;
					if (madeIT != null) {
						return true;
					}
				} catch (Exception e) {
					getLogger().error("JVM Issue (canConvert)", e);
				}
				return true;
			}
		} catch (Throwable t) {
			getLogger().error("JVM Issue (canConvert)", t);
			return false;
		}
		return false;
	}

	@Override public <T> T convertTo(Class<T> c) throws ClassCastException {
		for (Object o : getObjects()) {
			if (o == null) {
				continue;
			}
			if (!c.isInstance(o)) {
				continue;
			}
			try {
				return c.cast(o);
			} catch (Exception e) {
				getLogger().error("JVM Issue (canConvert)", e);
				return (T) o;
			}
		}
		throw new ClassCastException("Cannot convert " + this + " to " + c);
	}

	public WrapperValue getWrapperValue() {
		if (this instanceof WrapperValue) {
			return (WrapperValue) this;
		}
		return new WrapperValue() {

			@Override public Class getObjectClass() {
				return ScreenBoxImpl.this.getObjectClass();
			}

			@Override public void reallySetValue(Object newObject) throws UnsupportedOperationException {
				if (newObject == ScreenBoxImpl.this)
					return;
				if (newObject == ScreenBoxImpl.this.getValueOrThis())
					return;
				throw new UnsupportedOperationException("value");
			}

			@Override public Object reallyGetValue() {
				return ScreenBoxImpl.this.getValueOrThis();
			}

		};

	}

	/** 
	 * This returns the decomposed Mixins
	 * @return
	 */
	public Iterable<Object> getObjects() {
		Object o = getValue();
		if (o != null && o != this) {
			return Arrays.asList(new Object[] { o, this, getIdent(), getShortLabel() });
		}
		return Arrays.asList(new Object[] { this, getIdent(), getShortLabel(), });
	}

	@Override public <T, E extends T> Iterable<E> getObjects(Class<T> type) {
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

	public Object getValueOrThis() {
		return Utility.first(getValue(), ScreenBoxImpl.this);
	}

	public Class getObjectClass() {
		return getValueOrThis().getClass();
	}

	@Override public void setObject(Object newObject) throws InvocationTargetException {
		if (getClass() == ScreenBoxImpl.class) {
			valueSetAs = newObject;
			return;
		}
		getWrapperValue().reallySetValue(newObject);
	}

	@Override public Object getValue() {
		if (getClass() == ScreenBoxImpl.class) {
			if (valueSetAs != null)
				return valueSetAs;
			return valueSetAs;
		}
		WrapperValue wv = getWrapperValue();
		if (wv != null && wv != this)
			return wv.reallyGetValue();
		return this;
	}

	@Override public DisplayContext findDisplayContext(Box b) {
		if (b instanceof GetDisplayContext) {
			return ((GetDisplayContext) b).getDisplayContext();
		}
		Debuggable.notImplemented();
		return null;
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
			customizer = new UseEditor(editor, objClass, this);
		}

		if (customizer instanceof Component) {
			customizer = pnl = ComponentHost.asPanel((Component) customizer, val);
			((ComponentHost) pnl).focusOnBox(this);
			return pnl;

		}
		return null;
	}

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

	@Override final public JPanel getPropertiesPanel() {
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

	@Override public List<TrigType> getTriggers() {
		List<TrigType> tgs = super.getTriggers();
		for (Class cls : getTypes()) {
			TriggerMenuFactory.addClassLevelTriggers(getDisplayContext(), cls, tgs, this.getWrapperValue());
		}
		return tgs;
	}

	protected void putBoxPanel(Object kind, JPanel bp) {
		JPanel oldBP = findExistingBoxPanel(kind);
		if (oldBP != null) {
			//theLogger.warn("Replacing old ScreenBoxPanel link for " + getShortLabel() + " to {} with {} ", oldBP, bp);
		}
		myPanelMap.put(toKey(kind), bp);
	}

	public void setDisplayContextProvider(DisplayContextProvider dcp) {
		myDCP = dcp;
	}
}
