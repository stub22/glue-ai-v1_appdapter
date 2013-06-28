package org.appdapter.gui.rimpl;

import java.awt.Color;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.FeatureDescriptor;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.PropertyVetoException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.appdapter.api.trigger.AnyOper.UIHidden;
import org.appdapter.api.trigger.AnyOper.UISalient;
import org.appdapter.api.trigger.BT;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxContext;
import org.appdapter.api.trigger.BoxPanelSwitchableView;
import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.api.trigger.DisplayType;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.api.trigger.TriggerImpl;
import org.appdapter.core.component.KnownComponent;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.log.Loggable;
import org.appdapter.core.name.Ident;
import org.appdapter.gui.api.UIAware;
import org.appdapter.gui.api.Utility;
import org.appdapter.gui.box.WrapperValue;
import org.appdapter.gui.util.CollectionSetUtils;
import org.appdapter.gui.util.PromiscuousClassUtils;

public class TriggerForInstance extends TriggerImpl implements UIAware, Comparable<Trigger> {

	static int ADD_ALL = 255;
	static int ADD_FROM_PASTE_SRC_ONLY = 255;
	static int ADD_FROM_PASTE_TARG_ONLY = 255;

	private static Class[] CLASS0 = new Class[0];

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

	@SuppressWarnings("unchecked") public static <TrigType> void addClassLevelTriggersPerClass(DisplayContext ctx, Class cls, List<TrigType> tgs, WrapperValue poj, int rulesOfAdd) {
		try {
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

	@SuppressWarnings("unchecked") public static <TrigType> void addFeatureTriggers(DisplayContext ctx, Class cls, FeatureDescriptor[] fd, List<TrigType> tgs, WrapperValue poj, int rulesOfAdd) {
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
			TriggerForInstance tfi = new TriggerForInstance(ctx, cls, poj, f);
			tfi.applySalience(isSalientMethod);
			if (!tgs.contains(tfi))
				tgs.add((TrigType) tfi);
		}

	}

	private UISalient isSalientMethod;

	private void applySalience(UISalient isSalientMethod) {
		if (isSalientMethod == null)
			return;
		this.isSalientMethod = isSalientMethod;
		String mn = isSalientMethod.MenuName();
		if (mn != null && mn.length() > 0) {
			menuName = mn;
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
			@Override public boolean isCompleteOn(Annotation e) {
				return resultOf(e);
			}

			@Override public boolean resultOf(Annotation e) {
				return class1.isAnnotationPresent(e.annotationType());
			}
		})) {
			return true;
		}
		return false;
	}

	private static String describeFD(FeatureDescriptor fd) {
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
	public String menuName;
	FeatureDescriptor _featureDescriptor;
	Object _object;

	private DisplayContext displayContext;

	JMenuItem jmi;

	public TriggerForInstance(DisplayContext ctx, Class cls, Object obj, FeatureDescriptor fd) {
		_clazz = cls;
		_object = obj;
		_featureDescriptor = fd;
		displayContext = ctx;
		setDescription(describeFD(fd));
		setShortLabel(getMenuPath());
	}

	private void addSubResult(Box targetBox, Object obj, Class expected) throws PropertyVetoException {
		expected = PromiscuousClassUtils.nonPrimitiveTypeFor(expected);
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
		org.appdapter.api.trigger.DisplayType dt = Utility.getDisplayType(expected);
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

	private Class classOrFirstInterfaceR(Class _clazz2) {
		Class sc = classOrFirstInterface(_clazz2);
		if (sc != null)
			return sc;
		return _clazz2;

	}

	private Class classOrFirstInterface(Class _clazz2) {

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

	/**
	 * Compares this object with the specified object for order. 
	 *
	 * <p>The subclasser must ensure <tt>sgn(x.compareTo(y)) ==
	 * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
	 * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
	 * <tt>y.compareTo(x)</tt> throws an exception.)
	 *
	 * @param   o the Trigger to be compared.
	 * @return  a 0 = if the objects are the same
	 *   +/-1 if the triggers have the same side effect
	 *   +/-2 if the triggers have a different side effect 
	 *
	 * @throws ClassCastException if the specified object's type prevents it
	 *         from being compared to this object.
	 */
	@Override public int compareTo(@SuppressWarnings("rawtypes") Trigger o) {
		if (this == o)
			return 0;
		else if (this == null)
			return 2;

		if (equalsByObject(o))
			return 0;

		boolean ej = equalJob(o);

		int labelDif = toString().compareTo(o.toString());
		if (ej)
			return (int) Math.signum(labelDif);
		return labelDif;
	}

	public boolean equalJob(Trigger obj) {
		if (equalsByObject(obj))
			return true;

		KnownComponent other = (KnownComponent) obj;
		/// assume they are named the same
		if (other.getShortLabel().equals(getShortLabel()))
			return true;

		// for now assume a different job if a different datatype
		if (!(other instanceof TriggerForInstance)) {
			return false;
		}

		TriggerForInstance tfi = (TriggerForInstance) obj;
		Class rt = getReturnType();
		if (rt != tfi.getReturnType())
			return false;
		Method rm = this.getMethod();
		if (rm != null) {
			Method om = tfi.getMethod();
			if (om != null) {
				boolean sameMName = rm.getName().equals(om.getName());
				if (sameMName)
					return true;
				return false;
			}
		}
		if (rt == void.class)
			return false;
		return true;

	}

	@Override public boolean equals(Object obj) {
		if (!(obj instanceof Trigger)) {
			return false;
		}
		Trigger other = (Trigger) obj;
		if (equalsByObject(other))
			return true;
		if (equalJob(other))
			return true;
		return false;
	}

	private boolean equalsByObject(Trigger o) {
		if (this == o)
			return true;
		else if (this == null)
			return false;
		if (!(o instanceof TriggerForInstance)) {
			return false;
		}
		TriggerForInstance tfi = (TriggerForInstance) o;
		Method rm = this.getMethod();
		if (rm != null) {
			Method om = tfi.getMethod();
			if (om != null)
				return rm.getName().equals(om.getName());
		}
		if (tfi.getFieldSummary().equals(getFieldSummary()))
			return true;
		return false;
	}

	@Override public void fire(Box targetBox) {
		try {
			fireIT(targetBox);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

	}

	public void fireIT(Box targetBox) throws InvocationTargetException {
		try {
			Method m = getReadMethodObject(_featureDescriptor);
			if (m != null) {
				Class rt = m.getReturnType();
				Object obj = Utility.invokeFromUI(_object, m);
				if (rt != void.class)
					addSubResult(targetBox, obj, rt);
				return;
			}
			getLogger().debug(this.toString() + " firing on " + targetBox.toString());
		} catch (InvocationTargetException e) {
			throw e;
		} catch (Throwable e) {
			throw new InvocationTargetException(e);
		}
	}

	private Class getDeclaringClass() {
		Method m = getMethod();
		if (m == null)
			return _clazz;
		return m.getDeclaringClass();
	}

	@Override public String getDescription() {
		String myDescription = super.getDescription();
		if (myDescription == null) {
			myDescription = "" + _featureDescriptor;
			setDescription(myDescription);
		}
		return myDescription;
	}

	DisplayContext getDisplayContext() {
		if (displayContext != null)
			return displayContext;
		return Utility.getCurrentContext();
	}

	@Override public Ident getIdent() {
		return super.getIdent();
	}

	public String getMenuName() {
		if (menuName != null)
			return menuName;
		return (" " + _featureDescriptor.getDisplayName() + " ").replace(" get", "Show ").replace(" set", "Replace ").trim();
	}

	public String getMenuPath() {
		String s = getMenuName();
		if (isStatic()) {
			s = "Static|" + s;
		}
		s = Utility.getShortClassName(classOrFirstInterfaceR(_clazz)) + "|" + s;
		Class getRet = getReturnType();
		/*if (getRet == void.class) {
			s = "Invoke|" + s;
		} else {
			s = _featureDescriptor.getClass().getSimpleName() + "|" + s;
		}*/
		s = s.replace("PropertyDescriptor|", "Show ");
		return s;
	}

	public Method getMethod() {
		Method m = getReadMethodObject(_featureDescriptor);
		return m;
	}

	private Class[] getParameters() {
		Method m = getMethod();
		if (m == null)
			return CLASS0;
		return m.getParameterTypes();
	}

	private Class getReturnType() {
		Method m = getMethod();
		if (m == null)
			return void.class;
		return m.getReturnType();
	}

	@Override public String getShortLabel() {
		String myShortLabel = super.getShortLabel();
		if (myShortLabel == null) {
			myShortLabel = "" + _featureDescriptor;
			setShortLabel(myShortLabel);
		}
		return myShortLabel;
	}

	private boolean isStatic() {
		Method m = getMethod();
		if (m == null)
			return false;
		return Modifier.isStatic(m.getModifiers());
	}

	@Override public void setDescription(String description) {
		super.setDescription(description);
	}

	@Override public void setIdent(Ident id) {
		super.setIdent(id);
	}

	private void setMenuInfo() {
		jmi.setText(getMenuName());
		jmi.setToolTipText(getDescription());
		if (_featureDescriptor instanceof PropertyDescriptor) {
			jmi.setBackground(Color.GREEN);
		}
		Method m = getReadMethodObject(_featureDescriptor);
		if (m == null) {
			jmi.setBackground(Color.RED);
			return;
		}
		Class[] pts = m.getParameterTypes();
		boolean isStatic = Modifier.isStatic(m.getModifiers());
		if (isStatic) {
			jmi.setBackground(Color.ORANGE);
		}
		int needsArgument = pts.length;
		if (isStatic)
			needsArgument = pts.length - 1;
		if (needsArgument > 1) {
			jmi.setForeground(Color.GRAY);
			jmi.setBackground(Color.BLACK);
		} else {
			if (needsArgument > 0) {
				jmi.setForeground(Color.GRAY);
			}
		}

	}

	@Override public void setShortLabel(String description) {
		super.setShortLabel(description);
	}

	@Override public String toString() {
		return getDescription();
	}

	@Override public void visitComponent(JComponent comp) {
		if (comp instanceof JMenuItem) {
			jmi = (JMenuItem) comp;
			setMenuInfo();
		}

	}
}
