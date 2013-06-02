package org.appdapter.gui.box;

import java.awt.Component;
import java.awt.Container;

import org.appdapter.api.trigger.Box;
import org.appdapter.gui.browse.DisplayContext;
import org.appdapter.gui.pojo.DisplayType;

public class DisplayPair implements DisplayContext {

	public DisplayPair(String title, Object boxOrObj, Component vis, DisplayType displayType, Container parent, BoxPanelSwitchableView bpsv) {
		m_title = title;
		m_view = vis;
		m_parent_component = parent;
		if (boxOrObj instanceof Box) {
			m_box = (Box) boxOrObj;
		} else {
			m_obj = boxOrObj;
		}
		m_toplevel = bpsv;
		m_displayType = displayType;
		((BoxPanelSwitchableViewImpl) m_toplevel).registerPair(this, false);
	}

	String m_title;
	Box m_box;
	Object m_obj;
	DisplayType m_displayType;
	Component m_view;
	Container m_parent_component;
	BoxPanelSwitchableView m_toplevel;
	public boolean isAdded;

	public String getTitle() {
		return m_title;
	}

	@Override public BoxPanelSwitchableView getBoxPanelTabPane() {
		return m_toplevel;
	}

	@Override public Component getComponent() {
		return m_view;
	}

	public Object getObject() {
		if (m_obj != null)
			return m_obj;
		return m_box;
	}

}
