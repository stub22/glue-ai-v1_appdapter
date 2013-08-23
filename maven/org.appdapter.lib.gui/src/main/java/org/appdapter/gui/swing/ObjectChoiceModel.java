package org.appdapter.gui.swing;

import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComponent;

import org.appdapter.core.convert.ToFromKeyConverter;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.browse.Utility;

class ObjectChoiceModel extends AbstractListModel implements ComboBoxModel {
	//Vector listeners = new Vector();
	java.util.List<Object> objectValues;
	Object selectedObject = null;

	final Class type;
	final NamedObjectCollection context;
	private ToFromKeyConverter<Object, String> converter;

	final JComponent combo;
	final public boolean useStringProxies;
	final PropertyChangeSupport propSupport;

	@SuppressWarnings("unchecked") public ObjectChoiceModel(NamedObjectCollection noc, Class cls, ToFromKeyConverter<Object, String> convert, JComponent comp, PropertyChangeSupport ps) {
		context = noc;
		type = cls;
		this.converter = convert;
		useStringProxies = convert != null;
		combo = comp;
		propSupport = ps;
	}

	@Override public synchronized void setSelectedItem(Object anItem) {
		if (anItem instanceof String) {
			if (useStringProxies) {
				anItem = this.stringToObject((String) anItem);
			}
		}
		if (!Debuggable.isRelease()) {
			String title = this.objectToString(anItem);
			Object obj = stringToObject(title);
			if (obj != anItem) {
				Utility.bug("Not round tripping " + anItem);
			}
		}
		if (selectedObject != anItem) {
			Object oldValue = selectedObject;
			selectedObject = anItem;
			combo.setToolTipText(Utility.makeTooltipText(selectedObject));
			propSupport.firePropertyChange("selection", oldValue, anItem);
		}
	}

	private String objectToString(Object anItem) {
		return converter.toKey(anItem);
	}

	private Object stringToObject(String title) {
		return converter.fromKey(title, type);
	}

	@Override public Object getSelectedItem() {
		return selectedObject;
	}

	public Object getSelectedBean() {
		return selectedObject;
	}

	@Override public int getSize() {
		if (objectValues == null)
			return 0;
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
			Class ft = type;
			Collection col = context.findObjectsByType(ft);
			if (col.size() == 0) {
				Utility.theLogger.warn("col.size() == 0 for " + ft);
			}
			objectValues = new LinkedList();
			for (Object o : col) {

				objectValues.add(o);
			}
		}
		objectValues.add(ObjectChoiceComboPanel.NULLOBJECT);
		setSelectedItem(selected);
	}
}