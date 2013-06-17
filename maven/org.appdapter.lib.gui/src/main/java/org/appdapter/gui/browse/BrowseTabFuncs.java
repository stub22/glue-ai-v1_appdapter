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

import javax.swing.JPanel;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxImpl;
import org.appdapter.api.trigger.BoxPanelSwitchableView;
import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.api.trigger.DisplayType;
import org.appdapter.api.trigger.ObjectKey;
import org.appdapter.api.trigger.ScreenBox;
import org.appdapter.api.trigger.ScreenBox.Kind;
import org.appdapter.gui.api.Utility;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BrowseTabFuncs {

	static WeakHashMap<String, BoxImpl> boxKeyToImpl = new WeakHashMap<String, BoxImpl>();

	protected static boolean isBoxTabKnown_maybe(DisplayContext dc, BoxImpl nonPanel) {
		BoxPanelSwitchableView tabbedPane = dc.getBoxPanelTabPane();
		String key = ObjectKey.factory.getKeyName(tabbedPane, nonPanel);
		return boxKeyToImpl.containsKey(key);
	}

	protected static boolean isBoxTabKnown(DisplayContext dc, String label) {
		BoxPanelSwitchableView tabbedPane = dc.getBoxPanelTabPane();
		String key = ObjectKey.factory.getKeyName(tabbedPane, label);
		return boxKeyToImpl.containsKey(key);
	}

	protected static boolean isBoxTabKnown_maybe_not(DisplayContext dc, JPanel bp) {
		BoxPanelSwitchableView tabbedPane = dc.getBoxPanelTabPane();
		if (tabbedPane.containsComponent(bp)) {
			Utility.theLogger.warn("gettign subcomponent!" + bp);
			return true;
		}
		return false;
	}

	protected static void setSelectedBoxTab(DisplayContext dc, JPanel boxP) {
		BoxPanelSwitchableView tabbedPane = dc.getBoxPanelTabPane();
		tabbedPane.setSelectedComponent(boxP);
	}

	protected static void addBoxTab(DisplayContext dc, JPanel boxP, String label) {
		BoxPanelSwitchableView tabbedPane = dc.getBoxPanelTabPane();
		tabbedPane.addComponent(label, boxP, DisplayType.PANEL);
		if (!tabbedPane.containsComponent(boxP)) {
			tabbedPane.addComponent(label, boxP, DisplayType.PANEL);
			if (!tabbedPane.containsComponent(boxP)) {
				throw new RuntimeException("Cant add " + boxP + " to " + dc);
			}
		}
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
	public static void openBoxPanelAndFocus(DisplayContext dc, ScreenBox boxI, Kind kind) {

		JPanel boxP = boxI.findOrCreateBoxPanel(kind);
		if (Utility.selectedDisplaySontext == null) {
			Utility.selectedDisplaySontext = dc;
		}
		String tabLabel = kind.toString() + "-" + boxI.getShortLabel();
		if (!isBoxTabKnown(dc, tabLabel)) {
			addBoxTab(dc, boxP, tabLabel);
		}
		setSelectedBoxTab(dc, boxP);
		focusOnPanelBox(boxP, boxI);
	}

	public static void focusOnPanelBox(JPanel boxP, Box boxI) {
		return;
	}

}
