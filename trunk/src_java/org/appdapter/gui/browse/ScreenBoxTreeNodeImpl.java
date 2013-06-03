package org.appdapter.gui.browse;

import java.awt.Component;

import javax.swing.tree.TreeNode;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.gui.box.AppGUIWithTabsAndTrees;
import org.appdapter.gui.box.BoxPanelSwitchableView;
import org.appdapter.gui.box.BoxPanelSwitchableViewImpl;
import org.appdapter.gui.box.ScreenBoxTreeNode;
import org.appdapter.gui.pojo.DisplayType;

public class ScreenBoxTreeNodeImpl extends BoxPanelSwitchableViewImpl implements ScreenBoxTreeNode, TreeNode {

	public ScreenBoxTreeNodeImpl(Box rootBox) {
		super((MutableBox) rootBox);
	}

	@Override public String toString() {
		// TODO Auto-generated method stub
		String toStr = super.toString();
		if (toStr == null) {
			return "<ScreenBoxTreeNodeImpl null>";
		}
		return toStr;
	}

	@Override public BoxPanelSwitchableView getBoxPanelTabPane() {
		// TODO Auto-generated method stub
		return super.getScreenBoxTreeParent();
	}

	final public DisplayContext findDisplayContext(Box b) {
		return super.findDisplayContextFromTree(b);
	}

	@Override public Component getComponent() {
		return super.getComponent(getAppGUI(DisplayType.TREE));
	}

	@Override public AppGUIWithTabsAndTrees getAppGUI(DisplayType attachType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public ScreenBoxTreeNode findDescendantNodeForBox(Box b) {
		// TODO Auto-generated method stub
		return null;
	}

}
