package org.appdapter.gui.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.appdapter.api.trigger.Box;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.GetSetObject;
import org.appdapter.gui.browse.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesPanel<BoxType extends Box> extends ScreenBoxPanel<BoxType> implements GetSetObject {
	static Logger theLogger = LoggerFactory.getLogger(PropertiesPanel.class);

	public static Class EDITTYPE = Object.class;

	//DisplayContext context = new EmptyPOJOCollectionContext();
	//Object objectValue = null;
	Class objClass;

	private PropertyComparator propertyComparator = new PropertyComparator();

	private DisplayContext context;
	boolean staticOnly = false;
	boolean showFields = false;

	// private LessString lessString = new LessString();
	public PropertiesPanel() {
		this(Utility.getDisplayContext(), null, null, false, true);
	}

	public PropertiesPanel(DisplayContext context, Object val, Class objClass, boolean staticOnly, boolean showFields) {
		this.context = context;
		this.objClass = objClass;
		this.staticOnly = staticOnly;
		this.showFields = showFields;
		final Object val0 = val;
		setObject(val0);
	}

	@Override public Object getValue() {
		return objectValue;
	}

	@Override public Class<? extends Object> getClassOfBox() {
		return Object.class;
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
			return (o instanceof PropertiesPanel.PropertyComparator);
		}
	}

	@Override protected void initSubclassGUI() {
		removeAll();
		//setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		//setLayout(new VerticalLayout(VerticalLayout.LEFT, true));
		setLayout(new BorderLayout());

		JPanel buttonPanel = new JJPanel(new FlowLayout(FlowLayout.LEFT));
		JButton reloadButton = new JButton("Refresh Properties");
		buttonPanel.add(reloadButton);
		reloadButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent event) {
				reloadObjectGUI(getValue());
			}
		});
		this.add(buttonPanel);
	}

	@Override protected void completeSubClassGUI() {
		initSubclassGUI();

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
				HashSet<String> propsShown = new HashSet<String>();

				while (it.hasNext()) {
					PropertyDescriptor p = (PropertyDescriptor) it.next();
					String attributeName = p.getDisplayName();
					propsShown.add(attributeName);
					PropertyValueControl pvc = new PropertyValueControl(context, attributeName, source, p);
					sheet.add(attributeName + ":", pvc);
				}
				if (propsShown.size() == 0)
					showFields = true;

				if (showFields) {
					for (Field f : ReflectUtils.getAllFields(objClass)) {
						String attributeName = f.getName();
						for (String s : propsShown) {
							if (ReflectUtils.matchesName(attributeName, s))
								f = null;
							break;
						}
						if (f == null)
							continue;
						if (this.staticOnly) {
							if (!ReflectUtils.isStatic(f))
								continue;
						}
						propsShown.add(attributeName);
						PropertyValueControl pvc = new PropertyValueControl(context, attributeName, source, f);
						sheet.add(attributeName + ":", pvc);
					}
				}
				add("Center", sheet);
			} catch (Exception err) {
				theLogger.error("An error occurred", err);
			}
		}

	}

	@Override protected boolean reloadObjectGUI(Object val) {
		if (val == this) {
			return true;
		}
		objectValue = val;
		if (objClass == null && objectValue != null) {
			objClass = val.getClass();
		}

		Utility.replaceRunnable(this, new Runnable() {
			public void run() {
				if (objectValue != null) {
					completeSubClassGUI();
				} else {
					initSubclassGUI();
				}
			};
		});
		return true;
	}

}
