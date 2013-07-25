package org.appdapter.core.convert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public interface Converter extends TypeAssignable {
	public class ConverterSorter implements Comparator<Converter> {

		Object value;
		Class from;
		Class to;

		public ConverterSorter(Object val, Class fromC, Class toC) {
			value = val;
			from = fromC;
			to = toC;
		}

		@Override public int compare(Converter o1, Converter o2) {
			return o1.declaresConverts(value, from, to).compareTo(o2.declaresConverts(value, from, to));
		}

	}

	public class AggregateConverter implements Converter {

		List<Converter> cnverters;

		public AggregateConverter(List<Converter> registeredConverters) {
			cnverters = registeredConverters;
		}

		@Override public <T> T recast(Object obj, Class<T> objNeedsToBe) throws NoSuchConversionException {
			NoSuchConversionException issue0 = null;
			Object made = obj;
			Class from = obj.getClass();
			List<Converter> cnverters = this.cnverters;
			synchronized (cnverters) {
				cnverters = new ArrayList<Converter>(cnverters);
			}
			Collections.sort(cnverters, new ConverterSorter(obj, from, objNeedsToBe));
			for (Converter converter : cnverters) {
				try {
					made = converter.recast(obj, objNeedsToBe);
				} catch (NoSuchConversionException e) {
					issue0 = e;
				} catch (Throwable e) {
					issue0 = new NoSuchConversionException(obj, objNeedsToBe, e);
				}
				if (objNeedsToBe.isInstance(made))
					return (T) made;
			}
			if (made != obj && made != null) {
				obj = made;
				for (Converter converter : cnverters) {
					try {
						made = converter.recast(obj, objNeedsToBe);
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

		@Override public Integer declaresConverts(Object val, Class from, Class objNeedsToBe) {
			List<Converter> cnverters = this.cnverters;
			synchronized (cnverters) {
				cnverters = new ArrayList<Converter>(cnverters);
			}
			boolean maybe = false;
			for (Converter converter : cnverters) {
				if (converter.declaresConverts(val, from, objNeedsToBe) == WILL)
					return WILL;
				if (converter.declaresConverts(val, from, objNeedsToBe) == MIGHT)
					maybe = true;
			}
			if (maybe)
				return MIGHT;
			return WONT;
		}
	}

	public <T> T recast(Object obj, Class<T> objNeedsToBe) throws NoSuchConversionException;
}