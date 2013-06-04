package org.appdapter.gui.swing;

import java.util.LinkedList;

import org.appdapter.gui.box.BoxPanelSwitchableView;
import org.appdapter.gui.box.ScreenBoxPanel;
import org.appdapter.gui.browse.DisplayContext;
import org.appdapter.gui.pojo.NamedObjectCollection;

public interface POJOAppContext extends DisplayContext {

	BoxPanelSwitchableView getBoxPanelTabPane();

	NamedObjectCollection getNamedObjectCollection();

	void showError(String string, Throwable object);

	ScreenBoxPanel showScreenBox(Object value);

}
