package org.appdapter.gui.editors;

import java.beans.BeanDescriptor;

import org.appdapter.gui.objbrowser.model.SimplePOJOInfo;

public class ThrowablePOJOInfo extends SimplePOJOInfo {
  public BeanDescriptor getPOJODescriptor() {
    return new BeanDescriptor(Throwable.class, ThrowableCustomizer.class);
  }
}