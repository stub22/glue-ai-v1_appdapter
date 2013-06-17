package org.appdapter.gui.browse;

import java.awt.Component;

import org.appdapter.api.trigger.AppGUIWithTabsAndTrees;
import org.appdapter.api.trigger.DisplayType;
import org.appdapter.api.trigger.NamedObjectCollection;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.box.BoxPanelSwitchableViewImpl;
import org.appdapter.gui.box.ScreenBoxImpl;

@SuppressWarnings({ "serial", "unchecked" })
public class BrowsePanelSwitchableViewFromUI extends BoxPanelSwitchableViewImpl {

	final AppGUIWithTabsAndTrees gui;

	public BrowsePanelSwitchableViewFromUI(NamedObjectCollection namedObjectsHeld, AppGUIWithTabsAndTrees g) {
		super(namedObjectsHeld);
		this.gui = g;
	}

	public Component getComponent() {
		return (Component) gui;
	}

	public AppGUIWithTabsAndTrees getAppGUI(DisplayType attachType) {
		return gui;
	}

	@Override public ScreenBoxImpl attachChild(String title, Object anyObject, DisplayType attachType, boolean showAsap) {
		Debuggable.notImplemented();
		return null;
	}

}