package org.appdapter.gui.trigger;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.appdapter.gui.box.AbstractScreenBoxTreeNodeImpl;
import org.appdapter.gui.browse.Utility;

public class TriggerMouseAdapter extends MouseAdapter {

	public void mouseClicked(MouseEvent e) {
		mouseEvent(e);
	}

	public void mousePressed(MouseEvent e) {
		mouseEvent(e);
	}

	public void mouseReleased(MouseEvent e) {
		mouseEvent(e);
	}

	public void mouseEvent(MouseEvent e) {
		if (e.isConsumed())
			return;
		synchronized (e) {
			// so popup menue event wont overlap
			mouseEvent0(e);
		}
	}

	public void mouseEvent0(MouseEvent e) {
		Object source = e.getSource();
		if (source instanceof JTable) {
			mouseEvent(e, (JTable) source);
		} else if (source instanceof JTree) {
			mouseEvent(e, (JTree) source);
		} else {
			TriggerMenuFactory.theLogger.trace("Click on " + source.getClass() + " " + source);
			if (e.isPopupTrigger() && !e.isConsumed()) {
				TriggerMenuFactory.buildPopupMenuAndShow(e, true, source, e.getComponent(), e);
			}
		}

	}

	public void mouseEvent(MouseEvent e, JTable source) {
		int row = source.rowAtPoint(e.getPoint());
		int column = source.columnAtPoint(e.getPoint());

		if (!source.isRowSelected(row)) {
			source.changeSelection(row, column, false, false);
		}

		if (e.isPopupTrigger() && !e.isConsumed()) {
			int columns = source.getColumnCount();
			Object cellSubBox = source.getValueAt(row, column);
			Object rowObject = null;
			TableModel tm = source.getModel();
			if (tm instanceof ListModel) {
				ListModel lm = (ListModel) tm;
				rowObject = lm.getElementAt(row);
			}

			if (e.isShiftDown() || columns < 2) {
				rowObject = Utility.dref(cellSubBox);
			}
			TriggerMenuFactory.buildPopupMenuAndShow(e, true, cellSubBox, rowObject);
		}
	}

	public void mouseEvent(MouseEvent e, JTree tree) {
		int x = e.getX();
		int y = e.getY();
		TreePath path = tree.getPathForLocation(x, y);
		if (path == null) {
			return;
		}
		tree.setSelectionPath(path);
		if (e.isPopupTrigger() && !e.isConsumed()) {

			// Nodes are not *required* to implement TreeNode
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
			Object uo = treeNode.getUserObject();
			JPopupMenu popup = TriggerMenuFactory.buildPopupMenuAndShow(e, true, uo, treeNode);
		}
	}
}