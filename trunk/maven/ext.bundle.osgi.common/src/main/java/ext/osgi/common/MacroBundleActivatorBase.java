/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.org).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package ext.osgi.common;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.PropertyConfigurator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Note:  Macro bundles are *not* required to use this class
 */

public abstract class MacroBundleActivatorBase implements BundleActivator, FrameworkListener {
	public static boolean MACRO_LAUNCHER = false;

	public static boolean isOSGIProperty(String string, Object value) {
		String sp = System.getProperty(string, null);
		if (sp == null)
			return value == null;
		String sv = "" + value;
		return sp.equalsIgnoreCase(sv);
	}

	public static void debugLoaders(String clazzName) {
		Class clazz = null;
		try {
			clazz = Class.forName(clazzName, false, null);
			clazz.getDeclaredMethods();
			clazz.getDeclaredFields();
			if (isOSGIProperty("test.classloader", true)) {
				Class class2;
				class2 = Class.forName(clazz.getName(), true, null);
				if (class2 != clazz) {
					warning("Classes not same as in current loader " + clazz);
					debugLoadersInfo(clazz);
					debugLoadersInfo(class2);
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			warning("Class has errors in current loader " + clazzName);
			if (clazz != null)
				debugLoadersInfo(clazz);
			if (e instanceof NoClassDefFoundError) {
				return;
			}
			if (e instanceof Error)
				throw (Error) e;
		}
	}

	public static void debugLoaders(Class clazz) {
		try {
			clazz.getDeclaredMethods();
			clazz.getDeclaredFields();
			if (isOSGIProperty("test.classloader", true)) {
				Class class2;
				class2 = Class.forName(clazz.getName(), true, null);
				if (class2 != clazz) {
					warning("Classes not same as in current loader " + clazz);
					debugLoadersInfo(clazz);
					debugLoadersInfo(class2);
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			warning("Class has errors in current loader " + clazz);
			debugLoadersInfo(clazz);
			if (e instanceof NoClassDefFoundError) {
				return;
			}
			if (e instanceof Error)
				throw (Error) e;
		}
	}

	public static void debugLoadersInfo(Class clazz) {
		ClassLoader myLoader = clazz.getClassLoader();
		ClassLoader pLoader = myLoader.getParent();
		ClassLoader gpLoader = pLoader.getParent();
		ClassLoader threadCL = Thread.currentThread().getContextClassLoader();
		ClassLoader systemCL = ClassLoader.getSystemClassLoader();

		trace("Classloader for the main class is: " + myLoader);
		trace("Parent Classloader is: " + pLoader);
		trace("GrandParent Classloader is: " + gpLoader);
		trace("ContextClassloader for currentThread is: " + threadCL);
		trace("SystemCL is: " + systemCL);
	}

	public static void debugResolve(String desc, ClassLoader cl, String resourcePath) {
		trace("------------");
		URL rurl = cl.getResource(resourcePath);
		trace("Classloader[" + desc + ", " + cl + "].getResource(" + resourcePath + ") = " + rurl);
		URL surl = ClassLoader.getSystemResource(resourcePath);
		trace("Classloader[" + desc + ", " + cl + "].getSystemResource(" + resourcePath + ") = " + surl);
		trace("------------");
	}

	protected static String bundleCtxName(BundleContext bc) {
		if (bc == null)
			return "NULL";
		return "" + bc.getBundle();
	}

	public static void trace(String string, Object... args) {
		System.out.println("[System.out] Trace: " + string);
	}

	public static void warning(String string, Object... args) {
		System.err.println("[System.err] Warning: " + string);
	}

	static public class TodoItem implements Runnable {

		private Runnable work;
		private Object doneBy;

		public TodoItem(String bundleActivatorBase, Runnable runnable) {
			this.work = runnable;
			this.doneBy = bundleActivatorBase;
		}

		@Override public String toString() {
			return "TodoItem: " + doneBy + " " + work;
		}

		@Override public void run() {
			work.run();
		}

		public void runProtected() {
			try {
				work.run();
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("runProtected " + this + " caused " + e);
			}

		}

		@Override public boolean equals(Object obj) {
			if (obj instanceof TodoItem) {
				return getName().compareTo(((TodoItem) obj).getName()) == 0;
			}
			if (work == obj)
				return true;
			return super.equals(obj);
		}

		public String getName() {
			return "" + doneBy;
		}
	}

	public interface BundleClassWatcher {

		void registerClassLoader(BundleActivator bundleActivatorBase, BundleContext bundleCtx);

		void unregisterClassLoader(BundleActivator bundleActivatorBase, BundleContext bundleCtx);

	}

	public static interface BootPhaseConst {
		int UNKNOWN = 0;
		int UNSTARTED = 10;
		int STARTING = 20;
		int COMPLETED_START = 30;
		int REGISTERINGSERVICES = 40;
		int COMPLETED_REGISTERSERVICES = 50;
		int FRAMEWORKSTARTING = 60;
		int COMPLETED_FRAMEWORKSTARTED = 70;
		int PRE_CONFIG = 71;
		int DURRING_CONFIG = 75;
		int POST_CONFIG = 79;
		int LAUNCHING = 80;
		int LAUNCHING_COMPLETE = 90;
		int RUNNING = 90;
	}

	public int bundleBootPhase = BootPhaseConst.UNSTARTED;
	public static String EQBAR = "========================================================================================";

	public BundleContext m_context;

	final public static MacroStartupSettings macroStartupSettings = new MacroStartupSettings();

	static public class MacroStartupSettings {

		public boolean flagTrue(String key) {
			return hasSetting(key) && sameValue(getSetting(key), Boolean.TRUE);
		}

		public void runNow(String key, Runnable value) {
			runTodoItem(asTodoItem(key, value));
		}

		public BundleActivator firstBundleActivatorBase = null;
		public Map settingsMap = new HashMap();
		public List<String> servicesBegun = new ArrayList<String>();
		public List<String> servicesMissing = new ArrayList<String>();
		public List<String> servicesDisabled = new ArrayList<String>();

		public void putSetting(String key, Object value) {
			key = toKeyCase(key);
			synchronized (settingsMap) {
				settingsMap.put(key, value);
			}
		}

		public <T> T getSetting(String key) {
			Object string = System.getProperty(key, System.getenv(key));
			if (string != null)
				return (T) string;
			String key2 = toKeyCase(key);
			string = System.getProperty(key2, System.getenv(key2));
			if (string != null)
				return (T) string;
			return (T) settingsMap.get(key2);
		}

		public boolean doRun(String key) {
			key = toKeyCase(key);
			if (servicesDisabled.contains(key)) {
				return false;
			}
			Object settingObject = getSetting(key);
			if (settingObject != null) {
				if (sameValue(settingObject, Boolean.TRUE))
					return true;
			} else {
				settingObject = getSetting("*");
				if (settingObject != null) {
					if (sameValue(settingObject, Boolean.FALSE))
						return false;
					if (sameValue(settingObject, Boolean.TRUE))
						return true;
				}
			}
			return true;
		}

		public boolean dontRun(String key) {
			key = toKeyCase(key);
			if (servicesBegun.contains(key)) {
				return true;
			}
			if (servicesDisabled.contains(key)) {
				return true;
			}
			Object settingObject = getSetting(key);
			if (settingObject != null) {
				if (sameValue(settingObject, Boolean.FALSE))
					return true;
			} else {
				settingObject = getSetting("*");
				if (settingObject != null) {
					if (sameValue(settingObject, Boolean.FALSE))
						return true;
				}
			}
			return false;
		}

		private boolean sameValue(Object v1, Object v2) {
			return ("" + v1).equalsIgnoreCase("" + v2);
		}

		public boolean hasSetting(String key) {
			synchronized (settingsMap) {
				return settingsMap.containsKey(toKeyCase(key));
			}
		}

		public void offerFirst(BundleActivator bundleActivatorBase) {
			if (firstBundleActivatorBase == null)
				firstBundleActivatorBase = bundleActivatorBase;
		}

		public void makeStartupBundle(BundleActivator activator) {
			firstBundleActivatorBase = activator;
		}

		public void runTodoList(List<TodoItem> registerServicesPhaseTodo) {
			if (registerServicesPhaseTodo == null)
				return;
			TodoItem item = null;
			while (!registerServicesPhaseTodo.isEmpty()) {
				synchronized (registerServicesPhaseTodo) {
					item = registerServicesPhaseTodo.remove(0);
				}
				runTodoItem(item);
			}

		}

		private void runTodoItem(TodoItem item) {
			String keyString = item.getName();
			keyString = toKeyCase(keyString);
			synchronized (servicesBegun) {
				if (servicesBegun.contains(keyString))
					return;
				if (dontRun(keyString)) {
					System.err.println("-------------Skipping (dontRun) " + keyString);
				}
				if (!doRun(keyString)) {
					System.err.println("---------Skipping (!doRun) " + keyString);
					return;
				}
				servicesBegun.add(keyString);
			}
			System.err.println("+++================= Running " + keyString);
			item.runProtected();
		}

		public static Map<Integer, List<TodoItem>> phaseTodosMap = new HashMap<Integer, List<TodoItem>>();

		public static List<TodoItem> getPhaseTodo(int phaseAt) {
			return getPhaseTodo(phaseAt, true);
		}

		public static List<TodoItem> getPhaseTodo(int phaseAt, boolean createIfMissing) {
			synchronized (phaseTodosMap) {
				if (createIfMissing && !phaseTodosMap.containsKey(phaseAt)) {
					phaseTodosMap.put(phaseAt, new ArrayList<TodoItem>());
				}
				return phaseTodosMap.get(phaseAt);
			}
		}

		public void launchPhases() {
			boolean runAgain = true;
			while (runAgain) {
				runAgain = false;
				for (int phaseAt = 0; phaseAt < 100; phaseAt++) {
					List<TodoItem> todos = getPhaseTodo(phaseAt, false);
					if (todos == null || todos.size() == 0)
						continue;
					runAgain = true;
					System.err.println("PHASE=" + phaseAt + " SIZE=" + todos.size());
					macroStartupSettings.runTodoList(todos);
					break;
				}
			}
		}

		public void addMacroService(int bootPhase, String key, Runnable value) {
			key = toKeyCase(key);
			synchronized (macroStartupSettings) {
				getPhaseTodo(bootPhase).add(asTodoItem(key, value));
			}
		}

		public void scheduleFrameworkStartEventHandler(final MacroBundleActivatorBase bundleActivatorBase) {
			String key = bundleActivatorBase.getClass().getName();
			addMacroService(BootPhaseConst.FRAMEWORKSTARTING, key + "_frameworkstarted", new Runnable() {
				@Override public void run() {
					try {
						bundleActivatorBase.dispatchFrameworkStartedEvent0(bundleActivatorBase.m_context.getBundle(), null);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

		}

		public void possiblyStart(final MacroBundleActivatorBase bundleActivatorBase) {
			bundleActivatorBase.m_context.addFrameworkListener(new FrameworkListener() {
				@Override public void frameworkEvent(FrameworkEvent fe) {
					int eventType = fe.getType();
					if (eventType == FrameworkEvent.STARTED) {
						launchPhases();
					}

				}
			});

		}

	}

	public void addMacroService(String key, Runnable value) {
		addMacroService(BootPhaseConst.RUNNING, key, value);
	}

	public void addMacroPreService(String key, Runnable value) {
		addMacroService(BootPhaseConst.PRE_CONFIG, key, value);
	}

	public void addMacroService(int bootPhase, String key, Runnable value) {
		key = toKeyCase(key);
		synchronized (macroStartupSettings) {
			macroStartupSettings.addMacroService(bootPhase, key, value);
		}
	}

	public static TodoItem asTodoItem(String key, Runnable value) {
		if (value instanceof TodoItem)
			return (TodoItem) value;
		key = toKeyCase(key);
		return new TodoItem(key, value);
	}

	public void addMacroServiceLocal(int bootPhase, String key, Runnable value) {
		key = toKeyCase(key);
		String pn = getClass().getPackage().getName();
		pn = pn.substring(pn.indexOf('.') + 1);
		pn = toKeyCase(pn + key);
		addMacroService(bootPhase, key, value);
	}

	public static String toKeyCase(String key) {
		key = key.toLowerCase();
		boolean remove = true;
		while (remove) {
			remove = false;
			for (String s : new String[] { "start", "launch", "osgi.", "core.", "lib.", ".", "com.", "org.", "hrkind.", "appdapter.", "friendularity.", "cogchar.", "ext.", "bundle.", "-", "is",
					"use", "init", "connect", }) {
				if (key.startsWith(s) && key.length() + 2 > s.length()) {
					key = key.substring(s.length());
					remove = true;
				}
			}
		}
		key = key.replace("activator", "");
		key = key.replace(".", "_");
		key = key.replace("__", "_");

		key = key.replace("framework", "fw");

		key = key.replace("frame", "panel");
		key = key.replace("spanel", "panel");
		key = key.replace("panel", "gui");
		return key;
	}

	protected boolean startedEventScheduled = false;
	protected boolean hasDispatchedFrameworkStartedEvent = false;
	final protected Object dispatchEventLock = new Object();

	private boolean legacyStartWasOverriden_Check = false;
	public static BundleClassWatcher classLoaderUtils;

	/**
	 * Receives notification of a general {@code FrameworkEvent} object.
	 *
	 * @param event The {@code FrameworkEvent} object.
	 */
	public void frameworkEvent(FrameworkEvent fe) {
		int eventType = fe.getType();
		if (eventType == FrameworkEvent.STARTED) {
			getLogger().info("********  OSGi Framework has STARTED, calling dispatchFrameworkStartedEvent()");
			dispatchFrameworkStartedEvent0(fe.getBundle(), fe.getThrowable());
		} else {
			getLogger().info("************************ Got frameworkEvent with eventType=" + eventType + ", bundle=" + fe.getBundle());
		}
	}

	public MacroBundleActivatorBase() {
		synchronized (macroStartupSettings) {
			macroStartupSettings.offerFirst(this);
			if (classLoaderUtils != null) {
				classLoaderUtils.registerClassLoader(this, null);
			}
		}
	}

	/**
	 * There are two things you might do in your overriding start() method, which
	 * are generally only done in asingle  "top" bundle (generally corresponding to a project
	 * that you want to "run" from maven, or use as the seed of your own launch strategy).
	 *
	 * If you call these methods from multiple bundles, then you will have to deal with
	 * the fact that those invocations (and their callbacks) may generally occur in any order.
	 *
	 * 1) Call forceLog4JConfig() if you like Log4J logging.
	 * 2) Call scheduleFrameworkStartEventHandler() to schedule a callback allowing you to
	 * safely begin your application setup *after* all initial bundles have been started.
	 *
	 * @param bundleCtx
	 * @throws Exception
	 */
	@Override public void start(BundleContext bundleCtx) throws Exception {
		this.m_context = bundleCtx;
		synchronized (startedBundles) {
			if (startedBundles.contains(this))
				return;
			addStartedBundle(this);
		}
		synchronized (macroStartupSettings) {
			macroStartupSettings.offerFirst(this);
			if (classLoaderUtils != null) {
				classLoaderUtils.registerClassLoader(this, bundleCtx);
			}
		}
		//getLogger().info(describe("start<BundleActivatorBase>", bundleCtx));
		bundleCtx.addFrameworkListener(this);
		//scheduleFrameworkStartEventHandler(bundleCtx);
		bundleBootPhase = BootPhaseConst.UNSTARTED;
	}

	public static ArrayList<BundleActivator> startedBundles = new ArrayList<BundleActivator>();

	private void addStartedBundle(BundleActivator bundleActivatorBase) {
		synchronized (startedBundles) {
			if (startedBundles.contains(bundleActivatorBase))
				return;
			startedBundles.add(bundleActivatorBase);
		}
		String key = bundleActivatorBase.getClass().getName();
		/*
		addMacroService(BootPhaseConst.STARTING, key + "_start", new Runnable() {
			@Override public void run() {
				try {
					bundleBootPhase = BootPhaseConst.STARTING;
					legacyStartWasOverriden_Check = true;
					registerConfig(m_context);
					if (legacyStartWasOverriden_Check == true) {
						getLogger().warn("This bundle used move_from_legacy_start");
					}
				} catch (Exception e) {
					e.printStackTrace();
					bundleBootPhase = BootPhaseConst.COMPLETED_START;
				}
			}
		});*/
		addMacroService(BootPhaseConst.REGISTERINGSERVICES, key + "_registerservices", new Runnable() {
			@Override public void run() {
				bundleBootPhase = BootPhaseConst.REGISTERINGSERVICES;
				try {
					registerServices(m_context);
				} catch (Exception e) {
					getLogger().error("registerServices: " + e, e);
					e.printStackTrace();
				}
				bundleBootPhase = BootPhaseConst.COMPLETED_REGISTERSERVICES;
			}
		});
		addMacroService(BootPhaseConst.FRAMEWORKSTARTING, key + "_frameworkstarted", new Runnable() {
			@Override public void run() {
				bundleBootPhase = BootPhaseConst.FRAMEWORKSTARTING;
				try {
					dispatchFrameworkStartedEvent0(m_context.getBundle(), null);
				} catch (Exception e) {
					getLogger().error("registerServices: " + e, e);
					e.printStackTrace();
				}
				bundleBootPhase = BootPhaseConst.COMPLETED_FRAMEWORKSTARTED;
			}
		});
		addMacroService(BootPhaseConst.LAUNCHING, key + "_launch", new Runnable() {
			@Override public void run() {
				bundleBootPhase = BootPhaseConst.LAUNCHING;
				try {
					if (isLauncherBundle()) {
						launchApplication(m_context);
					}
				} catch (Exception e) {
					getLogger().error("LAUNCHING: " + e, e);
					e.printStackTrace();
				}
				bundleBootPhase = BootPhaseConst.RUNNING;
			}
		});
	}

	@Override public void stop(BundleContext bundleCtx) throws Exception {
		synchronized (macroStartupSettings) {
			if (classLoaderUtils != null) {
				classLoaderUtils.unregisterClassLoader(this, bundleCtx);
			}
		}
		getLogger().info(describe("stop<BundleActivatorBase>", bundleCtx));
	}

	static protected String getBundleName(BundleActivator key) {
		return key.getClass().getName();
	}

	/** The code in here should be moved between two methods:
	 *
	 *
	 *  handleFrameworkStartedEvent  =
	 *     everything has had start(.) called now
	 *     the best time to create objects and populate fields
	 *     hopefully the best time to win a race at setting a OSGi framework property
	 *
	 * registerServices = time to change properies and register serveices
	 *
	 *  laucheBundle =
	 * 		everything has had handleFrameworkStartedEvent(.) called now
	 *			use isLauncherBundle() in your code to see if you are the toplevel bundle
	 *
	 * We might go to three methods
	 */
	protected void registerConfig(BundleContext bundleCtx) throws Exception {
		legacyStartWasOverriden_Check = false;
	}

	protected void registerServices(BundleContext context0) {

	}

	/** Override this method if you need notification after all bundles in your container have been started.
	 * Normally you would override this method in a "top" bundle to setup your application code.
	 * Important:  This handler will be called now regardless if you called scheduleFrameworkStartEventHandler()
	 * @param bundleCtx - Fetched from the bundle of the FrameworkEvent, so it should be nice and fresh.
	 */
	protected void handleFrameworkStartedEvent(BundleContext bundleCtx) throws Exception {

	}

	/** Override this method with the code that you'd use if this Bundle was intend to be Launched
	 * However, it will only be called if this was the launched bundle like -Prun-on-felix
	 */
	protected void launchApplication(BundleContext bundleCtx) throws Exception {
		if (isLauncherBundle()) {
			getLogger().warn("this bundle was missing LAUNCHING");
		}
	}

	/**
	 * Call this method from any bundle's start() to schedule a callback to its handleFrameworkStartedEvent() method.
	 *
	 * @param bundleCtx - used to schedule the callback, and then forgotten.
	 */
	final protected void scheduleFrameworkStartEventHandler(BundleContext bundleCtx) {
		synchronized (dispatchEventLock) {
			if (startedEventScheduled)
				return;
			startedEventScheduled = true;
		}
		if (MACRO_LAUNCHER) {
			MacroBundleActivatorBase.macroStartupSettings.scheduleFrameworkStartEventHandler(this);
			return;
		}
		bundleCtx.addFrameworkListener(this);
	}

	final void dispatchFrameworkStartedEvent0(Bundle eventBundle, Throwable eventThrowable) {
		String thrownMsg = (eventThrowable == null) ? "OK" : eventThrowable.getClass().getName();
		getLogger().info("dispatchFrameworkStartedEvent<BundleActivatorBase> ( bundle={}, msg={}", eventBundle, thrownMsg);
		if (eventThrowable == null) {
			BundleContext bc = eventBundle.getBundleContext();
			if (bc == null) {
				getLogger().info("Cannot find bundle context for event bundle, so there will be no callback to app startup: {} ", eventBundle);
			}
			try {
				if (MACRO_LAUNCHER) {
					if (isLauncherBundle()) {
						MACRO_LAUNCHER = false;
						reallyLaunch(this);
					}
				} else {
					synchronized (dispatchEventLock) {
						if (hasDispatchedFrameworkStartedEvent)
							return;
						hasDispatchedFrameworkStartedEvent = true;
					}
					handleFrameworkStartedEvent(bc);
				}
			} catch (Exception e) {
				getLogger().error("handleFrameworkStartedEvent " + e, e);
			}
		} else {
			getLogger().warn("No callback to application startup, due to throwable ", eventThrowable);
		}
	}

	/**
	 * If you override this
	 * @param eventBundle
	 * @param eventThrowable
	 */
	private void reallyLaunch(BundleActivator bundleActivatorBase) {
		macroStartupSettings.launchPhases();
		//launchApplication(bc);

	}

	protected String describe(String action, BundleContext bundleCtx) {
		Bundle b = bundleCtx.getBundle();
		String msg = getClass().getCanonicalName() + "." + action + "(ctx=[" + bundleCtx + "], bundle=[" + b + "])";
		return msg;
	}

	protected Logger myLogger;

	protected Logger getLogger() {
		if (myLogger == null) {
			myLogger = LoggerFactory.getLogger(getClass());
		}
		return myLogger;
	}

	protected void forceLog4jConfig() {
		if (isLauncherBundle())
			forceLog4jConfig0();
	}

	public void logInfo(String msg) {
		getLogger().info(msg);
	}

	public boolean isLauncherBundle() {
		synchronized (macroStartupSettings) {
			return macroStartupSettings.firstBundleActivatorBase == this;
		}
	}

	/**
	 * This should usually be called only once during a system's runtime lifetime.
	 * (To be tested:  works OK to update properties at runtime?)
	 *
	 * Prints some debug to stdout about known classloaders, then builds a URL using the
	 * *local* CL of this concrete class (which will be *your* class is you make subtypes
	 * of BasicDebugger or BundleActivatorBase!) to create a resource URL into the classpath
	 * (under OSGi, the bundle-classpath-of) of that concrete class, which under OSGi will
	 * look something like:
	 *
	 *  bundle://221.0:1/log4j.properties
	 *
	 * ...which is then passed to Log4jFuncs.forceLog4jConfig.
	 *
	 * What this means is that you can put a log4j.properties into any bundle of your own,
	 * then call "forceLog4jConfig()" ONCE from that bundle's activator (or framework-START-event
	 * handler), and then your properties should be in place.
	 */
	protected void forceLog4jConfig0() {
		// Logger logger = getLogger();

		// To get more determinism over when this happens (before other bundles that use logging are launched),
		// need to mess with Felix auto-start properties?
		ClassLoader threadCL = Thread.currentThread().getContextClassLoader();
		ClassLoader localCL = getClass().getClassLoader();
		System.out.println("thread-context-CL=" + threadCL);
		System.out.println("local-CL=" + localCL);

		String resPath = "log4j.properties";

		URL threadURL = threadCL.getResource(resPath);
		URL localURL = localCL.getResource(resPath);
		System.out.println("[System.out] forceLog4jConfig() threadCL resolved " + resPath + " to threadURL " + threadURL);
		System.out.println("[System.out] forceLog4jConfig() localCL resolved  " + resPath + " to  localURL " + localURL);
		System.out.println("[System.out] " + getClass().getCanonicalName() + " is forcing Log4J to read config from localURL: " + localURL);

		try {

			forceLog4jConfig(localURL);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		try {
			Logger logger = getLogger();

			try {
				logger.warn("{forceLog4JConfig} - This message was printed at WARN level to SLF4J, after forcing config for Log4J to localURL: " + localURL);
			} catch (Throwable t2) {
				t2.printStackTrace();
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static void forceLog4jConfig(URL propertiesURL) {
		System.out.println(new StringBuilder().append("[System.out] ").append(MacroBundleActivatorBase.class.getName()).append(" is forcing Log4J to read config from propertiesURL: ")
				.append(propertiesURL).toString());
		PropertyConfigurator.configure(propertiesURL);
	}

	public void ensureExtClassesAreFindable() {
		// TODO Auto-generated method stub

	}

}
