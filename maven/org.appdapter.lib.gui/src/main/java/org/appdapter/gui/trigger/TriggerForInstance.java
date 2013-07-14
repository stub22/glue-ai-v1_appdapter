package org.appdapter.gui.trigger;

import java.awt.event.ActionEvent;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.FeatureDescriptor;
import java.beans.MethodDescriptor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyVetoException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.appdapter.api.trigger.AnyOper.UIHidden;
import org.appdapter.api.trigger.AnyOper.UISalient;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxContext;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.api.trigger.TriggerImpl;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.log.Loggable;
import org.appdapter.core.name.Ident;
import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.BoxPanelSwitchableView;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.DisplayType;
import org.appdapter.gui.api.Ontologized.AskIfEqual;
import org.appdapter.gui.api.UIAware;
import org.appdapter.gui.api.WrapperValue;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.util.CollectionSetUtils;

abstract public class TriggerForInstance<B extends Box<TriggerImpl<B>>> extends TriggerImpl<B> implements AskIfEqual, UIAware, Action {

	static int ADD_ALL = 255;
	static int ADD_FROM_PASTE_SRC_ONLY = 255;
	static int ADD_FROM_PASTE_TARG_ONLY = 255;

	static Class[] CLASS0 = new Class[0];

	public static <TrigType> void addClasses(DisplayContext ctx, Class cls, HashSet<Class> classesVisited) {
		if (cls == null)
			return;
		if (classesVisited.contains(cls))
			return;
		classesVisited.add(cls);
		addClasses(ctx, cls, classesVisited);
		for (Class cls2 : cls.getInterfaces()) {
			addClasses(ctx, cls2, classesVisited);
		}
		addClasses(ctx, cls.getSuperclass(), classesVisited);
	}

	public static <TrigType> void addPanelClasses(DisplayContext ctx, Class cls, List<TrigType> tgs, WrapperValue poj) {
		for (Class pnlClz : Utility.findPanelClasses(cls)) {
			if (pnlClz == null)
				continue;
			CollectionSetUtils.addIfNew(tgs, ((TrigType) new ShowPanelTrigger(ctx, cls, poj, pnlClz)));
		}
	}

	DisplayContext getDisplayContext() {
		if (displayContext != null)
			return displayContext;
		return Utility.getCurrentContext();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		return same(obj);
	}

	public String getMenuPath() {
		String s = getMenuName();
		return s;
	}

	public Object getValueOr(Box targetBox) {
		if (_object != null) {
			return Utility.dref(_object, true);
		}
		return Utility.dref(targetBox);
	}

	@Override
	public boolean same(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		return toString().equals(obj.toString());
	}

	public static <TrigType> void addClassLevelTriggers(DisplayContext ctx, Class cls, List<TrigType> tgs, WrapperValue poj) {
		HashSet<Class> skippedTriggersClasses = getSkippedTriggerClasses();
		HashSet<Class> flat = new HashSet<Class>();
		addClasses(ctx, cls, flat);
		for (Class cls2 : flat) {
			if (skippedTriggersClasses.contains(cls2))
				continue;
			addClassLevelTriggersPerClass(ctx, cls2, tgs, poj, ADD_ALL);
		}
	}

	@SuppressWarnings("unchecked")
	public static <TrigType> void addClassLevelTriggersPerClass(DisplayContext ctx, Class cls, List<TrigType> tgs, WrapperValue poj, int rulesOfAdd) {
		try {
			addPanelClasses(ctx, cls, tgs, poj);
			boolean onlyThisClass = true;
			BeanInfo bi = Utility.getBeanInfo(cls, onlyThisClass);
			if (bi == null)
				return;
			addFeatureTriggers(ctx, cls, bi.getMethodDescriptors(), tgs, poj, rulesOfAdd);
			addFeatureTriggers(ctx, cls, bi.getEventSetDescriptors(), tgs, poj, rulesOfAdd);
			addFeatureTriggers(ctx, cls, bi.getPropertyDescriptors(), tgs, poj, rulesOfAdd);
		} catch (Exception e) {
			Utility.theLogger.error("" + cls, e);

		}
	}

	@SuppressWarnings("unchecked")
	public static <TrigType> void addFeatureTriggers(DisplayContext ctx, Class cls, FeatureDescriptor[] fd, List<TrigType> tgs, WrapperValue poj, int rulesOfAdd) {
		boolean clsHidden = hasAnotation(cls, UIHidden.class);
		boolean clsSalient = hasAnotation(cls, UISalient.class);
		UISalient isSalientCls = (UISalient) cls.getAnnotation(UISalient.class);
		for (FeatureDescriptor f : fd) {
			UISalient isSalientMethod = isSalientCls;
			AnnotatedElement method = getAnnotatedElement(cls, f);
			if (method != null) {
				if (hasAnotation(method, UIHidden.class)) {
					continue;
				}
				if (hasAnotation(method, UISalient.class)) {
					isSalientMethod = method.getAnnotation(UISalient.class);
				} else if (clsHidden) {
					continue;
				}
			}
			TriggerForMethod tfi = new TriggerForMethod(ctx, cls, poj, f);
			tfi.applySalience(isSalientMethod);
			if (!tgs.contains(tfi))
				tgs.add((TrigType) tfi);
		}

	}

	private static AnnotatedElement getAnnotatedElement(Class cls, FeatureDescriptor f) {
		AnnotatedElement method = getAnyMethodObject(f);
		if (method == null) {
			method = getAnyFieldObject(cls, f);
		}
		if (method == null) {
			//warn("cant get Accessable object for " + f + " in " + cls);
			return null;
		}
		return method;
	}

	private static Field getAnyFieldObject(Class cls, FeatureDescriptor fd) {
		if (cls == null) {
			return null;
		}
		String name = fd.getName();
		if (fd instanceof PropertyDescriptor) {
			for (Field f : cls.getDeclaredFields()) {
				if (f.getName().equalsIgnoreCase(name)) {
					return f;
				}
			}
		}
		return getAnyFieldObject(cls.getSuperclass(), fd);
	}

	private static boolean hasAnotation(AnnotatedElement method, final Class<? extends Annotation> class1) {
		if (method == null)
			return false;
		if (true)
			return method.isAnnotationPresent(class1);

		Annotation[] decl = method.getAnnotations();
		if (CollectionSetUtils.containsOne(decl, new CollectionSetUtils.TAccepts<Annotation>() {
			@Override
			public boolean isCompleteOn(Annotation e) {
				return resultOf(e);
			}

			@Override
			public boolean resultOf(Annotation e) {
				return class1.isAnnotationPresent(e.annotationType());
			}
		})) {
			return true;
		}
		return false;
	}

	static String describeFD(FeatureDescriptor fd) {
		return fd.getName() + " " + fd.getShortDescription() + " isExpert=" + fd.isExpert() + " isHidden=" + fd.isHidden() + " " + fd.getClass().getSimpleName() + " " + getReadMethodObject(fd);
	}

	static Method getAnyMethodObject(FeatureDescriptor _featureDescriptor) {
		if (_featureDescriptor instanceof MethodDescriptor) {
			MethodDescriptor md = (MethodDescriptor) _featureDescriptor;
			return md.getMethod();
		}
		if (_featureDescriptor instanceof EventSetDescriptor) {
			EventSetDescriptor md = (EventSetDescriptor) _featureDescriptor;
			return md.getGetListenerMethod();//md.getGetListenerMethod();
		}
		if (_featureDescriptor instanceof PropertyDescriptor) {
			PropertyDescriptor md = (PropertyDescriptor) _featureDescriptor;
			Method m = md.getReadMethod();
			if (m != null)
				return m;
			Method m2 = md.getWriteMethod();
			if (m2 != null)
				return m2;
		}
		return null;
	}

	static Method getReadMethodObject(FeatureDescriptor _featureDescriptor) {
		if (_featureDescriptor instanceof MethodDescriptor) {
			MethodDescriptor md = (MethodDescriptor) _featureDescriptor;
			return md.getMethod();
		}
		if (_featureDescriptor instanceof EventSetDescriptor) {
			EventSetDescriptor md = (EventSetDescriptor) _featureDescriptor;
			return null;//md.getGetListenerMethod();
		}
		if (_featureDescriptor instanceof PropertyDescriptor) {
			PropertyDescriptor md = (PropertyDescriptor) _featureDescriptor;
			Method m = md.getReadMethod();
			if (m != null)
				return m;
			Method m2 = md.getWriteMethod();
			if (m2 != null)
				return m2;
		}
		return null;
	}

	private static HashSet<Class> getSkippedTriggerClasses() {
		HashSet<Class> flat = new HashSet<Class>();
		flat.add(Object.class);
		flat.add(BasicDebugger.class);
		flat.add(Loggable.class);
		//		flat.add(NoObject.class);
		flat.add(UIHidden.class);
		flat.add(Annotation.class);
		return flat;
	}

	Class _clazz;
	Object _object;
	DisplayContext displayContext;
	JMenuItem jmi;

	static Class classOrFirstInterfaceR(Class _clazz2) {
		Class sc = classOrFirstInterface(_clazz2);
		if (sc != null)
			return sc;
		return _clazz2;

	}

	static Class classOrFirstInterface(Class _clazz2) {

		if (_clazz2 == null)
			return null;
		if (_clazz2 == Object.class)
			return null;
		if (_clazz2.isInterface())
			return _clazz2;
		if (hasAnotation(_clazz2, UISalient.class))
			return _clazz2;
		for (Class c : _clazz2.getInterfaces()) {
			if (hasAnotation(c, UIHidden.class))
				continue;
			if (c.getPackage().getName().startsWith("j"))
				continue;
			c = classOrFirstInterface(c);
			if (c == null)
				continue;
			return c;
		}
		Class sc = classOrFirstInterface(_clazz2.getSuperclass());
		if (sc != null)
			return sc;
		return _clazz2;
	}

	protected void addSubResult(Box targetBox, Object obj, Class expected) throws PropertyVetoException {
		expected = Utility.nonPrimitiveTypeFor(expected);
		if (Number.class.isAssignableFrom(expected)) {
			expected = String.class;
			obj = "" + obj;
		}
		if (Enum.class.isAssignableFrom(expected)) {
			expected = String.class;
			obj = "" + obj;
		}
		if (obj == null) {
			obj = "Null " + expected;
			expected = String.class;
		}
		if (expected == String.class) {
			Utility.setLastResult(this, obj, expected);
			try {
				Utility.browserPanel.showMessage("" + obj);
				return;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
		DisplayType dt = Utility.getDisplayType(expected);
		final DisplayType edt = dt;
		if (dt == DisplayType.TREE) {
			BT boxed = Utility.getTreeBoxCollection().findOrCreateBox(null, obj);
			BoxContext bc = targetBox.getBoxContext();
			bc.contextualizeAndAttachChildBox((Box) targetBox, (MutableBox) boxed);
			return;
		}
		if (dt == DisplayType.TOSTRING) {
			Utility.setLastResult(this, obj, expected);
			return;
		}
		try {
			Utility.getCurrentContext().showScreenBox(obj);
		} catch (Exception e) {
			BT boxed = Utility.getTreeBoxCollection().findOrCreateBox(null, obj);
			BoxContext bc = targetBox.getBoxContext();
			JPanel pnl = boxed.getPropertiesPanel();
			if (dt == DisplayType.FRAME) {
				BoxPanelSwitchableView jtp = Utility.getBoxPanelTabPane();
				jtp.addComponent(pnl.getName(), pnl, DisplayType.FRAME);
				return;
			}
			BoxPanelSwitchableView jtp = Utility.getBoxPanelTabPane();
			jtp.addComponent(pnl.getName(), pnl, DisplayType.PANEL);

		}

	}

	abstract public void fireIT(Box targetBox) throws InvocationTargetException;

	@Override
	public void fire(Box targetBox) {
		try {
			fireIT(targetBox);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

	}

	@Override
	public Ident getIdent() {
		return super.getIdent();
	}

	@Override
	public void setDescription(String description) {
		if (getValue(Action.NAME) == null)
			putValue(Action.NAME, description);
		super.setDescription(description);
	}

	@Override
	public void setIdent(Ident id) {
		super.setIdent(id);
	}

	@Override
	public void setShortLabel(String title) {
		putValue(Action.NAME, title);
		super.setShortLabel(title);
	}

	abstract public String getMenuName();

	@Override
	public String toString() {
		String s = getDescription();
		if (s != null)
			return s;
		return getMenuPath();
	}

	abstract void setMenuInfo();

	abstract Object getIdentityObject();

	@Override
	public void visitComponent(JComponent comp) {
		if (comp instanceof JMenuItem) {
			jmi = (JMenuItem) comp;
			jmi.setText(getMenuName());
			jmi.setToolTipText(getDescription());
			setMenuInfo();
		}
	}

	private Action actionImpl = new AbstractAction() {

		{
			setEnabled(true);
		}

		public String toString() {
			return getMenuPath();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			TriggerForInstance.this.actionPerformed(e);

		}

	};

	@Override
	public Object getValue(String key) {
		return actionImpl.getValue(key);
	}

	@Override
	public void putValue(String key, Object value) {
		actionImpl.putValue(key, value);

	}

	@Override
	public void setEnabled(boolean b) {
		actionImpl.setEnabled(b);

	}

	@Override
	public boolean isEnabled() {
		return actionImpl.isEnabled();
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		actionImpl.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		actionImpl.addPropertyChangeListener(listener);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			fireIT(Utility.boxObject(e.getSource()).asBox());
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
			throw Debuggable.reThrowable(e1);
		}
	}
	
	abstract public int hashCode();
}
