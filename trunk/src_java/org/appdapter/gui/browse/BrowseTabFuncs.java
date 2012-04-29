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

package org.appdapter.gui.browse;

import org.appdapter.gui.box.ScreenBoxPanel;
import org.appdapter.gui.box.ScreenBox;
import javax.swing.JTabbedPane;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BrowseTabFuncs {
	protected static boolean isBoxTabKnown(DisplayContext dc, ScreenBoxPanel bp) {
		JTabbedPane tabbedPane = dc.getBoxPanelTabPane();
		return ((tabbedPane.indexOfComponent(bp) >= 0) ? true : false);
	}
	protected static void setSelectedBoxTab(DisplayContext dc, ScreenBoxPanel boxP) {
		JTabbedPane tabbedPane = dc.getBoxPanelTabPane();
		tabbedPane.setSelectedComponent(boxP);
	}	
	protected static void addBoxTab(DisplayContext dc, ScreenBoxPanel boxP, String label) {
		JTabbedPane tabbedPane = dc.getBoxPanelTabPane();
		tabbedPane.add(label, boxP);
	}
	public static void openBoxPanelAndFocus(DisplayContext dc, ScreenBox boxI, ScreenBoxPanel.Kind kind) {
		ScreenBoxPanel boxP = boxI.findBoxPanel(kind);
		if (!isBoxTabKnown(dc, boxP)) {
			String tabLabel = kind.toString() + "-" + boxI.getShortLabel();
			addBoxTab(dc, boxP, tabLabel);
		}
		setSelectedBoxTab(dc, boxP);
		boxP.focusOnBox(boxI);
	}

}
