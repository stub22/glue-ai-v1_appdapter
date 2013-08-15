package org.appdapter.gui.swing;

import static org.appdapter.core.convert.ReflectUtils.copyOf;

import java.awt.dnd.DropTargetListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.event.ChangeListener;

import org.appdapter.api.trigger.Box;
import org.appdapter.gui.api.BoxPanelSwitchableView;
import org.appdapter.gui.api.Chooser;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.api.ValueChangeListener;
import org.appdapter.gui.browse.Utility;
import org.slf4j.LoggerFactory;

/**
 * A GUI component that shows what a Collection contains,
 * and lets you add and remove elements.
 *
 * 
 */
public class CollectionContentsPanel<BoxType extends Box>

extends BaseCollectionContentsPanel<BoxType> implements ValueChangeListener, DropTargetListener, Chooser<Object>, ChangeListener {

	@Override public Collection getValue() {
		return getCollection();
	}

	public static Class EDITTYPE = Collection.class;

	public CollectionContentsPanel() {
		super();
	}

	public CollectionContentsPanel(DisplayContext context, String titleStr, NamedObjectCollection noc, Collection collection, Class filterc, BoxPanelSwitchableView tabs, boolean valueIsNotCollection) {
		super(context, titleStr, noc, filterc, tabs, false);
		this.objectValue = collection;
		reloadContents();
	}

	public void reloadContents00() {

		Collection collection = getCollection();
		panel.removeAll();
		if (collection == null) {
			reloaded();
			return;
		}
		collection = copyOf(collection);
		int size = collection.size();
		for (Object value : collection) {
			if (filter != null) {
				if (!filter.isInstance(value))
					continue;
			}
			SmallObjectView view = new SmallObjectView(context, nameMaker, value) {
				@Override public boolean isRemovable(Object value) {
					return CollectionContentsPanel.this.containsObject(value);
				}
			};
			view.addChangeListener(this);
			panel.add(view);
		}
		if (panel.getComponentCount() > size) {
			// something added more?
			LoggerFactory.getLogger(getClass()).warn("Wrong number of compoents in " + this);
		}
		reloaded();
	}

	protected boolean containsObject(Object value) {
		return getCollection().contains(value);

	}

	protected void removeObject(Object oldValue, int i) {
		Collection collection = getCollection();
		collection.remove(oldValue);
		reloadContents();
	}

	protected void replaceValue(Object oldValue, Object newValue) {
		Collection collection = getCollection();
		Iterator it = collection.iterator();
		while (it.hasNext()) {
			final Object value = it.next();
			if (value == oldValue) {
				it.remove();
				collection.add(newValue);
				return;
			}
		}
	}

	public void addObject(Object o, int i) {
		Collection collection = getCollection();
		collection.add(o);
		reloadContents();
	}

	public Collection getCollection() {
		return Utility.recastCC(super.getValue(), Collection.class);
	}

	@Override public Class<Collection> getClassOfBox() {
		return Collection.class;
	}

}