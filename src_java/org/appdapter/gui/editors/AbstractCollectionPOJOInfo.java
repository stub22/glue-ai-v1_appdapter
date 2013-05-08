package org.appdapter.gui.editors;

import java.beans.BeanDescriptor;
import java.util.AbstractCollection;

import org.appdapter.gui.objbrowser.model.SimplePOJOInfo;

public class AbstractCollectionPOJOInfo extends SimplePOJOInfo {
  public BeanDescriptor getPOJODescriptor() {
    return new BeanDescriptor(AbstractCollection.class, CollectionCustomizer.class);
  }
}