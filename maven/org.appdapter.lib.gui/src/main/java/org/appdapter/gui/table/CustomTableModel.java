package org.appdapter.gui.table;

import static org.appdapter.core.log.Debuggable.printStackTrace;

import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.table.TableModel;

import org.appdapter.core.convert.NoSuchConversionException;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.gui.browse.Utility;

public class CustomTableModel extends BeanTableModel implements TableModel {


	public CustomTableModel() {
		this(Map.Entry.class);
	}

	public CustomTableModel(Class rowClass) {
		this(null, rowClass, rowClass, null, null);
	}

	public CustomTableModel(CellConversions conversions, Class rowClass, Class ancestorClass, List modelData, String[] colNames) {
		super(rowClass, ancestorClass, modelData, colNames);
		// TODO Auto-generated constructor stub
	}

	@Override public Class<?> getColumnClass(int columnIndex) {
		if (columnClasses != null && columnIndex < columnClasses.length) {
			Class cc = columnClasses[columnIndex];
			if (cc != null)
				return cc;
		}
		if (isEmpty()) {
			return Object.class;
		}
		return getValueAt(0, columnIndex).getClass();
	}

	@Override public int getColumnCount() {
		if (columnNames == null)
			return 0;
		int size = columnNames.size();
		if (size == 0) {
			return 0;
		}
		return size;
	}

	@Override public String getColumnName(int columnIndex) {
		return "" + columnNames.get(columnIndex);
	}

	protected Object getNamedValue(Object statement, String namedProp) throws NoSuchConversionException, SecurityException, NoSuchFieldException, Throwable {
		String localName = ".*" + namedProp.toUpperCase() + ".*";
		try {
			Object r = ReflectUtils.getObjectPropertyValue(statement, statement.getClass(), localName, null, false);
			if (r == null)
				return SafeJTable.WAS_NULL;
			return r;
		} catch (Throwable e) {
			throw e;
		}
	}

	@Override public int getRowCount() {
		if (isEmpty())
			return 0;
		List list = getRows();
		return list.size();
	}

	public Object getRowObject(int rowIndex) {
		return getRows().get(rowIndex);
	}

	@Override public Object getValueAt(int rowIndex, int columnIndex) {
		if (isEmpty())
			return SafeJTable.WAS_NULL;
		Object statement = getRowObject(rowIndex);
		try {
			Object ele = getNamedValue(statement, getColumnName(columnIndex));
			if (ele instanceof JComponent) {
				ele = Utility.dref(ele);
			}
			return ele;
		} catch (Throwable e) {
			printStackTrace(e);
			return statement;
		}
	}

	@Override public boolean isCellEditable(int rowIndex, int columnIndex) {
		return super.isCellEditable(rowIndex, columnIndex);
	}

	private boolean isEmpty() {
		List list = getRows();
		return (list == null || list.size() == 0);
	}

	protected void setNamedValue(Object statement, String named, Object recast) throws NoSuchConversionException, NoSuchFieldException {
		String localName = ".*" + named.toUpperCase() + ".*";
		ReflectUtils.setObjectPropertyValue(statement, statement.getClass(), localName, null, false, recast);
	}

	@Override public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (isEmpty())
			return;
		try {
			Object statement = getRowObject(rowIndex);
			String namedProp = getColumnName(columnIndex);
			Object was = getNamedValue(statement, namedProp);
			if (was != null && aValue != null && was.equals(aValue)) {
				return;
			}
			Class objNeedsToBe = getColumnClass(columnIndex);
			if (!objNeedsToBe.isInstance(was)) {
				was = null;
			}

			setNamedValue(statement, namedProp, aValue);
		} catch (Throwable e) {
			printStackTrace(e);
		}
	}

}