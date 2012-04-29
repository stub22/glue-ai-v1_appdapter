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

import org.appdapter.api.trigger.BoxContext;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.api.trigger.BoxImpl;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.core.component.KnownComponent;
import org.appdapter.core.component.KnownComponentImpl;
import org.appdapter.gui.browse.DisplayContext;
import org.appdapter.gui.repo.DatabaseManagerPanel;
import org.appdapter.gui.repo.ModelMatrixPanel;
import org.appdapter.gui.repo.RepoManagerPanel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**  Base implementation of our demo Swing Panel boxes.  
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
		myPanelMap.put(kind, bp);
	}
	protected ScreenBoxPanel getBoxPanel(ScreenBoxPanel.Kind kind) {
		return myPanelMap.get(kind);
	}
	@Override public ScreenBoxPanel findBoxPanel(ScreenBoxPanel.Kind kind) {
		ScreenBoxPanel bp = getBoxPanel(kind);
		if (bp == null) {
			bp = makeBoxPanel(kind);
		}
		return bp;
	}
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
		}
		if (bp != null) {
			// Subclasses might do something fancier to share panels among instances.
			putBoxPanel(kind, bp);
		}
		return bp;
	}




}
