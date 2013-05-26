package org.appdapter.gui.pojo;

import java.awt.Component;
import java.beans.Customizer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;

public class UseEditor extends ScreenBoxedPOJOWithPropertiesPanel implements Customizer {

	private Component custEditor;
	private PropertyEditor ec;

	@Override public void setObject(Object bean) {
		Class targetType = bean.getClass();
		PropertyEditor ec = Utility.findEditor(targetType);
		// TODO Auto-generated method stub

	}

	@Override protected void initSubClassGUI() throws Throwable {
		this.custEditor = ec.getCustomEditor();
		tabs.add(this.custEditor);
	}

}
