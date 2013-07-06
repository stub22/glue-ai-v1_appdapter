package org.appdapter.gui.swing;

import java.awt.BorderLayout;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.GetSetObject;
import org.appdapter.gui.api.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesPanel extends ScreenBoxPanel implements GetSetObject {
	static Logger theLogger = LoggerFactory.getLogger(PropertiesPanel.class);

	//DisplayContext context = new EmptyPOJOCollectionContext();
	//Object objectValue = null;
	Class objClass;

	private PropertyComparator propertyComparator = new PropertyComparator();

	private DisplayContext context;
	boolean staticOnly = false;

	// private LessString lessString = new LessString();

	public PropertiesPanel(DisplayContext context, Object val, Class objClass, boolean staticOnly) {
		this.context = context;
		this.objClass = objClass;
		this.staticOnly = staticOnly;
		setObject(val);
	}

	@Override public Object getValue() {
		return objectValue;
	}

	public void setObject(Object val) {
		if (val == this) {
			return;
		}
		this.objectValue = val;
		if (objClass == null) {
			objClass = val.getClass();
		}
		if (objClass == objectValue) {
			reloadObjectGUI(null);
		} else {
			reloadObjectGUI(val);
		}
	}

	/*private JComponent createRow(PropertyDescriptor descriptor) {
	  JPanel panel = new JPanel();
	  BorderLayout layout = new BorderLayout(10, 5);
	  panel.setLayout(layout);
	  panel.add("West", new JLabel(descriptor.getDisplayName() + ":"));
	  panel.add("Center", new PropertyValueControl(context, val, descriptor));
	  return panel;
	} */

	/* class DougysTableModel extends AbstractTableModel {
	   public int getRowCount() {
	   }
	   public int getColumnCount() {
	   }
	   public Object getValueAt(int row, int column) {
	   }
	 }*/

	class PropertyComparator implements Comparator {
		@Override public int compare(Object first, Object second) {
			PropertyDescriptor a = (PropertyDescriptor) first;
			PropertyDescriptor b = (PropertyDescriptor) second;
			String nameA = a.getName();
			String nameB = b.getName();
			return nameA.compareTo(nameB);
		}

		@Override public boolean equals(Object o) {
			return (o instanceof PropertyComparator);
		}
	}

	@Override protected void initSubclassGUI() {
		removeAll();
		//setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		//setLayout(new VerticalLayout(VerticalLayout.LEFT, true));
		setLayout(new BorderLayout());
	}

	@Override protected void completeSubClassGUI() {
		removeAll();
		setLayout(new BorderLayout());

		Object source = getValue();
		if (source != null) {
			try {
				PropertySheet sheet = new PropertySheet();

				if (objClass == null) {
					objClass = source.getClass();
				}
				BeanInfo info = Introspector.getBeanInfo(objClass);

				java.util.List props = Arrays.asList(info.getPropertyDescriptors());
				Collections.sort(props, propertyComparator);

				Iterator it = props.iterator();

				while (it.hasNext()) {
					PropertyDescriptor p = (PropertyDescriptor) it.next();
					String attributeName = p.getDisplayName();
					PropertyValueControl pvc = new PropertyValueControl(context, attributeName, source, p);
					sheet.add(attributeName + ":", pvc);
				}
				add("Center", sheet);
			} catch (Exception err) {
				theLogger.error("An error occurred", err);
			}
		}

	}

	@Override protected boolean reloadObjectGUI(Object val) {
		objectValue = val;
		if (val != null) {
			completeSubClassGUI();
		} else {
			initSubclassGUI();
		}
		return true;
	}

}
