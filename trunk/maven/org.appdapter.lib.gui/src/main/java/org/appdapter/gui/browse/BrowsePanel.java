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
/*
 * BrowsePanel.java
 *
 * Created on Oct 25, 2010, 1:18:06 PM
 */

package org.appdapter.gui.browse;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.tree.TreeModel;

import org.appdapter.api.trigger.AnyOper.Singleton;
import org.appdapter.api.trigger.AnyOper.UIHidden;
import org.appdapter.api.trigger.AnyOper.UISalient;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxContext;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.api.trigger.UserResult;
import org.appdapter.core.convert.NoSuchConversionException;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.BoxPanelSwitchableView;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.DisplayType;
import org.appdapter.gui.api.IShowObjectMessageAndErrors;
import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.box.AbstractScreenBoxTreeNodeImpl;
import org.appdapter.gui.box.ScreenBoxContextImpl;
import org.appdapter.gui.box.ScreenBoxImpl;
import org.appdapter.gui.swing.CollectionEditorUtil;
import org.appdapter.gui.swing.DisplayContextUIImpl;
import org.appdapter.gui.swing.LookAndFeelMenuItems;
import org.appdapter.gui.swing.ObjectTabsForTabbedView;
import org.appdapter.gui.swing.SafeJMenu;

import com.jidesoft.swing.JideScrollPane;
import com.jidesoft.swing.JideSplitPane;
import com.jidesoft.swing.JideTabbedPane;
import com.jidesoft.tree.StyledTreeCellRenderer;

/**
 * @author Stu B. <www.texpedient.com>
 */
@UIHidden
public class BrowsePanel extends javax.swing.JPanel implements IShowObjectMessageAndErrors, Singleton {

	public TreeModel myTreeModel;
	public DisplayContextUIImpl app;
	public AddToTreeListener addToTreeListener;
	public BoxContext myBoxContext;
	private AddToTreeListener addClipToTreeListener;

	@UISalient
	boolean OnTreeFocusShowObject = false;

	public BoxContext getBoxContext() {
		return myBoxContext;
	}

	public static void main(String[] args) {
		Utility.ensureRunning();
	}

	/** Creates new form BrowsePanel */
	public BrowsePanel(TreeModel tm, BoxContext bctx0) {
		synchronized (Utility.featureQueueLock) {
			init(tm, bctx0, Utility.uiObjects, Utility.getClipboard());
		}
	}

	public void init(TreeModel tm, BoxContext bctx0, NamedObjectCollection ctx, NamedObjectCollection clipboard) {
		Utility.browserPanel = this;
		myTreeModel = tm;
		initComponents();
		JTree tree = myTree;
		Utility.theBoxPanelDisplayContext = myBoxPanelSwitchableViewImpl = new ObjectTabsForTabbedView(myBoxPanelTabPane, true);
		setTabbedPaneOptions();
		Utility.controlApp = app = new DisplayContextUIImpl(myBoxPanelSwitchableViewImpl, this, ctx);
		Utility.clipBoardUtil = new CollectionEditorUtil(clipboard.getName(), app, clipboard);
		myBoxPanelTabPane.add("Clipboard", Utility.clipBoardUtil.getGUIPanel());
		myBoxPanelTabPane.setBackground(Color.LIGHT_GRAY);
		UIManager.put("nimbusBase", new ColorUIResource(128, 128, 128));
		myBoxContext = bctx0;
		hookTree();
		this.addToTreeListener = new AddToTreeListener(myTree, ctx, bctx0, (MutableBox) bctx0.getRootBox(), true);
		//addClipboard(clipboard);

		Utility.addObjectFeatures(this);
		invalidate();
	}

	private void addClipboard(NamedObjectCollection clipboard) {
		try {
			Box suposeRoot = myBoxContext.getRootBox();
			ScreenBoxImpl clipboardBox = new ScreenBoxImpl();
			clipboardBox.setObject(clipboard);
			addToTreeListener.addChildObject(suposeRoot, "Clipboard", clipboardBox);
			ScreenBoxContextImpl clipContext = new ScreenBoxContextImpl(clipboardBox);
			this.addClipToTreeListener = new AddToTreeListener(myTree, clipboard, clipContext, clipboardBox, false);
		} catch (Exception e) {

		}
	}

	private void setTabbedPaneOptions() {
		/**JIDESOFT	 */
		myBoxPanelTabPane.setUseDefaultShowCloseButtonOnTab(true);
		myBoxPanelTabPane.setShowCloseButtonOnTab(true);
		myBoxPanelTabPane.setTabEditingAllowed(true);
		myBoxPanelTabPane.setBoldActiveTab(true);
		myBoxPanelTabPane.setShowCloseButtonOnMouseOver(false);
	}

	private void hookTree() {
		ToolTipManager.sharedInstance().registerComponent(myTree);
		/**JIDESOFT */
		SearchableDemo.installSearchable(myTree);
		myTree.setCellRenderer(new StyledTreeCellRenderer() {

			protected void customizeStyledLabel(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hazFocus) {
				try {
					if (value instanceof AbstractScreenBoxTreeNodeImpl) {
						Box box = ((AbstractScreenBoxTreeNodeImpl) value).getBox();
						if (box != null)
							value = box;
					}
					String valueStr = Utility.getUniqueName(value);
					super.customizeStyledLabel(tree, valueStr, sel, expanded, leaf, row, hazFocus);
					setText(valueStr);
					if (OnTreeFocusShowObject && hazFocus && sel) {
						Object deref = Utility.dref(value);
						if (deref != null && deref != value) {
							try {
								String text = getText();
								showScreenBox(text, deref);
							} catch (Exception e) {
								e.printStackTrace();
								throw Debuggable.reThrowable(e);
							}
						}
					}
				} catch (Throwable t) {
					Debuggable.printStackTrace(t);
				}
				// here is the code to customize the StyledLabel for each tree node
			}

			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hazFocus) {

				Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hazFocus);
				setToolTipText(Utility.makeTooltipText(value));
				if (c != this) {
					return c;
				}
				return this;
			}

		});

	}

	public void setVisible(boolean aFlag) {
		checkParent();
		super.setVisible(aFlag);
		Utility.updateToolsMenu();
		Utility.updateLastResultsMenu();
	}

	public void addNotify() {
		super.addNotify();
		checkParent();
	}

	public void show() {
		checkParent();
		super.show();
	}

	Container hookedParent = null;
	private JMenuBar oldJMenuBar;
	boolean checkingParent = false;

	public synchronized void checkParent() {
		if (checkingParent)
			return;
		Container p = getTopLevelAncestor();
		if (p == null) {
			return;
		}
		checkingParent = true;
		if (p == hookedParent)
			return;
		unHookFrom(hookedParent);
		hookTo(p);
	}

	private void unHookFrom(Container p) {
		if (p == null)
			return;
		if (p instanceof JFrame) {
			JFrame jf = (JFrame) p;
			if (jf.getJMenuBar() == myTopFrameMenu) {
				jf.setJMenuBar(oldJMenuBar);
			}
		}
	}

	private void hookTo(Container p) {
		if (p == null)
			return;
		if (p instanceof JFrame) {
			JFrame jf = (JFrame) p;
			JMenuBar nowMenuBar = jf.getJMenuBar();
			if (nowMenuBar != oldJMenuBar) {
				oldJMenuBar = nowMenuBar;
			}
			if (myTopFrameMenu == null) {

			}
			resetMenu();
			jf.setJMenuBar(myTopFrameMenu);
			myTopFrameMenu.setVisible(true);

			jf.setLayout(new BorderLayout());
			jf.add(this);
			return;
		}
	}

	private static void createLookAndFeelMenu() {
		try {
			JMenuBar menuBar = Utility.getMenuBar();
			menuBar.add(new LookAndFeelMenuItems("Look and Feel"));
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private void resetMenu() {

		JMenuBar menuBar;
		JMenu menu, submenu;
		menuBar = myTopFrameMenu = Utility.getMenuBar();

		myTopFrameMenu.removeAll();

		menuBar.add(Utility.clipBoardUtil.getFileMenu());

		menu = Utility.toolsMenu = new SafeJMenu(false, "Tools", this);
		menu.setMnemonic(KeyEvent.VK_T);
		menu.getAccessibleContext().setAccessibleDescription("Tool menu items");
		menuBar.add(menu);

		menu = Utility.lastResultsMenu = new SafeJMenu(false, "Results", this);
		menu.setMnemonic(KeyEvent.VK_R);
		menu.getAccessibleContext().setAccessibleDescription("Last returns");
		menuBar.add(menu);

		createLookAndFeelMenu();

		if (oldJMenuBar != null && oldJMenuBar != menuBar)
			menuBar.add(oldJMenuBar);

	}

	//GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		myTopFrameMenu = new javax.swing.JMenuBar();
		myBrowserSplitPane = new JideSplitPane();
		myTreeScrollPane = new JideScrollPane();
		myTree = new javax.swing.JTree();
		myContentPanel = new javax.swing.JPanel();
		myBoxPanelStatus = new javax.swing.JTextField();
		myBoxPanelTabPane = new JideTabbedPane();
		myHomeBoxPanel = new javax.swing.JPanel();
		myLowerPanel = new JPanel();
		myCmdInputTextField = new javax.swing.JTextField();
		myLogScrollPane = new JideScrollPane();
		myLogTextArea = new javax.swing.JTextArea();

		setLayout(new java.awt.BorderLayout());

		myBoxPanelTabPane = new JideTabbedPane();
		myTreeScrollPane = new JideScrollPane();
		myLogScrollPane = new JideScrollPane();
		myBrowserSplitPane.setDividerLocation(0, 140);

		myTree.setModel(myTreeModel);
		myTreeScrollPane.setViewportView(myTree);

		myBrowserSplitPane.add(myTreeScrollPane);

		myContentPanel.setBackground(new java.awt.Color(214,217,223));
		myContentPanel.setLayout(new java.awt.BorderLayout());

		myBoxPanelStatus.setText("Extra text field - used for status display and special console input .   This screen shows a box navigation system.");
		myBoxPanelStatus.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				myBoxPanelStatusActionPerformed(evt);
			}
		});
		myContentPanel.add(myBoxPanelStatus, java.awt.BorderLayout.NORTH);

		myBoxPanelTabPane.setBackground(new java.awt.Color(204, 204, 255));
		myBoxPanelTabPane.setAutoscrolls(true);

		javax.swing.GroupLayout myHomeBoxPanelLayout = new javax.swing.GroupLayout(myHomeBoxPanel);
		myHomeBoxPanel.setLayout(myHomeBoxPanelLayout);
		myHomeBoxPanelLayout.setHorizontalGroup(myHomeBoxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 784, Short.MAX_VALUE));
		myHomeBoxPanelLayout.setVerticalGroup(myHomeBoxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 519, Short.MAX_VALUE));

		myBoxPanelTabPane.add("Home", myHomeBoxPanel);

		myLowerPanel.setLayout(new java.awt.BorderLayout());

		myCmdInputTextField
				.setText("console input - type/paste commands/uris/urls here, and see output in resizable pane below.   NOTE:  The tabs at upper right can hold anyObject Swing GUI components");
		myCmdInputTextField.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				myCmdInputTextFieldActionPerformed(evt);
			}
		});
		myLowerPanel.add(myCmdInputTextField, java.awt.BorderLayout.NORTH);

		myLogTextArea.setColumns(20);
		myLogTextArea.setEditable(false);
		myLogTextArea.setRows(5);
		myLogTextArea.setText("one\ntwo\nthree\nfour\nfive\nsix\nseven\neight\nnine\nten");
		myLogScrollPane.setViewportView(myLogTextArea);

		myLowerPanel.add(myLogScrollPane, java.awt.BorderLayout.CENTER);

		myBoxPanelTabPane.add("Command", myLowerPanel);

		myContentPanel.add(myBoxPanelTabPane, java.awt.BorderLayout.LINE_START);

		myBrowserSplitPane.add(myContentPanel);

		add(myBrowserSplitPane, java.awt.BorderLayout.CENTER);
	}// </editor-fold>
	 //GEN-END:initComponents

	private void myCmdInputTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myCmdInputTextFieldActionPerformed
		// TODO add your handling code here:
	}//GEN-LAST:event_myCmdInputTextFieldActionPerformed

	private void myBoxPanelStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myBoxPanelStatusActionPerformed
		// TODO add your handling code here:

	}//GEN-LAST:event_myBoxPanelStatusActionPerformed

	public void addTreeMouseAdapter(MouseAdapter ma) {
		myTree.addMouseListener(ma);
	}

	public JTextField getBoxPanelStatus() {
		return myBoxPanelStatus;
	}

	//GEN-BEGIN:variables
	// Variables declaration - do not modify
	private javax.swing.JTextField myBoxPanelStatus;
	public JideTabbedPane myBoxPanelTabPane;
	private JideSplitPane myBrowserSplitPane;
	private javax.swing.JTextField myCmdInputTextField;
	private javax.swing.JPanel myContentPanel;
	private javax.swing.JPanel myHomeBoxPanel;
	private JideScrollPane myLogScrollPane;
	private javax.swing.JTextArea myLogTextArea;
	private javax.swing.JPanel myLowerPanel;
	private javax.swing.JMenuBar myTopFrameMenu;
	private JTree myTree;
	private JideScrollPane myTreeScrollPane;
	private BoxPanelSwitchableView myBoxPanelSwitchableViewImpl;

	// End of variables declaration//GEN-END:variables
	Class myBoxPanelStatusType = null;

	public NamedObjectCollection getTreeBoxCollection() {
		return Utility.getTreeBoxCollection();
	}

	public UserResult showMessage(String message, Class expected) {
		myBoxPanelStatus.setText(message);
		myBoxPanelStatusType = expected;
		Object was = null;
		if (expected != null && expected != String.class) {
			try {
				was = Utility.fromString(message, expected);
			} catch (NoSuchConversionException e) {
			}
			if (expected.isInstance(was)) {
				JPanel pnl = Utility.getPropertiesPanel(was);
				showScreenBox(message, pnl);
			}
		}

		return null;
	}

	public String getMessage() {
		return myBoxPanelStatus.getText();
	}

	public UserResult showScreenBox(String title, Object anyObject) {

		try {
			if (anyObject == null)
				return Utility.asUserResult(null);
			return addObject(title, anyObject, Utility.getDisplayType(anyObject.getClass()), true, false);
		} catch (Exception e) {
			throw Debuggable.UnhandledException(e);
		}
	}

	@Override public UserResult showScreenBox(Object anyObject) throws Exception {
		return showScreenBox(null, anyObject);
	}

	LinkedList<Object> workingOnShowingObject = new LinkedList<Object>();

	public UserResult addObject(String title, Object anyObject, DisplayType attachType, boolean showASAP, boolean expandChildren) {
		synchronized (workingOnShowingObject) {
			if (workingOnShowingObject.contains(anyObject))
				return UserResult.SUCCESS;
			workingOnShowingObject.add(anyObject);
		}
		try {
			BT impl = getTreeBoxCollection().findOrCreateBox(title, anyObject);
			if (showASAP) {
				return app.showScreenBox(title, impl);
			}
			if (expandChildren) {
				treeExpand(anyObject);
			}
			return UserResult.SUCCESS;
		} catch (Exception e) {
			//throw Debuggable.UnhandledException(e);
			return UserResult.SUCCESS;
		} finally {
			synchronized (workingOnShowingObject) {
				workingOnShowingObject.remove(anyObject);
			}
		}
	}

	public void treeExpand(Object anyObject) {
		addToTreeListener.treeExpand(anyObject, 1);
	}

	public UserResult showScreenBox(String title, Object anyObject, DisplayType attachType) {
		return addObject(title, anyObject, attachType, true, false);
	}

	public BoxPanelSwitchableView getBoxPanelTabPane() {
		if (myBoxPanelSwitchableViewImpl == null) {
			myBoxPanelSwitchableViewImpl = Utility.getBoxPanelTabPane();
		}
		return myBoxPanelSwitchableViewImpl;
	}

	public Component getComponent() {
		return this;
	}

	public JTree getTree() {
		return myTree;
	}

	public JTabbedPane getTabbedPane() {
		return myBoxPanelTabPane;
	}

	public Container getGenericContainer() {
		return myBoxPanelTabPane;
	}

	public JDesktopPane getDesktopPane() {
		// for now we do not want to use desktop panes
		return null;//myDesktopPane;
	}

	public Collection getTriggersFromUI(Object object) {
		return app.getTriggersFromUI(object);
	}

	public UserResult showError(String msg, Throwable error) {
		return app.showError(msg, error);
	}

	public DisplayContext getDisplayContext() {
		return app;
	}

	public void addTab(String title, JComponent thing) {
		myBoxPanelTabPane.add(title, thing);

	}

	@Override public UserResult addObject(String title, Object anyObject, boolean showASAP, boolean expandChildren) {
		UserResult res = addObject(title, anyObject, DisplayType.TREE, showASAP, expandChildren);
		return res;
	}

}
