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
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.appdapter.api.trigger.Box;
import org.appdapter.gui.box.BoxPanelSwitchableView;
import org.appdapter.gui.box.ScreenBoxTreeNode;
import org.appdapter.gui.box.UIProvider;
import org.appdapter.gui.pojo.DisplayType;

/**
 * @author Stu B. <www.texpedient.com>
 */
abstract public class AbstractScreenBoxTreeNodeImpl extends DefaultMutableTreeNode implements UIProvider, DisplayContextProvider, DisplayContext {
	private DisplayContext myDisplayContext;
	private Component myTreeComponent;

	public AbstractScreenBoxTreeNodeImpl() {
		super();
	}

	public AbstractScreenBoxTreeNodeImpl(Box box) {
		super(box);
	}

	public AbstractScreenBoxTreeNodeImpl(Box box, boolean allowsChildren) {
		super(box, allowsChildren);
	}

	final public DisplayContext getDisplayContext() {
		DisplayContext foundDC = myDisplayContext;
		if (foundDC == null) {

			ScreenBoxTreeNode parentNode = getScreenBoxTreeParent();
			if (parentNode != null) {
				foundDC = parentNode.getDisplayContext();
			}
		}
		return foundDC;
	}

	/* (non-Javadoc)
	 * @see org.appdapter.gui.box.DisplayNode#getBox()
	 */
	final public Box getBox() {
		return (Box) getUserObject();
	}

	/* (non-Javadoc)
	 * @see org.appdapter.gui.box.DisplayNode#findDescendantNodeForBox(org.appdapter.api.trigger.Box)
	 */
	final public DisplayContext findDisplayContextFromTree(Box b) {
		if (b == getUserObject()) {
			return this;
		} else {
			int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				AbstractScreenBoxTreeNodeImpl childNode = (AbstractScreenBoxTreeNodeImpl) getChildAt(i);
				DisplayContext matchNode = childNode.findDisplayContext(b);
				if (matchNode != null) {
					return matchNode;
				}
			}
		}
		return null;
	}

	final @Override public void add(MutableTreeNode childNode) {
		super.add((MutableTreeNode) childNode);

	}

	final public ScreenBoxTreeNodeImpl getScreenBoxTreeParent() {
		Object parent = getParent();
		if (parent == null || parent instanceof ScreenBoxTreeNode)
			return (ScreenBoxTreeNodeImpl) parent;
		return null;
	}

	final public void setDisplayContext(DisplayContext dc) {
		myDisplayContext = dc;
	}
}