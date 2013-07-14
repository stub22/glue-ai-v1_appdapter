package org.appdapter.gui.browse;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.appdapter.core.log.Debuggable;

public class PropertyDescriptorForField extends PropertyDescriptor {

	private static Method m_fw;
	private static Method m_fr;

	static public interface FakeMethods {

		public Object fakeReadMethod();

		public void fakeWriteMethod(Object o);
	}

	public Object getFieldValue(Object obj) {
		f.setAccessible(true);
		try {
			return f.get(obj);
		} catch (Throwable t) {
			throw Debuggable.reThrowable(t);
		}
	}

	static {
		try {
			m_fw = FakeMethods.class.getMethod("fakeReadMethod");
			m_fr = FakeMethods.class.getMethod("fakeWriteMethod", Object.class);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	final Field f;
	private Method readMethodOr;
	private Method writeMethodOr;

	public PropertyDescriptorForField(Field fld) throws IntrospectionException {
		this(fld, propertyNameForField(fld), null, (Method) null);
	}

	/**
	 * Sets the method that should be used to read the property value.
	 *
	 * @param readMethod The new read method.
	 */
	public synchronized void setReadMethod(Method readMethod)
				throws IntrospectionException {
		readMethodOr = readMethod;
	}

	public PropertyDescriptorForField(Field fld, String propertyNameForField, Method rm, Method wm) throws IntrospectionException {
		super(propertyNameForField, rm, wm);
		f = fld;
		readMethodOr = m_fr;
		writeMethodOr = m_fw;
	}

	public synchronized java.lang.Class<?> getPropertyType() {
		return f.getType();
	}

	@Override public String getDisplayName() {
		return super.getDisplayName();
	}

	public synchronized Method getReadMethod() {
		return readMethodOr;
	}

	public synchronized Method getWriteMethod() {
		return writeMethodOr;
	}

	/**
	 * Sets the method that should be used to write the property value.
	 *
	 * @param writeMethod The new write method.
	 */
	public synchronized void setWriteMethod(Method writeMethod)
				throws IntrospectionException {
		writeMethodOr = writeMethod;
	}

	public static String propertyNameForField(Field f) {
		return clipPropertyNameMethod(f.getName(), "my");
	}

	public static String clipPropertyNameMethod(String propertyName, String... clipOff) {
		int clipCheck = 1;
		for (String clip : clipOff) {
			int clipLen = clip.length();
			if (propertyName.length() > clipLen + 1) {
				if (propertyName.startsWith(clip)) {
					clipCheck = 0;
					propertyName = propertyName.substring(clipLen);
				}
			}
		}
		if (clipCheck < propertyName.length()) {
			if (propertyName.charAt(clipCheck) == '_') {
				propertyName = propertyName.substring(clipCheck + 1);
			}
		}

		while (propertyName.startsWith("_")) {
			propertyName = propertyName.substring(1);
		}
		return propertyName;
	}
}
