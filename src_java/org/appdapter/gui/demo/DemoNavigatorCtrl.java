package org.appdapter.gui.demo;

import javax.swing.tree.TreeModel;

import org.appdapter.api.trigger.BoxContext;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.gui.box.DisplayContextProvider;
import org.appdapter.gui.box.ScreenBoxTreeNode;
import org.appdapter.gui.browse.DisplayContext;
import org.appdapter.gui.pojo.DisplayType;

public class DemoNavigatorCtrl extends BaseDemoNavigatorCtrl implements DisplayContext {

	public DemoNavigatorCtrl(BoxContext bc, TreeModel tm, ScreenBoxTreeNode rootBTN, DisplayContextProvider dcp) {
		super(bc, tm, rootBTN, dcp);
	}

	@Override public void launchFrame(String title) {
		super.launchFrame(title);
	}

	@Override public void addBoxToRoot(MutableBox childBox, boolean reload) {
		super.addBoxToRoot(childBox, reload);
	}

	public void addRepo(String title, Object boxOrRepo) {
		super.addObject(title, boxOrRepo, DisplayType.TREE, true);
	}
}
