package org.appdapter.gui.swing;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractListModel;


/**
 * A GUI component that shows all the static methods for a given class,
 * and provides ways of executing these methods.
 *
 * 
 */
 public class StaticMethodList extends JJList {
  private Model model;
  private Class cls;

  public StaticMethodList(Class cls) throws Exception {
    super();
    this.cls = cls;
    this.model = new Model();
    setModel(model);
  }

  public Method getSelectedMethod() {
    return model.getMethodAt(getSelectedIndex());
  }

  class Model extends AbstractListModel {
    List methods;

    public Model() throws Exception {
      Method[] methodsArray = cls.getMethods();

      methods = new LinkedList();

      for (int i = 0; i < methodsArray.length; ++i) {
        Method method = methodsArray[i];
        if (Modifier.isStatic(method.getModifiers())) {
          methods.add(method);
        }
      }

      Collections.sort(methods, new MethodComparator());
    }

    public Method getMethodAt(int index) {
      try {
        return (Method) methods.get(index);
      } catch (Exception err) {
        return null;
      }
    }

    @Override
	public Object getElementAt(int index) {
      try {
        return getMethodAt(index).getName();
      } catch (Exception err) {
        return null;
      }
    }

    @Override
	public int getSize() {
      return methods.size();
    }
  }

  class MethodComparator implements Comparator {
    @Override
	public int compare(Object first, Object second) {
      Method a = (Method) first;
      Method b = (Method) second;
      String nameA = a.getName();
      String nameB = b.getName();
      return nameA.compareTo(nameB);
    }

    @Override
	public boolean equals(Object o) {
      return (o instanceof MethodComparator);
    }
  }
}
