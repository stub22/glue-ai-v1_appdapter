package org.appdapter.gui.box;

import java.awt.Component;

import org.appdapter.api.trigger.Box;
import org.appdapter.gui.browse.DisplayContext;

public class DisplayContextNearBox implements DisplayContext {

	private BoxPanelSwitchableView m_switchableTabs;
	private Component m_component;
	private Box m_box;

	public DisplayContextNearBox(Box view, Component boxComponent, BoxPanelSwitchableView tabs) {
		this.m_box = view;
		this.m_component = boxComponent;
		this.m_switchableTabs = tabs;
	}

	@Override public BoxPanelSwitchableView getBoxPanelTabPane() {
		return m_switchableTabs;
	}

	@Override public Component getComponent() {
		return m_component;
	}

}
