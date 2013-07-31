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

package org.appdapter.gui.trigger;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.FeatureDescriptor;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.MenuElement;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.appdapter.api.trigger.AnyOper.UIHidden;
import org.appdapter.api.trigger.AnyOper.UISalient;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.core.component.KnownComponent;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.log.Loggable;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.UIAware;
import org.appdapter.gui.api.WrapperValue;
import org.appdapter.gui.box.AbstractScreenBoxTreeNodeImpl;
import org.appdapter.gui.browse.PropertyDescriptorForField;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.swing.SafeJMenu;
import org.appdapter.gui.swing.SafeJMenuItem;
import org.appdapter.gui.util.CollectionSetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
@SuppressWarnings("unchecked")
public class TriggerMenuFactory<TT extends Trigger<Box<TT>> & KnownComponent> {

	static public class JMenuWithPath extends SafeJMenu {
		ArrayList<Component> mcomps = new ArrayList<Component>();

		public JMenuWithPath(String lbl, Object obj) {
			super(true, lbl, obj);
		}

		@Override public Component add(Component c) {
			ensureUnequelyNamed(c);
			Component r = super.add(c);
			mcomps.add(c);
			ensureFoundNamed(c);
			return r;
		}

		@Override public Component add(Component c, int index) {
			ensureUnequelyNamed(c);
			mcomps.add(index, c);
			Component r = super.add(c, index);
			ensureFoundNamed(c);
			return r;
		}

		@Override public JMenuItem add(JMenuItem c) {
			ensureUnequelyNamed(c);
			JMenuItem r = super.add(c);
			ensureFoundNamed(c);
			return r;
		}

		private void ensureFoundNamed(Component c) {
			String fnd = TriggerMenuFactory.getLabel(c, 1);
			Component found = findChildNamed(this, true, fnd);
			if (found != null) {
				return;
			}
			found = findChildNamed(this, true, fnd);
			Debuggable.mustBeSameStrings("found=" + found, fnd);
		}

		private void ensureUnequelyNamed(Component c) {
			String fnd = TriggerMenuFactory.getLabel(c, 1);
			Component found = findChildNamed(this, true, fnd);
			if (found == null) {
				Component p = getParent();
				return;
			}
			Debuggable.mustBeSameStrings("found=" + found, fnd);
		}

		@Override public Component[] getComponents() {
			if (true)
				return mcomps.toArray(new Component[mcomps.size()]);
			return super.getMenuComponents();
		}

		@Override public String getText() {
			return super.getText();
		}

		@Override public void removeAll() {
			Debuggable.notImplemented();
			mcomps.clear();
			super.removeAll();
		}

		@Override public String toString() {
			Component p = getParent();//.toString();
			if (p != null) {
				return "" + TriggerMenuFactory.getLabel(p, 1) + "->" + TriggerMenuFactory.getLabel(this, 1);
			}
			return TriggerMenuFactory.getLabel(this, 1);
		}
	}

	static public class TriggerSorter implements Comparator<Trigger> {

		@Override public int compare(Trigger o1, Trigger o2) {
			int r = getTriggerSortName(o1).toLowerCase().compareTo(getTriggerSortName(o2).toLowerCase());
			if (r == 0) {
				return getTriggerName(o1).toLowerCase().compareTo(getTriggerName(o2).toLowerCase());
			}
			return r;
		}
	}

	static Logger theLogger = LoggerFactory.getLogger(TriggerMenuFactory.class);

	static TriggerMenuFactory triggerMenuFactory = new TriggerMenuFactory();

	public static final TriggerFilter ADD_ALL = new TriggerFilter(true);

	public static final TriggerFilter ADD_INSTANCE = new TriggerFilter(false) {
		{
			addInstance = true;
			addSuperClass = true;
			addAllAccessLevels(true);
		}
	};

	public static final TriggerFilter ADD_STATIC = new TriggerFilter(false) {
		{
			addStatic = true;
			addSuperClass = true;
			addAllAccessLevels(true);
		}
	};

	public final static Class[] CLASS0 = new Class[0];

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

	public static <TrigType> void addTriggersForInstance(DisplayContext ctx, Class cls, List<TrigType> tgs, WrapperValue poj) {
		for (TriggerAdder ta : Utility.getTriggerAdders(ctx, cls, poj)) {
			ta.addTriggersForObjectInstance(ctx, cls, tgs, poj, ADD_ALL, "");
		}
	}

	public static <TrigType> void addTriggersForObjectInstance(DisplayContext ctx, Class cls, List<TrigType> tgs, WrapperValue poj, TriggerFilter rulesOfAdd, String menuPrepend,
			boolean isDeclNonStatic) {
		addTriggersForObjectInstanceMaster(ctx, cls, tgs, poj, rulesOfAdd, menuPrepend, isDeclNonStatic);
	}

	public static <TrigType> void addTriggersForObjectInstanceMaster(DisplayContext ctx, Class cls, List<TrigType> tgs, WrapperValue poj, TriggerFilter rulesOfAdd, String menuName,
			boolean isDeclNonStatic) {
		addClassLevelTriggers00(ctx, cls, tgs, poj, rulesOfAdd, menuName, isDeclNonStatic);

		if (rulesOfAdd.addPanelClasses)
			addPanelClasses(ctx, cls, tgs, poj);

		if (rulesOfAdd.addGlobalStatics)
			addGlobalStatics(ctx, cls, tgs, poj);

		Object inst = Utility.dref(poj);
		if (inst instanceof Class) {
			addClassLevelTriggers00(ctx, (Class) inst, tgs, null, rulesOfAdd, menuName, isDeclNonStatic);
		}
	}

	static <TrigType> void addClassLevelTriggers00(DisplayContext ctx, Class cls, List<TrigType> tgs, WrapperValue poj, TriggerFilter rulesOfAdd, String menuName, boolean isDeclNonStatic) {
		HashSet<Class> skippedTriggersClasses = getSkippedTriggerClasses();
		HashSet<Class> flat = new HashSet<Class>();
		if (rulesOfAdd.addSuperClass) {
			addClasses(ctx, cls, flat);
		} else {
			flat.add(cls);
		}
		for (Class cls2 : flat) {
			if (skippedTriggersClasses.contains(cls2))
				continue;
			if (cls2.isInterface())
				continue;
			addClassLevelTriggersPerClass(ctx, cls2, tgs, poj, rulesOfAdd, menuName, isDeclNonStatic);
		}
	}

	public static <TrigType> void addClassLevelTriggersPerClass(DisplayContext ctx, Class cls, List<TrigType> tgs, WrapperValue poj, TriggerFilter rulesOfAdd, String menuName, boolean isDeclNonStatic) {
		boolean allowStatic = poj == null || isDeclNonStatic;
		boolean allowNonStatic = poj != null;
		for (Method m : cls.getDeclaredMethods()) {
			if (m.isSynthetic())
				continue;
			if (!cls.isInterface() && ReflectUtils.isOverride(m))
				continue;
			boolean isStatic = ReflectUtils.isStatic(m);
			if (isStatic && !allowStatic)
				continue;
			if (!isStatic && !allowNonStatic)
				continue;
			addFMethodTrigWF(ctx, cls, m, tgs, poj, rulesOfAdd, menuName, null, false);
		}
		for (Field m : cls.getDeclaredFields()) {
			if (m.isSynthetic())
				continue;
			boolean isStatic = ReflectUtils.isStatic(m);
			if (isStatic && !allowStatic)
				continue;
			if (!isStatic && !allowNonStatic)
				continue;
			if (ReflectUtils.nonPrimitiveTypeFor(ReflectUtils.getReturnType(m)) == Boolean.class) {
				try {
					addFMethodTrigWF(ctx, cls, m, tgs, poj, rulesOfAdd, menuName, PropertyDescriptorForField.findOrCreate(m), false);
				} catch (Throwable e) {
					Utility.theLogger.error("" + cls, e);
				}
			}
		}
	}

	public static <TrigType> void addClassLevelTriggersPerBeanClass(DisplayContext ctx, Class cls, List<TrigType> tgs, WrapperValue poj, TriggerFilter rulesOfAdd, String menuName,
			boolean isDeclNonStatic) {
		try {
			boolean onlyThisClass = true;
			BeanInfo bi = Utility.getBeanInfo(cls, onlyThisClass, poj);
			if (bi == null)
				return;
			addFeatureTriggers(ctx, cls, bi.getMethodDescriptors(), tgs, poj, rulesOfAdd, menuName, isDeclNonStatic);
			addFeatureTriggers(ctx, cls, bi.getEventSetDescriptors(), tgs, poj, rulesOfAdd, menuName, isDeclNonStatic);
			addFeatureTriggers(ctx, cls, bi.getPropertyDescriptors(), tgs, poj, rulesOfAdd, menuName, isDeclNonStatic);
		} catch (Exception e) {
			Utility.theLogger.error("" + cls, e);

		}
	}

	@SuppressWarnings("unchecked") public static <TrigType> void addFeatureTriggers(DisplayContext ctx, Class cls, FeatureDescriptor[] fd, List<TrigType> tgs, WrapperValue poj,
			TriggerFilter rulesOfAdd, String menuName, boolean isDeclNonStatic) {
		for (FeatureDescriptor f : fd) {
			addFeatureDesc(ctx, cls, f, tgs, poj, rulesOfAdd, menuName, isDeclNonStatic);
		}

	}

	@SuppressWarnings("unchecked") public static <TrigType> void addFeatureDesc(DisplayContext ctx, Class cls, FeatureDescriptor fd, List<TrigType> tgs, WrapperValue poj, TriggerFilter rulesOfAdd,
			String menuName, boolean isDeclNonStatic) {
		if (!rulesOfAdd.addEvents && fd instanceof EventSetDescriptor)
			return;

		if (fd instanceof MethodDescriptor) {
			MethodDescriptor md = (MethodDescriptor) fd;
			addFMethodTrigWF(ctx, cls, md.getMethod(), tgs, poj, rulesOfAdd, menuName, fd, false);
			return;
		}

		if (fd instanceof PropertyDescriptor) {
			PropertyDescriptor md = (PropertyDescriptor) fd;
			addFMethodTrigWF(ctx, cls, md.getReadMethod(), tgs, poj, rulesOfAdd, menuName, fd, true);
			addFMethodTrigWF(ctx, cls, md.getWriteMethod(), tgs, poj, rulesOfAdd, menuName, fd, false);
			return;
		}
	}

	private static <TrigType> void addFMethodTrigWF(DisplayContext ctx, Class cls, Member method, List<TrigType> tgs, WrapperValue poj, TriggerFilter rulesOfAdd, String menuName,
			FeatureDescriptor featureDesc, boolean isSafe) {
		if (method == null)
			return;
		addMethodAsTrigWF0(ctx, cls, method, tgs, poj, rulesOfAdd, menuName, false, featureDesc, isSafe);
	}

	public static <TrigType> void addMethodAsTrig(DisplayContext ctx, Class cls, Member method, List<TrigType> tgs, WrapperValue poj, TriggerFilter rulesOfAdd, String menuName,
			boolean isDeclNonStatic, boolean isSafe) {
		addMethodAsTrigWF0(ctx, cls, method, tgs, poj, rulesOfAdd, menuName, isDeclNonStatic, null, isSafe);
	}

	private static <TrigType> void addMethodAsTrigWF0(DisplayContext ctx, Class cls, Member method, List<TrigType> tgs, WrapperValue poj, TriggerFilter rulesOfAdd, String menuName,
			boolean isDeclNonStatic, FeatureDescriptor featureDesc, boolean isSafe) {
		boolean clsHidden = hasAnotation(cls, UIHidden.class);
		boolean clsSalient = hasAnotation(cls, UISalient.class);
		UISalient isSalientCls = (UISalient) cls.getAnnotation(UISalient.class);
		UISalient isSalientMethod = isSalientCls;
		AnnotatedElement ae = (AnnotatedElement) method;
		if (hasAnotation(ae, UIHidden.class)) {
			return;
		}
		if (hasAnotation(ae, UISalient.class)) {
			isSalientMethod = ae.getAnnotation(UISalient.class);
		} else {
			if (clsHidden)
				return;
			isSalientMethod = isSalientCls;
		}
		if (!rulesOfAdd.accepts((Member) method))
			return;

		Class cls00 = method.getDeclaringClass();
		TriggerForMember tfi = new TriggerForMember(menuName, ctx, cls00, poj, method, isDeclNonStatic, featureDesc, isSafe);
		tfi.applySalience(isSalientMethod);
		if (!tgs.contains(tfi))
			tgs.add((TrigType) tfi);

	}

	public static <TrigType> void addGlobalStatics(DisplayContext ctx, Class cls, List<TrigType> tgs, WrapperValue poj) {
		if (!UtilityMenuOptions.addGlobalStatics)
			return;
		for (Trigger trig : Utility.getGlobalStaticTriggers(ctx, cls, poj)) {
			CollectionSetUtils.addIfNew(tgs, (TrigType) trig);
		}
	}

	public static <TrigType> void addPanelClasses(DisplayContext ctx, Class cls, List<TrigType> tgs, WrapperValue poj) {
		if (!UtilityMenuOptions.addPanelClasses)
			return;
		for (Class pnlClz : Utility.findPanelClasses(cls)) {
			if (pnlClz == null)
				continue;
			CollectionSetUtils.addIfNew(tgs, ((TrigType) new ShowPanelTrigger(ctx, cls, poj, pnlClz)));
		}
	}

	static void addSeparatorIfNeeded(Container popup, int greaterThan) {
		if (popup.getComponentCount() > greaterThan) {
			if (popup instanceof JMenu) {
				JMenu jmenu = (JMenu) popup;
				jmenu.addSeparator();
				return;
			}
			if (popup instanceof JPopupMenu) {
				JPopupMenu jmenu = (JPopupMenu) popup;
				jmenu.addSeparator();
				return;
			}
		}
	}

	public static void addTriggerToPoppup(Container popup, Box box, String[] path, int idx, Trigger trig) {
		boolean isLast = path.length - idx == 1;
		if (idx >= path.length) {
			// trying to get something longer than array
			return;
		}
		final String lbl = path[idx].trim();
		Component child = findChildNamed(popup, true, lbl.toLowerCase());
		if (isLast) {
			if (child == null) {
				popup.add(makeMenuItem(box, lbl, trig));
			}
			return;
		}
		if (child == null) {
			JMenuWithPath item = new JMenuWithPath(lbl, box);
			popup.add(item, 0);
			//addSeparatorIfNeeded(popup, 1);
			child = item;
		}
		addTriggerToPoppup((Container) child, box, path, idx + 1, trig);
	}

	public static void addTriggerToPoppup(Container popup, Box box, Trigger trig) {
		String[] path = getTriggerPath(trig);
		org.appdapter.gui.trigger.TriggerMenuFactory.addTriggerToPoppup(popup, box, path, 0, trig);
	}

	private static Component[] childrenOf(Container popup) {
		if (popup instanceof JMenu)
			return ((JMenu) popup).getMenuComponents();
		return popup.getComponents();
	}

	public static Class classOrFirstInterface(Class _clazz2) {
		if (true)
			return _clazz2;
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

	public static Class classOrFirstInterfaceR(Class _clazz2) {
		if (true)
			return _clazz2;
		Class sc = classOrFirstInterface(_clazz2);
		if (sc != null)
			return sc;
		return _clazz2;

	}

	public static String describeMethod(Member fd) {
		return fd.toString() + " decl=" + fd.getDeclaringClass();
	}

	public static String describeFD(FeatureDescriptor fd) {
		return fd.getName() + " " + fd.getShortDescription() + " " + fd.getClass().getSimpleName() + " " + Debuggable.toInfoStringF(fd);

	}

	static Component findChildNamed(Container popup, boolean toLowerCase, Comparable<String> fnd) {
		Component[] comps = childrenOf(popup);
		if (comps == null || comps.length == 0)
			return null;
		Component c2 = null;
		for (Component c : comps) {
			String name = getLabel(c, 1);
			if (name == null)
				continue;
			if (toLowerCase) {
				name = name.toLowerCase();
			}
			if (fnd.compareTo(name) == 0)
				return c;
			String name2 = c.getName();
			if (name2 != null) {
				if (fnd.compareTo(name2) == 0)
					c2 = c;
			}
		}
		return c2;
	}

	public static Field getAnyFieldObject(Class cls, FeatureDescriptor fd) {
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

	public static TriggerMenuFactory getInstance(Object obj) {
		Class triggerClass = Object.class;
		if (obj instanceof Class) {
			triggerClass = (Class) obj;
		} else if (obj != null) {
			triggerClass = obj.getClass();
		}
		return triggerMenuFactory;
	}

	public static String getLabel(Component c, int maxDepth) {

		if (c instanceof JPopupMenu) {
			return ((JPopupMenu) c).getLabel();
		}
		if (c instanceof JMenu) {
			return ((JMenu) c).getText();
		}
		if (c instanceof JMenuItem) {
			return ((JMenuItem) c).getText();
		}
		if (c instanceof JLabel) {
			return ((JLabel) c).getText();
		}
		if (c instanceof AbstractButton) {
			return ((AbstractButton) c).getText();
		}
		if (c instanceof JTextComponent) {
			return ((JTextComponent) c).getText();
		}
		if (c instanceof MenuElement) {
			Component c2 = ((MenuElement) c).getComponent();
			if (c != c2) {
				String text = getLabel(c2, maxDepth);
				if (text != null)
					return text;
			}
		}
		if (maxDepth <= 0)
			return null;

		if (c instanceof Container) {
			int mustBeWithin = 2;
			for (Component c2 : ((Container) c).getComponents()) {
				String text = getLabel(c2, maxDepth - 1);
				if (text != null)
					return text;
				mustBeWithin--;
				if (mustBeWithin <= 0)
					break;
			}
		}
		return null;
	}

	public static Method getReadMethodObject(FeatureDescriptor _featureDescriptor) {
		if (_featureDescriptor instanceof MethodDescriptor) {
			MethodDescriptor md = (MethodDescriptor) _featureDescriptor;
			return md.getMethod();
		}
		if (_featureDescriptor instanceof EventSetDescriptor) {
			EventSetDescriptor md = (EventSetDescriptor) _featureDescriptor;
			Method em = md.getGetListenerMethod();// md.getGetListenerMethod();
			if (em != null)
				return em;
			return null;// md.getGetListenerMethod();
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

	public static String getShortLabel(Trigger trig) {
		String toStr;
		if (trig instanceof KnownComponent) {
			toStr = ((KnownComponent) trig).getShortLabel();
			if (isRealLabel(toStr))
				return toStr;
		}
		return "" + trig;
	}

	private static HashSet<Class> getSkippedTriggerClasses() {
		HashSet<Class> flat = new HashSet<Class>();
		flat.add(Object.class);
		flat.add(BasicDebugger.class);
		flat.add(Loggable.class);
		// flat.add(NoObject.class);
		flat.add(UIHidden.class);
		flat.add(Annotation.class);
		return flat;
	}

	public static String getTriggerName(Trigger trig) {
		String[] path = getTriggerPath(trig);
		if (path == null || path.length == 0)
			return "" + getShortLabel(trig);
		return path[path.length - 1].trim();
	}

	public static String[] getTriggerPath(Trigger trig) {
		return getShortLabel(trig).split("\\|");
	}

	public static String getTriggerSortName(Trigger t) {
		String[] tn = getTriggerName(t).split("|");
		return tn[tn.length - 1];
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

	private static boolean isRealLabel(String toStr) {
		if (toStr == null)
			return false;
		if (toStr.length() == 0)
			return false;
		return true;
	}

	public static AbstractButton makeMenuItem(final Box b, String lbl_unused, final Trigger trig) {

		if (trig instanceof ButtonFactory) {
			return ((ButtonFactory) trig).makeMenuItem(b);
		}
		AbstractButton jmi = new SafeJMenuItem(b, true, getTriggerName(trig));
		if (trig instanceof UIAware) {
			jmi = (AbstractButton) ((UIAware) trig).visitComponent(jmi);
		}
		if (trig instanceof ActionListener) {
			jmi.addActionListener((ActionListener) trig);
		} else {
			jmi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					trig.fire(b);
				}
			});
		}
		return jmi;
	}

	///===================================== INSTANCE METHODS =======================================================

	public TriggerMenuFactory() {

	}

	public void addMenuItem(Action a, Box box, JMenu menu) {
		addTriggerToPoppup(menu, box, ensureTrigger(a));
	}

	public void addMenuItem(Action a, Box box, JPopupMenu menu) {
		addTriggerToPoppup(menu, box, ensureTrigger(a));
	}

	public void addTriggersToPopup(Box box, JComponent popup) {
		if (box instanceof UIAware) {
			popup = ((UIAware) box).visitComponent(popup);
		}
		if (popup instanceof TriggerPopupMenu) {
			// Allready added the items?
			//return;
		}
		List<TT> trigs = new ArrayList();
		trigs.addAll(box.getTriggers());
		int c1 = trigs.size();
		Collections.sort(trigs, new TriggerSorter());
		int c2 = trigs.size();
		if (true || c1 == c2) {
			HashMap<String, TT> map = new HashMap<String, TT>();
			for (TT t : trigs) {
				map.put(t.getShortLabel().toLowerCase(), t);
			}
			trigs = new ArrayList<TT>(map.values());
			c2 = trigs.size();
			Collections.sort(trigs, new TriggerSorter());
		}
		for (TT trig : trigs) {
			addTriggerToPoppup(popup, box, trig);
		}
	}

	public TriggerPopupMenu buildPopupMenu(Box<TT> box) {
		TriggerPopupMenu popup = new TriggerPopupMenu(null, null, null, box);
		addTriggersToPopup(box, popup);
		return popup;
	}

	private Trigger ensureTrigger(final Action a) {
		if (a instanceof Trigger)
			return (Trigger) a;
		return new TriggerForAction(a);
	}

	public MouseAdapter makePopupMouseAdapter() {
		MouseAdapter ma = new MouseAdapter() {

			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					requestContextPopup(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					requestContextPopup(e);
				}
			}

			private void requestContextPopup(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				Object src = e.getSource();
				if (!(src instanceof JTree)) {
					if (src == null)
						src = this;
					theLogger.trace("Click Not in the tree " + src + " " + src.getClass());
					return;
				}
				JTree tree = (JTree) e.getSource();
				TreePath path = tree.getPathForLocation(x, y);
				if (path == null) {
					return;
				}
				tree.setSelectionPath(path);

				// Nodes are not *required* to implement TreeNode
				DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
				Object uo = treeNode.getUserObject();
				TriggerPopupMenu popup;
				Box<TT> box = (Box<TT>) Utility.asBoxed(uo);

				// String label = "popup: " + obj.toString(); // obj.getTreeLabel();
				popup = buildPopupMenu(box);
				if (treeNode instanceof AbstractScreenBoxTreeNodeImpl) {
					((AbstractScreenBoxTreeNodeImpl) treeNode).addExtraTriggers(popup);
				}
				popup.show(tree, x, y);
			}
		};
		return ma;
	}

}
