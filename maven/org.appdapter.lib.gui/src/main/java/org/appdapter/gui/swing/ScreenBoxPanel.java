package org.appdapter.gui.swing;

import org.appdapter.api.trigger.BT;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.GetObject;
import org.appdapter.gui.api.GetSetObject;
import org.appdapter.gui.api.FocusOnBox;
import org.appdapter.gui.api.Utility;
import org.slf4j.LoggerFactory;

/**
 * A Tabbed GUI component used to render 
 * 
 */
abstract public class ScreenBoxPanel<BoxType extends Box> extends ObjectView<BoxType> implements GetSetObject, FocusOnBox<BoxType> {

	protected abstract boolean reloadObjectGUI(Object obj) throws Throwable;

	protected abstract void initSubclassGUI() throws Throwable;

	protected abstract void completeSubClassGUI() throws Throwable;

	public ScreenBoxPanel() {
		this(null);
	}

	public boolean isObjectBoundGUI() {
		return true;
	}

	public ScreenBoxPanel(Object pojObject) {
		super(true);
		if (pojObject instanceof BT) {
			throw new ClassCastException("Need to pass the rraw object here! " + pojObject);
		}
		try {
			initGUI();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	final protected void reallySetValue(Object bean) {
		if (objClass == null)
			objClass = bean.getClass();
		if (bean != null) {
			if (objectValue == bean)
				return;
			objectValue = bean;
			try {
				reloadObjectGUI(bean);
			} catch (Throwable e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				initGUI();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	boolean initedGuiOnce = false;
	boolean completedGuiOnce = false;

	public final boolean initGUI() throws Throwable {
		synchronized (valueLock) {
			if (!initedGuiOnce) {
				initedGuiOnce = true;
				initSubclassGUI();
			}
			if (isObjectBoundGUI()) {
				Object objectValue = getValue();
				if (objectValue == null)
					return false;
			}
			try {
				if (completedGuiOnce)
					return true;
				completedGuiOnce = true;
				completeSubClassGUI();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				initedGuiOnce = false;
			}
			return initedGuiOnce;
		}
	}

	/**
	 * Set the object to be customized.  This method should be called only
	 * once, before the Customizer has been added to any parent AWT container.
	 * @param bean  The object to be customized.
	 */
	public void setObject(Object bean) {
		synchronized (valueLock) {
			super.setObject(bean);
			if (objectValue == null && bean != null) {
				reallySetValue(bean);
			}
		}
	}

	protected Class objClass;
	protected GetSetObject box;

	@Override public void focusOnBox(Box b) {
		synchronized (valueLock) {
			if (b instanceof GetSetObject)
				box = (GetSetObject) b;
			Object bv = box.getValue();
			if (bv != null)
				setObject(bv);
		}
	}

	@Override public Object getValue() {
		synchronized (valueLock) {
			Object val = null;
			if (box != null) {
				Object bv = box.getValue();
				val = Utility.dref(bv, box);
				if (val != null && val != box) {
					return val;
				}
			}
			return Utility.dref(objectValue, box);
		}
	}

	/**
	 * 
	 * 
	 * Called whenever the pojo is switched. Caused the GUI to update to render
	 * the new pojObject instead.
	 */

	@Override public void objectValueChanged(Object oval, Object bean) {
		synchronized (valueLock) {
			if (oval != null) {
				if (objClass == null)
					objClass = oval.getClass();
			}
			if (objectValue != bean)
				reallySetValue(bean);
		}

	}
}
