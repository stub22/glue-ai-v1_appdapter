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

package org.appdapter.gui.trigger;

import org.appdapter.gui.box.ScreenBoxPanel;
import org.appdapter.api.trigger.TriggerImpl;
import org.appdapter.gui.box.ScreenBox;
import org.appdapter.gui.browse.BrowseTabFuncs;
import org.appdapter.gui.browse.DisplayContext;
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
	public static class OpenTrigger <VB extends ScreenBox<TriggerImpl<VB>>> extends  TriggerImpl<VB> {
		private ScreenBoxPanel.Kind	myPanelKind;

		public void setPanelKind(ScreenBoxPanel.Kind kind) {
			myPanelKind = kind;
		}
		@Override public void fire(VB targetVB) {
			theLogger.info(toString() + "-firing, opening box panel");
			DisplayContext dc = targetVB.getDisplayContext();
			BrowseTabFuncs.openBoxPanelAndFocus(dc, targetVB, myPanelKind);
		}
	}
	public static class CloseTrigger  <VB extends ScreenBox<TriggerImpl<VB>>> extends  TriggerImpl<VB> {
		@Override public void fire(VB targetBox) {
			theLogger.info(toString() + "-closing viewableBox: " + targetBox);	
		}
	}
}
