package org.appdapter.gui.rimpl;

import java.awt.Color;
import java.beans.FeatureDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.appdapter.api.trigger.AnyOper.UISalient;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.core.component.KnownComponent;
import org.appdapter.gui.api.Utility;
import org.appdapter.gui.util.CollectionSetUtils;
import org.appdapter.gui.util.HRKRefinement.AskIfEqual;

public class TriggerForMethod extends TriggerForInstance implements Comparable<Trigger>, AskIfEqual {

	private UISalient isSalientMethod;

	public void applySalience(UISalient isSalientMethod) {
		if (isSalientMethod == null)
			return;
		this.isSalientMethod = isSalientMethod;
		String mn = isSalientMethod.MenuName();
		if (mn != null && mn.length() > 0) {
			menuName = mn;
		}

	}

	public String menuName;
	FeatureDescriptor _featureDescriptor;

	public TriggerForMethod(DisplayContext ctx, Class cls, Object obj, FeatureDescriptor fd) {
		_clazz = cls;
		_object = obj;
		_featureDescriptor = fd;
		displayContext = ctx;
		setDescription(describeFD(fd));
		setShortLabel(getMenuPath());
	}

	@Override Object getIdentityObject() {
		return CollectionSetUtils.first(getReadMethodObject(_featureDescriptor), _featureDescriptor);
	}

	/**
	 * Compares this object with the specified object for order. 
	 *
	 * <p>The subclasser must ensure <tt>sgn(x.compareTo(y)) ==
	 * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
	 * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
	 * <tt>y.compareTo(x)</tt> throws an exception.)
	 *
	 * @param   o the Trigger to be compared.
	 * @return  a 0 = if the objects are the same
	 *   +/-1 if the triggers have the same side effect
	 *   +/-2 if the triggers have a different side effect 
	 *
	 * @throws ClassCastException if the specified object's type prevents it
	 *         from being compared to this object.
	 */
	@Override public int compareTo(@SuppressWarnings("rawtypes") Trigger o) {
		if (this == o)
			return 0;
		else if (this == null)
			return 2;

		if (equalsByObject(o))
			return 0;

		boolean ej = equalJob(o);

		int labelDif = toString().compareTo(o.toString());
		if (ej)
			return (int) Math.signum(labelDif);
		return labelDif;
	}

	public boolean equalJob(Trigger obj) {
		if (equalsByObject(obj))
			return true;
		if (!(obj instanceof KnownComponent)) {
			return false;
		}
		KnownComponent other = (KnownComponent) obj;
		/// assume they are named the same
		if (other.getShortLabel().equals(getShortLabel()))
			return true;

		// for now assume a different job if a different datatype
		if (!(obj instanceof TriggerForMethod)) {
			return false;
		}

		TriggerForMethod tfi = (TriggerForMethod) obj;
		Class rt = getReturnType();
		if (rt != tfi.getReturnType())
			return false;
		Method rm = this.getMethod();
		if (rm != null) {
			Method om = tfi.getMethod();
			if (om != null) {
				boolean sameMName = rm.getName().equals(om.getName());
				if (sameMName)
					return true;
				return false;
			}
		}
		if (rt == void.class)
			return false;
		return true;

	}

	@Override public boolean equals(Object obj) {
		if (!(obj instanceof Trigger)) {
			return false;
		}
		Trigger other = (Trigger) obj;
		if (equalsByObject(other))
			return true;
		if (equalJob(other))
			return true;
		return false;
	}

	private boolean equalsByObject(Trigger o) {
		if (this == o)
			return true;
		else if (this == null)
			return false;
		if (!(o instanceof TriggerForMethod)) {
			return false;
		}
		TriggerForMethod tfi = (TriggerForMethod) o;
		Method rm = this.getMethod();
		if (rm != null) {
			Method om = tfi.getMethod();
			if (om != null)
				return rm.getName().equals(om.getName());
		}
		if (tfi.getFieldSummary().equals(getFieldSummary()))
			return true;
		return false;
	}

	public void fireIT(Box targetBox) throws InvocationTargetException {
		Method m = getReadMethodObject(_featureDescriptor);
		if (m != null) {
			Class rt = m.getReturnType();
			try {
				Object obj = Utility.invokeFromUI(_object, m);
				if (rt != void.class)
					addSubResult(targetBox, obj, rt);
				return;

			} catch (InvocationTargetException e) {
				throw e;
			} catch (Throwable e) {
				throw new InvocationTargetException(e);
			}
		}
		getLogger().debug(this.toString() + " firing on " + targetBox.toString());
	}

	private Class getDeclaringClass() {
		Method m = getMethod();
		if (m == null)
			return _clazz;
		return m.getDeclaringClass();
	}

	@Override public String getDescription() {
		String myDescription = super.getDescription();
		if (myDescription == null) {
			myDescription = "" + getIdentityObject();
			setDescription(myDescription);
		}
		return myDescription;
	}

	public String getMenuName() {
		if (menuName != null)
			return menuName;
		return (" " + _featureDescriptor.getDisplayName() + " ").replace(" get", "Show ").replace(" set", "Replace ").trim();
	}

	public String getMenuPath() {
		String s = getMenuName();
		if (isStatic()) {
			s = "Static|" + s;
		}
		s = Utility.getShortClassName(classOrFirstInterfaceR(_clazz)) + "|" + s;
		Class getRet = getReturnType();
		/*if (getRet == void.class) {
			s = "Invoke|" + s;
		} else {
			s = _featureDescriptor.getClass().getSimpleName() + "|" + s;
		}*/
		s = s.replace("PropertyDescriptor|", "Show ");
		return s;
	}

	public Method getMethod() {
		Method m = getReadMethodObject(_featureDescriptor);
		return m;
	}

	private Class[] getParameters() {
		Method m = getMethod();
		if (m == null)
			return CLASS0;
		return m.getParameterTypes();
	}

	private Class getReturnType() {
		Method m = getMethod();
		if (m == null)
			return void.class;
		return m.getReturnType();
	}

	@Override public String getShortLabel() {
		String myShortLabel = super.getShortLabel();
		if (myShortLabel == null) {
			myShortLabel = "" + _featureDescriptor;
			setShortLabel(myShortLabel);
		}
		return myShortLabel;
	}

	private boolean isStatic() {
		Method m = getMethod();
		if (m == null)
			return false;
		return Modifier.isStatic(m.getModifiers());
	}

	public void setMenuInfo() {
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