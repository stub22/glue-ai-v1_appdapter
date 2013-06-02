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
 * RepoManagerPanel.java
 *
 * Created on Oct 27, 2010, 11:12:00 PM
 */

package org.appdapter.gui.repo;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import org.appdapter.api.trigger.Box;
import org.appdapter.core.store.MutableRepoBox;
import org.appdapter.gui.browse.TriggerMenuFactory;
import org.appdapter.gui.pojo.ScreenBoxedPOJOPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class RepoManagerPanel extends ScreenBoxedPOJOPanel<MutableRepoBox> {
	static Logger theLogger = LoggerFactory.getLogger(RepoManagerPanel.class);

	@Override protected void initGUI() {
		initComponents();
	}

	private MutableRepoBox myFocusBox;
	private RepoGraphTableModel myRGTM;

	/** Creates new form RepoManagerPanel */
	public RepoManagerPanel() {
		initComponents();
		if (!java.beans.Beans.isDesignTime()) {
			myRGTM = new RepoGraphTableModel();
			myGraphTable.setModel(myRGTM);
			makeTablePopupHandler(myGraphTable);
		}
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		myVerticalSplit = new javax.swing.JSplitPane();
		myTopPanel = new javax.swing.JPanel();
		myGraphTableScroller = new javax.swing.JScrollPane();
		myGraphTable = new javax.swing.JTable();
		myBottomPanel = new javax.swing.JPanel();
		myTF_graphName = new javax.swing.JTextField();
		myBut_chooseFile = new javax.swing.JButton();
		jLabel1 = new javax.swing.JLabel();

		setLayout(new java.awt.BorderLayout());

		myVerticalSplit.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

		myTopPanel.setLayout(new java.awt.BorderLayout());

		myGraphTable.setModel(new javax.swing.table.DefaultTableModel(
				new Object[][] { { null, null, null, null }, { null, null, null, null }, { null, null, null, null }, { null, null, null, null } }, new String[] { "Title 1", "Title 2", "Title 3",
						"Title 4" }));
		myGraphTableScroller.setViewportView(myGraphTable);

		myTopPanel.add(myGraphTableScroller, java.awt.BorderLayout.CENTER);

		myVerticalSplit.setTopComponent(myTopPanel);

		myTF_graphName.setText("jTextField1");
		myTF_graphName.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				myTF_graphNameActionPerformed(evt);
			}
		});

		myBut_chooseFile.setText("choose file");
		myBut_chooseFile.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				myBut_chooseFileActionPerformed(evt);
			}
		});

		jLabel1.setText("new graph name");

		javax.swing.GroupLayout myBottomPanelLayout = new javax.swing.GroupLayout(myBottomPanel);
		myBottomPanel.setLayout(myBottomPanelLayout);
		myBottomPanelLayout.setHorizontalGroup(myBottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				myBottomPanelLayout
						.createSequentialGroup()
						.addGap(31, 31, 31)
						.addGroup(
								myBottomPanelLayout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(jLabel1)
										.addGroup(
												myBottomPanelLayout.createSequentialGroup()
														.addComponent(myTF_graphName, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(29, 29, 29)
														.addComponent(myBut_chooseFile))).addContainerGap(124, Short.MAX_VALUE)));
		myBottomPanelLayout.setVerticalGroup(myBottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				myBottomPanelLayout
						.createSequentialGroup()
						.addGap(54, 54, 54)
						.addComponent(jLabel1)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(
								myBottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(myTF_graphName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(myBut_chooseFile)).addContainerGap(169, Short.MAX_VALUE)));

		myVerticalSplit.setRightComponent(myBottomPanel);

		add(myVerticalSplit, java.awt.BorderLayout.CENTER);
	}// </editor-fold>//GEN-END:initComponents

	private TriggerMenuFactory myTMF;

	private void makeTablePopupHandler(JTable jTable) {
		jTable.addMouseListener(new MouseAdapter() {

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					JTable source = (JTable) e.getSource();
					int row = source.rowAtPoint(e.getPoint());
					int column = source.columnAtPoint(e.getPoint());

					if (!source.isRowSelected(row)) {
						source.changeSelection(row, column, false, false);
					}
					JPopupMenu cellPopMenu = fetchMenuForFocusCell(row, column);
					if (cellPopMenu != null) {
						cellPopMenu.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			}
		});
	}

	private JPopupMenu fetchMenuForFocusCell(int row, int col) {
		JPopupMenu popMenu = null;
		Box cellSubBox = myRGTM.findSubBox(row, col);
		if (cellSubBox != null) {
			if (myTMF == null) {
				myTMF = new TriggerMenuFactory(); // TODO: Needs type params
			}
			popMenu = myTMF.buildPopupMenu(cellSubBox);
		}
		return popMenu;
	}

	private void myTF_graphNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myTF_graphNameActionPerformed
		// TODO add your handling code here:
	}//GEN-LAST:event_myTF_graphNameActionPerformed

	private void myBut_chooseFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myBut_chooseFileActionPerformed
		JFileChooser chooser = new JFileChooser(myFocusBox.getUploadHomePath());
		/*
		 * // Note: source for ExampleFileFilter can be found in FileChooserDemo, // under the demo/jfc directory in
		 * the Java 2 SDK, Standard Edition. ExampleFileFilter filter = new ExampleFileFilter();
		 * filter.addExtension("jpg"); filter.addExtension("gif"); filter.setDescription("JPG & GIF Images");
		 * chooser.setFileFilter(filter);
		 */
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File selectedFile = chooser.getSelectedFile();
			String fileName = selectedFile.getPath();
			String graphName = myTF_graphName.getText();
			String sourceURL = "file:" + fileName;
			theLogger.info("User selected source URL [" + sourceURL + "] and target graph [" + graphName + "]");

			myFocusBox.importGraphFromURL(graphName, sourceURL, true);
			myRGTM.refreshStats(myFocusBox);
		}
	}//GEN-LAST:event_myBut_chooseFileActionPerformed

	@Override public void focusOnBox(MutableRepoBox b) {
		myFocusBox = b;
		myRGTM.focusOnRepo(b);
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JLabel jLabel1;
	private javax.swing.JPanel myBottomPanel;
	private javax.swing.JButton myBut_chooseFile;
	private javax.swing.JTable myGraphTable;
	private javax.swing.JScrollPane myGraphTableScroller;
	private javax.swing.JTextField myTF_graphName;
	private javax.swing.JPanel myTopPanel;
	private javax.swing.JSplitPane myVerticalSplit;

	// End of variables declaration//GEN-END:variables

	@Override public Object getValue() {
		if (myFocusBox != null)
			return myFocusBox;
		return this;
	}

	@Override public void objectValueChanged(Object oldValue, Object newValue) {
		focusOnBox((Box) newValue);

	}
}
