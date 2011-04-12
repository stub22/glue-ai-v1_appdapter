/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.gui.box;

import com.appdapter.gui.browse.DisplayContext;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

/**
 * @author winston
 */
public class BoxContextImpl implements BoxContext, DisplayContextProvider {
	private	BoxTreeNode			myRootNode;
	private	DefaultTreeModel	myTreeModel;
	public BoxContextImpl() { }
	private void setRootNode(BoxTreeNode rootNode) {
		myRootNode = rootNode;
	}
	private BoxTreeNode findNodeForBox(Box b) {
		return myRootNode.findDescendantNodeForBox(b);
	}
	public Box getParentBox(Box child) {
		BoxTreeNode childNode = findNodeForBox(child);
		return ((BoxTreeNode) childNode.getParent()).getBox();
	}
	public List<Box> getOpenChildBoxes(Box parent) {
		List<Box> results = new ArrayList<Box>();
		BoxTreeNode parentNode = findNodeForBox(parent);
		Enumeration childNodeEnum = parentNode.children();
		while (childNodeEnum.hasMoreElements()) {
			BoxTreeNode btn = (BoxTreeNode) childNodeEnum.nextElement();
			Box childBox = btn.getBox();
			results.add(childBox);
		}
		return results;
	}
	public	DisplayContext	findDisplayContext(Box viewable) {
		BoxTreeNode btn = findNodeForBox(viewable);
		return btn.findDisplayContext();
	}
	public void contextualizeAndAttachRootBox(MutableBox rootBox) {
		BoxTreeNode rootNode = new BoxTreeNode(rootBox);
		setRootNode(rootNode);
		rootBox.setContext(this);
		rootBox.setDisplayContextProvider(this);
	}
	private BoxTreeNode attachChildBoxNode(BoxTreeNode parentNode, Box childBox) {
		//  childBox should already have context(==this) and displayContext.
		if (childBox.getBoxContext() != this) {
			throw new RuntimeException("Refusing to attach a childBox[" + childBox + "] which is not in this context [" + this + "]");
		}
		BoxTreeNode childNode = new BoxTreeNode(childBox);
		parentNode.add(childNode);
		if (myTreeModel != null) {
			myTreeModel.reload(parentNode);
		}
		return childNode;
	}
	public void contextualizeAndAttachChildBox(Box<?> parentBox, MutableBox<?> childBox) {
		BoxTreeNode parentNode = findNodeForBox(parentBox);
		childBox.setContext(this);
		BoxTreeNode childNode = attachChildBoxNode(parentNode, childBox);
		childBox.setDisplayContextProvider(this);
	}
	public TreeModel getTreeModel() {
		if (myTreeModel == null) {
			myTreeModel = new DefaultTreeModel(myRootNode);
		}
		return myTreeModel;
	}
}
