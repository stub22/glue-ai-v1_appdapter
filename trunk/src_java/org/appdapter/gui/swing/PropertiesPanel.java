

package org.appdapter.gui.swing;

import java.awt.BorderLayout;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.appdapter.gui.pojo.POJOCollectionWithBoxContext;
import org.appdapter.gui.pojo.Utility;
import org.appdapter.gui.swing.impl.JJPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesPanel extends JJPanel {
  static Logger theLogger = LoggerFactory.getLogger(PropertiesPanel.class);

  POJOCollectionWithBoxContext context = new EmptyPOJOCollectionContext();
  Object object = null;

  private PropertyComparator propertyComparator = new PropertyComparator();

  // private LessString lessString = new LessString();

  public PropertiesPanel(POJOCollectionWithBoxContext context, Object object) {
    if (context != null)
      this.context = context;
    setBean(object);
  }

  public PropertiesPanel(Object object) {
    this(null, object);
  }


  public void setBean(Object object) {
    this.object = object;
    removeAll();
    //setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    //setLayout(new VerticalLayout(VerticalLayout.LEFT, true));
    setLayout(new BorderLayout());

    if (object != null) {
      try {
        PropertySheet sheet = new PropertySheet();

        BeanInfo info = Utility.getPOJOInfo(object.getClass());

        java.util.List props = Arrays.asList(info.getPropertyDescriptors());
        Collections.sort(props, propertyComparator);

        Iterator it = props.iterator();

        while(it.hasNext()) {
          PropertyDescriptor p = (PropertyDescriptor) it.next();
          sheet.add(
            p.getDisplayName() + ":",
            new PropertyValueControl(context, object, p)
          );
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
    panel.add("Center", new PropertyValueControl(context, object, descriptor));
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
    @Override
	public int compare(Object first, Object second) {
      PropertyDescriptor a = (PropertyDescriptor) first;
      PropertyDescriptor b = (PropertyDescriptor) second;
      String nameA = a.getName();
      String nameB = b.getName();
      return nameA.compareTo(nameB);
    }

    @Override
	public boolean equals(Object o) {
      return (o instanceof PropertyComparator);
    }
  }
}
