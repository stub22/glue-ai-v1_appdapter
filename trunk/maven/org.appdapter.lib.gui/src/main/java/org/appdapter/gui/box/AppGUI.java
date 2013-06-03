package org.appdapter.gui.box;

import java.awt.Container;

import javax.swing.JDesktopPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;

public interface AppGUI {

	JTree getTree();

	JTabbedPane getTabbedPane();

	Container getGenericContainer();

	JDesktopPane getDesktopPane();

}