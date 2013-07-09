package org.appdapter.gui.util;

import static org.appdapter.gui.util.CollectionSetUtils.addIfNew;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.Ontologized;
import org.appdapter.gui.api.Ontologized.HRKRefinement;

import sun.reflect.Reflection;

//import org.slf4j.helpers.NOPLoggerFactory;

@SuppressWarnings("restriction")
abstract public class PromiscuousClassUtils {

	public static boolean missingDepsOK = false;
	public static boolean treatmissingLikeOptional = true;

	// Thread local to detect class loading cycles.
	private final static ThreadLocal m_depthCheck = new ThreadLocal();

	public static Class findLoadedClassByName(String name) {
		synchronized (classnameToClass) {
			Class c = classnameToClass.get(name);
			if (c != null) {
				return c;
			}
		}
		return null;
	}

	public static Class findLoadedClassByName(String className, ClassLoader loader) {
		if (loader == null)
			loader = getThreadClassLoader();
		Class c = null;
		try {
			c = callProtectedMethod(false, loader, "findLoadedClass", className);
			if (c != null)
				return c;
		} catch (InvocationTargetException e) {
			Debuggable.printStackTrace(e.getCause());
		} catch (NoSuchMethodException e) {
			Debuggable.printStackTrace(e);
		}
		if (c == null) {
			c = findLoadedClassByName(className);
			if (c == null)
				return null;
			// something is odd maybe resolve it here?
		}
		return c;
	}

	public static URL findPromiscuousResource(String name, Collection<URL> exceptFor) throws Exception {
		FromManyClassLoader pcl = getPromiscuousClassLoader();
		if (name.startsWith("/"))
			name = name.substring(1);
		URL url = pcl.findPromiscuousResource(name, exceptFor);
		if (url != null && (exceptFor == null || !exceptFor.contains(url))) {
			return url;
		}
		return null;
	}

	public static Object findClassOrResourceByDelegationOverride(String pkgname, String name, boolean isClass, boolean useAllClassLoaders) {
		return PromiscuousClassUtils.findClassOrResourceByDelegation(pkgname, name, isClass, useAllClassLoaders);
	}

	public static String getClassPackage(String className) {
		if (className == null) {
			className = "";
		}
		return (className.lastIndexOf('.') < 0) ? "" : className.substring(0, className.lastIndexOf('.'));
	}

	public static String getResourcePackage(String resource) {
		if (resource == null) {
			resource = "";
		}
		// NOTE: The package of a resource is tricky to determine since
		// resources do not follow the same naming conventions as classes.
		// This code is pessimistic and assumes that the package of a
		// resource is everything up to the last '/' character. By making
		// this choice, it will not be possible to load resources from
		// imports using relative resource names. For example, if a
		// bundle exports "foo" and an importer of "foo" tries to load
		// "/foo/bar/myresource.txt", this will not be found in the exporter
		// because the following algorithm assumes the package name is
		// "foo.bar", not just "foo". This only affects imported resources,
		// local resources will work as expected.
		String pkgName = (resource.startsWith("/")) ? resource.substring(1) : resource;
		pkgName = (pkgName.lastIndexOf('/') < 0) ? "" : pkgName.substring(0, pkgName.lastIndexOf('/'));
		pkgName = pkgName.replace('/', '.');
		return pkgName;
	}

	public static Object findClassOrResourceByDelegationLastChance(String pkgname, String name, boolean isClass, boolean useAllClassLoaders) //throws ClassNotFoundException, ResourceNotFoundException
	{
		return PromiscuousClassUtils.findClassOrResourceByDelegation(pkgname, name, isClass, useAllClassLoaders);
	}

	public static Object findClassOrResourceByDelegation(String pkgname, String name, boolean isClass, boolean useAllClassLoaders) //throws ClassNotFoundException, ResourceNotFoundException
	{
		// Get the package of the target class/resource.
		if (pkgname == null || pkgname.equals("")) {
			pkgname = (isClass) ? getClassPackage(name) : getResourcePackage(name);
		}
		if (isClass) {
			Class z = findLoadedClassByName(name);
			if (z != null) {
				if (loadPackageInBundleOnly(pkgname)) {
					return null;
				}
				return z;
			}
		}
		if (loadPackageInBundleOnly(pkgname)) {
			return null;
		}
		LinkageError le = null;
		ClassNotFoundException cnf0 = null;
		boolean wasFound = false;
		try {
			Object o = findClassOrResourceByDelegationUncaugth(pkgname, name, isClass, useAllClassLoaders);
			if (o != null) {
				wasFound = true;
				return o;
			}
		} catch (ClassNotFoundException cnf) {
			cnf0 = cnf;
		} catch (NoClassDefFoundError cnf) {
			le = cnf;
			//	throw cnf;
		} catch (LinkageError cnf) {
			le = cnf;

		} finally {
			if (!wasFound && loadPackageOutsideBundleOnly(pkgname)) {
				throw new RuntimeException("loadPackageOutsideBundleOnly p=" + pkgname + " c=" + name);
			}
		}
		return null;
	}

	public static Object findClassOrResourceByDelegationUncaugth(String pkgname, String name, boolean isClass, boolean useAllClassLoaders) throws ClassNotFoundException {
		ClassLoader ccl = getCallerClassLoader();
		ClassLoader pcl = getPromiscuousClassLoader();
		ClassLoader useCL = getThreadClassLoader();
		if (useCL == null) {
			useCL = ccl;
		}
		Object co;
		try {
			if (useAllClassLoaders) {
				if (pcl == null) {
					useCL = pcl;
				}
			}
			co = findClassOrResourceByDelegationWithClassloader(pkgname, name, false, useCL, isClass);
		} catch (NoClassDefFoundError e) {
			return null;
		} catch (ClassNotFoundException e) {
			return null;
		} catch (Error e) {
			if (true)
				throw e;
			return null;
		} catch (RuntimeException e) {
			if (true)
				throw e;
			return null;
		} catch (Exception e) {
			return null;
		} catch (Throwable e) {
			return null;
		}

		if (co == null)
			return null;
		if (isClass && pkgname != null) {
			if (pkgname.startsWith("scala")) {
				return null;
			}
			if (pkgname.startsWith("java")) {
				return null;
			}
		}
		return co;

	}

	public static boolean loadPackageInBundleOnly(String pkgname) {
		if (true)
			return false;
		if (true)
			return true;
		if (pkgname != null) {
			if (pkgname.contains("puma") || pkgname.contains("preview") || pkgname.contains("content"))
				return true;
		}
		return false;
	}

	public static boolean loadPackageOutsideBundleOnly(String pkgname) {
		if (true)
			return false;
		if (pkgname != null) {
			if (pkgname.contains("java") || pkgname.contains("org.appdapter"))
				return true;
		}
		return false;
	}

	public static ClassLoader getThreadClassLoader() {
		return getCorrectLoader(Thread.currentThread().getContextClassLoader());
	}

	public static ClassLoader bootstrapClassLoader = getSystemClassLoader().getParent();

	public static ClassLoader getBootstrapClassLoader() {
		if (bootstrapClassLoader != null) {
			return bootstrapClassLoader;
		}
		return Object.class.getClassLoader();
	}

	public static ClassLoader getSystemClassLoader() {
		return ClassLoader.getSystemClassLoader();
	}

	public static Object findClassOrResourceByDelegationWithClassloader(String pkgname, String name, boolean initialize, ClassLoader useCL, boolean isClass) throws ClassNotFoundException, Throwable /*throws , ResourceNotFoundException */{

		if (isClass) {
			Class z = findLoadedClassByName(name);
			if (z != null)
				return z;
		}
		String m_cycleCheck = "m_cycleCheck isClass = " + isClass;
		if (contains(m_cycleCheck, name)) {
			// If a cycle is detected, we should return null to break the
			// cycle. This should only ever be return to internal class
			// loading code and not to the actual instigator of the class load.
			return null;
		}
		Integer depthCheck = (Integer) m_depthCheck.get();
		if (depthCheck == null) {
			depthCheck = Integer.valueOf(1);
			m_depthCheck.set(depthCheck);
		} else {
			depthCheck = depthCheck.intValue() + 1;
			m_depthCheck.set(depthCheck);
		}
		int depth = depthCheck;
		try {
			push(m_cycleCheck, name);
			if (isClass) {
				{
					try {
						Class fnd1 = Class.forName(name, initialize, getCorrectLoader(useCL));
						if (fnd1 != null) {
							ClassLoader cl = fnd1.getClassLoader();
							return fnd1;
						} else {
							// wierd?
							return null;
						}
					} catch (ClassNotFoundException e) {
						trace("while findClassOrResourceByDelegationWithClassloader " + name + "" + e);
						if (depth > 1)
							throw e;
						return null;
					} catch (NoClassDefFoundError e) {
						trace("while findClassOrResourceByDelegationWithClassloader " + name + "" + e);
						if (depth > 1)
							throw e;
						return null;
					} catch (LinkageError e) {
						trace("while findClassOrResourceByDelegationWithClassloader " + name + "" + e);
						if (depth > 1)
							throw e;
						return null;
					}

				}
			} else {
				URL fnd1 = useCL.getResource(name);
				if (fnd1 != null)
					return fnd1;
				return null;
			}

		} finally {
			pop(m_cycleCheck, name);
			depthCheck = depth - 1;
			m_depthCheck.set(depthCheck);
		}
	}

	public static void trace(String string) {
		// TODO Auto-generated method stub

	}

	//static NamingResolver namingResolver = new ClassLoadingNamingResolver();

	public static HashSet<Class> classesSeen = new HashSet<Class>();
	public static Map<String, Class> classnameToClass = new HashMap();
	public static boolean verifyClassNames = false;

	public static <T> Class<T> rememberClass(String name, Class<T> findClass) {
		if (findClass == null)
			return findClass;
		if (name == null) {
			name = findClass.getCanonicalName();
		}
		synchronized (classesSeen) {
			addIfNew(classesSeen, findClass);
		}
		if (classesSeen.size() % 1000 == 1) {

		}
		try {
			String cn = name;
			if (verifyClassNames) {
				try {
					String cn0 = findClass.getCanonicalName();
					if (cn0 != null && name.compareTo(cn0) != 0) {
						classnameToClass.put(cn0, findClass);
					}
				} catch (Throwable err) {
					debugLog("while trying to 'rememberClass' " + name + " " + " " + findClass + " had " + err);
				}
			}
			try {
				String cn1 = findClass.getName();
				if (cn1 != null && name.compareTo(cn1) != 0) {
					classnameToClass.put(cn1, findClass);
				}
			} catch (Throwable err) {
				debugLog("while trying to 'rememberClass' " + name + " " + " " + findClass + " had " + err);
			}
			if (cn != null) {
				classnameToClass.put(cn, findClass);
			}
		} catch (Exception e) {

		}
		return findClass;
	}

	private static void debugLog(String msg) {
		//LoggerFactory.getLogger(PromiscuousClassUtils.class).debug(msg);
	}

	public static ArrayList<ClassLoader> allClassLoaders = new ArrayList<ClassLoader>();

	public static FromManyClassLoader many;

	private static Map<String, Throwable> classNotFoundYet = new HashMap<String, Throwable>();

	public synchronized static FromManyClassLoader getPromiscuousClassLoader() {
		if (many == null) {
			ClassLoader parent = getBootstrapClassLoader();
			synchronized (allClassLoaders) {
				allClassLoaders.add(parent);
			}
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
		ensureInstalled(true, false, false);
	}

	public static void ensureInstalled(boolean spyOnly, boolean sharedFirst, boolean fallBackPromiscuous) {
		ClassLoader cl1 = ClassLoader.getSystemClassLoader();
		if (!(cl1 instanceof HRKRefinement)) {
			coerceClassloader(cl1, spyOnly, sharedFirst, fallBackPromiscuous);
		}
		ClassLoader cl2 = Thread.currentThread().getContextClassLoader();

		ClassLoader cl3 = coerceClassloader(cl2, spyOnly, sharedFirst, fallBackPromiscuous);
		if (cl3 != cl2) {
			Thread.currentThread().setContextClassLoader(cl3);
		}
		//namingResolver.toString();
	}

	private static Map<ClassLoader, ClassLoader> switchedOutClassLoader = new HashMap<ClassLoader, ClassLoader>();

	public static ClassLoader coerceClassloader(ClassLoader cl2, boolean spyOnly, boolean sharedFirst, boolean fallBackPromiscuous) {
		if (cl2 instanceof IsolatingClassLoaderBase)
			return (IsolatingClassLoaderBase) cl2;
		if (!sharedFirst && !spyOnly)
			return cl2;
		if (cl2 instanceof HRKRefinement) {
			return cl2;
		}
		synchronized (switchedOutClassLoader) {
			IsolatedClassLoader icl = (IsolatedClassLoader) switchedOutClassLoader.get(cl2);
			if (icl == null) {
				addClassloader(cl2);
				ClassLoader parentWillBe = null;
				List<ClassLoader> clList = new ArrayList<ClassLoader>();
				if (sharedFirst) {
					clList.add(getPromiscuousClassLoader());
				}
				if (spyOnly) {
					parentWillBe = cl2;
				} else {
					clList.add(cl2);
				}
				if (fallBackPromiscuous && !sharedFirst) {
					clList.add(getPromiscuousClassLoader());
				}
				icl = new IsolatedClassLoader(parentWillBe, spyOnly, sharedFirst, fallBackPromiscuous, clList);
				switchedOutClassLoader.put(cl2, icl);
			}
			return icl;
		}
	}

	public static <T> Class<T> forName(String className, boolean initialize, ClassLoader loader) throws ClassNotFoundException, NoClassDefFoundError {
		ClassNotFoundException cnf = null;
		loader = getCorrectLoader(loader);
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
			throw new ClassNotFoundExceptionF(className, e);
		}
	}

	public static ClassLoader getCorrectLoader(ClassLoader loader) {
		if (loader == bootstrapClassLoader && bootstrapClassLoader != null)
			return loader;
		if (loader == many && many != null)
			return loader;
		return getSystemClassLoader();
	}

	public static Class findPromiscuousClass(String className) throws ClassNotFoundException {
		return getPromiscuousClassLoader().findClass(className);
	}

	public static <T> Class<T> forName(String className) throws ClassNotFoundException {
		return forName(className, false, getCallerClassLoader());
	}

	// Returns the invoker's class loader, or null if none.
	// NOTE: This must always be invoked when there is exactly one intervening
	// frame from the core libraries on the stack between this method's
	// invocation and the desired invoker.
	public static ClassLoader getCallerClassLoader() {
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
			Debuggable.printStackTrace(e);
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

	@SuppressWarnings("unchecked")
	static public <T> T callProtectedMethodNullOnUncheck(Object from, String methodName, Object... args) {
		Throwable whyBroken = null;
		try {
			return callProtectedMethod(true, from, methodName, args);
		} catch (Throwable e) {
			whyBroken = e.getCause();
			Throwable wb = e;
			while (wb != null) {
				whyBroken = wb;
				if (whyBroken instanceof NoSuchMethodException) {
					return null;
				}
				if (whyBroken instanceof ClassNotFoundException) {
					return null;
				}
				if (whyBroken instanceof NoClassDefFoundError) {
					return null;
				}
				if (whyBroken instanceof MissingResourceException) {
					return null;
				}
				if (whyBroken instanceof LinkageError) {
					return null;
				}
				if (whyBroken instanceof IOException) {
					return null;
				}
				if (whyBroken instanceof NullPointerException) {
					return null;
				}
				Throwable twb = wb.getCause();
				if (twb == wb || twb == null) {
					break;
				}
				wb = twb;
			}
			Debuggable.UnhandledException(whyBroken);
			Debuggable.printStackTrace(whyBroken);

			if (whyBroken instanceof Error)
				throw ((Error) whyBroken);
			if (whyBroken instanceof RuntimeException)
				throw ((RuntimeException) whyBroken);
			throw Debuggable.reThrowable(whyBroken);
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

	@SuppressWarnings("unchecked")
	static public <T> T callProtectedMethod(boolean skipNSM, Object from, String methodName, Object... args) throws InvocationTargetException, NoSuchMethodException {
		Throwable whyBroken = null;
		Method foundm = null;
		Class c = from.getClass();
		while (c != null) {
			try {
				for (Method method : c.getDeclaredMethods()) {
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
						Object po = args[peekAt];
						if (pc.isPrimitive()) {
							if (po == null)
								continue;
							pc = nonPrimitiveTypeFor(pc);
						}
						if (po != null)
							if (!pc.isInstance(po))
								continue;
					}
					method.setAccessible(true);
					try {
						return (T) method.invoke(from, args);
					} catch (InvocationTargetException e) {
						whyBroken = e.getCause();
						throw e;
					}
				}

			} catch (IllegalArgumentException e) {
				whyBroken = e;
			} catch (SecurityException e) {
				whyBroken = e;
			} catch (IllegalAccessException e) {
				whyBroken = e;
			}
			c = c.getSuperclass();
		}
		// whyBroken.printStackTrace();

		if (whyBroken instanceof RuntimeException) {
			throw (RuntimeException) whyBroken;
		}
		if (whyBroken instanceof Error) {
			throw (Error) whyBroken;
		}
		if (skipNSM)
			return null;
		throw new NoSuchMethodException(Debuggable.toInfoStringArgV("NoSuchMethod " + methodName + " on=", from, "args=", args));
		// NoSuchMethod
		// if (true)
		// throw new RuntimeException(whyBroken);
		//return null;
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

	public static void applyConfigReplacements(Properties props) {
		// Perform variable substitution for system properties.
		for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
			String name = (String) e.nextElement();

			props.setProperty(name, substAll(props.getProperty(name)));
		}
	}

	public static String substAll(String str) {
		return substAll(str, true, true);
	}

	public static String bundleJarToDir(String str) {
		return substAll(str, false, false);
	}

	static public interface ArchiveSearcher {
		String replace(String target, boolean maySplit, int depth);
	}

	public static String substAll(String str, boolean maySplit, int depth, ArchiveSearcher searcher) {
		if (depth == 0)
			return str;
		if (str == null || str.length() < 3)
			return str;
		String oldold = str;
		if (maySplit) {
			for (String splitter : new String[] { "\n", ",", "\r", File.pathSeparator, " " }) {

				if (str.contains(splitter)) {
					String[] properties = str.split("(?<!\\\\)" + splitter);
					int pl = properties.length;
					if (pl > 1) {
						for (int i = pl - 1; i >= 0; i--) {
							String prop = properties[i];
							properties[i] = searcher.replace(prop, false, depth - 1);
						}
						str = join(properties, splitter);
						return str;
					}
				}
			}
		}
		return searcher.replace(str, maySplit, depth);
	}

	public static String substAll(String str, boolean keepJar, boolean maySplit) {
		if (str == null || str.length() < 3)
			return str;
		String oldold = str;
		if (maySplit) {
			for (String splitter : new String[] { "\n", ",", "\r", File.pathSeparator, " " }) {

				if (str.contains(splitter)) {
					String[] properties = str.split("(?<!\\\\)" + splitter);
					if (properties.length > 1) {
						int i = 0;
						for (String prop : properties) {
							properties[i] = substAll(prop, keepJar, false);
							i++;
						}
						str = join(properties, splitter);
						return str;
					}
				}
			}
		}

		//str = subst(str, "\\:", ":");
		str = subst(str, "\\", "/");
		str = subst(str, "0.9.0.jar", "0.9.1-SNAPSHOT.jar");
		str = subst(str, "0.9.0/", "0.9.1-SNAPSHOT/");
		str = subst(str, "D:", "d:");
		str = subst(str, "C:", "c:");
		if (str.contains("bundle") && keepJar)
			return str;
		String old = str;
		str = subst(str, "c.*/repository/org/robokind/", "d:/dev/hrk/org.robokind/maven/projects/SPLIT");
		str = subst(str, "c.*/repository/org/appdapter/", "d:/dev/hrk/org.appdapter/maven/SPLIT");
		str = subst(str, "c.*/repository/org/cogchar/", "d:/dev/hrk/org.cogchar/maven/SPLIT");
		str = subst(str, "c:/Users/Administrator/.m2/repository/org/robokind/", "d:/dev/hrk/org.robokind/maven/projects/SPLIT");
		str = subst(str, "c:/Users/Administrator/.m2/repository/org/appdapter/", "d:/dev/hrk/org.appdapter/maven/SPLIT");
		str = subst(str, "c:/Users/Administrator/.m2/repository/org/cogchar/", "d:/dev/hrk/org.cogchar/maven/SPLIT");
		if (!str.equals(old)) {
			if (keepJar) {
				return old;
			}
			String str2[] = str.split("SPLIT");
			String rest = str2[1];
			rest = rest.split("/")[0];
			str = str2[0] + "" + rest + "/target/classes/";
			str.toString();
		}
		if (str.startsWith("d:/dev/hrk/org.robokind/maven/projects/")) {
			if (!keepJar) {
				return "d:/dev/hrk_eclipse_workspace/Robokind/target/eclipse-classes";
			}
		}
		//org.robokind.headless.messaging/0.9.1/org.robokind.headless.messaging-0.9.1-SNAPSHOT.jar;

		return str;
	}

	private static String subst(String property, String b, String a) {
		if (property == null || property.length() < 1)
			return property;
		return property.replace(b, a);
	}

	private static String join(String[] properties, String splitter) {
		if (properties == null)
			return null;
		int len = properties.length;
		if (len == 0)
			return "";
		if (len == 1)
			return properties[0];
		StringBuffer buff = new StringBuffer(properties[len - 1]);
		for (int i = 1; i < len; i++) {
			buff.append(splitter);
			buff.append(properties[i]);
		}
		return buff.toString();
	}

	public static ThreadLocal<Map<String, List<?>>> m_currentModule = new ThreadLocal<Map<String, List<?>>>();

	public static <T> void push(String act, T impl) {
		List<T> currentModulePrev = getModuleStack(act);
		currentModulePrev.add(0, impl);
	}

	public static <T> boolean contains(String act, T impl) {
		List<T> currentModulePrev = getModuleStack(act);
		return currentModulePrev.contains(impl);
	}

	public static <T> int size(String act) {
		List<T> currentModulePrev = getModuleStack(act);
		return currentModulePrev.size();
	}

	public static synchronized Map<String, List<?>> getModuleStackMap() {
		Map<String, List<?>> currentModulePrev = m_currentModule.get();
		if (currentModulePrev == null) {
			currentModulePrev = new HashMap<String, List<?>>();
			m_currentModule.set(currentModulePrev);
		}
		return currentModulePrev;
	}

	public static <T> List<T> getModuleStack(String act) {
		Map map = getModuleStackMap();
		List<T> currentModulePrev = (List<T>) map.get(act);
		if (currentModulePrev == null) {
			currentModulePrev = new LinkedList<T>();
			map.put(act, currentModulePrev);
		}
		return currentModulePrev;
	}

	public static <T> T pop(String act, T impl) {
		List<T> currentModulePrev = getModuleStack(act);
		T was = currentModulePrev.get(0);
		assert (was == impl);
		return currentModulePrev.remove(0);
	}

	public static <T> T peek(String act) {
		List<T> currentModulePrev = getModuleStack(act);
		if (currentModulePrev.size() < 1)
			return (T) null;
		return currentModulePrev.get(0);

	}

	public static <T> T peek(String act, int idx) {
		List<T> currentModulePrev = getModuleStack(act);
		int len = currentModulePrev.size() - 1;
		int fnd = 0;
		T was = null;
		while (idx > 0) {
			T iz = currentModulePrev.get(fnd);
			if (iz != was) {
				idx--;
				was = iz;
			}
			if (fnd >= idx) {
				return null;
			}
			fnd++;
		}
		return was;

	}

	public static Class ensureLoadable(String className, boolean tryInit, ClassLoader threadCL) {
		PrintStream ps = System.err;
		threadCL = getCorrectLoader(threadCL);
		try {
			Class c = Class.forName(className, false, threadCL);
			if (tryInit) {
				Class.forName(className, true, threadCL);
			}
			rememberClass(className, c);
			return c;
		} catch (Throwable e) {
			ClassLoader pcl = getPromiscuousClassLoader();
			if (pcl != threadCL) {
				Class c = ensureLoadable(className, tryInit, pcl);
				if (c != null) {
					ps.println("PCL can see but not thread-context-CL=" + threadCL + " cannot see " + className + " " + e);
					if (threadCL != null) {
						try {
							callProtectedMethod(false, threadCL, "resolveClass", c);
						} catch (Throwable e1) {
							Debuggable.printStackTrace(e1);
						}
					}
				}
				return c;
			}
			return null;
		}
	}

	public static boolean ensureLoadable(String className) {
		if (ensureLoadable(className, false, null) != null)
			return true;
		ensureLoadable(className, false, null);
		return false;
	}

	public static <T> T castAs(Object obj) {
		try {
			return (T) obj;
		} catch (ClassCastException cce) {
			return null;
		}
	}

	public static int scanLoadable(String path, final boolean recurse, boolean canSplit, final boolean doWithBytes) {
		ClassLoader loader = getThreadClassLoader();
		try {
			return scanLoadable(path, recurse, canSplit, loader, doWithBytes, getProtectionDomain(loader));
		} catch (Throwable r) {
			throw Debuggable.reThrowable(r);
		}
	}

	private static InputStream getInputStream(String path) throws IOException {
		File file = new File(path);
		if (file.exists()) {
			return new FileInputStream(file);
		}
		try {
			return new URL(path).openStream();
		} catch (MalformedURLException e) {
			throw new IOException(e);
		}
	}

	private static InputStream getInputStream(URL path) throws IOException {
		return path.openStream();
	}

	public static int scanLoadable(final String pathIn, final boolean recurse, final boolean canSplit, final ClassLoader loader, final boolean doWithBytes, final ProtectionDomain pd) {
		String path = pathIn;
		File file = new File(path);
		if (file.exists()) {
			String np = file.getAbsolutePath();
			if (!np.equals(path)) {
				return scanLoadable(np, recurse, false, loader, doWithBytes, pd);
			}
			return scanLoadablePathRoot(path, path, loader, recurse, doWithBytes, pd);
		}
		try {
			URL url = new URL(path);
			file = new File(url.getFile());
			InputStream is = null;
			if (file.exists()) {
				path = file.getAbsolutePath();
			}
			if (url != null) {
				return scanFile(path, path, url.openStream(), loader, doWithBytes, pd);
			}
		} catch (MalformedURLException e) {
		} catch (IOException e) {
			Debuggable.printStackTrace(e);
		}
		if (!canSplit) {
			System.err.println("Invalid path: " + pathIn);
			return 0;
		}
		final int[] collect = new int[1];
		substAll(path, true, -1, new ArchiveSearcher() {
			@Override
			public String replace(String target, boolean maySplit, int depth) {
				int fnd = scanLoadable(target, recurse, maySplit, loader, doWithBytes, pd);
				collect[0] += fnd;
				return target;
			}
		});
		return collect[0];
	}

	public static int scanFile(String packageOff, String path, InputStream openStream, ClassLoader loader, boolean doWithBytes, ProtectionDomain pd) {
		if (path.endsWith(".class")) {
			try {
				String className = getFClassName(packageOff, path);
				boolean useBytesHere = doWithBytes || className == null;
				return scanClassFile(className, path, openStream, loader, useBytesHere, pd) != null ? 1 : 0;
			} catch (IOException e) {
				Debuggable.printStackTrace(e);
			}
			return 0;
		}
		// otherwise its a zipish thing
		try {
			return scanJarFile(asURL(path), openStream, loader, "", doWithBytes, pd);
		} catch (IOException e) {
			Debuggable.printStackTrace(e);
			return 0;
		}
	}

	private static URL asURL(String path) {
		URL url;
		try {
			url = new URL(path);
			return url;
		} catch (MalformedURLException e) {
			File f = new File(path);
			if (f.exists())
				try {
					return f.getAbsoluteFile().toURL();
				} catch (MalformedURLException e1) {
				}
			return null;
		}

	}

	static HashSet<URL> scannedURLS = new HashSet<URL>();

	public static boolean isScanned(URL scanned) {
		if (scannedURLS.contains(scanned)) {
			System.out.println("Already scanned " + scanned);
			return false;
		}
		scannedURLS.add(scanned);
		return true;
	}

	private static int scanLoadablePathRoot(String packageOff, String path, ClassLoader loader, final boolean recurse, final boolean doWithBytes, ProtectionDomain pd) {
		try {
			File f = new File(packageOff);
			if (f.isDirectory()) {
				URL scan = asURL(path);
				if (isScanned(scan))
					return 0;
				System.out.println("Loading: " + scan);
				return scanLoadablePath(packageOff, packageOff, path, loader, recurse, doWithBytes, pd);
			}
			return scanLoadablePath(packageOff, packageOff, path, loader, recurse, doWithBytes, pd);
		} catch (Throwable r) {
			throw Debuggable.reThrowable(r);
		}
	}

	private static int scanLoadablePath(String opak, String packageOff, String path, ClassLoader loader, final boolean recurse, final boolean doWithBytes, ProtectionDomain pd) {
		try {

			File f = new File(path);
			if (f.isDirectory()) {

				int fnd = 0;
				for (File o : f.listFiles()) {
					String of = o.getAbsolutePath();
					fnd += scanLoadablePath(opak, packageOff, of, loader, recurse, doWithBytes, pd);
				}
				return fnd;
			}
			if (f.exists()) {
				if (path.endsWith(".jar") || path.endsWith(".zip") || path.endsWith(".war") || path.endsWith(".class")) {
					try {
						return scanFile(packageOff, path, null, loader, doWithBytes, pd);
					} catch (Throwable e) {
						Debuggable.printStackTrace(e);
					}
				}
				return 0;
			}
			return 0;

		} catch (Throwable r) {
			throw Debuggable.reThrowable(r);
		}

	}

	private static String getFClassName(String packageOff, String path) {
		if (path.startsWith(packageOff))
			path = path.substring(packageOff.length());
		String className = path.replaceAll("/", "\\.").replaceAll("\\\\", "\\.").substring(0, path.length() - 6);
		if (className.startsWith("."))
			className = className.substring(1);
		return className;
	}

	public static int scanJarFile(URL jarName, InputStream jarIn, ClassLoader loader, String packageNameAndBelow, boolean doWithBytes, ProtectionDomain pd) throws IOException {
		if (isScanned(jarName))
			return 0;
		int found = 0;
		if (jarIn == null) {
			jarIn = getInputStream(jarName);
		}
		URLClassLoader loader0 = castAs(loader);
		if (loader0 != null) {
			try {
				callProtectedMethod(false, loader0, "addURL", jarName);
			} catch (InvocationTargetException e) {
				Debuggable.printStackTrace(e);
				loader0 = null;
				// fall thru and define below
			} catch (NoSuchMethodException e) {
				Debuggable.printStackTrace(e);
				loader0 = null;
				// fall thru and define below
			}
		}
		if (loader0 == null) {
			loader0 = URLClassLoader.newInstance(new URL[] { jarName }, loader);
			if (pd == null) {
				pd = getProtectionDomain(loader);
			}
			addClassloader(loader0);
			loader = loader0;
		}

		System.out.println("Loading: " + jarName);

		ZipInputStream jarFile = new ZipInputStream(jarIn);
		try {
			ZipEntry jarEntry;
			while (true) {
				jarEntry = jarFile.getNextEntry();
				if (jarEntry == null) {
					break;
				}
				String entryName = jarEntry.getName();
				if (entryName.endsWith(".class")) {
					found++;
					Class c = null;
					String className = entryName.replaceAll("/", "\\.").substring(0, jarEntry.getName().length() - 6);
					if (!className.startsWith(packageNameAndBelow))
						continue;
					String path = "jar:" + jarName + "!" + entryName;

					if (!doWithBytes) {
						try {
							c = findLoadedClassByName(className, loader);
							if (c == null) {
								c = Class.forName(className, false, loader);
								c = rememberClass(className, c);
							}
							continue;
						} catch (Throwable e) {
							Debuggable.printStackTrace(e);
							// fall thru and define below
						}

					}
					byte[] buffer = getBytes(jarFile);
					InputStream bis = new ByteArrayInputStream(buffer);
					try {
						scanClassFile(className, path, bis, loader, doWithBytes, pd);
					} catch (Throwable r) {
						Debuggable.printStackTrace(r);
					}
					bis.close();
					//c = defineClass(loader, className, buffer);

					//	InputStream clzins = new ByteArrayInputStream(buffer);
					//	scanClassFile(className, "jar:" + jarName + "!" + entryName, clzins, loader, doWithBytes);

				}

			}
		} catch (Throwable ee) {
			Debuggable.printStackTrace(ee);
		} finally {
			try {
				if (jarFile != null)
					jarFile.close();
			} catch (IOException e) {
			}
		}
		return found;
	}

	public static ProtectionDomain getProtectionDomain(ClassLoader loader) {
		return loader.getClass().getProtectionDomain();
	}

	public static void pingLoadable(Class anyClass) {
		//PromiscuousClassUtils.scanLoadable("D:/MyEclipse2013/configuration/org.eclipse.osgi/bundles/1277/1/.cp/lib/scala-library.jar", true, false, true);
		PromiscuousClassUtils.scanLoadable("C:\\Users\\Administrator\\.m2\\repository\\org\\scala-lang\\scala-library\\2.8.1\\scala-library-2.8.1.jar", true, false, true);
		//		PromiscuousClassUtils.scanLoadable("file:/c:/Users/Administrator/.m2/repository/org/slf4j/slf4j-log4j12/1.6.1/slf4j-log4j12-1.6.1.jar", true, false, true);
		PromiscuousClassUtils.scanLoadable("file:/c:/Users/Administrator/.m2/repository/org/slf4j/slf4j-api/1.5.8/slf4j-api-1.5.8.jar", true, false, true);
		PromiscuousClassUtils.scanLoadable("file:/c:/Users/Administrator/.m2/repository/org/slf4j/slf4j-log4j12/1.5.8/slf4j-log4j12-1.5.8.jar", true, false, true);
		String cp = System.getProperty("java.class.path");
		scanLoadable(cp, true, true, true);
		PromiscuousClassUtils.scanLoadable("C:\\Users\\Administrator\\.m2\\repository\\dmiles\\resources\\", true, false, true);
		PromiscuousClassUtils.ensureLoadable("org.slf4j.helpers.NOPLoggerFactory");
		Class c = PromiscuousClassUtils.findLoadedClassByName("org.slf4j.helpers.NOPLoggerFactory");
		PromiscuousClassUtils.ensureLoadable("scala.ScalaObject");
		PromiscuousClassUtils.ensureLoadable("org.slf4j.Logger");
		c = PromiscuousClassUtils.findLoadedClassByName("org.slf4j.spi.LocationAwareLogger");
		PromiscuousClassUtils.ensureLoadable("org.slf4j.spi.LocationAwareLogger");
		PromiscuousClassUtils.ensureLoadable("org.appdapter.osgi.core.BundleActivatorBase");
		PromiscuousClassUtils.ensureLoadable("org.slf4j.ILoggerFactory");
		PromiscuousClassUtils.ensureLoadable("org.slf4j.helpers.SubstituteLoggerFactory");
		PromiscuousClassUtils.scanLoadable("D:\\dev\\hrk_eclipse_workspace\\Robokind\\target\\eclipse-classes\\", true, false, false);
	}

	public static Class defineClass(ClassLoader loader, String className, byte[] bytes, ProtectionDomain pd) throws LinkageError {
		try {
			if (loader == null) {
				loader = getThreadClassLoader();
			}
			pd = null;
			Class c = null;
			if (className == null) {
				c = callProtectedMethod(false, loader, "defineClass", bytes, 0, bytes.length, pd);
				className = c.getCanonicalName();
			} else {
				if (className.startsWith("java.") || className.startsWith("sun."))
					try {
						return Class.forName(className, false, null);
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					}
				c = callProtectedMethod(false, loader, "findLoadedClass", className);
				if (c == null) {
					c = findLoadedClassByName(className);
					if (c == null) {
						c = callProtectedMethod(false, loader, "defineClass", className, bytes, 0, bytes.length, pd);
					} else {
						// something is odd
					}
				}
			}
			rememberClass(className, c);
			return c;
		} catch (InvocationTargetException e) {
			Throwable t = e.getCause();
			if (t instanceof NoClassDefFoundError) {
				System.err.println("while: defining " + className + " caught " + t);
				// A class refers to an uunresolvable class.. thatr is bound to happen with 34rd party jars
			} else {
				if (false)
					Debuggable.printStackTrace(t);
				System.err.println("while: defining " + className + " caught " + t);
				return null;
			}
			if (t instanceof LinkageError) {
				if (true)
					return null;
				throw (LinkageError) t;
			}
			return null;
		} catch (NoSuchMethodException e) {
			Debuggable.printStackTrace(e);
			return null;
		}
	}

	private static byte[] getBytes(InputStream is) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[2048];
		OutputStream out = new BufferedOutputStream(bos);
		int len;

		while ((len = is.read(buf)) > 0) {
			out.write(buf, 0, len);
		}

		// release resource
		out.flush();
		out.close();
		byte[] bytes = bos.toByteArray();
		return bytes;
	}

	private static File findClassesDir(Class<?> clazz) {
		try {
			String path = clazz.getProtectionDomain().getCodeSource().getLocation().getFile();
			final String codeSourcePath = URLDecoder.decode(path, "UTF-8");
			return new File(codeSourcePath, clazz.getPackage().getName().replace('.', File.separatorChar));
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}

	private static Class scanClassFile(String className, String path, InputStream fis, ClassLoader loader, boolean doWithBytes, ProtectionDomain pd) throws IOException {
		if (!doWithBytes) {
			Class c;
			try {
				c = Class.forName(className, false, loader);
				rememberClass(className, c);
				return c;
			} catch (Throwable e) {
				Debuggable.printStackTrace(e);
			}
		}
		boolean closeStream = false;
		if (fis == null) {
			closeStream = true;
			fis = getInputStream(path);
		}
		byte[] bytes = getBytes(fis);
		if (closeStream)
			fis.close();
		return defineClass(loader, className, bytes, pd);
	}

	public static void addLocationToSystemClassPath(String location) {
		PromiscuousClassUtils.scanLoadable(location, true, false, null, true, null);
	}

}
