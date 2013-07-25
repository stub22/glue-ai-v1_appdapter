package org.appdapter.gui.trigger;

import java.lang.reflect.Method;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.TriggerImpl;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.WrapperValue;
import org.appdapter.gui.browse.Utility;

public class ShowPanelTrigger<BT extends Box<TriggerImpl<BT>>> extends TriggerForInstance<BT> {

	final Class panelClass;

	public ShowPanelTrigger(DisplayContext ctx, Class cls, WrapperValue obj, Class fd) {
		arg0Clazz = cls;
		_object = obj;
		panelClass = fd;
		displayContext = ctx;
		setDescription("Open a panel that is an instance of " + panelClass);
		setShortLabel(getMenuName());
	}

	public String getMenuName() {
		return "Show " + Utility.spaceCase(Utility.getShortClassName(panelClass));
	}

	@Override
	public void fireIT(Box targetBox) {
		Object value = getValueOr(targetBox);
		try {
			Method m = ReflectUtils.getDeclaredMethod(panelClass, "focusOnBox", (Class)null);
			if (m != null) {
				value = targetBox;
			} else {
				m = ReflectUtils.getDeclaredMethod(panelClass, "setObject", Object.class);
				if (m == null)
					m = ReflectUtils.getDeclaredMethod(panelClass, "setValue", Object.class);
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

	@Override
	void setMenuInfo() {
		// TODO Auto-generated method stub

	}

	@Override
	Object getIdentityObject() {
		return panelClass;
	}
	
	@Override
	public int hashCode() {
		return getIdentityObject().hashCode();
	}
}