package org.appdapter.gui.trigger;

import static org.appdapter.gui.trigger.TriggerMenuFactory.describeMethod;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.FeatureDescriptor;
import java.beans.PropertyVetoException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import javax.swing.AbstractButton;
import javax.swing.JMenu;

import org.appdapter.api.trigger.AnyOper.AskIfEqual;
import org.appdapter.api.trigger.AnyOper.UISalient;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.api.trigger.TriggerImpl;
import org.appdapter.core.component.KnownComponent;
import org.appdapter.core.convert.NoSuchConversionException;
import org.appdapter.core.convert.OptionalArg;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.UIAware;
import org.appdapter.gui.browse.PropertyDescriptorForField;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.swing.SafeJCheckBoxMenuItem;
import org.appdapter.gui.swing.SafeJMenuItem;

import com.hp.hpl.jena.rdf.model.Container;

public class TriggerForMember<BT extends Box<TriggerImpl<BT>>> extends TriggerForInstance<BT> implements Comparable<Trigger>, AskIfEqual, TriggerForClass {
	//extends TriggerForInstance implements Comparable<Trigger>, AskIfEqual {

	@UISalient
	static public boolean PrefixWithIndirectyWhenIndirect = false;

	/**
	 * 
	 *   isDeclNonStatic = true .. for static Methods can be treated like instance methods 
	 *      by placing the menu target as the first param
	 */
	boolean isDeclNonStatic;
	protected UISalient isSalientMethod;

	/**
	 *  can invoke a method without changing things
	 */
	public Member member;
	public FeatureDescriptor featureDesc;
	Class[] params;
	Object[] filledInParams;
	private String menuFormat;
	public PropertyDescriptorForField propDesc;
	Object retvalCache;
	boolean isSideEffectSafe;

	// means the _object is ingnored
	boolean isDeclaredLikeStatic;

	public TriggerForMember(String menuName, DisplayContext ctx, Class cls, Object obj, Member fd, boolean isDeclNonStatic0, FeatureDescriptor feature, boolean hasNoSideEffects) {
		init(menuName, ctx, cls, obj, fd, isDeclNonStatic0, feature);
		member = fd;
		isSideEffectSafe = hasNoSideEffects;
		recheckSideEffects(fd);
	}

	private void recheckSideEffects(Member fd) {
		if (!isSideEffectSafe) {
			if (fd != null && nonPrimReturnType() != Void.class) {
				if (getMissingParametersAtPopup() == 0) {
					String memberName = fd.getName();
					if (memberName.startsWith("get") || memberName.startsWith("is")) {
						if (isSafeOfSideEffects(getDeclaringClass()) && isSafeOfSideEffects(getReturnType())) {
							isSideEffectSafe = true;
						}
					}
				}
			}
			if (isSideEffectSafe)
				theLogger.warn("isSideEffectSafe " + fd);
		}
	}

	private boolean isSafeOfSideEffects(Class clz) {
		if (Component.class.isAssignableFrom(clz))
			return false;
		if (Thread.class.isAssignableFrom(clz))
			return false;
		return true;
	}

	@Override public boolean appliesTarget(Class cls, Object anyObject) {
		Class classOfBox = getDeclaringClass();
		return ReflectUtils.convertsTo(anyObject, cls, classOfBox);
	}

	public boolean appliesToOperand(Class cls) {
		if (cls == null || cls == Void.class || cls == void.class) {
			return noOperand();
		}
		Class[] params = getParameters();
		if (params.length > 1)
			return ReflectUtils.isAssignableFrom(params[1], cls);
		return false;
	}

	public void applySalience(UISalient uiSalient) {
		if (uiSalient == null)
			return;
		this.isSalientMethod = uiSalient;
		String mn = uiSalient.MenuName();
		if (mn != null && mn.length() > 0) {
			setShortLabel(mn);
			setMenuInfo();
		}
		if (uiSalient.IsSideEffectSafe()) {
			isSideEffectSafe = true;
		}
		if (uiSalient.IsNotSideEffectSafe()) {
			isSideEffectSafe = false;
		}
		if (uiSalient.TreatLikeStatic()) {
			isDeclaredLikeStatic = true;
		}
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

	public TriggerForInstance createTrigger(String menuFmt, DisplayContext ctx, Object poj) {
		if (menuFmt == null)
			menuFmt = this.menuFormat;
		return new TriggerForMember(menuFmt, ctx, arg0Clazz, poj, member, isDeclNonStatic, featureDesc, isSideEffectSafe);
	}

	public boolean equalJob(Trigger obj) {

		if (obj instanceof TriggerForClass)

			if (equalsByObject(obj))
				return true;
		if (!(obj instanceof KnownComponent)) {
			return false;
		}
		if (!(obj instanceof TriggerForType))
			return false;
		if (true)
			return getIdentityObject() == ((TriggerForType) obj).getIdentityObject();

		KnownComponent other = (KnownComponent) obj;
		/// assume they are named the same
		String osl = other.getShortLabel();
		if (osl != null && osl.equals(getShortLabel()))
			return true;

		// for now assume a different job if a different datatype
		if (!(obj instanceof TriggerForMember)) {
			return false;
		}

		TriggerForMember tfi = (TriggerForMember) obj;
		Class rt = getReturnType();
		if (rt != tfi.getReturnType())
			return false;
		Member rm = this.getMember();
		if (rm != null) {
			Member om = tfi.getMember();
			if (om != null) {
				boolean sameMName = rm.getName().equals(om.getName());
				if (sameMName) {
					if (ReflectUtils.getParameterTypes(om).length != ReflectUtils.getParameterTypes(rm).length) {
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
		if (!(o instanceof TriggerForMember)) {
			return false;
		}
		TriggerForMember tfi = (TriggerForMember) o;
		Member rm = this.getMember();
		if (rm != null) {
			Member om = tfi.getMember();
			if (om != null)
				return rm.getName().equals(om.getName());
		}
		if (tfi.getFieldSummary().equals(getFieldSummary()))
			return true;
		return false;
	}

	public void fireIT(Box targetBox, ActionEvent actevt) throws InvocationTargetException {
		getLogger().debug(Debuggable.toInfoStringArgV(this, " firing on ", targetBox, " with " + actevt));
		Object obj = valueOf(targetBox, actevt, true, true);
		Class rt = nonPrimReturnType();
		try {
			Utility.addSubResult(this, targetBox, actevt, obj, rt);
		} catch (PropertyVetoException e) {
			Debuggable.printStackTrace(e);
		}

	}

	public Class getDeclaringClass() {
		if (arg0Clazz != null)
			return arg0Clazz;
		Class fi = getMember().getDeclaringClass();//  classOrFirstInterfaceR(_clazz);
		if (propDesc != null) {
			fi = propDesc.getField().getDeclaringClass();
		}
		return fi;
	}

	@Override public String getDescription() {
		String myDescription = super.getDescription();
		if (myDescription == null) {
			myDescription = "" + getIdentityObject();
			setDescription(myDescription);
		}
		return myDescription;
	}

	public @Override Object getIdentityObject() {
		return member;
	}

	public Member getMember() {
		if (propDesc != null) {
			return propDesc.getField();
		}
		return member;
	}

	public String getMenuPath() {
		if (menuFormat != null && !menuFormat.contains("%") && menuFormat.length() > 1)
			// pre-named?
			return menuFormat;
		Object o1 = Utility.dref(_object);
		Class tdc = getDeclaringClass();
		Class mdc = member.getDeclaringClass();
		return getMenuPath(menuFormat, !isDeclNonStatic && ReflectUtils.isStatic(member), o1, tdc, mdc, getReturnType(), new Callable() {
			@Override public Object call() throws Exception {
				return valueOf(null, null, false, false);
			}
		}, member.getName(), getMissingParametersAtPopup(), new Utility.UtilityOptionalArgs(), this);
	}

	static public String getMenuPath(String menuFormat, boolean isStatic, Object o1, Class tdc, Class mdc, Class getReturnType,

	Callable valueOf, String memberName, int missingParamsTotal, OptionalArg optionals, TriggerForMember memb) {
		String s = menuFormat;
		if (s == null || s.length() == 0) {
			s = "%c|%m";
		}
		if (isStatic) {
			s = "Static|" + s;
		}
		Class fi = mdc;
		if (s.contains("%c")) {
			String strval = Utility.getShortClassName(fi);
			//	if (true || ((o1 != null && o1.getClass() != _clazz) || _clazz.getDeclaredMethods().length > 6)) {
			if (o1 != null && fi != o1.getClass()) {
				//strval = "Indirectly|" + strval;
			}
			s = replace(s, "%c", strval);
		}
		if (s.contains("%d")) {
			fi = mdc;
			String strval = Utility.getShortClassName(fi);
			if (o1 != null && !fi.isInstance(o1)) {
				if (PrefixWithIndirectyWhenIndirect)
					strval = "Indirectly|" + strval;
			}
			s = replace(s, "%d", strval);
		}
		if (s.contains("%o")) {
			String strval = Utility.getUniqueName(ReflectUtils.recastOrNull(o1, tdc, "<NoConversion>"));
			s = replace(s, "%o", strval);
		}
		if (s.contains("%i")) {
			fi = mdc;
			if (!fi.isInterface()) {
				Class[] fis = fi.getInterfaces();
				if (fis.length > 0) {
					fi = fis[0];
				}
				if (!fi.isInterface()) {
					if (o1 != null) {
						fi = o1.getClass();
						fis = fi.getInterfaces();
						if (fis.length > 0) {
							fi = fis[0];
						}
					}
				}
				if (!fi.isInterface()) {
					fis = fi.getInterfaces();
					if (fis.length > 0) {
						fi = fis[0];
					}
				}
				if (!fi.isInterface()) {
					fi = tdc;
				}
				if (!fi.isInterface()) {
					fis = fi.getInterfaces();
					if (fis.length > 0) {
						fi = fis[0];
					}
				}
			}
			String strval = Utility.getShortClassName(fi);
			if (o1 != null && !fi.isInstance(o1)) {
				if (PrefixWithIndirectyWhenIndirect)
					strval = "Indirectly|" + strval;
			}
			s = replace(s, "%i", strval);
		}

		if (s.contains("%r")) {
			String strval = Utility.getShortClassName(getReturnType);
			s = replace(s, "%r", strval);
		}

		if (s.contains("%v")) {
			String strval = "";
			try {
				Object obj = valueOf.call();
				if (obj != null) {
					strval = strval + Utility.getUniqueName(obj);
				}
			} catch (Exception e) {
			}
			s = replace(s, "%v", strval);
		}
		if (s.contains("%m")) {
			String strval = memberName;
			if (false && strval.length() > 4) {
				if (Character.isUpperCase(strval.charAt(3))) {
					strval = replace(strval, "get", "Show");
					strval = replace(strval, "set", "Replace");
				} else if (Character.isUpperCase(strval.charAt(2))) {
					strval = replace(strval, "is", "Show");
				}
			}
			boolean mayUseMAN = true;
			Class[] params = memb.getParameters();
			Object[] filledInParams = memb.filledInParams;
			int plen = params.length;
			if (missingParamsTotal == 1 && plen == 2 && optionals != null) {

				int last = plen - 1;
				Class operand = params[last];
				try {
					Object paste = optionals.getArg(operand);
					if (operand.isInstance(paste)) {
						missingParamsTotal = 0;
						if (filledInParams == null) {
							memb.filledInParams = new Object[] { memb._object, paste };
						} else {
							filledInParams[last] = paste;
						}

						strval = "Paste|'" + Utility.getUniqueName(paste) + "' to " + strval;
						mayUseMAN = false;
					}
				} catch (NoSuchConversionException e) {
				}
			}
			strval = Utility.spaceCase(Utility.properCase(strval));
			if (missingParamsTotal > 0) {
				filledInParams = memb.filledInParams;
				if (filledInParams != null) {
					int tryForMore = getNullCount(filledInParams);
					if (optionals != null) {
						for (int i = 0; i < plen; i++) {
							Class needed = params[i];
							Object have = filledInParams[i];
							if (have == null) {
								try {
									have = optionals.getArg(needed);
									if (needed.isInstance(have)) {
										filledInParams[i] = have;
										tryForMore--;
										strval = "arg" + i + "='" + Utility.getUniqueName(have) + "' " + strval;
									}
								} catch (NoSuchConversionException e) {
									//		break;
								}
							}
						}
					}
					if (tryForMore == 0) {
						missingParamsTotal = 0;
						mayUseMAN = false;
						strval = "With Param Context|" + strval;
					}
				}
				if (mayUseMAN) {
					strval = "More Params Needed|" + strval;
				}
			}
			s = replace(s, "%m", strval);
		}
		return s;
	}

	private static String replace(String s, String f, String r) {
		return s.replace(f, r);
	}

	public int getMissingParametersAtPopup() {
		Class[] params = getParameters();
		int needsArgumentsTotal = params.length;

		if (!isDeclaredLikeStatic) {
			if (_object != null) {
				if (filledInParams == null) {
					filledInParams = new Object[params.length];
				}
				filledInParams[0] = _object;
				needsArgumentsTotal--;
			}
		}
		return needsArgumentsTotal;
	}

	static public int getNullCount(Object[] filledIn) {
		int nullz = 0;
		for (Object o : filledIn) {
			if (o == null)
				nullz++;
		}
		return nullz;
	}

	public Class[] getParameters() {
		if (params == null) {
			Member m = getMember();
			ArrayList<Class> al = new ArrayList<Class>();
			if (m != null) {
				if (!ReflectUtils.isStatic(m))
					al.add(getDeclaringClass());

				if (!(m instanceof Field)) {
					for (Class c : ReflectUtils.getParameterTypes(m)) {
						al.add(c);
					}
				}
			}
			params = al.toArray(new Class[al.size()]);
			if (filledInParams == null) {
				filledInParams = new Object[params.length];
			}
		}
		return params;
	}

	public Class getReturnType() {
		if (propDesc != null) {
			return propDesc.getField().getType();
		}
		Member m = getMember();
		if (m == null)
			return null;
		return ReflectUtils.getReturnType(m);
	}

	public Object getSafeValue() {
		if (retvalCache != null)
			return retvalCache;
		if (isSideEffectSafe) {
			try {
				return retvalCache = valueOf(null, null, false, false);
			} catch (InvocationTargetException e) {
			}
		}
		return null;
	}

	public void init(String menuFmt, DisplayContext ctx, Class cls, Object obj, Member fd, boolean isDeclNonStatic0, FeatureDescriptor feature) {
		if (menuFmt != null && menuFmt.length() < 2) {
			menuFmt = null;
		}
		this.menuFormat = menuFmt;
		arg0Clazz = cls;
		member = fd;
		displayContext = ctx;
		featureDesc = feature;
		_object = obj;
		isDeclNonStatic = isDeclNonStatic0;
		if (isDeclNonStatic && !ReflectUtils.isStatic(member)) {
			theLogger.warn("isDeclNonStatic to non static " + member);
		}
		String desc = describeMethod(fd);
		if (featureDesc instanceof PropertyDescriptorForField) {
			isSideEffectSafe = true;
			propDesc = (PropertyDescriptorForField) feature;
			desc = desc + " " + propDesc.getShortDescription();
			//menuName = propDesc.getSyntheticName(member);
			Field f = propDesc.getField();
			if (propDesc.getReadMethod() == member) {
				member = f;
				arg0Clazz = f.getDeclaringClass();
			}
			setDescription(desc);
		} else {
			setDescription(desc);
		}
		setShortLabel(getShortLabel());
	}

	@Override public AbstractButton makeMenuItem(String menuName, final Object b) {
		retvalCache = null;
		//AbstractButton jmi;
		if (menuName == null)
			menuName = getMenuName();

		if (jmi != null) {
			jmi = null;
		}
		final TriggerForMember trig = this;

		if (isSideEffectSafe && nonPrimReturnType() == Boolean.class) {
			jmi = new SafeJCheckBoxMenuItem(b, true, menuName, null, getSafeValue() == Boolean.TRUE);

		} else {

			jmi = new SafeJMenuItem(b, true, menuName);
		}

		if (trig instanceof UIAware) {
			jmi = (AbstractButton) ((UIAware) trig).visitComponent(jmi);
		}
		jmi.addActionListener(this);
		//if (this.jmi == null)
		//	this.jmi = jmi;
		return jmi;
	}

	private Class nonPrimReturnType() {
		return ReflectUtils.nonPrimitiveTypeFor(getReturnType());
	}

	public boolean noOperand() {
		Class[] params = getParameters();
		return params.length < 2;
	}

	public void setMenuInfo() {
		if (jmi == null) {
			jmi = makeMenuItem(null, _object);
		}
		Member m = getMember();
		if (m == null) {
			jmi.setBackground(Color.RED);
			return;
		}
		boolean isStatic = !isDeclNonStatic && ReflectUtils.isStatic(m);
		if (isStatic) {
			jmi.setBackground(Color.ORANGE);
		}

		if (nonPrimReturnType() == Boolean.class) {
			if (isSideEffectSafe) {
				boolean b;
				try {
					Object res = getSafeValue();
					if (res != null) {
						b = (boolean) (Boolean) res;
						jmi.setSelected(b);
					}
				} catch (Throwable e) {
				}
			}
		}

		int missingArgumentsTotal = getMissingParametersAtPopup();

		if (filledInParams != null) {
			int nulls = getNullCount(filledInParams);
			missingArgumentsTotal = nulls;
		}
		if (missingArgumentsTotal > 1) {
			jmi.setForeground(Color.WHITE);
			jmi.setBackground(Color.BLACK);
		} else if (missingArgumentsTotal > 2) {
			jmi.setForeground(Color.GRAY);
			jmi.setBackground(Color.BLACK);
		} else {
			if (missingArgumentsTotal > 0) {
				jmi.setForeground(Color.GRAY);
			}
		}
	}

	public Object valueOf(Box targetBox, ActionEvent actevt, boolean wantSideEffect, boolean isPaste) throws InvocationTargetException {
		boolean was = Debuggable.QuitelyDoNotShowExceptions;
		if (!wantSideEffect) {
			//	Debuggable.QuitelyDoNotShowExceptions = true;
		} else {
			if (eventHandled) {
				return null;
			}
		}
		try {
			return valueOfImpl(targetBox, actevt, wantSideEffect, isPaste);
		} finally {
			Debuggable.QuitelyDoNotShowExceptions = was;
			lastEvent = null;
			eventHandled = false;
		}
	}

	// ==true mean another event handler has handled the event
	boolean eventHandled = false;
	MouseEvent lastEvent = null;

	@Override public void onMouseEvent(MouseEvent event) {
		lastEvent = event;
		boolean popOutAndNotUp = event.getButton() != 3;
		// handling a right click
		Class type = nonPrimReturnType();
		if (type == Void.class)
			return;
		TriggerMenuFactory tmf = TriggerMenuFactory.getInstance(this);
		if (isSideEffectSafe) {
			Object valueAtTip;
			try {

				if (jmi == null) {
					return;
				}
				synchronized (jmi) {
					//event.consume();
					if (eventHandled)
						return;
				}
				valueAtTip = getSafeValue();
				if (valueAtTip == null) {
					valueAtTip = type;
				}
				TriggerPopupMenu tpm = tmf.buildPopupMenu(valueAtTip);
				jmi.add(Utility.getUniqueName(valueAtTip), tpm);
				tpm.show(jmi, event.getX(), event.getY());
				eventHandled = true;
			} catch (Throwable e) {
				e.printStackTrace();
			}

		}

	}

	private Object valueOfImpl(Box targetBox, ActionEvent actevt, boolean wantSideEffect, boolean isPaste) throws InvocationTargetException {
		Class rt = nonPrimReturnType();
		Member m = getMember();
		{

			try {
				Object tryValue = targetBox;
				if (_object != null) {
					tryValue = _object;
				}
				Object obj;
				if (m instanceof Field) {
					String op = "Setting";
					Field f = (Field) m;
					String fname = f.getName();
					Class t = f.getType();
					Object value = ReflectUtils.getFieldValue(tryValue, f.getDeclaringClass(), arg0Clazz, fname);
					if (wantSideEffect) {
						boolean doSet = false;
						if (rt == Boolean.class) {
							value = !((boolean) (Boolean) value);
							doSet = true;
							op = "Toggling";
						} else {
							if (isPaste) {
								value = Utility.getOptionalArg(t);
								op = "Paste";
							}
						}
						if (doSet) {
							getLogger().debug(op + " " + f + " = " + value);
							Class fclass = f.getDeclaringClass();
							ReflectUtils.setField(tryValue, fclass, arg0Clazz, fname, value);
						} else {
							getLogger().debug("Not " + op + "  " + f + " = " + value);
						}
					}
					return value;
				}
				// is a method
				if (m instanceof Method) {
					if (isSideEffectSafe || wantSideEffect) {
						obj = Utility.invokeFromUI(tryValue, (Method) m);
						return obj;
					} else {
						return retvalCache;
					}
				}
				return m;
			} catch (InvocationTargetException e) {
				throw e;
			} catch (Throwable e) {
				e.printStackTrace();
				throw new InvocationTargetException(e);
			}
		}
	}
}