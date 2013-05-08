package org.appdapter.gui.pojo;

import org.appdapter.api.trigger.Box;
import org.appdapter.gui.box.ScreenBoxPanel;
import org.appdapter.gui.objbrowser.model.POJOSwizzler;

/**
 * A GUI component used to render a POJO in a user interface. The standard
 * implementations are ScreenBoxedPOJORef (an icon-like implementation) and
 * ScreenBoxedPOJOWithProperties (a complete window with all the details about
 * the POJO).
 */
abstract public class ScreenBoxedPOJO<BoxType extends Box> extends
		ScreenBoxPanel<BoxType> {
	private Object pojObject;

	@Override
	public void focusOnBox(Box b) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public ScreenBoxedPOJO() {
	}

	public ScreenBoxedPOJO(Object pojObject) {
		if (pojObject instanceof POJOSwizzler) {
			throw new ClassCastException("Need to pass the rraw object here! " + pojObject);
		}
		this.pojObject = pojObject;
	}

	public Object getPOJO() {
		return pojObject;
	}

	public void setBean(Object newpojObject) {
		Object oldpojObject = pojObject;
		if (oldpojObject != newpojObject) {
			pojObject = newpojObject;
			objectChanged(oldpojObject, newpojObject);
		}
	}

	/**
	 * Called whenever the pojo is switched. Caused the GUI to update to render
	 * the new pojObject instead.
	 */
	abstract protected void objectChanged(Object oldpojObject,
			Object newpojObject);
}