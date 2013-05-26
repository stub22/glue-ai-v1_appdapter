package org.appdapter.demo;

import javax.swing.JTabbedPane;

import org.appdapter.api.trigger.BoxPanelSwitchableView;
import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.gui.pojo.NamedObjectCollection;

public interface ObjectNavigatorGUI extends DisplayContext {

	NamedObjectCollection getCollectionWithSwizzler();

	void showMessage(String string);

	BoxPanelSwitchableView getBoxPanelTabPane();

	JTabbedPane getRealPanelTabPane();

}