package org.appdapter.gui.pojo;

import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.*;

import org.appdapter.gui.box.*;
import org.appdapter.gui.pojo.*;
import org.appdapter.gui.swing.POJOAppContext;
import org.appdapter.gui.browse.*;
import org.appdapter.api.trigger.*;

public interface POJOCollection extends DisplayContext {

	public static String MISSING_COMPONENT = "<null>";

	POJOBox addComponent(String title, Component view, DisplayType attachType);

	ScreenBoxImpl addObject(String title, Object child, DisplayType attachType, boolean showAsap);

	/**
	 * Listeners will be notifed when the currently object selection is changed.
	 */
	void addPropertyChangeListener(PropertyChangeListener p);

	/**
	 * Checks if this collection contains the given object box
	 */
	boolean containsComponent(Component view);

	boolean containsObject(Object child, DisplayType attachType);

	Component findComponentByObject(Object child, DisplayType attachType);

	Set<POJOBox> findComponentsByPredicate(Comparator<POJOBox> cursor, DisplayType attachTyp);

	DisplayContext findDisplayContext(Box child);

	Object findObjectByComponent(Component view);

	/**
	 * Listeners will find out when objects are added or removed
	 */
	void addListener(POJOCollectionListener l);

	/**
	 * Returns the box corresponding to the given object, i.e the
	 * Box who's object corresponds to the given one. Returns null if
	 * the ObjectNavigator does not contain the given object.
	 */
	POJOBox addObject(Object object);

	POJOBox addObject(String title, Object obj) throws PropertyVetoException;

	/**
	 * Checks if this collection contains the given object
	 */
	boolean containsObject(Object object);

	/**
	 * Creates a new object of the given class and adds to this collection. The
	 * given class must have an empty constructor.
	 * 
	 * @throws InstantiationException
	 *             if the given Class represents an class, an
	 *             interface, an array class, a primitive type, or void; or if
	 *             the instantiation fails for some other reason
	 * @throws IllegalAccessException
	 *             if the given class or initializer is not accessible.
	 * 
	 * @returns the newly created Box
	 */
	Object createAndAddBox(Class cl) throws InstantiationException, IllegalAccessException;

	/**
	 * Returns the box with the given name, or null if none.
	 */
	@SuppressWarnings("rawtypes") POJOBox findBoxByName(String name);

	/**
	 * Returns all objects representing objects that are an instance of the
	 * given class or interface, either directly or indirectly.
	 */
	<T> Collection<POJOBox<? extends T>> findBoxByType(Class<T> type);

	/**
	 * Returns an iterator over all the object boxs. NOTE - this could be a
	 * bit slow! Avoid whenever possible. The code can be optimized for this,
	 * but it isn't right now.
	 */
	Iterable<Box> getBoxes();

	/**
	 * Returns the current number of objects in the collection
	 */
	int getBoxesCount();

	NamedObjectCollection getNamedObjectCollection();

	/**
	 * Returns an iterator over all the object screen boxes. NOTE - this could be a
	 * bit slow! Avoid whenever possible. The code can be optimized for this,
	 * but it isn't right now.
	 */
	Iterable<ScreenBoxImpl> getScreenBoxes();

	/**
	 * Listeners will be notifed when the currently object selection is changed.
	 */
	//void addPropertyChangeListener(PropertyChangeListener p);

	Object getSelectedComponent();

	POJOBox getSelectedObject(DisplayType attachType);

	/**
	 * Returns the currently selected object, or null if none.
	 */
	Object getSelectedPOJO();

	Dimension getSize(DisplayType attachType);

	Iterable<DisplayType> getSupportedAttachTypes();

	String getTitleOf(Object view);

	String getTitleOf(Object child, DisplayType attachType);

	/**
	 * Returns UI type actions that can be carried out on the given object:
	 *    
	 *    Such as hide, maximize, cut, paste to another tree
	 * 
	 */
	Collection getTriggersFromUI(Object object);

	/**
	 * This is used for ScreenBoxImpls to tell their POJOCollection that a
	 * property such as "name" or "selected" has changed. The POJOCollection
	 * will update its state as necessary.
	 */
	void propertyChange(PropertyChangeEvent evt);

	/**
	 * Listeners will be notifed when the currently object selection is changed.
	 */
	//void removePropertyChangeListener(PropertyChangeListener p);

	void registerPair(POJOBox displayContextNearBox, boolean insertChild);

	void removeComponent(Component view);

	/**
	 * Listeners will find out when objects are added or removed
	 */
	void removeListener(POJOCollectionListener l);

	/**
	 * Removes the given object, if it is inside this collection. If not,
	 * nothing happens.
	 * <p>
	 * 
	 * POJOListeners will be notified.
	 * <p>
	 * 
	 * If the object was selected, the current selection will change to null and
	 * property change listeners will be notified.
	 * <p>
	 * 
	 */
	boolean removeObject(Object obj);

	void removeObject(Object child, DisplayType attachType);

	/**
	 * Listeners will be notifed when the currently object selection is changed.
	 */
	void removePropertyChangeListener(PropertyChangeListener p);

	void save(File destination) throws IOException;

	void setSelectedComponent(Component view);

	/**
	 * Makes the given object the currently selected one. The previously
	 * selected object (if any) will be deselected, and a property change event
	 * will be fired.
	 * 
	 * @throws PropertyVetoException
	 *             if someone refused to let the selected object change
	 */
	void setSelectedPOJO(Object object) throws PropertyVetoException;

	/**
	 * Displays the given message somehow
	 * @return 
	 */
	ScreenBoxPanel showMessage(String string);

	/**
	 * Displays the given error message somehow
	 */
	void showError(String msg, Throwable err);

	/**
	 * Opens up a GUI to show the details of the given object
	 * @return 
	 */
	ScreenBoxPanel showScreenBox(Box<?> child, DisplayType attachType);

	ScreenBoxPanel showScreenBox(Object object);

	ScreenBoxPanel showScreenBox(Object object, boolean asap);

	ScreenBoxPanel showScreenBox(String title, Object child, DisplayType attachType);

	/**
	 * This is used for ScreenBoxImpls to tell their POJOCollection that a
	 * property such as "name" or "selected" is about to change, allowing the
	 * POJOCollection to fire a PropertyVetoException to stop the change if it
	 * likes.
	 * <p>
	 * 
	 * This would happen, for example, if someone is trying to rename a object
	 * to a name that another object within this collection already has.
	 */
	void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException;

	void reload();

	BoxPanelSwitchableView getBoxPanelTabPane();

	POJOAppContext getPOJOAppContext();

}