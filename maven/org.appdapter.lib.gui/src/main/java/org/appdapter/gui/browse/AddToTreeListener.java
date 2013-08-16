package org.appdapter.gui.browse;

import java.awt.Container;
import java.beans.PropertyVetoException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.RandomAccess;

import javax.swing.JMenu;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxContext;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.api.POJOCollectionListener;

import scala.collection.parallel.ParIterableLike.Foreach;

public class AddToTreeListener implements POJOCollectionListener {

	public String toString() {
		return Debuggable.toInfoStringF(this);
	}

	NamedObjectCollection col;
	MutableBox root;
	BoxContext mybctx;
	Container jtree;
	boolean organizeIntoClasses;
	HashSet<Class> classesAtFirstLevel = new HashSet();
	HashSet<Object> objectsAtSecondLevel = new HashSet();

	public AddToTreeListener(Container myTree, NamedObjectCollection ctx, BoxContext bctx, MutableBox root, boolean organizeIntoClasses) {
		mybctx = bctx;
		jtree = myTree;
		col = ctx;
		this.root = root;
		this.organizeIntoClasses = organizeIntoClasses;
		Utility.invokeAfterLoader(new Runnable() {

			@Override public void run() {
				addboxes(col);
				col.addListener(AddToTreeListener.this, false);
			}
		});
	}

	private void addboxes(NamedObjectCollection ctx) {
		Iterator it = ctx.getBoxes();
		while (it.hasNext()) {
			BT b = (BT) it.next();
			pojoAdded0(b.getValueOrThis(), b);
		}
		invalidate();
	}

	private void invalidate() {
		jtree.invalidate();
	}

	@Override public void pojoAdded(Object obj, BT box, Object senderCollection) {
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
				box = Utility.asWrapped(d);
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
			addChildObject(root, box.getShortLabel(), anyObject);
		} else {
			removeChildObject(root, box.getShortLabel(), anyObject);
		}

	}

	void pojoUpdateObjectWithClass(Object obj, BT box0, boolean isRemoval) {
		obj = Utility.drefO(obj);
		pojoUpdateObjectWithClass(obj, isRemoval);
	}

	void pojoUpdateObjectWithClass(Object obj, boolean isRemoval) {

		Class oc = obj.getClass();
		if (oc.isArray()) {
			Class compType = oc.getComponentType();
			if (Utility.isSystemPrimitive(compType))
				return;
			for (Object o : (Object[]) obj) {
				Utility.addObjectFeatures(o);
			}
			return;
		}

		synchronized (this) {
			if (obj instanceof Class) {
				addFirstLevelClass((Class) obj, isRemoval);
				return;
			}
		}
		if (obj instanceof RandomAccess)
			return;

		if (obj instanceof JMenu)
			return;

		if (Utility.isSystemPrimitive(oc))
			return;

		if (!isRemoval) {
			//	Utility.addObjectFeatures(obj);
		}
		synchronized (this) {
			addFirstLevelClass(oc, isRemoval);
			addSecondLevelObject(obj, isRemoval);
		}

	}

	private void addFirstLevelClass(Class newClassMaybe, boolean isRemoval) {
		if (BT.class == newClassMaybe) {
			if (classesAtFirstLevel.contains(newClassMaybe))
				return;
		}
		if (classesAtFirstLevel.contains(newClassMaybe))
			return;
		classesAtFirstLevel.add(newClassMaybe);
		MutableBox rootBox = getFirstBox(null);

		saveInClassTree(rootBox, Class.class, asMutable(newClassMaybe), isRemoval);

		for (Object o : objectsAtSecondLevel) {
			if (newClassMaybe.isInstance(asValue(o))) {
				saveInClassTree(rootBox, newClassMaybe, asMutable(o), isRemoval);
			}
		}
		if (newClassMaybe.isInterface())
			return;
		for (Class ifc : newClassMaybe.getInterfaces()) {
			if (!Utility.isLocalInterface(ifc))
				continue;
			addFirstLevelClass(ifc, isRemoval);
		}
	}

	private void addSecondLevelObject(Object o, boolean isRemoval) {
		if (objectsAtSecondLevel.contains(o))
			return;
		objectsAtSecondLevel.add(o);
		MutableBox rootBox = getFirstBox(null);
		for (Class newClassMaybe : classesAtFirstLevel) {
			if (newClassMaybe.isInstance(asValue(o))) {
				saveInClassTree(rootBox, newClassMaybe, asMutable(o), isRemoval);
			}
		}
	}

	private Object asValue(Object o) {
		return Utility.drefO(o);
	}

	private MutableBox asMutable(Object o) {
		if (o instanceof MutableBox)
			return (MutableBox) o;
		return (MutableBox) findOrCreateBox(null, o);
	}

	public MutableBox getFirstBox(Object obj) {
		if (obj == null) {
			if (root != null)
				return root;
			return (MutableBox) mybctx.getRootBox();
		}
		return asMutable(obj);
	}

	private void saveInClassTree(Box belowBox, Class oc, MutableBox objectBox, boolean isRemoval) {
		try {
			saveInTreeWC(belowBox, oc, objectBox, isRemoval);
		} catch (Exception e2) {
			Debuggable.printStackTrace(e2);
		}
	}

	private void saveInTreeWC(Box belowBox, Class oc, MutableBox objectBox, boolean isRemoval) {
		String cn = Utility.getSpecialClassName(oc);
		MutableBox objectClassBox = (MutableBox) findOrCreateBox(cn, oc);
		if (!isRemoval) {
			mybctx.contextualizeAndAttachChildBox(belowBox, objectClassBox);
			mybctx.contextualizeAndAttachChildBox((Box) objectClassBox, objectBox);
		} else {
			mybctx.contextualizeAndAttachChildBox(belowBox, objectClassBox);
			mybctx.contextualizeAndDetachChildBox((Box) objectClassBox, objectBox);
		}
	}

	@Override public void pojoRemoved(Object obj, BT box, Object senderCollection) {
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
		MutableBox objectBox = (MutableBox) findOrCreateBox(title, child);
		mybctx.contextualizeAndAttachChildBox((Box) parent, objectBox);
	}

	public void removeChildObject(Object anyObject, String title, Object child) throws PropertyVetoException {
		MutableBox parent = getFirstBox(anyObject);
		MutableBox objectBox = (MutableBox) findOrCreateBox(title, child);
		mybctx.contextualizeAndDetachChildBox((Box) parent, objectBox);

	}

	private MutableBox findOrCreateBox(String title, Object child) {
		if (child instanceof MutableBox)
			return (MutableBox) child;
		try {
			return (MutableBox) Utility.asWrapped(title, child);
		} catch (PropertyVetoException e) {
			throw Debuggable.reThrowable(e);
		}
	}
}
