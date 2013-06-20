package org.appdapter.gui.api;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;

import org.appdapter.api.trigger.BoxPanelSwitchableView;
import org.appdapter.api.trigger.DisplayType;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.swing.SingleTabFrame;

public class ObjectTabsForTabbedView implements BoxPanelSwitchableView {

	final JTabbedPane tabs;

	public ObjectTabsForTabbedView(JTabbedPane tbs) {
		if (tbs.toString() == null) {

		}
		tabs = tbs;
	}

	@Override public boolean clearChildren() {
		tabs.removeAll();
		return true;
	}

	@Override public boolean removeTab(String title, Component view) {

		int index = indexOf(title, view);

		int catb = -1;
		if (view != null) {
			catb = tabs.indexOfTabComponent(view);
		}
		int removedTimes = 0;
		int tat = -1;
		if (title != null) {
			tat = tabs.indexOfTab(title);
			if (tat != -1) {
				removedTimes++;
				tabs.remove(tat);
			}
		}
		int cat = -1;
		if (view != null) {
			cat = tabs.indexOfTabComponent(view);
			if (cat != -1) {
				tabs.remove(cat);
				removedTimes++;
			}
		}

		return removedTimes > 0;
	}

	public int indexOf(String title, Component view) {
		int index = unverifiedIndex(title, view);
		if (index == -1)
			return -1;
		Component comp = tabs.getTabComponentAt(index);

		boolean wrongComponet = false;
		if (view != null && comp != null && comp != view) {
			wrongComponet = true;
			// already have a tab like that
		}
		String title2 = tabs.getTitleAt(index);
		if (title != null && (title2 == null || !title2.equals(title))) {
			if (wrongComponet) {
				return -1;
			} // wrong title!

		}
		if (wrongComponet) {
			index = tabs.indexOfTabComponent(view);
			if (index != -1) {
				Debuggable.warn("weird tabs for ", tabs, title, view);
			}
			return -1;
		}
		return index;
	}

	public int unverifiedIndex(String title, Component view) {

		int catb = -1;
		if (view != null) {
			catb = tabs.indexOfTabComponent(view);
		}
		int tat = -1;
		if (title != null) {
			tat = tabs.indexOfTab(title);
			if (tat != -1) {
				return tat;
			}
		}
		return catb;
	}

	@Override public boolean bringToFront(String title, Component view) {
		int index = indexOf(title, view);
		if (index != -1) {
			tabs.setSelectedIndex(index);
			return true;
		}
		return false;
	}

	@Override public boolean sendToBack(String title, Component view) {
		int index = indexOf(title, view);
		if (index == -1) {
			return false;
		}
		int sel = tabs.getSelectedIndex();
		if (sel != index) {
			// already to back;
			return true;
		}
		if (index != -1 && childCount() > 1) {
			if (index == 0) {
				tabs.setSelectedIndex(childCount() - 1);
				return true;
			} else {
				tabs.setSelectedIndex(index - 1);
				return true;
			}
		}
		return false;
	}

	@Override public Container getContainer() {
		return tabs;
	}

	@Override public int childCount() {
		return tabs.getComponentCount();
	}

	@Override public boolean addTab(String title, Component view) {
		int index = indexOf(title, view);
		if (index == -1) {
			tabs.addTab(title, view);
			return true;
		}
		Component comp = tabs.getTabComponentAt(index);
		boolean wrongComponet = false;
		if (view != null && comp != null && comp != view) {
			wrongComponet = true;
			// already have a tab like that
		}
		String title2 = tabs.getTitleAt(index);
		if (title != null && (title2 == null || !title2.equals(title))) {
			if (!wrongComponet) {
				tabs.setTitleAt(index, title);
				return true;
			}

		}
		return false;

	}

	@Override public void insertTab(String title, Icon icon, Component component, String tip, int index) {
		tabs.insertTab(title, icon, component, tip, index);

	}

	@Override public void addChangeListener(ChangeListener cc) {
		tabs.addChangeListener(cc);

	}

	@Override public int indexOfComponent(Component view) {
		return tabs.indexOfComponent(view);
	}

	@Override public int getSelectedIndex() {
		return tabs.getSelectedIndex();
	}

	@Override public Dimension getPreferredChildSize() {
		return tabs.getPreferredSize();
	}

	@Override public void addComponent(String title, Component view, DisplayType panel) {
		addTab(title, view);
	}

	@Override public boolean containsComponent(Component view) {
		return tabs.indexOfComponent(view) != -1;
	}

	@Override public void setSelectedComponent(Component view) {
		tabs.setSelectedComponent(view);

	}

	@Override public Dimension getSize(DisplayType frame) {
		return tabs.getPreferredSize();
	}
}
