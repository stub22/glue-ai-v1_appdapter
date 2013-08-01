package org.appdapter.core.convert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.appdapter.core.convert.Converter.ConverterSorter;

public class AggregateConverter implements Converter {

	List<Converter> cnverters;

	public AggregateConverter(List<Converter> registeredConverters) {
		cnverters = registeredConverters;
	}

	@Override public <T> T convert(Object obj, Class<T> objNeedsToBe, int maxConverts) throws NoSuchConversionException {
		return recast(obj, objNeedsToBe, maxConverts, this);
	}

	public <T> T recast(Object obj, Class<T> objNeedsToBe, int maxConverts, Object exceptFor) throws NoSuchConversionException {
		NoSuchConversionException issue0 = null;
		Object made = obj;
		Class from = obj.getClass();
		if (objNeedsToBe.isPrimitive()) {
			objNeedsToBe = ReflectUtils.nonPrimitiveTypeFor(objNeedsToBe);
		}
		if (objNeedsToBe.isInstance(obj))
			return objNeedsToBe.cast(obj);
		List<Converter> cnverters = this.cnverters;
		synchronized (cnverters) {
			cnverters = new ArrayList<Converter>(cnverters);
		}
		Collections.sort(cnverters, new ConverterSorter(obj, from, objNeedsToBe));
		for (Converter converter : cnverters) {
			if (exceptFor == converter) {
				continue;
			}
			try {
				made = converter.convert(obj, objNeedsToBe, maxConverts);
			} catch (NoSuchConversionException e) {
				issue0 = e;
			} catch (Throwable e) {
				issue0 = new NoSuchConversionException(obj, objNeedsToBe, e);
			}
			if (objNeedsToBe.isInstance(made))
				return (T) made;
		}
		// set this to try and set a breakpoint to see why conversion failed
		if (false & made != obj && made != null) {
			obj = made;
			for (Converter converter : cnverters) {
				try {
					made = converter.convert(obj, objNeedsToBe, maxConverts);
				} catch (NoSuchConversionException e) {
					issue0 = e;
				} catch (Throwable e) {
					issue0 = new NoSuchConversionException(obj, objNeedsToBe, e);
				}
				if (objNeedsToBe.isInstance(made))
					return (T) made;
			}
		}
		if (issue0 == null)
			issue0 = new NoSuchConversionException(obj, objNeedsToBe);

		throw issue0;
	}

	@Override public Integer declaresConverts(Object val, Class from, Class objNeedsToBe, int maxCvt) {
		return declaresConverts(val, from, objNeedsToBe, maxCvt, this);
	}

	public Integer declaresConverts(Object val, Class from, Class objNeedsToBe, int maxCvt, Object exceptFor) {
		List<Converter> cnverters = this.cnverters;
		synchronized (cnverters) {
			cnverters = new ArrayList<Converter>(cnverters);
		}
		boolean maybe = false;
		for (Converter converter : cnverters) {
			if (exceptFor == converter) {
				continue;
			}
			if (converter.declaresConverts(val, from, objNeedsToBe, maxCvt) == WILL)
				return WILL;
			if (converter.declaresConverts(val, from, objNeedsToBe, maxCvt) == MIGHT)
				maybe = true;
		}
		if (maybe)
			return MIGHT;
		return WONT;
	}
}