package org.appdapter.gui.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public abstract class CollectionSetUtils {

	public interface TAccepts<S> {

		boolean isCompleteOn(S e);

		boolean resultOf(S e);

	}

	public static <T> boolean addToList(Collection<T> list, T element) {
		if (element instanceof HRKRefinement.DontAdd) {
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

	public static <T> boolean addIfNew(Collection<T> list, T element, boolean nullOK) {
		if (!nullOK && element == null)
			return false;
		if (element instanceof HRKRefinement.AskIfEqual) {
			HRKRefinement.AskIfEqual aie = (HRKRefinement.AskIfEqual) element;
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

}
