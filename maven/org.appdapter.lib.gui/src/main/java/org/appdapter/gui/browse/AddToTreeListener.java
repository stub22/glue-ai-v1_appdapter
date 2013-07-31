package org.appdapter.gui.browse;

import java.awt.Container;
import java.beans.PropertyVetoException;
import java.util.Iterator;
import java.util.Map;
import java.util.RandomAccess;

import javax.swing.JTree;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxContext;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.api.POJOCollection;
import org.appdapter.gui.api.POJOCollectionListener;

public class AddToTreeListener implements POJOCollectionListener {

	public String toString() {
		return Debuggable.toInfoStringF(this);
	}

	NamedObjectCollection col;
	MutableBox root;
	BoxContext mybctx;
	Container jtree;
	boolean organizeIntoClasses;

	public AddToTreeListener(Container myTree, NamedObjectCollection ctx, BoxContext bctx, MutableBox root, boolean organizeIntoClasses) {
		mybctx = bctx;
		jtree = myTree;
		col = ctx;
		this.root = root;
		this.organizeIntoClasses = organizeIntoClasses;
		addboxes(ctx);
		ctx.addListener(this);
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
		if (!isRemoval) {
			if (d != null && d != obj) {
				obj = d;
			}
			if (obj == null) {
				obj = box.getValueOrThis();
			}
			if (box == null) {
				box = col.asWrapped(d);
			}
		}
		if (organizeIntoClasses) {
			pojoUpdateObjectWithClass(obj, box, isRemoval);
		} else {
			pojoUpdateObjectOnly(obj, box, isRemoval);
		}
	}

	void pojoUpdateObjectOnly(Object anyObject, BT box, boolean isRemoval) throws PropertyVetoException {

		Class oc = anyObject.getClass();

		if (!isRemoval) {
			Utility.addObjectFeatures(anyObject);
			addChildObject(root, box.getUniqueName(), anyObject);
		} else {
			removeChildObject(root, box.getUniqueName(), anyObject);
		}

	}

	void pojoUpdateObjectWithClass(Object obj, BT box, boolean isRemoval) throws PropertyVetoException {

		Class oc = obj.getClass();

		if (oc.isArray())
			return;
		if (Utility.isToStringType(oc))
			return;
		if (obj instanceof RandomAccess)
			return;

		if (!isRemoval) {
			Utility.addObjectFeatures(obj);
		}

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
		if (obj == null) {
			if (root != null)
				return root;
			return (MutableBox) mybctx.getRootBox();
		}
		return (MutableBox) findOrCreateBox(col, null, obj);
	}

	private void saveInClassTree(Box belowBox, Class oc, MutableBox objectBox, boolean isRemoval) throws PropertyVetoException {
		try {
			saveInTreeWC(belowBox, oc, objectBox, isRemoval);
		} catch (Exception e2) {
			Debuggable.printStackTrace(e2);
		}
	}

	private void saveInTreeWC(Box belowBox, Class oc, MutableBox objectBox, boolean isRemoval) {
		String cn = Utility.getShortClassName(oc);
		MutableBox objectClassBox = (MutableBox) findOrCreateBox(col, cn, oc);
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

	public void treeExpand(Object anyObject, int i) {
		BT bt = Utility.asWrapped(anyObject);
		try {
			pojoUpdate(bt.getValue(), bt, false);
			treeExpand0(anyObject, i);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
			throw Debuggable.reThrowable(e);
		}
	}

	public void treeExpand0(Object anyObject, int i) {
		if (i >= 0) {
			int ii = i - 1;
			try {
				Map map = Utility.propertyDescriptors(anyObject, true, true);
				for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
					Map.Entry entry = (Map.Entry) iterator.next();
					String title = "" + entry.getKey();
					Object child = entry.getValue();
					addChildObject(anyObject, title, child);
					if (i > 0) {
						treeExpand0(child, ii);
					}
				}

			} catch (PropertyVetoException e) {
				Debuggable.printStackTrace(e);
				throw Debuggable.reThrowable(e);
			}
		}

	}

	public void addChildObject(Object anyObject, String title, Object child) throws PropertyVetoException {
		MutableBox parent = getFirstBox(anyObject);
		MutableBox objectBox = (MutableBox) findOrCreateBox(col, title, child);
		mybctx.contextualizeAndAttachChildBox((Box) parent, objectBox);
	}

	public void removeChildObject(Object anyObject, String title, Object child) throws PropertyVetoException {
		MutableBox parent = getFirstBox(anyObject);
		MutableBox objectBox = (MutableBox) findOrCreateBox(col, title, child);
		mybctx.contextualizeAndAttachChildBox((Box) parent, objectBox);

	}

	private MutableBox findOrCreateBox(NamedObjectCollection col2, String title, Object child) {
		if (child instanceof MutableBox)
			return (MutableBox) child;
		try {
			return (MutableBox) col.findOrCreateBox(title, child).asBox();
		} catch (PropertyVetoException e) {
			throw Debuggable.reThrowable(e);
		}
	}
}
