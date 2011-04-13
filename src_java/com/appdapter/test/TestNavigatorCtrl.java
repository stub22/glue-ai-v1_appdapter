/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import javax.swing.tree.TreeModel;

/**
 *
 * @author winston
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
	public void addBoxToRoot(MutableBox childBox) {
		Box rootBox =  myBC.getRootBox();
		myBC.contextualizeAndAttachChildBox(rootBox, childBox);
	}
}
