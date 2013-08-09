package org.appdapter.gui.table;

import java.util.List;

import org.appdapter.gui.browse.ToFromKeyConverter;

public interface CellConversions {

	<T> T convertWas(T was, Object aValue, Class<T> objNeedsToBe);

	List listFromHolder(Object o);

	ToFromKeyConverter getCellConverter(Class clz);
}
