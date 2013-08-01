package org.appdapter.core.convert;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Comparator;

public interface Converter extends TypeAssignable {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface ConverterMethod {
		/**
		 *  true if the remaining arguments use selected instances
		 *  
		 *  example would be something like 
		 *  
		 *  Thread.currentThread() to be put into an missing argument requiring a Thread
		 * @return 
		 */
		public boolean HasOptionalArgs() default false;

		public boolean AllowTranstiveConversions() default true;
	}

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
			return o1.declaresConverts(value, from, to, MCVT).compareTo(o2.declaresConverts(value, from, to, MCVT));
		}

	}

	public <T> T convert(Object obj, Class<T> objNeedsToBe, int maxConverts) throws NoSuchConversionException;

}