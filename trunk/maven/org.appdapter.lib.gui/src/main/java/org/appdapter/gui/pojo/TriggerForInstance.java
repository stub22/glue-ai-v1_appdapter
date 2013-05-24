package org.appdapter.gui.pojo;

import java.awt.Color;
import java.beans.EventSetDescriptor;
import java.beans.FeatureDescriptor;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxContext;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.api.trigger.TriggerImpl;
import org.appdapter.core.name.Ident;
import org.appdapter.gui.box.ScreenBoxPanel;
import org.appdapter.gui.browse.ButtonTabComponent;
import org.appdapter.gui.util.PromiscuousClassUtils;

public class TriggerForInstance extends TriggerImpl implements UIAware {

	Class _clazz;
	Object _object;
	FeatureDescriptor _featureDescriptor;

	@Override public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if (!(obj instanceof Trigger)) {
			return false;
		}
		Trigger other = (Trigger) obj;
		if (other.toString().equals(toString()))
			return true;
		if (other instanceof TriggerForInstance) {
			TriggerForInstance tfi = (TriggerForInstance) obj;
			Method rm = this.getMethod();
			if (rm != null) {
				Method om = tfi.getMethod();
				if (om != null)
					return rm.getName().equals(om.getName());
			}
		}
		return false;
	}

	@Override public void setIdent(Ident id) {
		super.setIdent(id);
	}

	@Override public Ident getIdent() {
		return super.getIdent();
	}

	@Override public String getDescription() {
		String myDescription = super.getDescription();
		if (myDescription == null) {
			myDescription = "" + _featureDescriptor;
			setDescription(myDescription);
		}
		return myDescription;
	}

	@Override public void setDescription(String description) {
		super.setDescription(description);
	}

	@Override public String getShortLabel() {
		String myShortLabel = super.getShortLabel();
		if (myShortLabel == null) {
			myShortLabel = "" + _featureDescriptor;
			setShortLabel(myShortLabel);
		}
		return myShortLabel;
	}

	@Override public void setShortLabel(String description) {
		super.setShortLabel(description);
	}

	@Override public String toString() {
		return getDescription();
	}

	public TriggerForInstance(Class cls, Object obj, FeatureDescriptor fd) {
		_clazz = cls;
		_object = obj;
		_featureDescriptor = fd;
		setDescription(describeFD(fd));
		setShortLabel(getMenuName());
	}

	public String getMenuName() {
		return (" " + _featureDescriptor.getDisplayName() + " ").replace(" get", "Show ").replace(" set", "Replace ");
	}

	private static String describeFD(FeatureDescriptor fd) {
		return fd.getName() + " " + fd.getShortDescription() + " isExpert=" + fd.isExpert() + " isHidden=" + fd.isHidden() + " " + fd.getClass().getSimpleName() + " " + getReadMethodObject(fd);
	}

	@Override public void fire(Box targetBox) {
		try {
			fireIT(targetBox);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

	}

	public Method getMethod() {
		Method m = getReadMethodObject(_featureDescriptor);
		return m;
	}

	static Method getReadMethodObject(FeatureDescriptor _featureDescriptor) {
		if (_featureDescriptor instanceof MethodDescriptor) {
			MethodDescriptor md = (MethodDescriptor) _featureDescriptor;
			return md.getMethod();
		}
		if (_featureDescriptor instanceof EventSetDescriptor) {
			EventSetDescriptor md = (EventSetDescriptor) _featureDescriptor;
			return null;//md.getGetListenerMethod();
		}
		if (_featureDescriptor instanceof PropertyDescriptor) {
			PropertyDescriptor md = (PropertyDescriptor) _featureDescriptor;
			Method m = md.getReadMethod();
			if (m != null)
				return m;
			Method m2 = md.getWriteMethod();
			if (m2 != null)
				return m2;
		}
		return null;
	}

	static Method getAnyMethodObject(FeatureDescriptor _featureDescriptor) {
		if (_featureDescriptor instanceof MethodDescriptor) {
			MethodDescriptor md = (MethodDescriptor) _featureDescriptor;
			return md.getMethod();
		}
		if (_featureDescriptor instanceof EventSetDescriptor) {
			EventSetDescriptor md = (EventSetDescriptor) _featureDescriptor;
			return md.getGetListenerMethod();//md.getGetListenerMethod();
		}
		if (_featureDescriptor instanceof PropertyDescriptor) {
			PropertyDescriptor md = (PropertyDescriptor) _featureDescriptor;
			Method m = md.getReadMethod();
			if (m != null)
				return m;
			Method m2 = md.getWriteMethod();
			if (m2 != null)
				return m2;
		}
		return null;
	}

	public void fireIT(Box targetBox) throws InvocationTargetException {
		try {
			Method m = getReadMethodObject(_featureDescriptor);
			if (m != null) {
				Class rt = m.getReturnType();
				Object obj = Utility.invoke(_object, m);
				if (rt != void.class)
					addSubResult(targetBox, obj, rt);
				return;
			}
			getLogger().debug(this.toString() + " firing on " + targetBox.toString());
		} catch (InvocationTargetException e) {
			throw e;
		} catch (Exception e) {
			throw new InvocationTargetException(e);
		}
	}

	private void addSubResult(Box targetBox, Object obj, Class expected) {
		expected = PromiscuousClassUtils.nonPrimitiveTypeFor(expected);
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
				POJOApp app = Utility.getCurrentPOJOApp();
				app.showScreenBox(obj);
				return;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
		DisplayType dt = POJOBox.getDisplayType(expected);
		switch (dt) {
		case TREE: {
			POJOBox boxed = Utility.findOrCreateBox(obj);
			BoxContext bc = targetBox.getBoxContext();
			bc.contextualizeAndAttachChildBox(targetBox, boxed);
			return;
		}
		case TOSTRING:
			Utility.setLastResult(this, obj, expected);
			return;
		}
		try {
			Utility.getCurrentContext().showScreenBox(obj);
		} catch (Exception e) {
			POJOBox boxed = Utility.findOrCreateBox(obj);
			BoxContext bc = targetBox.getBoxContext();
			ScreenBoxPanel pnl = boxed.getPropertiesPanel();
			switch (dt) {
			case PANEL: {
				JTabbedPane jtp = Utility.repoNav.getBoxPanelTabPane();
				jtp.add(pnl);
				return;
			}
			case MODAL: {
				JComponent jtp = Utility.repoNav.getDesk();
				jtp.add(pnl);
				return;
			}
			default: {
				JComponent jtp = Utility.repoNav.getDesk();
				jtp.add(pnl);
				break;
			}
			}
		}
	}

	JMenuItem jmi;

	@Override public void visitComponent(JComponent comp) {
		if (comp instanceof JMenuItem) {
			jmi = (JMenuItem) comp;
			setMenuInfo();
		}

	}

	private void setMenuInfo() {
		jmi.setText(getMenuName());
		jmi.setToolTipText(getDescription());
		if (_featureDescriptor instanceof PropertyDescriptor) {
			jmi.setBackground(Color.GREEN);
		}
		Method m = getReadMethodObject(_featureDescriptor);
		if (m == null) {
			jmi.setBackground(Color.RED);
			return;
		}
		Class[] pts = m.getParameterTypes();
		boolean isStatic = Modifier.isStatic(m.getModifiers());
		if (isStatic) {
			jmi.setBackground(Color.ORANGE);
		}
		int needsArgument = pts.length;
		if (isStatic)
			needsArgument = pts.length - 1;
		if (needsArgument > 1) {
			jmi.setForeground(Color.GRAY);
			jmi.setBackground(Color.BLACK);
		} else {
			if (needsArgument > 0) {
				jmi.setForeground(Color.GRAY);
			}
		}

	}
}
