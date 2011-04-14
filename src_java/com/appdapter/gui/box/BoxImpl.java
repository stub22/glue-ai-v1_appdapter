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

package com.appdapter.gui.box;

import com.appdapter.gui.browse.DisplayContext;
import com.appdapter.gui.repo.DatabaseManagerPanel;
import com.appdapter.gui.repo.ModelMatrixPanel;
import com.appdapter.gui.repo.RepoManagerPanel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BoxImpl<TrigType extends Trigger<? extends BoxImpl<TrigType>>> extends KnownComponentImpl implements KnownComponent, MutableBox<TrigType>, ViewableBox<TrigType> {
	static Logger theLogger = LoggerFactory.getLogger(BoxImpl.class);
	// Because it's a "provider", we have an extra layer of indirection between box and display, enabling independence.
	private DisplayContextProvider			myDCP;
	
	private	BoxContext						myBoxContext;


	private List<TrigType>				myTriggers = new ArrayList<TrigType>();
	// A box may have up to one panel for any kind.
	private	Map<BoxPanel.Kind, BoxPanel>	myPanelMap = new HashMap<BoxPanel.Kind, BoxPanel>();


	public void setDisplayContextProvider(DisplayContextProvider dcp) {
		myDCP = dcp;
	}
	public void setContext(BoxContext bc) {
		myBoxContext = bc;
	}
	@Override public BoxContext getBoxContext() {
		return myBoxContext;
	}
	public void clearTriggers() {
		myTriggers.clear();
	}
	public void attachTrigger(TrigType trig) {
		myTriggers.add(trig);
	}
	@Override public List<TrigType> getTriggers() {
		return myTriggers;
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


	protected void putBoxPanel(BoxPanel.Kind kind, BoxPanel bp) {
		myPanelMap.put(kind, bp);
	}
	protected BoxPanel getBoxPanel(BoxPanel.Kind kind) {
		return myPanelMap.get(kind);
	}
	@Override public BoxPanel findBoxPanel(BoxPanel.Kind kind) {
		BoxPanel bp = getBoxPanel(kind);
		if (bp == null) {
			bp = makeBoxPanel(kind);
		}
		return bp;
	}
	protected BoxPanel makeBoxPanel(BoxPanel.Kind kind) {
		BoxPanel bp = null;
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

	@Override protected String getFieldSummary() {
		return "triggerCount=" + myTriggers.size();
	}


}
