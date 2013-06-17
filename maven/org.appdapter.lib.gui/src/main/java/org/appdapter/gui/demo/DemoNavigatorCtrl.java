package org.appdapter.gui.demo;

import javax.swing.JFrame;
import javax.swing.tree.TreeModel;

import org.appdapter.api.trigger.BoxContext;
import org.appdapter.api.trigger.BrowserPanelGUI;
import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.api.trigger.DisplayContextProvider;
import org.appdapter.api.trigger.ITabUI;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.api.trigger.ScreenBoxTreeNode;
import org.appdapter.core.log.Debuggable;
import org.appdapter.demo.DemoBrowserCtrl;

abstract public class DemoNavigatorCtrl extends BaseDemoNavigatorCtrl implements DisplayContext, org.appdapter.demo.DemoBrowserCtrl {

	public DemoNavigatorCtrl() {
		super();
	}

	public DemoNavigatorCtrl(BoxContext bc, TreeModel tm, ScreenBoxTreeNode rootBTN, DisplayContextProvider dcp) {
		super(bc, tm, rootBTN, dcp);
	}

	@Override public void launchFrame(String title) {
		super.launchFrame(title);
	}

	public void addBoxToRoot(MutableBox childBox, boolean reload) {
		super.addBoxToRoot(childBox, reload);
	}

	@Override public void addRepo(String title, Object boxOrRepo) {
		super.addRepo(title, boxOrRepo);
	}

	@Override public void addObject(String title, Object boxOrRepo, boolean showASP) {
		super.addObject(title, boxOrRepo, showASP);
	}

	public void addObject(String title, Object boxOrRepo) {
		super.addObject(title, boxOrRepo, false);
	}

	public JFrame getFrame() {
		return super.getFrame();
	}
}
