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
	public static class OpenTrigger extends TriggerImpl {
		private BoxPanel.Kind	myPanelKind;

		public void setPanelKind(BoxPanel.Kind kind) {
			myPanelKind = kind;
		}
		@Override public void fire(Box targetBox) {
			theLogger.info(toString() + "-firing, opening box panel");
			ViewableBox  vb = (ViewableBox) targetBox;
			DisplayContext dc = vb.getDisplayContext();
			BrowseTabs.openBoxPanelAndFocus(dc, vb, myPanelKind);
		}
	}
	public static class CloseTrigger extends TriggerImpl {
		@Override public void fire(Box targetBox) {
			theLogger.info(toString() + "-closing");
			
		}
	}
}
