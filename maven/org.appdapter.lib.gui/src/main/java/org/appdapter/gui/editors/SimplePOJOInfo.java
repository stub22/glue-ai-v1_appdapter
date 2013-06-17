package org.appdapter.gui.editors;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.lang.reflect.Field;
import java.util.ArrayList;

abstract public class SimplePOJOInfo extends SimpleBeanInfo {

	final public BeanDescriptor getBeanDescriptor() {
		return new BeanDescriptor(beanClass, customizers.get(0));
	}

	Class beanClass;
	BeanInfo normalBeanInfo;
	ArrayList<Class> customizers = new ArrayList<Class>();

	public SimplePOJOInfo(Class cls, Class customizer) {
		super();
		beanClass = cls;
		customizers.add(customizer);
	}

	static int inGNInfo = 0;

	BeanInfo getNormalBeanInfo() {
		if (normalBeanInfo == null)
			try {
				normalBeanInfo = Introspector.getBeanInfo(beanClass);
			} catch (Throwable e) {
				throw BrokenBeanInfo(e);
			}
		return normalBeanInfo;
	}

	final public BeanInfo[] getAdditionalBeanInfo() {
		if (true)
			return null;
		if (inGNInfo > 0)
			return null;
		inGNInfo++;
		try {
			return new BeanInfo[] { getNormalBeanInfo() };
		} finally {
			inGNInfo--;
		}
	}

	@Override public PropertyDescriptor[] getPropertyDescriptors() {
		if (true)
			return null;
		if (properties == null) {
			properties = computeFieldProperties(beanClass);
		}
		return properties;
	}

	private PropertyDescriptor[] computeFieldProperties(Class beanClass2) {
		ArrayList<Field> fields = new ArrayList<Field>();
		PropertyDescriptor[] pdsAlready = null;//getNormalBeanInfo().getPropertyDescriptors();

		while (beanClass2 != null) {
			for (Field f : beanClass2.getDeclaredFields()) {
				if (pdsAlready != null) {
					for (PropertyDescriptor pd : pdsAlready) {
						if (pd.getName().equalsIgnoreCase(fixName(f.getName()))) {
							f = null;
							break;
						}
					}
				}
				if (f == null)
					continue;
				fields.add(f);
			}
			beanClass2 = beanClass2.getSuperclass();
		}
		return fieldsToProperties(fields);
	}

	static String fixName(String name) {
		if (name.startsWith("m_")) {
			name = name.substring(2);
		} else if (name.startsWith("f_")) {
			name = name.substring(2);
		}
		while (name.startsWith("_")) {
			name = name.substring(1);
		}
		return name;
	}

	private PropertyDescriptor[] fieldsToProperties(ArrayList<Field> fields) {
		int len = fields.size();
		ArrayList<PropertyDescriptor> pds = new ArrayList<PropertyDescriptor>();
		for (Field f : fields) {
			try {
				pds.add(new PropertyDescriptorForField(f).makePD());
			} catch (Throwable e) {
				throw BrokenBeanInfo(e);
			}
		}
		return pds.toArray(new PropertyDescriptor[len]);
	}

	static RuntimeException BrokenBeanInfo(Throwable e) {
		if (e instanceof Error) {
			e.printStackTrace();
			throw (Error) e;
		}
		if (e instanceof RuntimeException)
			return (RuntimeException) e;
		return new RuntimeException(e);
	}

	private PropertyDescriptor[] properties;

	static public class PropertyDescriptorForField {

		Class beanClass;
		Class propertyType;
		String propertyName;
		Field fld;
		private java.lang.reflect.Method writeMethod;
		private java.lang.reflect.Method readMethod;
		private Object m_obj;

		public PropertyDescriptorForField(Field f) throws IntrospectionException {
			fld = f;
			beanClass = f.getDeclaringClass();
			propertyName = fixName(f.getName());
			propertyType = f.getType();
		}

		public PropertyDescriptor makePD() {
			refreshRWMethods();
			try {
				return new PropertyDescriptor(propertyName, readMethod, writeMethod) {
					@Override public String getName() {
						return propertyName;
					}

					@Override public synchronized java.lang.reflect.Method getReadMethod() {
						refreshRWMethods();
						return readMethod;
					}

					@Override public synchronized java.lang.reflect.Method getWriteMethod() {
						refreshRWMethods();
						return writeMethod;
					}
				};
			} catch (IntrospectionException e) {
				throw BrokenBeanInfo(e);
			}
		}

		public Object readNamedProperty() {
			try {
				return fld.get(m_obj);
			} catch (Throwable e) {
				throw BrokenBeanInfo(e);
			}
		}

		void writeNamedProperty(Object value) {
			try {
				fld.set(m_obj, value);
			} catch (Throwable e) {
				throw BrokenBeanInfo(e);
			}
		}

		protected void refreshRWMethods() {
			readMethod = getMethod("readNamedProperty");
			writeMethod = getMethod("writeNamedProperty", Object.class);
		}

		public java.lang.reflect.Method getMethod(String string, Class... parameterTypes) {
			try {
				return getClass().getDeclaredMethod(string, parameterTypes);
			} catch (Throwable e) {
				throw BrokenBeanInfo(e);
			}
		}
	}

}
