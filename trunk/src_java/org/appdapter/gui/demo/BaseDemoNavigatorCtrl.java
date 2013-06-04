/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.org).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.appdapter.gui.demo;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxContext;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.gui.box.BoxPanelSwitchableView;
import org.appdapter.gui.box.DisplayContextProvider;
import org.appdapter.gui.box.ScreenBoxImpl;
import org.appdapter.gui.box.ScreenBoxPanel;
import org.appdapter.gui.box.ScreenBoxTreeNode;
import org.appdapter.gui.browse.BrowsePanel;
import org.appdapter.gui.browse.DisplayContext;
import org.appdapter.gui.browse.ScreenBoxTreeNodeImpl;
import org.appdapter.gui.browse.TriggerMenuFactory;
import org.appdapter.gui.pojo.DisplayType;
import org.appdapter.gui.pojo.NamedObjectCollection;
import org.appdapter.gui.pojo.Utility;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BaseDemoNavigatorCtrl implements DisplayContext {

	private TreeModel myTM;
	private BoxContext myBoxCtx;
	private ScreenBoxTreeNodeImpl myRootBTN;
	private DisplayContextProvider myDCP;
	private BrowsePanel myBP;
	private JFrame myJFrame;

	public BrowsePanel getBrowsePanel() {
		return myBP;
	}

	public BaseDemoNavigatorCtrl(BoxContext bc, TreeModel tm, ScreenBoxTreeNode rootBTN, DisplayContextProvider dcp) {
		myBoxCtx = bc;
		myTM = tm;
		myRootBTN = (ScreenBoxTreeNodeImpl) rootBTN;
		myDCP = dcp;
		setupBrowsePanel();
	}

	private void setupBrowsePanel() {
		Utility.browserPanel = myBP = new BrowsePanel(myTM);
		myRootBTN.setDisplayContext(myBP);
		TriggerMenuFactory tmf = new TriggerMenuFactory(); // TODO: Needs type params
		MouseAdapter menuMA = tmf.makePopupMouseAdapter();
		myBP.addTreeMouseAdapter(menuMA);
	}

	public void launchFrame(String title) {
		myJFrame = Utility.getAppFrame();
		if (myJFrame == null) {
			myJFrame = new JFrame();
			myJFrame.setTitle(title);
			myJFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			myJFrame.getContentPane().add(myBP, BorderLayout.CENTER);
			myBP.checkParent();
			myJFrame.pack();

			myJFrame.setVisible(true);
		} else {

			throw new RuntimeException("Frame already launched!");
		}
	}

	public void addBoxToRoot(MutableBox childBox, boolean reload) {
		Box rootBox = myBoxCtx.getRootBox();
		myBoxCtx.contextualizeAndAttachChildBox(rootBox, childBox);
		if (reload) {
			((DefaultTreeModel) myTM).reload();
		}
	}

	public ScreenBoxPanel showScreenBox(Object anyObject) {
		try {
			return Utility.boxPanelDisplayContext.showScreenBox(anyObject);
		} catch (Exception e) {
			myBP.showError(e.getMessage(), e);
			return null;
		}
	}

	public JFrame getFrame() {
		return myJFrame;
	}

	public DisplayContext addObject(String title, Object object, DisplayType displayType) throws UnsatisfiedLinkError {
		try {
			return Utility.boxPanelDisplayContext.addObject(title, object, displayType, true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	public Component getComponent() {
		return myBP;
	}

	public NamedObjectCollection getNamedObjectCollection() {
		return myBP.getNamedObjectCollection();
	}

	public void showMessage(String message) {
		myBP.showMessage(message);
	}

	public BoxPanelSwitchableView getBoxPanelTabPane() {
		return myBP.getBoxPanelTabPane();
	}

	public ScreenBoxImpl addObject(String title, Object child, DisplayType attachType, boolean showAsap) throws UnsatisfiedLinkError {
		return myBP.getBoxPanelTabPane().addObject(title, child, attachType, showAsap);

	}

}
