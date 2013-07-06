package org.appdapter.gui.rimpl;

import java.lang.reflect.Method;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.gui.api.Utility;
import org.appdapter.gui.box.WrapperValue;

public class ShowPanelTrigger extends TriggerForInstance {

	Class panelClass;

	public ShowPanelTrigger(DisplayContext ctx, Class cls, WrapperValue obj, Class fd) {
		_clazz = cls;
		_object = obj;
		panelClass = fd;
		displayContext = ctx;
		setDescription("Open a panel that is an instance of " + panelClass);
		setShortLabel(getMenuName());
	}

	public String getMenuName() {
		return "Show " + Utility.spaceCase(Utility.getShortClassName(panelClass));
	}

	@Override public void fireIT(Box targetBox) {
		Object value = getValueOr(targetBox);
		try {
			Method m = Utility.getDeclaredMethod(panelClass, "focusOnBox", targetBox.getClass());
			if (m != null) {
				value = targetBox;
			} else {
				m = Utility.getDeclaredMethod(panelClass, "setObject", Object.class);
				if (m == null)
					m = Utility.getDeclaredMethod(panelClass, "setValue", Object.class);
				if (m == null) {
					getLogger().error("No way to set object in panel " + panelClass);
					return;
				}
			}
			Object cust = Utility.newInstance(panelClass);
			m.invoke(cust, value);
			//@TODO cust.addPropertyChangeListener(listener)
			getDisplayContext().attachChildUI(null, cust, true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override void setMenuInfo() {
		// TODO Auto-generated method stub

	}

	@Override Object getIdentityObject() {
		return panelClass;
	}
}