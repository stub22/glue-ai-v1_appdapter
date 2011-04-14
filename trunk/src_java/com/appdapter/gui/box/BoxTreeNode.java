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
package com.appdapter.gui.box;

import com.appdapter.gui.browse.BrowsePanel;
import com.appdapter.gui.browse.DisplayContext;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BoxTreeNode extends DefaultMutableTreeNode {
	private		DisplayContext		myDisplayContext;

	public BoxTreeNode() {
		super();
	}
	public BoxTreeNode(Box box) {
		super(box);
	}
	public BoxTreeNode(Box box, boolean allowsChildren) {
		super(box, allowsChildren);
	}
	public void setDisplayContext(DisplayContext dc) {
		myDisplayContext = dc;
	}
	public DisplayContext getDisplayContext() {
		return myDisplayContext;
	}
	protected DisplayContext findDisplayContext() {
		DisplayContext foundDC = getDisplayContext();
		if (foundDC == null) {
			BoxTreeNode parentNode = (BoxTreeNode) getParent();
			if (parentNode != null) {
				foundDC = parentNode.findDisplayContext();
			}
		}
		return foundDC;
	}
	public Box getBox() {
		return (Box) getUserObject();
	}
	public BoxTreeNode findDescendantNodeForBox(Box b) {
		if (b == getUserObject()) {
			return this;
		} else {
			int childCount = getChildCount();
			for (int i=0; i < childCount; i++) {
				BoxTreeNode childNode = (BoxTreeNode) getChildAt(i);
				BoxTreeNode matchNode = childNode.findDescendantNodeForBox(b);
				if (matchNode != null) {
					return matchNode;
				}
			}
		}
		return null;
	}
}
