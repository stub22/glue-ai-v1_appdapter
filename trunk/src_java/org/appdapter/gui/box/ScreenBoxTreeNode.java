package org.appdapter.gui.box;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.appdapter.api.trigger.Box;
import org.appdapter.gui.browse.DisplayContext;

public interface ScreenBoxTreeNode extends TreeNode, BoxPanelSwitchableView {

	public abstract void setDisplayContext(DisplayContext dc);

	public abstract DisplayContext getDisplayContext();

	public abstract Box getBox();

	public abstract ScreenBoxTreeNode getScreenBoxTreeParent();

	public abstract ScreenBoxTreeNode findDescendantNodeForBox(Box b);

	public abstract void add(MutableTreeNode childNode);

}
