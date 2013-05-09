package org.appdapter.gui.editors;

import org.appdapter.gui.pojo.ScreenBoxedPOJOWithPropertiesPanel;
import org.appdapter.gui.swing.ClassConstructorsPanel;
import org.appdapter.gui.swing.ErrorPanel;
import org.appdapter.gui.swing.StaticMethodsPanel;

/**
 * A panel containing a complete GUI for a class,
 * including properties, methods, static methods,
 * and constructors <p>
 *
 * 
 */
public class ClassCustomizer extends ScreenBoxedPOJOWithPropertiesPanel {
  @Override
protected void initGUI() {
    super.initGUI();

    try {
      ClassConstructorsPanel constructors = new ClassConstructorsPanel((Class) getObject());
      tabs.insertTab("Constructors", null, constructors, null, 0);
    } catch (Exception err) {
      tabs.insertTab("Constructors", null, new ErrorPanel("Could not show constructors", err), null, 0);
    }

    try {
      StaticMethodsPanel statics = new StaticMethodsPanel((Class) getObject());
      tabs.insertTab("Static methods", null, statics, null, 1);
    } catch (Exception err) {
      tabs.insertTab("Static methods", null, new ErrorPanel("Could not show static methods", err), null, 1);
    }
  }
}