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

public class PropertiesPanel extends SingleTabFrame implements GetSetObject {
	static Logger theLogger = LoggerFactory.getLogger(PropertiesPanel.class);

	//DisplayContext context = new EmptyPOJOCollectionContext();
	//Object objectValue = null;
	Class objClass;

	private PropertyComparator propertyComparator = new PropertyComparator();

	private DisplayContext context;

	// private LessString lessString = new LessString();

	public PropertiesPanel(DisplayContext context, Object val, Class objClass) {
		if (context != null)
			this.context = context;
		this.objClass = objClass;
		setObject(val);
	}

	public PropertiesPanel(Object val) {
		this(null, val, Utility.getClassNullOk(val));
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

		removeAll();
		//setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		//setLayout(new VerticalLayout(VerticalLayout.LEFT, true));
		setLayout(new BorderLayout());

		if (val != null) {
			try {
				PropertySheet sheet = new PropertySheet();

				BeanInfo info = Introspector.getBeanInfo(val.getClass());

				java.util.List props = Arrays.asList(info.getPropertyDescriptors());
				Collections.sort(props, propertyComparator);

				Iterator it = props.iterator();

				while (it.hasNext()) {
					PropertyDescriptor p = (PropertyDescriptor) it.next();
					sheet.add(p.getDisplayName() + ":", new PropertyValueControl(context, val, p));
				}
				add("Center", sheet);
			} catch (Exception err) {
				theLogger.error("An error occurred", err);
			}
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

	@Override protected void initSubclassGUI() throws Throwable {
	}

	@Override protected void completeSubClassGUI() throws Throwable {
		removeAll();
		//setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		//setLayout(new VerticalLayout(VerticalLayout.LEFT, true));
		setLayout(new BorderLayout());
		Object val = getValue();
		if (val != null) {
			try {
				PropertySheet sheet = new PropertySheet();

				BeanInfo info = Utility.getBeanInfo(objClass);

				java.util.List props = Arrays.asList(info.getPropertyDescriptors());
				Collections.sort(props, propertyComparator);

				Iterator it = props.iterator();

				while (it.hasNext()) {
					PropertyDescriptor p = (PropertyDescriptor) it.next();
					sheet.add(p.getDisplayName() + ":", new PropertyValueControl(context, val, p));
				}
				add("Center", sheet);
			} catch (Exception err) {
				theLogger.error("An error occurred", err);
			}
		}

	}

	@Override protected boolean reloadObjectGUI(Object obj) throws Throwable {
		Debuggable.notImplemented();
		return false;
	}

}
