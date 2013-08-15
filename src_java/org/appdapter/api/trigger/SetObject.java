package org.appdapter.api.trigger;

import java.lang.reflect.InvocationTargetException;

import org.appdapter.core.convert.NoSuchConversionException;

public interface SetObject<T> {

	void setObject(T object) throws InvocationTargetException;
}
