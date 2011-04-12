/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.gui.browse;

import com.appdapter.gui.box.BoxPanel;
import com.appdapter.gui.box.ViewableBox;
import javax.swing.JTabbedPane;

/**
 *
 * @author winston
 */
public class BrowseTabs {
	protected static boolean isBoxTabKnown(DisplayContext dc, BoxPanel bp) {
		JTabbedPane tabbedPane = dc.getBoxPanelTabPane();
		return ((tabbedPane.indexOfComponent(bp) >= 0) ? true : false);
	}
	protected static void setSelectedBoxTab(DisplayContext dc, BoxPanel boxP) {
		JTabbedPane tabbedPane = dc.getBoxPanelTabPane();
		tabbedPane.setSelectedComponent(boxP);
	}	
	protected static void addBoxTab(DisplayContext dc, BoxPanel boxP, String label) {
		JTabbedPane tabbedPane = dc.getBoxPanelTabPane();
		tabbedPane.add(label, boxP);
	}
	public static void openBoxPanelAndFocus(DisplayContext dc, ViewableBox boxI, BoxPanel.Kind kind) {
		BoxPanel boxP = boxI.findBoxPanel(kind);
		if (!isBoxTabKnown(dc, boxP)) {
			String tabLabel = kind.toString() + "-" + boxI.getShortLabel();
			addBoxTab(dc, boxP, tabLabel);
		}
		setSelectedBoxTab(dc, boxP);
		boxP.focusOnBox(boxI);
	}

}
