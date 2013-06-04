package org.appdapter.gui.swing;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.appdapter.gui.pojo.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoxesByType {
	private static Logger theLogger = LoggerFactory.getLogger(BoxesByType.class);

	public JTableComponent jcomp = new JTableComponent();

	public static class JTableComponent extends JTable {

	}

	List objects;
	POJOAppContext context;
	BeanInfo objectInfo;
	PropertyDescriptor[] props;
	Class objectClass;
	Model model;

	public BoxesByType(POJOAppContext context, List objects, Class objectClass,
			String[] propNames) throws Exception {
		objectInfo = Utility.getBeanInfo(objectClass);

		if (objects == null) {
			this.objects = new LinkedList();
		} else {
			this.objects = objects;
		}

		this.objectClass = objectClass;

		if (context == null) {
			this.context = Utility.getPOJOAppContext();
		} else {
			this.context = context;
		}

		if (propNames == null || propNames.length == 0) {
			props = objectInfo.getPropertyDescriptors();
		} else {
			LinkedList list = new LinkedList();
			Map map = createPropertyMap();
			for (int i = 0; i < propNames.length; ++i) {
				PropertyDescriptor p = (PropertyDescriptor) map
						.get(propNames[i]);
				if (p != null) {
					list.add(p);
				}
			}
			props = (PropertyDescriptor[]) list.toArray();
		}
		model = new Model();
		jcomp.setModel(model);
	}

	public void addObject(Object object) {
		if (object == null)
			return;

		if (objectClass.isInstance(object)) {
			objects.add(object);

			// @optimize
			model.fireTableDataChanged();

		} else {
			throw new IllegalArgumentException(
					"BeanTable.addObject(): this table only accepts objects of type "
							+ objectClass + ", not " + object.getClass());
		}
	}

	public void removeObject(Object object) {
		if (object == null)
			return;

		objects.remove(object);

		// @optimize
		model.fireTableDataChanged();
	}

	private Map createPropertyMap() {
		Map map = new HashMap();
		PropertyDescriptor[] array = objectInfo.getPropertyDescriptors();
		for (int i = 0; i < array.length; ++i) {
			map.put(array[i].getName(), array[i]);
		}
		return map;
	}

	private Object getPropertyValue(Object object, PropertyDescriptor prop)
			throws Exception {
		Method method = prop.getReadMethod();
		return method.invoke(object, new Object[0]);
	}

	private void setPropertyValue(Object object, PropertyDescriptor prop,
			Object value) throws Exception {
		Method method = prop.getWriteMethod();
		method.invoke(object, new Object[] { value });
	}

	public Object getSelectedBean() {
		int row = this.jcomp.getSelectedRow();
		if (row == -1) {
			return null;
		} else {
			return objects.get(row);
		}
	}

	class Model extends AbstractTableModel {
		@Override
		public int getRowCount() {
			return objects.size();
		}

		@Override
		public int getColumnCount() {
			return props.length;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return props[columnIndex].getDisplayName();
		}

		/*
		 * public Class getColumnClass(int columnIndex) { Class c =
		 * props[columnIndex].getPropertyType();
		 * 
		 * //@temp
		 * 
		 * System.out.println("c = " + c); if (c.equals(boolean.class)) return
		 * Boolean.class; if (c.equals(int.class)) return Integer.class; else
		 * return c;
		 * 
		 * }
		 */

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
			// return props[columnIndex].getWriteMethod() != null;
		}

		public Object getPOJOAt(int rowIndex) {
			return objects.get(rowIndex);
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object object = getPOJOAt(rowIndex);
			PropertyDescriptor prop = props[columnIndex];
			try {
				return getPropertyValue(object, prop);
			} catch (Throwable err) {
				theLogger.error("An error occured in BeanTable.getValueAt(...)", err);
				return "?";
			}
		}
		/*
		 * public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		 * { Object object = getPOJOAt(rowIndex); PropertyDescriptor prop =
		 * props[columnIndex]; try { setPropertyValue(object, prop, aValue); }
		 * catch (Throwable err) {
		 * theLogger.error("An error occured in BeanTable.setValueAt(...)", err); } }
		 */
	}
}
