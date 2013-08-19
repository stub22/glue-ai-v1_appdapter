package org.appdapter.gui.swing;

import static org.appdapter.core.log.Debuggable.*;
import org.appdapter.api.trigger.Box;
import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.BoxPanelSwitchableView;
import org.appdapter.gui.api.FocusOnBox;
import org.appdapter.gui.api.GetSetObject;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.editors.ObjectPanel;

/**
 * A Tabbed GUI component used to render
 *
 */
abstract public class ScreenBoxPanel<BoxType extends Box> extends ObjectView<BoxType> implements GetSetObject, FocusOnBox<BoxType>, ObjectPanel {

	protected abstract boolean reloadObjectGUI(Object obj) throws Throwable;

	protected abstract void initSubclassGUI() throws Throwable;

	protected abstract void completeSubClassGUI() throws Throwable;

	public void setTabHost(BoxPanelSwitchableView objectTabsForTabbedView) {
		parentTabs = objectTabsForTabbedView;
	}

	protected BoxPanelSwitchableView parentTabs;

	public ScreenBoxPanel() {
		this(null);
	}

	public boolean isObjectBoundGUI() {
		return true;
	}

	public ScreenBoxPanel(Object pojObject) {
		super(true);
		if (pojObject instanceof BT) {
			throw new RuntimeException("Need to pass the rraw object here! " + pojObject);
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
		{
			if (showingInGUI == bean)
				return;
			if (workingOnInGUI == bean)
				return;
			try {
				Object wasObjectValue = objectValue;
				workingOnInGUI = bean;
				reloadObjectGUI(bean);
				if (objectValue == wasObjectValue) {
					objectValue = bean;
				}
				showingInGUI = objectValue;

			} catch (Throwable e1) {
				printStackTrace(e1);
			} finally {
				workingOnInGUI = null;
			}
			try {
				initGUI();
			} catch (Throwable e) {
				printStackTrace(e);
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
				Object objectValue = getValue();
				if (objectValue == null)
					return false;
				completedGuiOnce = true;
				completeSubClassGUI();
			} catch (Throwable e) {
				printStackTrace(e);
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
	private Object showingInGUI;
	private Object workingOnInGUI;

	@Override public void focusOnBox(BoxType b) {
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
				try {
					reallySetValue(bean);
				} catch (Throwable e) {
					printStackTrace(e);
				}
		}

	}

}
