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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.PropertyConfigurator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Note: Macro bundles are *not* required to use this class
 */

public abstract class MacroBundleActivatorBase implements BundleActivator, FrameworkListener {
	public interface ListenableMap<K, V> {
		public static final String PROP_PUT = "put";

		public V putNoFire(K k, V v);

		public V removeNoFire(K k);

		public void addPropertyChangeListener(PropertyChangeListener listener);

		public void removePropertyChangeListener(PropertyChangeListener listener);

		Set<Map.Entry<K, V>> entrySet();

		public V get(K name);
	}

	public static boolean MACRO_LAUNCHER = false;

	public static boolean isOSGIProperty(final String string, final Object value) {
		final String sp = System.getProperty(string, null);
		if (sp == null)
			return value == null;
		final String sv = "" + value;
		return sp.equalsIgnoreCase(sv);
	}

	static public class ListenerMap<K, V> extends HashMap<K, V> implements Map<K, V>, ListenableMap<K, V> {
		public class LEntry {

			private final K key;
			private final V value;

			public LEntry(final K k, final V v) {
				this.key = k;
				this.value = v;
			}

			public K getKey() {
				return key;
			}

			public Object getValue() {
				return value;
			}

		}

		private final PropertyChangeSupport propertySupport;

		public ListenerMap() {
			super();
			propertySupport = new PropertyChangeSupport(this);
		}

		@Override
		public V put(final K k, final V v) {
			final V old = super.put(k, v);
			propertySupport.firePropertyChange(PROP_PUT, newEntry(k, old), newEntry(k, v));
			return old;
		}

		private Object newEntry(final K k, final V v) {
			return new LEntry(k, v);
		}

		@Override
		public V remove(final Object k) {
			final V old = super.remove(k);
			propertySupport.firePropertyChange(PROP_PUT, old, null);
			return old;
		}

		@Override
		public V removeNoFire(final Object k) {
			final V old = super.remove(k);
			return old;
		}

		@Override
		public V putNoFire(final K k, final V v) {
			final V old = super.put(k, v);
			return old;
		}

		@Override
		public void addPropertyChangeListener(final PropertyChangeListener listener) {
			propertySupport.addPropertyChangeListener(listener);
		}

		@Override
		public void removePropertyChangeListener(final PropertyChangeListener listener) {
			propertySupport.removePropertyChangeListener(listener);
		}
	}

	public static void debugLoaders(final String clazzName) {
		Class clazz = null;
		try {
			clazz = Class.forName(clazzName, false, null);
			clazz.getDeclaredMethods();
			clazz.getDeclaredFields();
			if (isOSGIProperty("test.classloader", true)) {
				final Class class2;
				class2 = Class.forName(clazz.getName(), true, null);
				if (class2 != clazz) {
					warning("Classes not same as in current loader " + clazz);
					debugLoadersInfo(clazz);
					debugLoadersInfo(class2);
				}
			}
		} catch (final Throwable e) {
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

	public static void debugLoaders(final Class clazz) {
		try {
			clazz.getDeclaredMethods();
			clazz.getDeclaredFields();
			if (isOSGIProperty("test.classloader", true)) {
				final Class class2;
				class2 = Class.forName(clazz.getName(), true, null);
				if (class2 != clazz) {
					warning("Classes not same as in current loader " + clazz);
					debugLoadersInfo(clazz);
					debugLoadersInfo(class2);
				}
			}
		} catch (final Throwable e) {
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

	public static void debugLoadersInfo(final Class clazz) {
		final ClassLoader myLoader = clazz.getClassLoader();
		final ClassLoader pLoader = myLoader.getParent();
		final ClassLoader gpLoader = pLoader.getParent();
		final ClassLoader threadCL = Thread.currentThread().getContextClassLoader();
		final ClassLoader systemCL = ClassLoader.getSystemClassLoader();

		trace("Classloader for the main class is: " + myLoader);
		trace("Parent Classloader is: " + pLoader);
		trace("GrandParent Classloader is: " + gpLoader);
		trace("ContextClassloader for currentThread is: " + threadCL);
		trace("SystemCL is: " + systemCL);
	}

	public static void debugResolve(final String desc, final ClassLoader cl, final String resourcePath) {
		trace("------------");
		final URL rurl = cl.getResource(resourcePath);
		trace("Classloader[" + desc + ", " + cl + "].getResource(" + resourcePath + ") = " + rurl);
		final URL surl = ClassLoader.getSystemResource(resourcePath);
		trace("Classloader[" + desc + ", " + cl + "].getSystemResource(" + resourcePath + ") = " + surl);
		trace("------------");
	}

	protected static String bundleCtxName(final BundleContext bc) {
		if (bc == null)
			return "NULL";
		return "" + bc.getBundle();
	}

	public static void trace(final String string, final Object... args) {
		System.out.println("[System.out] Trace: " + string);
	}

	public static void warning(final String string, final Object... args) {
		System.err.println("[System.err] Warning: " + string);
	}

	public enum TodoItemState {
		UNSTARTED, RUNNING, PAUSED, COMPLETED, EXCEPTION
	}

	static public class TodoItem implements Runnable {
		TodoItemState state = TodoItemState.UNSTARTED;
		Throwable lastException = null;
		private final Runnable work;
		private final Object doneBy;
		static Object stateLock = new Object();

		public TodoItem(final String name, final Runnable runnable) {
			this.work = runnable;
			this.doneBy = name;
		}

		@Override
		public String toString() {
			return "TodoItem:  " + state + " " + doneBy + " " + work;
		}

		@Override
		public void run() {
			synchronized (stateLock) {
				if (state != TodoItemState.UNSTARTED) {
					return;
				}
				state = TodoItemState.RUNNING;
			}
			try {
				work.run();
				synchronized (stateLock) {
					state = TodoItemState.COMPLETED;
				}
			} catch (final Throwable e) {
				synchronized (stateLock) {
					state = TodoItemState.EXCEPTION;
				}
				lastException = e;
				e.printStackTrace();
				System.err.println("runProtected " + this + " caused " + e);
			}
		}

		@Override
		public boolean equals(final Object obj) {
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
		final public int UNKNOWN = 0;
		public int UNSTARTED = 10;
		public int STARTING = 20;
		public int COMPLETED_START = 30;
		public int REGISTERINGSERVICES = 40;
		public int COMPLETED_REGISTERSERVICES = 50;
		public int FRAMEWORKSTARTING = 60;
		public int COMPLETED_FRAMEWORKSTARTED = 70;
		public int PRE_CONFIG = 71;
		public int DURRING_CONFIG = 75;
		public int POST_CONFIG = 79;
		public int LAUNCHING = 80;
		public int LAUNCHING_COMPLETE = 90;
		public int RUNNING = 90;
		public int RUNNING_COMPLETED_ERROR = 99;
		public int RUNNING_COMPLETE = 100;
		public int EXITED_COMPLETE = 101;
		public int ON_DEMAND = 200;
	}

	public static String getPhaseName(final Object phase) {
		final Class enumClass = BootPhaseConst.class;
		final String s = getEnumValueName(phase, enumClass);
		if (s != null)
			return s;
		return "UNKNOWN_" + phase;
	}

	public static String getEnumValueName(final Object phase, Class searchClass) {
		Class phaseClass = null;
		boolean isNull = true;
		int phaseHC = Integer.MIN_VALUE;
		if (phase != null) {
			isNull = false;
			phaseClass = phase.getClass();
			phaseHC = phase.hashCode();
		}
		for (final Field f : searchClass.getDeclaredFields()) {
			final int mods = f.getModifiers();
			if (Modifier.isStatic(mods)) {
				try {
					if (!f.isAccessible())
						f.setAccessible(true);
					final Object fv = f.get(null);
					if (fv == null) {
						if (isNull) {

							return f.getName();
						}

						continue;
					}
					if (isNull)
						continue;
					if (phaseHC == fv.hashCode()) {
						if (!phase.equals(fv))
							continue;
						return f.getName();
					}
				} catch (final IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (final IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		for (final Class cc : searchClass.getInterfaces()) {
			final String r = getEnumValueName(phase, cc);
			if (r != null)
				return r;
		}
		searchClass = searchClass.getSuperclass();
		if (searchClass != null) {
			return getEnumValueName(phase, searchClass);
		}
		return null;
	}

	public int bundleBootPhase = BootPhaseConst.UNSTARTED;
	public static String EQBAR = "========================================================================================";

	public BundleContext m_context;
	static public Map<String, String> settingsName = new HashMap();
	final public static MacroStartupSettings macroStartupSettings = new MacroStartupSettings();

	static public class MacroStartupSettings implements BootPhaseConst {

		public ListenerMap actionCallbackMap = new ListenerMap();
		public Map<String, Integer> defaultStartLevel = new HashMap();

		public boolean flagTrue(final String key) {
			return hasSetting(key) && sameValue(getSetting(key), Boolean.TRUE);
		}

		public void runNow(final String key, final Runnable value) {
			runTodoItem(asTodoItem(key, value));
		}

		public void runNow(final String key) {
			runTodoItem(asTodoItem(key, getRunnable(key)));
		}

		public Runnable getRunnable(String key) {
			key = toKeyCase(key);
			synchronized (actionCallbackMap) {
				return (Runnable) actionCallbackMap.get(key);
			}
		}

		public BundleActivator firstBundleActivatorBase = null;
		public Map<String, Object> settingsMap = new HashMap();
		public HashSet<String> servicesBegun = new HashSet<>();

		public HashSet<String> servicesMissing = new HashSet<>();
		public HashSet<String> servicesDisabled = new HashSet<>();
		public HashSet<String> servicesKnown = new HashSet<>();
		public final Map<Integer, List<TodoItem>> servicesTodo;

		private MacroStartupSettings() {
			this.servicesTodo = phaseTodosMap;
		}

		private void addServiceName(String key) {
			key = toKeyCase(key);
			if (servicesKnown.contains(key))
				return;
			servicesKnown.add(key);
		}

		public void putSetting(String key, final Object value) {
			key = toKeyCase(key);
			synchronized (settingsMap) {
				final Object old = settingsMap.put(key, value);
				if (value instanceof Boolean) {
					setServiceEnabled(key, (boolean) (Boolean) value);
				}
				if (!sameValue(old, value)) {
					handleEvent(key);
				}

			}
		}

		public void setServiceEnabled(String key, final boolean b) {
			addServiceName(key);
			key = toKeyCase(key);
			if (b) {
				if (servicesDisabled.remove(key)) {
					//servicesBegun.remove(key);
					handleEvent(key);
				}
			} else {
				if (servicesDisabled.add(key)) {
					handleEvent(key);
				}
			}

		}

		public String getProperty(final Bundle bundle, final String defult, final String... tryFrom) {
			boolean blankString = false;
			BundleContext context = null;
			if (bundle != null) {
				context = bundle.getBundleContext();
			}
			for (final String s : tryFrom) {

				String args = null;
				if (bundle != null) {
					args = context.getProperty(s);
				}
				if (args != null) {
					if (args.length() == 0) {
						blankString = true;
						continue;
					}
					return args;
				}
				final Object oargs = getSetting(s);
				if (oargs != null)
					return "" + oargs;
			}
			if (blankString)
				return "";
			return defult;
		}

		public <T> T getSetting(final String key) {
			Object string = System.getProperty(key, System.getenv(key));
			if (string != null)
				return (T) string;
			final String key2 = toKeyCase(key);
			string = System.getProperty(key2, System.getenv(key2));
			if (string != null)
				return (T) string;
			return (T) settingsMap.get(key2);
		}

		public boolean isEnabled(String key) {
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

		public boolean isBegun(String key) {
			key = toKeyCase(key);
			if (servicesBegun.contains(key)) {
				return true;
			}
			return false;
		}

		public boolean isService(String key) {
			key = toKeyCase(key);
			if (servicesKnown.contains(key)) {
				return true;
			}
			return false;
		}

		public boolean isRunnable(String key) {
			key = toKeyCase(key);
			if (actionCallbackMap.containsKey(key)) {
				return true;
			}
			return false;
		}

		public boolean isDisabled(String key) {
			key = toKeyCase(key);
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

		private boolean sameValue(final Object v1, final Object v2) {
			if (v1 == v2)
				return true;

			return ("" + v1).equalsIgnoreCase("" + v2);
		}

		public boolean hasSetting(final String key) {
			synchronized (settingsMap) {
				return settingsMap.containsKey(toKeyCase(key));
			}
		}

		public void offerFirst(final BundleActivator bundleActivatorBase) {
			if (firstBundleActivatorBase == null)
				firstBundleActivatorBase = bundleActivatorBase;
		}

		public void makeStartupBundle(final BundleActivator activator) {
			firstBundleActivatorBase = activator;
		}

		public void runTodoList(final List<TodoItem> registerServicesPhaseTodo) {
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

		private void runTodoItem(final TodoItem item) {
			String key = item.getName();
			key = toKeyCase(key);
			synchronized (servicesBegun) {
				if (isBegun(key))
					return;
				if (isDisabled(key)) {
					System.err.println("-------------Skipping (dontRun) " + key);
				}
				if (!isEnabled(key)) {
					System.err.println("---------Skipping (!doRun) " + key);
					return;
				}
				servicesBegun.add(key);
			}
			System.err.println("+++================= Running " + key);
			handleEvent(key);
			item.run();
		}

		public static Map<Integer, List<TodoItem>> phaseTodosMap = new HashMap<>();

		public static List<TodoItem> getPhaseTodo(final int phaseAt) {
			return getPhaseTodo(phaseAt, true);
		}

		public static List<TodoItem> getPhaseTodo(final int phaseAt, final boolean createIfMissing) {
			synchronized (phaseTodosMap) {
				if (createIfMissing && !phaseTodosMap.containsKey(phaseAt)) {
					phaseTodosMap.put(phaseAt, new ArrayList<TodoItem>());
				}
				return phaseTodosMap.get(phaseAt);
			}
		}

		public void raiseToPhase(final int top) {
			boolean runAgain = true;
			while (runAgain) {
				runAgain = false;
				for (int phaseAt = 0; phaseAt < top; phaseAt++) {
					final List<TodoItem> todos = getPhaseTodo(phaseAt, false);
					if (todos == null || todos.size() == 0)
						continue;
					runAgain = true;
					System.err.println("PHASE=" + getPhaseName(phaseAt) + " SIZE=" + todos.size());
					macroStartupSettings.runTodoList(todos);
					break;
				}
			}
		}

		public void launchPhases() {
			raiseToPhase(BootPhaseConst.RUNNING_COMPLETE);
		}

		public void addMacroService(final int bootPhase, String key, final Runnable value) {
			key = toKeyCase(key);
			addServiceName(key);
			final TodoItem todoItem = asTodoItem(key, value);
			synchronized (actionCallbackMap) {
				actionCallbackMap.put(key, todoItem);
				handleEvent(key);
			}
			synchronized (macroStartupSettings) {
				getPhaseTodo(bootPhase).add(todoItem);
				defaultStartLevel.put(key, bootPhase);
			}
		}

		public void scheduleFrameworkStartEventHandler(final MacroBundleActivatorBase bundleActivatorBase) {
			final String key = bundleActivatorBase.getClass().getName();
			addMacroService(BootPhaseConst.FRAMEWORKSTARTING, key + "_frameworkstarted", new Runnable() {
				@Override
				public void run() {
					try {
						bundleActivatorBase.dispatchFrameworkStartedEvent0(bundleActivatorBase.m_context.getBundle(), null);
					} catch (final Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

		}

		public void possiblyStart(final MacroBundleActivatorBase bundleActivatorBase) {
			bundleActivatorBase.m_context.addFrameworkListener(new FrameworkListener() {
				@Override
				public void frameworkEvent(final FrameworkEvent fe) {
					final int eventType = fe.getType();
					if (eventType == FrameworkEvent.STARTED) {
						launchPhases();
					}

				}
			});

		}

		public String getFieldValuesString() {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final PrintStream ps = new PrintStream(baos);
			printMacroStateInfo(ps, null, true);
			final String content = baos.toString(); // e.g. ISO-8859-1
			return content;
		}

		public void printMacroStateInfo(final PrintStream ps, final BundleContext bc, final boolean onlyNoteable) {
			if (bc != null) {
				final Bundle[] bundles = bc.getBundles();
				for (final Bundle b : bundles) {
					final int state = b.getState();
					if (onlyNoteable && state == Bundle.ACTIVE)
						continue;
					ps.println(b.getBundleId() + ": " + toBundleStateString(state) + " " + getBundleName(b));
				}
			}
			final Set<String> names = new HashSet<>(servicesBegun);
			names.addAll(servicesDisabled);
			names.addAll(servicesMissing);
			synchronized (settingsMap) {
				names.addAll(settingsMap.keySet());
			}
			for (final String n : names) {
				if (servicesBegun.contains(n)) {
					if (onlyNoteable)
						continue;
				}
				final String valuestatus = getValueStatus(n);
				ps.println(n + "=" + valuestatus);
			}
		}

		public String getServiceName(String n) {
			n = toKeyCase(n);
			final String name = settingsName.get(n);
			if (name != null)
				return name;
			return n;
		}

		public String getValueStatus(String n) {
			n = toKeyCase(n);
			String status = "";
			String value = getProperty(null, null, n);
			if (servicesBegun.contains(n)) {
				status += " %BEGUN";
			}
			if (servicesDisabled.contains(n)) {
				status += " %DISABLED";
			}
			if (servicesMissing.contains(n)) {
				status += " %MISSING";
			}
			if (status == "") {
				status = " %WAITING";
			}
			if (value == null)
				value = "";
			final String valuestatus = value + status;
			return valuestatus;
		}

		/**
		 * Return a String representation of a bundle state
		 */
		public static String toBundleStateString(final int state) {
			switch (state) {
				case Bundle.UNINSTALLED:
					return "UNINSTALLED";
				case Bundle.INSTALLED:
					return "INSTALLED";
				case Bundle.RESOLVED:
					return "RESOLVED";
				case Bundle.STARTING:
					return "STARTING";
				case Bundle.STOPPING:
					return "STOPPING";
				case Bundle.ACTIVE:
					return "ACTIVE";
				default:
					return "UNKNOWN";
			}
		}

		public void removeBegun() {
			synchronized (servicesBegun) {
				synchronized (actionCallbackMap) {
					for (final Object n : servicesBegun.toArray()) {
						actionCallbackMap.remove(n);
					}
				}
			}
			// TODO Auto-generated method stub

		}

		public void ensureReady(String key) {
			key = toKeyCase(key);
			addServiceName(key);
			if (!isBegun(key)) {
				runNow(key);
			}
		}
		//  This feature needs to move to JFlux.
		//  Appdapter is no longer allowed to use
		//  import org.jflux.api.core.Listener;
		//	private List<Listener<String>> myListeners = new ArrayList<Listener<String>>();

		public void handleEvent(final String event) {
			// STU SEZ:  CAn we see the tasty events we're missinggetLogger().info("Received event {}", event);
			//		for (Listener<String> l : myListeners) {
			//			l.handleEvent(event);
			//		}
		}
/*
		public void registerServiceChanged(Listener<String> listener) {
			if (listener == this) {
				return;
			}
			if (!myListeners.contains(listener)) {
				myListeners.add(listener);
			}
		}

		public void removeListener(Listener<String> listener) {
			myListeners.remove(listener);
		}
		*/
	}

	public void addMacroService(final String key, final Runnable value) {
		addMacroService(BootPhaseConst.RUNNING, key, value);
	}

	public void addMacroService(final String key, final boolean enabled, final Runnable value) {
		if (!macroStartupSettings.isDisabled(key))
			macroStartupSettings.setServiceEnabled(key, enabled);
		addMacroService(BootPhaseConst.RUNNING, key, value);
	}

	public void addMacroPreService(final String key, final Runnable value) {
		addMacroService(BootPhaseConst.PRE_CONFIG, key, value);
	}

	public void addMacroService(final int bootPhase, String key, final Runnable value) {
		key = toKeyCase(key);
		synchronized (macroStartupSettings) {
			macroStartupSettings.addMacroService(bootPhase, key, value);
		}
	}

	public static TodoItem asTodoItem(String key, final Runnable value) {
		if (value instanceof TodoItem)
			return (TodoItem) value;
		key = toKeyCase(key);
		return new TodoItem(key, value);
	}

	public void addMacroServiceLocal(final int bootPhase, String key, final Runnable value) {
		key = toKeyCase(key);
		String pn = getClass().getPackage().getName();
		pn = pn.substring(pn.indexOf('.') + 1);
		pn = toKeyCase(pn + key);
		addMacroService(bootPhase, key, value);
	}

	public static String toKeyCase(String key) {
		final String name = key;
		key = key.toLowerCase();
		boolean remove = true;
		while (remove) {
			remove = false;
			for (final String s : new String[]{"start", "launch", "osgi.", "core.", "lib.", ".", "com.", "org.", "hrkind.", "appdapter.", "friendularity.", "cogchar.", "ext.", "cogchar", "bundle.", "-", "is", "use", "init", "connect", "call", "connect",}) {
				if (key.startsWith(s) && key.length() + 2 > s.length()) {
					key = key.substring(s.length());
					remove = true;
				}
			}
		}
		while (remove) {
			remove = false;
			for (final String s : new String[]{"application", "app", "lifecycles", "lifecycle", "service", "services", "launcher",}) {
				if (key.endsWith(s) && key.length() + 2 > s.length()) {
					key = key.substring(0, key.length() - s.length());
					remove = true;
				}
			}
		}
		key = key.replace("behaviour", "behavior");
		key = key.replace("behaviormaster", "bm");
		key = key.replace("behavior", "bm");
		key = key.replace("bmdemo", "bmd");
		key = key.replace("bmd", "bm");
		key = key.replace("configuration", "config");
		key = key.replace("config", "conf");

		key = key.replace("activator", "");
		key = key.replace(".", "_");
		key = key.replace("__", "_");

		key = key.replace("framework", "fw");

		key = key.replace("frame", "panel");
		key = key.replace("spanel", "panel");
		key = key.replace("panel", "gui");

		if (false) {
			if (key.indexOf("gui") > 0) {
				key = "gui_" + key.replace("gui", "");
			} else if (key.indexOf("conf") > 0) {
				key = "conf_" + key.replace("conf", "");
			} else if (key.indexOf("launch") > 0) {
				key = "launch_" + key.replace("launch", "");
			} else if (!key.startsWith("launch_")) {
				key = "launch_" + key;
			}
		}
		final String oldName = settingsName.get(key);
		if (oldName == null || oldName.length() < name.length() || (oldName.length() == name.length() && !name.toLowerCase().equals(name))) {
			settingsName.put(key, name);
		}
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
	@Override
	public void frameworkEvent(final FrameworkEvent fe) {
		final int eventType = fe.getType();
		if (eventType == FrameworkEvent.STARTED) {
			getLogger().info("********  OSGi Framework has STARTED, calling dispatchFrameworkStartedEvent()");
			dispatchFrameworkStartedEvent0(fe.getBundle(), fe.getThrowable());
		} else {
			getLogger().info("************************ Got frameworkEvent with eventType=" + eventType + ", bundle=" + getBundleName(fe.getBundle()));
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
	 * There are two things you might do in your overriding start() method, which are generally only
	 * done in asingle "top" bundle (generally corresponding to a project that you want to "run"
	 * from maven, or use as the seed of your own launch strategy).
	 *
	 * If you call these methods from multiple bundles, then you will have to deal with the fact
	 * that those invocations (and their callbacks) may generally occur in any order.
	 *
	 * 1) Call forceLog4JConfig() if you like Log4J logging. 2) Call scheduleFrameworkStartEventHandler()
	 * to schedule a callback allowing you to safely begin your application setup *after* all
	 * initial bundles have been started.
	 */
	@Override
	public void start(final BundleContext bundleCtx) throws Exception {
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
		if (!isFakeOSGI()) {
			bundleCtx.removeFrameworkListener(this);
			bundleCtx.addFrameworkListener(this);
		}
		//scheduleFrameworkStartEventHandler(bundleCtx);
		bundleBootPhase = BootPhaseConst.UNSTARTED;
	}

	public boolean isFakeOSGI() {
		return m_context == null;
	}

	public static ArrayList<BundleActivator> startedBundles = new ArrayList<>();

	private void addStartedBundle(final BundleActivator bundleActivatorBase) {
		synchronized (startedBundles) {
			if (startedBundles.contains(bundleActivatorBase))
				return;
			startedBundles.add(bundleActivatorBase);
		}
		final String key = bundleActivatorBase.getClass().getName();
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
			@Override
			public void run() {
				bundleBootPhase = BootPhaseConst.REGISTERINGSERVICES;
				try {
					registerServices(m_context);
				} catch (final Exception e) {
					getLogger().error("registerServices: " + e, e);
					e.printStackTrace();
				}
				bundleBootPhase = BootPhaseConst.COMPLETED_REGISTERSERVICES;
			}
		});
		addMacroService(BootPhaseConst.FRAMEWORKSTARTING, key + "_frameworkstarted", new Runnable() {
			@Override
			public void run() {
				bundleBootPhase = BootPhaseConst.FRAMEWORKSTARTING;
				try {
					Bundle b = null;
					if (!isFakeOSGI()) {
						b = m_context.getBundle();
					}
					dispatchFrameworkStartedEvent0(b, null);
				} catch (final Exception e) {
					getLogger().error("registerServices: " + e, e);
					e.printStackTrace();
				}
				bundleBootPhase = BootPhaseConst.COMPLETED_FRAMEWORKSTARTED;
			}
		});
		addMacroService(BootPhaseConst.LAUNCHING, key + "_launch", new Runnable() {
			@Override
			public void run() {
				bundleBootPhase = BootPhaseConst.LAUNCHING;
				try {
					if (isLauncherBundle()) {
						launchApplication(m_context);
					}
				} catch (final Exception e) {
					getLogger().error("LAUNCHING: " + e, e);
					e.printStackTrace();
				}
				bundleBootPhase = BootPhaseConst.RUNNING;
			}
		});
	}

	@Override
	public void stop(final BundleContext bundleCtx) throws Exception {
		synchronized (macroStartupSettings) {
			if (classLoaderUtils != null) {
				classLoaderUtils.unregisterClassLoader(this, bundleCtx);
			}
		}
		getLogger().info(describe("stop<BundleActivatorBase>", bundleCtx));
	}

	static protected String getBundleName(final BundleActivator key) {
		return key.getClass().getName();
	}

	static public String getBundleName(final Bundle b) {
		if (b == null)
			return "NO_BUNDLE";
		String sym = b.getSymbolicName();
		if (sym != null && sym.length() > 0) {
			return sym;
		}
		sym = b.getLocation();
		if (sym != null && sym.length() > 0) {
			return sym;
		}
		return b.toString();
	}

	/**
	 * The code in here should be moved between two methods:
	 *
	 *
	 * handleFrameworkStartedEvent = everything has had start(.) called now the best time to create
	 * objects and populate fields hopefully the best time to win a race at setting a OSGi framework
	 * property
	 *
	 * registerServices = time to change properies and register serveices
	 *
	 * laucheBundle = everything has had handleFrameworkStartedEvent(.) called now use
	 * isLauncherBundle() in your code to see if you are the toplevel bundle
	 *
	 * We might go to three methods
	 */
	protected void registerConfig(final BundleContext bundleCtx) throws Exception {
		legacyStartWasOverriden_Check = false;
	}

	protected void registerServices(final BundleContext context0) {

	}

	/**
	 * Override this method if you need notification after all bundles in your container have been
	 * started. Normally you would override this method in a "top" bundle to setup your application
	 * code. Important: This handler will be called now regardless if you called
	 * scheduleFrameworkStartEventHandler()
	 *
	 * @param bundleCtx - Fetched from the bundle of the FrameworkEvent, so it should be nice and
	 *                  fresh.
	 */
	protected void handleFrameworkStartedEvent(final BundleContext bundleCtx) throws Exception {

	}

	/**
	 * Override this method with the code that you'd use if this Bundle was intend to be Launched
	 * However, it will only be called if this was the launched bundle like -Prun-on-felix
	 */
	protected void launchApplication(final BundleContext bundleCtx) throws Exception {
		if (isLauncherBundle()) {
			getLogger().warn("this bundle was missing LAUNCHING");
		}
	}

	/**
	 * Call this method from any bundle's start() to schedule a callback to its
	 * handleFrameworkStartedEvent() method.
	 *
	 * @param bundleCtx - used to schedule the callback, and then forgotten.
	 */
	final protected void scheduleFrameworkStartEventHandler(final BundleContext bundleCtx) {
		synchronized (dispatchEventLock) {
			if (startedEventScheduled)
				return;
			startedEventScheduled = true;
		}
		if (MACRO_LAUNCHER) {
			MacroBundleActivatorBase.macroStartupSettings.scheduleFrameworkStartEventHandler(this);
			return;
		}
		// the macrostarter system also works outside OSGi
		if (bundleCtx != null) {
			bundleCtx.removeFrameworkListener(this);
			bundleCtx.addFrameworkListener(this);
		}
	}

	final void dispatchFrameworkStartedEvent0(final Bundle eventBundle, final Throwable eventThrowable) {
		final String thrownMsg = (eventThrowable == null) ? "OK" : eventThrowable.getClass().getName();
		final String bundleName = getBundleName(eventBundle);
		getLogger().info("dispatchFrameworkStartedEvent<BundleActivatorBase> ( bundle={}, msg={}", bundleName, thrownMsg);
		if (eventThrowable == null) {
			BundleContext bc = null;
			if (eventBundle != null) {
				bc = eventBundle.getBundleContext();
			}
			if (bc == null) {
				getLogger().info("Cannot find bundle context for event bundle, so there will be no callback to app startup: {} ", bundleName);
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
			} catch (final Exception e) {
				getLogger().error("handleFrameworkStartedEvent " + e, e);
			}
		} else {
			getLogger().warn("No callback to application startup, due to throwable ", eventThrowable);
		}
	}

	/**
	 * If you override this
	 */
	private void reallyLaunch(final BundleActivator bundleActivatorBase) {
		macroStartupSettings.launchPhases();
		//launchApplication(bc);

	}

	protected String describe(final String action, final BundleContext bundleCtx) {
		Bundle b = null;
		if (bundleCtx != null)
			b = bundleCtx.getBundle();
		final String msg = getClass().getCanonicalName() + "." + action + "(ctx=[" + bundleCtx + "], bundle=[" + getBundleName(b) + "])";
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

	public void logInfo(final String msg) {
		getLogger().info(msg);
	}

	public boolean isLauncherBundle() {
		synchronized (macroStartupSettings) {
			return macroStartupSettings.firstBundleActivatorBase == this;
		}
	}

	/**
	 * This should usually be called only once during a system's runtime lifetime. (To be tested:
	 * works OK to update properties at runtime?)
	 *
	 * Prints some debug to stdout about known classloaders, then builds a URL using the *local* CL
	 * of this concrete class (which will be *your* class is you make subtypes of BasicDebugger or
	 * BundleActivatorBase!) to create a resource URL into the classpath (under OSGi, the
	 * bundle-classpath-of) of that concrete class, which under OSGi will look something like:
	 *
	 * bundle://221.0:1/log4j.properties
	 *
	 * ...which is then passed to Log4jFuncs.forceLog4jConfig.
	 *
	 * What this means is that you can put a log4j.properties into any bundle of your own, then call
	 * "forceLog4jConfig()" ONCE from that bundle's activator (or framework-START-event handler),
	 * and then your properties should be in place.
	 */
	protected void forceLog4jConfig0() {
		// Logger logger = getLogger();

		// To get more determinism over when this happens (before other bundles that use logging are launched),
		// need to mess with Felix auto-start properties?
		final ClassLoader threadCL = Thread.currentThread().getContextClassLoader();
		final ClassLoader localCL = getClass().getClassLoader();
		System.out.println("thread-context-CL=" + threadCL);
		System.out.println("local-CL=" + localCL);

		final String resPath = "log4j.properties";

		final URL threadURL = threadCL.getResource(resPath);
		final URL localURL = localCL.getResource(resPath);
		System.out.println("[System.out] forceLog4jConfig() threadCL resolved " + resPath + " to threadURL " + threadURL);
		System.out.println("[System.out] forceLog4jConfig() localCL resolved  " + resPath + " to  localURL " + localURL);
		System.out.println("[System.out] " + getClass().getCanonicalName() + " is forcing Log4J to read config from localURL: " + localURL);

		try {

			forceLog4jConfig(localURL);
		} catch (final Throwable t) {
			t.printStackTrace();
		}
		try {
			final Logger logger = getLogger();

			try {
				logger.info("{forceLog4JConfig} - This message was printed at INFO level to SLF4J, after forcing config for Log4J to localURL: " + localURL);
			} catch (final Throwable t2) {
				t2.printStackTrace();
			}
		} catch (final Throwable t) {
			t.printStackTrace();
		}
	}

	public static void forceLog4jConfig(final URL propertiesURL) {
		System.out.println(new StringBuilder().append("[System.out] ").append(MacroBundleActivatorBase.class.getName()).append(" is forcing Log4J to read config from propertiesURL: ").append(propertiesURL).toString());
		PropertyConfigurator.configure(propertiesURL);
	}

	public void ensureExtClassesAreFindable() {
		// TODO Auto-generated method stub

	}

}
