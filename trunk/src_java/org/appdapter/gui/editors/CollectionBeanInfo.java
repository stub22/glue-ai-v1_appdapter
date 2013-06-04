package org.appdapter.gui.editors;

import java.beans.BeanDescriptor;
import java.util.Collection;

import org.appdapter.gui.pojo.SimplePOJOInfo;

public class CollectionBeanInfo extends SimplePOJOInfo {
	public BeanDescriptor getPOJODescriptor() {
		return new BeanDescriptor(Collection.class, CollectionCustomizer.class);
	}
}