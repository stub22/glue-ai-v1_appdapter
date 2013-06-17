package org.appdapter.api.trigger;

import java.awt.Component;
import java.awt.Container;

import javax.swing.Icon;
import javax.swing.event.ChangeListener;


public interface AddTabFrames {

	public interface ObjectTabs {

		public boolean clearTabs();

		public Container getContainer();

		public int countOfTabs();

		public boolean addTab(String title, Component view);

		public boolean removeTab(String title, Component view);

		public int indexOf(String title, Component view);

		public boolean bringToFront(String title, Component view);

		public boolean sendToBack(String title, Component view);

		public void insertTab(String title, Icon icon, Component component, String tip, int index);

		public void addChangeListener(ChangeListener cc);

		public int indexOfComponent(Component view);

		public int getSelectedIndex();

	}

	@Override public boolean equals(Object obj);

	enum SetTabTo {
		ADD, REMOVE,
	}

	public void setTabs(ObjectTabs tabs, DisplayContext context, Object object, Class objClass, SetTabTo cmds);
}
