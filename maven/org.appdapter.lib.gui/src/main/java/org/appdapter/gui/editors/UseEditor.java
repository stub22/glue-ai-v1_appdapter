package org.appdapter.gui.editors;

import java.awt.Component;
import java.beans.Customizer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;

import org.appdapter.api.trigger.Box;
import org.appdapter.gui.api.GetSetObject;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.swing.ScreenBoxPanel;

public class UseEditor<BoxType extends Box> extends ScreenBoxPanel<BoxType> implements Customizer, ObjectPanel {

	private PropertyEditor ec;
	private Component custEditor;

	public UseEditor() {

	}

	public UseEditor(PropertyEditor editor, Class objectClass, GetSetObject getSetObject) {
		box = getSetObject;
		objClass = objectClass;
		ec = editor;
		setObject(box.getValue());
	}

	/**
	 * Register a listener for the PropertyChange event.  The customizer
	 * should fire a PropertyChange event whenever it changes the target
	 * bean in a way that might require the displayed properties to be
	 * refreshed.
	 *
	 * @param listener  An object to be invoked when a PropertyChange
	 *		event is fired.
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		super.addPropertyChangeListener(listener);
	}

	/**
	 * Remove a listener for the PropertyChange event.
	 *
	 * @param listener  The PropertyChange listener to be removed.
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		super.removePropertyChangeListener(listener);
	}

	@Override protected void completeSubClassGUI() throws Throwable {
		removeAll();
		this.custEditor = ec.getCustomEditor();
		add(this.custEditor);
	}

	@Override protected boolean reloadObjectGUI(Object object) {
		if (object != null) {
			if (box != null) {
				try {
					box.setObject(object);
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			objClass = object.getClass();
		}
		if (ec == null && objClass != null) {
			ec = Utility.findEditor(objClass);
		}
		return false;
	}

	@Override protected void initSubclassGUI() throws Throwable {
		if (ec == null && objClass != null) {
			ec = Utility.findEditor(objClass);
		}

	}

	@Override public Class<? extends Object> getClassOfBox() {
		return Object.class;
	}

}
