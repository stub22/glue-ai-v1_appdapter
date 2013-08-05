package org.appdapter.core.convert;

import java.awt.Component;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.appdapter.api.trigger.AnyOper.AskIfEqual;
import org.appdapter.api.trigger.AnyOper.DontAdd;
import org.appdapter.api.trigger.AnyOper.HRKRefinement;
import org.appdapter.core.convert.Converter.ConverterMethod;
import org.appdapter.core.log.Debuggable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.util.Pair;

public class ReflectUtils {

	static List<Converter> registeredConverters = new ArrayList<Converter>();
	final public static AggregateConverter DEFAULT_CONVERTER = new AggregateConverter(registeredConverters);

	public static void registerConverter(Converter utilityConverter) {
		if (utilityConverter == DEFAULT_CONVERTER)
			return;
		synchronized (registeredConverters) {
			registeredConverters.remove(utilityConverter);
			registeredConverters.add(utilityConverter);
		}
	}

	public static void registerConverterMethod(final Method m, ConverterMethod cmi) {
		registerConverter(new ConverterFromMember(m, true, cmi));
	}

	static public <T> T noSuchConversion(Object obj, Class<T> objNeedsToBe, Throwable nsc) throws NoSuchConversionException {
		if (nsc instanceof NoSuchConversionException)
			throw (NoSuchConversionException) nsc;
		throw new NoSuchConversionException(obj, objNeedsToBe, nsc);
	}

	static Integer leastOfCvt(int assignableFromW, int assignableFromW2) {
		int max = Math.max(assignableFromW, assignableFromW2);
		if (max == Converter.WILL) {
			return max;
		}
		return max;
	}

	public static List<Converter> getConverters(Object val, Class objClass, Class objNeedsToBe, int... which) {
		List<Converter> cnverters = getRegisteredConverters();
		List<Converter> matched = new ArrayList<Converter>();
		synchronized (cnverters) {
			cnverters = new ArrayList<Converter>(cnverters);
		}
		for (Converter c : cnverters) {
			int r = c.declaresConverts(val, objClass, objNeedsToBe, c.MCVT);
			for (int w : which) {
				if (r == w)
					matched.add(c);
			}
		}
		return matched;
	}

	public static <E> List<E> copyOfU(Collection<E> copyIt) {
		synchronized (copyIt) {
			return Collections.unmodifiableList(new ArrayList(copyIt));
		}
	}

	public static <E> List<E> copyOf(Collection<E> copyIt) {
		synchronized (copyIt) {
			return new ArrayList<E>(copyIt);
		}
	}

	public static List<Converter> getRegisteredConverters() {
		return copyOf(registeredConverters);
	}

	public static Logger theLogger = LoggerFactory.getLogger(ReflectUtils.class);

	public static <T> T convertList(List e, Converter converter, Class<T> type) throws NoSuchConversionException, Throwable {
		int maxCvt = converter.MCVT;
		T result = null;
		if (e == null || e.size() == 0)
			return result;
		int len = e.size();
		if (type.isArray()) {
			Class ctype = type.getComponentType();
			result = (T) Array.newInstance(ctype, e.size());
			for (int i = 0; i < len; i++) {
				Object using = e.get(i);
				Array.set(result, i, converter.convert(using, ctype, maxCvt));
			}
			return result;
		}
		if (!Collection.class.isAssignableFrom(type)) {
			Object using = e.get(0);
			if (len != 1) {
				theLogger.warn("Can only use one result from " + e + " only using " + using);
			}
			return converter.convert(using, type, maxCvt);
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
			cresult.add(converter.convert(its.next(), compType, maxCvt));
		}
		return result;
	}

	public static Method getDeclaredMethod(Class search, String name, Class... parameterTypes) throws SecurityException {
		Method m = getDeclaredMethod(search, name, false, false, TypeAssignable.PERFECT, parameterTypes.length, ANY_PublicPrivatePackageProtected, parameterTypes);
		if (m != null)
			return m;
		m = getDeclaredMethod(search, name, true, false, TypeAssignable.CASTING_ONLY, parameterTypes.length, ANY_PublicPrivatePackageProtected, parameterTypes);
		if (m == null)
			return null;
		m = getDeclaredMethod(search, name, true, true, TypeAssignable.ANY, parameterTypes.length, ANY_PublicPrivatePackageProtected, parameterTypes);
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
		Debuggable.printStackTrace(why);
		if (true)
			throw Debuggable.reThrowable(why);
		return why;

	}

	public static Object invokeA(Object obj, Converter converter, Method method, Object[] params) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException {
		return invokeAV(obj, converter, method, params, OptionalArg.NONE);
	}

	public static Object invokeOptional(Object obj, Method method, OptionalArg optionalArgs) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		// TODO Auto-generated method stub
		return invokeAV(obj, DEFAULT_CONVERTER, method, new Object[0], optionalArgs);
	}

	public static Object invokeConstructorOptional(Converter converter, OptionalArg optionalArgs, Class clz, final Object... args) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, InstantiationException {
		Constructor[] cons = clz.getDeclaredConstructors();
		if (cons == null || cons.length == 0) {
			return clz.newInstance();
		}
		int l = cons.length;

		if (converter == null)
			converter = DEFAULT_CONVERTER;

		final OptionalArg optionalArgZ = new AggregateOptionalArgs(asList(new OptionalArgFromCollectionAndConvertor(args, converter, false), optionalArgs));

		if (l == 1) {
			return invokeAVConstructors(converter, cons[0], args, optionalArgZ);
		}

		List<Pair<Constructor, Object[]>> constructors = new ArrayList<Pair<Constructor, Object[]>>();

		for (Constructor con : cons) {
			try {
				Object[] c = makeParams_canThrow(converter, getAllParameters(con), con.isVarArgs(), args, optionalArgZ);
				if (c != null)
					constructors.add(new Pair<Constructor, Object[]>(con, c));
			} catch (Throwable t) {

			}
		}
		Collections.sort(constructors, new Comparator<Pair<Constructor, Object[]>>() {
			@Override public int compare(Pair<Constructor, Object[]> o1, Pair<Constructor, Object[]> o2) {
				float f1 = evaluateAO(o1.getRight(), getAllParameters(o1.getLeft()));
				float f2 = evaluateAO(o2.getRight(), getAllParameters(o2.getLeft()));
				return f1 > f2 ? -1 : (f1 == f2 ? 0 : 1);
			}

		});
		for (Pair<Constructor, Object[]> p : constructors) {
			return invokeRealConstructor(p.getLeft(), p.getRight());
		}
		return null;

	}

	static float evaluateAO(Object[] value, Class[] allParameters) {
		int g = 0;
		int b = 0;
		int good = allParameters.length;
		for (int i = 0; i < good; i++) {
			Class c0 = allParameters[i];
			Object v = value[i];
			if (c0.isInstance(v)) {
				g++;
				if (!c0.isInterface()) {
					g++;
				}
			} else {
				b++;
			}
		}
		if (b > 0)
			return -b;
		return g;
	}

	private static Class[] getAllParameters(Constructor con) {
		return con.getParameterTypes();
	}

	static Object[] makeParams(Converter converter, Class[] mts, boolean isVarArgs, Object[] params, OptionalArg optionalArg) {
		try {
			return makeParams_canThrow(converter, mts, isVarArgs, params, optionalArg);
		} catch (Throwable t) {
			return new Object[mts.length];
		}
	}

	static Object[] makeParams_canThrow(Converter converter, Class[] mts, boolean isVarArgs, Object[] params, OptionalArg optionalArg) {
		optionalArg.reset();
		int ml = mts.length;
		if (params == null) {
			params = new Object[0];
		}
		int pl = params.length;

		if (ml == 0) {
			return new Object[0];
		}

		// we have one Object[] params
		if (!isVarArgs && params.length == 1 && params[0] instanceof Object[]) {
			if (!mts[0].isArray()) {
				return makeParams_canThrow(converter, mts, isVarArgs, (Object[]) params[0], optionalArg);
			}
		}

		Object[] mps = new Object[ml];
		int lastParamNum = mts.length - 1;
		int neededArgs = mts.length;
		if (isVarArgs) {
			neededArgs--;
		}

		int filledIn = 0;
		int failures = 0;
		if (neededArgs > pl) {
			if (optionalArg == null) {
				Debuggable.warn("Not enough arguments ! neededArgs = " + neededArgs);
				failures = neededArgs;
				return mps;
			}
			for (int i = 0; i < ml; i++) {
				Class pt = mts[i];
				try {
					Object b = optionalArg.getArg(pt);
					Object p = recast(converter, b, pt);
					if (pt.isInstance(p)) {
						mps[i] = p;
						filledIn++;
					} else {
						failures++;
					}
				} catch (Throwable tt) {
					Debuggable.expectedToIgnore(tt, NoSuchConversionException.class);
					failures++;
				}

			}
		} else {
			boolean anyChange = false;
			for (int i = 0; i < neededArgs; i++) {
				Object b = params[i];
				Class pt = mts[i];
				try {
					Object p = recast(converter, b, pt);
					if (pt.isInstance(p)) {
						mps[i] = p;
						filledIn++;
					} else {
						failures++;
					}
				} catch (Throwable tt) {
					Debuggable.expectedToIgnore(tt, NoSuchConversionException.class);
					failures++;
				}
			}
		}

		if (!isVarArgs) {
			// this is ideal
			if (pl == ml) {
				return mps;
			} else {
				return mps;
			}
		}

		if (isVarArgs) {
			Object lpv = null;
			if (params[lastParamNum].getClass().isArray()) {
				lpv = params[lastParamNum];
			} else {
				Class pt = mts[lastParamNum].getComponentType();
				ArrayList xp = new ArrayList();
				for (int i = lastParamNum; i < pl; i++) {
					Object p = params[i];
					Object o = recastOrNull(converter, p, pt);
					if (o == null)
						return mps;
					xp.add(o);
				}
				lpv = xp.toArray();
			}
			mps[lastParamNum] = recastOrNull(converter, lpv, mts[lastParamNum]);
			return makeParams_canThrow(converter, mts, isVarArgs, mps, optionalArg);
		}
		return mps;

	}

	private static Object recastOrNull(Converter converter, Object p, Class pt) {
		try {
			return recast(converter, p, pt);
		} catch (Throwable e) {
			Debuggable.expectedToIgnore(e, NoSuchConversionException.class);
			return null;
		}
	}

	private static Object invokeAVConstructors(Converter converter, Constructor method, Object[] params, OptionalArg optionalArg) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, InstantiationException {

		optionalArg.reset();
		boolean isStatic = isStatic(method);
		Class[] mts = method.getParameterTypes();

		int ml = mts.length;
		if (params == null) {
			params = new Object[0];
		}
		int pl = params.length;
		method.setAccessible(true);

		if (ml == 0) {
			return invokeRealConstructor(method);
		}
		boolean isVarArgs = method.isVarArgs();

		// we have one Object[] params
		if (!isVarArgs && params.length == 1 && params[0] instanceof Object[]) {
			if (!mts[0].isArray()) {
				return invokeAVConstructors(converter, method, (Object[]) params[0], optionalArg);
			}
		}

		Object[] mps = new Object[ml];
		int lastParamNum = mts.length - 1;

		int neededArgs = ml - pl;
		if (isVarArgs)
			neededArgs--;

		if (neededArgs > 0) {
			if (optionalArg == null) {
				Debuggable.warn("Not enough arguments ! neededArgs = " + neededArgs);
			} else {
				for (int i = 0; i < neededArgs; i++) {
					int workingOn = i + lastParamNum;
					Class pt = mts[workingOn];
					mps[workingOn] = optionalArg.getArg(pt);
				}
			}
		}
		boolean anyChange = false;
		for (int i = 0; i < lastParamNum; i++) {
			Object p = params[i];
			Class pt = mts[i];
			mps[i] = recast(converter, p, pt);
			if (mps[i] != p) {
				anyChange = true;
			}
		}
		if (!isVarArgs) {
			// this is ideal
			if (pl == ml) {
				return invokeRealConstructor(method, mps);
			} else {

			}
		}

		if (isVarArgs) {
			Object lpv = null;
			if (params[lastParamNum].getClass().isArray()) {
				lpv = params[lastParamNum];
			} else {
				Class pt = mts[lastParamNum].getComponentType();
				ArrayList xp = new ArrayList();
				for (int i = lastParamNum; i < pl; i++) {
					Object p = params[i];
					Object o = recastOrNull(converter, p, pt);
					if (o == null)
						return noSuchConversion(p, pt, null);
					xp.add(o);
				}
				lpv = xp.toArray();
			}
			mps[lastParamNum] = recastOrNull(converter, lpv, mts[lastParamNum]);
			return invokeAVConstructors(converter, method, mps, optionalArg);
		}

		return invokeRealConstructor(method, mps);

	}

	private static Object invokeAV(Object obj, Converter converter, Method method, Object[] params, OptionalArg optionalArg) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException {

		optionalArg.reset();

		Object obj0 = obj;
		boolean isStatic = isStatic(method);
		Class[] mts = method.getParameterTypes();

		int ml = mts.length;
		if (params == null) {
			params = new Object[0];
		}
		int pl = params.length;

		if (isStatic) {
			if (ml > pl && obj0 != null) {
				params = insertToArray(params, 0, obj);
				return invokeAV(null, converter, method, params, optionalArg);
			}
		}

		if (!isStatic && ml < pl && obj0 == null) {
			if (obj0 == null) {
				obj0 = params[0];
			}
			params = removeElement(params, 0);
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
					Method method2 = getDeclaredMethod(searchMethods, method.getName(), mts);
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
			if (!mts[0].isArray()) {
				return invokeA(obj0, converter, method, (Object[]) params[0]);
			}
		}

		Object[] mps = null;

		int lastNonVarArg = ml - 1;
		if (isVarArgs) {
			lastNonVarArg--;
		}

		int missingArgs = lastNonVarArg - pl + 1;

		if (missingArgs > 0) {
			mps = makeParams_canThrow(converter, mts, isVarArgs, params, optionalArg);
		}
		if (mps == null) {
			mps = new Object[ml];
		}
		boolean anyChange = false;
		for (int i = 0; i <= lastNonVarArg; i++) {
			Class pt = mts[i];
			Object was = mps[i];
			if (pt.isInstance(was))
				continue;
			Object p = params[i];
			mps[i] = recast(converter, p, pt);
			if (mps[i] != p) {
				anyChange = true;
			}
		}

		if (!isVarArgs) {
			int nullCount = countOfNulls(mps);
			// this is ideal
			if (nullCount == 0) {
				return invokeReal(method, obj0, mps);
			} else {

			}
		}

		if (isVarArgs) {
			int lastParamNum = ml - 1;
			Object lpv = null;
			if (params[lastParamNum].getClass().isArray()) {
				lpv = params[lastParamNum];
			} else {
				Class pt = mts[lastParamNum].getComponentType();
				ArrayList xp = new ArrayList();
				for (int i = lastParamNum; i < pl; i++) {
					Object p = params[i];
					Object o = recastOrNull(converter, p, pt);
					if (o == null)
						return mps;
					xp.add(o);
				}
				lpv = xp.toArray();
			}
			mps[lastParamNum] = recastOrNull(converter, lpv, mts[lastParamNum]);
			return invokeA(obj0, converter, method, mps);
		}
		return invokeReal(method, obj0, mps);

	}

	public static int countOfNulls(Object... mps) {
		int nullz = 0;
		for (Object n : mps) {
			if (n == null)
				nullz++;
		}
		return nullz;
	}

	public static int mismatchCount(Class[] mts, Object... mps) {
		int nullz = 0;
		int mpl = mps.length;
		for (int i = 0; i < mts.length; i++) {
			Class ts = mts[i];
			if (i < mpl) {
				Object mp = mps[i];
				if (!ts.isInstance(mp)) {
					nullz++;
				}
			} else {
				//nullz++;
				break;
			}

		}
		return nullz;
	}

	public static <T> T[] insertToArray(T[] elementData, int pos, T e) {
		int oldLen = elementData.length;
		T[] elementData0 = Arrays.copyOf(elementData, oldLen + 1);
		System.arraycopy(elementData0, pos, elementData0, pos + 1, oldLen - pos);
		elementData0[pos] = e;
		return elementData0;
	}

	public static <T> T[] removeElement(T[] elementData, int pos) {
		int oldLen = elementData.length;
		int newLen = oldLen - 1;
		T[] elementData0 = Arrays.copyOf(elementData, oldLen);
		System.arraycopy(elementData, pos + 1, elementData0, pos, newLen - pos);
		T[] elementData1 = Arrays.copyOf(elementData0, newLen);
		return elementData1;
	}

	private static Object invokeRealConstructor(Constructor method, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		try {
			return method.newInstance((Object[]) args);
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

		Object neededLock = getLock(obj);

		try {
			if (neededLock != null) {
				synchronized (neededLock) {
					return method.invoke(obj, (Object[]) args);
				}
			}
			return method.invoke(obj, (Object[]) args);

		} catch (IllegalAccessException e) {
			Debuggable.warn(e);
			throw e;
		} catch (IllegalArgumentException e) {
			Debuggable.warn(e);
			throw e;
		} catch (InvocationTargetException e) {
			Debuggable.warn(e);
			throw e;
		} finally {
			if (neededLock != null) {

			}
		}
	}

	private static Object getLock(Object obj) {
		if (obj == null)
			return null;
		if (obj instanceof Component) {
			return ((Component) obj).getTreeLock();
		}
		if (obj instanceof Model) {
			// this is incorrect but is an exmple
			return ((Model) obj).getLock();
		}
		return null;
	}

	public static Object recast(Object obj, Class objNeedsToBe, int maxCvt) throws NoSuchConversionException {
		return recast(DEFAULT_CONVERTER, obj, objNeedsToBe, maxCvt);
	}

	private static Object recast(Converter converter, Object p, Class pt) throws NoSuchConversionException {
		int maxCvt = converter.MCVT;
		return recast(converter, p, pt, maxCvt);
	}

	public static <T> T recast(Converter converter, Object obj0, Class<T> objNeedsToBe, int maxCvt) throws NoSuchConversionException {
		if (objNeedsToBe == null)
			return null;
		if (converter == null)
			return (T) obj0;
		try {
			return converter.convert(obj0, objNeedsToBe, maxCvt);
		} catch (NoSuchConversionException e) {
			Debuggable.printStackTrace(e);
			return (T) obj0;
		}
	}

	public static <T> T recast(Object obj0, Class<T> objNeedsToBe) throws NoSuchConversionException {
		return recast(DEFAULT_CONVERTER, obj0, objNeedsToBe, Converter.MCVT);
	}

	public static Object recastOrNull(Object obj0, Class objNeedsToBe, Object otherwise) {
		try {
			return recast(DEFAULT_CONVERTER, obj0, objNeedsToBe, Converter.MCVT);
		} catch (NoSuchConversionException e) {
			Debuggable.printStackTrace(e);
			return otherwise;
		}
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
		Field field = findField(val, classOf, otherwise, fieldname);
		Object val0;
		if (isStatic(field))
			val0 = null;
		else
			val0 = recast(val, field.getDeclaringClass());
		Object recast = recast(value, field.getType());
		setFieldValue(field, val0, recast);
		return;
	}

	public static void setFieldValue(Field field, Object obj, Object recast) throws IllegalArgumentException, IllegalAccessException {
		field.setAccessible(true);
		Object before = field.get(obj);
		boolean isFinal = Modifier.isFinal(field.getModifiers());
		Class wrapper = field.getType();
		if (wrapper.isPrimitive()) {
			if (before.equals(recast))
				return;
			else if (wrapper == Boolean.TYPE)
				field.setBoolean(obj, (Boolean) recast);
			else if (wrapper == Byte.TYPE)
				field.setByte(obj, (Byte) recast);
			else if (wrapper == Character.TYPE)
				field.setChar(obj, (Character) recast);
			else if (wrapper == Short.TYPE)
				field.setShort(obj, (Short) recast);
			else if (wrapper == Integer.TYPE)
				field.setInt(obj, (Integer) recast);
			else if (wrapper == Long.TYPE)
				field.setLong(obj, (Long) recast);
			else if (wrapper == Float.TYPE)
				field.setFloat(obj, (Float) recast);
			else if (wrapper == Double.TYPE)
				field.setDouble(obj, (Double) recast);
			else {
				field.set(obj, recast);
			}
			return;
		}
		if (before == recast)
			return;
		field.set(obj, recast);
	}

	public static Object getFieldValue(Object val, Class classOf, Class otherwise, String fieldname) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, Throwable {
		Field field = findField(val, classOf, otherwise, fieldname);
		Object val0;
		field.setAccessible(true);
		if (isStatic(field))
			val0 = null;
		else {
			if (val == null)
				return null;
			val0 = recastOrNull(val, field.getDeclaringClass(), null);
			if (val0 == null)
				return null;
		}
		return field.get(val0);

	}

	private static Field findField(Object val, Class classOf, Class otherwise, String fieldname) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Collection<Field> fields = findFields(val, classOf, otherwise, false, null, false, fieldname);
		if (fields.size() > 0)
			return fields.iterator().next();

		throw new NoSuchFieldException(classOf + " " + fieldname);
	}

	public static Collection<Field> findFields(Object val, Class classOf, Class otherwise, boolean caseInsensitive, Class mustBe, boolean endsWith, String fieldname) throws NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {

		NoSuchFieldException why = null;
		SecurityException se = null;
		IllegalArgumentException iae = null;
		boolean needLoop = endsWith || fieldname == null || mustBe != null;
		Field field;
		if (!needLoop && classOf != null) {
			try {
				field = classOf.getField(fieldname);
				try {
					return Collections.singleton(field);
				} catch (IllegalArgumentException e) {
					iae = e;
				}
			} catch (SecurityException e) {
				se = e;
			} catch (NoSuchFieldException e) {
				why = e;
				// search Declared area
				if (otherwise == null)
					otherwise = classOf;
			}
		}
		List<Field> fields = null;
		while (otherwise != null) {
			try {
				for (Field f : otherwise.getDeclaredFields()) {
					if (!methodMatches(f.getName(), fieldname, caseInsensitive, endsWith)) {
						continue;
					}

					if (fields == null)
						fields = new ArrayList<Field>();
					fields.add(f);
				}
				return fields;
			} catch (SecurityException e) {
				se = e;
				why = null;
			}
		}
		if (iae != null)
			throw iae;
		if (why != null)
			throw why;
		throw se;

	}

	public static boolean setObjectPropertyValue(Object object, Class c, String localName, Converter converter, List e, boolean replaceValueNotAddTo, boolean okIfFieldNotFound)
			throws NoSuchConversionException, NoSuchFieldException, SecurityException {

		Class cvtTo = null;
		try {
			Method m = getDeclaredMethod(c, "set" + localName, true, true, converter, 1, PUBLIC_ONLY, (Class) null);
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
			setFieldValue(f, object, value);
			return true;
		} catch (ClassCastException nsf) {
			throw new NoSuchConversionException(e, cvtTo, nsf);
		} catch (NoSuchConversionException nsf) {
			throw nsf;
		} catch (NoSuchFieldException nsf) {
			if (okIfFieldNotFound)
				return false;
			throw nsf;
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

	//PublicPrivatePackageProtected
	public static int NONE = 0x0000;
	public static int PUBLIC_ONLY = 0x1000;
	public static int ANY_PublicPrivatePackageProtected = 0x1111;

	public static Method getDeclaredMethod(Class c, String name, boolean caseInsensitive, boolean checkOnlyName, int paramCount) throws SecurityException {
		return getDeclaredMethod(c, name, caseInsensitive, checkOnlyName, TypeAssignable.ANY, paramCount, ANY_PublicPrivatePackageProtected);
	}

	public static Method getDeclaredMethod(Class c, String name, boolean caseInsensitive, boolean checkOnlyName, TypeAssignable useTypeAssignable, int paramCount, long level, Class... parameterTypes)
			throws SecurityException {
		boolean slowLoop = name == null || level != PUBLIC_ONLY || caseInsensitive || checkOnlyName || useTypeAssignable != null || paramCount != -1;
		SecurityException se = null;
		if (level == PUBLIC_ONLY) {
			try {
				if (slowLoop) {
					for (Method m : c.getMethods()) {
						if (methodMatches(m, name, caseInsensitive, checkOnlyName, useTypeAssignable, paramCount, parameterTypes))
							return m;
					}
				} else {
					return c.getMethod(name, parameterTypes);
				}
			} catch (SecurityException e) {
				se = e;
			} catch (NoSuchMethodException e) {
			}
		}
		List<Method> methods = getDeclaredMethods(c, name, caseInsensitive, checkOnlyName, useTypeAssignable, 1, paramCount, level, parameterTypes);
		if (methods != null && methods.size() > 0) {
			return methods.get(0);
		}
		{
			///methods = getDeclaredMethods(c, name, caseInsensitive, checkOnlyName, useTypeAssignable, 1, paramCount, level, parameterTypes);
		}

		return null;
	}

	public static List<Method> getDeclaredMethods(Class c, String name, boolean caseInsensitive, boolean checkOnlyName, TypeAssignable useTypeAssignable, int maxNum, int paramCount, long level,
			Class... parameterTypes) throws SecurityException {
		List<Method> methods = new ArrayList<Method>();
		getDeclaredMethods(methods, c, name, caseInsensitive, checkOnlyName, useTypeAssignable, maxNum, paramCount, level, parameterTypes);
		return methods;
	}

	private static void getDeclaredMethods(Collection<Method> methods, Class c, String name, boolean caseInsensitive, boolean checkOnlyName, TypeAssignable useTypeAssignable, int maxNum,
			int paramCount, long level, Class... parameterTypes) throws SecurityException {
		boolean slowLoop = name == null || caseInsensitive || checkOnlyName || useTypeAssignable != null || paramCount != -1;
		while (c != null) {
			try {
				if (slowLoop) {
					for (Method m : c.getDeclaredMethods()) {
						if (name != null && !methodMatches(m.getName(), name, caseInsensitive, false))
							continue;
						if (!checkOnlyName && !methodMatches(m, name, caseInsensitive, checkOnlyName, useTypeAssignable, paramCount, parameterTypes))
							continue;
						if (!protectionLevelIncludes(m.getModifiers(), level))
							continue;
						addIfNew(methods, m);
					}
				} else {
					addIfNew(methods, c.getDeclaredMethod(name, parameterTypes));
				}
			} catch (SecurityException e) {
			} catch (NoSuchMethodException nsf2) {
			}
			c = c.getSuperclass();
			continue;
		}
	}

	private static boolean protectionLevelIncludes(int modifiers, long level) {
		boolean isPublic = Modifier.isPublic(modifiers);
		if (isPublic && hasBits(level, 0x1000))
			return true;
		boolean isPrivate = Modifier.isPrivate(modifiers);
		if (isPrivate && hasBits(level, 0x0100))
			return true;
		boolean isProtected = Modifier.isProtected(modifiers);

		if (isProtected && hasBits(level, 0x0010))
			return true;

		boolean isPackage = !isPublic && !isPrivate && !isProtected;

		if (isPackage && hasBits(level, 0x0001))
			return true;

		return false;
	}

	private static boolean hasBits(long level, int i) {
		if ((level & i) == i)
			return true;
		return false;
	}

	public static Constructor getDeclaredConstructor(Class c, TypeAssignable useTypeAssignable, int paramCount, Class... parameterTypes) throws SecurityException {
		boolean slowLoop = useTypeAssignable != null || paramCount != -1;
		SecurityException se = null;
		try {
			if (slowLoop) {
				for (Constructor m : c.getConstructors()) {
					if (methodMatches(m.getParameterTypes(), useTypeAssignable, paramCount, parameterTypes))
						return m;
				}
			} else {
				return c.getConstructor((Class[]) parameterTypes);
			}
		} catch (SecurityException e) {
			se = e;
		} catch (NoSuchMethodException e) {
		}
		try {
			if (slowLoop) {
				for (Constructor m : c.getDeclaredConstructors()) {
					if (methodMatches(m.getParameterTypes(), useTypeAssignable, paramCount, parameterTypes))
						return m;
				}
				return null;
			}
			return c.getDeclaredConstructor(parameterTypes);
		} catch (SecurityException e) {
		} catch (NoSuchMethodException nsf2) {
		}
		if (se != null)
			throw se;
		return null;
	}

	private static boolean methodMatches(Method m, String name, boolean caseInsensitive, boolean checkOnlyName, TypeAssignable useTypeAssignable, int paramCount, Class... parameterTypes) {
		if (name != null) {
			if (!methodMatches(m.getName(), name, caseInsensitive, false))
				return false;
		}
		return checkOnlyName || methodMatches(m.getParameterTypes(), useTypeAssignable, paramCount, parameterTypes);
	}

	private static boolean methodMatches(String m, String name, boolean caseInsensitive, boolean endsWith) {
		if (name != null) {

			if (endsWith) {
				if (caseInsensitive) {
					if (!m.equalsIgnoreCase(name))
						return false;
				} else {
					if (!m.equals(name))
						return false;
				}
			} else {
				if (caseInsensitive) {
					m = m.toLowerCase();
					name = name.toLowerCase();
				}
				return m.endsWith(name);
			}
		}

		return true;
	}

	private static boolean methodMatches(Class[] mp, TypeAssignable useTypeAssignable, int paramCount, Class... parameterTypes) {

		int mplength = mp.length;
		if (paramCount != -1) {
			if (mplength != paramCount)
				return false;
			else
				return true;
		}

		if (mp.length != parameterTypes.length)
			return false;

		for (int i = 0; i < mp.length; i++) {
			Class must = mp[i];
			Class have = parameterTypes[i];
			if (must == null || have == null)
				continue;
			if (useTypeAssignable.declaresConverts(null, have, must, TypeAssignable.MCVT) == TypeAssignable.WONT)
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

	public static Class[] getParameterTypes(Member rm) {
		if (rm instanceof Method) {
			return ((Method) rm).getParameterTypes();
		}
		if (rm instanceof Constructor) {
			return ((Constructor) rm).getParameterTypes();
		}
		if (rm instanceof Field) {
			return new Class[] { ((Field) rm).getType() };
		}
		return new Class[] { rm.getDeclaringClass() };
	}

	public static Class getReturnType(Member rm) {
		if (rm instanceof Method) {
			return ((Method) rm).getReturnType();
		}
		if (rm instanceof Constructor) {
			return ((Constructor) rm).getDeclaringClass();
		}
		if (rm instanceof Field) {
			return ((Field) rm).getType();
		}
		return rm.getDeclaringClass();
	}

	public static boolean isOverride(Method m) {
		return isOverride(m, m.getDeclaringClass());
	}

	public static boolean isOverride(Method m, Class dc) {
		if (dc.isInterface()) {
			Debuggable.warn("isOverride to " + dc);
			return false;
		}
		if (dc == null)
			return false;
		Class sclz = dc.getSuperclass();
		if (sclz == null)
			return false;
		try {
			if (sclz.getMethod(m.getName(), (Class[]) m.getParameterTypes()) != null) {
				return true;
			}
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}
		return false;
	}

	public static boolean isCreatable(Class utilClass) {
		if (utilClass.isArray())
			return false;
		int mods = utilClass.getModifiers();
		if (Modifier.isAbstract(mods))
			return false;
		return !utilClass.isInterface();
	}

	public static Class findLoadedClass(String fqcn) {
		try {
			return Class.forName(fqcn, false, Thread.currentThread().getContextClassLoader());
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}

	public static <T> T getFieldValueOr(String fqcn, String fieldname, T defaultV) {
		Class c = ReflectUtils.findLoadedClass(fqcn);
		if (c == null)
			return defaultV;
		Object v;
		try {
			v = ReflectUtils.getFieldValue(null, c, c, fieldname);
			if (v != null)
				return (T) v;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return defaultV;
	}

	public static boolean convertsTo(Object anyObject, Class objClassMaybe, Class objNeedsToBe) {
		if (anyObject != null) {
			if (objNeedsToBe == null)
				return true;
			if (objNeedsToBe.isPrimitive()) {
				objNeedsToBe = nonPrimitiveTypeFor(objNeedsToBe);
			}
			if (objNeedsToBe.isInstance(anyObject))
				return true;
		}
		if (objClassMaybe != null && objNeedsToBe != null && objNeedsToBe.isAssignableFrom(objClassMaybe)) {
			return true;
		}
		return getConverters(anyObject, objClassMaybe, objNeedsToBe, Converter.WILL).size() > 0;
	}

	public static Class getTypeClass(Object typeVariable, Collection exceptFor) {
		if (typeVariable == null || exceptFor.contains(typeVariable)) {
			return null;
		}
		exceptFor.add(typeVariable);
		if (typeVariable instanceof Class)
			return (Class) typeVariable;
		Class c = typeVariable.getClass();
		if (c.isArray()) {
			int len = Array.getLength(typeVariable);
			for (int i = 0; i < len; i++) {
				c = getTypeClass(Array.get(typeVariable, i), exceptFor);
				if (c != null)
					return c;
			}
			return null;
		}
		if (typeVariable instanceof GenericArrayType) {
			GenericArrayType pt = (GenericArrayType) typeVariable;
			c = getTypeClass(pt.getGenericComponentType(), exceptFor);
			if (c != null)
				return Array.newInstance(c, 0).getClass();
		}
		if (typeVariable instanceof GenericDeclaration) {
			GenericDeclaration pt = (GenericDeclaration) typeVariable;
			c = getTypeClass(pt.getTypeParameters(), exceptFor);
			if (c != null)
				return c;
		}
		if (typeVariable instanceof WildcardType) {
			WildcardType pt = (WildcardType) typeVariable;
			for (Type t : pt.getUpperBounds()) {
				c = getTypeClass(t, exceptFor);
				if (c != null)
					return c;
			}
			for (Type t : pt.getLowerBounds()) {
				c = getTypeClass(t, exceptFor);
				if (c != null)
					return c;
			}
		}
		if (typeVariable instanceof TypeVariable) {
			TypeVariable pt = (TypeVariable) typeVariable;
			for (Type t : pt.getBounds()) {
				if (t != typeVariable) {
					c = getTypeClass(t, exceptFor);
					if (c != null)
						return c;
				}
			}
			c = getTypeClass(pt.getGenericDeclaration(), exceptFor);
			if (c != null)
				return c;
		}
		if (typeVariable instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) typeVariable;
			c = getTypeClass(pt.getRawType(), exceptFor);
			if (c != null)
				return c;
			for (Type t : pt.getActualTypeArguments()) {
				c = getTypeClass(t, exceptFor);
				if (c != null)
					return c;
			}
			c = getTypeClass(pt.getOwnerType(), exceptFor);
			if (c != null)
				return c;
		}
		return null;
	}

	public static String getCanonicalSimpleName(Class clz) {
		return getCanonicalSimpleName(clz, false);
	}

	public static String getCanonicalSimpleName(Class clz, boolean includePackaging) {
		if (clz == null)
			return "<?>";
		if (clz.isArray()) {
			return getCanonicalSimpleName(clz.getComponentType(), includePackaging) + "[]";
		}
		String c = clz.getSimpleName();
		if (c == null || c.length() == 0) {
			c = clz.getCanonicalName();
			if (c != null & !includePackaging) {
				c = c.substring(c.lastIndexOf('.') + 1);
			}
		}
		if (c == null || c.length() == 0) {
			c = clz.getName();
			if (!includePackaging)
				c = c.substring(c.lastIndexOf('.') + 1);
		}
		if (clz.isAnonymousClass()) {
			Class clz1 = clz.getSuperclass();
			if (clz1 == null || clz1 == Object.class) {
				Class[] cs = clz.getInterfaces();
				if (cs != null && cs.length > 0) {
					return getCanonicalSimpleName(cs[0], false) + "-" + c;
				}
			}
			return getCanonicalSimpleName(clz1, false) + "-" + c;
		}
		return c;
	}

	public static Object getFieldValue(Object obj, Field f) throws IllegalArgumentException, IllegalAccessException {
		if (isStatic(f))
			return f.get(null);
		try {
			return f.get(recast(obj, f.getDeclaringClass()));
		} catch (NoSuchConversionException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public interface TAccepts<S> {

		boolean isCompleteOn(S e);

		boolean resultOf(S e);

	}

	public static <T> boolean addToList(Collection<T> list, T element) {
		if (element instanceof DontAdd) {
			return false;
		}
		if (element instanceof HRKRefinement) {
			if ((list instanceof List)) {
				((List<T>) list).add(0, element);
				return true;
			}
		}
		return list.add(element);
	}

	public static <T> boolean addIfNew(Collection<T> list, T element) {
		return addIfNew(list, element, true);
	}

	public static <T> boolean addIfNewSkipNull(Collection<T> list, T element) {
		return addIfNew(list, element, false);
	}

	public static <T> boolean addIfNew(Collection list, Iterable elements, boolean nullOK) {
		boolean changed = false;
		if (elements == null)
			return false;
		for (Object t : elements) {
			if (addIfNew(list, t, nullOK))
				changed = true;
		}
		return changed;
	}

	public static <T> boolean addIfNew(Collection<T> list, T element, boolean nullOK) {
		if (!nullOK && element == null)
			return false;
		if (element instanceof AskIfEqual) {
			AskIfEqual aie = (AskIfEqual) element;
			for (Object e : list) {
				if (aie.same(e))
					return false;
			}
		} else {
			if (list.contains(element))
				return false;
		}
		return addToList(list, element);
	}

	public static <T, ET> boolean addAllNew(Collection<T> list, ET[] elements) {
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

	public static <T, ET> boolean addAllNew(Collection<T> list, Enumeration<ET> elements) {
		boolean changed = false;

		while (elements.hasMoreElements()) {
			ET t0 = elements.nextElement();
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

	public static <T, ET> boolean addAllNew(Collection<T> list, Iterable<ET> elements) {
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

	public static <T> boolean containsOne(T[] elements, TAccepts<T> e) {
		for (Object t0 : elements) {
			T t;
			try {
				t = (T) t0;
			} catch (ClassCastException cce) {
				cce.printStackTrace();
				continue;
			}
			if (e.isCompleteOn(t))
				return e.resultOf(t);
		}
		return false;
	}

	public static <T> T[] arrayOf(T... args) {
		return args;
	}

	public static <T> List<T> asList(T... args) {
		return Arrays.asList(args);
	}

	public static String join(String sep, String... args) {
		return join(sep, 0, -1, args);
	}

	public static String join(String sep, int start, int len, String... args) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		int argslength = args.length;
		for (int i = start; i < argslength; i++) {
			if (len == 0)
				break;
			len--;
			String item = args[i];
			if (first)
				first = false;
			else
				sb.append(sep);
			sb.append(item);
		}
		return sb.toString();
	}

	public static <T> Iterable<T> iterableOf(T... args) {
		return Arrays.asList((T[]) args);
	}

	public static <T> T first(Object... args) {
		for (Object o : args) {
			if (o == null)
				continue;
			try {
				T t = (T) o;
				return t;
			} catch (ClassCastException cce) {

			}

		}
		return null;
	}

	public static boolean isAssignableFrom(Class objNeedsToBe, Class from) {
		if (objNeedsToBe.isAssignableFrom(from))
			return true;
		if (DEFAULT_CONVERTER.declaresConverts(null, from, objNeedsToBe, Converter.MCVT) == Converter.WILL) {
			return true;
		}
		return false;
	}

	public static int isAssignableFromW(Class objNeedsToBe, Class from) {
		if (objNeedsToBe.isAssignableFrom(from))
			return Converter.WILL;
		return DEFAULT_CONVERTER.declaresConverts(null, from, objNeedsToBe, Converter.MCVT);
	}

	static Map<Class<? extends Annotation>, Map<AnnotatedElement, ? extends Annotation>> cachedAnonationsByAC = new HashMap<Class<? extends Annotation>, Map<AnnotatedElement, ? extends Annotation>>();

	public static <A extends Annotation> A getAnnotationOn(AnnotatedElement m, Class<A> annotationClass) {
		Map<AnnotatedElement, A> cachedAnonations;
		synchronized (cachedAnonationsByAC) {
			cachedAnonations = (Map<AnnotatedElement, A>) cachedAnonationsByAC.get(annotationClass);
			if (cachedAnonations == null) {
				cachedAnonations = new HashMap<AnnotatedElement, A>();
				cachedAnonationsByAC.put(annotationClass, cachedAnonations);
			}
		}

		synchronized (cachedAnonations) {
			if (cachedAnonations.containsKey(m)) {
				return (A) cachedAnonations.get(m);
			}
			A on = getAnnotationOn0(m, annotationClass);
			cachedAnonations.put(m, on);
			return (A) on;
		}
	}

	public static <A extends Annotation> A getAnnotationOn0(AnnotatedElement m, Class<A> annotationClass) {
		A on = m.getAnnotation(annotationClass);
		if (on != null)
			return on;
		Class dec = null;
		if (on == null) {
			if (m instanceof Member) {
				Member memb = (Member) m;
				dec = memb.getDeclaringClass();
				if (m instanceof Method) {
					Method method = (Method) m;
					try {
						Class sc = dec.getSuperclass();
						if (sc != null && sc != dec) {
							method = sc.getMethod(((Member) m).getName(), method.getParameterTypes());
							on = getAnnotationOn(method, annotationClass);
							if (on != null)
								return on;
						}
					} catch (NoSuchMethodException nsm) {

					}

				}
			}
			if (m instanceof Class) {
				dec = (Class) m;
			}
			if (dec != null) {
				on = (A) dec.getAnnotation(annotationClass);
				if (on == null && dec.isAnonymousClass()) {
					Class dec2 = dec.getSuperclass();
					if (dec2 != null && dec != dec2) {
						on = getAnnotationOn(dec2, annotationClass);
					}
					if (on == null) {
						dec2 = dec.getDeclaringClass();
						if (dec2 != null && dec != dec2) {
							on = getAnnotationOn(dec2, annotationClass);
						}
					}
				}
			}
		}
		return on;
	}

	public static boolean implementsAllClasses(Class clz, Class... classes) {
		for (Class c : classes) {
			if (!isAssignableFrom(c, clz))
				return false;
		}
		return true;
	}
}
