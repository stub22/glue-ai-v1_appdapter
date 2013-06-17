package org.appdapter.gui.util;

import static org.appdapter.gui.util.CollectionSetUtils.addAllNew;
import static org.appdapter.gui.util.CollectionSetUtils.addIfNew;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.List;

import org.appdapter.core.log.Debuggable;

abstract public class IsolatingClassLoaderBase extends URLClassLoader implements HRKRefinement, Ontologized.HRKAdded {

	public IsolatingClassLoaderBase(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	public IsolatingClassLoaderBase() {
		super(new URL[0]);
	}

	public IsolatingClassLoaderBase(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
		super(urls, parent, factory);
		// TODO Auto-generated constructor stub
	}

	@Override final public Class loadClass(String class_name, boolean resolve) throws ClassNotFoundException {
		return PromiscuousClassUtils.rememberClass(class_name, loadClassRemember(class_name, resolve));
	}

	public Class loadClassRemember(String class_name, boolean resolve) throws ClassNotFoundException {
		Class fnd = null;
		ClassNotFoundException cnf = null;
		try {
			fnd = super.loadClass(class_name, false);
		} catch (ClassNotFoundException e) {
			cnf = e;
		}
		try {
			if (fnd == null)
				fnd = loadClassParentNoResolve(class_name);
		} catch (ClassNotFoundException e) {
			if (cnf == null) {
				e.printStackTrace();
				cnf = e;
			}
		}
		if (fnd == null) {
			if (cnf != null)
				throw cnf;
			//throw cnf;
		}
		if (resolve) {
			resolveClass(fnd);
		}
		return fnd;
	}

	public Class loadClassParentNoResolve(String class_name) throws ClassNotFoundException {
		ClassLoader p = getParent();
		try {
			return PromiscuousClassUtils.callProtectedMethod(false, p, "loadClass", class_name, false);
		} catch (InvocationTargetException e1) {
			Throwable ee = e1.getCause();
			if (ee instanceof ClassNotFoundException)
				throw (ClassNotFoundException) ee;
			Debuggable.UnhandledException(ee);
			if (ee instanceof RuntimeException)
				throw (RuntimeException) ee;
			throw new ClassNotFoundException("InvallidTarget: ", ee);
		} catch (NoSuchMethodException ee) {
			throw new ClassNotFoundException("PROGRAMMER ERROR: ", ee);
		}
	}

	@Override final public String toString() {
		ArrayList<Object> strings = new ArrayList<Object>();
		this.addPaths(strings, true);
		final StringBuilder str = new StringBuilder();
		str.append(getClass());
		str.append('[');
		appendURLS(str, ";", strings);
		str.append(']');
		return str.toString();
	}

	public abstract boolean addPaths(List<Object> strings, boolean includeParent);

	/**
	 * Adds the given URLs to the classpath.
	 * 
	 * @param additions
	 *            URLs to add.
	 */
	public void addURLs(final URL[] additions) {
		for (URL url : additions) {
			addURL(url);
		}
	}

	/**
	 * <code>
	 * public void addURL(final URL url) {
	 *   super.addURL_super(url);
	 * }
	 * </code>
	 */
	@Override public abstract void addURL(URL url);

	public final void addURL_super(URL url) {
		super.addURL(url);
	}

	public static <T> void appendURLS(final StringBuilder str, String sep, Iterable<T> urls) {
		boolean first = true;
		for (T u : urls) {
			Object url = u;
			if (url instanceof URL) {
				url = u.toString();
			}
			if (!(url instanceof String))
				continue;
			if (!first) {
				str.append(sep);
			} else {
				first = false;
			}
			str.append(url);
		}
	}

	public static boolean pathsOf(List<Object> strings, ClassLoader cl, boolean includeParent) {
		if (cl == getSystemClassLoader())
			return addIfNew(strings, "$CLASSPATH");
		if (cl instanceof IsolatingClassLoaderBase) {
			return ((IsolatingClassLoaderBase) cl).addPaths(strings, includeParent);
		}
		if (cl instanceof URLClassLoader) {
			boolean changed = addCollection(strings, cl, ((URLClassLoader) cl).getURLs());
			if (includeParent) {
				if (pathsOf(strings, cl.getParent(), includeParent))
					changed = true;
			}
			return changed;
		}
		return addIfNew(strings, cl.toString());
	}

	public static <ET> boolean addCollection(List<Object> strings, ClassLoader thiz, ET[] elems) {
		boolean changed = false;
		if (addAllNew(strings, elems))
			changed = true;
		return changed;
	}
}
