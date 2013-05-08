package org.appdapter.gui.swing;

import java.beans.BeanInfo;
import java.beans.MethodDescriptor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractListModel;

import org.appdapter.gui.objbrowser.model.Utility;
import org.appdapter.gui.swing.impl.JJList;

/**
 * A GUI component that shows all the methods for a given object, and provides
 * ways of executing these methods.
 * 
 * 
 */
public class MethodList extends JJList {
	private Model model;
	private Object object;
	public Class<?> objectClass;

	public MethodList(Object object, boolean asTypeObject) throws Exception {
		super();
		this.object = object;
		this.objectClass = object.getClass();
		if (object instanceof Class) {
			if (asTypeObject)
				objectClass = (Class) object;
			object = null;
		}
		this.model = new Model();
		setModel(model);
	}

	public Method getSelectedMethod() {
		return model.getMethodAt(getSelectedIndex());
	}

	class Model extends AbstractListModel {
		List methods;

		public Model() throws Exception {
			BeanInfo info = Utility.getPOJOInfo(getPOJOClass());

			MethodDescriptor[] descriptors;
			descriptors = info.getMethodDescriptors();
			methods = new LinkedList();
			for (int i = 0; i < descriptors.length; ++i) {
				MethodDescriptor descriptor = descriptors[i];
				methods.add(descriptor.getMethod());
			}

			Collections.sort(methods, new MethodComparator());
		}

		private Class<?> getPOJOClass() {
			return objectClass;
		}

		public Method getMethodAt(int index) {
			return (Method) methods.get(index);
		}

		public Object getElementAt(int index) {
			return getMethodAt(index).getName();
		}

		public int getSize() {
			return methods.size();
		}
	}

	class MethodComparator implements Comparator {
		public int compare(Object first, Object second) {
			Method a = (Method) first;
			Method b = (Method) second;
			String nameA = a.getName();
			String nameB = b.getName();
			return nameA.compareTo(nameB);
		}

		public boolean equals(Object o) {
			return (o instanceof MethodComparator);
		}
	}

}