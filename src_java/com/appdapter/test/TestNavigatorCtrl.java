/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.com).
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

package com.appdapter.test;

import com.appdapter.gui.box.Box;
import com.appdapter.gui.box.BoxContext;
import com.appdapter.gui.box.BoxTreeNode;
import com.appdapter.gui.box.DisplayContextProvider;
import com.appdapter.gui.box.MutableBox;
import com.appdapter.gui.browse.TriggerMenuFactory;
import com.appdapter.gui.browse.BrowsePanel;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import javax.swing.JFrame;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class TestNavigatorCtrl {


	public		TreeModel				myTM;
	public		BoxContext				myBC;
	public		BoxTreeNode				myRootBTN;
	private		DisplayContextProvider	myDCP;
	private		BrowsePanel				myBP;
	private		JFrame					myJFrame;

	public TestNavigatorCtrl(BoxContext bc, TreeModel tm, BoxTreeNode rootBTN, DisplayContextProvider dcp) {
		myBC = bc;
		myTM = tm;
		myRootBTN = rootBTN;
		myDCP = dcp;

		setupBrowsePanel();
	}
	private void setupBrowsePanel() {
		myBP = new BrowsePanel(myTM);
		myRootBTN.setDisplayContext(myBP);
		TriggerMenuFactory tmf = new TriggerMenuFactory(); // TODO: Needs type params
		MouseAdapter menuMA = tmf.makePopupMouseAdapter();
		myBP.addTreeMouseAdapter(menuMA);
	}
	public void launchFrame(String title) {
		if (myJFrame == null) {
			myJFrame = new JFrame(title);

			myJFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			myJFrame.getContentPane().add(myBP, BorderLayout.CENTER);

			myJFrame.pack();

			myJFrame.setVisible(true);
		} else {
			throw new RuntimeException("Frame already launched!");
		}
	}
	public void addBoxToRoot(MutableBox childBox, boolean reload) {
		Box rootBox =  myBC.getRootBox();
		myBC.contextualizeAndAttachChildBox(rootBox, childBox);
		if (reload) {
			((DefaultTreeModel) myTM).reload();
		}
	}
}
