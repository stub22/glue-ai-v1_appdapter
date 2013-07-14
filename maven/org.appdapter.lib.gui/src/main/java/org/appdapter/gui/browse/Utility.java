package org.appdapter.gui.browse;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSpinner.DateEditor;
import javax.swing.ToolTipManager;
import javax.tools.FileObject;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.GetObject;
import org.appdapter.api.trigger.UserResult;
import org.appdapter.core.boot.ClassLoaderUtils;
import org.appdapter.core.component.KnownComponent;
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
import org.appdapter.gui.editors.UseEditor;
import org.appdapter.gui.repo.ModelMatrixPanel;
import org.appdapter.gui.repo.RepoManagerPanel;
import org.appdapter.gui.swing.CollectionEditorUtil;
import org.appdapter.gui.swing.ErrorDialog;
import org.appdapter.gui.swing.FileMenu;
import org.appdapter.gui.swing.NamedItemChooserPanel;
import org.appdapter.gui.swing.ObjectChoiceComboPanel;
import org.appdapter.gui.util.CollectionSetUtils;
import org.appdapter.gui.util.FunctionalClassRegistry;
import org.appdapter.gui.util.PromiscuousClassUtils;
import org.slf4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;

//import sun.beans.editors.ColorEditor;
//import sun.beans.editors.IntEditor;

public class Utility {
	public static List EMPTYLIST = new ArrayList();
	public static Collection<AddTabFrames> addTabFramers = new HashSet<AddTabFrames>();
	final static HashMap<Object, BT> allBoxes = new HashMap();
	public static Logger theLogger = org.slf4j.LoggerFactory.getLogger(Utility.class);
	private static final Class[] CLASS0 = new Class[0];
	public static final Class Stringable = Enum.class;
	final static Map<Class, ToFromStringConverter> toFromString = new HashMap<Class, ToFromStringConverter>();
	static HashMap<String, LinkedList> displayLists = new HashMap<String, LinkedList>();

	// ==== Instance variables ==========
	public static BrowsePanel browserPanel;
	public static NamedObjectCollection uiObjects = new BoxedCollectionImpl("All UI Objects", null);

	static {
		try {
			uiObjects.findOrCreateBox("AssemberCacheGrabber1", new AssemberCacheGrabber());
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}

		toFromString.put(Ident.class, new ToFromStringConverter<Ident>(Ident.class) {

			@Override public String toString(Ident toBecomeAString) {
				return toBecomeAString.getAbsUriString();
			}

			@Override public Ident fromString(String title, Class further) {
				return new FreeIdent(title);
			}
		});
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw reThrowable(e);
		}
	}

	//public static FileMenu fileMenu = new FileMenu();
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
		registerTabs(ArrayContentsPanelTabFramer.class, Object[].class);

		registerPanels(RepoManagerPanel.class, Repo.class);
		registerPanels(ArrayContentsPanel.class, Object[].class);
		registerPanels(ObjectChoiceComboPanel.class, Class.class);
		registerPanels(ModelMatrixPanel.class, Model.class);

		registerCustomizer(ArrayContentsPanel.class, Object[].class);
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

	public static Method getDeclaredMethod(Class search, String name, Class... parameterTypes) throws SecurityException {
		Method m = getDeclaredMethod(search, name, false, parameterTypes);
		if (m != null)
			return m;
		m = getDeclaredMethod(search, name, true, parameterTypes);
		if (m == null)
			return null;
		return m;
	}

	public static Method getDeclaredMethod(Class search, String name, boolean laxPTs, Class... parameterTypes) throws SecurityException {
		try {
			return search.getDeclaredMethod(name, parameterTypes);
		} catch (NoSuchMethodException e) {
			if (laxPTs) {
				int ptlen = parameterTypes.length;
				for (Method m : search.getDeclaredMethods()) {
					if (!m.getName().equalsIgnoreCase(name))
						continue;
					Class[] mp = m.getParameterTypes();
					if (mp.length != ptlen)
						continue;
					boolean cant = false;
					for (int i = 0; i < ptlen; i++) {
						if (Utility.isDisjoint(mp[i], parameterTypes[i])) {
							cant = true;
							break;
						}
					}
					if (cant)
						continue;
					return m;
				}
			}
			Class nis = search.getSuperclass();
			if (nis != null)
				return getDeclaredMethod(nis, name, laxPTs, parameterTypes);
			return null;
		}
	}

	public static boolean isDisjoint(Class to, Class from) {
		to = nonPrimitiveTypeFor(to);
		from = nonPrimitiveTypeFor(from);
		if (to.isAssignableFrom(from))
			return false;
		if (from.isAssignableFrom(to))
			return false;
		return true;
	}

	public static Object invoke(Object obj0, Method method, Object... params) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Object obj = obj0;
		boolean isStatic = Modifier.isStatic(method.getModifiers());
		Class[] ts = method.getParameterTypes();
		Class objNeedsToBe = method.getDeclaringClass();
		int ml = ts.length;
		if (isStatic) {
			obj = null;
		} else {
			Object obj2 = recast(obj, objNeedsToBe);
			if (!objNeedsToBe.isInstance(obj2)) {
				Class searchMethods = obj.getClass();
				Method method2 = getDeclaredMethod(searchMethods, method.getName(), ts);
				if (method2 != null) {
					return invoke(obj, method, params);
				}
				throw new IllegalArgumentException("no such method on " + obj0 + " of type " + method);

			} else {
				obj = obj2;
			}
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

	public static <T> T recast(final Object val, Class<T> objNeedsToBe, LinkedList<Object> except) {
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

	public static Class nonPrimitiveTypeFor(Class type) {
		return PromiscuousClassUtils.nonPrimitiveTypeFor(type);
	}

	static public boolean isToStringType(Class type) {
		return DisplayType.TOSTRING == getDisplayType(type);
	}

	public static Object fromString(String title, Class type) throws ClassCastException {
		ToFromStringConverter conv = getToFromConverter(type);
		if (conv != null)
			return conv.fromString(title, type);

		if (!isToStringType(type)) {
			BT box = getTreeBoxCollection().findBoxByName(title);
			if (box == null) {
				throw new ClassCastException(type + " findBoxByName " + title);
			}
			return box.convertTo(type);
		}

		if (type == String.class)
			return title;
		type = nonPrimitiveTypeFor(type);

		ClassCastException cce = null;
		Class searchType = type;
		while (searchType != null) {
			for (Method m : searchType.getDeclaredMethods()) {
				if (m.getReturnType() == type) {
					Class[] pt = m.getParameterTypes();
					if (pt != null && pt.length == 1 && pt[0] == String.class && Modifier.isStatic(m.getModifiers())) {
						try {
							m.setAccessible(true);
							return m.invoke(null, title);
						} catch (Throwable e) {
							cce = new ClassCastException(type + " " + m.getName() + " " + title);
						}
					}
				}
			}
			searchType = searchType.getSuperclass();
		}
		if (cce != null)
			throw cce;
		throw new ClassCastException(type + " fromString " + title);
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
		boolean wasInPing = isInClassLoadPing();
		try {
			inClassLoadingPing.set(Boolean.TRUE);
			return Introspector.getBeanInfo(c, stopAtClass);
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

	private static ThreadLocal<Boolean> inClassLoadingPing = new ThreadLocal<Boolean>();

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
				getCurrentPOJOApp().showScreenBox(obj);
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
		PropertyEditor pe = findEditor(objClass);
		if (pe != null) {
			return UseEditor.class;
		}
		return null;
	}

	public static boolean usePropertyEditorManager = true;

	/**
	 * Locate a value editor for a given target type.
	 *
	 * @param targetType  The Class object for the type to be edited
	 * @return An editor object for the given target class. 
	 * The result is null if no suitable editor can be found.
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
			objects.add(boxObject(pojo));
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

	public static BT boxObject(Object pojo) {
		if (pojo instanceof BT)
			return (BT) pojo;
		if (pojo instanceof IGetBox)
			return ((IGetBox) pojo).getBT();
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
		ToFromStringConverter conv = getToFromConverter(object.getClass());
		if (conv != null)
			return conv.toString(object);
		return "" + object;
	}

	static public <T> ToFromStringConverter<T> getToFromConverter(Class<? extends T> findToString) {
		synchronized (toFromString) {
			for (Class c : toFromString.keySet()) {
				if (c.isAssignableFrom(findToString)) {
					return toFromString.get(c);
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
		if (getToFromConverter(expected) != null) {
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
		if (b == null)
			return dref(userObject) == null;
		return dref(userObject) == dref(b);
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
			if (dc == null) // there is an situation that can cause this on some JVMs
				dc = clz;
			String cn = dc.getCanonicalName();
			if (cn.contains("Bundle")) {
				if (true)
					return false; //for testing if OSGi loading is safe yet
				return true;
			}
		}
		return false;
	}

	public static Collection<PropertyDescriptor> getProperties(Object object) throws IntrospectionException {
		Map<String, PropertyDescriptor> props = new HashMap<String, PropertyDescriptor>();
		final Class beanClass = object.getClass();
		PropertyDescriptor[] pdsa = getBeanInfo(beanClass).getPropertyDescriptors();
		if (pdsa != null) {
			for (PropertyDescriptor p : pdsa) {
				props.put(p.getName(), p);
			}
		}
		int fnum = -1;
		for (final Field f : getAllFields(beanClass)) {
			fnum++;
			String propName = PropertyDescriptorForField.clipPropertyNameMethod(f.getName(), "my").toLowerCase();
			PropertyDescriptor pd = props.get(propName);
			if (pd == null) {
				pd = new PropertyDescriptorForField(f);
				props.put(pd.getName().toLowerCase(), pd);
			}
		}
		Collection<Method> ml = getAllMethods(beanClass);
		for (Method m : ml) {
			String propName = PropertyDescriptorForField.clipPropertyNameMethod(m.getName(), "is", "get", "set").toLowerCase();
			PropertyDescriptor pd = props.get(propName);
			Class[] pts = m.getParameterTypes();
			int ptsl = pts.length;
			if (pd != null) {
				if (ptsl == 0 && isAssignableTypes(m.getReturnType(), pd.getPropertyType())) {
					pd.setReadMethod(m);
					continue;
				} else {
					if (ptsl == 1 && m.getReturnType() == void.class && isAssignableTypes(pts[0], pd.getPropertyType())) {
						pd.setWriteMethod(m);
						continue;
					}
				}
			}
		}
		return props.values();
	}

	private static boolean isAssignableTypes(Class<?> c1, Class<?> c2) {
		if (c1 == void.class || c2 == void.class)
			return false;
		if (c1.isAssignableFrom(c2) || c2.isAssignableFrom(c1))
			return true;
		return c1.isInterface() && c2.isInterface();
	}

	private static boolean disjointTypes(Class<?> c1, Class<?> c2) {
		if (c1 == void.class || c2 == void.class)
			return true;
		if (c1.isAssignableFrom(c2) || c2.isAssignableFrom(c1))
			return false;
		return false;
	}

	private static Collection<Method> getAllMethods(Class clz) {
		List<Method> methods = new ArrayList<Method>();
		while (clz != null) {
			for (Method m : clz.getDeclaredMethods()) {
				methods.add(m);
			}
			clz = clz.getSuperclass();
		}
		return methods;
	}

	private static Collection<Field> getAllFields(Class clz) {
		List<Field> methods = new ArrayList<Field>();
		while (clz != null) {
			for (Field m : clz.getDeclaredFields()) {
				methods.add(m);
			}
			clz = clz.getSuperclass();
		}
		return methods;
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
}