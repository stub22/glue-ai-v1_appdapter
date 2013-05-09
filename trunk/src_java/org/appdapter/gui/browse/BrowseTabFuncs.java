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

import java.util.WeakHashMap;

import org.appdapter.api.trigger.BoxImpl;
import org.appdapter.gui.box.ObjectKey;
import org.appdapter.gui.box.ScreenBoxPanel;
import org.appdapter.gui.box.ScreenBox;
import javax.swing.JTabbedPane;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BrowseTabFuncs {

	static WeakHashMap<String, BoxImpl> boxKeyToImpl = new WeakHashMap<String, BoxImpl>();

	protected static boolean isBoxTabKnown_maybe(DisplayContext dc, BoxImpl nonPanel) {
		JTabbedPane tabbedPane = dc.getBoxPanelTabPane();
		String key = ObjectKey.factory.getKeyName(tabbedPane, nonPanel);
		return boxKeyToImpl.containsKey(key);
	}

	protected static boolean isBoxTabKnown(DisplayContext dc, String label) {
		JTabbedPane tabbedPane = dc.getBoxPanelTabPane();
		String key = ObjectKey.factory.getKeyName(tabbedPane, label);
		return boxKeyToImpl.containsKey(key);
	}

	protected static boolean isBoxTabKnown_maybe_not(DisplayContext dc, ScreenBoxPanel bp) {
		JTabbedPane tabbedPane = dc.getBoxPanelTabPane();
		return ((tabbedPane.indexOfComponent(bp) >= 0) ? true : false);
	}

	protected static void setSelectedBoxTab(DisplayContext dc,
			ScreenBoxPanel boxP) {
		JTabbedPane tabbedPane = dc.getBoxPanelTabPane();
		tabbedPane.setSelectedComponent(boxP);
	}

	protected static void addBoxTab(DisplayContext dc, ScreenBoxPanel boxP,
			String label) {
		JTabbedPane tabbedPane = dc.getBoxPanelTabPane();
		tabbedPane.add(label, boxP);
	}

	/**
	 * It's OK to call this repeatedly for the same boxI.
	 * 
	 * @param dc
	 * @param boxI
	 *            - We rely on this boxI to "find" the right panel, and return
	 *            the same panel if it's been opened before.
	 *            
	 *       But! we can only have one panel of each kind per box
	 * @param kind
	 */
	public static void openBoxPanelAndFocus(DisplayContext dc, ScreenBox boxI,
			ScreenBoxPanel.Kind kind) {

		ScreenBoxPanel boxP = boxI.findBoxPanel(kind);
		String tabLabel = kind.toString() + "-" + boxI.getShortLabel();
		if (!isBoxTabKnown(dc, tabLabel)) {
			addBoxTab(dc, boxP, tabLabel);
		}
		setSelectedBoxTab(dc, boxP);
		boxP.focusOnBox(boxI);
	}

}
