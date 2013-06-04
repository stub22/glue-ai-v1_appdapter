package org.appdapter.gui.editors;

import java.beans.BeanDescriptor;

import org.appdapter.gui.pojo.SimplePOJOInfo;

/**
 * A BeanInfo for java.lang.Class. This one provides a Customizer
 * GUI class which the same as LargeBeanView, but adds tabs
 * for static methods and constructors as well.
*/
public class ClassBeanInfo extends SimplePOJOInfo {
  public BeanDescriptor getPOJODescriptor() {
    return new BeanDescriptor(Class.class, ClassCustomizer.class);
  }
}