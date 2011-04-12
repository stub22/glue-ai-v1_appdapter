/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.gui.box;

import javax.swing.JPanel;

/**
 * BoxPanels may be used to view many different boxes.
 * 
 * @author winston
 */
public abstract class BoxPanel<BoxType extends Box> extends JPanel {
	public enum Kind {
		MATRIX,
		DB_MANAGER,
		REPO_MANAGER,
		OTHER
	}
	public abstract void focusOnBox(BoxType b);
}
