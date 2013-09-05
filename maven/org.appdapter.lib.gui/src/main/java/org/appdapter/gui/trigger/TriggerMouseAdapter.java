package org.appdapter.gui.trigger;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.accessibility.Accessible;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.table.TableModel;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.appdapter.api.trigger.CallableWithParameters;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.swing.ObjectView;
import org.appdapter.gui.swing.SmallObjectView;

import com.jidesoft.swing.JideTabbedPane;
import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;

public class TriggerMouseAdapter extends MouseAdapter implements PopupMenuListener {

	static Object installKBListenerLock = new Object();
	static boolean installKBListener = false;

	static {
		installMouseListeners();
	}

	public static void installMouseListeners() {
		if (installKBListener)
			return;
		synchronized (installKBListenerLock) {
			if (installKBListener)
				return;
			installKBListener = true;
		}
		KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		focusManager.addPropertyChangeListener("permanentFocusOwner", new PropertyChangeListener() {
			@Override public void propertyChange(PropertyChangeEvent evt) {
				TriggerMouseAdapter.noticeComponent(evt.getNewValue());
			}
		});
	}

	public static class CutPasteMouseAdapter extends MouseAdapter {

		@Override public void mousePressed(MouseEvent e) {
			doPopup(e);
		}

		@Override public void mouseReleased(MouseEvent e) {
			doPopup(e);
		}

		synchronized public void doPopup(final MouseEvent e) {
			Component c = e.getComponent();
			if (c instanceof JTextComponent) {
				boolean noneHandled = false;
				boolean someoneMightHandle = false;
				if (!e.isConsumed() && e.isPopupTrigger()) {
					// popup 
					int ourIndex = -1;
					int index = -1;
					for (MouseListener ma : e.getComponent().getMouseListeners()) {
						index++;
						if (ma == this) {
							ourIndex = index;
							continue;
						}
						if (ma instanceof TriggerMouseAdapter)
							return;
						if (ma instanceof MouseAdapter) {
							if (ourIndex == -1) {
								noneHandled = true;
							} else {
								someoneMightHandle = true;
								// move ourselves to the end of list
								c.removeMouseListener(this);
								c.addMouseListener(this);
							}
						}
					}
					if (someoneMightHandle) {
						return;
					}
					e.consume();
					final JTextComponent component = (JTextComponent) e.getComponent();
					final JPopupMenu menu = new JPopupMenu();
					JMenuItem item;
					item = new JMenuItem(new DefaultEditorKit.CopyAction());
					item.setText("Copy");
					item.setEnabled(component.getSelectionStart() != component.getSelectionEnd());
					item.getAction().putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl C"));
					menu.add(item);
					item = new JMenuItem(new DefaultEditorKit.CutAction());
					item.setText("Cut");
					item.setEnabled(component.isEditable() && component.getSelectionStart() != component.getSelectionEnd());
					item.getAction().putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl X"));
					menu.add(item);
					item = new JMenuItem(new DefaultEditorKit.PasteAction());
					item.setText("Paste");
					item.setEnabled(component.isEditable());
					item.getAction().putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl V"));
					menu.add(item);
					menu.addSeparator();
					menu.add(new AbstractAction("Select All") {
						{
							putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl A"));
						}

						public void actionPerformed(ActionEvent e) {
							component.selectAll();
						}

						public boolean isEnabled() {
							return component.isEnabled() && component.getText().length() > 0;
						}
					});

					menu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		}
	}

	static public void installContextMenu(Component c) {
		if (c instanceof JTextComponent) {
			c.addMouseListener(new CutPasteMouseAdapter());
		} else if (c instanceof Container)
			for (Component c1 : ((Container) c).getComponents()) {
				installContextMenu(c1);
			}
	}

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
			if (ma != null) {
				comp.removeMouseListener(ma);
			}
			ma = new TriggerMouseAdapter(comp);
			comp2adapter.put(comp, ma);
			comp.addMouseListener(ma);
			ma.setObject(represented);
			return ma;
		}
	}

	private void setObject(Object represented) {
		if (attachedTo == represented)
			return;
		if (!(attachedToComponent instanceof Component)) {
			Utility.bug(this, "attachedToComponent", attachedTo);
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
		comp.removeMouseListener(this);
		comp.addMouseListener(this);
		installContextMenu(comp);
		addWorkarrounds(comp);
	}

	boolean addWorkarrounds(Component comp) {
		if (comp instanceof JComboBox) {
			JComboBox combo = (JComboBox) comp;
			addWorkarrounds(combo);
			return true;
		}
		return false;
	}

	@Override public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
		EventQueue.invokeLater(new Runnable() {
			@Override public void run() {
				JComboBox combo = (JComboBox) e.getSource();
				Accessible a = combo.getAccessibleContext().getAccessibleChild(0);
				//Or Accessible a = combo.getUI().getAccessibleChild(combo, 0);
				if (a instanceof BasicComboPopup) {
					BasicComboPopup pop = (BasicComboPopup) a;
					Point p = new Point(combo.getSize().width, 0);
					SwingUtilities.convertPointToScreen(p, combo);
					pop.setLocation(p);
				}
			}
		});
	}

	@Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
	}

	@Override public void popupMenuCanceled(PopupMenuEvent e) {
	}

	private static ImageIcon makeRolloverIcon(ImageIcon srcIcon) {
		RescaleOp op = new RescaleOp(new float[] { 1.2f, 1.2f, 1.2f, 1.0f }, new float[] { 0f, 0f, 0f, 0f }, null);
		BufferedImage img = new BufferedImage(srcIcon.getIconWidth(), srcIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = img.getGraphics();
		//g.drawImage(srcIcon.getImage(), 0, 0, null);
		srcIcon.paintIcon(null, g, 0, 0);
		g.dispose();
		return new ImageIcon(op.filter(img, null));
	}

	//private final ImageIcon icon = 
	private CallableWithParameters doubleClickCall;

	static ImageIcon icon = null;

	boolean addWorkarrounds(JComboBox combo) {
		if (combo.getUI() instanceof WindowsComboBoxUI) {
			combo.setUI(new WindowsComboBoxUI() {

				@Override protected JButton createArrowButton() {

					if (icon == null) {
						try {
							icon = new ImageIcon(getClass().getResource("14x14.png"));
						} catch (Exception e) {

						}
					}
					JButton button = new JButton(icon) {
						@Override public Dimension getPreferredSize() {
							return new Dimension(14, 14);
						}
					};
					button.setRolloverIcon(makeRolloverIcon(icon));
					button.setFocusPainted(false);
					button.setContentAreaFilled(false);
					return button;
				}
			});
		} else {
			combo.setUI(new BasicComboBoxUI() {
				@Override protected JButton createArrowButton() {
					JButton button = super.createArrowButton();
					((BasicArrowButton) button).setDirection(SwingConstants.EAST);
					return button;
				}
			});
		}
		combo.addPopupMenuListener(this);
		return true;
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
			if (e.getClickCount() > 1 && doubleClickCall != null) {
				if (source != null) {
					doubleClickCall.call(source);
				}
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

	public void setDoubleClick(String string, CallableWithParameters o) {
		this.doubleClickCall = o;

	}

	public static void noticeComponent(Object newValue) {

		if (newValue instanceof JTextComponent) {
			JTextComponent comp = (JTextComponent) newValue;
			MouseListener[] ml = comp.getMouseListeners();
			boolean hasRightClickListener = false;
			for (int i = 0; i < ml.length; i++) {
				if (ml[i] instanceof CutPasteMouseAdapter) {
					hasRightClickListener = true;
					break;
				}
			}

			if (!hasRightClickListener) {
				comp.addMouseListener(new CutPasteMouseAdapter());
			}
		}
	}

}