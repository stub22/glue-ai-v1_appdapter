package org.appdapter.gui.trigger;

import static org.appdapter.gui.trigger.TriggerMenuFactory.describeMethod;

import java.awt.Color;
import java.beans.FeatureDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.appdapter.api.trigger.AnyOper.AskIfEqual;
import org.appdapter.api.trigger.AnyOper.UISalient;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.api.trigger.TriggerImpl;
import org.appdapter.core.component.KnownComponent;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.WrapperValue;
import org.appdapter.gui.browse.PropertyDescriptorForField;
import org.appdapter.gui.browse.Utility;

public class TriggerForMethod<BT extends Box<TriggerImpl<BT>>> extends TriggerForInstance<BT> implements Comparable<Trigger>, AskIfEqual {
	//extends TriggerForInstance implements Comparable<Trigger>, AskIfEqual {

	final boolean isDeclNonStatic;

	public void applySalience(UISalient uiSalient) {
		if (uiSalient == null)
			return;
		this.isSalientMethod = uiSalient;
		String mn = uiSalient.MenuName();
		if (mn != null && mn.length() > 0) {
			menuName = mn;
		}

	}

	public boolean appliesTarget(Class cls) {
		List<Class> params = getParameters();
		if (params.size() > 0)
			return params.get(0).isAssignableFrom(cls);
		return false;
	}

	public boolean noOperand() {
		List<Class> params = getParameters();
		return params.size() < 2;
	}

	public boolean appliesToOperand(Class cls) {
		if (cls == null || cls == Void.class || cls == void.class) {
			return noOperand();
		}
		List<Class> params = getParameters();
		if (params.size() > 1)
			return params.get(1).isAssignableFrom(cls);
		return false;
	}

	public TriggerForInstance createTrigger(String prependMenu, DisplayContext ctx, WrapperValue poj) {
		return new TriggerForMethod(prependMenu, ctx, arg0Clazz, poj, method, true, featureDesc);
	}

	public String menuName;
	public String prependMenu;
	public Method method;
	final public FeatureDescriptor featureDesc;
	public PropertyDescriptorForField propDesc;

	//FeatureDescriptor _featureDescriptor;

	/*public TriggerForMethod(DisplayContext ctx, Class cls, Object obj, FeatureDescriptor fd) {
		_clazz = cls;
		_object = obj;
		_featureDescriptor = fd;
		displayContext = ctx;
		setDescription(describeFD(fd));
		setShortLabel(getMenuPath());
	}*/

	public TriggerForMethod(String prepend, DisplayContext ctx, Class cls, Object obj, Method fd, boolean isDeclNonStatic0, FeatureDescriptor feature) {
		prependMenu = prepend;
		arg0Clazz = cls;
		displayContext = ctx;
		method = fd;
		featureDesc = feature;
		_object = obj;
		isDeclNonStatic = isDeclNonStatic0;
		if (isDeclNonStatic && !ReflectUtils.isStatic(method)) {
			Debuggable.warn("");
		}
		String desc = describeMethod(fd);
		if (featureDesc instanceof PropertyDescriptorForField) {
			propDesc = (PropertyDescriptorForField) feature;
			desc = desc + " " + propDesc.getShortDescription();
			menuName = propDesc.getSyntheticName(method);

			setDescription(desc);
		} else {
			setDescription(desc);
		}
		setShortLabel(getMenuPath());
	}

	@Override Object getIdentityObject() {
		return method;
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
		String osl = other.getShortLabel();
		if (osl != null && osl.equals(getShortLabel()))
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
				if (sameMName) {
					if (om.getParameterTypes().length != rm.getParameterTypes().length) {
						return false;
					}
					return true;
				}
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
		Method m = getMethod();
		if (m != null) {
			Class rt = ReflectUtils.nonPrimitiveTypeFor(m.getReturnType());
			try {
				Object tryValue = targetBox;
				if (_object != null) {
					tryValue = _object;
				}
				Object obj = Utility.invokeFromUI(tryValue, m);
				if (obj != null) {
					Class rc = obj.getClass();
					if (!rt.isAssignableFrom(rc)) {
						rt = rc;
					}

				}
				if (rt != Void.class)
					addSubResult(targetBox, obj, rt);
				return;

			} catch (InvocationTargetException e) {
				throw e;
			} catch (Throwable e) {
				e.printStackTrace();
				throw new InvocationTargetException(e);
			}
		}
		getLogger().debug(this.toString() + " firing on " + targetBox.toString());
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
		return (" " + getMethod().getName() + " ").replace(" get", "Show ").replace(" set", "Replace ").trim();
	}

	public String getMenuPath() {
		String s = getMenuName();
		if (!isDeclNonStatic && ReflectUtils.isStatic(getMethod())) {
			s = "Static|" + s;
		}
		Class fi = getMethod().getDeclaringClass();//  classOrFirstInterfaceR(_clazz);
		Object o1 = Utility.dref(_object);
		if (Proxy.isProxyClass(fi)) {

			if (propDesc != null) {
				fi = propDesc.getField().getDeclaringClass();
			}
		}

		String shortClassName = Utility.getShortClassName(fi);

		//	if (true || ((o1 != null && o1.getClass() != _clazz) || _clazz.getDeclaredMethods().length > 6)) {
		s = shortClassName + "|" + s;
		//	}
		Class getRet = getReturnType();
		/*if (getRet == void.class) {
			s = "Invoke|" + s;
		} else {
			s = _featureDescriptor.getClass().getSimpleName() + "|" + s;
		}*/
		s = s.replace("PropertyDescriptor|", "Show ");
		s = s.replace("ScalaObject|", "");
		s = prependMenu + s;
		if (o1 != null && !isDeclNonStatic && /*!_clazz.isInstance(o1)*/fi != o1.getClass()) {
			s = "Indirectly|" + s;
		} else {
			return s;
		}
		return s;
	}

	public Method getMethod() {
		return method;
	}

	public List<Class> getParameters() {
		Method m = getMethod();
		ArrayList<Class> al = new ArrayList<Class>();
		if (m == null)
			return al;
		if (!ReflectUtils.isStatic(m))
			al.add(m.getDeclaringClass());
		for (Class c : m.getParameterTypes()) {
			al.add(c);
		}
		return al;
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
			myShortLabel = "" + getMethod();
			setShortLabel(myShortLabel);
		}
		return myShortLabel;
	}

	public void setMenuInfo() {
		Method m = getMethod();
		if (m == null) {
			jmi.setBackground(Color.RED);
			return;
		}
		boolean isStatic = !isDeclNonStatic && ReflectUtils.isStatic(m);
		if (isStatic) {
			jmi.setBackground(Color.ORANGE);
		}

		int needsArgumentsTotal = getParameters().size();

		if (_object != null)
			needsArgumentsTotal--;

		if (needsArgumentsTotal > 1) {
			jmi.setForeground(Color.WHITE);
			jmi.setBackground(Color.BLACK);
		} else if (needsArgumentsTotal > 2) {
			jmi.setForeground(Color.GRAY);
			jmi.setBackground(Color.BLACK);
		} else {
			if (needsArgumentsTotal > 0) {
				jmi.setForeground(Color.GRAY);
			}
		}
	}

	@Override public int hashCode() {
		Method rm = this.getMethod();
		if (rm != null)
			return rm.hashCode();
		return toString().hashCode();
	}
}