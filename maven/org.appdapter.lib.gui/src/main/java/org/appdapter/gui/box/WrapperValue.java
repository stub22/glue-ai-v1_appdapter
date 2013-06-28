package org.appdapter.gui.box;

import java.beans.PropertyVetoException;

public interface WrapperValue {

	/*abstract public Iterable<Class> getTypes();

	abstract public Iterable<Object> getObjects();
	*/
	abstract public Class getObjectClass();

	/**
	 * Returns the object that this value wrapper represents
	 */
	abstract public Object getValue();

	//abstract public Object getValueOrThis();

	abstract public void reallySetValue(Object newObject) throws UnsupportedOperationException;
}
