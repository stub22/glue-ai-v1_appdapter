package org.appdapter.core.log;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Logger;

public abstract class Debuggable {

	public static int PRINT_DEPTH = 3;
	public static LinkedList<Object> allObjectsForDebug = new LinkedList<Object>();

	public static Logger LOGGER = Logger.getLogger(Debuggable.class.getSimpleName());

	@Override public String toString() {
		return toInfoStringF(this, PRINT_DEPTH);
	}

	public static String toInfoStringArgV(Object... params) {
		return toInfoStringA(params, ",", PRINT_DEPTH);
	}

	public static String toInfoStringCompound(String str, Object... params) {
		return str + "(" + toInfoStringA(params, ",", PRINT_DEPTH) + ")";
	}

	public static <T extends Object> T NoSuchClassImpl(Object... objects) {
		String dstr = "NoSuchClassImpl" + Debuggable.toInfoStringA(objects, ":", PRINT_DEPTH);

		RuntimeException rte = warn(dstr);
		// if (true) return null;
		if (true) {
			throw rte;
		}
		return null;
	}

	public static RuntimeException warn(Object... objects) {
		String dstr = Debuggable.toInfoStringA(objects, " : ", PRINT_DEPTH);
		RuntimeException rte = new NullPointerException(dstr);
		rte.printStackTrace();
		return rte;
	}

	public static String trace(Object... objects) {
		String dstr = Debuggable.toInfoStringA(objects, " : ", PRINT_DEPTH);
		return dstr;
	}

	public static String toInfoStringA(Object[] params, String sep, int depth) {
		if (params == null)
			return "<Null[]>";
		if (params.length == 0)
			return "/*0*/";
		if (params.length == 1)
			return toInfoStringV(params[0], depth);
		depth--;
		boolean needComma = false;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < params.length; i++) {
			String str = "" + params[i];
			Object next = params[i];
			if ((next instanceof String) && (str.startsWith("=") || str.endsWith("]"))) {
				sb.append(next);
				continue;
			}
			if (needComma) {
				sb.append(sep);
			}
			if (str.endsWith("=")) {
				sb.append(str);
				needComma = false;
			} else {
				sb.append(toInfoStringV(params[i], depth));
				needComma = true;
			}
		}
		return sb.toString();
	}

	public static String toInfoStringQ(String str, boolean quoted) {
		if (!quoted)
			return str;
		return "\"" + str.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
	}

	public static String toInfoStringV(Object o, int depth) {
		return toInfoStringV(o, true, depth);
	}

	public static String toInfoStringO(Object o) {
		return toInfoStringV(o, true, PRINT_DEPTH);
	}

	public static String toInfoStringV(Object o, boolean quoted, int depth) {
		if (o == null)
			return "<Null>";
		if (o instanceof Number)
			return "" + o;
		if (o instanceof Enum)
			return o.getClass().getCanonicalName() + "." + o;
		if (o instanceof byte[])
			return toInfoStringQ(new String((byte[]) o), quoted);
		if (o instanceof char[])
			return toInfoStringQ(new String((char[]) o), quoted);
		if (o instanceof CharSequence)
			return toInfoStringQ(o.toString(), quoted);
		if (o instanceof Class)
			return toInfoStringC((Class) o);
		if (o instanceof Throwable)
			return toInfoStringThrowable((Throwable) o);
		if (o instanceof Object[])
			return "[" + toInfoStringA((Object[]) o, ",", depth) + "]";
		if (!declaresToString(o.getClass()))
			return toInfoStringF(o, depth);
		return "" + o;
	}

	private static String toInfoStringThrowable(Throwable o) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		try {
			o.printStackTrace(pw);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return sw.toString();
	}

	final public static Class[] CLASSES0 = new Class[0];
	public static final boolean IsAndroid = false;

	private static boolean declaresToString(Class<? extends Object> class1) {
		try {
			Class declOn = class1.getMethod("toString", CLASSES0).getDeclaringClass();
			return declOn != Object.class;
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}
		return false;
	}

	static ThreadLocal<HashSet<String>> DontDescend = new ThreadLocal<HashSet<String>>() {
		@Override protected HashSet<String> initialValue() {
			return new HashSet<String>();
		}
	};

	public static String toInfoStringF(Object o) {
		return toInfoStringF(o, PRINT_DEPTH);
	}

	public static String toInfoStringF(Object o, int depth) {
		if (o == null)
			return "<Null>";
		String key = objKey(o);
		HashSet<String> keys = DontDescend.get();
		if (DontDescend.get().contains(key) || depth <= 0)
			return "{" + key + "}";
		try {
			keys.add(key);
			return toInfoStringFC(o, o.getClass(), depth - 1);
		} finally {
			keys.remove(key);
		}
	}

	public static String toInfoStringFC(Object o, Class c, int depth) {
		if (o == null)
			return /* "(" + T.class + ")" + */"<Null>";
		if (c == null || c == Object.class)
			return objKey(o);
		java.lang.reflect.Field[] fs = c.getDeclaredFields();
		if (fs.length > 0) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < fs.length; i++) {
				java.lang.reflect.Field f = fs[i];
				boolean isSt = Modifier.isStatic(f.getModifiers());
				if (isSt)
					continue;
				f.setAccessible(true);
				try {
					Object val = f.get(o);
					sb.append(f.getName() + "=" + toInfoStringV(val, depth) + ",");
				} catch (Error e) {
					UnhandledException(e);
				} catch (Exception e) {
				}

			}
			return sb.toString() + toInfoStringFC(o, c.getSuperclass(), depth);
		}
		return "cls0=" + toInfoStringC(c);
	}

	public static String objKey(Object o) {
		return "cls=" + toInfoStringC(o.getClass()) + ",inst=" + java.lang.System.identityHashCode(o);
	}

	private static String toInfoStringC(Class c) {
		if (c == null)
			return "<?>";
		return c.getSimpleName();
	}

	public static boolean mustBeSameStrings(String gs1, String gs2) {
		return makeMatchable(gs1).equals(makeMatchable(gs2));
	}

	private static Object makeMatchable(String gs1) {
		String gs2 = gs1.replaceAll("\n", " ").replaceAll("  ", " ");
		return gs2.trim();
	}

	public static Logger getLogger(Class<?> name) {
		return Logger.getLogger(name.getSimpleName());
	}

	public static RuntimeException UnhandledException(Throwable e) {
		e.printStackTrace();
		return warn("e=" + e.getMessage());
	}

	public static <T> T notImplemented(Object... params) {
		String msg = "notImplemented: " + toInfoStringA(params, ",", PRINT_DEPTH);
		if (true)
			throw new AbstractMethodError(msg);
		return (T) null;
	}

	public static void eclImplemented(Object... params) {
		String msg = "eclImplemented: " + toInfoStringA(params, ",", PRINT_DEPTH);
		warn(msg);
	}

	public static RuntimeException reThrowable(Throwable e) {
		if (e instanceof InvocationTargetException) {
			e = e.getCause();
		}
		if (e instanceof Error) {
			throw ((Error) e);
		}
		if (e instanceof RuntimeException)
			throw (RuntimeException) e;
		return reThrowable(e, RuntimeException.class, true, true);
	}

	public static <T extends Throwable> T reThrowable(Throwable e, Class<T> classOf) {
		if (classOf == null)
			throw reThrowable(e);
		return reThrowable(e, classOf, false, false);
	}

	public static <T extends Throwable> T reThrowable(Throwable e, Class<T> classOf, boolean throwFirstErrorCause, boolean throwFirstRTECause) {
		Throwable e1 = e;
		e.fillInStackTrace();
		Error err = null;
		RuntimeException rte = null;
		boolean needOther = true;
		while (true) {
			if (classOf.isInstance(e1))
				return (T) e1;
			if (throwFirstErrorCause && e1 instanceof Error) {
				err = (Error) e1;
				throwFirstErrorCause = throwFirstRTECause = false;
			}
			if (throwFirstRTECause && e1 instanceof RuntimeException) {
				rte = (RuntimeException) e1;
				throwFirstErrorCause = throwFirstRTECause = false;
			}
			Throwable e2 = e1.getCause();
			if (e2 == e1 || e2 == null) {
				break;
			}
			if (e1 instanceof InvocationTargetException) {
				e = e2;
			}
			e1 = e2;
		}
		if (err != null)
			throw err;
		if (rte != null)
			throw rte;
		return wrapException(e, classOf, RuntimeException.class);
	}

	public static <T extends Throwable, O extends Throwable> T wrapException(Throwable e, Class<T> classOf, Class<O> otherwise) throws O {
		T wrapped = wrapException(e, classOf);
		if (wrapped != null)
			return wrapped;
		O otherw = wrapException(e, otherwise);
		if (otherw != null)
			throw otherw;
		throw new RuntimeException("DEBUGGABLE^^^^^^^^^^&&&&&&&&&&&&&&&&&&&&&&&&& RETHROWWWABLE: " + e.getMessage(), e);
	}

	public static <T extends Throwable> T wrapException(Throwable cause, Class<T> newClass) {
		if (newClass == null) {
			throw reThrowable(cause, RuntimeException.class);
		}
		Throwable newVer;
		try {
			newVer = newInstance(newClass, C_ST, cause.getMessage(), cause);
		} catch (Throwable nsm) {
			try {
				newVer = newInstance(newClass, C_T, cause);
			} catch (Throwable nsm1) {
				try {
					newVer = newInstance(newClass, C_S, cause.getMessage());
					newVer.initCause(cause);
				} catch (Throwable nsm2) {
					try {
						newVer = newInstance(newClass, C_0);
						newVer.initCause(cause);
					} catch (Throwable nsm3) {
						return null;
						// newVer = rte = new RuntimeException(e.getMessage(), e);
					}
				}
			}
		}
		setCause(newVer, cause);
		return (T) newVer;
	}

	private static <T extends Throwable> void setCause(Throwable ex, Throwable cause) {
		try {
			ex.initCause(cause);
		} catch (Throwable unseen) {
		}
		try {
			StackTraceElement[] st = cause.getStackTrace();
			if (st != null)
				ex.setStackTrace(st);
			Throwable ec = ex.getCause();
			if (ec != cause) {
				try {
					Debuggable.setField(ex, ex.getClass(), Throwable.class, "cause", cause);
				} catch (Throwable e2) {
					// cannot set the cause?!
				}
			}
		} catch (Throwable unseen) {
		}
	}

	public static <T> T newInstance(Class<T> classOf, Class[] types, Object... args) throws Throwable {
		Constructor<T> c = classOf.getDeclaredConstructor((Class[]) types);
		c.setAccessible(true);
		return c.newInstance(args);
	}

	public static void setField(Object obj, Class classOf, Class otherwise, String fieldname, Object value) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, Throwable {
		{
			Throwable why = null;
			Field causeF;
			try {
				causeF = classOf.getField(fieldname);
				try {
					causeF.set(obj, value);
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
				causeF.set(obj, value);
			} catch (SecurityException e) {
				throw e;
			} catch (NoSuchFieldException e) {
				if (why != null)
					throw why;
				throw e;
			}
		}
	}

	public static Object getField(Object obj, Class classOf, Class otherwise, String fieldname) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, Throwable {
		{
			Throwable why = null;
			Field causeF;
			if (classOf != null) {
				try {
					causeF = classOf.getField(fieldname);
					try {
						causeF.setAccessible(true);
						return causeF.get(obj);
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
			}
			try {
				causeF = otherwise.getDeclaredField(fieldname);
				causeF.setAccessible(true);
				return causeF.get(obj);
			} catch (SecurityException e) {
				throw e;
			} catch (NoSuchFieldException e) {
				classOf = otherwise.getSuperclass();
				if (classOf != null) {
					try {
						return getField(obj, null, classOf, fieldname);
					} catch (NoSuchFieldException nsfe) {

					}
				}
				if (why != null)
					throw why;
				throw e;
			}
		}
	}

	final static Class[] C_ST = new Class[] { String.class, Throwable.class };
	final static Class[] C_S = new Class[] { String.class };
	final static Class[] C_T = new Class[] { Throwable.class };
	final static Class[] C_0 = new Class[] {};

	public static void printStackTrace(final Throwable ex) {
		printStackTrace(ex, System.err, -1);
		return;
	}

	public static void printStackTrace(final Throwable ex, PrintStream ps, int maxLines) {
		Throwable e = ex;
		while (e != null) {
			printStackTraceLocal(e, ps, 100);
			ps.println("\n Caused by... ");
			Throwable c = e.getCause();
			if (c == null || c == e) {
				return;
			}
			e = c;
		}
	}

	private static int printStackTraceLocal(Throwable ex, PrintStream ps, int maxDepth) {
		int depthShown = 0;
		if (ex == null) {
			ps.println("NULL TRHOWABLE");
			return depthShown;
		}
		ps.println(ex.getClass() + ": " + ex.getMessage());
		StackTraceElement[] elems = ex.getStackTrace();
		if (elems == null) {
			ps.println("NULL TRHOWABLE ELS");
			return depthShown;
		}
		int td = elems.length - 1;
		int es = 0;
		while (maxDepth > 0) {
			maxDepth--;
			depthShown++;
			StackTraceElement el = elems[es];
			ps.println(" at " + el);
			es++;
			if (es >= td)
				break;
		}
		return depthShown;
	}

	public static String details(Throwable ex) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		printStackTrace(ex, ps, -1);
		try {
			return baos.toString("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			return baos.toString();
		}
	}

}
