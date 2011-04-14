/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.com).
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

package com.appdapter.gui.browse;

import com.appdapter.gui.box.Box;
import com.appdapter.gui.box.BoxImpl;
import com.appdapter.gui.box.KnownComponent;
import com.appdapter.gui.box.Trigger;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class TriggerMenuFactory<TT extends Trigger<Box<TT>> & KnownComponent> {

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
		for (TT trig : box.getTriggers()) {
			popup.add(makeMenuItem(box, trig));
		}
		return popup;
	}

	public JMenuItem makeMenuItem(final Box<TT> b, final TT trig) {
		JMenuItem jmi = new JMenuItem(trig.getShortLabel());
		jmi.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				trig.fire(b);
			}
		});
		return jmi;
	}
}
