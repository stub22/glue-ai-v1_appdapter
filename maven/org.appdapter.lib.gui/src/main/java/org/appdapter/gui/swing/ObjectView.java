package org.appdapter.gui.swing;

import javax.swing.JPanel;

import org.appdapter.api.trigger.Box;
import org.appdapter.gui.api.*;
import org.appdapter.gui.box.WrapperValue;
import org.appdapter.gui.impl.JJPanel;
import org.slf4j.LoggerFactory;

/**
 * A GUI component used to render a Box in a user interface. The standard
 * implementations are ScreenBoxedPOJORef (an icon-like implementation) and
 * ScreenBoxedPOJOWithProperties (a complete window with all the details about
 * the POJO).
 */
abstract public class ObjectView<BoxType extends Box>

extends JJPanel implements GetSetObject {

	abstract public void focusOnBox(Box b);

	public Object valueLock = new Object();

	public ObjectView(boolean initForTabbedHosting) {
		super(initForTabbedHosting);
	}

	protected abstract boolean initGUI() throws Throwable;

	/**
	 * Return the live object in which we think we are updating
	 * 
	 * This can be 'this' object
	 * 
	 */
	@Override abstract public Object getValue();

	public Object objectValue;

	public void setObject(Object newpojObject) {
		synchronized (valueLock) {
			Object oldpojObject = objectValue;
			if (oldpojObject != newpojObject) {
				objectChanged(oldpojObject, newpojObject);
				try {
					initGUI();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Called whenever the pojo is switched. Caused the GUI to update to render
	 * the new pojObject instead.
	 */
	final protected void objectChanged(Object oldValue, Object newValue) {
		synchronized (valueLock) {
			objectValueChanged(oldValue, newValue);
			if (objectValue != newValue) {
				objectValue = newValue;
				super.firePropertyChange("value", oldValue, newValue);
			}
		}
	}

	public abstract void objectValueChanged(Object oldValue, Object newValue);

	abstract protected void reallySetValue(Object bean);
}