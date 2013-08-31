package org.appdapter.gui.editors;

import java.awt.Color;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.Type;

public class StringEditor extends PropertyEditorSupport {
	public StringEditor() {

	}

	public static Type[] EDITTYPE = new Type[] { String.class };

	@Override public void setAsText(String s) {
		setValue(s);
	}

	@Override public String getAsText() {
		return (String) getValue();
	}
}