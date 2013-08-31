package org.appdapter.gui.trigger;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.appdapter.api.trigger.CallableWithParameters;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.swing.ObjectView;
import org.appdapter.gui.swing.SmallObjectView;

import com.jidesoft.swing.JideTabbedPane;

public class TriggerMouseAdapter extends MouseAdapter {

	static Map<Component, TriggerMouseAdapter> comp2adapter = new HashMap();

	public static TriggerMouseAdapter installMouseAdapter(Component goc) {
		synchronized (comp2adapter) {
			Component comp = (Component) goc;
			TriggerMouseAdapter ma = comp2adapter.get(comp);
			if (ma == null) {
				ma = new TriggerMouseAdapter(comp);
				comp.removeMouseListener(ma);
				comp.addMouseListener(ma);
			}
			return ma;
		}
	}

	public static TriggerMouseAdapter installMouseAdapter(Component comp, Object represented) {
		synchronized (comp2adapter) {
			TriggerMouseAdapter ma = comp2adapter.get(comp);
			if (ma == null) {
				ma = new TriggerMouseAdapter(comp);
				comp.removeMouseListener(ma);
				comp.addMouseListener(ma);
			}
			ma.setObject(represented);
			return ma;
		}
	}

	private void setObject(Object represented) {
		if (attachedTo == represented)
			return;
		if (!(attachedTo instanceof Component)) {
			Utility.bug(this, "attathedTo", attachedTo);
		}
		attachedTo = represented;
	}

	public Object getObject() {
		Object source = attachedTo;
		if (!(source instanceof Component)) {
			Object source2 = getComponent();
			if (source2 != null)
				source = source2;
		}
		if (!(source instanceof Component)) {
			source = attachedTo;
		}
		if (source instanceof Component) {
			ObjectView host = (ObjectView) SwingUtilities.getAncestorOfClass(ObjectView.class, (Component) source);
			if (host != null) {
				Object source2 = host.getValue();
				if (source2 != null)
					source = source2;
			}
		}
		return source;
	}

	private Object getComponent() {
		return attachedToComponent;
	}

	private Object attachedTo;
	private Object attachedToComponent;

	private TriggerMouseAdapter(Component comp) {
		attachedToComponent = comp;
		synchronized (comp2adapter) {
			comp2adapter.put(comp, this);
		}
	}

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
			// so popup menu events wont overlap
			mouseEvent0(e);
		}
	}

	public void mouseEvent0(MouseEvent e) {
		Object source = e.getSource();
		if (source instanceof JTable) {
			mouseEvent(e, (JTable) source);
		} else if (source instanceof JTree) {
			mouseEvent(e, (JTree) source);
			return;
		}

		if (e.isConsumed())
			return;

		if (!(source instanceof Component)) {
			Object source2 = e.getComponent();
			if (source2 != null)
				source = source2;
		}
		if (!(source instanceof Component)) {
			source = attachedTo;
		}
		if (source instanceof Component) {
			ObjectView host = (ObjectView) SwingUtilities.getAncestorOfClass(ObjectView.class, (Component) source);
			if (host != null) {
				Object source2 = host.getValue();
				if (source2 != null)
					source = source2;
			}
		}
		if (attachedTo == attachedToComponent) {
			attachedTo = null;
		}
		if (attachedTo == null) {
			attachedTo = getObject();
		}
		if (attachedTo == attachedToComponent) {
			attachedTo = null;
		}
		if (attachedTo != null) {
			source = attachedTo;
		}
		if (e.isPopupTrigger() && !e.isConsumed()) {
			String cn = Utility.getUniqueNamePretty(source);
			Component c = e.getComponent();
			TriggerMenuFactory.theLogger.warn("isPopupTrigger " + cn + "\n-" + e);
			TriggerMenuFactory.buildPopupMenuAndShow(e, true, source, c, e);
			e.consume();
		} else {
			String cn = Utility.getUniqueNamePretty(source);
			Component c = e.getComponent();
			c.setVisible(true);
			Component cp = c.getParent();
			if (cp instanceof JTabbedPane) {
				JTabbedPane jtp = (JTabbedPane) cp;
				jtp.setSelectedComponent(c);
			}
			TriggerMenuFactory.theLogger.trace("Click " + cn + "\n-" + e);
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
			e.consume();
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
			e.consume();
		}
	}

	public void addToComponent(JideTabbedPane c, boolean andChildren) {
		final MouseAdapter ma = this;
		updateComponentTreeUI(c, andChildren, 0, new CallableWithParameters<Component, Component>() {
			@Override public Component call(Component box, Object... params) {
				box.removeMouseListener(ma);
				box.addMouseListener(ma);
				return box;
			}
		});
	}

	private static void updateComponentTreeUI(Component c, boolean getChildren, int depth, CallableWithParameters<Component, Component> defaults) {
		boolean getMenu = false;

		if (c instanceof SmallObjectView && depth > 0)
			return;
		try {
			if (c instanceof JideTabbedPane) {
				c = defaults.call(c);
			} else {
				c = defaults.call(c);
			}
			if (getMenu) {
				if (c instanceof JComponent) {
					JComponent jc = (JComponent) c;
					JPopupMenu jpm = jc.getComponentPopupMenu();
					if (jpm != null /*&& jpm.isVisible() && jpm.getInvoker() == jc*/) {
						updateComponentTreeUI(jpm, getChildren, depth + 1, defaults);
					}
				}
			}
		} catch (Throwable t) {

		}

		Component[] children = null;
		if (c instanceof JMenu) {
			children = ((JMenu) c).getMenuComponents();
		} else if (c instanceof Container) {
			children = ((Container) c).getComponents();
		}
		if (children != null && getChildren) {
			for (int i = 0; i < children.length; i++) {
				updateComponentTreeUI(children[i], getChildren, depth + 1, defaults);
			}
		}
	}
}