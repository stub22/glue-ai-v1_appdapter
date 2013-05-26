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
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.TreeModel;

import org.appdapter.api.trigger.BoxPanelSwitchableView;
import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.demo.ObjectNavigatorGUI;
import org.appdapter.gui.demo.LargeClassChooser;
import org.appdapter.gui.demo.ObjectNavigator;
import org.appdapter.gui.pojo.BrowsePanelContolApp;
import org.appdapter.gui.pojo.NamedObjectCollection;
import org.appdapter.gui.pojo.Utility;

import com.jidesoft.swing.JideSplitPane;
import com.jidesoft.swing.JideTabbedPane;
import com.jidesoft.tree.StyledTreeCellRenderer;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BrowsePanel extends javax.swing.JPanel implements DisplayContext, ObjectNavigatorGUI {

	private TreeModel myTreeModel;
	final BrowsePanelContolApp app;

	/** Creates new form BrowsePanel */
	public BrowsePanel(TreeModel tm) {
		Utility.mainDisplayContext = this;
		myTreeModel = tm;
		initComponents();
		myBoxPanelTabPane.setUseDefaultShowCloseButtonOnTab(true);
		myBoxPanelTabPane.setShowCloseButtonOnTab(true);
		myBoxPanelTabPane.setTabEditingAllowed(true);
		myBoxPanelTabPane.setBoldActiveTab(true);
		myBoxPanelTabPane.setShowCloseButtonOnMouseOver(false);
		app = new BrowsePanelContolApp(this);
		NamedObjectCollection ctx = Utility.context;
		Utility.selectionOfCollectionPanel = new LargeClassChooser(app);
		Utility.objectNavigator = new ObjectNavigator(Utility.selectionOfCollectionPanel, app, ctx);
		//myBoxPanelTabPane.add("Class Browser", Utility.selectionOfCollectionPanel);
		myBoxPanelTabPane.add("POJO Browser", Utility.objectNavigator.getPOJOCollectionPanel());
		hookTree();
	}

	private void hookTree() {
		myTree.setCellRenderer(new StyledTreeCellRenderer() {
			protected void customizeStyledLabel(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				super.customizeStyledLabel(tree, value, sel, expanded, leaf, row, hasFocus);
				String text = getText();
				// here is the code to customize she StyledLabel for each tree node
			}
		});

	}

	@Override public void setVisible(boolean aFlag) {
		super.setVisible(aFlag);
		checkParent();
	}

	public void addNotify() {
		super.addNotify();
		checkParent();
	}

	@Override public void show() {
		super.show();
		checkParent();
	}

	Container hookedParent = null;
	private JMenuBar oldJMenuBar;
	boolean checkingParent = false;

	public void checkParent() {
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
			if (myTopFrameMenu == null || true) {
				resetMenu();
			}
			jf.setJMenuBar(myTopFrameMenu);
			jf.setLayout(new BorderLayout());
			jf.add(this);
			return;
		}
	}

	private static void createLookAndFeelMenu() {
		JMenuBar menuBar = Utility.getMenuBar();
		menuBar.add(new LookAndFeelMenuItems("Look and Feel"));
	}

	private void resetMenu() {

		JMenuBar menuBar;
		JMenu menu, submenu;
		JMenuItem menuItem;
		JRadioButtonMenuItem rbMenuItem;
		JCheckBoxMenuItem cbMenuItem;
		menuBar = myTopFrameMenu = Utility.getMenuBar();
		myTopFrameMenu.removeAll();
		menuBar.add(Utility.objectNavigator.getMenu());
		//Build the first menu.
		menu = new JMenu("A Menu");
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription("The only menu in this program that has menu items");
		menuBar.add(menu);

		//a group of JMenuItems
		menuItem = new JMenuItem("A text-only menu item", KeyEvent.VK_T);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
		menu.add(menuItem);

		menuItem = new JMenuItem("Both text and icon", new ImageIcon("images/middle.gif"));
		menuItem.setMnemonic(KeyEvent.VK_B);
		menu.add(menuItem);

		menuItem = new JMenuItem(new ImageIcon("images/middle.gif"));
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
		submenu = new JMenu("A submenu");
		submenu.setMnemonic(KeyEvent.VK_S);

		menuItem = new JMenuItem("An item in the submenu");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
		submenu.add(menuItem);

		menuItem = new JMenuItem("Another item");
		submenu.add(menuItem);
		menu.add(submenu);

		createLookAndFeelMenu();

		if (oldJMenuBar != null && oldJMenuBar != menuBar)
			menuBar.add(oldJMenuBar);

	}

	//GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		myTopFrameMenu = new javax.swing.JMenuBar();
		myBrowserSplitPane = new JideSplitPane();
		myTreeScrollPane = new com.jidesoft.swing.JideScrollPane();
		myTree = new javax.swing.JTree();
		myContentPanel = new javax.swing.JPanel();
		myBoxPanelStatus = new javax.swing.JTextField();
		myBoxPanelTabPane = new com.jidesoft.swing.JideTabbedPane();
		myHomeBoxPanel = new javax.swing.JPanel();
		myLowerPanel = new JPanel();
		myCmdInputTextField = new javax.swing.JTextField();
		myLogScrollPane = new com.jidesoft.swing.JideScrollPane();
		myLogTextArea = new javax.swing.JTextArea();

		setLayout(new java.awt.BorderLayout());

		myBrowserSplitPane.setDividerLocation(0, 140);

		myTree.setModel(myTreeModel);
		myTreeScrollPane.setViewportView(myTree);

		myBrowserSplitPane.addPane(myTreeScrollPane);

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
		myBoxPanelTabPane.setBoldActiveTab(true);

		javax.swing.GroupLayout myHomeBoxPanelLayout = new javax.swing.GroupLayout(myHomeBoxPanel);
		myHomeBoxPanel.setLayout(myHomeBoxPanelLayout);
		myHomeBoxPanelLayout.setHorizontalGroup(myHomeBoxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 784, Short.MAX_VALUE));
		myHomeBoxPanelLayout.setVerticalGroup(myHomeBoxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 519, Short.MAX_VALUE));

		myBoxPanelTabPane.addTab("Home", myHomeBoxPanel);

		myLowerPanel.setLayout(new java.awt.BorderLayout());

		myCmdInputTextField.setText("console input - type/paste commands/uris/urls here, and see output in resizable pane below.   NOTE:  The tabs at upper right can hold any Swing GUI components");
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

		myBoxPanelTabPane.addTab("Command", myLowerPanel);

		myContentPanel.add(myBoxPanelTabPane, java.awt.BorderLayout.LINE_START);

		myBrowserSplitPane.addPane(myContentPanel);

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

	@Override public BoxPanelSwitchableView getBoxPanelTabPane() {
		return Utility.tabbedPanels;
	}

	@Override public JTabbedPane getRealPanelTabPane() {
		return myBoxPanelTabPane;
	}

	public JTextField getBoxPanelStatus() {
		return myBoxPanelStatus;
	}

	//GEN-BEGIN:variables
	// Variables declaration - do not modify
	private javax.swing.JTextField myBoxPanelStatus;
	private JideTabbedPane myBoxPanelTabPane;
	private JideSplitPane myBrowserSplitPane;
	private javax.swing.JTextField myCmdInputTextField;
	private javax.swing.JPanel myContentPanel;
	private javax.swing.JPanel myHomeBoxPanel;
	private JScrollPane myLogScrollPane;
	private javax.swing.JTextArea myLogTextArea;
	private javax.swing.JPanel myLowerPanel;
	private javax.swing.JMenuBar myTopFrameMenu;
	private javax.swing.JTree myTree;
	private JScrollPane myTreeScrollPane;

	// End of variables declaration//GEN-END:variables

	@Override public NamedObjectCollection getCollectionWithSwizzler() {
		return Utility.getCollectionWithSwizzler();
	}

	public void showMessage(String message) {
		myBoxPanelStatus.setText(message);
	}
}
