package org.appdapter.gui.browse;

import java.beans.PropertyVetoException;
import java.util.Iterator;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;

import org.appdapter.api.trigger.BT;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxContext;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.api.trigger.NamedObjectCollection;
import org.appdapter.api.trigger.POJOCollectionListener;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.Utility;

public class AddToTreeListener implements POJOCollectionListener {

	JTree jtree;
	NamedObjectCollection col;
	BoxContext mybctx;

	public AddToTreeListener(JTree myTree, NamedObjectCollection ctx, BoxContext bctx) {
		mybctx = bctx;
		jtree = myTree;
		col = ctx;
		addboxes(ctx);
		col.addListener(this);
	}

	private void addboxes(NamedObjectCollection ctx) {
		Iterator it = ctx.getBoxes();
		while (it.hasNext()) {
			BT b = (BT) it.next();
			pojoAdded0(b.getValue());
		}
		invalidate();
	}

	private void invalidate() {
		jtree.invalidate();
	}

	@Override public void pojoAdded(Object obj) {
		pojoAdded0(obj);
		invalidate();
	}

	void pojoAdded0(Object obj) {
		try {
			pojoAdded00(obj);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
			throw Debuggable.reThrowable(e);
		}
	}

	void pojoAdded00(Object obj) throws PropertyVetoException {
		BT box = col.findBoxByObject(obj);
		Class oc = box.getObjectClass();
		String cn = Utility.getShortClassName(oc);
		TreeModel tm = jtree.getModel();
		ScreenBoxTreeNodeImpl rootBTN = (ScreenBoxTreeNodeImpl) tm.getRoot();
		MutableBox ccb = (MutableBox) col.findOrCreateBox(cn, oc).asBox();
		mybctx.contextualizeAndAttachChildBox(getClassesBox(), ccb);
		mybctx.contextualizeAndAttachChildBox((Box) ccb, (MutableBox) col.findOrCreateBox(null, obj).asBox());
	}

	@Override public void pojoRemoved(Object obj) {
		pojoRemoved0(obj);
		invalidate();
	}

	void pojoRemoved0(Object obj) {
		BT box = col.findBoxByObject(obj);
		Class oc = box.getObjectClass();
		String cn = Utility.getShortClassName(oc);
		TreeModel tm = jtree.getModel();
		ScreenBoxTreeNodeImpl rootBTN = (ScreenBoxTreeNodeImpl) tm.getRoot();
		AbstractScreenBoxTreeNodeImpl chn = rootBTN.attachChildObect(cn, oc);
		chn.detachChildObect(null, obj);

	}

	public MutableBox getClassesBox() {
		return (MutableBox) mybctx.getRootBox();
	}
}
