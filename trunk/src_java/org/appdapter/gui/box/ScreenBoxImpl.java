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

package org.appdapter.gui.box;

import org.appdapter.api.trigger.BoxImpl;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.gui.browse.DisplayContext;
import org.appdapter.gui.repo.DatabaseManagerPanel;
import org.appdapter.gui.repo.ModelMatrixPanel;
import org.appdapter.gui.repo.RepoManagerPanel;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**  Base implementation of our demo Swing Panel boxes. 
 * The default implementation can own one swing panel of each "Kind".
 * This owner does not actually create any kind of GUI resource until it is asked to
 * findBoxPanel(kind).  A strongheaded purpose-specific box might ignore "Kind",
 * and always return whatever panel it thinks is "best".  
 * <br/> 
 * @author Stu B. <www.texpedient.com>
 */
public class ScreenBoxImpl<TrigType extends Trigger<? extends ScreenBoxImpl<TrigType>>> extends BoxImpl<TrigType> implements ScreenBox<TrigType> {
	static Logger theLogger = LoggerFactory.getLogger(ScreenBoxImpl.class);
	// Because it's a "provider", we have an extra layer of indirection between box and display, enabling independence.
	private DisplayContextProvider			myDCP;
	

	// A box may have up to one panel for any kind.
	private	Map<ScreenBoxPanel.Kind, ScreenBoxPanel>	myPanelMap = new HashMap<ScreenBoxPanel.Kind, ScreenBoxPanel>();


	public void setDisplayContextProvider(DisplayContextProvider dcp) {
		myDCP = dcp;
	}

	
	public DisplayContext getDisplayContext() {
		if (myDCP != null) {
			return myDCP.findDisplayContext(this);
		}
		return null;
	}

	public void dump() {
		theLogger.info("DUMP-DUMP-DE-DUMP");
	}


	protected void putBoxPanel(ScreenBoxPanel.Kind kind, ScreenBoxPanel bp) {
		ScreenBoxPanel oldBP = getBoxPanel(kind);
		if (oldBP != null) {
			theLogger.warn("Replacing old ScreenBoxPanel link for " + getShortLabel() + " to {} with {} ", oldBP, bp);
		}
		myPanelMap.put(kind, bp);
	}
	protected ScreenBoxPanel getBoxPanel(ScreenBoxPanel.Kind kind) {
		return myPanelMap.get(kind);
	}
	/**
	 * The box panel returned might be one that we "made" earlier, 
	 * or one that we make right now,
	 * or it might be one that someone "put" onto me.
	 * @param kind
	 * @return 
	 */
	@Override public ScreenBoxPanel findBoxPanel(ScreenBoxPanel.Kind kind) {
		ScreenBoxPanel bp = getBoxPanel(kind);
		if (bp == null) {
			bp = makeBoxPanel(kind);
		}
		return bp;
	}
	/**
	 * This whole "kind" thing is a ruse allowing us to make some hardwired basic panel types
	 * without the conceptual bloat of yet another registry of named things.  The real generality
	 * comes when you override this ScreenBoxImpl class, and provide your own OTHER kind of panel.
	 * When these mechanisms mature, we will expand to a proper GUI component type registry.
	 * @param kind
	 * @return 
	 */
	protected ScreenBoxPanel makeBoxPanel(ScreenBoxPanel.Kind kind) {
		ScreenBoxPanel bp = null;
		switch(kind) {
		case MATRIX:
			bp = new ModelMatrixPanel();
		break;
		case REPO_MANAGER:
			bp = new RepoManagerPanel();
		break;
		case DB_MANAGER:
			bp = new DatabaseManagerPanel();
		break;
		case OTHER:
			bp = makeOtherPanel();
		break;
		default:
				throw new RuntimeException("Found unexpected ScreenBoxPanelKind: " + kind);
		}
		if (bp != null) {
			// Subclasses might do something fancier to share panels among instances.
			putBoxPanel(kind, bp);
		}
		return bp;
	}
	/** Override this to create an app-specific ScreenBoxPanel kind, and configure
	 * your app to request a panel of kind "OTHER", using BrowseTabFuncs.openBoxPanelAndFocus,
	 * PanelTriggers.OpenTrigger, or your own mechanism.  Note that your ScreenBoxPanel
	 * may be able to display any number of boxes, by responding to the focusOnBox method.
	 * If those boxes are screen boxes, you may want to tell them to 
	 * putBoxPanel() the one currently displaying them, in case they are later asked
	 * to findBoxPanel themselves.
	 * @return 
	 */
	protected ScreenBoxPanel makeOtherPanel() { 
		theLogger.warn("Default implementation of makeOtherPanel() for {} is returning null", getShortLabel());
		return null;
	}

}
