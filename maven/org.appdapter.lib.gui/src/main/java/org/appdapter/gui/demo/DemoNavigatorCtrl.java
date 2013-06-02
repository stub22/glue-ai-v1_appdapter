package org.appdapter.gui.demo;

import javax.swing.JFrame;
import javax.swing.tree.TreeModel;

import org.appdapter.api.trigger.BoxContext;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.gui.box.ScreenBoxTreeNode;
import org.appdapter.gui.browse.DisplayContext;
import org.appdapter.gui.browse.DisplayContextProvider;
import org.appdapter.gui.pojo.DisplayType;

public abstract class DemoNavigatorCtrl implements DisplayContext {

	public DemoNavigatorCtrl() {

	}

	public DemoNavigatorCtrl(BoxContext bc, TreeModel tm, ScreenBoxTreeNode rootBTN, DisplayContextProvider dcp) {

	}

	public abstract void launchFrame(String frametitle) throws UnsatisfiedLinkError;

	public abstract void addBoxToRoot(MutableBox child, boolean reload) throws UnsatisfiedLinkError;

	public abstract void addObject(String title, Object child, DisplayType attachType, boolean showAsap) throws UnsatisfiedLinkError;

	public abstract JFrame getFrame() throws UnsatisfiedLinkError;

}
