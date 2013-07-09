package org.appdapter.gui.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.api.ObjectCollectionRemoveListener;
import org.appdapter.gui.api.POJOCollectionListener;
import org.appdapter.gui.browse.Utility;

/**
 * A GUI component showing the list of objects in the collection
 */
public class LargeObjectChooser extends JPanel implements POJOCollectionListener {

	public Object getValue() {
		return getObject();
	}

	NamedObjectCollection localCollection;
	Class filter;
	JPanel panel;
	JScrollPane scroll;
	Border defaultScrollBorder;

	@Override public String getName() {
		String named = localCollection.toString();
		if (filter != null) {
			named = Utility.getShortClassName(filter) + " of " + named;
		}
		return named;
	}

	public LargeObjectChooser(Class filterc, NamedObjectCollection context0) {
		super(false);
		filter = filterc;
		this.localCollection = context0;
		initGUI();
		localCollection.addListener(this);
	}

	@Override public Dimension getPreferredSize() {
		return new Dimension(130, 200);
		// return Utility.getMaxDimension(new Dimension(250, 200),
		// super.getPreferredSize());
	}

	@Override public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	@Override public void pojoAdded(Object obj, BT box) {
		// @optimize
		if (filter != null) {
			if (!filter.isInstance(obj))
				return;
		}
		reloadContents();
	}

	@Override public void pojoRemoved(Object obj, BT box) {
		// @optimize
		if (filter != null) {
			if (!filter.isInstance(obj))
				return;
		}
		reloadContents();
	}

	public void setTitle(String title) {
		//defaultScrollBorder = new Defa
		//	scroll.setBorder(new TitledBorder(defaultScrollBorder, title));
	}

	private void initGUI() {
		panel = new JPanel();
		panel.setLayout(new VerticalLayout());
		setTitle(getName());
		scroll = new JScrollPane(panel);
		defaultScrollBorder = scroll.getBorder();

		//setBorder(new TitledBorder(new , "XYZ"));
		setLayout(new BorderLayout());
		add("Center", scroll);

		reloadContents();
	}

	public void reloadContents() {
		panel.removeAll();

		Iterator it = localCollection.getObjects();
		while (it.hasNext()) {
			Object value = it.next();
			if (filter != null) {
				if (!filter.isInstance(value))
					continue;
			}
			SmallObjectView view = new SmallObjectView(null, localCollection, value, null) {
				@Override public void valueChanged(Object oldValue, Object newValue) {
					replaceInContext(localCollection, oldValue, newValue);
					super.valueChanged(oldValue, newValue);
				}
			};
			view.setRemoveListener(new ObjectCollectionRemoveListener() {
				@Override public void objectRemoved(Object oldValue, Collection parent) {
					replaceInContext(localCollection, oldValue, null);
				}
			});
			panel.add(view);
		}
		invalidate();
		validate();
		repaint();
	}

	protected void replaceInContext(NamedObjectCollection context2, Object oldValue, Object newValue) {
		context2.findOrCreateBox(oldValue).setValue(newValue);
	}

	public Object getObject() {
		return localCollection.getSelectedObject();
	}

	public void setSelectedObject(Object object) throws PropertyVetoException {
		localCollection.setSelectedObject(object);

	}

}