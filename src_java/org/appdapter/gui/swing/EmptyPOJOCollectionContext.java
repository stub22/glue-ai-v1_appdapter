

package org.appdapter.gui.swing;
import java.util.Collection;
import java.util.Vector;

import org.appdapter.gui.objbrowser.model.POJOCollectionListener;
import org.appdapter.gui.objbrowser.model.POJOCollectionWithBoxContext;

public class EmptyPOJOCollectionContext implements POJOCollectionWithBoxContext{
  /**
   * Adds a new object, if it wasn't already there
   *
   * @returns true if the object was added, false if the object was already there
   */
  public boolean addPOJO(Object object) {
    return false;
  }

  /**
   * Removes a object, if it is there
   *
   * @returns true if the object was removed, false if that object wasn't in this context
   */
  public boolean removePOJO(Object object) {
    return false;
  }

  public Collection getPOJOCollectionOfType(Class type) {
    return new Vector();
  }

  public boolean containsPOJO(Object object) {
    return false;
  }

  public void showScreenBox(Object object) {
  }

  public void addListener(POJOCollectionListener o) {
  }

  public void removeListener(POJOCollectionListener o) {
  }

  public Collection getActions(Object object) {
    return new Vector();
  }

  public Object findPOJO(String name) {
    return null;
  }

  public String getPOJOName(Object object) {
    return "" + object;
  }

  public void showError(String msg, Throwable err) {
    new ErrorDialog(msg, err).show();
  }
}