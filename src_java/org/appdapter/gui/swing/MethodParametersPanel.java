package org.appdapter.gui.swing;

import java.awt.BorderLayout;
import java.lang.reflect.Method;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.appdapter.gui.objbrowser.model.POJOCollectionWithBoxContext;
import org.appdapter.gui.objbrowser.model.Utility;
import org.appdapter.gui.swing.impl.JVPanel;

/**
 * A GUI component showing the parameters of a method.
 * The parameter values can be retrieved using getValues().
 * The current method can be changed any time, causing
 * the GUI to update itself for the new method.
 *
 * 
 */
public class MethodParametersPanel extends JVPanel {
  POJOCollectionWithBoxContext context;
  Method currentMethod = null;
  PropertyValueControl[] paramViews = null;
  JPanel childPanel;

  public MethodParametersPanel() {
    this(Utility.getCurrentInstances());
  }

  public MethodParametersPanel(POJOCollectionWithBoxContext context) {
    this.context = context;
    setLayout(new BorderLayout());
  }

  /**
   * Returns the current values set in the method parameters
   */
  public Object[] getValues() {
    Object[] params = new Object[paramViews.length];
    for (int i = 0; i < paramViews.length; ++i) {
      params[i] = paramViews[i].getValue();
    }
    return params;
  }

  public Method getMethod() {
    return currentMethod;
  }

  public synchronized void setMethod(Method method) {
    if (currentMethod != method) {
      if (childPanel != null) {
        childPanel.removeAll();
      }
      childPanel = new JPanel();
      childPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, true));
      if (method != null) {
        Class[] params = method.getParameterTypes();
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
    currentMethod = method;

  }
}