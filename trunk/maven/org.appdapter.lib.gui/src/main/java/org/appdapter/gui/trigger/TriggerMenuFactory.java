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
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.Action;
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
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.UIAware;
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

	static public class TriggerSorter implements Comparator {

		@Override public int compare(Object o1, Object o2) {
			int r = getLabel(o1, 2).compareToIgnoreCase(getLabel(o2, 2));
			if (r == 0) {
				return ((Integer) System.identityHashCode(o1)).compareTo(System.identityHashCode(o2));
			}
			return r;
		}
	}

	static public class JMenuWithPath extends SafeJMenu {

		public JMenuWithPath(String lbl, Object obj) {
			super(true, lbl, obj);
		}
	}

	//It also looks better if you're ignoring case sensitivity:
	public static TriggerSorter nodeComparator = new TriggerSorter();

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

	public static <TrigType> void addTriggersForInstance(DisplayContext ctx, Class cls, List<TrigType> tgs, Object poj) {
		for (TriggerAdder ta : Utility.getTriggerAdders(ctx, cls, poj)) {
			ta.addTriggersForObjectInstance(ctx, cls, tgs, poj, ADD_ALL, null);
		}
	}

	public static <TrigType> void addTriggersForObjectInstance(DisplayContext ctx, Class cls, List<TrigType> tgs, Object poj, TriggerFilter rulesOfAdd, String menuFmt, boolean isDeclNonStatic) {
		addTriggersForObjectInstanceMaster(ctx, cls, tgs, poj, rulesOfAdd, menuFmt, isDeclNonStatic);
	}

	public static <TrigType> void addTriggersForObjectInstanceMaster(DisplayContext ctx, Class cls, List<TrigType> tgs, Object poj, TriggerFilter rulesOfAdd, String menuFmt, boolean isDeclNonStatic) {
		addClassLevelTriggers00(ctx, cls, tgs, poj, rulesOfAdd, menuFmt, isDeclNonStatic);

		if (rulesOfAdd.addPanelClasses)
			addPanelClasses(ctx, cls, tgs, poj);

		if (rulesOfAdd.addGlobalStatics)
			addGlobalStatics(ctx, cls, tgs, poj);

		Object inst = Utility.dref(poj);
		if (inst instanceof Class) {
			TriggerFilter copy = (TriggerFilter) rulesOfAdd.clone();
			copy.addInstance = false;
			addClassLevelTriggers00(ctx, (Class) inst, tgs, null, copy, menuFmt, isDeclNonStatic);
		}
	}

	static <TrigType> void addClassLevelTriggers00(DisplayContext ctx, Class cls, List<TrigType> tgs, Object poj, TriggerFilter rulesOfAdd, String menuFmt, boolean isDeclNonStatic) {
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
			addClassLevelTriggersPerClass(ctx, cls2, tgs, poj, rulesOfAdd, menuFmt, isDeclNonStatic);
		}
	}

	public static <TrigType> void addClassLevelTriggersPerClass(DisplayContext ctx, Class cls, List<TrigType> tgs, Object poj, TriggerFilter rulesOfAdd, String menuFmt, boolean isDeclNonStatic) {
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
			addFMethodTrigWF(ctx, cls, m, tgs, poj, rulesOfAdd, menuFmt, null, false);
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
					addFMethodTrigWF(ctx, cls, m, tgs, poj, rulesOfAdd, menuFmt, PropertyDescriptorForField.findOrCreate(m), true);
				} catch (Throwable e) {
				//	Utility.theLogger.warn("" + cls + ": " + e);
				}
			}
		}
	}

	public static <TrigType> void addClassLevelTriggersPerBeanClass(DisplayContext ctx, Class cls, List<TrigType> tgs, Object poj, TriggerFilter rulesOfAdd, String menuFmt, boolean isDeclNonStatic) {
		try {
			boolean onlyThisClass = true;
			BeanInfo bi = Utility.getBeanInfo(cls, onlyThisClass, poj);
			if (bi == null)
				return;
			addFeatureTriggers(ctx, cls, bi.getMethodDescriptors(), tgs, poj, rulesOfAdd, menuFmt, isDeclNonStatic);
			addFeatureTriggers(ctx, cls, bi.getEventSetDescriptors(), tgs, poj, rulesOfAdd, menuFmt, isDeclNonStatic);
			addFeatureTriggers(ctx, cls, bi.getPropertyDescriptors(), tgs, poj, rulesOfAdd, menuFmt, isDeclNonStatic);
		} catch (Exception e) {
			Utility.theLogger.error("" + cls, e);

		}
	}

	@SuppressWarnings("unchecked") public static <TrigType> void addFeatureTriggers(DisplayContext ctx, Class cls, FeatureDescriptor[] fd, List<TrigType> tgs, Object poj, TriggerFilter rulesOfAdd,
			String menuFmt, boolean isDeclNonStatic) {
		for (FeatureDescriptor f : fd) {
			addFeatureDesc(ctx, cls, f, tgs, poj, rulesOfAdd, menuFmt, isDeclNonStatic);
		}

	}

	@SuppressWarnings("unchecked") public static <TrigType> void addFeatureDesc(DisplayContext ctx, Class cls, FeatureDescriptor fd, List<TrigType> tgs, Object poj, TriggerFilter rulesOfAdd,
			String menuFmt, boolean isDeclNonStatic) {
		if (!rulesOfAdd.addEvents && fd instanceof EventSetDescriptor)
			return;

		if (fd instanceof MethodDescriptor) {
			MethodDescriptor md = (MethodDescriptor) fd;
			addFMethodTrigWF(ctx, cls, md.getMethod(), tgs, poj, rulesOfAdd, menuFmt, fd, false);
			return;
		}

		if (fd instanceof PropertyDescriptor) {
			PropertyDescriptor md = (PropertyDescriptor) fd;
			addFMethodTrigWF(ctx, cls, md.getReadMethod(), tgs, poj, rulesOfAdd, menuFmt, fd, true);
			addFMethodTrigWF(ctx, cls, md.getWriteMethod(), tgs, poj, rulesOfAdd, menuFmt, fd, false);
			return;
		}
	}

	private static <TrigType> void addFMethodTrigWF(DisplayContext ctx, Class cls, Member method, List<TrigType> tgs, Object poj, TriggerFilter rulesOfAdd, String menuFmt,
			FeatureDescriptor featureDesc, boolean isSafe) {
		if (method == null)
			return;
		addMethodAsTrigWF0(ctx, cls, method, tgs, poj, rulesOfAdd, menuFmt, false, featureDesc, isSafe);
	}

	public static <TrigType> void addMethodAsTrig(DisplayContext ctx, Class cls, Member method, List<TrigType> tgs, Object poj, TriggerFilter rulesOfAdd, String menuFmt, boolean isDeclNonStatic,
			boolean isSafe) {
		addMethodAsTrigWF0(ctx, cls, method, tgs, poj, rulesOfAdd, menuFmt, isDeclNonStatic, null, isSafe);
	}

	private static <TrigType> void addMethodAsTrigWF0(DisplayContext ctx, Class cls, Member method, List<TrigType> tgs, Object poj, TriggerFilter rulesOfAdd, String menuFmt, boolean isDeclNonStatic,
			FeatureDescriptor featureDesc, boolean isSafe) {
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
		TriggerForMember tfi = new TriggerForMember(menuFmt, ctx, cls00, poj, method, isDeclNonStatic, featureDesc, isSafe);
		tfi.applySalience(isSalientMethod);
		if (!tgs.contains(tfi))
			tgs.add((TrigType) tfi);

	}

	public static <TrigType> void addGlobalStatics(DisplayContext ctx, Class cls, List<TrigType> tgs, Object poj) {
		if (!UtilityMenuOptions.addGlobalStatics)
			return;
		for (Trigger trig : Utility.getGlobalStaticTriggers(ctx, cls, poj)) {
			CollectionSetUtils.addIfNew(tgs, (TrigType) trig);
		}
	}

	public static <TrigType> void addPanelClasses(DisplayContext ctx, Class cls, List<TrigType> tgs, Object poj) {
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

	public static void addTriggerToPoppup(Container popup, Object box, String[] path, int idx, Trigger trig) {
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

	public static void addTriggerToPoppup(Container popup, Object box, Trigger trig) {
		String[] path = getTriggerPath(trig);
		addTriggerToPoppup(popup, box, path, 0, trig);
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

	public static Component findChildNamed(Container popup, boolean toLowerCase, Comparable<String> fnd) {
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

	public static String getLabel(Object c, int maxDepth) {
		if (c instanceof Trigger) {
			return getTriggerSortName((Trigger) c);
		}
		if (c instanceof KnownComponent) {
			String shortLabel = ((KnownComponent) c).getShortLabel();
			if (shortLabel != null)
				return shortLabel;
		}
		if (c instanceof Component) {
			return getLabelC((Component) c, maxDepth);
		}
		return "" + c;
	}

	public static String getLabelC(Component c, int maxDepth) {

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

	public static String getShortLabel(Trigger t) {
		if (t == null)
			return "<null trigger>";
		String shortLabel = null;
		if (t instanceof KnownComponent) {
			shortLabel = ((KnownComponent) t).getShortLabel();
		} else if (t instanceof TriggerForInstance) {
			shortLabel = ((TriggerForInstance) t).getMenuPath();
		} else if (t instanceof Component) {
			shortLabel = getLabel((Component) t, 2);
		}
		if (shortLabel != null && isRealLabel(shortLabel))
			return shortLabel;
		return "" + t;
	}

	private static HashSet<Class> getSkippedTriggerClasses() {
		HashSet<Class> flat = new HashSet<Class>();
		flat.add(Object.class);
		//flat.add(BasicDebugger.class);
		//flat.add(Loggable.class);
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
		return getTriggerPath(getShortLabel(trig));
	}

	private static String[] getTriggerPath(String shortLabel) {
		return shortLabel.split("\\|");
	}

	public static String getTriggerSortName(Trigger t) {
		String[] tn = getTriggerPath(t);
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

	public static AbstractButton makeMenuItem(final Object/**/b, String lbl, final Trigger trig) {

		if (trig instanceof ButtonFactory) {
			return ((ButtonFactory) trig).makeMenuItem(lbl, b);
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
					trig.fire(Utility.asBox(b, e));
				}
			});
		}
		return jmi;
	}

	///===================================== INSTANCE METHODS =======================================================

	public TriggerMenuFactory() {

	}

	public void addMenuItem(Action a, Object/**/box, JMenu menu) {
		addTriggerToPoppup(menu, box, ensureTrigger(a));
	}

	public void addMenuItem(Action a, Object/**/box, JPopupMenu menu) {
		addTriggerToPoppup(menu, box, ensureTrigger(a));
	}

	static public void addTriggersToPopup(Object/**/box, JComponent popup) {
		if (box instanceof UIAware) {
			popup = ((UIAware) box).visitComponent(popup);
		}
		if (popup instanceof TriggerPopupMenu) {
			// Allready added the items?
			//return;
		}
		List<Trigger> trigs = getTriggers(box);
		for (Trigger trig : trigs) {
			addTriggerToPoppup(popup, box, trig);
		}
	}

	private static List<Trigger> sortTriggers(List<Trigger> trigs) {
		HashMap<String, Trigger> map = new HashMap<String, Trigger>();
		for (Trigger t : trigs) {
			String shortLabel = getShortLabel(t);
			map.put(shortLabel.toLowerCase(), t);
		}
		trigs = new ArrayList(map.values());
		Collections.sort(trigs, new TriggerSorter());
		return trigs;
	}

	static List<Trigger> getTriggers(Object box) {
		if (!(box instanceof Box)) {
			box = Utility.asBoxed(box);
		}
		return sortTriggers(((Box) box).getTriggers());
	}

	static public TriggerPopupMenu buildPopupMenu(Object/**/box) {
		TriggerPopupMenu popup = new TriggerPopupMenu(null, null, box);
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
				//Object/**/box = (Object/**/) Utility.asBoxed(uo);

				// String label = "popup: " + obj.toString(); // obj.getTreeLabel();
				popup = buildPopupMenu(uo);
				if (treeNode instanceof AbstractScreenBoxTreeNodeImpl) {
					((AbstractScreenBoxTreeNodeImpl) treeNode).addExtraTriggers(popup);
				}
				popup.show(tree, x, y);
			}
		};
		return ma;
	}

	public static void addMap(Map lastResults, Container popup) {
		Map<?, ?> lr;
		synchronized (lastResults) {
			lr = new HashMap(lastResults);
		}
		for (Map.Entry me : lr.entrySet()) {
			String[] path = getTriggerPath(Utility.getUniqueName(me.getKey()));
			Object box = me.getValue();
			List trigs = getTriggers(box);
			addTriggersToPoppup(box, path, trigs, popup);
		}
	}

	public static void addCollection(String prepend, Collection lastResults, Container popup) {
		String[] path;
		if (prepend == null || prepend.length() == 0) {
			path = new String[0];
		} else {
			path = getTriggerPath(prepend);
		}
		ArrayList lr;
		synchronized (lastResults) {
			lr = new ArrayList(lastResults);
		}
		for (Object box : lr) {
			List trigs = getTriggers(box);
			addTriggersToPoppup(box, path, trigs, popup);
		}
	}

	private static void addTriggersToPoppup(Object box, String[] path, Collection<Trigger> triggers, Container popup) {
		for (Trigger t : triggers) {
			List<String> sl = joinArrays(path, getTriggerPath(t));
			addTriggerToPoppup(popup, box, sl.toArray(new String[sl.size()]), 0, t);
		}

	}

	private static <T> List<T> joinArrays(T[] path, T[] ts) {
		if (path == null || path.length == 0)
			return Arrays.asList(ts);
		ArrayList<T> sl = new ArrayList();
		sl.addAll(Arrays.asList(path));
		if (ts != null || ts.length > 0)
			sl.addAll(Arrays.asList(ts));
		return sl;
	}
}
