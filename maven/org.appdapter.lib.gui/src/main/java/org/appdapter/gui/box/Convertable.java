package org.appdapter.gui.box;

public interface Convertable {

	<T> T convertTo(Class<T> c) throws ClassCastException;

	<T> boolean canConvert(Class<T> c);
}
