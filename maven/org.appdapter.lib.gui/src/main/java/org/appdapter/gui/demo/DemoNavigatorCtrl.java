package org.appdapter.gui.demo;

import javax.swing.JFrame;
import javax.swing.tree.TreeModel;

import org.appdapter.api.trigger.BoxContext;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.api.trigger.UserResult;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.DisplayContextProvider;
import org.appdapter.gui.api.ScreenBoxTreeNode;

public class DemoNavigatorCtrl extends BaseDemoNavigatorCtrl implements DisplayContext, org.appdapter.demo.DemoBrowserCtrl {

	public DemoNavigatorCtrl() {
		super();
	}

	@Override public void addObject(String title, Object obj, boolean displayForegroundASAP) {
		attachChildUI(title, obj, displayForegroundASAP);
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

	@Override public UserResult attachChildUI(String title, Object boxOrRepo, boolean showASAP) {
		return super.attachChildUI(title, boxOrRepo, showASAP);
	}

	public void addObject(String title, Object boxOrRepo) {
		attachChildUI(title, boxOrRepo, false);
	}

	public JFrame getFrame() {
		return super.getFrame();
	}

	@Override public void show() {
		super.launchFrame(getClass().getCanonicalName());
	}
}
