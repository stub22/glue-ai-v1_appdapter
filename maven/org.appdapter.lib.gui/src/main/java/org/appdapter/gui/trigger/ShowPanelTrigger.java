package org.appdapter.gui.trigger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;

import javax.swing.AbstractButton;

import org.appdapter.api.trigger.AnyOper.UISalient;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.TriggerImpl;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.UIAware;
import org.appdapter.gui.api.WrapperValue;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.swing.SafeJMenuItem;

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

	public String getMenuPath() {
		return "Show " + Utility.spaceCase(Utility.getShortClassName(panelClass));
	}

	@Override public String getDescription() {
		return "Open a panel that is an instance of " + getIdentityObject() + " for " + _object;
	}

	@Override public Class getReturnType() {
		return panelClass;
	}

	public Object valueOf(Box targetBox, ActionEvent actevt, boolean wantSideEffect, boolean isPaste) {
		if (!wantSideEffect)
			return null;
		Object value = getValueOr(targetBox);
		try {
			Method m = ReflectUtils.getDeclaredMethod(panelClass, "focusOnBox", (Class) null);
			if (m != null) {
				value = targetBox;
			} else {
				m = ReflectUtils.getDeclaredMethod(panelClass, "setObject", Object.class);
				if (m == null)
					m = ReflectUtils.getDeclaredMethod(panelClass, "setValue", Object.class);
				if (m == null) {
					getLogger().error("No way to set object in panel " + panelClass);
					return null;
				}
			}
			Object cust = Utility.newInstance(panelClass);
			m.invoke(cust, value);
			//@TODO cust.addPropertyChangeListener(listener)
			getDisplayContext().addObject(null, cust, true);
			return cust;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override Object getIdentityObject() {
		return panelClass;
	}

	@Override public int hashCode() {
		return getIdentityObject().hashCode();
	}

	@Override public void applySalience(UISalient isSalient) {
		// TODO Auto-generated method stub
	}

	@Override public AbstractButton makeMenuItem(final Box b) {
		final ShowPanelTrigger trig = this;
		AbstractButton jmi = new SafeJMenuItem(b, true, getMenuName());
		if (this instanceof UIAware) {
			jmi = (AbstractButton) ((UIAware) this).visitComponent(jmi);
		}
		jmi.addActionListener(trig);
		return jmi;
	}

	@Override void setMenuInfo() {
	}
}