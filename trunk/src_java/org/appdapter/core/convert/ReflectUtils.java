package org.appdapter.core.convert;

import static org.appdapter.core.log.Debuggable.expectedToIgnore;
import static org.appdapter.core.log.Debuggable.printStackTrace;
import static org.appdapter.core.log.Debuggable.reThrowable;
import static org.appdapter.core.log.Debuggable.warn;

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
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.appdapter.api.trigger.AnyOper.AskIfEqual;
import org.appdapter.api.trigger.AnyOper.DontAdd;
import org.appdapter.api.trigger.AnyOper.HRKRefinement;
import org.appdapter.api.trigger.AnyOper.UIHidden;
import org.appdapter.api.trigger.AnyOper.UISalient;
import org.appdapter.api.trigger.AnyOper.UtilClass;
import org.appdapter.core.convert.Converter.ConverterMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.google.inject.Inject;
//import com.google.inject.TypeLiteral;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.util.Pair;

@UIHidden
abstract public class ReflectUtils implements UtilClass {

	final static List<Converter> registeredConverters = new ArrayList<Converter>();
	final public static AggregateConverter DEFAULT_CONVERTER = new AggregateConverter(registeredConverters);
	final public static Converter NO_CONVERTER = Converter.CASTING_ONLY;
	private static final Class[] CLASS0 = new Class[0];

	public static void registerConverter(Converter utilityConverter) {
		if (utilityConverter == DEFAULT_CONVERTER)
			return;
		synchronized (registeredConverters) {
			registeredConverters.remove(utilityConverter);
			registeredConverters.add(0, utilityConverter);
		}
	}

	@ConverterMethod//
	public static Object[] collectionToArray(Collection col) {
		return col.toArray();
	}

	@ConverterMethod//
	public static List arrayToList(Object[] col) {
		return new ArrayList(Arrays.asList(col));
	}

	@ConverterMethod//
	public static List colToList(Collection col) {
		if (col instanceof List)
			return (List) col;
		return new ArrayList(col);
	}

	@ConverterMethod//
	public static Set colToSet(Collection col) {
		if (col instanceof Set)
			return (Set) col;
		return new HashSet(col);
	}

	static public <T> T convertTo(Class<T> c, Iterable objs) throws ClassCastException {
		for (Object o : objs) {
			if (o == null) {
				continue;
			}
			if (!c.isInstance(o)) {
				continue;
			}
			try {
				return c.cast(o);
			} catch (Exception e) {
				theLogger.error("JVM Issue (canConvert)", e);
				return (T) o;
			}
		}
		return null;
	}

	static public <T> boolean canConvert(Class<T> c, Iterable objs) {
		try {
			for (Object o : objs) {
				if (o == null) {
					continue;
				}
				if (c != null && !c.isInstance(o)) {
					continue;
				}
				return true;
			}
			for (Object o : objs) {
				if (o == null) {
					continue;
				}
				if (o instanceof Convertable) {
					Convertable oc = (Convertable) o;
					if (oc.canConvert(c))
						return true;
				}
				if (c != null) {
					if (!isAssignableFrom(c, o.getClass()))
						continue;
				}
				return true;
			}
		} catch (Throwable t) {
			theLogger.error("JVM Issue (canConvert)", t);
			return false;
		}
		return false;
	}

	public static void registerConverterMethod(final Method m, ConverterMethod cmi) {
		registerConverter(new ConverterFromMember(m, true, cmi));
	}

	static public <T> T noSuchConversion(Object obj, Class<T> objNeedsToBe, Throwable nsc) throws NoSuchConversionException {
		if (nsc instanceof NoSuchConversionException)
			throw noSuchConversionException(obj, objNeedsToBe, nsc);
		if (nsc instanceof ClassCastException)
			throw noSuchConversionException(obj, objNeedsToBe, nsc);

		throw noSuchConversionException(obj, objNeedsToBe, nsc);
	}

	static public NoSuchConversionException noSuchConversionException(Object obj, Class objNeedsToBe, Throwable nsc) {
		if (nsc instanceof NoSuchConversionException)
			return (NoSuchConversionException) nsc;
		if (nsc instanceof ClassCastException)
			return new NoSuchConversionException(obj, objNeedsToBe, nsc);

		return new NoSuchConversionException(obj, objNeedsToBe, nsc);
	}

	static Integer leastOfCvt(int assignableFromW, int assignableFromW2) {
		int max = Math.max(assignableFromW, assignableFromW2);
		if (max == Converter.WILL) {
			return max;
		}
		return max;
	}

	public static Object convertUsingReflection(Object title, Class type) throws NoSuchConversionException {
		NoSuchConversionException cce = null;
		type = nonPrimitiveTypeFor(type);
		Class keyClass = title.getClass();
		if (type == keyClass)
			return title;

		for (Class searchType : new Class[] { type, keyClass }) {
			while (searchType != null) {
				for (Method m : searchType.getDeclaredMethods()) {
					if (type.isAssignableFrom(nonPrimitiveTypeFor(m.getReturnType()))) {
						Class[] pt = m.getParameterTypes();
						if (pt != null && pt.length == 1 && Modifier.isStatic(m.getModifiers())) {
							try {
								if (TypeAssignable.CASTING_ONLY.declaresConverts(title, keyClass, pt[0], null) == TypeAssignable.WONT)
									continue;
								m.setAccessible(true);
								Object o = recastRU(m.invoke(null, title), type);
								if (type.isInstance(o)) {
									registerConverterMethod(m, null);
									return o;
								}
							} catch (Throwable e) {
								cce = new NoSuchConversionException(type + " " + m.getName() + " " + title, e);
							}
						}
					}
				}
				searchType = searchType.getSuperclass();
			}
		}
		return noSuchConversion(title, type, cce);
	}

	public static List<Converter> getConverters(Object val, Class objClass, Class objNeedsToBe, int... which) {
		List<Converter> cnverters = getRegisteredConverters();
		List<Converter> matched = new ArrayList<Converter>();
		synchronized (cnverters) {
			cnverters = new ArrayList<Converter>(cnverters);
		}
		for (Converter c : cnverters) {
			int r = c.declaresConverts(val, objClass, objNeedsToBe, AggregateConverter.newMcvt());
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
		T result = null;
		if (e == null || e.size() == 0)
			return result;
		int len = e.size();
		if (type.isArray()) {
			Class ctype = type.getComponentType();
			result = (T) Array.newInstance(ctype, e.size());
			for (int i = 0; i < len; i++) {
				Object using = e.get(i);
				Array.set(result, i, converter.convert(using, ctype, AggregateConverter.newMcvt()));
			}
			return result;
		}
		if (!Collection.class.isAssignableFrom(type)) {
			Object using = e.get(0);
			if (len != 1) {
				theLogger.warn("Can only use one result from " + e + " only using " + using);
			}
			return converter.convert(using, type, AggregateConverter.newMcvt());
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
			cresult.add(converter.convert(its.next(), compType, AggregateConverter.newMcvt()));
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

	public static Class nonPrimitiveTypeFor(Class prim) {
		Class wrapper = primitives.get(prim);
		if (wrapper != null)
			return wrapper;
		return prim;
	}

	public static boolean isPrimitiveBox(Class prim) {
		return primitives.containsValue(prim);
	}

	public static Map<Class, Class> primitives = new HashMap<Class, Class>(10);

	static {
		primitives.put(Boolean.TYPE, Boolean.class);
		primitives.put(Byte.TYPE, Byte.class);
		primitives.put(Character.TYPE, Character.class);
		primitives.put(Double.TYPE, Double.class);
		primitives.put(Float.TYPE, Float.class);
		primitives.put(Integer.TYPE, Integer.class);
		primitives.put(Long.TYPE, Long.class);
		primitives.put(Short.TYPE, Short.class);
		primitives.put(Void.TYPE, Void.class);
	}

	public static Class wrapperTypeFor(Class cls) {
		if (cls.isPrimitive())
			cls = nonPrimitiveTypeFor(cls);
		return cls;
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
		printStackTrace(why);
		if (true)
			throw reThrowable(why);
		return why;

	}

	public static Object invokeA(Object obj, Converter converter, Method method, Object[] params) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException {
		return invokeAV(obj, converter, method, params, OptionalArg.NONE);
	}

	public static Object invokeOptional(Object obj, Method method, OptionalArg optionalArgs, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException {
		// TODO Auto-generated method stub
		return invokeAV(obj, DEFAULT_CONVERTER, method, args, optionalArgs);
	}

	public static Object invokeConstructorOptional(Converter converter, OptionalArg optionalArgs, Class cls, final Object... args) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, InstantiationException {
		Constructor[] cons = cls.getDeclaredConstructors();
		if (cons == null || cons.length == 0) {
			return cls.newInstance();
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
				warn("Not enough arguments ! neededArgs = " + neededArgs);
				failures = neededArgs;
				return mps;
			}
			for (int i = 0; i < ml; i++) {
				Class pt = mts[i];
				try {
					Object b = optionalArg.getArg(pt);
					Object p = recastRU3(converter, b, pt);
					if (pt.isInstance(p)) {
						mps[i] = p;
						filledIn++;
					} else {
						failures++;
					}
				} catch (Throwable tt) {
					expectedToIgnore(tt, NoSuchConversionException.class);
					failures++;
				}

			}
		} else {
			boolean anyChange = false;
			for (int i = 0; i < neededArgs; i++) {
				Object b = params[i];
				Class pt = mts[i];
				try {
					Object p = recastRU3(converter, b, pt);
					if (pt.isInstance(p)) {
						mps[i] = p;
						filledIn++;
					} else {
						failures++;
					}
				} catch (Throwable tt) {
					expectedToIgnore(tt, NoSuchConversionException.class);
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
					Object o = recastOrNull3(converter, p, pt);
					if (o == null)
						return mps;
					xp.add(o);
				}
				lpv = xp.toArray();
			}
			mps[lastParamNum] = recastOrNull3(converter, lpv, mts[lastParamNum]);
			return makeParams_canThrow(converter, mts, isVarArgs, mps, optionalArg);
		}
		return mps;

	}

	private static Object recastOrNull3(Converter converter, Object p, Class pt) {
		try {
			return recastRU3(converter, p, pt);
		} catch (Throwable e) {
			expectedToIgnore(e, NoSuchConversionException.class);
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
				warn("Not enough arguments ! neededArgs = " + neededArgs);
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
			mps[i] = recastRU3(converter, p, pt);
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
					Object o = recastOrNull3(converter, p, pt);
					if (o == null)
						return noSuchConversion(p, pt, null);
					xp.add(o);
				}
				lpv = xp.toArray();
			}
			mps[lastParamNum] = recastOrNull3(converter, lpv, mts[lastParamNum]);
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
				Object r = null;
				try {
					r = recastRU3(converter, obj0, objNeedsToBe);
				} catch (Throwable t) {
				}
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
			mps[i] = recastRU3(converter, p, pt);
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
					Object o = recastOrNull3(converter, p, pt);
					if (o == null)
						return mps;
					xp.add(o);
				}
				lpv = xp.toArray();
			}
			mps[lastParamNum] = recastOrNull3(converter, lpv, mts[lastParamNum]);
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
			warn(e);
			throw e;
		} catch (IllegalArgumentException e) {
			warn(e);
			throw e;
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			warn(e);
			throw e;
		}
	}

	private static Object invokeReal(Method method, Object obj, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (!isStatic(method)) {
			Class objectMustBe = method.getDeclaringClass();
			if (!objectMustBe.isInstance(obj)) {
				Object o = recastRU(obj, objectMustBe);
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
		Throwable e1 = null;
		InvocationTargetException orig = null;
		try {
			if (neededLock != null) {
				synchronized (neededLock) {
					return method.invoke(obj, (Object[]) args);
				}
			}
			return method.invoke(obj, (Object[]) args);

		} catch (IllegalAccessException e) {
			warn(e);
			throw e;
		} catch (IllegalArgumentException e) {
			warn(e);
			throw e;
		} catch (InvocationTargetException e) {
			orig = e;
			e1 = e.getCause();
		} catch (Throwable e) {
			orig = new InvocationTargetException(e);
			e1 = e;
		} finally {
			if (neededLock != null) {

			}
		}
		if (e1 instanceof RuntimeException)
			throw (RuntimeException) e1;
		if (e1 instanceof Error)
			throw (Error) e1;
		throw orig;
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

	public static Object recastRU(Object obj, Class objNeedsToBe, List maxConverts) throws NoSuchConversionException {
		return recast4(DEFAULT_CONVERTER, obj, objNeedsToBe, maxConverts);
	}

	private static Object recastRU3(Converter converter, Object p, Class pt) throws NoSuchConversionException {
		List maxConverts = AggregateConverter.getMcvt();
		List was = new ArrayList(maxConverts);
		try {
			return recast4(converter, p, pt, maxConverts);
		} finally {
			AggregateConverter.setMcvt(was);
		}
	}

	private static <T> T recast4(Converter converter, Object obj0, Class<T> objNeedsToBe, List maxConverts) throws NoSuchConversionException {
		if (objNeedsToBe == null)
			return null;
		if (converter == null)
			return (T) obj0;
		try {
			return converter.convert(obj0, objNeedsToBe, maxConverts);
		} catch (NoSuchConversionException e) {
			return noSuchConversion(obj0, objNeedsToBe, e);
		} catch (ClassCastException e) {
			return noSuchConversion(obj0, objNeedsToBe, e);
		} catch (Throwable e) {
			printStackTrace(e);
			return noSuchConversion(obj0, objNeedsToBe, e);
		}
	}

	public static <T> T recastRU(Object obj0, Class<T> objNeedsToBe) throws NoSuchConversionException {
		List maxConverts = AggregateConverter.getMcvt();
		List was = new ArrayList(maxConverts);
		try {
			return recast4(DEFAULT_CONVERTER, obj0, objNeedsToBe, maxConverts);
		} finally {
			AggregateConverter.setMcvt(was);
		}
	}

	public static Object recastOrOtherwise(Object obj0, Class objNeedsToBe, Object otherwise) {
		List maxConverts = AggregateConverter.getMcvt();
		List was = new ArrayList(maxConverts);
		try {
			try {
				return recast4(DEFAULT_CONVERTER, obj0, objNeedsToBe, maxConverts);
			} catch (NoSuchConversionException e) {
				//printStackTrace(e);
				return otherwise;
			}
		} finally {
			AggregateConverter.setMcvt(was);
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
			val0 = recastRU(val, field.getDeclaringClass());
		Object recast = recastRU(value, field.getType());
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
			val0 = recastRU(val, field.getDeclaringClass());
			if (val0 == null)
				return null;
		}
		return field.get(val0);

	}

	public static Field findField(Object val, Class classOf, Class otherwise, String fieldname) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
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
					if (fieldname != null && !methodMatches(f.getName(), fieldname, caseInsensitive, endsWith)) {
						continue;
					}
					if (mustBe != null) {
						if (!mustBe.isAssignableFrom(f.getType()))
							continue;
					}
					if (fields == null)
						fields = new ArrayList<Field>();
					fields.add(f);
				}
			} catch (SecurityException e) {
				se = e;
				why = null;
			}
			otherwise = otherwise.getSuperclass();
		}
		for (Class ow : classOf.getInterfaces()) {
			try {
				for (Field f : ow.getDeclaredFields()) {
					if (fieldname != null && !methodMatches(f.getName(), fieldname, caseInsensitive, endsWith)) {
						continue;
					}
					if (mustBe != null) {
						if (!mustBe.isAssignableFrom(f.getType()))
							continue;
					}
					if (fields == null)
						fields = new ArrayList<Field>();
					fields.add(f);
				}
			} catch (SecurityException e) {
				se = e;
				why = null;
			}
		}
		if (fields != null)
			return fields;

		if (iae != null)
			throw iae;
		if (why != null)
			throw why;

		if (se != null)
			throw se;
		return new ArrayList<Field>();

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
			throw noSuchConversionException(e, cvtTo, nsf);
		} catch (NoSuchConversionException nsf) {
			throw noSuchConversionException(e, cvtTo, nsf);
		} catch (NoSuchFieldException nsf) {
			if (okIfFieldNotFound)
				return false;
			throw nsf;
		} catch (Throwable t) {
			throw reThrowable(t);
		}
	}

	public static Object getObjectPropertyValue(Object object, Class c, String localName, Converter converter, boolean okIfFieldNotFound) throws NoSuchConversionException, NoSuchFieldException,
			SecurityException {

		Class cvtTo = null;
		try {
			Method m = getDeclaredMethod(c, localName, true, true, converter, 0, ANY_PublicPrivatePackageProtected);
			if (m == null)
				m = getDeclaredMethod(c, "get" + localName, true, true, converter, 0, PUBLIC_ONLY);
			if (m == null)
				m = getDeclaredMethod(c, "is" + localName, true, true, converter, 0, PUBLIC_ONLY);
			if (m != null) {
				m.setAccessible(true);
				return invokeReal(m, object);
			}
		} catch (Throwable e1) {
			e1.printStackTrace();
		}

		try {
			Field f = getDeclaredField(c, localName);
			cvtTo = f.getType();
			return getFieldValue(object, f);
		} catch (NoSuchFieldException nsf) {
			if (okIfFieldNotFound)
				return false;
			throw nsf;
		} catch (Throwable t) {
			throw reThrowable(t);
		}
	}

	public static boolean setObjectPropertyValue(Object object, Class c, String localName, Converter converter, boolean okIfFieldNotFound, Object value) throws NoSuchConversionException,
			NoSuchFieldException, SecurityException {

		Class cvtTo = null;
		try {
			Method m = getDeclaredMethod(c, localName, true, true, converter, 1, ANY_PublicPrivatePackageProtected);
			if (m == null)
				m = getDeclaredMethod(c, "set" + localName, true, true, converter, 1, PUBLIC_ONLY);
			if (m == null)
				m = getDeclaredMethod(c, "is" + localName, true, true, converter, 1, PUBLIC_ONLY);
			if (m != null) {
				cvtTo = m.getParameterTypes()[0];
				m.setAccessible(true);
				m.invoke(object, value);
				return true;
			}
		} catch (SecurityException e1) {
		} catch (NoSuchConversionException nsf) {
			//			throw nsf;			
		} catch (Throwable e1) {
		}

		try {
			Field f = getDeclaredField(c, localName);
			cvtTo = f.getType();
			setFieldValue(f, object, value);
			return true;
		} catch (NoSuchFieldException nsf) {
			if (okIfFieldNotFound)
				return false;
			throw nsf;
		} catch (Throwable t) {
			throw reThrowable(t);
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
		Pattern p = makePattern(name);
		while (c != null) {
			try {
				for (Field f : c.getDeclaredFields()) {
					if (matchesName(p, f.getName()))
						return f;
				}
			} catch (SecurityException se) {
				throw se;
			} catch (Exception nsf2) {
			}
			c = c.getSuperclass();
			continue;
		}
		throw nsf;
	}

	private static boolean matchesName(Pattern p, String name) {
		Matcher m = p.matcher(name);
		return m.matches();
	}

	public static boolean matchesName(String regex, String name) {
		return matchesName(makePattern(regex), name);
	}

	private static Pattern makePattern(String regex) {
		if (regex.toUpperCase().equals(regex))
			return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		return Pattern.compile(regex);
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
		final Class ci = c;
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
		for (Class cc : ci.getInterfaces()) {
			try {
				if (slowLoop) {
					for (Method m : cc.getDeclaredMethods()) {
						if (name != null && !methodMatches(m.getName(), name, caseInsensitive, false))
							continue;
						if (!checkOnlyName && !methodMatches(m, name, caseInsensitive, checkOnlyName, useTypeAssignable, paramCount, parameterTypes))
							continue;
						if (!protectionLevelIncludes(m.getModifiers(), level))
							continue;
						addIfNew(methods, m);
					}
				} else {
					addIfNew(methods, cc.getDeclaredMethod(name, parameterTypes));
				}
			} catch (SecurityException e) {
			} catch (NoSuchMethodException nsf2) {
			}
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

			if (matchesName(name, m))
				return true;

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

	private static boolean methodMatches(Class[] mp, TypeAssignable typeAssign, int paramCount, Class... parameterTypes) {

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
			if (typeAssign.declaresConverts(null, have, must, AggregateConverter.newMcvt()) == TypeAssignable.WONT)
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

	public static Collection<Method> getAllMethods(Class cls) {
		return getAllMethods(cls, false);
	}

	public static Collection<Method> getAllMethods(Class cls, boolean preferSuperclass) {
		final Class gi = cls;
		Map<String, Method> methods = new HashMap<String, Method>();
		while (cls != null) {
			addMethods(cls, preferSuperclass, methods);
			cls = cls.getSuperclass();
		}
		if (preferSuperclass) {
			for (Class c : gi.getInterfaces()) {
				addMethods(c, true, methods);
			}
		}
		return methods.values();
	}

	private static void addMethods(Class cls, boolean preferSuperclass, Map<String, Method> methods) {
		for (Method m : cls.getDeclaredMethods()) {
			String key = methodString(m);
			if (preferSuperclass || !methods.containsKey(key)) {
				if (isSynthetic(m)) {
					continue;
				}
				methods.put(key, m);
			}
		}
	}

	public static Method getInterfaceMethod(Class cls, Method from) {
		String name = from.getName();
		Class[] params = from.getParameterTypes();
		Method sc = getSuperClassiestMethod(cls, name, params);
		if (sc != null && sc.getDeclaringClass().isInterface())
			return sc;
		for (Class ifc : cls.getInterfaces()) {
			Method m = getSuperClassiestMethod(ifc, name, params);
			if (m != null && m.getDeclaringClass().isInterface())
				return m;
		}
		return sc;
	}

	public static Method getSuperClassiestMethod(Class cls, String name, Class[] params) {
		Method lastm = null;
		while (cls != null) {
			Method m;
			try {
				m = cls.getDeclaredMethod(name, params);
				// ikvm can return null
				if (m != null) {
					//Class dc = m.getDeclaringClass();
					//	if (dc.isInterface())
					lastm = m;
				}
			} catch (SecurityException e) {
			} catch (NoSuchMethodException e) {
			}
			cls = cls.getSuperclass();
		}
		return lastm;
	}

	public static String methodString(Method m) {
		StringBuffer sb = new StringBuffer();
		if (isStatic(m))
			sb.append("static ");
		sb.append(m.getName() + "(");
		Class[] params = m.getParameterTypes(); // avoid clone
		int pl = params.length;

		for (int j = 0; j < pl; j++) {
			if (j > 0)
				sb.append(",");
			sb.append(params[j].getName());
		}
		sb.append(")");
		return sb.toString();
	}

	public static Collection<Field> getAllFields(Class cls) {
		List<Field> methods = new ArrayList<Field>();
		while (cls != null) {
			for (Field m : cls.getDeclaredFields()) {
				methods.add(m);
			}
			cls = cls.getSuperclass();
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
			warn("isOverride to " + dc);
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

	public static Class getTypeClass(Object typeVariable, Class rawType) {
		return getTypeClass(typeVariable, rawType, new ArrayList() {
			{
				add(Object.class);
			}
		});
	}

	public static Class getTypeClass(Type typeVariable, int argNum) {
		if (typeVariable instanceof ParameterizedType) {
			if (argNum == 0)
				return getTypeClass(((ParameterizedType) typeVariable).getRawType(), 0);
			return getTypeClass(((ParameterizedType) typeVariable).getActualTypeArguments()[argNum - 1], 0);
		}
		if (argNum == 0)
			return (Class) typeVariable;
		return getTypeClass(typeVariable, typeVariable, new ArrayList() {
			{
				add(Object.class);
			}
		});
	}

	public static Class getTypeClass(Object typeVariable, Type rawType, Collection exceptFor) {
		if (typeVariable == null || exceptFor.contains(typeVariable)) {
			return null;
		}
		exceptFor.add(typeVariable);
		Class c = null;
		if (typeVariable instanceof Class) {
			Class c1 = null;
			c = (Class) typeVariable;
			if (isGeneric(c)) {
				c1 = getTypeClass(c.getTypeParameters(), rawType, exceptFor);
				if (c1 != null)
					return c1;
			}
			c1 = getTypeClass(c.getGenericSuperclass(), rawType, exceptFor);
			if (c1 != null)
				return c1;
			return c;
		}
		c = typeVariable.getClass();
		if (c.isArray()) {
			int len = Array.getLength(typeVariable);
			for (int i = 0; i < len; i++) {
				c = getTypeClass(Array.get(typeVariable, i), rawType, exceptFor);
				if (c != null)
					return c;
			}
			return null;
		}
		if (typeVariable instanceof GenericArrayType) {
			GenericArrayType pt = (GenericArrayType) typeVariable;
			c = getTypeClass(pt.getGenericComponentType(), rawType, exceptFor);
			if (c != null)
				return Array.newInstance(c, 0).getClass();
		}
		if (typeVariable instanceof GenericDeclaration) {
			GenericDeclaration pt = (GenericDeclaration) typeVariable;
			c = getTypeClass(pt.getTypeParameters(), rawType, exceptFor);
			if (c != null)
				return c;
		}
		if (typeVariable instanceof WildcardType) {
			WildcardType pt = (WildcardType) typeVariable;
			for (Type t : pt.getUpperBounds()) {
				c = getTypeClass(t, rawType, exceptFor);
				if (c != null)
					return c;
			}
			for (Type t : pt.getLowerBounds()) {
				c = getTypeClass(t, rawType, exceptFor);
				if (c != null)
					return c;
			}
		}
		if (typeVariable instanceof TypeVariable) {
			TypeVariable pt = (TypeVariable) typeVariable;
			for (Type t : pt.getBounds()) {
				if (t != typeVariable) {
					c = getTypeClass(t, rawType, exceptFor);
					if (c != null)
						return c;
				}
			}
			c = getTypeClass(pt.getGenericDeclaration(), rawType, exceptFor);
			if (c != null)
				return c;
		}
		if (typeVariable instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) typeVariable;
			if (rawType != null && pt.getRawType() != rawType) {
				return null;
			}
			for (Type t : pt.getActualTypeArguments()) {
				c = getTypeClass(t, rawType, exceptFor);
				if (c != null)
					return c;
			}
			c = getTypeClass(pt.getOwnerType(), rawType, exceptFor);
			if (c != null)
				return c;
		}
		return null;
	}

	///@Inject 
	public static ParameterizedType makeParameterizedType(final Class raw, final Type... typeArguments) {
		if (false) {
			//bind(new TypeLiteral<Dao<Foo>>(){}).to(GenericDAO.class);
			//return new com.google.inject.util.Modules.newParameterizedType(raw, typeArguments);
		}
		return new ParameterizedType() {
			@Override public boolean equals(Object obj) {
				if (!(obj instanceof Type))
					return false;
				return equalTypes(this, (Type) obj);
			}

			@Override public Type getRawType() {
				return raw;
			}

			@Override public Type getOwnerType() {
				return null;
			}

			@Override public Type[] getActualTypeArguments() {
				return typeArguments;
			}
		};
	}

	private static boolean equalTypes(Type[] a, Type[] b) {
		if (a == b)
			return true;
		if (a == null || b == null)
			return true;
		if (a.length != b.length)
			return false;
		int al = a.length;
		for (int i = 0; i < al; i++) {
			if (!equalTypes(a[i], b[i]))
				return false;
		}
		return true;
	}

	public static boolean equalTypes(Type a, Type b) {
		if (a.getClass() != b.getClass()) {
			if (a instanceof ParameterizedType) {
				if (b instanceof ParameterizedType) {
					if (!equalTypes(((ParameterizedType) a).getRawType(), ((ParameterizedType) b).getRawType()))
						return false;
					return equalTypes(((ParameterizedType) a).getActualTypeArguments(), ((ParameterizedType) b).getActualTypeArguments());
				} else {
					return equalTypes(b, a);
				}
			} else {
				if (b instanceof ParameterizedType) {
					return equalTypes(a, ((ParameterizedType) b).getRawType());
				} else {
					//
				}
			}
		}
		if (a.getClass() == b.getClass())
			return a == b;
		return false;
	}

	/**
	 * Perform an unchecked cast based on a type parameter.
	 * 
	 * @param <T> The type to which the object should be cast.
	 * @param o   The object.
	 * @return    The object, cast to the given type.
	 */
	@SuppressWarnings("unchecked") public static <T> T uncheckedCast(Object o) {
		return (T) o;
	}

	public static interface TypeArgumentDelegator {
		public Map<String, Type> getTypeArguments(Class<?> genericType);
	}

	public static <T> Type getTypeArgument(Class<T> genericType, String typeParameterName, T obj) {
		Map<String, Type> typeArguments = getTypeArguments(genericType, obj);
		return typeArguments == null ? null : typeArguments.get(typeParameterName);
	}

	/**
	 * Try to find the instantiation of all of genericTypes type parameters in objs class.
	 * 
	 * @param genericType   the generic supertype of objs class
	 * @param obj                   an instantiation of a subclass of genericType. All of genericTypes type
	 *                                              parameters must have been instantiated in the inheritance hierarchy.
	 * @return                              a map of genericTypes type parameters (their name in the source code) to
	 *                                              the type they are instantiated as in obj
	 */
	public static Map<String, Type> getTypeArguments(Class<?> genericType, Object obj) {
		if (obj instanceof TypeArgumentDelegator) {
			return ((TypeArgumentDelegator) obj).getTypeArguments(genericType);
		}
		Map<String, Type> typeMap = new TreeMap<String, Type>();
		return getTypeArguments(genericType, obj.getClass(), typeMap);
	}

	public static boolean isAssignableFrom(Type type1, Type type2) {
		if (type1 instanceof Class<?> && type2 instanceof Class<?>) {
			return ((Class<?>) type1).isAssignableFrom((Class<?>) type2);
		} else {
			return type1.equals(type2);
		}
	}

	private static Map<String, Type> getTypeArguments(Class<?> genericType, Type type, Map<String, Type> typeMap) {
		if (type instanceof ParameterizedType) {
			return getTypeArguments(genericType, (ParameterizedType) type, typeMap);
		} else if (type instanceof Class<?>) {
			return getTypeArguments(genericType, (Class<?>) type, typeMap);
		} else {
			throw new IllegalArgumentException();
		}
	}

	private static Map<String, Type> getTypeArguments(Class<?> genericType, Class<?> classType, Map<String, Type> typeMap) {
		if (genericType.isInterface()) {
			for (Type interfaceType : classType.getGenericInterfaces()) {
				Map<String, Type> result = getTypeArguments(genericType, interfaceType, typeMap);
				if (result != null)
					return result;
			}
		}

		Type superType = classType.getGenericSuperclass();
		if (superType != null) {
			return getTypeArguments(genericType, superType, typeMap);
		}

		return null;
	}

	private static Map<String, Type> getTypeArguments(Class<?> genericType, ParameterizedType paramType, Map<String, Type> typeMap) {
		Class<?> rawType = (Class<?>) paramType.getRawType();
		if (rawType == genericType) {
			// found it!
			TypeVariable<?> typeVars[] = rawType.getTypeParameters();
			Type actualTypes[] = paramType.getActualTypeArguments();
			Map<String, Type> result = new TreeMap<String, Type>();
			for (int i = 0; i < actualTypes.length; i++) {
				while (actualTypes[i] != null && actualTypes[i] instanceof TypeVariable<?>) {
					String key = typevarString((TypeVariable<?>) actualTypes[i]);
					if (typeMap.containsKey(key))
						actualTypes[i] = typeMap.get(key);
					else
						actualTypes[i] = null;
				}
				result.put(typeVars[i].getName(), actualTypes[i]);
			}
			return result;
		} else {
			TypeVariable<?> typeVars[] = rawType.getTypeParameters();
			Type actualTypes[] = paramType.getActualTypeArguments();
			for (int i = 0; i < typeVars.length; i++)
				typeMap.put(typevarString(typeVars[i]), actualTypes[i]);
			return getTypeArguments(genericType, paramType.getRawType(), typeMap);
		}
	}

	private static String typevarString(TypeVariable<?> tv) {
		return tv.getGenericDeclaration().toString() + " " + tv.getName();
	}

	public static String getCanonicalSimpleName(Class cls) {
		return getCanonicalSimpleName(cls, false);
	}

	public static String getCanonicalSimpleName(Class cls, boolean includePackaging) {
		if (cls == null)
			return "<?>";
		if (cls.isArray()) {
			return getCanonicalSimpleName(cls.getComponentType(), includePackaging) + "[]";
		}
		String c = cls.getSimpleName();
		if (cls.isPrimitive())
			return c;
		if (includePackaging && c != null) {
			c = cls.getPackage().getName() + "." + c;
		}
		if (c == null || c.length() == 0) {
			c = cls.getCanonicalName();
			if (c != null & !includePackaging) {
				c = c.substring(c.lastIndexOf('.') + 1);
			}
		}
		if (c == null || c.length() == 0) {
			c = cls.getName();
			if (!includePackaging)
				c = c.substring(c.lastIndexOf('.') + 1);
		}
		if (cls.isAnonymousClass()) {
			Class clz1 = cls.getSuperclass();
			if (clz1 == null || clz1 == Object.class) {
				Class[] cs = cls.getInterfaces();
				if (cs != null && cs.length > 0) {
					return getCanonicalSimpleName(cs[0], false) + "-" + c;
				}
			}
			return getCanonicalSimpleName(clz1, false) + "-" + c;
		}
		return c;
	}

	public static Object getFieldValue(Object obj, Field f) throws IllegalArgumentException, IllegalAccessException {
		boolean ca = f.isAccessible();
		try {
			if (!ca) {
				f.setAccessible(true);
			}
			if (isStatic(f))
				return f.get(null);
			return f.get(recastRU(obj, f.getDeclaringClass()));
		} catch (NoSuchConversionException e) {
			throw new IllegalArgumentException(e);
		} finally {
			if (!ca) {
				//f.setAccessible(false);
			}
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

	public static <T> List<T> toList(Iterator<T> args) {
		ArrayList al = new ArrayList();
		while (args.hasNext()) {
			al.add(args.next());
		}
		return al;
	}

	public static <T> List<T> toList(Set<T> args) {
		ArrayList al = new ArrayList();
		for (Object n : args) {
			al.add(n);
		}
		return al;
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
				if (o.getClass().isArray()) {
					throw new UnsupportedOperationException("first of an array of arrays " + args);
				}
				T t = (T) o;
				return t;
			} catch (ClassCastException cce) {

			}

		}
		return null;
	}

	public static boolean isAssignableFrom(Class objNeedsToBe, Class from) {
		if (isAssignableFromW(objNeedsToBe, from) == Converter.WILL) {
			return true;
		}
		return false;
	}

	public static int isAssignableFromW(Class objNeedsToBe, Class from) {
		if (objNeedsToBe.isAssignableFrom(from))
			return Converter.WILL;
		return DEFAULT_CONVERTER.declaresConverts(null, from, objNeedsToBe, AggregateConverter.newMcvt());
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
							method = sc.getDeclaredMethod(((Member) m).getName(), method.getParameterTypes());
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

	public static boolean implementsAllClasses(Class cls, Class... classes) {
		for (Class c : classes) {
			if (!isAssignableFrom(c, cls))
				return false;
		}
		return true;
	}

	public static boolean isSynthetic(Member theMethod) {
		if (!theMethod.isSynthetic())
			return false;
		return true;
	}

	public static Class getComponentType(final Class type) {
		if (isGeneric(type)) {

		}
		if (Iterable.class.isAssignableFrom(type))
			return getTypeClass(type, Iterable.class);

		if (type.isArray())
			return type.getComponentType();
		return null;
	}

	@UISalient public static boolean isGeneric(Class type) {
		if (type.getTypeParameters().length == 0)
			return false;
		return true;
	}

}
