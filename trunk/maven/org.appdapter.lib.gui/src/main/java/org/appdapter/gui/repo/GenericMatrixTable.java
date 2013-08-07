/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.org).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/*
 * ModelMatrixPanel.java
 *
 * Created on Oct 25, 2010, 8:12:03 PM
 */

package org.appdapter.gui.repo;

import static org.appdapter.core.log.Debuggable.printStackTrace;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.TableUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreeCellEditor;

import org.appdapter.api.trigger.Box;
import org.appdapter.core.convert.NoSuchConversionException;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.gui.api.WrapperValue;
import org.appdapter.gui.browse.ToFromKeyConverter;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.swing.CellEditorComponent;
import org.appdapter.gui.swing.PropertyValueControl;
import org.appdapter.gui.swing.ScreenBoxPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class GenericMatrixTable extends ScreenBoxPanel {
	public class CustomCellEditor extends CustomCellRenderer implements TableCellEditor, TreeCellEditor {

		int edit_col, edit_row;

		PropertyValueControl propertyValueControl;

		public CustomCellEditor(int columnNum, Class<?> columnCl, Object toS) {
			super(columnNum, columnCl, toS);
		}

		@Override public void addCellEditorListener(CellEditorListener l) {
			getCellEditor().addCellEditorListener(l);

		}

		@Override public void cancelCellEditing() {
			getCellEditor().cancelCellEditing();

		}

		public TableCellEditor getCellEditor() {
			return getCellEditorComponent().getCellEditor();
		}

		public CellEditorComponent getCellEditorComponent() {
			return getPropertyValueControl();
		}

		@Override public Object getCellEditorValue() {
			return getCellEditor().getCellEditorValue();
		}

		private PropertyValueControl getPropertyValueControl() {
			if (propertyValueControl == null) {
				Class clz = getColumnClass();
				propertyValueControl = new PropertyValueControl(null, getTitle(), clz, true, getCellConverter(clz));
			}
			return propertyValueControl;
		}

		@Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			return getCellEditor().getTableCellEditorComponent(table, value, isSelected, row, column);
		}

		public TreeCellEditor getTreeCellEditor() {
			return getPropertyValueControl().getTreeCellEditor();
		}

		@Override public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
			return getTreeCellEditor().getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
		}

		@Override public boolean isCellEditable(EventObject anEvent) {
			return getCellEditor().isCellEditable(anEvent);
		}

		@Override public void removeCellEditorListener(CellEditorListener l) {
			getCellEditor().removeCellEditorListener(l);

		}

		public void setEditRowCol(int row, int column) {
			propertyValueControl = null;
			edit_col = column;
			edit_row = row;
		}

		public void setValue(Object value) {
			String str = getTextName(value, edit_row, edit_col);
			super.setValue(str);
			//label.setText(str);
		}

		@Override public boolean shouldSelectCell(EventObject anEvent) {
			return getCellEditor().shouldSelectCell(anEvent);
		}

		@Override public boolean stopCellEditing() {
			return getCellEditor().stopCellEditing();
		}

	}

	public class CustomCellRenderer extends DefaultTableCellRenderer implements

	ListCellRenderer, TableCellRenderer {

		final JLabel label = this;
		Class columnClass;
		private int columnNumber;
		Color fColor, bColor;
		boolean hasFocus;
		private JComponent parentHolder;
		int render_col, render_row;
		private Object toStr;

		/**
		 * default constructor which sets border properties and opaque mode that
		 * means that label paints every pixel within its bounds.
		 */
		public CustomCellRenderer(int columnNum, Class<?> columnCl, Object toS) {
			super();
			//this.label = new JLabel();
			//add(label);
			setOpaque(false);
			columnClass = columnCl;
			columnNumber = columnNum;
			toStr = toS;
		}

		public Class getColumnClass() {
			// TODO Auto-generated method stub
			if (columnClass != null)
				return columnClass;
			return GenericMatrixTable.this.getTable().getColumnClass(columnNumber);
		}

		/**
		 * Return a component that has been configured to display the specified
		 * value.
		 * 
		 * @param list
		 *            The JList we're painting.
		 * @param value
		 *            The value returned by
		 *            <code>list.getModel().getElementAt(index)</code>.
		 * @param index
		 *            The cells index.
		 * @param isSelected
		 *            True if the specified cell was selected.
		 * @param cellHasFocus
		 *            True if the specified cell has the focus.
		 * @return label component configured to display cell for list
		 */
		@Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			try {
				this.render_col = 0;
				this.render_row = index;

				this.parentHolder = list;
				label.setComponentOrientation(list.getComponentOrientation());
				label.setEnabled(list.isEnabled());
				label.setFont(list.getFont());
				setValue(value);
				//ListData ld = (ListData) value;
				//Icon icon = ld.getIcon();
				//if (icon != null)
				//label.setIcon(icon);
				bColor = (isSelected ? list.getSelectionBackground() : list.getBackground());
				fColor = (isSelected ? list.getSelectionForeground() : list.getForeground());
				hasFocus = cellHasFocus;
			} catch (Throwable e) {
				theLogger.warn("getListCellRendererComponent Exception", e);
			}

			return this;
		}

		@Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			int width = table.getColumnModel().getColumn(column).getWidth();
			int height = table.getRowHeight(row);
			this.render_col = column;
			this.render_row = row;
			return renderComponent(table, value, row, column, isSelected, width, table.getSelectionBackground(), table.getSelectionForeground());

		}

		public String getTitle() {
			return "" + toStr;
		}

		/**
		 * This method is extension of <code>JLabel.paint(Graphics )</code>
		 * standard method to paint non standard selection for list control.
		 * 
		 * @param g
		 *            the specified Graphics window
		 */
		@Override public void paint(Graphics g) {

			super.paint(g);
			/*
						Icon icon = getIcon();
						
						try {
							// clear selection
							g.setColor(parentHolder.getBackground());
							g.fillRect(0, 0, getWidth(), getHeight());
							// paint cell
							g.setColor(bColor);
							int offset = 0;
							if (icon != null && getText() != null) {
								Insets ins = getInsets();
								offset = ins.left + icon.getIconWidth() + getIconTextGap() - 3;
							}
							g.fillRect(offset, 0, getWidth() - offset - 1, getHeight() - 1);
							// if cell has focus draw rectangle over selection and correct
							// text look like (adds font 3D effect)
							if (hasFocus) {
								g.draw3DRect(offset + 1, 1, getWidth() - offset - 2, getHeight() - 2, false);
								g.setColor(new Color(96, 96, 156));
								g.drawRect(offset, 0, getWidth() - offset - 1, getHeight() - 1);
								g.setColor(new Color(225, 225, 255));
								g.drawString(getText(), offset + 4, getHeight() - 3);
							}
							// sets default colors and call default paint method
							setForeground(fColor);
							setBackground(bColor);
							super.paint(g);
						} catch (Throwable e) {
							theLogger.warn("Paint rubber stamp for list exception", e);
						}*/
		}

		private Component renderComponent(JComponent list, Object value, int col, int row, boolean isSelected, int width, Color selectionBackground, Color selectionForeground) {
			try {
				this.parentHolder = list;
				label.setComponentOrientation(list.getComponentOrientation());
				label.setEnabled(list.isEnabled());
				label.setFont(list.getFont());
				setValue(value);
				//ListData ld = (ListData) value;
				//Icon icon = ld.getIcon();
				//if (icon != null)
				//label.setIcon(icon);
				bColor = (isSelected ? selectionBackground : list.getBackground());
				fColor = (isSelected ? selectionForeground : list.getForeground());
				hasFocus = isSelected;
			} catch (Throwable e) {
				theLogger.warn("getListCellRendererComponent Exception", e);
			}

			return this;
		}

		public void setValue(Object value) {
			String str = getTextName(value, render_row, render_col);
			super.setValue(str);
			//label.setText(str);
		}

		public TableCellEditor getEditor() {
			return new CustomCellEditor(columnNumber, columnClass, toStr);
		}
	}

	public interface GetListFromHolder {
		List listFromHolder(Object o);
	}

	private static final Object WAS_NULL = "<null>";

	public class ModelMatrixTableModel implements TableModel {

		@Override public void addTableModelListener(TableModelListener l) {
			// TODO Auto-generated method stub

		}

		@Override public Class<?> getColumnClass(int columnIndex) {
			if (columnClasses != null && columnIndex < columnClasses.length) {
				Class cc = columnClasses[columnIndex];
				if (cc != null)
					return cc;
			}
			if (isEmpty()) {
				if (cellClass != null)
					return cellClass;
				return Object.class;
			}
			return getValueAt(0, columnIndex).getClass();
		}

		@Override public int getColumnCount() {
			if (columnNames == null)
				return 0;
			return columnNames.length;
		}

		@Override public String getColumnName(int columnIndex) {
			return columnNames[columnIndex];
		}

		@Override public int getRowCount() {
			if (isEmpty())
				return 0;
			List list = getRowsFromObject();
			return list.size();
		}

		@Override public Object getValueAt(int rowIndex, int columnIndex) {
			if (isEmpty())
				return WAS_NULL;
			List list = getRowsFromObject();
			Object statement = getRowObject(rowIndex);
			try {
				return getNamedValue(statement, getColumnName(columnIndex));
			} catch (Throwable e) {
				printStackTrace(e);
				return "" + e;
			}
		}

		public Object getRowObject(int rowIndex) {
			return GenericMatrixTable.this.getRowObject(rowIndex);
		}

		@Override public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		private boolean isEmpty() {
			List list = getRowsFromObject();
			return (list == null || list.size() == 0);
		}

		@Override public void removeTableModelListener(TableModelListener l) {
			// TODO Auto-generated method stub

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
				Object recast = convertWas(was, aValue, objNeedsToBe);
				if (was == recast)
					return;

				setNamedValue(statement, namedProp, recast);
			} catch (Throwable e) {
				printStackTrace(e);
			}
		}

		protected Object getNamedValue(Object statement, String namedProp) throws NoSuchConversionException, SecurityException, NoSuchFieldException, Throwable {
			String localName = ".*" + namedProp.toUpperCase() + ".*";
			try {
				Object r = ReflectUtils.getObjectPropertyValue(statement, statement.getClass(), localName, null, false);
				if (r == null)
					return WAS_NULL;
				return r;
			} catch (Throwable e) {
				throw e;
			}
		}

		protected void setNamedValue(Object statement, String named, Object recast) throws NoSuchConversionException, NoSuchFieldException {
			String localName = ".*" + named.toUpperCase() + ".*";
			ReflectUtils.setObjectPropertyValue(statement, statement.getClass(), localName, null, false, recast);
		}

	}

	public Object convertWas(Object was, Object aValue, Class objNeedsToBe) {
		return Utility.recastCC(aValue, objNeedsToBe);
	}

	public ToFromKeyConverter getCellConverter(Class valueClazz) {
		return Utility.getToFromStringConverter(valueClazz);
	}

	public Object getRowObject(int rowIndex) {
		List list = getRowsFromObject();
		return list.get(rowIndex);
	}

	public class TableWithCustomRenderer extends javax.swing.JTable {

		public TableWithCustomRenderer() {
			super();
		}

		private boolean isSelectAllForMouseEvent = false;
		private boolean isSelectAllForActionEvent = false;
		private boolean isSelectAllForKeyEvent = false;

		//
		//  Overridden methods
		//
		/*
		 *  Override to provide Select All editing functionality
		 */
		public boolean editCellAt(int row, int column, EventObject e) {
			boolean result = super.editCellAt(row, column, e);

			if (isSelectAllForMouseEvent || isSelectAllForActionEvent || isSelectAllForKeyEvent) {
				selectAll(e);
			}

			return result;
		}

		/*
		 * Select the text when editing on a text related cell is started
		 */
		private void selectAll(EventObject e) {
			final Component editor = getEditorComponent();

			if (editor == null || !(editor instanceof JTextComponent))
				return;

			if (e == null) {
				((JTextComponent) editor).selectAll();
				return;
			}

			//  Typing in the cell was used to activate the editor

			if (e instanceof KeyEvent && isSelectAllForKeyEvent) {
				((JTextComponent) editor).selectAll();
				return;
			}

			//  F2 was used to activate the editor

			if (e instanceof ActionEvent && isSelectAllForActionEvent) {
				((JTextComponent) editor).selectAll();
				return;
			}

			//  A mouse click was used to activate the editor.
			//  Generally this is a double click and the second mouse click is
			//  passed to the editor which would remove the text selection unless
			//  we use the invokeLater()

			if (e instanceof MouseEvent && isSelectAllForMouseEvent) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						((JTextComponent) editor).selectAll();
					}
				});
			}
		}

		/*
		 *  Sets the Select All property for for all event types
		 */
		public void setSelectAllForEdit(boolean isSelectAllForEdit) {
			setSelectAllForMouseEvent(isSelectAllForEdit);
			setSelectAllForActionEvent(isSelectAllForEdit);
			setSelectAllForKeyEvent(isSelectAllForEdit);
		}

		/*
		 *  Set the Select All property when editing is invoked by the mouse
		 */
		public void setSelectAllForMouseEvent(boolean isSelectAllForMouseEvent) {
			this.isSelectAllForMouseEvent = isSelectAllForMouseEvent;
		}

		/*
		 *  Set the Select All property when editing is invoked by the "F2" key
		 */
		public void setSelectAllForActionEvent(boolean isSelectAllForActionEvent) {
			this.isSelectAllForActionEvent = isSelectAllForActionEvent;
		}

		/*
		 *  Set the Select All property when editing is invoked by
		 *  typing directly into the cell
		 */
		public void setSelectAllForKeyEvent(boolean isSelectAllForKeyEvent) {
			this.isSelectAllForKeyEvent = isSelectAllForKeyEvent;
		}

		//
		//  Static, convenience methods
		//
		/**
		 *  Convenience method to order the table columns of a table. The columns
		 *  are ordered based on the column names specified in the array. If the
		 *  column name is not found then no column is moved. This means you can
		 *  specify a null value to preserve the current order of a given column.
		 *
		 *  @param table        the table containing the columns to be sorted
		 *  @param columnNames  an array containing the column names in the
		 *                      order they should be displayed
		 */
		public void reorderColumns(JTable table, Object... columnNames) {
			TableColumnModel model = table.getColumnModel();

			for (int newIndex = 0; newIndex < columnNames.length; newIndex++) {
				try {
					Object columnName = columnNames[newIndex];
					int index = model.getColumnIndex(columnName);
					model.moveColumn(index, newIndex);
				} catch (IllegalArgumentException e) {
				}
			}
		}

		public TableCellEditor getCellEditor(int row, int column) {
			TableCellEditor render = null;//super.getCellEditor(row, column);
			if (render == null) {
				Class cclz = getColumnClass(column);
				render = new CustomCellEditor(column, cclz, "getCellEditor row=" + row);
			}
			if (render instanceof CustomCellEditor) {
				((CustomCellEditor) render).setEditRowCol(row, column);
			}
			return render;
		}

		public TableCellRenderer getCellRenderer(int row, int column) {
			TableCellRenderer render = super.getCellRenderer(row, column);
			if (render == null) {
				Class cclz = getColumnClass(column);
				render = new CustomCellRenderer(column, cclz, "GetCellRenderer row=" + row);
			}
			return render;
		}

		@Override public Class<?> getColumnClass(int column) {
			return super.getColumnClass(column);
		}

		public TableCellRenderer getColumnRenderer(int i, TableColumn column) {
			TableCellRenderer tcr = column.getCellRenderer();
			if (tcr == null) {
				Class cc = getColumnClass(i);
				if (cc != null) {
					tcr = getDefaultRenderer(cc);
				}
				if (tcr == null) {
					tcr = new CustomCellRenderer(i, cc, column);
				}
				column.setCellRenderer(tcr);
			}
			return tcr;

		}

		public TableCellEditor getDefaultEditor(Class<?> columnClass) {
			TableCellEditor tcr = super.getDefaultEditor(columnClass);
			if (tcr == null) {
				if (columnClass != null) {
					if (!columnClass.isInterface()) {
						for (Class ifc : columnClass.getInterfaces()) {
							tcr = super.getDefaultEditor(columnClass);
							if (tcr != null)
								return tcr;
						}
					}
				}
				return new CustomCellEditor(-1, columnClass, "Editor");
			}
			return tcr;
		}

		public TableCellRenderer getDefaultRenderer(Class<?> columnClass) {
			TableCellRenderer tcr = super.getDefaultRenderer(columnClass);
			if (tcr == null) {
				if (columnClass != null) {
					if (!columnClass.isInterface()) {
						for (Class ifc : columnClass.getInterfaces()) {
							tcr = super.getDefaultRenderer(columnClass);
							if (tcr != null)
								return tcr;
						}
					}
				}
				return new CustomCellRenderer(-1, columnClass, "getDefaultRenderer");
			}
			return tcr;
		}

		@Override public TableUI getUI() {
			return super.getUI();
		}

		/*
		* This method picks good column sizes.
		* If all column heads are wider than the column's cells'
		* contents, then you can just use column.sizeWidthToFit().
		*/
		private void initColumnSizes() {
			JTable table = this;
			TableModel tm = table.getModel();
			if (!(tm instanceof ModelMatrixTableModel))
				return;
			ModelMatrixTableModel model = (ModelMatrixTableModel) tm;
			TableColumn column = null;
			Component comp = null;
			int headerWidth = 0;
			int cellWidth = 0;
			TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();
			int mwidth = model.getColumnCount();
			for (int i = 0; i < mwidth; i++) {
				column = table.getColumnModel().getColumn(i);
				Class cclz = getColumnClass(i);
				setUpSportColumn(table, i, cclz, column);

				comp = headerRenderer.getTableCellRendererComponent(null, column.getHeaderValue(), false, false, 0, 0);
				headerWidth = comp.getPreferredSize().width;

				comp = getColumnRenderer(i, column).getTableCellRendererComponent(table, longValues[i], false, false, 0, i);
				cellWidth = comp.getPreferredSize().width;

				if (false) {
					System.out.println("Initializing width of column " + i + ". " + "headerWidth = " + headerWidth + "; cellWidth = " + cellWidth);
				}
				column.setPreferredWidth(Math.max(headerWidth, cellWidth));

			}
		}

		public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
			Object value = getValueAt(row, column);

			boolean isSelected = false;
			boolean hasFocus = false;

			// Only indicate the selection and focused cell if not printing
			if (!isPaintingForPrint()) {
				isSelected = isCellSelected(row, column);

				boolean rowIsLead = (selectionModel.getLeadSelectionIndex() == row);
				boolean colIsLead = (columnModel.getSelectionModel().getLeadSelectionIndex() == column);

				hasFocus = (rowIsLead && colIsLead) && isFocusOwner();
			}
			if (renderer == null) {
				Class cclz = getColumnClass(column);
				renderer = new CustomCellRenderer(column, cclz, columnModel.getColumn(column));
			}
			return renderer.getTableCellRendererComponent(this, value, isSelected, hasFocus, row, column);
		}

		@Override public void setModel(TableModel dataModel) {

			if (dataModel == null)
				dataModel = new ModelMatrixTableModel();

			if (dataModel != null) {
				super.setModel(dataModel);
				try {
					initColumnSizes();
				} catch (Throwable t) {
					printStackTrace(t);
				}
			}
		}

		public void setUpSportColumn(JTable table, int i, Class clz, TableColumn sportColumn) {
			TableCellRenderer renderer = sportColumn.getCellRenderer();

			CustomCellRenderer r = new CustomCellRenderer(i, clz, sportColumn);

			sportColumn.setCellEditor(r.getEditor());
			if (renderer == null)
				sportColumn.setCellRenderer(r);

			//Set up tool tips for the sport cells.
			renderer = sportColumn.getCellRenderer();
			((JComponent) renderer).setToolTipText("Click for combo box");
			sportColumn.setCellRenderer(renderer);
		}

		@Override public void updateUI() {
			try {
				super.updateUI();
			} catch (Throwable t) {
				printStackTrace(t);
			}
		}
	}

	static Logger theLogger = LoggerFactory.getLogger(GenericMatrixPanel.class);

	@UISalient static public GenericMatrixPanel showGenericMatrixPanel(final Object obj) {
		return new GenericMatrixPanel(obj.getClass()) {
			{
				setObject(obj);
			}
		};
	}

	protected Class<?> cellClass;
	protected Class[] columnClasses;
	protected String[] columnNames;
	protected GetListFromHolder listFromH;
	private List listOfRows;
	public final Object[] longValues = { "Jane", "Kathy", "None of the above", new Integer(20), Boolean.TRUE };

	protected Class matrixClass;

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPanel myActionPanel;
	private javax.swing.JButton myAddButton;
	private javax.swing.JSplitPane myBottomSplitPane;
	private javax.swing.JButton myEditButton;
	private javax.swing.JPanel myFilterPanel;
	private javax.swing.JTextField myQueryField;
	private javax.swing.JButton myRemoveButton;
	private javax.swing.JTable myTable;
	private javax.swing.JScrollPane myTableScrollPane;
	private javax.swing.JSplitPane myTopSplitPane;
	// End of variables declaration//GEN-END:variables

	protected Class rowClass;

	public GenericMatrixTable(Class matrixClass) {
		this(matrixClass, null, null, null);
	}

	/** Creates new form ModelMatrixPanel */
	public GenericMatrixTable(Class matrixClass, Class listClass, GetListFromHolder getList, String[] colNames) {
		super();
		this.matrixClass = matrixClass;
		this.rowClass = listClass;
		this.columnNames = colNames;
		this.listFromH = getList;
	}

	public GenericMatrixTable(WrapperValue wv) {
		this(wv.getObjectClass());
		setObject(wv.reallyGetValue());
	}

	@Override protected void completeSubClassGUI() {
	}

	@Override public void focusOnBox(Box b) {
		setObject(b);
		theLogger.info("Focusing on box: " + b);
	}

	@Override public Class<? extends Map<String, Model>> getClassOfBox() {
		// we only use reflection info
		return null;//(Class<? extends Map<String, Model>>) Map<String, Model>.class;
	}

	public List getRowsFromObject() {
		if (listOfRows != null)
			return listOfRows;
		Object model = getValue();
		if (model == null)
			return null;
		listOfRows = listFromHolder(model);
		return listOfRows;
	}

	public JTable getTable() {
		return myTable;
	}

	public void addRowComponent(Component view) {
		// TODO Auto-generated method stub		
	}

	public void addRowObject(Object obj) {
		List list = getList();
		list.add(obj);
		invalidate();
	}

	public List getList() {
		return getRowsFromObject();
	}

	public String getTextName(Object value, int edit_row, int edit_col) {
		String str = Utility.getUniqueName(value);
		return str;
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		myTopSplitPane = new javax.swing.JSplitPane();
		myBottomSplitPane = new javax.swing.JSplitPane();
		myTableScrollPane = new javax.swing.JScrollPane();
		myTable = new TableWithCustomRenderer();
		myActionPanel = new javax.swing.JPanel();
		myAddButton = new javax.swing.JButton();
		myEditButton = new javax.swing.JButton();
		myRemoveButton = new javax.swing.JButton();
		myFilterPanel = new javax.swing.JPanel();
		myQueryField = new javax.swing.JTextField();

		setLayout(new java.awt.BorderLayout());

		myTopSplitPane.setDividerLocation(60);
		myTopSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

		myBottomSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

		//myTable.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {}, new String[] { "Subject", "Predicate", "Object", "Model" }));
		myTableScrollPane.setViewportView(myTable);

		myBottomSplitPane.setTopComponent(myTableScrollPane);

		myAddButton.setText("add");
		myAddButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				myAddButtonActionPerformed(evt);
			}
		});
		myActionPanel.add(myAddButton);

		myEditButton.setText("save");
		myEditButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				myEditButtonActionPerformed(evt);
			}
		});
		myActionPanel.add(myEditButton);

		myRemoveButton.setText("remove");
		myActionPanel.add(myRemoveButton);

		myBottomSplitPane.setBottomComponent(myActionPanel);

		myTopSplitPane.setBottomComponent(myBottomSplitPane);

		myQueryField.setText("query");
		myQueryField.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				myQueryFieldActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout myFilterPanelLayout = new javax.swing.GroupLayout(myFilterPanel);
		myFilterPanel.setLayout(myFilterPanelLayout);
		myFilterPanelLayout.setHorizontalGroup(myFilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				myFilterPanelLayout.createSequentialGroup().addContainerGap()
						.addComponent(myQueryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(31, Short.MAX_VALUE)));
		myFilterPanelLayout.setVerticalGroup(myFilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				javax.swing.GroupLayout.Alignment.TRAILING,
				myFilterPanelLayout.createSequentialGroup().addContainerGap(52, Short.MAX_VALUE)
						.addComponent(myQueryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(28, 28, 28)));

		myTopSplitPane.setTopComponent(myFilterPanel);

		add(myTopSplitPane, java.awt.BorderLayout.CENTER);
	}// </editor-fold>//GEN-END:initComponents

	@Override protected void initSubclassGUI() throws Throwable {
		initComponents();
		Utility.makeTablePopupHandler(myTable);
	}

	@Override public boolean isObjectBoundGUI() {
		return true;
	}

	protected List listFromHolder(Object o) {
		if (listFromH != null) {
			return listFromH.listFromHolder(o);
		}
		return null;
	}

	private void myAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myAddButtonActionPerformed
		// TODO add your handling code here:
	}//GEN-LAST:event_myAddButtonActionPerformed

	private void myEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myEditButtonActionPerformed
		// TODO add your handling code here:
	}//GEN-LAST:event_myEditButtonActionPerformed

	private void myQueryFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myQueryFieldActionPerformed
		// TODO add your handling code here:
	}//GEN-LAST:event_myQueryFieldActionPerformed

	@Override public void objectValueChanged(Object oldValue, Object newValue) {
		try {
			reloadObjectGUI(newValue);
		} catch (Throwable e) {
			printStackTrace(e);
		}
	}

	@Override protected boolean reloadObjectGUI(Object obj) throws Throwable {
		this.objectValue = obj;
		myTable.setModel(new ModelMatrixTableModel());
		return true;
	}

	// means we render per cell instead
	public boolean isRenderPerRow() {
		return false;
	}
}
