package org.appdapter.gui.browse;

import org.appdapter.core.convert.Converter;
import org.appdapter.core.convert.NoSuchConversionException;

public class ProxyConvertor implements Converter {

	@Override public Integer declaresConverts(Object val, Class valClass, Class objNeedsToBe) {
		PropertyDescriptorForField f = PropertyDescriptorForField.proxyToField.get(objNeedsToBe);
		if (f == null)
			return WONT;
		return f.declaresConverts(val, valClass, objNeedsToBe);
	}

	@Override public <T> T recast(Object obj, Class<T> objNeedsToBe) throws NoSuchConversionException {
		PropertyDescriptorForField f = PropertyDescriptorForField.proxyToField.get(objNeedsToBe);
		if (f == null)
			throw new NoSuchConversionException(obj, objNeedsToBe);
		return f.recast(obj, objNeedsToBe);
	}

}
