package org.appdapter.gui.browse;

import java.util.Iterator;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.appdapter.api.trigger.BT;
import org.appdapter.api.trigger.NamedObjectCollection;
import org.appdapter.api.trigger.POJOCollectionListener;
import org.appdapter.gui.api.Utility;

public class AddToTreeListener implements POJOCollectionListener {

	JTree jtree;
	NamedObjectCollection col;

	public AddToTreeListener(JTree myTree, NamedObjectCollection ctx) {
		jtree = myTree;
		col = ctx;
		addboxes(ctx);
		col.addListener(this);
	}

	private void addboxes(NamedObjectCollection ctx) {
		Iterator it = ctx.getBoxes();
		while (it.hasNext()) {
			BT b = (BT) it.next();
			pojoAdded(b.getValue());
		}
	}

	@Override public void pojoAdded(Object obj) {
		BT box = col.findBoxByObject(obj);
		Class oc = box.getObjectClass();
		String cn = Utility.getShortClassName(oc);
		TreeModel tm = jtree.getModel();
		ScreenBoxTreeNodeImpl rootBTN = (ScreenBoxTreeNodeImpl) tm.getRoot();
		rootBTN.attachChildObect(cn, oc).attachChildObect(null, obj);
	}

	@Override public void pojoRemoved(Object obj) {
		BT box = col.findBoxByObject(obj);
		Class oc = box.getObjectClass();
		String cn = Utility.getShortClassName(oc);
		TreeModel tm = jtree.getModel();
		ScreenBoxTreeNodeImpl rootBTN = (ScreenBoxTreeNodeImpl) tm.getRoot();
		rootBTN.attachChildObect(cn, oc).detachChildObect(null, obj);
	}
}
