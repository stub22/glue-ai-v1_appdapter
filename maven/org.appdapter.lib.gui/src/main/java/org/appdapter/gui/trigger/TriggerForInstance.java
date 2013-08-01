package org.appdapter.gui.trigger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.text.DefaultEditorKit.PasteAction;

import org.appdapter.api.trigger.AnyOper.AskIfEqual;
import org.appdapter.api.trigger.AnyOper.UIHidden;
import org.appdapter.api.trigger.AnyOper.UISalient;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxContext;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.api.trigger.MutableTrigger;
import org.appdapter.api.trigger.TriggerImpl;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.name.Ident;
import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.BoxPanelSwitchableView;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.DisplayType;
import org.appdapter.gui.api.GetDisplayContext;
import org.appdapter.gui.api.UIAware;
import org.appdapter.gui.api.WrapperValue;
import org.appdapter.gui.browse.KMCTrigger;
import org.appdapter.gui.browse.Utility;

abstract public class TriggerForInstance<BoxType extends Box<? extends MutableTrigger<BoxType>>> extends TriggerImpl implements

ButtonFactory, AskIfEqual, UIAware, Action, ActionListener, KMCTrigger, GetDisplayContext {

	/**
	 *  the Class that this menu item is placed on (using the Box)
	 */
	Class arg0Clazz;
	/**
	 *  the object that this menu item is placed on (using the Box)
	 */
	WrapperValue _object;
	DisplayContext displayContext;
	AbstractButton jmi;

	//abstract protected void fireIT(Box b, ActionEvent e) throws InvocationTargetException;

	public void fireIT(Box targetBox, ActionEvent actevt) throws InvocationTargetException {
		getLogger().debug(this.toString() + " firing on " + targetBox.toString());
		Object obj = valueOf(targetBox, actevt, true, true);
		try {
			Utility.addSubResult(this, targetBox, actevt, obj, getReturnType());
		} catch (PropertyVetoException e) {
			Debuggable.printStackTrace(e);
		}

	}

	public abstract Object valueOf(Box targetBox, ActionEvent actevt, boolean wantSideEffect, boolean isPaste) throws InvocationTargetException;

	private Action actionImpl = new AbstractAction() {

		{
			setEnabled(true);
		}

		@Override public void actionPerformed(ActionEvent e) {
			TriggerForInstance.this.actionPerformed(e);
		}

		public String toString() {
			return getShortLabel();
		}

	};

	final @Override public void actionPerformed(ActionEvent e) {
		try {
			fireIT(Utility.asWrapped(e.getSource()).asBox(), e);
		} catch (InvocationTargetException e1) {
			Debuggable.printStackTrace(e1);
			throw Debuggable.reThrowable(e1);
		}
	}

	@Override public void addPropertyChangeListener(PropertyChangeListener listener) {
		actionImpl.addPropertyChangeListener(listener);
	}

	final @Override public String getShortLabel() {
		return getMenuPath();
	}

	@Override public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		return same(obj);
	}

	@Override public void fire(Box targetBox) {
		try {
			fireIT(targetBox, null);
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	public DisplayContext getDisplayContext() {
		if (displayContext != null)
			return displayContext;
		return Utility.getCurrentContext();
	}

	@Override public Ident getIdent() {
		return super.getIdent();
	}

	abstract Object getIdentityObject();

	final public String getMenuName() {
		String path = getMenuPath().trim();
		while (path.endsWith("|"))
			path = path.substring(0, path.length() - 1);
		String nym = path.substring(path.lastIndexOf('|') + 1);
		if (nym.trim().length() == 0)
			return path;
		return nym;
	}

	abstract public String getMenuPath();

	@Override public Object getValue(String key) {
		return actionImpl.getValue(key);
	}

	public Object getValueOr(Box targetBox) {
		if (_object != null) {
			return Utility.dref(_object, true);
		}
		return Utility.dref(targetBox);
	}

	abstract public int hashCode();

	@Override public boolean isEnabled() {
		return actionImpl.isEnabled();
	}

	abstract public Class getReturnType();

	@Override public void putValue(String key, Object value) {
		actionImpl.putValue(key, value);

	}

	@Override public void removePropertyChangeListener(PropertyChangeListener listener) {
		actionImpl.addPropertyChangeListener(listener);
	}

	@Override public boolean same(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		return toString().equals(obj.toString());
	}

	@Override public void setDescription(String description) {
		if (getValue(Action.NAME) == null)
			putValue(Action.NAME, description);
		super.setDescription(description);
	}

	@Override public void setEnabled(boolean b) {
		actionImpl.setEnabled(b);
	}

	abstract void setMenuInfo();

	@Override public void setShortLabel(String title) {
		putValue(Action.NAME, title);
		super.setShortLabel(title);
	}

	@Override public String toString() {
		String s = getDescription();
		if (s != null)
			return s;
		return getShortLabel();
	}

	@Override public JComponent visitComponent(JComponent comp) {
		if (comp instanceof JMenuItem) {
			jmi = (JMenuItem) comp;
			jmi.setName(getShortLabel());

			String str = getMenuName();
			if (str.trim().length() == 0) {
				jmi.setText(str);
			}
			jmi.setText(str);
			jmi.setToolTipText(getDescription());
			setMenuInfo();
		}
		return jmi;
	}

	abstract public void applySalience(UISalient isSalient);

}
