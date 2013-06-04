package org.appdapter.gui.pojo;

import org.appdapter.api.trigger.Box;

/**
 * A Tabbed GUI component used to render 
 * 
 */
abstract public class SingleTabFrame<BoxType extends Box> extends AbstractScreenBoxedPOJOPanel<BoxType> {

	@Override public void focusOnBox(Box b) {
		setObject(b);
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public SingleTabFrame() {
	}

	public SingleTabFrame(Object pojObject) {
		if (pojObject instanceof POJOBox) {
			throw new ClassCastException("Need to pass the rraw object here! " + pojObject);
		}
		this.lastFocus = pojObject;
		try {
			initGUI();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	Object lastFocus;

	@Override public Object getValue() {
		return lastFocus;
	}

	/**
	 * 
	 * 
	 * Called whenever the pojo is switched. Caused the GUI to update to render
	 * the new pojObject instead.
	 */
	@Override abstract public void objectValueChanged(Object oldpojObject, Object newpojObject);
}