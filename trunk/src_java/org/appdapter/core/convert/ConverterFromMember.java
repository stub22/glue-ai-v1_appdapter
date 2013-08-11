package org.appdapter.core.convert;

import static org.appdapter.core.convert.ReflectUtils.DEFAULT_CONVERTER;
import static org.appdapter.core.convert.ReflectUtils.isStatic;
import static org.appdapter.core.convert.ReflectUtils.noSuchConversion;

import java.lang.reflect.Method;
import java.util.List;

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

	public Integer isAssignableFromL(Class objNeedsToBe, Class from, List maxConverts) {
		if (from == objNeedsToBe)
			return WILL;
		if (objNeedsToBe == null || from == null)
			return MIGHT;
		if (objNeedsToBe.isAssignableFrom(from))
			return Converter.WILL;
		if (!allowTransitive || maxConverts.size() > 3) {
			return WONT;
		}
		return DEFAULT_CONVERTER.declaresConverts(null, from, objNeedsToBe, maxConverts);
	}

	@Override public Integer declaresConverts(Object val, Class valClass, Class objNeedsToBe, List maxConverts) {
		if (!objNeedsToBe.isAssignableFrom(to))
			return WONT;
		return isAssignableFromL(from, valClass, maxConverts);
	}

	@Override public <T> T convert(Object obj, Class<T> objNeedsToBe, List maxConverts) throws NoSuchConversionException {
		try {
			Object o = DEFAULT_CONVERTER.convert(obj, from, maxConverts);
			Object mid;
			if (isStatic) {
				mid = m.invoke(null, o);
			} else {
				mid = m.invoke(o);
			}
			return (T) DEFAULT_CONVERTER.convert(mid, objNeedsToBe, maxConverts);
		} catch (Throwable e) {
			return noSuchConversion(obj, objNeedsToBe, e);
		}

	}

}
