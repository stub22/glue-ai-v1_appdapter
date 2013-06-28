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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.*;
import java.util.List;

import org.appdapter.core.log.Debuggable;
import org.osgi.framework.BundleContext;
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
	private final static Logger theLogger = LoggerFactory.getLogger(ClassLoaderUtils.class.getName());

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
			registeredLoaders.add(cl);
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
			registeredLoaders.add(cl);
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
		List<ClassLoader> resourceLoaders = new ArrayList<ClassLoader>();
		if (context == null) {
			return resourceLoaders;
		}
		if (resourceClassLoaderType == null || resourceClassLoaderType.isEmpty()) {
			resourceClassLoaderType = ALL_RESOURCE_CLASSLOADER_TYPES;
		}
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
				registeredLoaders.add(l);
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
		addIfNew(resourceLoaders, Thread.currentThread().getContextClassLoader());
		addIfNew(resourceLoaders, ClassLoaderUtils.class.getClassLoader());
		addIfNew(resourceLoaders, ClassLoader.getSystemClassLoader());
		addAllIfNew(resourceLoaders, registeredLoaders);
		addAllIfNew(registeredLoaders, resourceLoaders);
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

	static HashSet<ClassLoader> registeredLoaders = new HashSet<ClassLoader>();

	public static void registerClassLoader(BundleContext context, ClassLoader loader, String resourceClassLoaderType) {
		if (context == null || loader == null) {
			theLogger.warn(Debuggable.toInfoStringCompound("registerClassLoader", context, loader, resourceClassLoaderType));
			return;
		}
		if (resourceClassLoaderType == null) {
			resourceClassLoaderType = "UNKNOWN";
			theLogger.warn(Debuggable.toInfoStringCompound("UNKNOWN TYPE: registerClassLoader", context, loader, resourceClassLoaderType));
		}
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put(RESOURCE_CLASSLOADER_TYPE, resourceClassLoaderType);
		registeredLoaders.add(loader);
		context.registerService(ClassLoader.class.getName(), loader, props);
	}
}
