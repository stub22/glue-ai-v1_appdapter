package org.appdapter.gui.pojo;

import org.appdapter.api.trigger.Box;

/**
 * A GUI component used to render a POJO in a user interface. The standard
 * implementations are ScreenBoxedPOJORef (an icon-like implementation) and
 * ScreenBoxedPOJOWithProperties (a complete window with all the details about
 * the POJO).
 */
abstract public class ScreenBoxedPOJOPanel<BoxType extends Box> extends
	AbstractScreenBoxedPOJOPanel<BoxType> {
	private Object pojObject;

	@Override
	public void focusOnBox(Box b) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public ScreenBoxedPOJOPanel() {
	}

	public ScreenBoxedPOJOPanel(Object pojObject) {
		if (pojObject instanceof POJOBox) {
			throw new ClassCastException("Need to pass the rraw object here! " + pojObject);
		}
		this.pojObject = pojObject;
	}

	@Override
	public Object getObject() {
		return pojObject;
	}

	@Override
	public void setObject(Object newpojObject) {
		Object oldpojObject = pojObject;
		if (oldpojObject != newpojObject) {
			pojObject = newpojObject;
			objectChanged(oldpojObject, newpojObject);
		}
	}

	/**
	 * 
	 * 
	 * Called whenever the pojo is switched. Caused the GUI to update to render
	 * the new pojObject instead.
	 */
	@Override
	abstract protected void objectChanged(Object oldpojObject,
			Object newpojObject);
}