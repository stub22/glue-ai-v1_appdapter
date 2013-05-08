package org.appdapter.gui.swing;

import java.awt.BorderLayout;
import java.lang.reflect.Constructor;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.appdapter.gui.objbrowser.model.POJOCollectionWithBoxContext;
import org.appdapter.gui.objbrowser.model.Utility;
import org.appdapter.gui.swing.impl.JJPanel;

/**
 * A GUI component showing the parameters for a given constructor.
 * The values can be obtained using getValues.
 *
 * 
 */
public class ConstructorParametersPanel extends JJPanel {
  POJOCollectionWithBoxContext context;
  Constructor currentConstructor = null;
  PropertyValueControl[] paramViews = null;
  JPanel childPanel;

  public ConstructorParametersPanel(POJOCollectionWithBoxContext context) {
    this.context = context;
    setLayout(new BorderLayout());
  }

  public ConstructorParametersPanel(POJOCollectionWithBoxContext context, Constructor c) {
    this.context = context;
    setLayout(new BorderLayout());
    setConstructor(c);
  }

  /**
   * The values currently set in the constructor parameters
   */
  public Object[] getValues() {
    Object[] params = new Object[paramViews.length];
    for (int i = 0; i < paramViews.length; ++i) {
      params[i] = paramViews[i].getValue();
    }
    return params;
  }

  public synchronized void setConstructor(Constructor constructor) {
    if (currentConstructor != constructor) {
      if (childPanel != null)
        childPanel.removeAll();
      childPanel = new JPanel();
      childPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, true));
      if (constructor != null) {
        Class[] params = constructor.getParameterTypes();
        paramViews = new PropertyValueControl[params.length];
        for (int i = 0; i < params.length; ++i) {
          JPanel row = new JPanel();
          row.setLayout(new BorderLayout());
          Class type = params[i];
          String shortName = Utility.getShortClassName(type);
          row.add("West", new JLabel(shortName + ":  "));
          PropertyValueControl field = new PropertyValueControl(context, type, true);
          paramViews[i] = field;
          row.add("Center", field);
          childPanel.add(row);
        }
      }
      removeAll();
      add("Center", childPanel);
      invalidate();
      validate();
      repaint();
    }
    currentConstructor = constructor;

  }
}
