package org.appdapter.core.convert;

import static org.appdapter.core.convert.ReflectUtils.DEFAULT_CONVERTER;
import static org.appdapter.core.convert.ReflectUtils.isStatic;
import static org.appdapter.core.convert.ReflectUtils.leastOfCvt;
import static org.appdapter.core.convert.ReflectUtils.noSuchConversion;

import java.lang.reflect.Method;

import org.appdapter.core.log.Debuggable;

public class ConverterFromMember extends Debuggable implements Converter {

	final Method m;
	final Class to, from;
	final boolean isStatic;
	boolean allowTransitive;
	boolean allowOptionalArgs;
	ConverterMethod info;

	@Override public boolean equals(Object obj) {
		return super.equals(obj) || ((obj instanceof ConverterFromMember) && (((ConverterFromMember) obj).m == m));
	}

	@Override public int hashCode() {
		return m.hashCode();
	}

	public ConverterFromMember(Method m, boolean transitive, ConverterMethod cmi) {
		this.m = m;
		info = cmi;
		allowTransitive = transitive;
		isStatic = isStatic(m);
		to = m.getReturnType();
		if (isStatic) {
			from = m.getParameterTypes()[0];
			allowOptionalArgs = m.getParameterTypes().length > 1;
		} else {
			from = m.getDeclaringClass();
			allowOptionalArgs = m.getParameterTypes().length > 0;
		}
		if (cmi != null) {
			allowTransitive = cmi.AllowTranstiveConversions();
			allowOptionalArgs = cmi.HasOptionalArgs();
		}
	}

	public int isAssignableFromL(Class objNeedsToBe, Class from, int maxCvt) {
		if (from == objNeedsToBe)
			return WILL;
		if (objNeedsToBe == null || from == null)
			return MIGHT;
		if (objNeedsToBe.isAssignableFrom(from))
			return Converter.WILL;
		if (!allowTransitive || maxCvt < 1) {
			return WONT;
		}
		maxCvt--;
		return DEFAULT_CONVERTER.declaresConverts(null, from, objNeedsToBe, maxCvt, this);
	}

	@Override public Integer declaresConverts(Object val, Class valClass, Class objNeedsToBe, int maxConverts) {
		if (!objNeedsToBe.isAssignableFrom(to))
			return WONT;
		return isAssignableFromL(from, valClass, maxConverts);
	}

	@Override public <T> T convert(Object obj, Class<T> objNeedsToBe, int maxConverts) throws NoSuchConversionException {
		try {
			Object o = DEFAULT_CONVERTER.convert(obj, from, maxConverts);
			if (isStatic) {
				return (T) DEFAULT_CONVERTER.convert(m.invoke(null, o), objNeedsToBe, maxConverts);
			} else {

				return (T) DEFAULT_CONVERTER.convert(m.invoke(o), objNeedsToBe, maxConverts);
			}
		} catch (Throwable e) {
			return noSuchConversion(obj, objNeedsToBe, e);
		}

	}

}
