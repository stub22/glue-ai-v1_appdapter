package org.appdapter.gui.swing;

import java.lang.reflect.InvocationTargetException;

import javax.swing.Box;

import org.appdapter.core.convert.NoSuchConversionException;

// To hide the name conflix of Box
public class JBox extends Box implements UISwingReplacement {

	public JBox(int axis) {
		super(axis);
	}

}
