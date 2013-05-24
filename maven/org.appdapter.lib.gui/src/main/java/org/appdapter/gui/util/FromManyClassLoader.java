package org.appdapter.gui.util;

import static org.appdapter.gui.util.PromiscuousClassUtils.isSomething;
import static org.appdapter.gui.util.PromiscuousClassUtils.rememberClass;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;


/**
 * A class loader to allow for loading of other jars that are added as a URL.
 */
public final class FromManyClassLoader extends IsolatingClassLoaderBase
		implements HRKRefinement.DontAdd {
	/** Dynamically added ClassLoaders. */
	private final Collection<ClassLoader> classLoadersToSearch;

	/**
	 * Constructs a new object.
	 * 
	 * @param parent
	 *            the parent class loader.
	 */
	public FromManyClassLoader(final Collection<ClassLoader> list,
			ClassLoader parent) {
		super(new URL[0], parent);
		classLoadersToSearch = list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addURL(final URL url) {
		super.addURL_super(url);
	}

	public void addClassLoader(final ClassLoader url) {
		if (getClassLoadersToSearch().contains(url)) {
			return;
		}
		synchronized (classLoadersToSearch) {
			classLoadersToSearch.add(url);
		}
	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		for (ClassLoader cl : getClassLoadersToSearch()) {
			Class<?> result = PromiscuousClassUtils
					.callProtectedMethodNullOnUncheck(cl, "findClass", name);
			if (isSomething(result))
				return rememberClass(result);
		}
		return rememberClass(super.findClass(name));
	}

	static Class[] CLASS_STRING_1 = new Class[] { String.class };

	@Override
	public URL findResource(String name) {
		for (ClassLoader cl : getClassLoadersToSearch()) {
			URL result = PromiscuousClassUtils
					.callProtectedMethodNullOnUncheck(cl, "findResource", name);
			if (isSomething(result))
				return result;
		}
		return super.findResource(name);
	}

	private Class<?> findLoadedClassFromOne(String name) {
		for (ClassLoader cl : getClassLoadersToSearch()) {
			Class<?> result = PromiscuousClassUtils
					.callProtectedMethodNullOnUncheck(cl, "findLoadedClass",
							name);
			if (isSomething(result))
				return rememberClass(result);
		}
		return rememberClass(super.findLoadedClass(name));
	}

	@Override
	public Enumeration<URL> findResources(String name) throws IOException {
		for (ClassLoader cl : getClassLoadersToSearch()) {
			Enumeration<URL> result = PromiscuousClassUtils
					.callProtectedMethodNullOnUncheck(cl, "findResources", name);
			if (isSomething(result))
				return result;
		}
		return super.findResources(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> loadClass(final String name) throws ClassNotFoundException {
		ClassNotFoundException orig = null;
		try {
			return rememberClass(loadClassUseSystem(name, true));
		} catch (ClassNotFoundException e) {
			orig = e;
		}
		try {
			return rememberClass(loadClassUseSystem(name, false));
		} catch (ClassNotFoundException e) {
			throw orig;
		}
	}

	public Class<?> loadClassUseSystem(final String name, boolean useSystem)
			throws ClassNotFoundException {
		Class<?> loadedClass = findLoadedClassFromOne(name);
		if (loadedClass == null) {
			try {
				if (useSystem || name.startsWith("java.")) {
					loadedClass = Class.forName(name);
				} else {
					loadedClass = findClass(name);
				}
			} catch (ClassNotFoundException e) {
				// Swallow exception
				// does not exist locally
			}
			if (loadedClass == null) {
				if (useSystem || name.startsWith("java.")) {
					final ClassLoader systemLoader = ClassLoader
							.getSystemClassLoader();
					loadedClass = systemLoader.loadClass(name);
				} else {
					loadedClass = super.loadClass(name);
				}
			}
		}
		return loadedClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addPaths(List<Object> strings, boolean includeParent) {
		boolean changed = false;
		for (ClassLoader url : getClassLoadersToSearch()) {
			if (pathsOf(strings, url, includeParent))
				changed = true;
		}
		if (includeParent) {
			if (pathsOf(strings, getParent(), includeParent))
				changed = true;
		}
		return changed;
	}

	/**
	 * @return the classLoadersToSearch
	 */
	private Collection<ClassLoader> getClassLoadersToSearch() {
		synchronized (classLoadersToSearch) {
			return new ArrayList<ClassLoader>(classLoadersToSearch);
		}
	}
}
