package org.appdapter.gui.browse;

import java.beans.PropertyVetoException;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

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
			pojoAdded0(b.getValue(), b);
		}
		invalidate();
	}

	private void invalidate() {
		jtree.invalidate();
	}

	@Override public void pojoAdded(Object obj, BT box) {
		pojoAdded0(obj, box);
		invalidate();
	}

	void pojoAdded0(Object obj, BT box) {
		try {
			pojoUpdate(obj, box, false);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
			throw Debuggable.reThrowable(e);
		}
	}

	void pojoUpdate(Object obj, BT box, boolean isRemoval) throws PropertyVetoException {
		Object d = Utility.dref(obj);
		if (d != null && d != obj) {
			//	obj = d;
		}
		Class oc = obj.getClass();
		if (oc.isArray())
			return;
		if (Utility.isToStringType(oc))
			return;
		if (obj instanceof RandomAccess)
			return;

		MutableBox objectBox = (MutableBox) box;

		MutableBox rootBox = getFirstBox(null);
		saveInClassTree(rootBox, oc, objectBox, isRemoval);
		if (obj instanceof Class)
			return;
		for (Class ifc : oc.getInterfaces()) {
			MutableBox faceBox = getFirstBox(Class.class);
			saveInClassTree(faceBox, ifc, objectBox, isRemoval);
		}
	}

	public MutableBox getFirstBox(Object obj) throws PropertyVetoException {
		if (obj == null)
			return (MutableBox) mybctx.getRootBox();
		return (MutableBox) col.findOrCreateBox(null, obj).asBox();
	}

	private void saveInClassTree(Box belowBox, Class oc, MutableBox objectBox, boolean isRemoval) throws PropertyVetoException {
		String cn = Utility.getShortClassName(oc);
		MutableBox objectClassBox = (MutableBox) col.findOrCreateBox(cn, oc).asBox();
		if (!isRemoval) {
			mybctx.contextualizeAndAttachChildBox(belowBox, objectClassBox);
			mybctx.contextualizeAndAttachChildBox((Box) objectClassBox, objectBox);
		} else {
			mybctx.contextualizeAndAttachChildBox(belowBox, objectClassBox);
			mybctx.contextualizeAndDetachChildBox((Box) objectClassBox, objectBox);
		}
	}

	@Override public void pojoRemoved(Object obj, BT box) {
		pojoRemoved0(obj, box);
		invalidate();
	}

	void pojoRemoved0(Object obj, BT box) {
		try {
			pojoUpdate(obj, box, true);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
			throw Debuggable.reThrowable(e);
		}
	}

}
