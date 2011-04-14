/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.com).
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

package com.appdapter.gui.trigger;

import com.appdapter.gui.box.Box;
import com.appdapter.gui.box.BoxPanel;
import com.appdapter.gui.box.TriggerImpl;
import com.appdapter.gui.box.ViewableBox;
import com.appdapter.gui.browse.BrowseTabs;
import com.appdapter.gui.browse.DisplayContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class PanelTriggers {
	static Logger theLogger = LoggerFactory.getLogger(PanelTriggers.class);
	public enum Kind {
		OPEN,
		CLOSE
	}
	public static class OpenTrigger <VB extends ViewableBox<TriggerImpl<VB>>> extends  TriggerImpl<VB> {
		private BoxPanel.Kind	myPanelKind;

		public void setPanelKind(BoxPanel.Kind kind) {
			myPanelKind = kind;
		}
		@Override public void fire(VB targetVB) {
			theLogger.info(toString() + "-firing, opening box panel");
			DisplayContext dc = targetVB.getDisplayContext();
			BrowseTabs.openBoxPanelAndFocus(dc, targetVB, myPanelKind);
		}
	}
	public static class CloseTrigger  <VB extends ViewableBox<TriggerImpl<VB>>> extends  TriggerImpl<VB> {
		@Override public void fire(VB targetBox) {
			theLogger.info(toString() + "-closing viewableBox: " + targetBox);	
		}
	}
}
