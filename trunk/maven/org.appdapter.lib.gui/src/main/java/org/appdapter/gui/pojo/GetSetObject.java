package org.appdapter.gui.pojo;

import java.lang.reflect.InvocationTargetException;

public interface GetSetObject {
	Object getObject();

	void setObject(Object obj) throws InvocationTargetException;
}
