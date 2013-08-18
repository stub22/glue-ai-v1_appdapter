package org.appdapter.gui.swing;

import static org.appdapter.core.log.Debuggable.printStackTrace;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditorSupport;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.appdapter.core.convert.NoSuchConversionException;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.api.POJOCollectionListener;
import org.appdapter.gui.browse.SearchableDemo;
import org.appdapter.gui.browse.ToFromKeyConverter;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.trigger.TriggerPopupMenu;

/**
 * A GUI widget that lets you choose among a set of objects
 * of a certain type.
 *
 */
public class ObjectChoiceComboPanel extends JJPanel implements POJOCollectionListener, MouseListener {

	PropertyEditorSupport editorSupport = new PropertyEditorSupport();

	final NamedObjectCollection context;
	PropertyChangeSupport propSupport = new PropertyChangeSupport(this);

	Class type;
	JComboBox combo;
	Model model;
	ToFromKeyConverter converter;

	public boolean useStringProxies;

	public ObjectChoiceComboPanel(Class type, Object value) {
		this(null, type, value, Utility.getToFromStringConverter(type));
	}

	public ObjectChoiceComboPanel(NamedObjectCollection context0, Class type0, Object value, ToFromKeyConverter conv) {
		super(false);
		this.converter = conv;
		this.type = type0;
		if (context0 == null)
			context0 = Utility.getTreeBoxCollection();
		this.context = context0;
		if (type == null) {
			Utility.bug("type of value unknown: " + value);
			type = Object.class;
			useStringProxies = false;
		} else {
			useStringProxies = Utility.isToStringType(type) && type != String.class;
		}
		initGUI();
		if (context != null)
			context.addListener(this, true);
		combo.setSelectedItem(value);
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
		Object whatWasSelectedObj = model.getSelectedBean();

		if (whatWasSelectedObj != object) {
			Utility.bug("SetSelection broken  on this " + this);
		}
	}

	@Override public void pojoAdded(Object obj, BT box, Object senderCollection) {
		if (type.isInstance(obj))
			model.reload();
	}

	@Override public void pojoRemoved(Object obj, BT box, Object senderCollection) {
		if (type.isInstance(obj))
			model.reload();
	}

	private void initGUI() {
		model = new Model();
		combo = new JComboBox(model);
		combo.setEditable(false);
		combo.setRenderer(new ObjectComboPrettyRender());
		setLayout(new BorderLayout());
		add("Center", combo);
		combo.addMouseListener(this);
		SearchableDemo.installSearchable(combo);
	}

	@Override public void mouseClicked(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showMenu(e.getX() + 5, e.getY() + 5, e);
		}
	}

	@Override public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showMenu(e.getX() + 5, e.getY() + 5, e);
		}
	}

	@Override public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showMenu(e.getX() + 5, e.getY() + 5, e);
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

	private void showMenu(int x, int y, MouseEvent e) {
		Object object = model.getSelectedBean();
		if (object != null) {
			TriggerPopupMenu menu = new TriggerPopupMenu(null, e, null);
			menu.addMenuFromObject(object);
			add(menu);
			menu.show(this, x, y);
		}
	}

	public String objectToString(Object object) {
		if (object == null)
			return "<null>";
		if (object instanceof String)
			return (String) object;
		try {
			if (converter != null) {
				Object key = converter.toKey(object);
				if (key instanceof String)
					return (String) key;

			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return Utility.getUniqueName(object, context);
	}

	public Object stringToObject(String title) {
		if (title.equals("<null>"))
			return null;
		Object obj = stringToObjectImpl(title);
		if (obj == null || (type != null && !type.isInstance(obj))) {
			obj = stringToObjectImpl(title);
			Utility.bug("stringToObjectImpl producing inccorect " + type + ": " + obj);
		}
		return obj;
	}

	public Object stringToObjectImpl(String title) {
		if (title == null || title.equals("<null>"))
			return null;
		if (type == String.class)
			return title;

		if (converter != null) {
			Object obj = converter.fromKey(title, type);
			if (obj != null) {
				return obj;
			}
		}
		Object obj = context.findObjectByName(title);
		if (obj != null)
			return obj;
		if (Utility.isToStringType(type)) {
			try {
				return Utility.fromString(title, type);
			} catch (NoSuchConversionException e) {
				printStackTrace(e);
			}
		}
		return null;
	}

	static class ObjectComboPrettyRender extends JLabel implements ListCellRenderer {

		public ObjectComboPrettyRender() {
			setOpaque(true);
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);
		}

		/*
		* This method finds the image and text corresponding
		* to the selected value and returns the label, set up
		* to display the text and image.
		*/
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			//Get the selected index. (The index param isn't
			//always valid, so just use the value.)
			int selectedIndex = ((Integer) value).intValue();

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			//Set the icon and text.  If icon was null, say so.
			ImageIcon icon = null;// images[selectedIndex];
			String title = Utility.getUniqueNamePretty(value);
			if (icon != null) {
				setIcon(icon);
			} else {
				//setUhOhText(pet + " (no image available)", list.getFont());
			}
			setText(title);
			setFont(list.getFont());

			return this;
		}

	}

	class Model extends AbstractListModel implements ComboBoxModel {
		//Vector listeners = new Vector();
		java.util.List<Object> objectValues;
		Object selectedObject = null;

		@SuppressWarnings("unchecked") public Model() {
			reload();
		}

		@Override public synchronized void setSelectedItem(Object anItem) {
			if (anItem instanceof String) {
				if (ObjectChoiceComboPanel.this.useStringProxies) {

					anItem = stringToObject((String) anItem);
				}
			}
			if (!Debuggable.isRelease()) {
				String title = objectToString(anItem);
				Object obj = stringToObject(title);
				if (obj != anItem) {
					Utility.bug("Not round tripping " + anItem);
				}
			}
			if (selectedObject != anItem) {
				Object oldValue = selectedObject;
				selectedObject = anItem;
				propSupport.firePropertyChange("selection", oldValue, anItem);
			}
		}

		@Override public Object getSelectedItem() {
			return selectedObject;
		}

		public Object getSelectedBean() {
			return selectedObject;
		}

		@Override public int getSize() {
			return objectValues.size();
		}

		@Override public Object getElementAt(int index) {
			try {
				return objectValues.get(index);
			} catch (Exception err) {
				return null;
			}
		}

		public synchronized void reload() {
			Object selected = getSelectedBean();
			if (context == null)
				objectValues = new LinkedList();
			else {
				Collection col = context.findObjectsByType(type);
				objectValues = new LinkedList();
				for (Object o : col) {

					objectValues.add(o);
				}
			}
			objectValues.add("<null>");
			setSelectedItem(selected);
		}
	}

	public Object getValue() {
		return model.getSelectedBean();
	}

}