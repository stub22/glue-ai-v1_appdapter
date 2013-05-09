package org.appdapter.gui.editors;

import java.beans.BeanDescriptor;
import java.util.AbstractCollection;

import org.appdapter.gui.pojo.SimplePOJOInfo;

public class AbstractCollectionBeanInfo extends SimplePOJOInfo {
  public BeanDescriptor getPOJODescriptor() {
    return new BeanDescriptor(AbstractCollection.class, CollectionCustomizer.class);
  }
}