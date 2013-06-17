package org.appdapter.api.trigger;

import java.awt.Component;
import java.awt.Dimension;

import org.appdapter.gui.box.ScreenBoxImpl;

public interface BoxPanelSwitchableView extends UIProvider, DisplayContextProvider, ITabUI {

	/**
	 * Returns an iterator over all the object screen boxes. NOTE - this could be a
	 * bit slow! Avoid whenever possible. The code can be optimized for this,
	 * but it isn't right now.
	 */
	Iterable<ScreenBoxImpl> getScreenBoxes();

	//Object getSelectedComponent();

	/**
	 * Checks if this collection contains the given object box
	 */
	boolean containsComponent(Component view);

	// DisplayType findComponent(Component view);

	//	Component findComponentByObject(Object child, DisplayType attachType);

	//	Set<POJOBox> findComponentsByPredicate(Comparator<POJOBox> cursor, DisplayType attachTyp);

	DisplayContext findDisplayContext(Box child);

	//	Object findObjectByComponent(Component view);

	BT addComponent(String title, Component view, DisplayType attachType);

	BT getSelectedObject(DisplayType attachType);

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
	//Collection getTriggersFromUI(Object object);

	/**
	 * Listeners will be notifed when the currently object selection is changed.
	 */
	//void removePropertyChangeListener(PropertyChangeListener p);

	//void registerPair(POJOBox displayContextNearBox, boolean insertChild);

	void removeComponent(Component view);

	//void removeObject(Object child, DisplayType attachType);

	void setSelectedComponent(Component view);

	/**
	 * Makes the given object the currently selected one. The previously
	 * selected object (if any) will be deselected, and a property change event
	 * will be fired.
	 * 
	 * @throws PropertyVetoException
	 *             if someone refused to let the selected object change
	 */
	//void setSelectedObject(Object object) throws PropertyVetoException;

	/**
	 * Displays the given message somehow
	 * @return 
	 */
	UserResult showMessage(String string);

	/**
	 * Displays the given error message somehow
	 */
	UserResult showError(String msg, Throwable err);

	UserResult attachChild(String title, Object anyObject, DisplayType attachType, boolean showAsap);

}
