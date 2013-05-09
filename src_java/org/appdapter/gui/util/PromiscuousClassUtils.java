package org.appdapter.gui.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;


abstract public class PromiscuousClassUtils  {

    static NamingResolver namingResolver = new ClassLoadingNamingResolver();

    public static ArrayList<Class> classesSeen = new ArrayList<Class>();

    public static Class<?> rememberClass(Class<?> findClass) {
        synchronized (classesSeen) {
            addIfNew(classesSeen, findClass);
        }
        return findClass;
    }

    public static ArrayList<ClassLoader> allClassLoaders = new ArrayList<ClassLoader>();

    public static FromManyClassLoader many;

    public synchronized static FromManyClassLoader getPromiscuousClassLoader() {
        if (many == null) {
            ClassLoader parent = FromManyClassLoader.class
                    .getClassLoader();
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
        ArrayList<Class> foundClasses = new ArrayList<Class>();
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
        // TODO Auto-generated method stub
        boolean wasTrue = Ontologized.class.isAssignableFrom(interfaceClass);
        if (!wasTrue) {
            Debuggable.LOGGER.warning("interfaceClass " + interfaceClass
                    + " is not Ontoligized");
        }
        Assert.assertTrue("All our classes are ontoligized", wasTrue);

    }

    public static boolean isCreateable(Class type) {
        if (type.isInterface())
            return false;
        Constructor[] v = type.getConstructors();
        if (v == null || v.length == 0)
            return false;
        return true;
    }

    public static <T> boolean addIfNew(List<T> list, T element) {
        if (list.contains(element))
            return false;
        if (element instanceof HRKRefinement.DontAdd) {
            return false;
        }
        if (element instanceof HRKRefinement) {
            list.add(0, element);
            return true;
        }
        list.add(element);
        return true;
    }

    public static <T, ET> boolean addAllNew(List<T> list, ET[] elements) {
        boolean changed = false;
        for (Object t0 : elements) {
            T t;
            try {
                t = (T) t0;
            } catch (ClassCastException cce) {
                cce.printStackTrace();
                continue;
            }
            if (addIfNew(list, t))
                changed = true;
        }
        return changed;
    }

    public static <T, ET> boolean addAllNew(List<T> list, Iterable<ET> elements) {
        boolean changed = false;
        for (ET t0 : elements) {
            T t;
            try {
                t = (T) t0;
            } catch (ClassCastException cce) {
                cce.printStackTrace();
                continue;
            }
            if (addIfNew(list, t))
                changed = true;
        }
        return changed;
    }

    private static Collection<Class> getInstalledClasses() {
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

    public static IsolatingClassLoaderBase coerceClassloader(ClassLoader cl2) {
        if (cl2 instanceof IsolatingClassLoaderBase)
            return (IsolatingClassLoaderBase) cl2;
        addClassloader(cl2);
        return new IsolatedClassLoader(cl2);
    }

    public static <T> Class<T> forName(String className)
            throws ClassNotFoundException {
        try {
            return (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            Class c = getPromiscuousClassLoader().findClass(className);
            if (c != null) {
                return c;
            }
            return c;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> Class<T> OneInstC(String p, String c) {
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
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
    static public <T> T callProtectedMethodNullOnUncheck(Object from,
            String methodName, Object... args) {
        try {
            return callProtectedMethod(true, from, methodName, args);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (InvocationTargetException e) {
            Throwable whyBroken = e.getCause();
            Debuggable.UnhandledException(whyBroken);
            throw Debuggable.asRuntimeException(whyBroken);
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
    static public <T> T callProtectedMethod(boolean skipNSM, Object from,
            String methodName, Object... args)
            throws InvocationTargetException, NoSuchMethodException {
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
            throw new NoSuchMethodException(Debuggable.toInfoStringArgV(
                    "NoSuchMethod " + methodName + " on=", from, "args=", args));
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

    static public <T> T createInstance(Class<T> clz)
            throws InstantiationException {
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
