package org.appdapter.gui.trigger;

import java.beans.PropertyVetoException;
import java.util.concurrent.Callable;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.CallableWithParameters;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.api.trigger.TriggerImpl;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.EditableTrigger;
import org.appdapter.gui.browse.Utility;

public class EditableTriggerImpl extends TriggerImpl implements EditableTrigger, CallableWithParameters, TriggerForClass {

	Trigger trigger;
	CallableWithParameters callableWithParameters;

	CallableWithParameters predicateWithParameters;
	Object objectValue;

	String menuPath = null;

	public EditableTriggerImpl(String shortLabel, Trigger t) {
		trigger = t;
		setMenuPath(shortLabel);
	}

	public EditableTriggerImpl(Class cls, String shortLabel, Trigger t) {
		setRequireClass(cls);
		trigger = t;
		setMenuPath(shortLabel);
	}

	public EditableTriggerImpl(final Class cls, String shortLabel, CallableWithParameters t) {
		setRequireClass(cls);
		callableWithParameters = t;
		setMenuPath(shortLabel);
	}

	public EditableTriggerImpl(CallableWithParameters<Boolean, Box> predicate, String menuLabel, CallableWithParameters function) {
		callableWithParameters = function;
		predicateWithParameters = predicate;
		setMenuPath(menuLabel);
	}

	public EditableTriggerImpl(CallableWithParameters<Boolean, Box> predicate, String menuLabel, Trigger t) {
		predicateWithParameters = predicate;
		trigger = t;
		setMenuPath(menuLabel);
	}

	public EditableTriggerImpl(EditableTriggerImpl editableTriggerImpl, String menuFmt, Object poj) {
		if (menuFmt == null) {
			menuFmt = editableTriggerImpl.getMenuPath();
		}
		objectValue = poj;
		trigger = editableTriggerImpl.trigger;
		predicateWithParameters = editableTriggerImpl.predicateWithParameters;
		callableWithParameters = editableTriggerImpl.callableWithParameters;
		setDescription(editableTriggerImpl.getDescription());
		setIdent(editableTriggerImpl.getIdent());
		setMenuPath(menuFmt);
	}

	protected void setMenuPath(String menuFmt) {
		menuFmt = checkMenuPath(menuFmt);
		menuPath = menuFmt;
		setShortLabel(menuFmt);
		String desc = getDescription();
		if (desc == null) {
			desc = Debuggable.toInfoStringF(this);
			setDescription(desc);
		}
	}

	protected String checkMenuPath(String menuFmt) {
		if (menuFmt == null || menuFmt.length() == 0) {
			menuFmt = null;
			getLogger().warn("Bad menu path: " + this);
		}
		return menuFmt;
	}

	public String getMenuPath() {
		return checkMenuPath(menuPath);
	}

	public void setRequireClass(final Class cls) {
		predicateWithParameters = new CallableWithParameters() {
			@Override public Object call(Object box, Object... params) {
				Object v = Utility.dref(box);
				return v instanceof Class && cls.isAssignableFrom((Class) v);
			}
		};
	}

	@Override public void fire(Box targetBox) {
		if (trigger != null) {
			trigger.fire(targetBox);
			return;
		}

		if (callableWithParameters != null) {
			Object result = call(getValue(targetBox));
			try {
				Utility.addSubResult(this, targetBox, null, result, null);
			} catch (PropertyVetoException e) {
				Debuggable.printStackTrace(e);
			}
			return;
		}

		getLogger().warn("No trigger impl for " + this);
	}

	protected Object getValue(Box targetBox) {
		if (objectValue != null)
			return objectValue;
		return targetBox.getValue();
	}

	@Override public Object call(Object box, Object... params) {
		Object use = box;
		if (box == null) {
			use = objectValue;
		} else if (box instanceof Box) {
			use = getValue((Box) box);
		}
		if (callableWithParameters != null) {
			return callableWithParameters.call(use, params);
		}
		fire(Utility.asBoxed(use));
		return null;
	}

	@Override public Object getIdentityObject() {
		return this;
	}

	@Override public boolean appliesTarget(Class cls, Object example) {
		if (predicateWithParameters != null) {
			return predicateWithParameters.call(cls, example) == Boolean.TRUE;
		}
		if (objectValue != null)
			return example == objectValue;

		return true;
	}

	@Override public EditableTriggerImpl createTrigger(String menuFmt, DisplayContext ctx, Object poj) {
		return new EditableTriggerImpl(this, menuFmt, poj);
	}

	@Override public boolean isFavorited() {
		return true;
	}

	@Override public boolean isSideEffectSafe() {
		return false;
	}
}
