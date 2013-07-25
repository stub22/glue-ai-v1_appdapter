package org.appdapter.core.convert;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.appdapter.core.log.Debuggable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReflectUtils {

	static List<Converter> registeredConverters = new ArrayList<Converter>();
	public static Converter.AggregateConverter DEFAULT_CONVERTER = new Converter.AggregateConverter(registeredConverters);

	public static void registerConverter(Converter utilityConverter) {
		if (utilityConverter == DEFAULT_CONVERTER)
			return;
		synchronized (registeredConverters) {
			registeredConverters.remove(utilityConverter);
			registeredConverters.add(utilityConverter);
		}
	}

	public static List<Converter> getConverters(Object val, Class objClass, Class objNeedsToBe, int... which) {
		List<Converter> cnverters = getRegisteredConverters();
		List<Converter> matched = new ArrayList<Converter>();
		synchronized (cnverters) {
			cnverters = new ArrayList<Converter>(cnverters);
		}
		for (Converter c : cnverters) {
			int r = c.declaresConverts(val, objClass, objNeedsToBe);
			for (int w : which) {
				if (r == w)
					matched.add(c);
			}
		}
		return matched;
	}

	public static List<Converter> getRegisteredConverters() {
		// TODO Auto-generated method stub
		return null;
	}

	public static Logger theLogger = LoggerFactory.getLogger(ReflectUtils.class);

	public static <T> T convertList(List e, Converter converter, Class<T> type) throws NoSuchConversionException, Throwable {
		T result = null;
		if (e == null || e.size() == 0)
			return result;
		int len = e.size();
		if (type.isArray()) {
			Class ctype = type.getComponentType();
			result = (T) Array.newInstance(ctype, e.size());
			for (int i = 0; i < len; i++) {
				Object using = e.get(i);
				Array.set(result, i, converter.recast(using, ctype));
			}
			return result;
		}
		if (!Collection.class.isAssignableFrom(type)) {
			Object using = e.get(0);
			if (len != 1) {
				theLogger.warn("Can only use one result from " + e + " only using " + using);
			}
			return converter.recast(using, type);
		}
		Class<T> concrete = null;
		if (Modifier.isAbstract(type.getModifiers()) || type.isInterface()) {
			for (Class c : new Class[] { ArrayList.class, Stack.class, HashSet.class }) {
				if (type.isAssignableFrom(c)) {
					concrete = c;
				}
			}
			if (concrete == null) {
				throw new NoSuchConversionException(e, type);
			}
		} else {
			concrete = type;
		}
		Collection cresult = (Collection) (result = concrete.newInstance());
		Iterator its = e.listIterator();
		Class<Object> compType = Object.class;
		while (its.hasNext()) {
			cresult.add(converter.recast(its.next(), compType));
		}
		return result;
	}

	public static Method getDeclaredMethod(Class search, String name, Class... parameterTypes) throws SecurityException {
		Method m = getDeclaredMethod(true, search, name, false, false, TypeAssignable.PERFECT, parameterTypes.length, parameterTypes);
		if (m != null)
			return m;
		m = getDeclaredMethod(true, search, name, true, false, TypeAssignable.CASTING_ONLY, parameterTypes.length, parameterTypes);
		if (m == null)
			return null;
		m = getDeclaredMethod(true, search, name, true, true, TypeAssignable.ANY, parameterTypes.length, parameterTypes);
		if (m == null)
			return null;
		return m;
	}

	/*
		public static Method getDeclaredMethod(Class search, String name, boolean laxPTs, Class... parameterTypes) throws SecurityException {
			try {
				getDeclaredMethod(search, name, true, checkOnlyName, useTypeAssignable, paramCount, parameterTypes)
				return search.getMethod(name, parameterTypes);
			} catch (NoSuchMethodException e) {
				if (laxPTs) {
					int ptlen = parameterTypes.length;
					for (Method m : search.getDeclaredMethods()) {
						if (!m.getName().equalsIgnoreCase(name))
							continue;
						Class[] mp = m.getParameterTypes();
						if (mp.length != ptlen)
							continue;
						boolean cant = false;
						for (int i = 0; i < ptlen; i++) {
							if (isDisjoint(mp[i], parameterTypes[i])) {
								cant = true;
								break;
							}
						}
						if (cant)
							continue;
						return m;
					}
				}
				Class nis = search.getSuperclass();
				if (nis != null)
					return getDeclaredMethod(nis, name, laxPTs, parameterTypes);
				return null;
			}
		}
	*/

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

	static public boolean isStatic(Member f) {
		if (f instanceof Constructor) {
			if (Modifier.isStatic(f.getModifiers()))
				return true;
			return true;
		}
		if (Modifier.isStatic(f.getModifiers()))
			return true;
		if (f instanceof Field && f.getDeclaringClass().isInterface())
			return true;
		return false;
	}

	public static Object invoke(Object obj0, Method method, Object... params) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return invoke(obj0, DEFAULT_CONVERTER, method, params);
	}

	public static Object invokeA(Object obj, Method method, Object[] params) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		return invokeA(obj, DEFAULT_CONVERTER, method, params);
	}

	public static Object invoke(Object obj, Converter converter, Method method, Object... params) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {

		Exception why = null;
		try {
			return invokeA(obj, converter, method, params);
		} catch (InvocationTargetException e) {
			why = e;
		} catch (NoSuchMethodException e) {
			why = e;
		}
		why.printStackTrace();
		if (true)
			throw Debuggable.reThrowable(why);
		return why;

	}

	public static Object invokeA(Object obj, Converter converter, Method method, Object[] params) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException {
		return invokeAV(obj, converter, method, params, null);
	}

	public static Object invokeOptional(Object obj, Method method, OptionalArg optionalArgs) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		// TODO Auto-generated method stub
		return invokeAV(obj, DEFAULT_CONVERTER, method, new Object[0], optionalArgs);
	}

	private static Object invokeAV(Object obj, Converter converter, Method method, Object[] params, OptionalArg optionalArg) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException {

		Object obj0 = obj;
		boolean isStatic = isStatic(method);
		Class[] ts = method.getParameterTypes();

		int ml = ts.length;
		if (params == null) {
			params = new Object[0];
		}
		int pl = params.length;

		if (isStatic) {
			ArrayList alparams = new ArrayList();
			if (ml > pl) {
				if (obj0 != null) {
					alparams.add(obj0);
					obj0 = null;
					for (Object p : params) {
						alparams.add(p);
					}
					params = alparams.toArray();
					return invokeAV(null, converter, method, params, optionalArg);
				}
			}

		}

		if (!isStatic && ml < pl && obj0 == null) {
			ArrayList alparams = new ArrayList();
			if (obj0 == null) {
				obj0 = params[0];
			}
			boolean removeOne = true;
			for (Object p : params) {
				if (removeOne) {
					removeOne = false;
					continue;
				}
				alparams.add(p);
			}
			params = alparams.toArray();
			return invokeAV(obj0, converter, method, params, optionalArg);
		}

		Class objNeedsToBe = method.getDeclaringClass();

		if (!isStatic) {
			if (!isProxyMethodClass(objNeedsToBe, method)) {
				Object r = recast(converter, obj0, objNeedsToBe);
				if (r != obj0 && r != null) {
					return invokeAV(r, converter, method, params, optionalArg);
				}
				if (objNeedsToBe != null && !objNeedsToBe.isInstance(obj0)) {
					Class searchMethods = obj0.getClass();
					Method method2 = getDeclaredMethod(searchMethods, method.getName(), ts);
					if (method2 != null && method2 != method) {
						return invokeAV(obj0, converter, method, params, optionalArg);
					}
				}
			}
		}

		if (isStatic)
			obj0 = null;
		else {

		}
		method.setAccessible(true);

		if (ml == 0) {
			return invokeReal(method, obj0);
		}
		boolean isVarArgs = method.isVarArgs();

		// we have one Object[] params
		if (!isVarArgs && params.length == 1 && params[0] instanceof Object[]) {
			if (!ts[0].isArray()) {
				return invokeA(obj0, converter, method, (Object[]) params[0]);
			}
		}

		Object[] nps = new Object[ml];
		int lastParamNum = ts.length;
		if (isVarArgs)
			lastParamNum--;

		int neededArgs = lastParamNum - pl;

		if (neededArgs > 0) {
			if (optionalArg == null) {
				Debuggable.warn("Not enough arguments ! neededArgs = " + neededArgs);
			} else {
				for (int i = 0; i < neededArgs; i++) {
					int workingOn = i + lastParamNum;
					Class pt = ts[workingOn];
					nps[workingOn] = optionalArg.getArg(pt);
				}
			}
		}
		boolean anyChange = false;
		for (int i = 0; i < lastParamNum; i++) {
			Object p = params[i];
			Class pt = ts[i];
			nps[i] = recast(converter, p, pt);
			if (nps[i] != p) {
				anyChange = true;
			}
		}
		if (!isVarArgs) {
			// this is ideal
			if (pl == ml) {
				return invokeReal(method, obj0, nps);
			} else {

			}
		}

		if (isVarArgs) {
			Object lpv = null;
			if (params[lastParamNum].getClass().isArray()) {
				lpv = params[lastParamNum];
			} else {
				Class pt = ts[lastParamNum].getComponentType();
				ArrayList xp = new ArrayList();
				for (int i = lastParamNum; i < pl; i++) {
					Object p = params[i];
					xp.add(recast(converter, p, pt));
				}
				lpv = xp.toArray();
			}
			nps[lastParamNum] = recast(converter, lpv, ts[lastParamNum]);
			return invokeA(obj0, converter, method, nps);
		}
		return invokeReal(method, obj0, nps);

	}

	private static Object invokeReal(Method method, Object obj, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (!isStatic(method)) {
			Class objectMustBe = method.getDeclaringClass();
			if (!objectMustBe.isInstance(obj)) {
				Object o = recast(DEFAULT_CONVERTER, obj, objectMustBe);
				if (o != obj && o != null) {
					if (!objectMustBe.isInstance(o)) {
						throw new IllegalArgumentException(" " + obj.getClass() + ": " + obj + " is not targable from " + method);
					} else {
						obj = o;
					}
				}
			}
		}

		try {
			return method.invoke(obj, (Object[]) args);
		} catch (IllegalAccessException e) {
			Debuggable.warn(e);
			throw e;
		} catch (IllegalArgumentException e) {
			Debuggable.warn(e);
			throw e;
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			Debuggable.warn(e);
			throw e;
		}
	}

	public static Object recast(Converter converter, Object obj0, Class objNeedsToBe) {
		if (objNeedsToBe == null)
			return null;
		if (converter == null)
			return obj0;
		try {
			return converter.recast(obj0, objNeedsToBe);
		} catch (NoSuchConversionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return obj0;
		}
	}

	public static Object recast(Object obj0, Class objNeedsToBe) {
		return recast(DEFAULT_CONVERTER, obj0, objNeedsToBe);
	}

	private static boolean isProxyMethodClass(Class objNeedsToBe, Member optionInfo) {
		if (ProxyMethodClass.class.isAssignableFrom(objNeedsToBe))
			return true;
		if (InvocationHandler.class.isAssignableFrom(objNeedsToBe))
			return true;
		if (Proxy.isProxyClass(objNeedsToBe))
			return true;
		return false;
	}

	public static <T> T newInstance(Class<T> classOf, Class[] types, Object... args) throws Throwable {
		Constructor<T> c = classOf.getDeclaredConstructor((Class[]) types);
		c.setAccessible(true);
		return c.newInstance(args);
	}

	public static void setField(Object val, Class classOf, Class otherwise, String fieldname, Object value) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, Throwable {
		{
			Throwable why = null;
			Field causeF;
			try {
				causeF = classOf.getField(fieldname);
				try {
					causeF.set(val, value);
					return;
				} catch (IllegalArgumentException e) {
					why = e;
				} catch (IllegalAccessException e) {
					why = e;
				}
			} catch (SecurityException e) {
				why = e;
			} catch (NoSuchFieldException e) {
				why = e;
			}
			try {
				causeF = otherwise.getDeclaredField(fieldname);
				causeF.setAccessible(true);
				causeF.set(val, value);
			} catch (SecurityException e) {
				throw e;
			} catch (NoSuchFieldException e) {

				if (why != null)
					throw why;
				throw e;
			}
		}
	}

	public static Object getFieldValue(Object val, Class classOf, Class otherwise, String fieldname) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, Throwable {
		{
			Throwable why = null;
			Field causeF;
			if (classOf != null) {
				try {
					causeF = classOf.getField(fieldname);
					try {
						causeF.setAccessible(true);
						return causeF.get(val);
					} catch (IllegalArgumentException e) {
						why = e;
					} catch (IllegalAccessException e) {
						why = e;
					}
				} catch (SecurityException e) {
					why = e;
				} catch (NoSuchFieldException e) {
					why = e;
					// search Declared area
					if (otherwise == null)
						otherwise = classOf;
				}
			}
			try {
				causeF = otherwise.getDeclaredField(fieldname);
				causeF.setAccessible(true);
				return causeF.get(val);
			} catch (SecurityException e) {
				throw e;
			} catch (NoSuchFieldException e) {
				classOf = otherwise.getSuperclass();
				if (classOf != null) {
					try {
						return getFieldValue(val, null, classOf, fieldname);
					} catch (NoSuchFieldException nsfe) {

					}
				}
				if (why != null)
					throw why;
				throw e;
			}
		}
	}

	public static boolean setObjectPropertyValue(Object object, Class c, String localName, Converter converter, List e, boolean replaceValueNotAddTo, boolean okIfFieldNotFound)
			throws NoSuchConversionException, NoSuchFieldException, SecurityException {

		Class cvtTo = null;
		try {
			Method m = getDeclaredMethod(true, c, "set" + localName, true, true, converter, 1, (Class) null);
			if (m != null) {
				cvtTo = m.getParameterTypes()[0];
				Object value = convertList(e, converter, cvtTo);
				m.setAccessible(true);
				m.invoke(object, value);
				return true;
			}
		} catch (SecurityException e1) {
		} catch (NoSuchMethodException e1) {
		} catch (NoSuchConversionException nsf) {
			//			throw nsf;			
		} catch (Throwable e1) {
		}

		try {
			Field f = getDeclaredField(c, localName);
			cvtTo = f.getType();
			Object value = convertList(e, converter, cvtTo);
			f.setAccessible(true);
			f.set(object, value);
			return true;
		} catch (ClassCastException nsf) {
			throw new NoSuchConversionException(e, cvtTo, nsf);
		} catch (NoSuchConversionException nsf) {
			throw nsf;
		} catch (NoSuchFieldException nsf) {
			if (okIfFieldNotFound)
				return false;
			throw Debuggable.reThrowable(nsf);
		} catch (Throwable t) {
			throw Debuggable.reThrowable(t);
		}
	}

	public static Field getDeclaredField(Class c, String name) throws SecurityException, NoSuchFieldException {
		NoSuchFieldException nsf = null;
		try {
			return c.getField(name);
		} catch (SecurityException e) {
		} catch (NoSuchFieldException e) {
			nsf = e;
		}
		while (c != null) {
			try {
				return c.getDeclaredField(name);
			} catch (SecurityException se) {
				throw se;
			} catch (NoSuchFieldException nsf2) {
				c = c.getSuperclass();
				continue;
			}
		}
		throw nsf;
	}

	public static Method getDeclaredMethod(boolean returnNullIfMissing, Class c, String name, boolean caseInsensitive, boolean checkOnlyName, TypeAssignable useTypeAssignable, int paramCount,
			Class... parameterTypes) {
		NoSuchMethodException nsf = null;
		boolean slowLoop = caseInsensitive || checkOnlyName || useTypeAssignable != null || paramCount != -1;
		try {
			if (slowLoop) {
				for (Method m : c.getMethods()) {
					if (methodMatches(m, name, caseInsensitive, checkOnlyName, useTypeAssignable, paramCount, parameterTypes))
						return m;
				}
			}
			return c.getMethod(name, parameterTypes);
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
			nsf = e;
		}
		while (c != null) {
			try {
				if (slowLoop) {
					for (Method m : c.getDeclaredMethods()) {
						if (methodMatches(m, name, caseInsensitive, checkOnlyName, useTypeAssignable, paramCount, parameterTypes))
							return m;
					}
				}
				return c.getDeclaredMethod(name, parameterTypes);
			} catch (SecurityException se) {
				throw se;
			} catch (NoSuchMethodException nsf2) {
				c = c.getSuperclass();
				continue;
			}
		}
		return null;
	}

	private static boolean methodMatches(Method m, String name, boolean caseInsensitive, boolean checkOnlyName, TypeAssignable useTypeAssignable, int paramCount, Class... parameterTypes) {
		if (name != null) {
			if (caseInsensitive) {
				if (!m.getName().equalsIgnoreCase(name))
					return false;
			} else {
				if (!m.getName().equals(name))
					return false;
			}
		}
		Class[] mp = m.getParameterTypes();
		int mplength = mp.length;
		if (paramCount != -1) {
			if (mplength != paramCount)
				return false;
		}
		if (checkOnlyName)
			return true;
		if (mp.length != parameterTypes.length)
			return false;
		for (int i = 0; i < mp.length; i++) {
			Class must = mp[i];
			Class have = parameterTypes[i];
			if (must == null || have == null)
				continue;
			if (useTypeAssignable.declaresConverts(null, have, must) == TypeAssignable.WONT)
				return false;
		}
		return true;
	}

	public static boolean isAssignableTypes(Class<?> c1, Class<?> c2) {
		if (c1 == void.class || c2 == void.class)
			return false;
		if (c1 == null || c2 == null)
			return true;
		c1 = nonPrimitiveTypeFor(c1);
		c2 = nonPrimitiveTypeFor(c2);
		if (c1.isAssignableFrom(c2) || c2.isAssignableFrom(c1))
			return true;
		return c1.isInterface() && c2.isInterface();
	}

	public static boolean isDisjointTypes(Class<?> c1, Class<?> c2) {
		return !isAssignableTypes(c1, c2);
	}

	public static Collection<Method> getAllMethods(Class clz) {
		List<Method> methods = new ArrayList<Method>();
		while (clz != null) {
			for (Method m : clz.getDeclaredMethods()) {
				methods.add(m);
			}
			clz = clz.getSuperclass();
		}
		return methods;
	}

	public static Collection<Field> getAllFields(Class clz) {
		List<Field> methods = new ArrayList<Field>();
		while (clz != null) {
			for (Field m : clz.getDeclaredFields()) {
				methods.add(m);
			}
			clz = clz.getSuperclass();
		}
		return methods;
	}

	public static boolean isSameType(Class have, Class must) {
		must = ReflectUtils.nonPrimitiveTypeFor(must);
		have = ReflectUtils.nonPrimitiveTypeFor(have);
		return (must == have);
	}

}
