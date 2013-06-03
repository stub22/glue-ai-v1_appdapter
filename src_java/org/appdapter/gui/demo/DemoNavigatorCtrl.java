package org.appdapter.gui.demo;

import javax.swing.tree.TreeModel;

import org.appdapter.api.trigger.BoxContext;
import org.appdapter.gui.box.DisplayContextProvider;
import org.appdapter.gui.box.ScreenBoxTreeNode;
import org.appdapter.gui.browse.DisplayContext;
import org.appdapter.gui.pojo.DisplayType;

public class DemoNavigatorCtrl extends DemoNavigatorCtrl_NewGUI implements DisplayContext {

	public DemoNavigatorCtrl(BoxContext bc, TreeModel tm, ScreenBoxTreeNode rootBTN, DisplayContextProvider dcp) {
		super(bc, tm, rootBTN, dcp);
	}
	
	public void addRepo(String title, Object boxOrRepo) {
		super.addObject(title, boxOrRepo, DisplayType.TREE, true);
	}
}
