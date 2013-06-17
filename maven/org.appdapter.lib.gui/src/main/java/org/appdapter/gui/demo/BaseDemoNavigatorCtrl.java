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
import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.appdapter.api.trigger.BT;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxContext;
import org.appdapter.api.trigger.BoxPanelSwitchableView;
import org.appdapter.api.trigger.BrowserPanelGUI;
import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.api.trigger.DisplayContextProvider;
import org.appdapter.api.trigger.DisplayType;
import org.appdapter.api.trigger.ITabUI;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.api.trigger.NamedObjectCollection;
import org.appdapter.api.trigger.POJOCollectionListener;
import org.appdapter.api.trigger.ScreenBoxTreeNode;
import org.appdapter.api.trigger.UserResult;
import org.appdapter.core.log.Debuggable;
import org.appdapter.demo.DemoBrowserCtrl;
import org.appdapter.gui.api.Utility;
import org.appdapter.gui.box.ScreenBoxContextImpl;
import org.appdapter.gui.browse.BrowsePanel;
import org.appdapter.gui.browse.ScreenBoxTreeNodeImpl;
import org.appdapter.gui.rimpl.TriggerMenuFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
abstract public class BaseDemoNavigatorCtrl implements BrowserPanelGUI, org.appdapter.demo.DemoBrowserCtrl {

	public BaseDemoNavigatorCtrl() {
		myBoxCtx = new ScreenBoxContextImpl();
		myTM = ((ScreenBoxContextImpl) myBoxCtx).getTreeModel();
	}

	public BrowserPanelGUI getLocalTreeAPI() {
		Debuggable.notImplemented();
		return null;
	}

	private TreeModel myTM;
	private BoxContext myBoxCtx;
	private ScreenBoxTreeNodeImpl myRootBTN;
	private DisplayContextProvider myDCP;
	private BrowsePanel myBP;
	private JFrame myJFrame;

	@Override public DisplayContext getDisplayContext() {
		if (myBP != null)
			return myBP.getDisplayContext();
		return this;
	}

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
		myBP = new BrowsePanel(myTM);
		myRootBTN.setDisplayContext(myBP.getDisplayContext());
		TriggerMenuFactory tmf = TriggerMenuFactory.getInstance(myBoxCtx); // TODO: Needs type params
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

	@Override public UserResult showScreenBox(Object anyObject) {
		try {
			return myBP.showScreenBox(anyObject);
		} catch (Exception e) {
			return myBP.showError(e.getMessage(), e);
		}
	}

	public JFrame getFrame() {
		return myJFrame;
	}

	public UserResult addObject(String title, Object object, DisplayType displayType) throws UnsatisfiedLinkError {
		return addObject(title, object, displayType, true);
	}

	public Component getComponent() {
		return myBP;
	}

	@Override public NamedObjectCollection getLocalBoxedChildren() {
		return myBP.getNamedObjectCollection();
	}

	@Override public UserResult showMessage(String message) {
		return myBP.showMessage(message);
	}

	@Override public BoxPanelSwitchableView getBoxPanelTabPane() {
		return myBP.getBoxPanelTabPane();
	}

	public UserResult addObject(String title, Object child, DisplayType attachType, boolean showAsap) throws UnsatisfiedLinkError {
		return myBP.addObject(title, child, attachType, showAsap);

	}

	public void addRepo(String title, Object boxOrRepo) {
		addObject(title, boxOrRepo, DisplayType.TREE, true);
	}

	@Override public Collection getTriggersFromUI(Object object) {
		return myBP.getTriggersFromUI(object);
	}

	@Override public UserResult showError(String msg, Throwable error) {
		return myBP.showError(msg, error);
	}

	public void addObject(String title, Object boxOrRepo, boolean showASP) {
		addObject(title, boxOrRepo, DisplayType.ANY, showASP);
	}

	@Override public ITabUI getLocalCollectionUI() {
		Debuggable.notImplemented();
		return null;
	}

	@Override public UserResult attachChildUI(String title, Object value) throws Exception {
		Debuggable.notImplemented();
		return null;
	}

	@Override public String getTitleOf(Object value) {
		Debuggable.notImplemented();
		return null;
	}

	@Override public void addListener(POJOCollectionListener objectChoice) {
		Debuggable.notImplemented();

	}

	@Override public Iterator<Object> getObjects() {
		Debuggable.notImplemented();
		return null;
	}

	@Override public Object findObjectByName(String n) {
		Debuggable.notImplemented();
		return null;
	}

	@Override public Collection findObjectsByType(Class type) {
		Debuggable.notImplemented();
		return null;
	}

	@Override public BT findOrCreateBox(Object newObject) {
		Debuggable.notImplemented();
		return null;
	}

	@Override public void renameObject(String oldName, String newName) throws PropertyVetoException {
		Debuggable.notImplemented();

	}

	@Override public void attachChildUI(String title, Object obj, boolean displayForegroundASAP) {
		Debuggable.notImplemented();
	}

	@Override public void initialize(String[] args) {
		Debuggable.notImplemented();
	}

}
