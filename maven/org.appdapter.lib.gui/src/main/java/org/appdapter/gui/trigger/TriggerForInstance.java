package org.appdapter.gui.trigger;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.appdapter.api.trigger.AnyOper.AskIfEqual;
import org.appdapter.api.trigger.AnyOper.UISalient;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxContext;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.api.trigger.TriggerImpl;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.name.Ident;
import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.BoxPanelSwitchableView;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.DisplayType;
import org.appdapter.gui.api.UIAware;
import org.appdapter.gui.browse.Utility;

abstract public class TriggerForInstance<B extends Box<TriggerImpl<B>>> extends TriggerImpl<B> implements AskIfEqual, UIAware, Action {

	Class arg0Clazz;

	Object _object;

	private Action actionImpl = new AbstractAction() {

		{
			setEnabled(true);
		}

		@Override public void actionPerformed(ActionEvent e) {
			TriggerForInstance.this.actionPerformed(e);

		}

		public String toString() {
			return getMenuPath();
		}

	};

	DisplayContext displayContext;

	protected UISalient isSalientMethod;
	JMenuItem jmi;

	@Override public void actionPerformed(ActionEvent e) {
		try {
			fireIT(Utility.asWrapped(e.getSource()).asBox());
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
			throw Debuggable.reThrowable(e1);
		}
	}

	@Override public void addPropertyChangeListener(PropertyChangeListener listener) {
		actionImpl.addPropertyChangeListener(listener);
	}

	protected void addSubResult(Box targetBox, Object obj, Class expected) throws PropertyVetoException {
		expected = ReflectUtils.nonPrimitiveTypeFor(expected);
		if (Number.class.isAssignableFrom(expected)) {
			expected = String.class;
			obj = "" + obj;
		}
		if (Enum.class.isAssignableFrom(expected)) {
			expected = String.class;
			obj = "" + obj;
		}
		if (obj == null) {
			obj = "Null " + expected;
			expected = String.class;
		}
		if (expected == String.class) {
			Utility.setLastResult(this, obj, expected);
			try {
				Utility.browserPanel.showMessage("" + obj);
				return;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
		DisplayType dt = Utility.getDisplayType(expected);
		final DisplayType edt = dt;
		if (dt == DisplayType.TREE) {
			BT boxed = Utility.getTreeBoxCollection().findOrCreateBox(null, obj);
			BoxContext bc = targetBox.getBoxContext();
			bc.contextualizeAndAttachChildBox((Box) targetBox, (MutableBox) boxed);
			return;
		}
		if (dt == DisplayType.TOSTRING) {
			Utility.setLastResult(this, obj, expected);
			return;
		}
		try {
			Utility.getCurrentContext().showScreenBox(obj);
		} catch (Exception e) {
			BT boxed = Utility.getTreeBoxCollection().findOrCreateBox(null, obj);
			BoxContext bc = targetBox.getBoxContext();
			JPanel pnl = boxed.getPropertiesPanel();
			if (dt == DisplayType.FRAME) {
				BoxPanelSwitchableView jtp = Utility.getBoxPanelTabPane();
				jtp.addComponent(pnl.getName(), pnl, DisplayType.FRAME);
				return;
			}
			BoxPanelSwitchableView jtp = Utility.getBoxPanelTabPane();
			jtp.addComponent(pnl.getName(), pnl, DisplayType.PANEL);

		}

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
			fireIT(targetBox);
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	abstract public void fireIT(Box targetBox) throws InvocationTargetException;

	DisplayContext getDisplayContext() {
		if (displayContext != null)
			return displayContext;
		return Utility.getCurrentContext();
	}

	@Override public Ident getIdent() {
		return super.getIdent();
	}

	abstract Object getIdentityObject();

	abstract public String getMenuName();

	public String getMenuPath() {
		String s = getMenuName();
		return s;
	}

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
		return getMenuPath();
	}

	@Override public void visitComponent(JComponent comp) {
		if (comp instanceof JMenuItem) {
			jmi = (JMenuItem) comp;
			jmi.setText(getMenuName());
			jmi.setToolTipText(getDescription());
			setMenuInfo();
		}
	}

}
