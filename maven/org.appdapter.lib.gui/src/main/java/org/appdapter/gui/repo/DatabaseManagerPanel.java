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
 * DatabaseManagerPanel.java
 *
 * Created on Oct 27, 2010, 11:11:14 PM
 */

package org.appdapter.gui.repo;

import java.sql.Connection;
import java.util.Map;

import org.appdapter.api.trigger.Box;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.swing.ScreenBoxPanel;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class DatabaseManagerPanel extends ScreenBoxPanel<Box> {

	@Override public Class<Connection> getClassOfBox() {
		return Connection.class;
	}

	/** Creates new form DatabaseManagerPanel */
	public DatabaseManagerPanel() {

	}

	@Override public boolean isObjectBoundGUI() {
		return false;
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {
		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 400, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 300, Short.MAX_VALUE));
	}// </editor-fold>//GEN-END:initComponents

	@Override protected void initSubclassGUI() throws Throwable {
		initComponents();
	}

	@Override protected void completeSubClassGUI() {
	}

	@Override public void focusOnBox(Box b) {
		setObject(b);
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override public void objectValueChanged(Object oldpojObject, Object newpojObject) {
		Debuggable.notImplemented();
	}

	@Override protected boolean reloadObjectGUI(Object obj) throws Throwable {
		Debuggable.notImplemented();
		return false;
	}
	
	public static Class EDITTYPE = Connection.class;

	// Variables declaration - do not modify//GEN-BEGIN:variables
	// End of variables declaration//GEN-END:variables

}
