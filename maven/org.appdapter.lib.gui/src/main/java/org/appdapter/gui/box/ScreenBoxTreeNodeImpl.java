package org.appdapter.gui.box;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.core.component.KnownComponent;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.BoxPanelSwitchableView;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.api.ScreenBoxTreeNode;
import org.appdapter.gui.browse.Utility;

public class ScreenBoxTreeNodeImpl extends AbstractScreenBoxTreeNodeImpl implements ScreenBoxTreeNode, TreeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ScreenBoxTreeNodeImpl(BoxPanelSwitchableView bsv, Box rootBox, boolean allowsChildrn, NamedObjectCollection noc) {
		super(noc, (MutableBox) rootBox, allowsChildrn);
		this.bsv = bsv;
	}

	@Override public String toString() {
		String toStr = null;
		userObject = getUserObject();
		if (userObject instanceof KnownComponent) {
			KnownComponent kc = (KnownComponent) userObject;
			toStr = kc.getShortLabel();
			if (toStr != null)
				return toStr;
		}

		toStr = super.toString();
		if (toStr == null) {
			return "<ScreenBoxTreeNodeImpl null>";
		}
		return toStr;
	}

	@Override public BoxPanelSwitchableView getBoxPanelTabPane() {
		if (bsv != null)
			return bsv;
		DisplayContext mDctx = getDisplayContext();
		if (mDctx != null) {
			bsv = mDctx.getBoxPanelTabPane();
			if (bsv != null)
				return bsv;
		}
		ScreenBoxTreeNodeImpl sbp = getScreenBoxTreeParent();
		if (sbp != null)
			return sbp.getBoxPanelTabPane();
		return Utility.getBoxPanelTabPane();
	}

	final public DisplayContext findDisplayContext(Box b) {
		return super.findTreeNodeDisplayContext(b);
	}

	@Override public ScreenBoxTreeNode findDescendantNodeForBox(Box b) {
		return super.findDescendantNodeForBox(b);
	}

	@Override public Object getValue() {
		return getBox().getValue();
	}

	@Override public void setObject(Object obj) throws InvocationTargetException {
		if (obj instanceof Box) {
			setBox((Box) obj);
			return;
		}
		setNonBox(obj);
	}

	private void setNonBox(Object obj) {
		throw Debuggable.warn("setNonBox", obj);
	}

	private void setBox(Box box) {
		setUserObject(box);
	}

	public Iterable<BT> getTreeChildren() {
		List<BT> results = new ArrayList<BT>();
		ScreenBoxTreeNode parentNode = (ScreenBoxTreeNode) this;
		Enumeration childNodeEnum = parentNode.children();
		while (childNodeEnum.hasMoreElements()) {
			ScreenBoxTreeNode btn = (ScreenBoxTreeNode) childNodeEnum.nextElement();
			results.add((BT) btn.getBox());
		}
		return results;
	}

	@Override public Iterable<BT> getTreeRepresentedChildren() {
		return getTreeChildren();
	}

	@Override public Iterable<Object> getCollectionUnboxed() {
		return Utility.unboxObjects(getTreeChildren());
	}

	@Override public Object getTreeRepresentedObject() {
		return this;
	}
}
