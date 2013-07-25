package org.appdapter.gui.browse;

import static org.appdapter.gui.trigger.TriggerMenuFactory.ADD_ALL;
import static org.appdapter.gui.trigger.TriggerMenuFactory.ADD_INSTANCE;
import static org.appdapter.gui.trigger.TriggerMenuFactory.ADD_STATIC;
import static org.appdapter.gui.trigger.TriggerMenuFactory.addMethodAsTrig;
import static org.appdapter.gui.trigger.TriggerMenuFactory.addTriggerToPoppup;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.beans.BeanInfo;
import java.beans.Customizer;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSpinner.DateEditor;
import javax.swing.ToolTipManager;
import javax.tools.FileObject;

import org.appdapter.api.trigger.AnyOper.Singleton;
import org.appdapter.api.trigger.AnyOper.UtilClass;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.GetObject;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.api.trigger.UserResult;
import org.appdapter.bind.rdf.jena.model.JenaLiteralUtils;
import org.appdapter.core.boot.ClassLoaderUtils;
import org.appdapter.core.component.KnownComponent;
import org.appdapter.core.convert.Converter;
import org.appdapter.core.convert.NoSuchConversionException;
import org.appdapter.core.convert.OptionalArg;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.core.convert.TypeAssignable;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.appdapter.core.store.Repo;
import org.appdapter.gui.api.AddTabFrames;
import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.BoxPanelSwitchableView;
import org.appdapter.gui.api.BrowserPanelGUI;
import org.appdapter.gui.api.Convertable;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.DisplayType;
import org.appdapter.gui.api.GetSetObject;
import org.appdapter.gui.api.IGetBox;
import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.api.WrapperValue;
import org.appdapter.gui.box.BoxedCollectionImpl;
import org.appdapter.gui.demo.DemoBrowser;
import org.appdapter.gui.editors.AbstractCollectionBeanInfo;
import org.appdapter.gui.editors.ArrayContentsPanel;
import org.appdapter.gui.editors.ArrayContentsPanel.ArrayContentsPanelTabFramer;
import org.appdapter.gui.editors.BooleanEditor;
import org.appdapter.gui.editors.ColorEditor;
import org.appdapter.gui.editors.IntEditor;
import org.appdapter.gui.editors.LargeObjectView;
import org.appdapter.gui.editors.LargeObjectView.BasicObjectCustomizer;
import org.appdapter.gui.editors.LargeObjectView.ClassCustomizer;
import org.appdapter.gui.editors.LargeObjectView.CollectionCustomizer;
import org.appdapter.gui.editors.LargeObjectView.ThrowableCustomizer;
import org.appdapter.gui.editors.SimplePOJOInfo;
import org.appdapter.gui.editors.UseEditor;
import org.appdapter.gui.repo.ModelMatrixPanel;
import org.appdapter.gui.repo.RepoManagerPanel;
import org.appdapter.gui.swing.CollectionEditorUtil;
import org.appdapter.gui.swing.DisplayContextUIImpl.UnknownIcon;
import org.appdapter.gui.swing.ErrorDialog;
import org.appdapter.gui.swing.FileMenu;
import org.appdapter.gui.swing.NamedItemChooserPanel;
import org.appdapter.gui.swing.ObjectChoiceComboPanel;
import org.appdapter.gui.swing.SafeJMenu;
import org.appdapter.gui.trigger.TriggerFilter;
import org.appdapter.gui.trigger.TriggerForInstance;
import org.appdapter.gui.trigger.TriggerForMethod;
import org.appdapter.gui.trigger.TriggerMenuFactory;
import org.appdapter.gui.trigger.TriggerMenuFactory.TriggerSorter;
import org.appdapter.gui.util.CollectionSetUtils;
import org.appdapter.gui.util.FunctionalClassRegistry;
import org.appdapter.gui.util.PromiscuousClassUtils;
import org.slf4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
//import sun.beans.editors.ColorEditor;
//import sun.beans.editors.IntEditor;

public class Utility {

	static public class UtilityConverter implements Converter {

		@Override public <T> T recast(Object obj, Class<T> objNeedsToBe) throws NoSuchConversionException {
			return Utility.recast(obj, objNeedsToBe);
		}

		@Override public Integer declaresConverts(Object val, Class objClass, Class objNeedsToBe) {
			if (objClass == String.class) {
				if (Utility.getToFromConverter(objNeedsToBe, String.class) != null)
					return WILL;
			}
			if (objClass != null) {
				if (Utility.getToFromConverter(objNeedsToBe, objClass) != null)
					return WILL;
			}
			return MIGHT;// .declaresConverts(val, objClass, objNeedsToBe);
		}

	}

	private static ThreadLocal<Boolean> inClassLoadingPing = new ThreadLocal<Boolean>();
	public static List EMPTYLIST = new ArrayList();
	public static Collection<AddTabFrames> addTabFramers = new HashSet<AddTabFrames>();
	final static HashMap<Object, BT> allBoxes = new HashMap();
	public static Logger theLogger = org.slf4j.LoggerFactory.getLogger(Utility.class);
	private static final Class[] CLASS0 = new Class[0];
	public static final Class Stringable = Enum.class;
	final static Map<Class, Map<Class, ToFromKeyConverter>> toFrmKeyCnvMap = new HashMap<Class, Map<Class, ToFromKeyConverter>>();
	static HashMap<String, LinkedList> displayLists = new HashMap<String, LinkedList>();

	// ==== Instance variables ==========
	public static BrowsePanel browserPanel;
	public static NamedObjectCollection uiObjects = new BoxedCollectionImpl("All UI Objects", null);

	static ArrayList<TriggerForInstance> appMenuGlobalTriggers = new ArrayList<TriggerForInstance>();
	static ArrayList<TriggerForMethod> objectContextMenuTriggers = new ArrayList<TriggerForMethod>();

	public static SafeJMenu toolsMenu;

	public static void addSingletonMethods(Object object) {
		addSingletonMethods(object, object.getClass());
	}

	public static void addSingletonMethods(Object object, Class clz) {
		List<TriggerForInstance> menuGlobalTriggers = Utility.appMenuGlobalTriggers;
		synchronized (menuGlobalTriggers) {
			WrapperValue wrap = asWrapped(object);
			DisplayContext ctx = getDisplayContext();
			int ps1 = menuGlobalTriggers.size();
			for (Method m : clz.getDeclaredMethods()) {
				// declared methods not static
				if (!ReflectUtils.isStatic(m)) {
					if (m.isSynthetic())
						continue;
					if (m.getParameterTypes().length == 0) {
						addMethodAsTrig(ctx, clz, m, menuGlobalTriggers, wrap, ADD_INSTANCE, "MadeGlobs", false);
					} else {

					}
				}
			}
			Collections.sort(menuGlobalTriggers, new TriggerSorter());
			int ps2 = menuGlobalTriggers.size();
			if (ps1 != ps2) {
				updateToolsMenu();
			}
		}
	}

	public static void addClassStaticMethods(Class clz) {
		List<TriggerForInstance> appMenuGlobalTriggers = Utility.appMenuGlobalTriggers;
		synchronized (appMenuGlobalTriggers) {
			DisplayContext ctx = getDisplayContext();
			int ps1 = appMenuGlobalTriggers.size();
			for (Method m : clz.getDeclaredMethods()) {
				if (ReflectUtils.isStatic(m)) {
					if (m.isSynthetic())
						continue;
					if (m.getParameterTypes().length == 0) {
						TriggerMenuFactory.addMethodAsTrig(ctx, clz, m, appMenuGlobalTriggers, null, ADD_STATIC, "TrigGlobally|", true);
					} else {
						TriggerMenuFactory.addMethodAsTrig(ctx, clz, m, objectContextMenuTriggers, null, ADD_STATIC, "TrigLocally|", true);
					}
				}
			}
			Collections.sort(appMenuGlobalTriggers, new TriggerSorter());
			int ps2 = appMenuGlobalTriggers.size();
			if (ps1 != ps2) {
				updateToolsMenu();
			}
		}
	}

	public static void updateToolsMenu() {
		List<TriggerForInstance> appMenuGlobalTriggers = Utility.appMenuGlobalTriggers;
		synchronized (appMenuGlobalTriggers) {
			if (toolsMenu == null)
				return;
			toolsMenu.removeAll();
			Collections.sort(appMenuGlobalTriggers, new TriggerSorter());
			for (TriggerForInstance tfi : appMenuGlobalTriggers) {
				addTriggerToPoppup(toolsMenu, null, tfi);
			}
		}
	}

	public static List<TriggerForInstance> getAppMenuGlobalMethods() {
		synchronized (appMenuGlobalTriggers) {
			return new ArrayList<TriggerForInstance>(appMenuGlobalTriggers);
		}
	}

	public static List<TriggerForMethod> getObjectGlobalMethods() {
		synchronized (objectContextMenuTriggers) {
			return new ArrayList<TriggerForMethod>(objectContextMenuTriggers);
		}
	}

	static {
		Utility.addSingletonMethods(new AssemberCacheGrabber());
		ReflectUtils.registerConverter(new UtilityConverter());
		Map<Class, ToFromKeyConverter> toFrmKeyCnv = getKeyConvMap(String.class);
		toFrmKeyCnv.put(Ident.class, new ToFromKeyConverter<Ident, String>(Ident.class, String.class) {

			@Override public String toKey(Ident toBecomeAString) {
				return toBecomeAString.getAbsUriString();
			}

			@Override public Ident fromKey(String title, Class further) {
				return new FreeIdent(title);
			}
		});
	}

	private static Map<Class, ToFromKeyConverter> getKeyConvMap(Class keyType) {
		Map<Class, ToFromKeyConverter> toFrmKeyCnv = toFrmKeyCnvMap.get(keyType);
		if (toFrmKeyCnv == null) {
			toFrmKeyCnv = new HashMap<Class, ToFromKeyConverter>();
			toFrmKeyCnvMap.put(keyType, toFrmKeyCnv);
		}
		return toFrmKeyCnv;
	}

	public static void addObjectFeatures(Object obj) {
		if (obj == null)
			return;
		if (obj instanceof Singleton) {
			Utility.addSingletonMethods((Singleton) obj);
		} else if (obj instanceof Class) {
			Utility.addClassStaticMethods((Class) obj);
		} else {
			if (obj instanceof UtilClass) {
				Class oc = obj.getClass();
				addClassStaticMethods(oc);
			}
		}
	}

	public static BrowserPanelGUI controlApp;
	public static BoxPanelSwitchableView theBoxPanelDisplayContext;
	public static NamedItemChooserPanel namedItemChooserPanel;
	public static CollectionEditorUtil collectionWatcher;
	public static DisplayContext selectedDisplaySontext;

	public LinkedList getBoxListFrom(Object key) {
		String strkey = key.toString();
		synchronized (displayLists) {
			LinkedList lst = displayLists.get(strkey);
			if (lst == null)
				lst = new LinkedList();
			displayLists.put(strkey, lst);
			return lst;
		}
	}

	static public Object getCachedComponent(Ident id) {
		NamedObjectCollection noc = getTreeBoxCollection();
		if (noc == null) {
			Debuggable.warn("NULL namedObjectCollection");
		}
		return noc.findBoxByName(id.toString());
	}

	static public void recordCreated(NamedObjectCollection noc, Ident id, Object comp) {
		BoxPanelSwitchableView boxPanelDisplayContext = getBoxPanelTabPane();
		try {
			recordCreated(noc.findOrCreateBox(id.toString(), comp));
		} catch (PropertyVetoException e) {
			Debuggable.printStackTrace(e);
			throw reThrowable(e);
		}
	}

	// public static FileMenu fileMenu = new FileMenu();
	public static JMenuBar appMenuBar0;
	public static FileMenu fileMenu;
	public static JFrame appFrame;

	public static JFrame getAppFrame() {
		if (appFrame == null) {
			if (appMenuBar0 != null) {
				appFrame = (JFrame) appMenuBar0.getTopLevelAncestor();
			}
		}
		return (JFrame) appFrame;
	}

	public static JMenuBar getMenuBar() {
		if (appMenuBar0 == null) {
			if (appFrame != null) {
				appMenuBar0 = appFrame.getJMenuBar();
			}
			if (appMenuBar0 == null) {
				appMenuBar0 = new JMenuBar();
			}
		}
		if (appFrame != null && appFrame.getJMenuBar() != appMenuBar0) {
			appFrame.setJMenuBar(appMenuBar0);
		}
		return appMenuBar0;
	}

	public static BoxPanelSwitchableView getBoxPanelTabPane() {
		if (theBoxPanelDisplayContext == null) {
			Debuggable.warn("NULL theBoxPanelDisplayContext");
		}
		return theBoxPanelDisplayContext;
	}

	/**
	 * The current ObjectNavigator being displayed
	 */
	static public NamedObjectCollection getTreeBoxCollection() {
		if (uiObjects == null) {
			Debuggable.warn("NULL uiObjects");
		}
		return uiObjects;
	}

	public static BrowsePanel getCurrentPOJOApp() {
		if (browserPanel == null) {
			Debuggable.warn("NULL browserPanel");
		}
		return browserPanel;
	}

	public static boolean ensureRunning() {
		if (browserPanel == null) {
			try {
				DemoBrowser.main(new String[0]);
			} catch (Exception e) {
				theLogger.error("ensureRunning() caught an exception", e);
			}
		}
		return true;
	}

	/**
	 * Returns the global objectsContext, or null if none has been set
	 */
	public static BrowserPanelGUI getCurrentContext() {
		if (controlApp == null) {
			Debuggable.warn("NULL controlApp");
		}
		return controlApp;
	}

	public static DisplayContext getDisplayContext() {
		if (selectedDisplaySontext != null)
			return selectedDisplaySontext;
		return controlApp;
	}

	/*
	 * public static void setDisplayContext(BrowserPanelGUI browserPanelGUI) {
	 * Debuggable.notImplemented();
	 * 
	 * }
	 */

	/**
	 * Sets the global objects context
	 */
	public static void setInstancesOfObjects(NamedObjectCollection newValue) {
		uiObjects = newValue;
	}

	/*
	 * public static void setDefaultContext(POJOCollectionWithSwizzler c) {
	 * context = c; }
	 * 
	 * public static POJOCollectionWithSwizzler getDefaultContext() { return
	 * context; }
	 */
	// public static POJOApp objectsContext = null;

	/*
	 * public static POJOBox addObject(Object object, boolean showASAP) {
	 * 
	 * if (object instanceof POJOBox) { return ((POJOBox) object); } try {
	 * BoxPanelSwitchableView boxPanelDisplayContext = getBoxPanelTabPane();
	 * return boxPanelDisplayContext.addObject(object, showASAP); } catch
	 * (Exception e) { theLogger.error("addObject", object, e); throw
	 * reThrowable(e); } }
	 */
	public static List getTriggersFromBeanInfo(BeanInfo beanInfo) {
		return EMPTYLIST;
	}

	private Utility() {
	}

	public static void registerEditors() {
		PropertyEditorManager.registerEditor(int.class, IntEditor.class);
		PropertyEditorManager.registerEditor(Integer.class, IntEditor.class);

		PropertyEditorManager.registerEditor(boolean.class, BooleanEditor.class);
		PropertyEditorManager.registerEditor(Boolean.class, BooleanEditor.class);

		PropertyEditorManager.registerEditor(Color.class, ColorEditor.class);

		PropertyEditorManager.registerEditor(Date.class, DateEditor.class);

		PropertyEditorManager.setEditorSearchPath(new String[] { AbstractCollectionBeanInfo.class.getPackage().getName() });
		// ClassFinder.getClasses(PropertyEditor);

		registerTabs(ClassCustomizer.class, Class.class);
		registerTabs(CollectionCustomizer.class, Collection.class);
		registerTabs(ThrowableCustomizer.class, Throwable.class);
		registerTabs(BasicObjectCustomizer.class, Throwable.class);
		registerTabs(ArrayContentsPanelTabFramer.class, Object[].class);

		registerPanels(RepoManagerPanel.class, Repo.class);
		registerPanels(ArrayContentsPanel.class, Object[].class);
		registerPanels(ObjectChoiceComboPanel.class, Class.class);
		registerPanels(ModelMatrixPanel.class, Model.class);

		registerCustomizer(ArrayContentsPanel.class, Object[].class);
		// registerCustomizer(IndexedCustomizer.class, List.class);
	}

	public static void registerTabs(Class class1, Class class2) {
		try {
			addTabFramers.add((AddTabFrames) newInstance(class1));
		} catch (InstantiationException e) {
			Debuggable.printStackTrace(e);
		} catch (IllegalAccessException e) {
			Debuggable.printStackTrace(e);
		}
	}

	private static void registerCustomizer(Class<? extends Customizer> customizer, Class<?>... clz) {
		FunctionalClassRegistry.addDelegateClass(Customizer.class, customizer, clz);
	}

	private static void registerPanels(Class customizer, Class<?>... clz) {
		FunctionalClassRegistry.addDelegateClass(Component.class, customizer, clz);
	}

	static {
		registerEditors();
	}

	public static void setBeanInfoSearchPath() {
		Introspector.setBeanInfoSearchPath(new String[] { AbstractCollectionBeanInfo.class.getPackage().getName() });
	}

	public static Object invokeFromUI(Object obj0, Method method) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return ReflectUtils.invokeOptional(obj0, method, new OptionalArg() {

			@Override public Object getArg(Class type) {
				Collection objs = getClipboard().findObjectsByType(type);
				if (objs.size() != 1) {
					throw new NoSuchElementException("" + type);
				}
				return objs.toArray()[0];
			}
		});
	}

	public static <T> T recast(Object obj, Class<T> objNeedsToBe) throws NoSuchConversionException {
		return recast(obj, objNeedsToBe, null);
	}

	public static <T> T recast(final Object val, Class<T> objNeedsToBe, LinkedList<Object> except) throws NoSuchConversionException {
		Object obj = val;
		if (obj == null)
			return null;
		if (obj instanceof Convertable) {
			Convertable cvt = (Convertable) obj;
			if (cvt.canConvert(objNeedsToBe)) {
				return cvt.convertTo(objNeedsToBe);
			}
		}
		if (objNeedsToBe.isInstance(obj)) {
			try {
				return (T) obj;
			} catch (Exception e) {
				throw reThrowable(e);
			}
		}
		obj = dref(val);
		if (obj != val) {
			Object res = recast(obj, objNeedsToBe, except);
			if (objNeedsToBe.isInstance(res))
				return (T) res;
			res = drefO(obj);
			if (objNeedsToBe.isInstance(res))
				return (T) res;
			obj = res;
		}
		if (obj instanceof String) {
			return (T) fromString((String) obj, objNeedsToBe);
		}
		return (T) obj;
	}

	static public boolean isToStringType(Class type) {
		return DisplayType.TOSTRING == getDisplayType(type);
	}

	public static Object fromString(Object title, Class type) throws NoSuchConversionException {
		Class keyClass = title.getClass();
		ToFromKeyConverter conv = getToFromConverter(type, keyClass);
		if (conv != null)
			return conv.fromKey(title, type);

		if (!isToStringType(type)) {
			if (title instanceof String) {
				String stitle = (String) title;
				BT box = getTreeBoxCollection().findBoxByName(stitle);
				if (box == null) {
					throw new NoSuchConversionException(type + " findBoxByName " + title);
				}
				return box.convertTo(type);
			}
		}

		if (type == keyClass)
			return title;
		type = ReflectUtils.nonPrimitiveTypeFor(type);

		NoSuchConversionException cce = null;
		Class searchType = type;
		while (searchType != null) {
			for (Method m : searchType.getDeclaredMethods()) {
				if (m.getReturnType() == type) {
					Class[] pt = m.getParameterTypes();
					if (pt != null && pt.length == 1 && Modifier.isStatic(m.getModifiers())) {
						try {
							if (TypeAssignable.CASTING_ONLY.declaresConverts(title, keyClass, pt[0]) == TypeAssignable.WONT)
								continue;
							m.setAccessible(true);
							return m.invoke(null, title);
						} catch (Throwable e) {
							cce = new NoSuchConversionException(type + " " + m.getName() + " " + title, e);
						}
					}
				}
			}
			searchType = searchType.getSuperclass();
		}
		if (cce != null)
			throw cce;
		throw new NoSuchConversionException(type + " fromString " + title);
	}

	public static RuntimeException reThrowable(Throwable e) {
		if (true)
			return Debuggable.reThrowable(e);
		if (e instanceof InvocationTargetException)
			e = e.getCause();
		if (e instanceof RuntimeException)
			return (RuntimeException) e;
		if (e instanceof Error)
			throw (Error) e;
		return new RuntimeException(e);
	}

	public static BeanInfo getBeanInfo(Class c, final Object toBindTo) throws IntrospectionException {
		return getBeanInfo(c, false, toBindTo);
	}

	public static BeanInfo getBeanInfo(Class c, boolean onlythisClass, final Object toBindTo) throws IntrospectionException {
		return getBeanInfo(c, onlythisClass, null, ADD_ALL);
	}

	public static BeanInfo getBeanInfo(final Class c, boolean onlythisClass, final Object toBindTo, TriggerFilter includeFields) throws IntrospectionException {
		final BeanInfo bi = getBeanInfoNoF(c, onlythisClass);
		return new SimplePOJOInfo(c, null, bi, toBindTo);
	}

	public static BeanInfo getBeanInfoNoF(Class c, boolean onlythisClass) throws IntrospectionException {
		Class stopAtClass = null;// Object.class;// c.getSuperclass();
		if (onlythisClass) {
			stopAtClass = c.getSuperclass();
			if (stopAtClass == null && !c.isInterface()) {
				stopAtClass = Object.class;
			}
		}
		if (stopAtClass == c) {
			stopAtClass = null;
		}
		boolean wasInPing = isInClassLoadPing();
		try {
			inClassLoadingPing.set(Boolean.TRUE);
			BeanInfo bi = Introspector.getBeanInfo(c, stopAtClass);
			return bi;
		} finally {
			inClassLoadingPing.set(wasInPing);
		}
	}

	public static boolean isInClassLoadPing() {
		return Boolean.TRUE.equals(inClassLoadingPing.get());
	}

	public static void isInClassLoadPing(boolean now) {
		inClassLoadingPing.set(now);
	}

	public static BeanInfo getPOJOInfo(Class<? extends Object> c, int useAllBeaninfo) throws IntrospectionException {
		return Introspector.getBeanInfo(c, useAllBeaninfo);
	}

	public static String loadFile(File file) throws IOException {
		FileInputStream fileIn = new FileInputStream(file);
		ByteArrayOutputStream stringOut = new ByteArrayOutputStream();
		copyStream(fileIn, stringOut);
		fileIn.close();
		return new String(stringOut.toByteArray());
	}

	public static String loadURL(URL url) throws Exception {
		InputStream in = url.openStream();
		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();

		copyStream(in, bytesOut);
		in.close();
		bytesOut.close();
		return new String(bytesOut.toByteArray());
	}

	public static void saveFile(File file, String contents) throws IOException {
		FileOutputStream fileOut = new FileOutputStream(file);
		PrintWriter writer = new PrintWriter(fileOut);
		writer.print(contents);
		writer.close();
	}

	public static void copyStream(InputStream from, OutputStream to) throws IOException {
		int i = from.read();
		while (i > -1) {
			to.write(i);
			i = from.read();
		}
	}

	public static boolean isEqual(Object o1, Object o2) {
		if (o1 == o2)
			return true;
		if (o1 == null || o2 == null)
			return false;
		return (o1.equals(o2));
	}

	/**
	 * Checks if the given object is null, or if toString().trim() == "".
	 */
	public static boolean isEmpty(Object o) {
		if (o == null) {
			return true;
		} else {
			return o.toString().trim().equals("");
		}
	}

	/**
	 * Moves the given window to the center of the screen
	 */
	public static void centerWindow(Window win) {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dim = win.getSize();
		win.setLocation((screen.width / 2) - (dim.width / 2), (screen.height / 2) - (dim.height / 2));
	}

	public static Dimension getConstrainedDimension(Dimension min, Dimension pref, Dimension max) {
		Dimension result = getMinDimension(max, getMaxDimension(min, pref));
		return result;
	}

	public static Dimension getMaxDimension(Dimension a, Dimension b) {
		return new Dimension(Math.max(a.width, b.width), Math.max(a.height, b.height));
	}

	public static Dimension getMinDimension(Dimension a, Dimension b) {
		return new Dimension(Math.min(a.width, b.width), Math.min(a.height, b.height));
	}

	public static String getShortClassName(Class c) {
		if (c.isArray()) {
			return getShortClassName(c.getComponentType()) + "Array";
		}
		String name = c.getName();
		int i = name.lastIndexOf(".");
		if (i == -1)
			return name;
		else
			return name.substring(i + 1);
	}

	/**
	 * Replace all occurences of 'a' in string 's' with 'b'
	 */
	public static String replace(String s, String a, String b) {

		int aLength = a.length();
		int bLength = b.length();
		StringBuffer buf = new StringBuffer(s);

		// how much length changes after each replacement
		int dif = b.length() - a.length();

		int len = s.length();
		int i = buf.toString().indexOf(a);
		int startFrom = 0;
		while (i != -1) {
			buf.replace(i, i + aLength, b);
			startFrom = i + bLength;
			i = buf.toString().indexOf(a, startFrom);
		}

		return buf.toString();
	}

	public static byte[] serialize(Object o) throws Exception {
		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
		ObjectOutputStream objectOut = new ObjectOutputStream(bytesOut);
		objectOut.writeObject(o);
		objectOut.close();
		return bytesOut.toByteArray();
	}

	public static Object deserialize(byte[] bytes) throws Exception {
		ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
		ObjectInputStream objectIn = new ObjectInputStream(bytesIn);
		Object o = objectIn.readObject();
		objectIn.close();
		return o;
	}

	/**
	 * Generates a default name for the given object, while will be something
	 * like "Button1", "Button2", etc.
	 * 
	 * @param nameIndex
	 */
	public static String generateUniqueName(Object object, Map<String, BT> checkAgainst) {
		return generateUniqueName_sug(object, null, checkAgainst);
	}

	public static String generateUniqueName(Object object, String suggestedName, Map<String, BT> checkAgainst) {
		String newName = generateUniqueName_sug(object, suggestedName, checkAgainst);
		if (suggestedName != null && !suggestedName.equals(newName)) {
			Debuggable.warn("did not get suggested name : " + suggestedName + " isntead got " + newName + " for " + object);
		}
		return newName;
	}

	public static String generateUniqueName_sug(Object object, String suggestedName, Map<String, BT> checkAgainst) {
		if (object == null)
			return "<null>";
		if (object instanceof Class) {
			return ((Class) object).getCanonicalName();
		}
		if (object instanceof KnownComponent) {
			String str = ((KnownComponent) object).getShortLabel();
			if (str != null) {

				if (checkAgainst != null) {
					BT other = null;
					other = checkAgainst.get(str);
					if (other == null)
						return str;
					if (other.representsObject(object))
						return str;
					if (str != null)
						return str;
				}
				// conflict!
			}
		}

		if (checkAgainst == null) {
			if (suggestedName == null)
				return getDefaultName(object);
			return suggestedName + getDefaultName(object);
		}

		String className = suggestedName;
		String name;
		int counter = 1;
		if (className == null) {
			className = Utility.getShortClassName(object.getClass());
			name = className + counter;
		} else {
			name = suggestedName;
		}

		boolean done = false;

		while (!done) {
			Object otherPOJO = checkAgainst.get(name);
			if (otherPOJO == null) {
				done = true;
			} else {
				++counter;
				name = className + counter;
			}
		}
		return name;
	}

	public static int identityHashCode(Object object) {
		return System.identityHashCode(object);
	}

	public static void setLastResult(Object whereFrom, Object obj, Class expected) {
		GetSetObject pnl = Utility.getPanelFor(expected);
		if (pnl != null) {
			try {
				pnl.setObject(obj);
			} catch (InvocationTargetException e) {
				theLogger.error("" + pnl, e);
			}
		} else {
			theLogger.info("result from " + whereFrom + " was " + obj);
			if (obj != null) {
				DisplayType attachType = getDisplayType(expected);
				getCurrentPOJOApp().showScreenBox(null, obj);
			}
		}

	}

	static Hashtable<Class, GetSetObject> panelsFor = new Hashtable<Class, GetSetObject>();

	private static GetSetObject getPanelFor(Class expected) {
		GetSetObject gso = panelsFor.get(expected);
		if (gso != null)
			return gso;
		if (isToStringType(expected)) {
			return new GetSetObject() {

				@Override public void setObject(Object object) throws InvocationTargetException {
					getCurrentPOJOApp().showMessage(makeToString(object));
				}

				@Override public Object getValue() {
					return getCurrentPOJOApp().getMessage();
				}
			};
		}
		return null;
	}

	public static Class<? extends Customizer> findCustomizerClass(Class objClass) {
		try {
			Class<? extends Customizer> c = FunctionalClassRegistry.findImplmentingForMatch(Customizer.class, objClass);
			if (c != null)
				return c;
			return makeCustomizerFromEditor(objClass);
		} catch (Throwable e) {
			theLogger.error(" " + objClass, e);
			return null;
		}
	}

	private static Class makeCustomizerFromEditor(Class objClass) {
		Class pe = findEditorClass(objClass);
		if (pe != null) {
			return UseEditor.class;
		}
		return null;
	}

	public static boolean usePropertyEditorManager = false;

	/**
	 * Locate a value editor for a given target type.
	 * 
	 * @param targetType
	 *            The Class object for the type to be edited
	 * @return An editor object for the given target class. The result is null
	 *         if no suitable editor can be found.
	 */
	public static PropertyEditor findEditor(Class targetType) {

		PropertyEditor ped = null;
		boolean wasInClassloaderPing = isInClassLoadPing();
		try {
			if (usePropertyEditorManager) {
				isInClassLoadPing(true);
				ped = PropertyEditorManager.findEditor(targetType);
				if (ped != null)
					return ped;
			}
		} catch (Throwable e) {
			// PropertyEditorManager is a wild and untamed thing
		} finally {
			isInClassLoadPing(wasInClassloaderPing);
		}
		Class<? extends PropertyEditor> pe = FunctionalClassRegistry.findImplmentingForMatch(PropertyEditor.class, targetType);
		if (pe == null || !PropertyEditor.class.isAssignableFrom(pe)) {
			return null;
		}
		try {
			ped = pe.newInstance();
			return ped;
		} catch (Throwable e) {
			Debuggable.UnhandledException(e);
			return null;
		}
	}

	public static Class findEditorClass(Class targetType) {

		PropertyEditor ped = null;
		boolean wasInClassloaderPing = isInClassLoadPing();
		try {
			if (usePropertyEditorManager) {
				isInClassLoadPing(true);
				ped = PropertyEditorManager.findEditor(targetType);
			}
		} catch (Throwable e) {
			// PropertyEditorManager is a wild and untamed thing
		} finally {
			isInClassLoadPing(wasInClassloaderPing);
		}
		Class<? extends PropertyEditor> pe = FunctionalClassRegistry.findImplmentingForMatch(PropertyEditor.class, targetType);
		if (pe == null || !PropertyEditor.class.isAssignableFrom(pe)) {
			if (ped != null)
				return ped.getClass();
		}
		return pe;
	}

	public static Collection<Class> findPanelClasses(Class targetType) {
		HashSet<Class> panelClass = new HashSet<Class>();
		PropertyEditor ped = null;
		boolean wasInClassloaderPing = isInClassLoadPing();
		try {
			if (usePropertyEditorManager) {
				isInClassLoadPing(true);
				ped = PropertyEditorManager.findEditor(targetType);
				if (ped != null) {
					panelClass.add(ped.getClass());
				}
			}
		} catch (Throwable e) {
			// PropertyEditorManager is a wild and untamed thing
		} finally {
			isInClassLoadPing(wasInClassloaderPing);
		}
		CollectionSetUtils.addIfNew(panelClass, FunctionalClassRegistry.findImplmentingForMatch(PropertyEditor.class, targetType), false);
		CollectionSetUtils.addIfNew(panelClass, FunctionalClassRegistry.findImplmentingForMatch(Customizer.class, targetType), false);
		CollectionSetUtils.addIfNew(panelClass, FunctionalClassRegistry.findImplmentingForMatch(Component.class, targetType), false);
		CollectionSetUtils.addIfNew(panelClass, findCustomizerClass(targetType), false);
		CollectionSetUtils.addIfNew(panelClass, makeCustomizerFromEditor(targetType), false);
		return panelClass;
	}

	public static List getSearchableClassList() {
		Debuggable.notImplemented();
		return PromiscuousClassUtils.getInstalledClasses();
	}

	public static <T> T newInstance(Class<T> customizerClass) throws InstantiationException, IllegalAccessException {
		for (Constructor cons : customizerClass.getDeclaredConstructors()) {
			if (cons.getParameterTypes().length == 0) {
				cons.setAccessible(true);

				try {
					return (T) cons.newInstance();
				} catch (Exception e) {
				}
			}
		}
		for (Constructor cons : customizerClass.getDeclaredConstructors()) {
			if (cons.getParameterTypes().length == 0) {
				cons.setAccessible(true);

				try {
					return (T) cons.newInstance();
				} catch (Exception e) {
				}
			}
		}
		T customizer = (T) customizerClass.newInstance();
		return customizer;
	}

	public static Action getConfigObject(String path, Class<Action> class1) {
		Debuggable.notImplemented();
		return null;
	}

	public static FileObject getConfigFile(String path) {
		Debuggable.notImplemented();
		Debuggable.notImplemented();
		return null;
	}

	public static HashMap<Object, JPanel> gpp = new HashMap();

	public static class AlreadyLooking extends JPanel {

	}

	public static AlreadyLooking alreadyLooking = new AlreadyLooking();

	public static JPanel getPropertiesPanel(Object object) {
		object = dref(object, object);
		JPanel view = (JPanel) gpp.get(object);
		if (view instanceof AlreadyLooking) {
			return null;
		}
		if (view != null)
			return view;
		gpp.put(object, alreadyLooking);
		Class objClass = object.getClass();
		Class<? extends Customizer> customizerClass = getCustomizerClassForClass(objClass);
		Customizer customizer;
		try {
			customizer = Utility.newInstance(customizerClass);
		} catch (Throwable e) {
			customizer = new LargeObjectView(Utility.getCurrentContext(), object);
		}
		customizer.setObject(object);
		if (customizer instanceof JPanel)
			view = (JPanel) customizer;
		else {
			theLogger.warn("customizer is not a Component " + customizer);
			view = new LargeObjectView(Utility.getCurrentContext(), object);
		}
		gpp.put(object, view);
		return view;
	}

	public static Class getCustomizerClassForClass(Class objClass) {
		Class<? extends Customizer> customizerClass = Utility.findCustomizerClass(objClass);
		if (objClass == Class.class && customizerClass != LargeObjectView.class) {
			Debuggable.warn("Broken Class Cusomizer = " + customizerClass);
			customizerClass = Utility.findCustomizerClass(objClass);
			customizerClass = LargeObjectView.class;
		} else {
			customizerClass = LargeObjectView.class;
		}
		return customizerClass;
	}

	public static boolean stringsEqual(String s1, String s2) {
		if (s1 == s2)
			return true;

		if (s1 == null || null == s2)
			return false;

		return s1.equalsIgnoreCase(s2);
	}

	public static Collection<BT> boxObjects(Iterable<Object> pojoCollectionObjects) {
		LinkedList<BT> objects = new LinkedList<BT>();
		for (Object pojo : pojoCollectionObjects) {
			objects.add(asWrapped(pojo));
		}
		return objects;
	}

	public static synchronized boolean recordCreated(Object box) {
		if (box instanceof BT) {
			return recordBCreated((BT) box);
		}
		if (allBoxes.containsKey(box))
			return false;
		allBoxes.put(box, null);
		return true;
	}

	public static synchronized boolean recordBCreated(BT box) {
		if (allBoxes.containsKey(box))
			return false;
		allBoxes.put(box, box);
		return true;
	}

	public static BT asWrapped(Object pojo) {
		if (pojo instanceof BT)
			return (BT) pojo;
		if (pojo instanceof IGetBox)
			return ((IGetBox) pojo).getBT();
		return uiObjects.findOrCreateBox(pojo);
	}

	public static Box asBoxed(Object pojo) {
		if (pojo instanceof Box)
			return (Box) pojo;
		if (pojo instanceof IGetBox.NotWrapper)
			return ((IGetBox.NotWrapper) pojo).asBox();

		if (pojo instanceof IGetBox)
			return ((IGetBox) pojo).getBT().asBox();
		return asWrapped(pojo).asBox();
	}

	public static Collection<Object> unboxObjects(Iterable<? extends BT> pojoCollectionObjects) {
		LinkedList<Object> objects = new LinkedList<Object>();
		for (BT pojo : pojoCollectionObjects) {
			objects.add(unboxObject(pojo));
		}
		return objects;
	}

	private static Object unboxObject(BT pojo) {
		return pojo.getValue();
	}

	/*
	 * public static POJOBox addObject(Object obj) { return addObject(obj,
	 * false); }
	 */
	public static UserResult showError(DisplayContext context, String msg, Throwable error) {
		if (msg == null)
			msg = error.getMessage();
		try {
			if (context == null) {
				JPanel pnl = new org.appdapter.gui.swing.ErrorDialog(msg, error);
				pnl.show();
				return Utility.asUserResult(pnl);
			} else {
				Utility.browserPanel.showScreenBox(error); // @temp
				return null;
			}
		} catch (Throwable err2) {
			ErrorDialog pnl = new ErrorDialog("A new error occurred while trying to display the original error '" + error + "'!", err2);
			pnl.show();
			return Utility.asUserResult(pnl);
		}

	}

	public static String getDefaultName(Object object) {
		String title = hasDefaultName(object);
		if (title == null) {
			return object.getClass().getCanonicalName() + "@" + identityHashCode(object);
		}
		return title;
	}

	public static String getUniqueName(Object object) {
		String title = hasDefaultName(object);
		if (title == null) {
			BT newBox = Utility.getTreeBoxCollection().findOrCreateBox(object);
			title = newBox.getUniqueName();
		}
		return title;
	}

	public static boolean isTitled(String title) {
		return title != null && title != NamedObjectCollection.MISSING_COMPONENT && !title.equals("<null>") && title.length() > 0;
	}

	public static String getUniqueName(Object object0, Box boxed, NamedObjectCollection noc) {

		Object object = object0;

		if (object == null) {
			object = dref(boxed, false);
		}

		object = dref(object, false);
		String title = hasDefaultName(object);
		if (title == null) {
			BT newBox = null;
			if (boxed instanceof BT) {
				newBox = (BT) boxed;
			} else if (object0 instanceof BT) {
				newBox = (BT) object0;
			}
			if (newBox != null) {
				title = newBox.getUniqueName();
			} else {
				if (noc != null) {
					title = noc.getTitleOf(object);
				}
			}
			if (!isTitled(title)) {
				title = getUniqueName(object);
			}
		}
		return title;
	}

	public static String getEasyDefaultName(Object object) {
		if (object == null)
			return "<null>";
		Class type = object.getClass();
		if (type == Class.class)
			return ((Class) object).getName();
		if (type == String.class)
			return ((String) object);
		if (isToStringType(type))
			return makeToString(object);
		return null;
	}

	public static String makeToString(Object object) {
		ToFromKeyConverter<?, String> conv = getToFromConverter(object.getClass(), String.class);
		if (conv != null)
			return conv.toKeyFromObject(object);
		return "" + object;
	}

	static public <T, K> ToFromKeyConverter<T, K> getToFromConverter(Class<T> valueClazz, Class<K> key) {
		Map<Class, ToFromKeyConverter> toFrmKeyCnv = getKeyConvMap(key);
		synchronized (toFrmKeyCnv) {
			for (Class c : toFrmKeyCnv.keySet()) {
				if (c.isAssignableFrom(valueClazz)) {
					return toFrmKeyCnv.get(c);
				}
			}
		}
		return null;
	}

	public static String hasDefaultName(Object object) {
		if (object == null)
			return "<null>";
		Class type = object.getClass();
		if (type == Class.class)
			return ((Class) object).getName();
		if (type == String.class)
			return ((String) object);
		if (isToStringType(type))
			return makeToString(object);
		if (object instanceof BT) {
			return ((BT) object).getUniqueName();
		}
		if (uiObjects.containsObject(object)) {
			String title = uiObjects.getTitleOf(object);
			if (isTitled(title)) {
				return title;
			}
		}
		if (object instanceof KnownComponent) {
			KnownComponent kc = (KnownComponent) object;
			Ident id = kc.getIdent();
			if (id != null) {
				Object f = JenaLiteralUtils.findComponent(id, object.getClass());
				if (f == object) {
					return id.getLocalName();
				}
				return id.getAbsUriString();
			}
		}
		return null;
	}

	static public org.appdapter.gui.api.DisplayType getDisplayType(Class expected) {
		if (expected.isPrimitive()) {
			return DisplayType.TOSTRING;
		}
		if (Number.class.isAssignableFrom(expected)) {
			return DisplayType.TOSTRING;
		}
		if (Enum.class.isAssignableFrom(expected)) {
			return DisplayType.TOSTRING;
		}
		if (expected == String.class || CharSequence.class.isAssignableFrom(expected)) {
			return DisplayType.TOSTRING;
		}
		if (expected == Boolean.class) {
			return DisplayType.TOSTRING;
		}
		if (getToFromConverter(expected, String.class) != null) {
			return DisplayType.TOSTRING;
		}
		return DisplayType.PANEL;
	}

	public static Class getClassNullOk(Object object) {
		if (object == null)
			return null;
		object = dref(object);
		return object.getClass();
	}

	public static Object dref(Object value, Object onNull) {
		return dref(value, onNull, 4, true);
	}

	public static Object dref(Object value) {
		return dref(value, true);
	}

	public static Object drefO(Object value) {
		return dref(value, false);
	}

	public static Object dref(Object value, boolean onlyBTAndPanels) {
		return dref(value, value, 4, onlyBTAndPanels);
	}

	public static Object dref(Object value, Object onNull, int depth, boolean onlyBTAndPanels) {
		if (value == null)
			return onNull;
		if (depth < 0) {
			return value;
		}
		Object derefd = null;

		try {
			if (value instanceof GetObject && value instanceof JComponent) {
				derefd = ((GetObject) value).getValue();
			} else if (value instanceof PropertyEditor) {
				derefd = ((PropertyEditor) value).getValue();
			} else if (value instanceof BT) {
				derefd = ((BT) value).getValue();
			} else if (value instanceof IGetBox) {
				derefd = ((IGetBox) value).getBT();
			} else if (!onlyBTAndPanels) {
				if (value instanceof GetObject) {
					derefd = ((GetObject) value).getValue();
				} else if (value instanceof WrapperValue) {
					derefd = ((WrapperValue) value).reallyGetValue();
				}
			}
		} catch (Throwable t) {

		}
		if (derefd != null && derefd != value) {
			value = dref(derefd, derefd, --depth, onlyBTAndPanels);
		}
		if (value == null)
			return onNull;
		return value;
	}

	public static <T> T[] ChoiceOf(T... o) {
		return o;
	}

	public static <T> T first(T... o) {
		for (T t : o) {
			if (t != null)
				return t;
		}
		return null;
	}

	public static java.net.URL getResource(String filename) {
		return get1Resource(CollectionSetUtils.arrayOf("", "icons/"), filename, CollectionSetUtils.arrayOf("", ".gif", ".jpg", ".ico"));
	}

	public static java.net.URL get1Resource(String[] prefix, String filename, String[] suffix) {
		java.net.URL url = null;
		for (String pfx : prefix)
			for (String sfx : suffix) {
				String tryFileName = pfx + filename + sfx;
				url = org.appdapter.gui.swing.IconView.class.getResource(tryFileName);
				if (url != null)
					return url;

				url = ClassLoaderUtils.getFileResource(ClassLoaderUtils.ALL_RESOURCE_CLASSLOADER_TYPES, tryFileName);
				if (url != null)
					return url;
			}
		return null;
	}

	public static UserResult asUserResult(Object obj) {
		if (obj instanceof UserResult)
			return (UserResult) obj;
		return UserResult.SUCCESS;
	}

	public static boolean boxesRepresentSame(Box b, Object userObject) {
		if (userObject == b)
			return true;
		Object b0 = dref(b);
		Object uo0 = dref(userObject);
		if (b == null || b0 == null)
			return uo0 == null;
		if (uo0 == b0)
			return true;
		Object b00 = drefO(b0);
		Object uo00 = drefO(uo0);
		if (uo00 == b00)
			return true;
		return false;
	}

	public static String properCase(String shortClassName) {
		if (shortClassName.contains(" ")) {
			StringBuffer buffer = new StringBuffer();
			for (String sp : shortClassName.split(" ")) {
				buffer.append(properCase(sp));
			}
			return buffer.toString();
		}
		return shortClassName.substring(0, 1).toUpperCase() + shortClassName.substring(1);
	}

	public static String spaceCase(String shortClassName) {
		boolean wasUpper = true;
		StringBuffer newName = new StringBuffer();
		for (char c : shortClassName.toCharArray()) {
			if (Character.isUpperCase(c)) {
				if (!wasUpper) {
					newName.append(" ");
				}
				wasUpper = true;
			} else {
				wasUpper = false;
			}
			newName.append(c);
		}
		return newName.toString();
	}

	public static String makeTooltipText(Object gv) {
		gv = dref(gv);
		if (gv == null) {
			gv = "<NULL>";
		} else {
			ToolTipManager man = ToolTipManager.sharedInstance();
			man.setInitialDelay(100);
			// man.setLightWeightPopupEnabled(!man.isLightWeightPopupEnabled());
			final String oc;
			if (gv instanceof Class) {
				oc = ((Class) gv).getCanonicalName();
			} else {
				oc = Debuggable.toInfoStringO(gv);
			}
			gv = Utility.getShortClassName(gv.getClass()) + ".this.toString: " + oc;
		}
		return "" + gv;
	}

	static NamedObjectCollection clipboardCollection = new BoxedCollectionImpl("Clipboard", null);

	public static NamedObjectCollection getClipboard() {
		return clipboardCollection;
	}

	public static boolean isOSGi() {
		ClassLoader cl = PromiscuousClassUtils.getCallerClassLoaderOrCurrent();
		if (cl != null) {
			Class clz = cl.getClass();
			Class dc = clz.getDeclaringClass();
			if (dc == null) // there is an situation that can cause this on some
							// JVMs
				dc = clz;
			String cn = dc.getCanonicalName();
			if (cn.contains("Bundle")) {
				if (true)
					return false; // for testing if OSGi loading is safe yet
				return true;
			}
		}
		return false;
	}

	public static Collection<PropertyDescriptor> getProperties(Object object) throws IntrospectionException {
		final Class beanClass = object.getClass();
		PropertyDescriptor[] pdsa = getBeanInfo(beanClass, false).getPropertyDescriptors();
		return getProperties(object, beanClass, pdsa);
	}

	public static Collection<PropertyDescriptor> getProperties(Object object, Class beanClass, PropertyDescriptor[] pdsa) throws IntrospectionException {
		Map<String, PropertyDescriptor> props = new HashMap<String, PropertyDescriptor>();
		if (pdsa != null) {
			for (PropertyDescriptor p : pdsa) {
				props.put(p.getName().toLowerCase(), p);
			}
		}
		int fnum = -1;
		for (final Field f : ReflectUtils.getAllFields(beanClass)) {
			fnum++;
			String propName = PropertyDescriptorForField.clipPropertyNameMethod(f.getName(), "my").toLowerCase();
			PropertyDescriptor pd = props.get(propName);
			if (pd == null) {
				pd = PropertyDescriptorForField.findOrCreate(f).makePD(object);
				props.put(pd.getName().toLowerCase(), pd);
			}
		}
		Collection<Method> ml = ReflectUtils.getAllMethods(beanClass);
		for (Method m : ml) {
			String propName = PropertyDescriptorForField.clipPropertyNameMethod(m.getName(), "is", "get", "set").toLowerCase();
			PropertyDescriptor pd = props.get(propName);
			Class[] pts = m.getParameterTypes();
			int ptsl = pts.length;
			if (pd != null) {
				Class propType = pd.getPropertyType();
				try {
					if (ptsl == 0 && ReflectUtils.isSameType(m.getReturnType(), propType)) {
						pd.setReadMethod(m);
						continue;
					} else {
						if (ptsl == 1 && m.getReturnType() == void.class && ReflectUtils.isSameType(pts[0], propType)) {
							pd.setWriteMethod(m);
							continue;
						}
					}
				} catch (Throwable t) {
					Debuggable.printStackTrace(t);
					throw reThrowable(t);

				}
			}
		}
		return props.values();
	}

	public static boolean instanceOf(Object value, Class clz) {
		if (clz == Stringable) {
			return value == null || isToStringType(value.getClass());
		}
		if (clz.isInstance(value))
			return true;
		return false;
	}

	public static boolean instanceOfAny(Object value, Class... clss) {
		for (Class c : clss) {
			if (instanceOf(value, c))
				return true;
		}
		return false;
	}

	public static BT asBTNoCreate(Object obj) {
		if (obj instanceof BT) {
			return (BT) obj;
		}
		if (obj instanceof IGetBox) {
			return ((IGetBox) obj).getBT();
		}
		return null;
	}

	static public Icon getIcon(Class info) {
		try {
			return getIcon(Utility.getBeanInfo(info, null));
		} catch (Throwable e) {
			Debuggable.printStackTrace(e);
			return new UnknownIcon();
		}
	}

	/**
	 * Returns an Icon for this object, determined using BeanInfo. If no icon
	 * was found a default icon will be returned.
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

	public static ImageIcon getIcon(String string) {
		return new ImageIcon(getImage(string));
	}

	public static Image getImage(String string) {
		return getImage(Utility.getResource(string));
	}

	public static Image getImage(URL uri) {
		try {
			return ImageIO.read(uri);
		} catch (IOException e) {
			Debuggable.printStackTrace(e);
			return null;
		}
	}

	public static java.net.URL getResource(final Class c, String name) {
		ClassLoader cl = ClassLoaderUtils.getClassLoader(c);
		if (cl == null) {
			// A system class.
			return ClassLoader.getSystemResource(name);
		}
		return cl.getResource(name);
	}

	public static java.awt.Image loadImage(final Class c, final String resourceName) {
		try {
			java.awt.image.ImageProducer ip = (java.awt.image.ImageProducer) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {
				public Object run() {
					java.net.URL url;
					if ((url = getResource(c, resourceName)) == null) {
						return null;
					} else {
						try {
							return url.getContent();
						} catch (java.io.IOException ioe) {
							return null;
						}
					}
				}
			});

			if (ip == null)
				return null;
			java.awt.Toolkit tk = java.awt.Toolkit.getDefaultToolkit();
			return tk.createImage(ip);
		} catch (Exception ex) {
			return null;
		}
	}

	public static Collection<Trigger> getGlobalStaticTriggers(DisplayContext ctx, Class cls, WrapperValue poj) {
		ArrayList<Trigger> triggersFound = new ArrayList<Trigger>();
		for (TriggerForMethod trigc : getObjectGlobalMethods()) {
			if (trigc.appliesTarget(cls))
				triggersFound.add(trigc.createTrigger("G-ST|", ctx, poj));
		}
		return triggersFound;
	}

}