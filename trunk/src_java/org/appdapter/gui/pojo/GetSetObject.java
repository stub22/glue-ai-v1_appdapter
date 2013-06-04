package org.appdapter.gui.pojo;

import java.lang.reflect.InvocationTargetException;

public interface GetSetObject {
	Object getValue();

	void setObject(Object obj) throws InvocationTargetException;
}
