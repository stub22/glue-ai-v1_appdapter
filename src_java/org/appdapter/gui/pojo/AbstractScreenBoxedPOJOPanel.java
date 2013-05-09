package org.appdapter.gui.pojo;

import org.appdapter.api.trigger.Box;
import org.appdapter.gui.box.ScreenBoxPanel;

/**
 * A GUI component used to render a POJO in a user interface. The standard
 * implementations are ScreenBoxedPOJORef (an icon-like implementation) and
 * ScreenBoxedPOJOWithProperties (a complete window with all the details about
 * the POJO).
 */
abstract public class AbstractScreenBoxedPOJOPanel<BoxType extends Box> extends ScreenBoxPanel<BoxType> implements GetSetObject {

	@Override
	public void focusOnBox(Box b) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * Return the live object in which we think we are updating
	 * 
	 * This can be 'this' object
	 * 
	 */
	@Override
	abstract public Object getObject();

	public void setObject(Object newpojObject) {
		Object oldpojObject = getObject();
		if (oldpojObject != newpojObject) {
			objectChanged(oldpojObject, newpojObject);
		}
	}

	/**
	 * Called whenever the pojo is switched. Caused the GUI to update to render
	 * the new pojObject instead.
	 */
	protected void objectChanged(Object oldValue, Object newValue) {
		super.firePropertyChange("value", oldValue, newValue);
	}

}