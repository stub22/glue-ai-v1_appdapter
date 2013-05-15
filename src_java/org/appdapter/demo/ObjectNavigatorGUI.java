package org.appdapter.demo;

import javax.swing.JComponent;

import org.appdapter.gui.pojo.POJOCollectionWithSwizzler;

public interface ObjectNavigatorGUI {

	POJOCollectionWithSwizzler getCollectionWithSwizzler();

	JComponent getDesk();

	void showMessage(String string);

}