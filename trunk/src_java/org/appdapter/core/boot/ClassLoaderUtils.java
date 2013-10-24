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

package org.appdapter.core.boot;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.appdapter.core.log.Debuggable;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class ClassLoaderUtils {
	public final static String ALL_RESOURCE_CLASSLOADER_TYPES = "*";

	public final static String RESOURCE_CLASSLOADER_TYPE = "ResourceClassLoaderType";
	private final static Logger theLogger = LoggerFactory.getLogger(ClassLoaderUtils.class);

	static {

	}

	private static <T> boolean addIfNew(Collection<T> col, T e) {
		if (e == null)
			return false;
		if (col.contains(e))
			return false;
		col.add(e);
		return true;
	}

	private static <T> boolean addAllIfNew(Collection<T> col, Iterable<T> ite) {
		if (ite == null)
			return false;
		boolean added = false;
		for (T e : ite)
			if (!col.contains(e)) {
				added = true;
				col.add(e);
			}
		return added;
	}

	public static ClassLoader findResourceClassLoader(String path, List<ClassLoader> cLoaders) {
		for (ClassLoader cl : cLoaders) {
			allSeenEverLoaders.add(cl);
			// This method will first search the parent class loader for the resource; if the parent is null the path of
			// the class loader built-in to the virtual machine is searched. That failing, this method will invoke
			// findResource(String) to find the resource.
			URL res = cl.getResource(path);
			if (res != null) {

				return cl;
			}
		}
		return null;
	}

	public static URL findResourceURL(String path, List<ClassLoader> cLoaders) {
		for (ClassLoader cl : cLoaders) {
			allSeenEverLoaders.add(cl);
			// This method will first search the parent class loader for the resource; if the parent is null the path of
			// the class loader built-in to the virtual machine is searched. That failing, this method will invoke
			// findResource(String) to find the resource.
			URL res = cl.getResource(path);
			if (res != null) {
				return res;
			}
		}
		return null;
	}

	public static List<ClassLoader> getFileResourceClassLoaders(BundleContext context, String resourceClassLoaderType) {
		if (resourceClassLoaderType == null || resourceClassLoaderType.isEmpty()) {
			resourceClassLoaderType = ALL_RESOURCE_CLASSLOADER_TYPES;
		}
		if (context == null) {
			return getFileResourceClassLoaders(resourceClassLoaderType);
		}
		List<ClassLoader> resourceLoaders = new ArrayList<ClassLoader>();
		ServiceReference[] loaders = null;
		String filter = "(" + RESOURCE_CLASSLOADER_TYPE + "=" + resourceClassLoaderType + ")";
		try {
			loaders = context.getServiceReferences(ClassLoader.class.getName(), filter);
		} catch (InvalidSyntaxException ex) {
			theLogger.warn("Syntax error with file resource ClassLoader filter string: " + filter + ".");
		}
		if (loaders == null || loaders.length == 0) {
			return resourceLoaders;
		}
		for (ServiceReference ref : loaders) {
			ClassLoader l = getLoader(context, ref);
			if (l != null) {
				resourceLoaders.add(l);
				allSeenEverLoaders.add(l);
			}
		}
		return resourceLoaders;
	}

	public static List<ClassLoader> getFileResourceClassLoaders(String resourceClassLoaderType, List<ClassLoader>... arrayOfcLoaders) {
		List<ClassLoader> resourceLoaders = new ArrayList<ClassLoader>();
		for (List<ClassLoader> cLoaders : arrayOfcLoaders) {
			if (cLoaders != null) {
				addAllIfNew(resourceLoaders, cLoaders);
			}
		}
		boolean isAll = false;
		boolean useDefaults = true;
		boolean useAllSeenLoaders = false;

		if (resourceClassLoaderType == null || resourceClassLoaderType.isEmpty() || resourceClassLoaderType.equals(ALL_RESOURCE_CLASSLOADER_TYPES)) {
			resourceClassLoaderType = ALL_RESOURCE_CLASSLOADER_TYPES;
			isAll = true;
			useAllSeenLoaders = true;
		}
		synchronized (namedLoaders) {

			if (isAll) {
				addAllIfNew(resourceLoaders, namedLoaders.values());
			} else {
				ClassLoader cl = namedLoaders.get(resourceClassLoaderType);
				if (cl == null) {
					theLogger.error("MISSING CLASSLOADER TYPE: ", resourceClassLoaderType);
					useAllSeenLoaders = true;
				} else {
					addIfNew(resourceLoaders, cl);
				}
			}
		}

		if (useDefaults) {
			addIfNew(resourceLoaders, Thread.currentThread().getContextClassLoader());
			addIfNew(resourceLoaders, ClassLoader.getSystemClassLoader());
		}
		if (useAllSeenLoaders)
			synchronized (allSeenEverLoaders) {
				addAllIfNew(resourceLoaders, allSeenEverLoaders);
			}
		theLogger.info(Debuggable.toInfoStringCompound("getFileResourceClassLoaders-Count", resourceLoaders.size(), resourceLoaders));
		return resourceLoaders;
	}

	private static ClassLoader getLoader(BundleContext context, ServiceReference ref) {
		if (context == null || ref == null) {
			theLogger.warn(Debuggable.toInfoStringCompound("returning null from getLoader", context, ref));
			return null;
		}
		Object obj = context.getService(ref);
		if (obj == null || !ClassLoader.class.isAssignableFrom(obj.getClass())) {
			theLogger.warn(Debuggable.toInfoStringCompound("returning null from getLoader+obj", context, ref, obj));
			return null;
		}
		return (ClassLoader) obj;
	}

	static HashSet<ClassLoader> allSeenEverLoaders = new HashSet<ClassLoader>();
	static Map<String, ClassLoader> namedLoaders = new HashMap<String, ClassLoader>();

	public static void registerClassLoader(BundleContext context, ClassLoader loader, String resourceClassLoaderType) {
		withClassLoader(context, loader, resourceClassLoaderType, false, false);
	}

	private static void withClassLoader(BundleContext context, ClassLoader loader, String resourceClassLoaderType, boolean contextOptional, boolean isRemoval) {
		if (loader == null && !isRemoval) {
			theLogger.error(Debuggable.toInfoStringCompound("NULLS in registerClassLoader", context, loader, resourceClassLoaderType, "contextOptional=", contextOptional, "isRemoval=", isRemoval));
			return;
		}
		boolean isNamed = true;
		if (resourceClassLoaderType == null) {
			resourceClassLoaderType = "UNKNOWN";
			theLogger
					.warn(Debuggable.toInfoStringCompound("UNKNOWN TYPE: registerClassLoader", context, loader, resourceClassLoaderType, "contextOptional=", contextOptional, "isRemoval=", isRemoval));
			isNamed = false;
		} else if (resourceClassLoaderType.isEmpty() || resourceClassLoaderType.equals(ALL_RESOURCE_CLASSLOADER_TYPES)) {
			resourceClassLoaderType = ALL_RESOURCE_CLASSLOADER_TYPES;
			isNamed = false;
		}/*
		 theLogger.info(Debuggable.toInfoStringCompound("registerClassLoader",
		 	context, loader, resourceClassLoaderType, "contextOptional=",
		 	contextOptional, "isRemoval=", isRemoval,
		 	"isNamed=", isNamed));*/
		synchronized (namedLoaders) {
			if (isRemoval) {
				allSeenEverLoaders.remove(loader);
				if (isNamed) {
					namedLoaders.remove(resourceClassLoaderType);
				}

			} else {
				allSeenEverLoaders.add(loader);
				if (isNamed) {
					namedLoaders.put(resourceClassLoaderType, loader);
				}
			}

		}
		if (context == null) {
			if (!contextOptional)
				theLogger
						.error(Debuggable.toInfoStringCompound("NULLS in registerClassLoader", context, loader, resourceClassLoaderType, "contextOptional=", contextOptional, "isRemoval=", isRemoval));
			return;
		}
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put(RESOURCE_CLASSLOADER_TYPE, resourceClassLoaderType);
		context.registerService(ClassLoader.class.getName(), loader, props);
	}

	public static void registerClassLoader(Object something, BundleContext ctx) {
		Class thisClass = something.getClass();
		if (something instanceof Class) {
			thisClass = (Class) something;
		}
		withClassLoader(ctx, getClassLoader(thisClass), thisClass.getPackage().getName(), true, false);

	}

	public static void unregisterClassLoader(Object something, BundleContext ctx) {
		Class thisClass = something.getClass();
		if (something instanceof Class) {
			thisClass = (Class) something;
		}
		withClassLoader(ctx, getClassLoader(thisClass), thisClass.getPackage().getName(), false, true);

	}

	public static ClassLoader getClassLoader(Class thisClass) {
		ClassLoader cl = null;
		Bundle bundle = FrameworkUtil.getBundle(thisClass);
		if (bundle != null)
			cl = getBundleClassLoader(bundle);
		if (cl != null)
			return cl;
		cl = thisClass.getClassLoader();
		if (cl != null)
			return cl;
		return null;
	}

	public static ClassLoader getClasssLoader_Require_POM_REference(Bundle bundle) {
		/*
		BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
		ClassLoader classLoader = bundleWiring.getClassLoader();
		return classLoader;*/
		return null;
	}

	public static ClassLoader getBundleClassLoader(Bundle bundle) {
		String bundleActivator = (String) bundle.getHeaders().get("Bundle-Activator");
		if (bundleActivator == null) {
			bundleActivator = (String) bundle.getHeaders().get("Jetty-ClassInBundle");
		}
		if (bundleActivator != null) {
			try {
				return bundle.loadClass(bundleActivator).getClassLoader();
			} catch (ClassNotFoundException e) {
				// should not happen as we are called if the bundle is started
				// anyways.
				e.printStackTrace();
			}
		}

		return internalGetFelixBundleClassLoader(bundle);
	}

	private static Field Felix_BundleImpl_m_modules_field;

	private static Field Felix_ModuleImpl_m_classLoader_field;

	private static Method Felix_adapt_method;

	private static Method Felix_bundle_wiring_getClassLoader_method;

	private static Class Felix_bundleWiringClazz;

	private static Boolean isFelix403 = null;

	private static ClassLoader internalGetFelixBundleClassLoader(Bundle bundle) {
		//firstly, try to find classes matching a newer version of felix

		if (isFelix403.booleanValue()) {
			try {
				Object wiring = Felix_adapt_method.invoke(bundle, new Object[] { Felix_bundleWiringClazz });
				ClassLoader cl = (ClassLoader) Felix_bundle_wiring_getClassLoader_method.invoke(wiring);
				return cl;
			} catch (Exception e) {
				return null;
			}
		}

		// Fallback to trying earlier versions of felix.
		if (Felix_BundleImpl_m_modules_field == null) {
			try {
				Class bundleImplClazz = bundle.getClass().getClassLoader().loadClass("org.apache.felix.framework.BundleImpl");
				Felix_BundleImpl_m_modules_field = bundleImplClazz.getDeclaredField("m_modules");
				Felix_BundleImpl_m_modules_field.setAccessible(true);
			} catch (ClassNotFoundException e) {
			} catch (NoSuchFieldException e) {
			}
		}

		// Figure out which version of the modules is exported
		Object currentModuleImpl;
		try {
			Object[] moduleArray = (Object[]) Felix_BundleImpl_m_modules_field.get(bundle);
			currentModuleImpl = moduleArray[moduleArray.length - 1];
		} catch (Throwable t2) {
			try {
				List<Object> moduleArray = (List<Object>) Felix_BundleImpl_m_modules_field.get(bundle);
				currentModuleImpl = moduleArray.get(moduleArray.size() - 1);
			} catch (Exception e) {
				return null;
			}
		}

		if (Felix_ModuleImpl_m_classLoader_field == null && currentModuleImpl != null) {
			try {
				Felix_ModuleImpl_m_classLoader_field = bundle.getClass().getClassLoader().loadClass("org.apache.felix.framework.ModuleImpl").getDeclaredField("m_classLoader");
				Felix_ModuleImpl_m_classLoader_field.setAccessible(true);
			} catch (ClassNotFoundException e) {
				return null;
			} catch (NoSuchFieldException e) {

				return null;
			}
		}
		// first make sure that the classloader is ready:
		// the m_classLoader field must be initialized by the
		// ModuleImpl.getClassLoader() private method.
		ClassLoader cl = null;
		try {
			cl = (ClassLoader) Felix_ModuleImpl_m_classLoader_field.get(currentModuleImpl);
			if (cl != null)
				return cl;
		} catch (Exception e) {

			return null;
		}

		// looks like it was not ready:
		// the m_classLoader field must be initialized by the
		// ModuleImpl.getClassLoader() private method.
		// this call will do that.
		try {
			bundle.loadClass("java.lang.Object");
			cl = (ClassLoader) Felix_ModuleImpl_m_classLoader_field.get(currentModuleImpl);
			return cl;
		} catch (Exception e) {
			return null;
		}
	}

	public static URL getFileResource(String resourceClassLoaderType, String resourceName) {
		for (ClassLoader cl : getFileResourceClassLoaders(resourceClassLoaderType)) {
			URL url = cl.getResource(resourceName);
			if (url != null)
				return url;
		}
		return null;
	}

	public static List<ClassLoader> getCurrentClassLoaderList() {
		//addIfNew(resourceLoaders, Thread.currentThread().getContextClassLoader());
		//addIfNew(resourceLoaders, ClassLoader.getSystemClassLoader());
		return (List<ClassLoader>) Collections.singletonList(Thread.currentThread().getContextClassLoader());
	}
}
