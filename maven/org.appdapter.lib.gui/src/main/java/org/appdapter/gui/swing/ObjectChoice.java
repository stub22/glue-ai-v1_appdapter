package org.appdapter.gui.swing;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditorSupport;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

import org.appdapter.api.trigger.NamedObjectCollection;
import org.appdapter.api.trigger.POJOCollectionListener;
import org.appdapter.gui.api.NamedObjectCollectionImpl;
import org.appdapter.gui.browse.CollectionEditorUtil;
import org.appdapter.gui.impl.JJPanel;
import org.appdapter.gui.rimpl.TriggerPopupMenu;

/**
 * A GUI widget that lets you choose among a set of objects
 * of a certain type.
 *
 */
public class ObjectChoice extends JJPanel implements POJOCollectionListener, MouseListener {

	PropertyEditorSupport editorSupport = new PropertyEditorSupport();

	final NamedObjectCollection context;
	PropertyChangeSupport propSupport = new PropertyChangeSupport(this);

	Class type;
	JComboBox combo;
	Model model;

	public ObjectChoice(Class type, Object value) {
		this(null, type, value);
	}

	public ObjectChoice(NamedObjectCollection context0, Class type0, Object value) {
		super(false);
		this.type = type0;
		if (context0 == null)
			context0 = new NamedObjectCollectionImpl("Empty Collection " + (++CollectionEditorUtil.newCollectionSerial) + " of " + type + " for " + value, null);
		this.context = context0;
		if (type == null)
			type = Object.class;
		initGUI();
		combo.setSelectedItem(value);
		if (context != null)
			context.addListener(this);
	}

	@Override public void addPropertyChangeListener(PropertyChangeListener p) {
		checkTransient();
		propSupport.addPropertyChangeListener(p);
	}

	private void checkTransient() {
		if (propSupport == null)
			propSupport = new PropertyChangeSupport(this);
	}

	@Override public void removePropertyChangeListener(PropertyChangeListener p) {
		checkTransient();
		propSupport.removePropertyChangeListener(p);
	}

	public void setSelection(Object object) {
		model.setSelectedItem(object);
	}

	@Override public void pojoAdded(Object obj) {
		model.reload();
	}

	@Override public void pojoRemoved(Object obj) {
		model.reload();
	}

	private void initGUI() {
		model = new Model();
		combo = new JComboBox(model);
		combo.setEditable(false);
		setLayout(new BorderLayout());
		add("Center", combo);
		combo.addMouseListener(this);
	}

	@Override public void mouseClicked(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showMenu(e.getX() + 5, e.getY() + 5);
		}
	}

	@Override public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showMenu(e.getX() + 5, e.getY() + 5);
		}
	}

	@Override public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showMenu(e.getX() + 5, e.getY() + 5);
		}
	}

	@Override public void mouseEntered(MouseEvent e) {
		//@temp
		//label.setForeground(Color.blue);
	}

	@Override public void mouseExited(MouseEvent e) {
		//label.setForeground(Color.black);
	}

	public Object getSelection() {
		return model.getSelectedItem();
	}

	private void showMenu(int x, int y) {
		Object object = model.getSelectedBean();
		if (object != null) {
			TriggerPopupMenu menu = new TriggerPopupMenu(null, object);
			add(menu);
			menu.show(this, x, y);
		}
	}

	class Model extends AbstractListModel implements ComboBoxModel {
		//Vector listeners = new Vector();
		java.util.List values;
		Object selected = null;

		@SuppressWarnings("unchecked") public Model() {
			if (context == null)
				values = new LinkedList();
			else {
				Collection col = context.findObjectsByType(type);
				values = new LinkedList(col);
			}
			values.add("<null>");
		}

		@Override public synchronized void setSelectedItem(Object anItem) {
			Object old = selected;
			selected = anItem;

			//if (old != selected)
			//  notifyListeners();

			if (selected != null && !values.contains(selected))
				values.add(selected);
			fireContentsChanged(this, -1, -1);
			if (selected != old) {
				propSupport.firePropertyChange("selection", old, selected);
			}
		}

		@Override public Object getSelectedItem() {
			if (selected == null)
				return "<null>";
			else
				return selected;
		}

		public Object getSelectedBean() {
			return selected;
		}

		@Override public int getSize() {
			return values.size();
		}

		@Override public Object getElementAt(int index) {
			try {
				return values.get(index);
			} catch (Exception err) {
				return null;
			}
		}

		public synchronized void reload() {
			Object selected = getSelectedBean();
			if (values == null)
				values = new LinkedList();
			else
				values = new LinkedList(context.findObjectsByType(type));
			values.add("<null>");
			setSelectedItem(selected);
		}
	}

	public Object getValue() {
		return model.getSelectedBean();
	}

}