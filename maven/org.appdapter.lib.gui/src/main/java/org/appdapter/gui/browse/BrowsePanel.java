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
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.util.Collection;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.tree.TreeModel;

import org.appdapter.api.trigger.BoxContext;
import org.appdapter.api.trigger.UserResult;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.BoxPanelSwitchableView;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.DisplayType;
import org.appdapter.gui.api.IShowObjectMessageAndErrors;
import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.swing.CollectionEditorUtil;
import org.appdapter.gui.swing.DisplayContextUIImpl;
import org.appdapter.gui.swing.LookAndFeelMenuItems;
import org.appdapter.gui.swing.ObjectTabsForTabbedView;
import org.appdapter.gui.swing.SafeJMenu;
import org.appdapter.gui.swing.SafeJMenuItem;

import com.jidesoft.swing.JideScrollPane;
import com.jidesoft.swing.JideSplitPane;
import com.jidesoft.swing.JideTabbedPane;
import com.jidesoft.tree.StyledTreeCellRenderer;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BrowsePanel extends javax.swing.JPanel implements IShowObjectMessageAndErrors {

	private TreeModel myTreeModel;
	final DisplayContextUIImpl app;
	private AddToTreeListener addToTreeListener;
	public BoxContext myBoxContext;

	public BoxContext getBoxContext() {
		return myBoxContext;
	}
	
	public static void main(String[] args) {
		Utility.ensureRunning();
	}

	/** Creates new form BrowsePanel */
	public BrowsePanel(TreeModel tm, BoxContext bctx0) {
		Utility.uiObjects.toString();
		Utility.browserPanel = this;
		myTreeModel = tm;
		initComponents();
		Utility.theBoxPanelDisplayContext = myBoxPanelSwitchableViewImpl = new ObjectTabsForTabbedView(myBoxPanelTabPane);
		setTabbedPaneOptions();
		Utility.controlApp = app = new DisplayContextUIImpl(myBoxPanelSwitchableViewImpl, this, Utility.uiObjects);
		NamedObjectCollection ctx = Utility.getTreeBoxCollection();
		Utility.collectionWatcher = new CollectionEditorUtil(app, ctx);
		//myBoxPanelTabPane.add("Class Browser", Utility.selectionOfCollectionPanel);
		myBoxPanelTabPane.add("POJO Browser", Utility.collectionWatcher.getNamedItemChooserPanel());
		myBoxContext = bctx0;
		hookTree();
		this.addToTreeListener = new AddToTreeListener(myTree, ctx, bctx0);
		invalidate();
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
		myTree.setCellRenderer(new StyledTreeCellRenderer() {
			protected void customizeStyledLabel(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hazFocus) {
				super.customizeStyledLabel(tree, value, sel, expanded, leaf, row, hazFocus);
				String text = getText();
				if (false && hazFocus && sel) {
					Object deref = Utility.dref(value);
					try {
						showScreenBox(deref);
					} catch (Exception e) {
						e.printStackTrace();
						throw Debuggable.reThrowable(e);
					}
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

		menuBar.add(Utility.collectionWatcher.getFileMenu());
		//Build the first menu.
		menuBar.add(makeSubMenu1Example());

		createLookAndFeelMenu();

		if (oldJMenuBar != null && oldJMenuBar != menuBar)
			menuBar.add(oldJMenuBar);

	}

	private JMenu makeSubMenu1Example() {
		SafeJMenu menu, submenu;
		SafeJMenuItem menuItem;
		JRadioButtonMenuItem rbMenuItem;
		JCheckBoxMenuItem cbMenuItem;

		Utility.toolsMenu = menu = new SafeJMenu("Tools");
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription("Tool menu items");
		Utility.updateToolsMenu();
		if (true)
			return menu;

		//a group of JMenuItems
		menuItem = new SafeJMenuItem("A text-only menu item", KeyEvent.VK_T);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
		menu.add(menuItem);

		menuItem = new SafeJMenuItem("Both text and icon", new ImageIcon("images/middle.gif"));
		menuItem.setMnemonic(KeyEvent.VK_B);
		menu.add(menuItem);

		menuItem = new SafeJMenuItem(new ImageIcon("images/middle.gif"));
		menuItem.setMnemonic(KeyEvent.VK_D);
		menu.add(menuItem);

		//a group of radio button menu items
		menu.addSeparator();
		ButtonGroup group = new ButtonGroup();
		rbMenuItem = new JRadioButtonMenuItem("A radio button menu item");
		rbMenuItem.setSelected(true);
		rbMenuItem.setMnemonic(KeyEvent.VK_R);
		group.add(rbMenuItem);
		menu.add(rbMenuItem);

		rbMenuItem = new JRadioButtonMenuItem("Another one");
		rbMenuItem.setMnemonic(KeyEvent.VK_O);
		group.add(rbMenuItem);
		menu.add(rbMenuItem);

		//a group of check box menu items
		menu.addSeparator();
		cbMenuItem = new JCheckBoxMenuItem("A check box menu item");
		cbMenuItem.setMnemonic(KeyEvent.VK_C);
		menu.add(cbMenuItem);

		cbMenuItem = new JCheckBoxMenuItem("Another one");
		cbMenuItem.setMnemonic(KeyEvent.VK_H);
		menu.add(cbMenuItem);

		//a submenu
		menu.addSeparator();
		submenu = new SafeJMenu("A submenu");
		submenu.setMnemonic(KeyEvent.VK_S);

		menuItem = new SafeJMenuItem("An item in the submenu");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
		submenu.add(menuItem);

		menuItem = new SafeJMenuItem("Another item");
		submenu.add(menuItem);

		menu.add(submenu);

		return menu;
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

		myContentPanel.setBackground(new java.awt.Color(51, 0, 51));
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

	public NamedObjectCollection getTreeBoxCollection() {
		return Utility.getTreeBoxCollection();
	}

	public UserResult showMessage(String message) {
		myBoxPanelStatus.setText(message);
		return null;
	}

	public String getMessage() {
		return myBoxPanelStatus.getText();
	}

	public UserResult showScreenBox(String title, Object anyObject) {
		try {
			if (anyObject == null)
				return Utility.asUserResult(null);
			return addObject(title, anyObject, Utility.getDisplayType(anyObject.getClass()), true);
		} catch (Exception e) {
			throw Debuggable.UnhandledException(e);
		}
	}

	@Override public UserResult showScreenBox(Object anyObject) throws Exception {
		return showScreenBox(null, anyObject);
	}

	public UserResult addObject(String title, Object anyObject, DisplayType attachType, boolean showAsap) {
		try {
			BT impl = getTreeBoxCollection().findOrCreateBox(title, anyObject);
			if (showAsap) {
				return app.showScreenBox(title, impl);
			}
			return UserResult.SUCCESS;
		} catch (Exception e) {
			throw Debuggable.UnhandledException(e);
		}
	}

	public UserResult showScreenBox(String title, Object anyObject, DisplayType attachType) {
		return addObject(title, anyObject, attachType, true);
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

	public Collection getTriggersFromUI(BT box, Object object) {
		return app.getTriggersFromUI(box, object);
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
}
