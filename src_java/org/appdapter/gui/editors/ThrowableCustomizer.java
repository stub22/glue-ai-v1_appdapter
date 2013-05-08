package org.appdapter.gui.editors;

import org.appdapter.gui.pojo.ScreenBoxedPOJOWithProperties;
import org.appdapter.gui.swing.ErrorPanel;

public class ThrowableCustomizer extends ScreenBoxedPOJOWithProperties {
  protected void initGUI() {
    super.initGUI();

    String name;
    Throwable object = (Throwable) getPOJO();
    if (object instanceof Error) {
      name = "Error";
    } else if (object instanceof RuntimeException) {
      name = "RuntimeException";
    } else if (object instanceof Exception) {
      name = "Exception";
    } else {
      name = "Throwable";
    }

    try {
      ErrorPanel errorPanel = new ErrorPanel(object);
      tabs.insertTab(name, null, errorPanel, null, 0);
    } catch (Exception err) {
      tabs.insertTab(name, null, new ErrorPanel("Could not show error info for " + object, err), null, 0);
    }
  }
}