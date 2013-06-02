package org.appdapter.demo;

import org.appdapter.gui.box.BoxPanelSwitchableView;
import org.appdapter.gui.box.UIProvider;
import org.appdapter.gui.browse.DisplayContext;
import org.appdapter.gui.pojo.DisplayType;
import org.appdapter.gui.pojo.NamedObjectCollection;

public interface ObjectNavigatorGUI extends UIProvider {

	NamedObjectCollection getNamedObjectCollection();

	void showMessage(String string);

	DisplayContext findOrCreateDisplayContext(String title, Object anyObject, DisplayType dispType);

	void showScreenBox(Object any);

	BoxPanelSwitchableView getBoxPanelTabPane();

}