package org.appdapter.gui.browse;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

abstract public class ToFromStringConverter<T> {
	protected Class<T> type;

	protected ToFromStringConverter(Class<T> clz) {
		type = clz;
	}

	protected ToFromStringConverter() {
		type = null;
	}

	public abstract T fromString(String title, Class specializedMaybe);

	public abstract String toString(T toBecomeAString);

	public Object fromStringSearch(String title) throws ClassCastException {
		if (type == String.class)
			return title;
		type = Utility.nonPrimitiveTypeFor(type);
		ClassCastException cce = null;
		Class searchType = type;
		while (searchType != null) {
			for (Method m : searchType.getDeclaredMethods()) {
				if (m.getReturnType() == type) {
					Class[] pt = m.getParameterTypes();
					if (pt != null && pt.length == 1 && pt[0] == String.class && Modifier.isStatic(m.getModifiers())) {
						try {
							m.setAccessible(true);
							return m.invoke(null, title);
						} catch (Throwable e) {
							cce = new ClassCastException(type + " " + m.getName() + " " + title);
						}
					}
				}
			}
			searchType = searchType.getSuperclass();
		}
		if (cce != null)
			throw cce;
		throw new ClassCastException(type + " fromString " + title);
	}

}
