package org.appdapter.gui.api;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.beans.BeanInfo;
import java.beans.Customizer;
import java.beans.IntrospectionException;
import java.beans.Introspector;  
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSpinner.DateEditor;
import javax.tools.FileObject;

import org.appdapter.api.trigger.AddTabFrames;
import org.appdapter.api.trigger.BT;
import org.appdapter.api.trigger.BoxPanelSwitchableView;
import org.appdapter.api.trigger.BrowserPanelGUI;
import org.appdapter.api.trigger.Convertable;
import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.api.trigger.DisplayType;
import org.appdapter.api.trigger.GetObject;
import org.appdapter.api.trigger.NamedObjectCollection;
import org.appdapter.api.trigger.UserResult;
import org.appdapter.core.component.KnownComponent;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.name.Ident;
import org.appdapter.core.store.Repo;
import org.appdapter.gui.box.ScreenBoxImpl;
import org.appdapter.gui.browse.BrowsePanel;
import org.appdapter.gui.browse.CollectionEditorUtil;
import org.appdapter.gui.browse.CollectionEditorUtil.FileMenu;
import org.appdapter.gui.browse.NamedItemChooserPanel;
import org.appdapter.gui.demo.DemoBrowser;
import org.appdapter.gui.editors.AbstractCollectionBeanInfo;
import org.appdapter.gui.editors.BooleanEditor;
import org.appdapter.gui.editors.ColorEditor;
import org.appdapter.gui.editors.IntEditor;
import org.appdapter.gui.editors.LargeObjectView;
import org.appdapter.gui.editors.LargeObjectView.BasicObjectCustomizer;
import org.appdapter.gui.editors.LargeObjectView.ClassCustomizer;
import org.appdapter.gui.editors.LargeObjectView.CollectionCustomizer;
import org.appdapter.gui.editors.LargeObjectView.ThrowableCustomizer;
import org.appdapter.gui.repo.RepoManagerPanel;
import org.appdapter.gui.swing.ErrorDialog;
import org.appdapter.gui.swing.ObjectChoice;
import org.appdapter.gui.util.CollectionSetUtils;
import org.appdapter.gui.util.PromiscuousClassUtils;
import org.slf4j.Logger;

//import sun.beans.editors.ColorEditor;
//import sun.beans.editors.IntEditor;

public class Utility {

	public static Logger theLogger = org.slf4j.LoggerFactory.getLogger(Utility.class);
	private static final Class[] CLASS0 = new Class[0];

	// ==== Instance variables ==========
	public static BrowsePanel browserPanel;
	public static NamedObjectCollection uiObjects = new NamedObjectCollectionImpl("All UI Objects", null);
	public static BrowserPanelGUI controlApp;
	public static BoxPanelSwitchableView theBoxPanelDisplayContext;
	public static NamedItemChooserPanel namedItemChooserPanel;
	public static CollectionEditorUtil collectionWatcher;
	public static DisplayContext selectedDisplaySontext;

	static HashMap<String, LinkedList> displayLists = new HashMap<String, LinkedList>();

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
		NamedObjectCollection namedObjectCollection = getToplevelBoxCollection();
		if (namedObjectCollection == null) {
			Debuggable.warn("NULL namedObjectCollection");
		}
		return namedObjectCollection.findBoxByName(id.toString());
	}

	static public void putCachedComponent(Ident id, Object comp) {
		BoxPanelSwitchableView boxPanelDisplayContext = getBoxPanelTabPane();
		try {
			recordCreated(uiObjects.findOrCreateBox(id.toString(), comp));
		} catch (PropertyVetoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw reThrowable(e);
		}
	}

	//public static FileMenu fileMenu = new FileMenu();
	public static JMenuBar appMenuBar0;
	public static FileMenu fileMenu;
	private static JFrame appFrame;

	public static JFrame getAppFrame() {
		if (appFrame == null) {
			appFrame = (JFrame) getMenuBar().getTopLevelAncestor();
		}
		return (JFrame) appFrame;
	}

	public static JMenuBar getMenuBar() {
		if (appMenuBar0 != null)
			appMenuBar0 = new JMenuBar();
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
	static public NamedObjectCollection getToplevelBoxCollection() {
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

	public static BrowserPanelGUI getDisplayContext() {
		return controlApp;
	}

	/*public static void setDisplayContext(BrowserPanelGUI browserPanelGUI) {
		Debuggable.notImplemented();

	}*/

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
	//public static POJOApp objectsContext = null;

	public static Object asPOJO(Object object) {
		if (object instanceof BT) {
			object = ((BT) object).getValue();
		}
		return object;
	}

	/*
	public static POJOBox addObject(Object object, boolean showASAP) {

		if (object instanceof POJOBox) {
			return ((POJOBox) object);
		}
		try {
			BoxPanelSwitchableView boxPanelDisplayContext = getBoxPanelTabPane();
			return boxPanelDisplayContext.addObject(object, showASAP);
		} catch (Exception e) {
			theLogger.error("addObject", object, e);
			throw reThrowable(e);
		}
	}
	 */
	public static List EMPTYLIST = new ArrayList();

	public static Collection<AddTabFrames> addTabFramers = new HashSet<AddTabFrames>();

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
		//ClassFinder.getClasses(PropertyEditor);

		registerTabs(ClassCustomizer.class, Class.class);
		registerTabs(CollectionCustomizer.class, Collection.class);
		registerTabs(ThrowableCustomizer.class, Throwable.class);
		registerTabs(BasicObjectCustomizer.class, Throwable.class);

		registerPanels(RepoManagerPanel.class, Repo.class);
		registerPanels(ObjectChoice.class, Class.class);

		//registerCustomizer(IndexedCustomizer.class, Object[].class);
		//registerCustomizer(IndexedCustomizer.class, List.class);
	}

	public static void registerTabs(Class class1, Class class2) {
		try {
			addTabFramers.add((AddTabFrames) newInstance(class1));
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	public static Object invokeFromUI(Object obj0, Method method) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return invoke(obj0, method);
	}

	public static Object invoke(Object obj0, Method method, Object... params) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Object obj = obj0;
		boolean isStatic = Modifier.isStatic(method.getModifiers());
		Class[] ts = method.getParameterTypes();
		Class objNeedsToBe = method.getDeclaringClass();
		int ml = ts.length;
		if (isStatic)
			obj = null;
		else {
			obj = recast(obj, objNeedsToBe);
		}
		int pl = params.length;

		method.setAccessible(true);
		if (ml == 0) {
			return method.invoke(obj);
		}
		// not an array of array
		if (pl > 1 && ml == pl) {
			if (isStatic)
				obj = null;
			return method.invoke(obj, params);
		}

		if (pl == 1 && ml > 1) {
			Object p0 = params[0];
			if (p0 instanceof Object[]) {
				method.invoke(obj, (Object[]) p0);
			}
		}
		return method.invoke(obj, params);
	}

	public static <T> T recast(Object obj, Class<T> objNeedsToBe) {
		return recast(obj, objNeedsToBe, null);
	}

	public static <T> T recast(Object obj, Class<T> objNeedsToBe, LinkedList<Object> except) {
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
		T result;
		try {
			result = (T) obj;
		} catch (Exception e) {
			throw reThrowable(e);
		}
		return result;
	}

	public static RuntimeException reThrowable(Throwable e) {
		if (e instanceof InvocationTargetException)
			e = e.getCause();
		if (e instanceof RuntimeException)
			return (RuntimeException) e;
		if (e instanceof Error)
			throw (Error) e;
		return new RuntimeException(e);
	}

	public static BeanInfo getBeanInfo(Class c) throws IntrospectionException {
		return getBeanInfo(c, false);
	}

	public static BeanInfo getBeanInfo(Class c, boolean onlythisClass) throws IntrospectionException {
		Class stopAtClass = null;//Object.class;// c.getSuperclass();
		if (onlythisClass) {
			stopAtClass = c.getSuperclass();
			if (stopAtClass == null && !c.isInterface()) {
				stopAtClass = Object.class;
			}
		}
		if (stopAtClass == c) {
			stopAtClass = null;
		}
		return Introspector.getBeanInfo(c, stopAtClass);
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
		if (o1 == null)
			return (o2 == null);
		else
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
	 * @param nameIndex 
	 */
	public static String generateUniqueName(Object object, Map<String, BT> checkAgainst) {
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

		String className = Utility.getShortClassName(object.getClass());
		if (checkAgainst == null) {
			return getDefaultName(object);
		}
		int counter = 1;
		boolean done = false;
		String name = "???";
		while (!done) {
			name = className + counter;
			Object otherPOJO = checkAgainst.get(name);
			if (otherPOJO == null) {
				done = true;
			} else {
				++counter;
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
		}

	}

	private static GetSetObject getPanelFor(Class expected) {
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
		PropertyEditor pe = findEditor(objClass);
		if (pe != null) {
			if (false)
				return UseEditor.class;
		}
		return null;
	}

	/**
	 * Locate a value editor for a given target type.
	 *
	 * @param targetType  The Class object for the type to be edited
	 * @return An editor object for the given target class. 
	 * The result is null if no suitable editor can be found.
	 */
	public static PropertyEditor findEditor(Class targetType) {
		PropertyEditor ped = PropertyEditorManager.findEditor(targetType);
		if (ped != null)
			return ped;
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

	static HashMap gpp = new HashMap();

	public static class TempPanel extends JPanel {

	}

	public static JPanel getPropertiesPanel(Object object) {
		JPanel view = (JPanel) gpp.get(object);
		if (view != null)
			return view;
		JPanel tp;
		if (object instanceof JPanel) {
			tp = (JPanel) object;
		} else {
			tp = new TempPanel();
		}
		gpp.put(object, tp);
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
			objects.add(boxObject(pojo));
		}
		return objects;
	}

	static HashMap allBoxes = new HashMap();

	public static synchronized boolean recordCreated(BT box) {
		if (allBoxes.containsKey(box))
			return false;
		allBoxes.put(box, box);
		return true;
	}

	private static BT boxObject(Object pojo) {
		if (pojo instanceof BT)
			return (BT) pojo;
		if (pojo instanceof HasPOJOBox)
			return ((HasPOJOBox) pojo).getPOJOBox();
		return uiObjects.findOrCreateBox(pojo);
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
		public static POJOBox addObject(Object obj) {
			return addObject(obj, false);
		}
	*/
	public static UserResult showError(DisplayContext context, String msg, Throwable error) {
		if (msg == null)
			msg = error.getMessage();
		try {
			if (context == null) {
				JPanel pnl = new org.appdapter.gui.swing.ErrorDialog(msg, error);
				pnl.show();
				return ScreenBoxImpl.asResult(pnl);
			} else {
				Utility.browserPanel.showScreenBox(error); // @temp
				return null;
			}
		} catch (Throwable err2) {
			ErrorDialog pnl = new ErrorDialog("A new error occurred while trying to display the original error '" + error + "'!", err2);
			pnl.show();
			return ScreenBoxImpl.asResult(pnl);
		}

	}

	public static String getDefaultName(Object object) {
		if (object == null)
			return "<null>";
		Class type = object.getClass();
		if (type == Class.class)
			return ((Class) object).getName();
		else
			return object.getClass().getCanonicalName() + "@" + identityHashCode(object);
	}

	static public org.appdapter.api.trigger.DisplayType getDisplayType(Class expected) {
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

	public static Class getClassNullOk(Object object) {
		if (object == null)
			return null;
		return object.getClass();
	}

	public static Object dref(Object value, Object onNull) {
		return dref(value, onNull, 4);
	}

	public static Object dref(Object value, Object onNull, int depth) {
		if (value == null)
			return onNull;
		onNull = value;
		if (depth < 0) {
			return value;
		}
		if (value instanceof GetObject) {
			value = ((GetObject) value).getValue();
		} else if (value instanceof PropertyEditor) {

			value = ((PropertyEditor) value).getValue();
		}
		value = dref(value, onNull, --depth);
		if (value == onNull)
			return onNull;
		return value;
	}

	public static <T> T[] ChoiceOf(T... o) {
		return o;
	}

	public static java.net.URL getResource(String filename) {
		return get1Resource(CollectionSetUtils.arrayOf("", "icons/"), filename, CollectionSetUtils.arrayOf("", ".gif", ".jpg", ".ico"));
	}

	public static java.net.URL get1Resource(String[] prefix, String filename, String[] suffix) {
		java.net.URL url = null;
		for (String pfx : prefix)
			for (String sfx : suffix) {
				url = org.appdapter.gui.swing.IconView.class.getResource(pfx + filename + sfx);
				if (url != null)
					return url;
				url = Utility.class.getResource(pfx + filename + sfx);
				if (url != null)
					return url;
			}
		return null;
	}

}