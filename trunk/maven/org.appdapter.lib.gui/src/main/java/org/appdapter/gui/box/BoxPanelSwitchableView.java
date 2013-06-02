package org.appdapter.gui.box;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JInternalFrame;

import org.appdapter.api.trigger.Box;
import org.appdapter.gui.browse.DisplayContext;
import org.appdapter.gui.pojo.DisplayType;

public interface BoxPanelSwitchableView extends UIProvider {

	public static String MISSING_COMPONENT = "<null>";

	Dimension getSize(DisplayType attachType);

	boolean containsComponent(Component view);

	void removeComponent(Component view);

	String getTitleOf(Component view);

	void setSelectedComponent(Component view);

	DisplayContext findDisplayContext(Box child);

	Component findComponentByObject(Object child, DisplayType attachType);

	Object findObjectByComponent(Component view);

	boolean containsObject(Object child, DisplayType attachType);

	void removeObject(Object child, DisplayType attachType);

	String getTitleOf(Object child, DisplayType attachType);

	DisplayPair getSelectedObject(DisplayType attachType);

	DisplayContext findDisplayContext(Object child);

	DisplayContext showObject(Box<?> child, DisplayType attachType);

	DisplayContext showObject(Object child, DisplayType attachType);

	DisplayPair addObject(String title, Object child, DisplayType attachType, boolean showAsap);

	DisplayPair addComponent(String title, Component view, DisplayType attachType);

}
