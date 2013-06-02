package org.appdapter.gui.box;

import java.lang.reflect.InvocationTargetException;

public interface GetSetObject {
	Object getValue();

	void setObject(Object obj) throws InvocationTargetException;
}
