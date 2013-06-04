package org.appdapter.gui.editors;

import org.appdapter.gui.pojo.ScreenBoxedPOJOWithPropertiesPanel;
import org.appdapter.gui.swing.ErrorPanel;

public class ThrowableCustomizer extends ScreenBoxedPOJOWithPropertiesPanel {
	@Override protected void initSubClassGUI() {

		String name;
		Throwable object = (Throwable) getValue();
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