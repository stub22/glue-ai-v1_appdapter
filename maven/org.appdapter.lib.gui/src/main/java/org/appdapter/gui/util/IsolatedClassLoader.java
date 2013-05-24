package org.appdapter.gui.util;

import static org.appdapter.gui.util.PromiscuousClassUtils.addClassloader;
import static org.appdapter.gui.util.PromiscuousClassUtils.rememberClass;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

/**
 * A class loader to allow for loading of other jars that are added as a URL.
 * 
 */
public final class IsolatedClassLoader extends IsolatingClassLoaderBase
		implements HRKRefinement {
	/** Dynamically added URLs. */
	private final Collection<URL> urls;

	/**
	 * Constructs a new object.
	 * 
	 * @param parent
	 *            the parent class loader.
	 */
	public IsolatedClassLoader(final ClassLoader parent) {
		super(new URL[0], parent);
		urls = new java.util.ArrayList<URL>();
		addClassloader(parent);
		addClassloader(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addURL(final URL url) {
		if (urls.contains(url)) {
			return;
		}

		urls.add(url);
		super.addURL_super(url);
	}

	/**
	 * Adds the given URLs to the classpath.
	 * 
	 * @param additions
	 *            URLs to add.
	 */
	@Override
	public void addURLs(final URL[] additions) {
		for (URL url : additions) {
			addURL(url);
		}
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		return rememberClass(super.findClass(name));
	}

	@Override
	public URL findResource(String name) {
		return super.findResource(name);
	}

	@Override
	public Enumeration<URL> findResources(String name) throws IOException {
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
		Class<?> loadedClass = findLoadedClass(name);
		if (loadedClass != null)
			return loadedClass;

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
				loadedClass = loadClass(name, false); // same as
														// super.loadClass(name);
			}
		}

		return loadedClass;
	}

	/**
	 * Returns the search path of URLs for loading classes and resources. This
	 * includes the original list of URLs specified to the constructor, along
	 * with any URLs subsequently appended by the addURL() method.
	 * 
	 * @return the search path of URLs for loading classes and resources.
	 */
	@Override
	public URL[] getURLs() {
		URL[] surls = super.getURLs();
		if (surls.length != urls.size()) {
			throw new RuntimeException("Bad get URLS! ");
		}
		return surls;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addPaths(List<Object> strings, boolean includeParent) {
		boolean changed = addCollection(strings, this, getURLs());
		if (includeParent) {
			if (pathsOf(strings, getParent(), includeParent))
				changed = true;
		}
		return changed;
	}

	public static <ET> boolean addCollection(List<Object> strings,
			ClassLoader thiz, ET[] elems) {
		boolean changed = false;
		if (PromiscuousClassUtils.addAllNew(strings, elems))
			changed = true;
		return changed;
	}
}
