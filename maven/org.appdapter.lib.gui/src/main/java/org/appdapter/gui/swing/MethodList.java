package org.appdapter.gui.swing;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractListModel;

import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.gui.browse.Utility;

/**
 * A GUI component that shows all the static methods for a given class,
 * and provides ways of executing these methods.
 *
 * 
 */
public class MethodList extends JJList {

	class MethodComparator implements Comparator {
		@Override public int compare(Object first, Object second) {
			Method a = (Method) first;
			Method b = (Method) second;
			String nameA = a.getName();
			String nameB = b.getName();
			return nameA.compareTo(nameB);
		}

		@Override public boolean equals(Object o) {
			return (o instanceof MethodComparator);
		}
	}

	boolean showStatic = true;
	boolean showNonStatic = true;
	boolean showNonPublic = true;
	private Object object;
	private Class objectClass;
	private ModelFromMethods model;

	public MethodList(Object object, boolean isClass) throws Exception {
		this(object, isClass ? (Class) object : null, isClass, !isClass);
	}

	public MethodList(Object object, Class objCl, boolean showStatics, boolean showNonStatics) throws Exception {
		this.object = object;
		this.showStatic = showStatics;
		this.showNonStatic = showNonStatics;
		if (objCl == null)
			objCl = object.getClass();
		this.objectClass = objCl;
		if (object instanceof Class) {
			objectClass = (Class) object;
			object = null;
		}
		this.model = new ModelFromMethods();
		setModel(model);
	}

	class ModelFromMethods extends AbstractListModel {
		final List methods = new LinkedList();

		public ModelFromMethods() throws Exception {
			fromGetMethods();
			fromBeanInfo();
		}

		private void fromBeanInfo() throws IntrospectionException {
			BeanInfo info = Utility.getBeanInfo(getObjectClass(), object);

			MethodDescriptor[] descriptors;
			descriptors = info.getMethodDescriptors();
			for (int i = 0; i < descriptors.length; ++i) {
				MethodDescriptor descriptor = descriptors[i];
				Method method = descriptor.getMethod();
				addMethod(method);
			}

			Collections.sort(methods, new MethodComparator());
		}

		private void addMethod(Method method) {
			boolean isPublic = Modifier.isPublic(method.getModifiers());
			if (!isPublic && !showNonPublic)
				return;
			boolean isStatic = ReflectUtils.isStatic(method);
			if (!showStatic && isStatic)
				return;
			if (!showNonStatic && !isStatic)
				return;
			if (methods.contains(method))
				return;
			methods.add(method);
		}

		private void fromGetMethods() {
			Method[] methodsArray = objectClass.getMethods();

			for (int i = 0; i < methodsArray.length; ++i) {
				Method method = methodsArray[i];
				addMethod(method);
			}

			Collections.sort(methods, new MethodComparator());

		}

		@Override public Object getElementAt(int index) {
			try {
				return getMethodAt(index).getName();
			} catch (Exception err) {
				return null;
			}
		}

		public Method getMethodAt(int index) {
			try {
				return (Method) methods.get(index);
			} catch (Exception err) {
				return null;
			}
		}

		@Override public int getSize() {
			return methods.size();
		}
	}

	public Class getObjectClass() {
		return this.objectClass;
	}

	public Method getSelectedMethod() {
		return model.getMethodAt(getSelectedIndex());
	}

}
