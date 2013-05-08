package org.appdapter.gui.editors;

import java.beans.BeanDescriptor;
import java.util.Collection;

import org.appdapter.gui.objbrowser.model.SimplePOJOInfo;

public class CollectionPOJOInfo extends SimplePOJOInfo {
	public BeanDescriptor getPOJODescriptor() {
		return new BeanDescriptor(Collection.class, CollectionCustomizer.class);
	}
}