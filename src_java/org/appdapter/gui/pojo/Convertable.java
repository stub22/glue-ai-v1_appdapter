package org.appdapter.gui.pojo;

public interface Convertable {

	<T> T convertTo(Class<T> c) throws ClassCastException;

	<T> boolean canConvert(Class<T> c);
}
