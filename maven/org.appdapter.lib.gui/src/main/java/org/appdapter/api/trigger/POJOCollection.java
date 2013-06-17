package org.appdapter.api.trigger;

import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.Iterator;


public interface POJOCollection extends UIProvider {

	public void addListener(POJOCollectionListener objectChoice);

	public DisplayContext getDisplayContext();

	public Iterator<Object> getObjects();

	public Object findObjectByName(String n);

	public Collection findObjectsByType(Class type);

	public BT findOrCreateBox(Object newObject);

	public void renameObject(String oldName, String newName) throws PropertyVetoException;

}
