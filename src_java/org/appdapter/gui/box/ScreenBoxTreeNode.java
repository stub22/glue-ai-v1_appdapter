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
package org.appdapter.gui.box;

import org.appdapter.api.trigger.Box;
import org.appdapter.gui.browse.BrowsePanel;
import org.appdapter.gui.browse.DisplayContext;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class ScreenBoxTreeNode extends DefaultMutableTreeNode implements DisplayNode {
	private		DisplayContext		myDisplayContext;

	
	public ScreenBoxTreeNode() {
		super();
	}
	public ScreenBoxTreeNode(Box box) {
		super(box);
	}
	public ScreenBoxTreeNode(Box box, boolean allowsChildren) {
		super(box, allowsChildren);
	}
	/* (non-Javadoc)
	 * @see org.appdapter.gui.box.DisplayNode#setDisplayContext(org.appdapter.gui.browse.DisplayContext)
	 */
	@Override
	public void setDisplayContext(DisplayContext dc) {
		myDisplayContext = dc;
	}
	/* (non-Javadoc)
	 * @see org.appdapter.gui.box.DisplayNode#getDisplayContext()
	 */
	@Override
	public DisplayContext getDisplayContext() {
		return myDisplayContext;
	}
	public DisplayContext findDisplayContext() {
		DisplayContext foundDC = getDisplayContext();
		if (foundDC == null) {
			DisplayNode parentNode = (DisplayNode) getParent();
			if (parentNode != null) {
				foundDC = parentNode.findDisplayContext();
			}
		}
		return foundDC;
	}
	/* (non-Javadoc)
	 * @see org.appdapter.gui.box.DisplayNode#getBox()
	 */
	@Override
	public Box getBox() {
		return (Box) getUserObject();
	}
	/* (non-Javadoc)
	 * @see org.appdapter.gui.box.DisplayNode#findDescendantNodeForBox(org.appdapter.api.trigger.Box)
	 */
	@Override
	public DisplayNode findDescendantNodeForBox(Box b) {
		if (b == getUserObject()) {
			return this;
		} else {
			int childCount = getChildCount();
			for (int i=0; i < childCount; i++) {
				DisplayNode childNode = (DisplayNode) getChildAt(i);
				DisplayNode matchNode = childNode.findDescendantNodeForBox(b);
				if (matchNode != null) {
					return matchNode;
				}
			}
		}
		return null;
	}
	@Override public void add(DisplayNode childNode) {
		super.add((MutableTreeNode) childNode);
		
	}
}
