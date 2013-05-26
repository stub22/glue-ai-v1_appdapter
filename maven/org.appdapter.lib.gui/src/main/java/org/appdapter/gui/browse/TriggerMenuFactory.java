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

package org.appdapter.gui.browse;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.MenuElement;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.core.component.KnownComponent;
import org.appdapter.gui.pojo.UIAware;
import org.appdapter.gui.util.Debuggable;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class TriggerMenuFactory<TT extends Trigger<Box<TT>> & KnownComponent> {

	public class TriggerSorter implements Comparator<TT> {

		@Override public int compare(TT o1, TT o2) {
			return getTriggerSortName(o1).compareTo(getTriggerSortName(o2));
		}
	}

	private String getTriggerSortName(TT t) {
		// TODO Auto-generated method stub
		String[] tn = getTriggerName(t).split(" ");
		return tn[tn.length - 1];
	}

	public MouseAdapter makePopupMouseAdapter() {
		MouseAdapter ma = new MouseAdapter() {

			private void requestContextPopup(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				JTree tree = (JTree) e.getSource();
				TreePath path = tree.getPathForLocation(x, y);
				if (path == null) {
					return;
				}
				tree.setSelectionPath(path);

				// Nodes are not *required* to implement TreeNode
				DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
				Box<TT> box = (Box<TT>) treeNode.getUserObject();

				// String label = "popup: " + obj.toString(); // obj.getTreeLabel();
				JPopupMenu popup = buildPopupMenu(box);

				popup.show(tree, x, y);
			}

			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					requestContextPopup(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					requestContextPopup(e);
				}
			}
		};
		return ma;
	}

	public JPopupMenu buildPopupMenu(Box<TT> box) {
		JPopupMenu popup = new JPopupMenu();
		if (box instanceof UIAware) {
			((UIAware) box).visitComponent(popup);
		}
		List<TT> trigs = new ArrayList<TT>(box.getTriggers());
		Collections.sort(trigs, new TriggerSorter());

		for (TT trig : trigs) {
			addTriggerToPoppup(popup, box, trig);
		}
		return popup;
	}

	private void addTriggerToPoppup(Container popup, Box<TT> box, TT trig) {
		String[] path = getTriggerPath(trig);
		addTriggerToPoppup(popup, box, path, 0, trig);
	}

	static public class JMenuWithPath extends JMenu {
		ArrayList<Component> mcomps = new ArrayList<Component>();

		public JMenuWithPath(String lbl) {
			super(lbl);
		}

		@Override public JMenuItem add(JMenuItem c) {
			ensureUnequelyNamed(c);
			JMenuItem r = super.add(c);
			ensureFoundNamed(c);
			return r;
		}

		@Override public Component add(Component c, int index) {
			ensureUnequelyNamed(c);
			mcomps.add(index, c);
			Component r = super.add(c, index);
			ensureFoundNamed(c);
			return r;
		}

		@Override public Component[] getComponents() {
			if (true)
				return mcomps.toArray(new Component[mcomps.size()]);
			return super.getMenuComponents();
		}

		private void ensureFoundNamed(Component c) {
			String fnd = TriggerMenuFactory.getLabel(c, 1);
			Component found = findChildNamed(this, true, fnd);
			if (found != null) {
				return;
			}
			found = findChildNamed(this, true, fnd);
			Debuggable.mustBeSameStrings("found=" + found, fnd);
		}

		private void ensureUnequelyNamed(Component c) {
			String fnd = TriggerMenuFactory.getLabel(c, 1);
			Component found = findChildNamed(this, true, fnd);
			if (found == null) {
				Component p = getParent();
				return;
			}
			Debuggable.mustBeSameStrings("found=" + found, fnd);
		}

		@Override public void removeAll() {
			// TODO Auto-generated method stub
			mcomps.clear();
			super.removeAll();
		}

		@Override public Component add(Component c) {
			ensureUnequelyNamed(c);
			Component r = super.add(c);
			mcomps.add(c);
			ensureFoundNamed(c);
			return r;
		}

		@Override public String getText() {
			return super.getText();
		}

		@Override public String toString() {
			Component p = getParent();//.toString();
			if (p != null) {
				return "" + TriggerMenuFactory.getLabel(p, 1) + "->" + TriggerMenuFactory.getLabel(this, 1);
			}
			return TriggerMenuFactory.getLabel(this, 1);
		}
	}

	private void addTriggerToPoppup(Container popup, Box<TT> box, String[] path, int idx, TT trig) {
		final String lbl = path[idx].trim();
		if (path.length - idx == 1) {
			popup.add(makeMenuItem(box, lbl, trig));
			return;
		}
		Component child = findChildNamed(popup, true, lbl.toLowerCase());
		if (child == null) {
			JMenuWithPath item = new JMenuWithPath(lbl);
			popup.add(item, 0);
			//addSeparatorIfNeeded(popup, 1);
			child = item;
		}
		addTriggerToPoppup((Container) child, box, path, idx + 1, trig);
	}

	static void addSeparatorIfNeeded(Container popup, int greaterThan) {
		if (popup.getComponentCount() > greaterThan) {
			if (popup instanceof JMenu) {
				JMenu jmenu = (JMenu) popup;
				jmenu.addSeparator();
				return;
			}
			if (popup instanceof JPopupMenu) {
				JPopupMenu jmenu = (JPopupMenu) popup;
				jmenu.addSeparator();
				return;
			}
		}
	}

	static Component findChildNamed(Container popup, boolean toLowerCase, Comparable<String> fnd) {
		Component[] comps = childrenOf(popup);
		if (comps == null || comps.length == 0)
			return null;
		Component c2 = null;
		for (Component c : comps) {
			String name = getLabel(c, 1);
			if (name == null)
				continue;
			if (toLowerCase) {
				name = name.toLowerCase();
			}
			if (fnd.compareTo(name) == 0)
				return c;
			String name2 = c.getName();
			if (name2 != null) {
				if (fnd.compareTo(name2) == 0)
					c2 = c;
			}
		}
		return c2;
	}

	private static Component[] childrenOf(Container popup) {
		if (popup instanceof JMenu)
			return ((JMenu) popup).getMenuComponents();
		return popup.getComponents();
	}

	public static String getLabel(Component c, int maxDepth) {

		if (c instanceof JPopupMenu) {
			return ((JPopupMenu) c).getLabel();
		}
		if (c instanceof JMenu) {
			return ((JMenu) c).getText();
		}
		if (c instanceof JMenuItem) {
			return ((JMenuItem) c).getText();
		}
		if (c instanceof JLabel) {
			return ((JLabel) c).getText();
		}
		if (c instanceof AbstractButton) {
			return ((AbstractButton) c).getText();
		}
		if (c instanceof JTextComponent) {
			return ((JTextComponent) c).getText();
		}
		if (c instanceof MenuElement) {
			Component c2 = ((MenuElement) c).getComponent();
			if (c != c2) {
				String text = getLabel(c2, maxDepth);
				if (text != null)
					return text;
			}
		}
		if (maxDepth <= 0)
			return null;

		if (c instanceof Container) {
			for (Component c2 : ((Container) c).getComponents()) {
				String text = getLabel(c2, maxDepth - 1);
				if (text != null)
					return text;
			}
		}
		return null;
	}

	public JMenuItem makeMenuItem(final Box<TT> b, String lbl, final TT trig) {
		String path[] = getTriggerPath(trig);
		JMenuItem jmi = new JMenuItem(getTriggerName(trig));
		if (trig instanceof UIAware) {
			((UIAware) trig).visitComponent(jmi);
		}
		jmi.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				trig.fire(b);
			}
		});
		return jmi;
	}

	private String getTriggerName(TT trig) {
		String[] path = getTriggerPath(trig);
		if (path == null || path.length == 0)
			return "" + trig.getShortLabel();
		return path[path.length - 1].trim();
	}

	private String[] getTriggerPath(TT trig) {
		return trig.getShortLabel().split("\\|");
	}
}
