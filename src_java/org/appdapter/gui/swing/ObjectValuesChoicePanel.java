package org.appdapter.gui.swing;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

import org.appdapter.gui.box.ScreenBoxPanel;
import org.appdapter.gui.browse.DisplayContext;
import org.appdapter.gui.pojo.POJOCollectionListener;

/**
 * A GUI widget that lets you choose among a set of objects
 * of a certain type.
 *
 */
public class ObjectValuesChoicePanel extends ScreenBoxPanel implements POJOCollectionListener, MouseListener {
	DisplayContext context = new EmptyPOJOCollectionContext();
	PropertyChangeSupport propSupport = new PropertyChangeSupport(this);

	Class type;
	JComboBox combo;
	Model model;
	
	@Override public DisplayContext getDisplayContext() {
		return context;
	}
	
	public ObjectValuesChoicePanel(Class type, Object value) {
		this(null, type, value);
	}

	public ObjectValuesChoicePanel(DisplayContext context, Class type, Object value) {
		this.type = type;
		if (context != null)
			this.context = context;
		if (type == null)
			type = Object.class;
		initGUI();
		combo.setSelectedItem(value);
		if (context != null)
			context.getBoxPanelTabPane().addListener(this);
	}

	@Override public void addPropertyChangeListener(PropertyChangeListener p) {
		propSupport.addPropertyChangeListener(p);
	}

	@Override public void removePropertyChangeListener(PropertyChangeListener p) {
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
			POJOPopupMenu menu = new POJOPopupMenu(object);
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
				Collection col = context.getBoxPanelTabPane().findBoxByType(type);
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
				values = new LinkedList(context.getBoxPanelTabPane().findBoxByType(type));
			values.add("<null>");
			setSelectedItem(selected);
		}
	}

}