package org.appdapter.gui.api;

import java.lang.reflect.InvocationTargetException;

import org.appdapter.core.convert.NoSuchConversionException;

public interface SetObject {

	void setObject(Object object) throws InvocationTargetException, NoSuchConversionException;
}
