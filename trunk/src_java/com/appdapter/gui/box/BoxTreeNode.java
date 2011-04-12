/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.gui.box;

import com.appdapter.gui.browse.BrowsePanel;
import com.appdapter.gui.browse.DisplayContext;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author winston
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
