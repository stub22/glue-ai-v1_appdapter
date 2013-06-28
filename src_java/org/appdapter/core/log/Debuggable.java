package org.appdapter.core.log;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.logging.Logger;

public abstract class Debuggable {

	public static int PRINT_DEPTH = 3;

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
			return "<[0]>";
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

	public static RuntimeException asRuntimeException(Throwable e) {
		Throwable useE = e;
		while (e != null) {
			if (e instanceof InvocationTargetException)
				useE = e = e.getCause();
			if (e instanceof RuntimeException)
				return (RuntimeException) e;
			if (e instanceof Error) {
				throw ((Error) e);
			}
			Throwable ne = e.getCause();
			if (ne == null || ne == e)
				break;
			e = ne;
		}
		return new RuntimeException(e.getMessage(), e);
	}

	public static <T> T notImplemented(Object... params) {
		String msg = "notImplemented: " + toInfoStringA(params, ",", PRINT_DEPTH);
		if (true)
			throw new AbstractMethodError(msg);
		return (T) null;
	}

	public static RuntimeException reThrowable(Throwable e) {
		printStackTrace(e);
		return asRuntimeException(e);
	}

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
