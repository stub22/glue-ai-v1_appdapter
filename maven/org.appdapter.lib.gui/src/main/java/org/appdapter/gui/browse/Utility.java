package org.appdapter.gui.browse;

import static org.appdapter.core.convert.ReflectUtils.addIfNew;
import static org.appdapter.core.convert.ReflectUtils.arrayOf;
import static org.appdapter.core.convert.ReflectUtils.copyOf;
import static org.appdapter.core.convert.ReflectUtils.isAssignableFrom;
import static org.appdapter.core.log.Debuggable.notImplemented;
import static org.appdapter.core.log.Debuggable.printStackTrace;
import static org.appdapter.core.log.Debuggable.reThrowable;
import static org.appdapter.core.log.Debuggable.toInfoStringO;
import static org.appdapter.core.log.Debuggable.warn;
import static org.appdapter.gui.trigger.TriggerMenuFactory.ADD_ALL;
import static org.appdapter.gui.trigger.TriggerMenuFactory.addMethodAsTrig;
import static org.appdapter.gui.trigger.TriggerMenuFactory.addTriggerToPoppup;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
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
import java.util.Set;
import java.util.concurrent.Callable;

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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.tools.FileObject;

import org.appdapter.api.trigger.AnyOper.Singleton;
import org.appdapter.api.trigger.AnyOper.UtilClass;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxContext;
import org.appdapter.api.trigger.GetObject;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.api.trigger.UserResult;
import org.appdapter.bind.rdf.jena.model.JenaLiteralUtils;
import org.appdapter.core.boot.ClassLoaderUtils;
import org.appdapter.core.component.IdentToObjectListener;
import org.appdapter.core.component.KnownComponent;
import org.appdapter.core.convert.Converter;
import org.appdapter.core.convert.Converter.ConverterMethod;
import org.appdapter.core.convert.NoSuchConversionException;
import org.appdapter.core.convert.OptionalArg;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.core.convert.TypeAssignable;
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
import org.appdapter.gui.api.GetDisplayContext;
import org.appdapter.gui.api.GetSetObject;
import org.appdapter.gui.api.IGetBox;
import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.api.SetObject;
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
import org.appdapter.gui.editors.LargeObjectView.TabPanelMaker;
import org.appdapter.gui.editors.LargeObjectView.ThrowableCustomizer;
import org.appdapter.gui.editors.ObjectPanel;
import org.appdapter.gui.editors.ObjectPanelHost;
import org.appdapter.gui.editors.SimplePOJOInfo;
import org.appdapter.gui.editors.UseEditor;
import org.appdapter.gui.repo.ModelAsTurtleEditor;
import org.appdapter.gui.repo.ModelMatrixPanel;
import org.appdapter.gui.repo.RepoManagerPanel;
import org.appdapter.gui.swing.CollectionEditorUtil;
import org.appdapter.gui.swing.DisplayContextUIImpl.UnknownIcon;
import org.appdapter.gui.swing.ErrorDialog;
import org.appdapter.gui.swing.FileMenu;
import org.appdapter.gui.swing.NamedItemChooserPanel;
import org.appdapter.gui.swing.ObjectChoiceComboPanel;
import org.appdapter.gui.swing.SafeJMenu;
import org.appdapter.gui.swing.ScreenBoxPanel;
import org.appdapter.gui.trigger.TriggerAdder;
import org.appdapter.gui.trigger.TriggerFilter;
import org.appdapter.gui.trigger.TriggerForClass;
import org.appdapter.gui.trigger.TriggerForInstance;
import org.appdapter.gui.trigger.TriggerMenuFactory;
import org.appdapter.gui.trigger.TriggerMenuFactory.TriggerSorter;
import org.appdapter.gui.trigger.UtilityMenuOptions;
import org.appdapter.gui.util.ClassFinder;
import org.appdapter.gui.util.PromiscuousClassUtilsA;
import org.slf4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.util.Pair;
//import sun.beans.editors.ColorEditor;
//import sun.beans.editors.IntEditor;

public class Utility extends UtilityMenuOptions {

	public static Logger theLogger = org.slf4j.LoggerFactory.getLogger(Utility.class);

	static public class UtilityConverter implements Converter {

		@Override public <T> T convert(Object obj, Class<T> objNeedsToBe, int maxCvt) throws NoSuchConversionException {
			return Utility.recast(obj, objNeedsToBe, maxCvt);
		}

		@Override public Integer declaresConverts(Object val, Class objClass, Class objNeedsToBe, int maxCvt) {
			if (objClass != null) {
				if (getToFromConverter(objNeedsToBe, objClass) != null)
					return WILL;
			}
			return MIGHT;// .declaresConverts(val, objClass, objNeedsToBe);
		}

	}

	static public class UtilityToFromConverterOnly implements Converter {

		@Override public <T> T convert(Object obj, Class<T> objNeedsToBe, int maxCvt) throws NoSuchConversionException {
			Converter converter = getToFromConverter(objNeedsToBe, obj.getClass());
			if (converter == null)
				return ReflectUtils.noSuchConversion(obj, objNeedsToBe, null);
			return converter.convert(obj, objNeedsToBe, maxCvt);
		}

		@Override public Integer declaresConverts(Object val, Class objClass, Class objNeedsToBe, int maxCvt) {
			if (objClass != null) {
				if (getToFromConverter(objNeedsToBe, objClass) != null)
					return WILL;
			}
			return WONT;// .declaresConverts(val, objClass, objNeedsToBe);
		}

	}

	static public class UtilityUnboxingConverterOnly implements Converter {

		@Override public <T> T convert(Object obj, Class<T> objNeedsToBe, int maxCvt) throws NoSuchConversionException {
			return Utility.recast(obj, objNeedsToBe, maxCvt);
		}

		@Override public Integer declaresConverts(Object val, Class objClass, Class objNeedsToBe, int maxCvt) {
			if (objClass != null && GetObject.class.isAssignableFrom(objClass)) {
				return MIGHT;
			}
			if (val instanceof GetObject) {
				return MIGHT;
			}
			return WONT;
		}
	}

	static public class UtilityConvertableConverterOnly implements Converter {

		@Override public <T> T convert(Object val, Class<T> objNeedsToBe, int maxCvt) throws NoSuchConversionException {
			if (val instanceof Convertable) {
				Convertable convertable = (Convertable) val;
				return convertable.convertTo(objNeedsToBe);
			}
			return Utility.recast(val, objNeedsToBe, maxCvt);
		}

		@Override public Integer declaresConverts(Object val, Class objClass, Class objNeedsToBe, int maxCvt) {
			if (objClass != null && Convertable.class.isAssignableFrom(objClass)) {
				return MIGHT;
			}
			if (val instanceof Convertable) {
				Convertable convertable = (Convertable) val;
				if (convertable.canConvert(objNeedsToBe))
					return WILL;
				return MIGHT;
			}
			return WONT;
		}
	}

	static public class UtilityOptionalArgs implements OptionalArg {

		@Override public Object getArg(Class type) throws NoSuchConversionException {
			Collection objs = findUIObjectsByType(type);
			if (objs.size() != 1) {
				synchronized (lastResults) {
					Object o = lastResults.get(type);
					if (o != null)
						return o;
					for (Object lr : lastResults.values()) {
						if (type.isInstance(lr))
							return lr;
					}
				}
				throw new NoSuchConversionException("" + type);
			}
			return objs.toArray()[0];
		}

		@Override public void reset() {
		}
	}

	private static ThreadLocal<Boolean> inClassLoadingPing = new ThreadLocal<Boolean>();
	public static Collection<AddTabFrames> addTabFramers = new HashSet<AddTabFrames>();
	final static HashMap<Object, BT> allBoxes = new HashMap();
	private static final Class[] CLASS0 = new Class[0];
	public static final Class Stringable = Enum.class;
	final static Map<Class, Map<Class, ToFromKeyConverter>> toFrmKeyCnvMap = new HashMap<Class, Map<Class, ToFromKeyConverter>>();
	static HashMap<String, LinkedList> displayLists = new HashMap<String, LinkedList>();
	static Map<Object, Thread> slowThreads = new HashMap<Object, Thread>();
	static List<Pair<Class, Class>> classToClassRegistry = new ArrayList<Pair<Class, Class>>();

	// ==== Instance variables ==========
	public static BrowsePanel browserPanel;
	public static NamedObjectCollection uiObjects = new BoxedCollectionImpl("All UI Objects", null);
	public static NamedObjectCollection clipboardCollection = new BoxedCollectionImpl("Clipboard", null);
	private static Collection<TriggerAdder> triggerAdders = new LinkedList<TriggerAdder>();

	static ArrayList<TriggerForInstance> appMenuGlobalTriggers = new ArrayList<TriggerForInstance>();
	static ArrayList<TriggerForClass> objectContextMenuTriggers = new ArrayList<TriggerForClass>();
	static public LinkedList<Object> featureQueueUp = new LinkedList<Object>();
	final static public Object featureQueueLock = appMenuGlobalTriggers;

	public static SafeJMenu toolsMenu;

	public static void addSingletonMethods(Object object) {
		addSingletonMethods(object, object.getClass());
	}

	public static void addSingletonMethods(Object object, Class clz) {
		synchronized (featureQueueLock) {
			addSingletonMethods00(object, clz);
		}
	}

	public static void addSingletonMethods00(Object object, Class clz) {
		List<TriggerForInstance> menuGlobalTriggers = appMenuGlobalTriggers;
		synchronized (menuGlobalTriggers) {
			WrapperValue wrap = asWrapped(object);
			DisplayContext ctx = getDisplayContext();
			int ps1 = menuGlobalTriggers.size();
			while (clz != null) {
				if (clz == Object.class) {
					break;
				}
				for (Method m : clz.getDeclaredMethods()) {
					// declared methods not static
					if (!ReflectUtils.isStatic(m)) {
						if (m.isSynthetic())
							continue;
						if (ReflectUtils.isOverride(m))
							continue;
						if (m.getParameterTypes().length == 0) {
							addMethodAsTrig(ctx, clz, m, menuGlobalTriggers, wrap, ADD_ALL, "%c|%m", false, false);
						} else {

						}
					}
				}
				for (Field m : clz.getDeclaredFields()) {
					if (!ReflectUtils.isStatic(m)) {
						if (m.isSynthetic())
							continue;
						if (m.getType() == boolean.class) {
							TriggerMenuFactory.addMethodAsTrig(ctx, clz, m, menuGlobalTriggers, wrap, ADD_ALL, "%c|%m", false, true);
						} else {

						}
					}
				}
				clz = clz.getSuperclass();
			}
			Collections.sort(menuGlobalTriggers, new TriggerSorter());
			int ps2 = menuGlobalTriggers.size();
			if (ps1 != ps2) {
				updateToolsMenu();
			}
		}
	}

	public static void addClassStaticMethods(Class clz) {
		synchronized (featureQueueLock) {
			if (!loadedClassMethods.add(clz))
				return;
			addClassStaticMethods00(clz);
		}
	}

	private static HashSet<Class> loadedClassMethods = new HashSet<Class>(1000);

	private static void addClassStaticMethods00(Class clz) {

		List<TriggerForInstance> appMenuGlobalTrigs = appMenuGlobalTriggers;
		synchronized (appMenuGlobalTrigs) {
			DisplayContext ctx = getDisplayContext();
			int ps1 = appMenuGlobalTrigs.size();
			while (clz != null) {
				if (clz == Object.class) {
					break;
				}

				for (Method m : clz.getDeclaredMethods()) {
					if (m.isSynthetic())
						continue;

					int plen = m.getParameterTypes().length;

					if (m.getReturnType() != void.class) {
						ConverterMethod cmi = ReflectUtils.getAnnotationOn(m, ConverterMethod.class);
						if (cmi != null) {
							ReflectUtils.registerConverterMethod(m, cmi);
						}
					}

					if (ReflectUtils.isOverride(m))
						continue;

					if (ReflectUtils.isStatic(m)) {
						if (plen == 0) {
							TriggerMenuFactory.addMethodAsTrig(ctx, clz, m, appMenuGlobalTrigs, null, ADD_ALL, "%d|%m", true, false);
						} else if (plen < 3) {
							TriggerMenuFactory.addMethodAsTrig(ctx, clz, m, objectContextMenuTriggers, null, ADD_ALL, "%d|%m", true, false);
						}
					}
				}

				for (Field m : clz.getDeclaredFields()) {
					if (ReflectUtils.isStatic(m)) {
						if (m.isSynthetic())
							continue;
						if (m.getType() == boolean.class) {
							TriggerMenuFactory.addMethodAsTrig(ctx, clz, m, appMenuGlobalTrigs, null, ADD_ALL, "%d|%m", true, true);
						} else {

						}
					}
				}
				clz = clz.getSuperclass();
			}

			if (ReflectUtils.implementsAllClasses(clz, ObjectPanel.class, Component.class)) {
				if (ReflectUtils.isCreatable(clz)) {
					try {
						Class panelFor = ReflectUtils.getTypeClass(clz.getMethod("getClassOfBox").getReturnType(), new LinkedList());
						if (panelFor != null) {
							registerPanel(clz, panelFor);
						}
					} catch (SecurityException e) {
					} catch (NoSuchMethodException e) {
					}

				}
			}
			int ps2 = appMenuGlobalTrigs.size();
			if (ps1 != ps2) {
				Collections.sort(appMenuGlobalTrigs, new TriggerSorter());
				updateToolsMenu();
			}
		}

	}

	public static void updateToolsMenu() {
		List<TriggerForInstance> appMenuGlobalTrigs = appMenuGlobalTriggers;
		synchronized (appMenuGlobalTrigs) {
			if (toolsMenu == null)
				return;
			toolsMenu.removeAll();
			Collections.sort(appMenuGlobalTrigs, new TriggerSorter());
			for (TriggerForInstance tfi : appMenuGlobalTrigs) {
				addTriggerToPoppup(toolsMenu, null, tfi);
			}
		}
	}

	public static List<TriggerForInstance> getAppMenuGlobalMethods() {
		synchronized (appMenuGlobalTriggers) {
			return new ArrayList<TriggerForInstance>(appMenuGlobalTriggers);
		}
	}

	public static List<TriggerForClass> getObjectGlobalMethods() {
		synchronized (objectContextMenuTriggers) {
			return new ArrayList<TriggerForClass>(objectContextMenuTriggers);
		}
	}

	static AssemblerCacheGrabber singletAssemblerCacheGrabber = new AssemblerCacheGrabber();
	static {
		addObjectFeatures(singletAssemblerCacheGrabber);
		JenaLiteralUtils.addIdListener(new IdentToObjectListener() {

			@Override public void registerURI(Ident id, Object value) {
				Utility.recordCreated(uiObjects, id, value);
			}

			@Override public void deregisterURI(Ident id, Object value) {
				Utility.recordCreated(uiObjects, id, null);
			}

		});
		addObjectFeatures(UtilityMenuOptions.class);
		addObjectFeatures(Utility.class);
		ReflectUtils.registerConverter(new UtilityConverter());
		Map<Class, ToFromKeyConverter> toFrmKeyCnv = getKeyConvMap(String.class, true);

		toFrmKeyCnv.put(Ident.class, new ToFromKeyConverter<Ident, String>(Ident.class, String.class) {

			@Override public String toKey(Ident toBecomeAString) {
				return toBecomeAString.getAbsUriString();
			}

			@Override public Ident fromKey(String title, Class further) {
				return new FreeIdent(title);
			}
		});
	}

	private static Map<Class, ToFromKeyConverter> getKeyConvMap(Class keyType, boolean createIfMissing) {
		return getKeyConvMap(toFrmKeyCnvMap, !createIfMissing ? null : new Callable() {
			@Override public Object call() throws Exception {
				return new HashMap<Class, ToFromKeyConverter>();
			}
		}, keyType);

	}

	private static <T> T getKeyConvMap(Map<Class, T> toFrmKeyCnvMap, Callable<T> whenMissing, Class keyType) {
		synchronized (toFrmKeyCnvMap) {
			T toFrmKeyCnv = toFrmKeyCnvMap.get(keyType);
			if (toFrmKeyCnv != null)
				return toFrmKeyCnv;
			for (Class key : toFrmKeyCnvMap.keySet()) {
				if (key.isAssignableFrom(keyType)) {
					toFrmKeyCnv = toFrmKeyCnvMap.get(keyType);
					if (toFrmKeyCnv != null)
						return toFrmKeyCnv;
				}
			}
			if (toFrmKeyCnv == null && whenMissing != null) {
				try {
					toFrmKeyCnv = whenMissing.call();
					toFrmKeyCnvMap.put(keyType, toFrmKeyCnv);
					return toFrmKeyCnv;
				} catch (Throwable e) {

				}
			}

			return toFrmKeyCnv;
		}
	}

	public static void addObjectFeatures(Object obj) {
		if (obj == null)
			return;
		synchronized (featureQueueLock) {
			addObjectFeatures0(obj);
		}
	}

	private static void addObjectFeatures0(Object obj) {
		if (featureQueueUp != null) {
			featureQueueUp.add(obj);
			return;
		}

		if (obj instanceof Singleton) {
			addSingletonMethods((Singleton) obj);
		} else if (obj instanceof Class) {
			Class clz = (Class) obj;
			if (!clz.isInterface()) {
				addClassStaticMethods(clz);
			}
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
			warn("NULL namedObjectCollection");
		}
		return noc.findBoxByName(id.toString());
	}

	static public void recordCreated(NamedObjectCollection noc, Ident id, Object comp) {
		//BoxPanelSwitchableView boxPanelDisplayContext = getBoxPanelTabPane();
		try {
			recordCreated(noc.findOrCreateBox(id.toString(), comp));
		} catch (PropertyVetoException e) {
			printStackTrace(e);
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
			warn("NULL theBoxPanelDisplayContext");
		}
		return theBoxPanelDisplayContext;
	}

	/**
	 * The current ObjectNavigator being displayed
	 */
	static public NamedObjectCollection getTreeBoxCollection() {
		if (uiObjects == null) {
			warn("NULL uiObjects");
		}
		return uiObjects;
	}

	public static BrowsePanel getCurrentPOJOApp() {
		if (browserPanel == null) {
			warn("NULL browserPanel");
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
			warn("NULL controlApp");
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
	 * notImplemented();
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
		registerTabs(SpecificObjectCustomizers.class, Object.class);

		registerPanel(RepoManagerPanel.class, Repo.class);
		registerPanel(ArrayContentsPanel.class, Object[].class);
		registerPanel(ObjectChoiceComboPanel.class, Class.class);
		registerPanel(ModelAsTurtleEditor.class, Model.class);
		registerPanel(ModelMatrixPanel.class, Model.class);

		registerCustomizer(ArrayContentsPanel.class, Object[].class);
		// registerCustomizer(IndexedCustomizer.class, List.class);
	}

	public static void registerTabs(Class<? extends AddTabFrames> class1, Class class2) {
		try {
			addTabFramers.add((AddTabFrames) newInstance(class1));
		} catch (Throwable e) {
			printStackTrace(e);
		}
	}

	private static void registerCustomizer(Class<? extends Customizer> customizer, Class<?>... clz) {
		addDelegateClass(Customizer.class, customizer, clz);
	}

	public static void addDelegateClass(Class ignored, Class customizer, Class<?>[] clz) {
		for (Class c : clz) {
			registerPair(customizer, c);
		}

	}

	public static void registerPanel(Class customizer, Class<?>... clz) {
		addDelegateClass(Component.class, customizer, clz);
	}

	static {
		registerEditors();
	}

	public static void setBeanInfoSearchPath() {
		Introspector.setBeanInfoSearchPath(new String[] { AbstractCollectionBeanInfo.class.getPackage().getName() });
	}

	public static Object invokeFromUI(Object obj0, Method method) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return ReflectUtils.invokeOptional(obj0, method, new UtilityOptionalArgs());
	}

	public static <T> T recast(Object obj, Class<T> objNeedsToBe, int maxCvt) throws NoSuchConversionException {
		return recast(obj, objNeedsToBe, maxCvt, null);
	}

	public static <T> T recast(final Object val, Class<T> objNeedsToBe, int maxCvt, LinkedList<Object> except) throws NoSuchConversionException {
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
			Object res = recast(obj, objNeedsToBe, maxCvt, except);
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
		int maxCvt = Converter.MCVT;
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
							if (TypeAssignable.CASTING_ONLY.declaresConverts(title, keyClass, pt[0], maxCvt) == TypeAssignable.WONT)
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

	public static RuntimeException reThrowable_UnusedNow(Throwable e) {
		if (true)
			return reThrowable(e);
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
		} catch (Throwable t) {
			printStackTrace(t);
			throw reThrowable(t, IntrospectionException.class);
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
			warn("did not get suggested name : " + suggestedName + " isntead got " + newName + " for " + object);
		}
		return newName;
	}

	public static String generateUniqueName_sug(Object object, String suggestedName, Map<String, BT> checkAgainst) {
		if (object == null)
			return "<null>";
		if (object instanceof Class) {
			return ReflectUtils.getCanonicalSimpleName((Class) object);
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
			className = getShortClassName(object.getClass());
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

	public static HashMap<Class, Object> lastResults = new HashMap<Class, Object>();

	public static void addLastResultType(Object obj, Class expected) {
		if (obj == null)
			return;
		synchronized (lastResults) {
			if (expected != null) {
				lastResults.put(expected, obj);
			}
			Class tighter = obj.getClass();
			if (tighter != expected) {
				lastResults.put(tighter, obj);
			}
		}
	}

	public static void addSubResult(Object from, Box targetBox, ActionEvent evt, Object obj, Class expected) throws PropertyVetoException {
		addLastResultType(obj, expected);
		expected = ReflectUtils.nonPrimitiveTypeFor(expected);
		if (expected == Void.class)
			return;

		if (Number.class.isAssignableFrom(expected)) {
			expected = String.class;
			obj = "" + obj;
		}
		if (Enum.class.isAssignableFrom(expected)) {
			expected = String.class;
			obj = "" + obj;
		}
		if (Boolean.class.isAssignableFrom(expected)) {
			expected = String.class;
			obj = "" + obj;
		}
		if (obj == null) {
			obj = "(" + expected.getName() + ")null";
			expected = String.class;
		}
		if (expected == String.class) {
			try {
				browserPanel.showMessage("" + obj, expected);
				return;
			} catch (Exception e) {
				printStackTrace(e);
				return;
			}
		}
		// narrow type
		if (expected.isInstance(obj)) {
			expected = obj.getClass();
		}
		addNonStringSubResult(targetBox, obj, expected);
	}

	public static void addNonStringSubResult(Box whereFrom, Object obj, Class expected) throws PropertyVetoException {
		DisplayType dt = getDisplayType(expected);
		final DisplayType edt = dt;
		if (dt == DisplayType.TREE) {
			BT boxed = getTreeBoxCollection().findOrCreateBox(null, obj);
			BoxContext bc = whereFrom.getBoxContext();
			bc.contextualizeAndAttachChildBox((Box) whereFrom, (MutableBox) boxed);
			return;
		}
		GetSetObject pnl4 = getPanelFor(expected);
		if (pnl4 != null) {
			try {
				pnl4.setObject(obj);
			} catch (InvocationTargetException e) {
				theLogger.error("" + pnl4, e);
			}
		}
		if (isToStringType(expected)) {
			theLogger.info("result from " + whereFrom + " was " + obj);
			String toStr = makeToString(obj);
			getCurrentPOJOApp().showMessage(toStr, expected);
		}
		try {
			getCurrentContext().showScreenBox(obj);
		} catch (Exception e) {
			BT boxed = getTreeBoxCollection().findOrCreateBox(obj);
			BoxContext bc = whereFrom.getBoxContext();
			JPanel pnl = boxed.getPropertiesPanel();
			if (dt == DisplayType.FRAME) {
				BoxPanelSwitchableView jtp = getBoxPanelTabPane();
				jtp.addComponent(pnl.getName(), pnl, DisplayType.FRAME);
				return;
			}
			BoxPanelSwitchableView jtp = getBoxPanelTabPane();
			jtp.addComponent(pnl.getName(), pnl, DisplayType.PANEL);
		}
	}

	static Hashtable<Class, GetSetObject> panelsFor = new Hashtable<Class, GetSetObject>();

	private static GetSetObject getPanelFor(Class expected) {
		final Class expectedType = expected;
		GetSetObject gso = panelsFor.get(expected);
		if (gso != null)
			return gso;
		return null;
	}

	public static Class<? extends Customizer> findCustomizerClass(Class objClass) {
		try {
			for (Class c : findImplmentingForMatch(Customizer.class, objClass)) {
				if (c != null)
					return c;
			}
			return makeCustomizerFromEditor(objClass);
		} catch (Throwable e) {
			theLogger.error(" " + objClass, e);
			return null;
		}
	}

	public static Collection<Class<? extends Component>> findComponentClasses(Class objClass) {
		Collection<Class<? extends Component>> im = findImplmentingForMatch(Component.class, objClass);
		return im;
	}

	private static <T> Collection<Class<? extends T>> findImplmentingForMatch(Class<T> mustBe, Class objClass) {
		boolean useAssignable = true;
		synchronized (classToClassRegistry) {
			List<Class<? extends T>> list = new ArrayList<Class<? extends T>>();
			for (Pair<Class, Class> rl : classToClassRegistry) {
				Class l = rl.getLeft();
				if (l == ModelAsTurtleEditor.class) {
					theLogger.debug("searching for " + mustBe + " for " + objClass);
				}
				if (!typesMatch(mustBe, l, useAssignable))
					continue;
				Class r = rl.getRight();
				if (typesMatch(r, objClass, useAssignable)) {
					list.add(l);
				}
			}
			return list;
		}
	}

	private static <T> void registerPair(Class<T> mustBe, Class objClass) {
		synchronized (classToClassRegistry) {
			classToClassRegistry.add(new Pair(mustBe, objClass));
		}

	}

	static boolean typesMatch(Class mustBe, Class srch, boolean useAssignable) {
		if (mustBe == srch)
			return true;
		if (useAssignable) {
			if (mustBe.isAssignableFrom(srch))
				return true;
			if (srch.isAssignableFrom(mustBe))
				return true;
		}
		if (isAssignableFrom(mustBe, srch)) {
			return true;
		}
		return false;
	}

	private static Class makeCustomizerFromEditor(Class objClass) {
		Class pe = findEditorClass(objClass);
		if (pe != null) {
			return UseEditor.class;
		}
		return null;
	}

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
		for (Class<? extends PropertyEditor> pe : findImplmentingForMatch(PropertyEditor.class, targetType)) {
			if (pe == null || !PropertyEditor.class.isAssignableFrom(pe)) {
				return null;
			}
			try {
				ped = pe.newInstance();
				return ped;
			} catch (Throwable e) {
			}
		}
		return null;
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
		for (Class<? extends PropertyEditor> pe : findImplmentingForMatch(PropertyEditor.class, targetType)) {
			if (pe == null || !PropertyEditor.class.isAssignableFrom(pe)) {
				continue;
			}
			return pe;
		}
		if (ped != null)
			return ped.getClass();

		return null;
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
		addIfNew(panelClass, findImplmentingForMatch(PropertyEditor.class, targetType), false);
		addIfNew(panelClass, findImplmentingForMatch(Customizer.class, targetType), false);
		addIfNew(panelClass, findImplmentingForMatch(Component.class, targetType), false);
		addIfNew(panelClass, findCustomizerClass(targetType), false);
		addIfNew(panelClass, makeCustomizerFromEditor(targetType), false);
		return panelClass;
	}

	public static List getSearchableClassList() {
		notImplemented();
		return PromiscuousClassUtilsA.getInstalledClasses();
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
		notImplemented();
		return null;
	}

	public static FileObject getConfigFile(String path) {
		notImplemented();
		notImplemented();
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
			customizer = newInstance(customizerClass);
		} catch (Throwable e) {
			customizer = new LargeObjectView(getCurrentContext(), object);
		}
		customizer.setObject(object);
		if (customizer instanceof JPanel)
			view = (JPanel) customizer;
		else {
			theLogger.warn("customizer is not a Component " + customizer);
			view = new LargeObjectView(getCurrentContext(), object);
		}
		gpp.put(object, view);
		return view;
	}

	static public class SpecificObjectCustomizers extends TabPanelMaker {

		@Override public void setTabs(BoxPanelSwitchableView tabs, DisplayContext context, Object object, Class objClass, SetTabTo cmd) {
			setTabs0(tabs, context, object, objClass, cmd);
		}

		public void setTabs0(BoxPanelSwitchableView tabs, DisplayContext context, Object object, Class objClass, SetTabTo cmd) {

			for (Class comp : Utility.findComponentClasses(objClass)) {
				if (comp == null) {
					return;
				}
				String prefix = ReflectUtils.getCanonicalSimpleName(comp);
				if (!ReflectUtils.isCreatable(comp))
					continue;

				if (ObjectPanelHost.class.isAssignableFrom(comp))
					continue;
				boolean declaredGood = false;
				if (ScreenBoxPanel.class.isAssignableFrom(comp) || ObjectPanel.class.isAssignableFrom(comp)) {
					declaredGood = true;
				}

				if (tabs.containsComponentOfClass(comp).size() > 0)
					continue;

				if (cmd == SetTabTo.ADD) {
					Component cp;
					try {
						cp = (Component) ReflectUtils.invokeConstructorOptional(new UtilityConverter(), new UtilityOptionalArgs(), comp, object);
						if (cp == null) {
							theLogger.warn("Did not create " + comp);
							continue;
						}
						if (cp instanceof SetObject) {
							((SetObject) cp).setObject(object);
						}
						tabs.addTab(prefix, cp);

					} catch (Throwable e) {
						e.printStackTrace();
						theLogger.error("Did not create " + comp, e);
					}

				}
				if (cmd == SetTabTo.REMOVE) {
					tabs.removeTab(prefix, null);
				}
			}
		}
	}

	public static Class getCustomizerClassForClass(Class objClass) {
		Class<? extends Customizer> customizerClass = findCustomizerClass(objClass);
		if (customizerClass != LargeObjectView.class) {
			customizerClass = findCustomizerClass(objClass);
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

	public static void showError(Object queryWorker, String msg, Throwable error) {
		DisplayContext dc = getDisplayContext();
		if (queryWorker instanceof DisplayContext) {
			dc = (DisplayContext) queryWorker;
		} else if (queryWorker instanceof GetDisplayContext) {
			dc = ((GetDisplayContext) queryWorker).getDisplayContext();
		}
		showError(dc, msg, error);
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
				return asUserResult(pnl);
			} else {
				browserPanel.showScreenBox(error); // @temp
				return null;
			}
		} catch (Throwable err2) {
			ErrorDialog pnl = new ErrorDialog("A new error occurred while trying to display the original error '" + error + "'!", err2);
			pnl.show();
			return asUserResult(pnl);
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
			BT newBox = getTreeBoxCollection().findOrCreateBox(object);
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
		Map<Class, ToFromKeyConverter> toFrmKeyCnv = getKeyConvMap(key, false);
		if (toFrmKeyCnv == null)
			return null;
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
		if (JPanel.class.isAssignableFrom(expected)) {
			return DisplayType.PANEL;
		}
		if (TreeNode.class.isAssignableFrom(expected)) {
			return DisplayType.TREE;
		}
		if (expected == String.class || CharSequence.class.isAssignableFrom(expected)) {
			return DisplayType.TOSTRING;
		}
		if (expected == Boolean.class) {
			return DisplayType.TOSTRING;
		}
		ToFromKeyConverter cvt = getToFromConverter(expected, String.class);
		if (cvt != null) {
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
			if (value instanceof JComponent && value instanceof GetObject) {
				derefd = ((GetObject) value).getValue();
			} else if (value instanceof DefaultMutableTreeNode) {
				derefd = ((DefaultMutableTreeNode) value).getUserObject();
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
		return get1Resource(arrayOf("", "icons/"), filename, arrayOf("", ".gif", ".jpg", ".ico"));
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
				oc = toInfoStringO(gv);
			}
			gv = getShortClassName(gv.getClass()) + ".this.toString: " + oc;
		}
		return "" + gv;
	}

	static {
		triggerAdders.add(new TriggerAdder() {
			@Override public String toString() {
				// TODO Auto-generated method stub
				return getClass().getSuperclass() + " addTriggersForObjectInstance";
			}

			@Override public <TrigType> void addTriggersForObjectInstance(DisplayContext ctx, Class cls, List<TrigType> tgs, WrapperValue poj, TriggerFilter rulesOfAdd, String menuPrepend) {
				TriggerMenuFactory.addTriggersForObjectInstanceMaster(ctx, cls, tgs, poj, ADD_ALL, menuPrepend, false);
			}
		});
	}

	public static NamedObjectCollection getClipboard() {
		return clipboardCollection;
	}

	public static boolean isOSGi() {
		ClassLoader cl = PromiscuousClassUtilsA.getCallerClassLoader();
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
							Method prm = pd.getReadMethod();
							if (pts[0] != propType) {
								//one is primtive;
								continue;
							}

							try {
								pd.setWriteMethod(m);
							} catch (IntrospectionException ie) {
								printStackTrace(ie);
							}
							continue;
						}
					}
				} catch (Throwable t) {
					printStackTrace(t);
					//throw reThrowable(t);

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
			if (!useBeanIcons)
				return null;
			return getIcon(getBeanInfo(info, null));
		} catch (Throwable e) {
			printStackTrace(e);
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
		return getImage(getResource(string));
	}

	public static Image getImage(URL uri) {
		try {
			return ImageIO.read(uri);
		} catch (IOException e) {
			printStackTrace(e);
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
		for (TriggerForClass trigc : getObjectGlobalMethods()) {
			if (trigc.appliesTarget(cls, poj))
				triggersFound.add(trigc.createTrigger(null, ctx, poj));
		}
		return triggersFound;
	}

	public static Collection<Object> findUIObjectsByType(Class type) {
		Collection c = getClipboard().findObjectsByType(type);
		if (c.size() > 0) {
			return c;
		}
		return getTreeBoxCollection().findObjectsByType(type);
	}

	public static Map<String, Object> propertyDescriptors(Object object, boolean skipNulls, boolean skipStringables) {
		Map<String, Object> showProps = new HashMap<String, Object>();
		try {
			for (PropertyDescriptor pd : getProperties(object)) {
				Class pt = pd.getPropertyType();
				if (skipStringables && (pt == null || (isToStringType(pt) || pt == Class.class)))
					continue;
				Object v = null;
				if (pd instanceof PropertyDescriptorForField) {
					try {
						v = ((PropertyDescriptorForField) pd).getFieldValue(object);
					} catch (Throwable t) {
						t.printStackTrace();
					}
				} else {
					if (pd == null)
						continue;
					Method rm = pd.getReadMethod();
					if (rm == null)
						continue;
					Class rtc = rm.getReturnType();
					if (Component.class.isAssignableFrom(rtc))
						continue;
					try {
						rm.setAccessible(true);
						v = rm.invoke(object);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}

				if (v == null && skipNulls)
					continue;
				showProps.put(pd.getName(), v);

			}
		} catch (IntrospectionException e) {
			e.printStackTrace();
		}
		return showProps;
	}

	public static void replaceRunnable(Object key, Runnable runnable) {
		if (!useSeperateSlowThreads) {
			runnable.run();
			return;
		}
		synchronized (slowThreads) {
			Thread fnd = slowThreads.get(key);
			if (fnd != null) {
				killThread(fnd);
			}
			fnd = new Thread(runnable, "Worker for " + key);
			slowThreads.put(key, fnd);
			fnd.start();
		}

	}

	private static void killThread(Thread fnd) {
		if (!fnd.isAlive())
			return;
		fnd.stop();
	}

	public static Collection<TriggerAdder> getTriggerAdders(DisplayContext ctx, Class cls, WrapperValue poj) {
		return copyOf(triggerAdders);
	}

	public static void addTriggerForClassInst(TriggerForClass utilClass) {
		theLogger.warn("Registering triggers from: " + utilClass);
		synchronized (objectContextMenuTriggers) {
			objectContextMenuTriggers.add(utilClass);
		}

	}

	public static void attachTo(Object anyObject, String title, Object child) {
		try {
			browserPanel.addToTreeListener.addChildObject(anyObject, title, child);
		} catch (PropertyVetoException e) {
			printStackTrace(e);
			throw reThrowable(e);
		}
	}

	static {
		new Thread("Init GUI") {
			@Override public void run() {
				// TODO Auto-generated method stub
				try {
					findAndloadMissingUtilityClasses();
				} catch (Throwable e) {
					printStackTrace(e);
				}
				try {
					findAndloadMissingTriggers();
				} catch (Throwable e) {
					printStackTrace(e);
				}
				try {
					singletAssemblerCacheGrabber.loadAssemblerInstances();
				} catch (Throwable e) {
					printStackTrace(e);
				}
				registerPanel(ModelAsTurtleEditor.class, Model.class);
			}
		}.start();

	}

	public static <T> Set<Class<? extends T>> getCoreClasses(Class<T> ancestor) {
		Set<Class<? extends T>> clz = new HashSet();
		try {
			clz.addAll(ClassFinder.getClasses("org.app", ancestor));
			clz.addAll(ClassFinder.getClasses("org.cog", ancestor));
			clz.addAll(ClassFinder.getClasses("org.rob", ancestor));
			clz.addAll(ClassFinder.getClasses("com.hr", ancestor));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return clz;

	}

	public static <T> T recast(Object obj, Class<T> objNeedsToBe) throws NoSuchConversionException {
		return recast(obj, objNeedsToBe, Converter.MCVT);
	}

	public static void addShutdownHook(Runnable runnable) {
		Runtime.getRuntime().addShutdownHook(new Thread(runnable, "Shutdown Hook for " + runnable));

	}

}