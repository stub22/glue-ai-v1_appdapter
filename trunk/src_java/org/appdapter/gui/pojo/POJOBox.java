package org.appdapter.gui.pojo;

import java.awt.Component;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.List;

import org.appdapter.api.trigger.AnyOper.UIHidden;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.gui.box.ScreenBoxPanel;
import org.appdapter.gui.browse.DisplayContext;

/**
 * A wrapper for objects used in the ScreenBox system. It holds an object,
 * a name, and info about whether it is selected or not. The "name" and
 * "selected" properties are bound and constrained, i.e. you can listen to
 * changes using addPropertyChangeListener, and you can also prevent changes in
 * some cases if you use addVetoableChangeListener.
 * 
 * 
 */
@UIHidden
abstract public interface POJOBox<TrigType extends Trigger<? extends POJOBox<TrigType>>> extends

Box<TrigType>, java.io.Serializable, GetSetObject, Convertable, MutableBox<TrigType> {

	public abstract Object getDisplayTarget(DisplayType attachType);

	public abstract org.appdapter.gui.pojo.DisplayType getDisplayType();

	@Override public <T> T[] getObjects(Class<T> type);

	@Override public <T> boolean canConvert(Class<T> c);

	@Override public <T> T convertTo(Class<T> c);

	public String getDebugName();

	/**
	 * This is used for ScreenComponentImpls to tell their BoxPanelSwitchableView that a
	 * property such as "name" or "selected" is about to change, allowing the
	 * BoxPanelSwitchableView to fire a PropertyVetoException to stop the change if it
	 * likes.
	 * <p>
	 * 
	 * This would happen, for example, if someone is trying to rename a object
	 * to a name that another object within this collection already has.
	 */
	public abstract void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException;

	/**
	 * This is used for ScreenComponentImpls to tell their BoxPanelSwitchableView that a
	 * property such as "name" or "selected" has changed. The BoxPanelSwitchableView
	 * will update its state as necessary.
	 */
	public abstract void propertyChange(PropertyChangeEvent evt);

	/** 
	 * This returns the decomposed Mixins
	 * @return
	 */
	public Object[] getObjects();

	@Override public List<TrigType> getTriggers();

	// ==== Event listener registration =============

	/**
	 * PropertyChangeListeners will find out when the name or selection state
	 * changes.
	 */
	public void addPropertyChangeListener(PropertyChangeListener p);

	/**
	 * PropertyChangeListeners will find out when the name or selection state
	 * changes.
	 */
	public void removePropertyChangeListener(PropertyChangeListener p);

	/**
	 * VetoableChangeListeners will find out when the name or selection state is
	 * about to change, and can prevent such changes if desired.
	 */
	public void addVetoableChangeListener(VetoableChangeListener v);

	/**
	 * VetoableChangeListeners will find out when the name or selection state is
	 * about to change, and can prevent such changes if desired.
	 */
	public void removeVetoableChangeListener(VetoableChangeListener v);

	// ===== Property getters and setters ========================

	/**
	 * Returns the object that this object wrapper represents
	 */
	abstract public Object getValue();

	/**
	 * Returns the Class[]s that this object wrapper represents
	 */
	abstract public List<Class> getTypes();

	/**
	 * Returns the name of this object
	 */
	public String getUniqueName();

	/**
	 * Changes the name of this object. The name should never be null.
	 * 
	 * @throws PropertyVetoException
	 *             if someone refused to allow the name to change
	 */
	public void setUniqueName(String newName) throws PropertyVetoException;

	/**
	 * Gets a BeanInfo object for this object, using the Introspector class
	 */
	public BeanInfo getBeanInfo();

	public Class<? extends Object> getPOJOClass();

	public boolean isTypeOf(Class type);

	public boolean representsObject(Object test);

	public boolean isNamed(String test);

	public ScreenBoxPanel getPropertiesPanel();

	public Component getComponent(DisplayType attachType);

	public abstract Object getObject();

}