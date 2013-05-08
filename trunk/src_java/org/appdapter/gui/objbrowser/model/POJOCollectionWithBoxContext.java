

package org.appdapter.gui.objbrowser.model;
import java.util.Collection;
import java.util.Set;

/**
 * Represents any kind of object-container environment with a Session
 *
 * 
 */
public interface POJOCollectionWithBoxContext {
  /**
   * Returns all objects of the given type (including subclasses)
   */
  public Collection getPOJOCollectionOfType(Class type);

  public Object findPOJO(String name);

  public String getPOJOName(Object object);

  /**
   * Checks if the context contains the given object
   */
  public boolean containsPOJO(Object object);

  /**
   * Opens up a GUI to show the details of the given object
   */
  public void showScreenBox(Object object) throws Exception;

  /**
   * Adds a POJOCollectionContextListener to this context. The listener will
   * find out when objects are added or removed.
   */
  public void addListener(POJOCollectionListener o);

  /**
   * Adds a POJOCollectionContextListener to this context. The listener will
   * find out when objects are added or removed.
   */
  public void removeListener(POJOCollectionListener o);

  /**
   * Returns all actions that can be carried out on the given object
   */
  public Collection getActions(Object object);

  /**
   * Adds a new object, if it wasn't already there
   *
   * @returns true if the object was added, false if the object was already there
   */
  public boolean addPOJO(Object object);

  /**
   * Removes a object, if it is there
   *
   * @returns true if the object was removed, false if that object wasn't in this context
   */
  public boolean removePOJO(Object object);

  /**
   * Displays the given error message somehow
   */
  public void showError(String msg, Throwable err);
}
