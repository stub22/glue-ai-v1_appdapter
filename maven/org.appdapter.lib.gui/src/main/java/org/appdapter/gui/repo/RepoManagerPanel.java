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

import java.beans.Customizer;
import java.io.File;
import java.lang.reflect.Type;

import javax.swing.JFileChooser;

import org.appdapter.api.trigger.AnyOper.UtilClass;
import org.appdapter.api.trigger.GetObject;
import org.appdapter.core.matdat.OmniLoaderRepo;
import org.appdapter.core.name.Ident;
import org.appdapter.core.store.Repo;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.editors.ObjectPanel;
import org.appdapter.gui.swing.ScreenBoxPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class RepoManagerPanel extends ScreenBoxPanel<MutableRepoBox> implements Customizer, UtilClass, ObjectPanel {
	static Logger theLogger = LoggerFactory.getLogger(RepoManagerPanel.class);

	@UISalient(IsPanel = true) static public RepoManagerPanel showRepoManagerPanel(final Repo repo) {
		RepoManagerPanel rp = new RepoManagerPanel();
		rp.setObject(repo);
		return rp;
	}

	@UISalient static public Repo.WithDirectory createNewRepoWithModelForDirectory(final Model repo) {
		return new OmniLoaderRepo(repo);
	}

	@UISalient(IsFactoryMethod = true)//
	static public Repo.WithDirectory createNewRepoWithBlankModelForDirectory() {
		return new OmniLoaderRepo(ModelFactory.createDefaultModel());
	}

	public static Type[] EDITTYPE = new Type[] { Repo.class, mapOf(Ident.class, makeParameterizedType(GetObject.class, Model.class)) };

	@Override protected void initSubclassGUI() throws Throwable {
		initComponents();
	}

	@Override protected void completeSubClassGUI() {
	}

	@Override public boolean isObjectBoundGUI() {
		return false;
	}

	@Override public Class<MutableRepoBox> getClassOfBox() {
		return MutableRepoBox.class;
	}

	private MutableRepoBox myFocusBox;
	private RepoGraphTableModel myRGTM;

	/** Creates new form RepoManagerPanel */
	public RepoManagerPanel() {
		if (!java.beans.Beans.isDesignTime()) {
			myRGTM = new RepoGraphTableModel();
			myGraphTable.setModel(myRGTM);
			Utility.makeTablePopupHandler(myGraphTable);
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
		if (b == myFocusBox)
			return;
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
			return myFocusBox.getValue();
		return this;
	}

	@Override public void objectValueChanged(Object oldValue, Object newValue) {
		if (newValue == null)
			return;
		reloadObjectGUI(newValue);
	}

	@Override protected boolean reloadObjectGUI(Object newValue) throws ClassCastException {
		MutableRepoBox mrb = null;
		if (newValue instanceof MutableRepoBox) {
			mrb = (MutableRepoBox) newValue;
		} else {
			mrb = new DefaultMutableRepoBoxImpl(Utility.getUniqueName(newValue), Utility.recastCC(newValue, Repo.WithDirectory.class));
		}
		focusOnBox((MutableRepoBox) mrb);
		return true;
	}
}
