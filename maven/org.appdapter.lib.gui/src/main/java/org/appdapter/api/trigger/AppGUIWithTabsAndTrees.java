package org.appdapter.api.trigger;

import java.awt.Container;

import javax.swing.JDesktopPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;


public interface AppGUIWithTabsAndTrees extends UIProvider {

	JTree getTree();

	JTabbedPane getTabbedPane();

	Container getGenericContainer();

	JDesktopPane getDesktopPane();

	NamedObjectCollection getLocalBoxedChildren();

	ITabUI getLocalCollectionUI();

	DisplayContext getDisplayContext();

}
