package org.appdapter.gui.util;

import static org.appdapter.gui.util.CollectionSetUtils.addIfNew;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.Utility;
import org.slf4j.LoggerFactory;

import sun.reflect.Reflection;

@SuppressWarnings("restriction")
abstract public class PromiscuousClassUtils {

	static NamingResolver namingResolver = new ClassLoadingNamingResolver();

	public static HashSet<Class> classesSeen = new HashSet<Class>();
	public static Map<String, Class> classnameToClass = new HashMap();

	public static <T> Class<T> rememberClass(String named, Class<T> findClass) {
		synchronized (classesSeen) {
			addIfNew(classesSeen, findClass);
		}
		try {
			String cn = named;
			try {
				String cn0 = findClass.getCanonicalName();
				if (cn0 != null && named.compareTo(cn0) != 0) {
					classnameToClass.put(cn0, findClass);
				}
			} catch (Throwable err) {
				debugLog("while trying to 'rememberClass' " + named + " " + " " + findClass + " had " + err);
			}

			try {
				String cn1 = findClass.getName();
				if (cn1 != null && named.compareTo(cn1) != 0) {
					classnameToClass.put(cn1, findClass);
				}
			} catch (Throwable err) {
				debugLog("while trying to 'rememberClass' " + named + " " + " " + findClass + " had " + err);
			}
			if (cn != null) {
				classnameToClass.put(cn, findClass);
			}
		} catch (Exception e) {

		}
		return findClass;
	}

	private static void debugLog(String msg) {
		LoggerFactory.getLogger(PromiscuousClassUtils.class).debug(msg);
	}

	public static ArrayList<ClassLoader> allClassLoaders = new ArrayList<ClassLoader>();

	public static FromManyClassLoader many;

	private static Map<String, Throwable> classNotFoundYet = new HashMap<String, Throwable>();

	public synchronized static FromManyClassLoader getPromiscuousClassLoader() {
		if (many == null) {
			ClassLoader parent = FromManyClassLoader.class.getClassLoader();
			many = new FromManyClassLoader(allClassLoaders, parent);
		}
		return many;
	}

	public static void addClassloader(ClassLoader useAlt) {
		if (useAlt == null)
			return;
		if (useAlt instanceof FromManyClassLoader)
			return;
		synchronized (allClassLoaders) {
			addIfNew(allClassLoaders, useAlt);
		}
	}

	public static Collection<Class> getImplementingClasses(Class interfaceClass) {
		PromiscuousClassUtils.ensureOntoligized(interfaceClass);
		HashSet<Class> foundClasses = new HashSet<Class>();
		PromiscuousClassUtils.ensureInstalled();
		for (Class type : getInstalledClasses()) {
			if (!isCreateable(type))
				continue;
			if (interfaceClass.isAssignableFrom(type)) {
				addIfNew(foundClasses, type);
			}
		}
		return foundClasses;
	}

	public static void ensureOntoligized(Class interfaceClass) {
		boolean wasTrue = true || Ontologized.class.isAssignableFrom(interfaceClass);
		if (!wasTrue) {
			Debuggable.LOGGER.warning("interfaceClass " + interfaceClass + " is not Ontoligized");
		}
		//Assert.assertTrue("All our classes are ontoligized", wasTrue);

	}

	public static boolean isCreateable(Class type) {
		if (type.isInterface())
			return false;
		if (Modifier.isAbstract(type.getModifiers()))
			return false;
		if (Modifier.isInterface(type.getModifiers()))
			return false;
		/*
		Constructor[] v = type.getConstructors();
		if (v == null || v.length == 0)
			return false;*/
		return true;
	}

	public static ArrayList<Class> getInstalledClasses() {
		synchronized (classesSeen) {
			return new ArrayList<Class>(classesSeen);
		}
	}

	public static void ensureInstalled() {
		ClassLoader cl1 = ClassLoader.getSystemClassLoader();
		addClassloader(cl1);
		ClassLoader cl2 = Thread.currentThread().getContextClassLoader();
		if (cl2 instanceof HRKRefinement) {
			return;
		}
		IsolatingClassLoaderBase cl3 = coerceClassloader(cl2);
		Thread.currentThread().setContextClassLoader(cl3);
		namingResolver.toString();
	}

	private static Map<ClassLoader, ClassLoader> switchedOutClassLoader = new HashMap<ClassLoader, ClassLoader>();

	public static IsolatingClassLoaderBase coerceClassloader(ClassLoader cl2) {
		if (cl2 instanceof IsolatingClassLoaderBase)
			return (IsolatingClassLoaderBase) cl2;
		IsolatedClassLoader icl = (IsolatedClassLoader) switchedOutClassLoader.get(cl2);
		if (icl == null) {
			addClassloader(cl2);
			icl = new IsolatedClassLoader(cl2);
			switchedOutClassLoader.put(cl2, icl);
		}
		return icl;
	}

	public static <T> Class<T> forName(String className, boolean initialize, ClassLoader loader) throws ClassNotFoundException, NoClassDefFoundError {
		ClassNotFoundException cnf = null;
		LinkageError ncde = null;
		try {
			return (Class<T>) rememberClass(className, (Class<T>) Class.forName(className, initialize, loader));
		} catch (VirtualMachineError e) {
			throw e;
		} catch (LinkageError e) {
			ncde = e;
			classNotFoundYet.put(className, e);
		} catch (ClassNotFoundException e) {
			cnf = e;
		} catch (SecurityException e) {
			//cnf = e;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			Class c = (Class<T>) findPromiscuousClass(className);
			if (c == null)
				throw new NoClassDefFoundError(className);
			return rememberClass(className, c);
		} catch (Throwable e) {
			if (cnf != null)
				throw cnf;
			if (e instanceof VirtualMachineError)
				throw (VirtualMachineError) e;
			if (ncde != null)
				throw ncde;
			if (e instanceof ClassNotFoundException)
				throw (ClassNotFoundException) e;
			if (e instanceof LinkageError)
				throw (LinkageError) e;
			throw new ClassNotFoundException(className, e);
		}
	}

	public static Class findPromiscuousClass(String className) throws ClassNotFoundException {
		return getPromiscuousClassLoader().findClass(className);
	}

	public static <T> Class<T> forName(String className) throws ClassNotFoundException {
		return forName(className, true, getCallerClassLoader());
	}

	// Returns the invoker's class loader, or null if none.
	// NOTE: This must always be invoked when there is exactly one intervening
	// frame from the core libraries on the stack between this method's
	// invocation and the desired invoker.
	static ClassLoader getCallerClassLoader() {
		// NOTE use of more generic Reflection.getCallerClass()
		Class caller = Reflection.getCallerClass(3);
		// This can be null if the VM is requesting it
		if (caller == null) {
			return null;
		}
		// CANT Circumvent security check
		return caller.getClassLoader();
	}

	public static <T> Class<T> OneInstC(String p, String c) {
		Debuggable.notImplemented();
		try {
			return (Class<T>) forName(p + "" + c);
		} catch (ClassNotFoundException e) {
			return null;
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}

	public static <T> T OneInstO(String p, String c) {
		Class clz = OneInstC(p, c);
		if (clz == null)
			return null;
		Debuggable.notImplemented();
		try {
			return (T) createInstance(clz);
		} catch (Exception e1) {
			Debuggable.UnhandledException(e1);
		}
		return null;
	}

	public static boolean isSomething(Object result) {
		if (result == null)
			return false;
		if (result instanceof Enumeration) {
			Enumeration e = (Enumeration) result;
			if (e.hasMoreElements())
				return true;
			return false;
		}
		if (result instanceof Collection) {
			Collection e = (Collection) result;
			if (e.size() > 0)
				return true;
			return false;
		}
		if (result instanceof Iterator) {
			Iterator e = (Iterator) result;
			if (e.hasNext())
				return true;
			return false;
		}
		if (result instanceof Iterable) {
			Iterator e = ((Iterable) result).iterator();
			if (e.hasNext())
				return true;
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked") static public <T> T callProtectedMethodNullOnUncheck(Object from, String methodName, Object... args) {
		Throwable whyBroken = null;
		try {
			return callProtectedMethod(true, from, methodName, args);
		} catch (Throwable e) {
			whyBroken = e;
			while (whyBroken.getCause() != null && whyBroken.getCause() != whyBroken) {
				whyBroken = whyBroken.getCause();
				if (whyBroken instanceof NoSuchMethodException) {
					return null;
				}
				if (whyBroken instanceof ClassNotFoundException) {
					return null;
				}
				if (whyBroken instanceof NoClassDefFoundError) {
					return null;
				}
			}
			Debuggable.UnhandledException(whyBroken);
			whyBroken.printStackTrace();
			if (whyBroken instanceof Error)
				throw ((Error) whyBroken);
			if (whyBroken instanceof RuntimeException)
				throw ((RuntimeException) whyBroken);
			throw Utility.reThrowable(whyBroken);
		}
	}

	public static Class nonPrimitiveTypeFor(Class wrapper) {
		if (!wrapper.isPrimitive())
			return wrapper;
		if (wrapper == Boolean.TYPE)
			return Boolean.class;
		if (wrapper == Byte.TYPE)
			return Byte.class;
		if (wrapper == Character.TYPE)
			return Character.class;
		if (wrapper == Short.TYPE)
			return Short.class;
		if (wrapper == Integer.TYPE)
			return Integer.class;
		if (wrapper == Long.TYPE)
			return Long.class;
		if (wrapper == Float.TYPE)
			return Float.class;
		if (wrapper == Double.TYPE)
			return Double.class;
		if (wrapper == Void.TYPE)
			return Void.class;
		throw new ClassCastException("cant make non primitive from :" + wrapper);
	}

	@SuppressWarnings("unchecked") static public <T> T callProtectedMethod(boolean skipNSM, Object from, String methodName, Object... args) throws InvocationTargetException, NoSuchMethodException {
		Throwable whyBroken = null;
		Method foundm = null;
		try {
			for (Method method : from.getClass().getDeclaredMethods()) {
				if (!method.getName().equals(methodName))
					continue;
				if (foundm == null)
					foundm = method;
				Class[] pts = method.getParameterTypes();
				if (pts.length != args.length)
					continue;
				foundm = method;
				int peekAt = args.length - 1;
				if (peekAt >= 0) {
					Class pc = pts[peekAt];
					Class npc = nonPrimitiveTypeFor(pc);
					Object po = args[peekAt];
					if (!npc.isInstance(po))
						continue;
				}
				method.setAccessible(true);
				try {
					return (T) method.invoke(from, args);
				} catch (InvocationTargetException e) {
					throw e;
				}
			}
			if (skipNSM)
				return null;
			throw new NoSuchMethodException(Debuggable.toInfoStringArgV("NoSuchMethod " + methodName + " on=", from, "args=", args));
			// NoSuchMethod

		} catch (IllegalArgumentException e) {
			whyBroken = e;
		} catch (SecurityException e) {
			whyBroken = e;
		} catch (IllegalAccessException e) {
			whyBroken = e;
		}

		// whyBroken.printStackTrace();

		if (whyBroken instanceof RuntimeException) {
			throw (RuntimeException) whyBroken;
		}
		if (whyBroken instanceof Error) {
			throw (Error) whyBroken;
		}
		// if (true)
		// throw new RuntimeException(whyBroken);
		return null;
	}

	static public <T> T createInstance(Class<T> clz) throws InstantiationException {
		Throwable why = null;
		try {
			try {
				final Constructor constructor = clz.getDeclaredConstructor();
				constructor.setAccessible(true);
				return (T) constructor.newInstance();
			} catch (IllegalArgumentException e) {
				why = e;
			} catch (InvocationTargetException e) {
				why = e.getCause();
			}
			return clz.newInstance();
		} catch (SecurityException e) {
			why = e;
		} catch (NoSuchMethodException e) {
			why = e;
		} catch (InstantiationException e) {
			why = e;
		} catch (IllegalAccessException e) {
			why = e;
		}
		return (T) Debuggable.NoSuchClassImpl("newInstance=" + clz + " err=", why);
	}

}
