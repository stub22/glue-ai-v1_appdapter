package org.appdapter.gui.editors;

import java.beans.BeanDescriptor;

import org.appdapter.gui.pojo.SimplePOJOInfo;

public class ThrowableBeanInfo extends SimplePOJOInfo {
  public BeanDescriptor getPOJODescriptor() {
    return new BeanDescriptor(Throwable.class, ThrowableCustomizer.class);
  }
}