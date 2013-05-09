package org.appdapter.gui.swing;

import java.awt.BorderLayout;

import javax.swing.JLabel;

import org.appdapter.gui.pojo.POJOCollectionWithBoxContext;
import org.appdapter.gui.pojo.ScreenBoxedPOJORef;
import org.appdapter.gui.pojo.Utility;
import org.appdapter.gui.swing.impl.JVPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A panel showing the output of a method execution.
 * Used by MethodsPanel.
 *
 * 
 */
class MethodResultPanel extends JVPanel {
  static Logger theLogger = LoggerFactory.getLogger(MethodResultPanel.class);

  JLabel label;
  ScreenBoxedPOJORef value = null;
  POJOCollectionWithBoxContext context;
  boolean isVoid = false;

  public MethodResultPanel(POJOCollectionWithBoxContext context) {
    this.context = context;
    label = new JLabel("Return value:  ");
    //value = new PropertyValueControl(false);
    setLayout(new BorderLayout());
    add("West", label);
    //add("Center", value);
  }

  public MethodResultPanel() {
    this(Utility.getCurrentContext());
  }

  /**
   * Designates the type of return value
   */
  public void setResultType(Class type) {
    if (type == Void.TYPE) {
      label.setText("(no return value)");
      isVoid = true;
    } else {
      String name = Utility.getShortClassName(type);
      label.setText("Return value (" + name + "):  ");
      //value.setFixedType(type);
      isVoid = false;
    }
    invalidate();
    validate();
  }

  /**
   * Sets the actual return value to be displayed.
   */
  public void setResultValue(Object object) {
    try {
      if (!(isVoid && object == null)) {
        if (value != null) {
          remove(value);
        }
        value = new ScreenBoxedPOJORef(context, object, true, true, true);

        add("Center", value);
        invalidate();
        validate();
      }
    } catch (Exception err) {
      theLogger.error("An error occurred", err);
    }
  }
}
