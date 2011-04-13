/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author winston
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
