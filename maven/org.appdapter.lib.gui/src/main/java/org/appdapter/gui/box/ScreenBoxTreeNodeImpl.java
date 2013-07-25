package org.appdapter.gui.box;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.appdapter.api.trigger.AnyOper.HasIdent;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.core.component.KnownComponent;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.name.Ident;
import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.BoxPanelSwitchableView;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.api.ScreenBoxTreeNode;
import org.appdapter.gui.api.WrapperValue;
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

	public Object getUserObject() {
		Object userObject = super.getUserObject();
		if (userObject instanceof WrapperValue) {
			userObject = ((WrapperValue) userObject).reallyGetValue();
		}
		return userObject;
	}

	@Override public String toString() {
		String w = wasToString();
		if (w.contains("default")) {
			w = wasToString();
		}
		return w;
	}

	public String wasToString() {
		String toStr = null;
		Object userObject = getUserObject();
		if (userObject instanceof WrapperValue) {
			Object rv = ((WrapperValue) userObject).reallyGetValue();
			if (rv != null)
				userObject = rv;
		}
		if (userObject instanceof HasIdent) {
			HasIdent kc = (HasIdent) userObject;
			Ident id = kc.getIdent();
			if (id != null) {
				toStr = id.getLocalName();
				if (toStr != null)
					return toStr;

			}
		}
		if (userObject instanceof KnownComponent) {
			KnownComponent kc = (KnownComponent) userObject;
			toStr = kc.getShortLabel();
			if (toStr != null)
				return toStr;
		}
		Object realUserObject = super.getUserObject();
		if (realUserObject instanceof KnownComponent) {
			KnownComponent kc = (KnownComponent) realUserObject;
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
