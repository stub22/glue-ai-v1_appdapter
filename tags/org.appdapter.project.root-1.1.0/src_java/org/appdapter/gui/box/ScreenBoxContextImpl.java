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

import org.appdapter.api.trigger.MutableBox;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxContextImpl;
import org.appdapter.gui.browse.DisplayContext;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

/**
 * @author Stu B. <www.texpedient.com>
 *
 * This class operates on raw box types.
 */
public class ScreenBoxContextImpl extends BoxContextImpl implements DisplayContextProvider  {
	private	ScreenBoxTreeNode			myRootNode;
	private	DefaultTreeModel	myTreeModel;
	public ScreenBoxContextImpl() { }
	private void setRootNode(ScreenBoxTreeNode rootNode) {
		myRootNode = rootNode;
	}
	public Box getRootBox() {
		Box result = null;
		if (myRootNode != null) {
			result = myRootNode.getBox();
		}
		return result;
	}
	private ScreenBoxTreeNode findNodeForBox(Box b) {
		return myRootNode.findDescendantNodeForBox(b);
	}
	public Box getParentBox(Box child) {
		ScreenBoxTreeNode childNode = findNodeForBox(child);
		return ((ScreenBoxTreeNode) childNode.getParent()).getBox();
	}
	// TODO:  Pass in the class parent of the expected children
	public List<Box> getOpenChildBoxes(Box parent) {
		List<Box> results = new ArrayList<Box>();
		ScreenBoxTreeNode parentNode = findNodeForBox(parent);
		Enumeration childNodeEnum = parentNode.children();
		while (childNodeEnum.hasMoreElements()) {
			ScreenBoxTreeNode btn = (ScreenBoxTreeNode) childNodeEnum.nextElement();
			Box childBox = btn.getBox();
			results.add(childBox);
		}
		return results;
	}

	public	DisplayContext	findDisplayContext(Box viewable) {
		ScreenBoxTreeNode btn = findNodeForBox(viewable);
		return btn.findDisplayContext();
	}
	public void contextualizeAndAttachRootBox(MutableBox rootBox) {
		ScreenBoxTreeNode rootNode = new ScreenBoxTreeNode(rootBox);
		setRootNode(rootNode);
		rootBox.setContext(this);
		((ScreenBox)	rootBox).setDisplayContextProvider(this);
	}
	private ScreenBoxTreeNode attachChildBoxNode(ScreenBoxTreeNode parentNode, Box childBox) {
		//  childBox should already have context(==this) and displayContext.
		if (childBox.getBoxContext() != this) {
			throw new RuntimeException("Refusing to attach a childBox[" + childBox + "] which is not in this context [" + this + "]");
		}
		ScreenBoxTreeNode childNode = new ScreenBoxTreeNode(childBox);
		parentNode.add(childNode);
		if (myTreeModel != null) {
			myTreeModel.reload(parentNode);
		}
		return childNode;
	}
	public void contextualizeAndAttachChildBox(Box<?> parentBox, MutableBox<?> childBox) {
		ScreenBoxTreeNode parentNode = findNodeForBox(parentBox);
		if (parentNode == null) {
			throw new RuntimeException("Can't find node for parentBox: " + parentBox);
		}
		childBox.setContext(this);
		ScreenBoxTreeNode childNode = attachChildBoxNode(parentNode, childBox);
		((ScreenBox) childBox).setDisplayContextProvider(this);
	}
	public TreeModel getTreeModel() {
		if (myTreeModel == null) {
			myTreeModel = new DefaultTreeModel(myRootNode);
		}
		return myTreeModel;
	}
	public void reloadTreeModel() {
		TreeModel tm = getTreeModel();
		DefaultTreeModel dtm = (DefaultTreeModel) tm;
		dtm.reload();
	}
}