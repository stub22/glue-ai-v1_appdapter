package org.appdapter.gui.util;

import static org.appdapter.gui.util.CollectionSetUtils.addAllNew;
import static org.appdapter.gui.util.CollectionSetUtils.addIfNew;
import static org.appdapter.gui.util.PromiscuousClassUtils.isSomething;
import static org.appdapter.gui.util.PromiscuousClassUtils.rememberClass;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.appdapter.core.log.Debuggable;


/**
 * A class loader to allow for loading of other jars that are added as a URL.
 */
public final class FromManyClassLoader extends IsolatingClassLoaderBase implements HRKRefinement.DontAdd {
	/** Dynamically added ClassLoaders. */
	private final Collection<ClassLoader> classLoadersToSearch;

	/**
	 * Constructs a new object.
	 * 
	 * @param parent
	 *            the parent class loader.
	 */
	public FromManyClassLoader(final Collection<ClassLoader> list, ClassLoader parent) {
		super(new URL[0], parent);
		classLoadersToSearch = list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void addURL(final URL url) {
		super.addURL_super(url);
	}

	public void addClassLoader(final ClassLoader url) {
		if (classLoadersToSearch.contains(url)) {
			return;
		}
		synchronized (classLoadersToSearch) {
			classLoadersToSearch.add(url);
		}
	}

	static Class[] CLASS_STRING_1 = new Class[] { String.class };

	@Override public URL findResource(String name) {
		return findPromiscuousResource(name, new ArrayList<URL>());
	}

	public URL findPromiscuousResource(String name, Collection<URL> exceptFor) {
		if (PromiscuousClassUtils.contains("findResource", name)) {
			return null;
		}
		PromiscuousClassUtils.push("findResource", name);
		try {
			Collection<ClassLoader> cls = getClassLoadersToSearch(true);
			URL url = null;
			for (ClassLoader cl : cls) {
				try {
					url = PromiscuousClassUtils.callProtectedMethodNullOnUncheck(cl, "findResource", name);
					if (isSomething(url)) {
						if (exceptFor.contains(url))
							continue;
						return url;
					}
				} catch (Throwable e) {
					e.printStackTrace();
					continue;
				}
			}
			try {
				Enumeration<URL> enumU = getResources(name);
				while (enumU.hasMoreElements()) {
					url = enumU.nextElement();
					if (url == null)
						continue;
					if (exceptFor != null && exceptFor.contains(url))
						continue;
					return url;
				}
				return null;
			} catch (Throwable t) {
				Debuggable.printStackTrace(t);
				return super.findResource(name);
			}

		} finally {
			PromiscuousClassUtils.pop("findResource", name);
		}
	}

	@Override public URL getResource(String name) {
		if (PromiscuousClassUtils.contains("getResource", name)) {
			return null;
		}
		PromiscuousClassUtils.push("getResource", name);
		try {
			Collection<ClassLoader> cls = getClassLoadersToSearch(true);
			Vector vect = new Vector();
			for (ClassLoader cl : cls) {
				URL result = PromiscuousClassUtils.callProtectedMethodNullOnUncheck(cl, "getResource", name);
				if (result != null)
					addIfNew(vect, result);
			}
			if (vect.size() < 1)
				return null;
			return (URL) vect.elements().nextElement();
		} finally {
			PromiscuousClassUtils.pop("getResource", name);
		}
	}

	@Override public Enumeration getResources(String name) throws IOException {
		Vector vect = new Vector();
		for (ClassLoader cl : getClassLoadersToSearch(true)) {
			Enumeration result = PromiscuousClassUtils.callProtectedMethodNullOnUncheck(cl, "getResources", name);
			if (result != null)
				addAllNew(vect, result);
		}
		return vect.elements();
	}

	@Override public Enumeration<URL> findResources(String name) throws IOException {
		Vector<URL> vect = new Vector<URL>();
		for (ClassLoader cl : getClassLoadersToSearch(true)) {
			Enumeration<URL> result = PromiscuousClassUtils.callProtectedMethodNullOnUncheck(cl, "findResources", name);
			if (result != null)
				addAllNew(vect, result);
		}
		return vect.elements();
	}

	public InputStream getResourceAsStream(String name) {
		for (ClassLoader cl : getClassLoadersToSearch(false)) {
			try {
				InputStream result = cl.getResourceAsStream(name);
				if (result != null) {
					if (result.available() > 0) {
						return result;
					}
				}
			} catch (IOException e) {
			}

		}
		return getParent().getResourceAsStream(name);
	}

	public Class<?> findLoadedClassLocalMethodology(String name) {
		Class pl = PromiscuousClassUtils.findLoadedClassByName(name);
		if (pl != null)
			return pl;
		for (ClassLoader cl : getClassLoadersToSearch(true)) {
			Class<?> result = PromiscuousClassUtils.callProtectedMethodNullOnUncheck(cl, "findLoadedClass", name);
			if (isSomething(result))
				return rememberClass(name, result);
		}
		return rememberClass(name, super.findLoadedClass(name));
	}

	@Override public Class findClassLocalMethodologyActuallyDefines(String name) throws ClassNotFoundException {
		boolean useSystem = (name.startsWith("java.") /*|| name.startsWith("scala.")|| name.startsWith("sun.")*/);
		if (useSystem) {
			try {
				final ClassLoader systemLoader = ClassLoader.getSystemClassLoader();
				Class loadedClass = systemLoader.loadClass(name);
				return loadedClass;
			} catch (Throwable t) {
				Debuggable.printStackTrace(t);
			}
		}
		Throwable cnf;
		for (ClassLoader cl : getClassLoadersToSearch(true)) {
			try {
				Class<?> result = PromiscuousClassUtils.callProtectedMethodNullOnUncheck(cl, "findClass", name);
				if (isSomething(result))
					return rememberClass(name, result);
			} catch (Throwable e) {
				Debuggable.printStackTrace(e);
				continue;
			}
		}
		return rememberClass(name, findClassSuperThruURLS(name));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public boolean addPathStringsForDebug(List<Object> strings, boolean includeParent) {
		boolean changed = false;
		for (ClassLoader url : getClassLoadersToSearch(false)) {
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
	private Collection<ClassLoader> getClassLoadersToSearch(boolean parentOK) {
		Collection<ClassLoader> al;
		synchronized (classLoadersToSearch) {
			al = new ArrayList<ClassLoader>(classLoadersToSearch);
		}
		al.remove(this);
		ClassLoader cl = getParent();
		if (cl != null) {
			al.remove(cl);
			if (parentOK) {
				al.add(cl);
			}
		}
		return al;
	}

}
