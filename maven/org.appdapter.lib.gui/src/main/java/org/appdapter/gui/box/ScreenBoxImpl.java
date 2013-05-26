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

import java.beans.PropertyVetoException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.api.trigger.DisplayContextProvider;
import org.appdapter.api.trigger.ScreenBox;
import org.appdapter.api.trigger.ScreenBoxPanel;
import org.appdapter.api.trigger.ScreenBoxPanel.Kind;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.gui.pojo.POJOBox;
import org.appdapter.gui.pojo.Utility;
import org.appdapter.gui.repo.DatabaseManagerPanel;
import org.appdapter.gui.repo.ModelMatrixPanel;
import org.appdapter.gui.repo.RepoManagerPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
/**  Base implementation of our demo Swing Panel boxes. 
 * The default implementation can own one swing panel of each "Kind".
 * This owner does not actually create any kind of GUI resource until it is asked to
 * findBoxPanel(kind).  A strongheaded purpose-specific box might ignore "Kind",
 * and always return whatever panel it thinks is "best".  
 * <br/> 
 * @author Stu B. <www.texpedient.com>
 */
public class ScreenBoxImpl<TrigType extends Trigger<? extends ScreenBoxImpl<TrigType>>>

extends POJOBox<TrigType> implements ScreenBox<TrigType> {

	static Logger theLogger = LoggerFactory.getLogger(ScreenBoxImpl.class);
	// Because it's a "provider", we have an extra layer of indirection between
	// box and display, enabling independence.
	private DisplayContextProvider myDCP;

	// A box may have up to one panel for any kind.
	private Map<ScreenBoxPanel.Kind, ScreenBoxPanel> myPanelMap = new HashMap<ScreenBoxPanel.Kind, ScreenBoxPanel>();

	private Object object;

	@Override public void setObject(Object obj) {
		object = obj;
		String ds = getDescription();
		if (ds == null) {
			setDescription("" + obj + " " + obj.getClass());
		}
	}

	public ScreenBoxImpl() {
		object = this;
	}

	public ScreenBoxImpl(Object object) {
		setObject(object);
	}

	public ScreenBoxImpl(String uniqueName, Object obj) throws PropertyVetoException {
		setName(uniqueName);
		setObject(obj);
	}

	@Override public Object getObject() {
		if (object == null) {
			theLogger.warn("Default implementation of getObject() for NULL is returning 'this'", getShortLabel());
			return this;
		}
		if (object != this)
			return object;
		theLogger.warn("Default implementation of getObject() for {} is returning 'this'", getShortLabel());
		return this;
	}

	@Override public Class<? extends Object> getPOJOClass() {
		return super.getPOJOClass();
	}

	@Override public List<Class> getTypes() {
		java.util.HashSet al = new java.util.HashSet<Class>();
		al.add(getPOJOClass());
		al.add(getClass());
		return new ArrayList<Class>(al);
	}

	@Override public void setDisplayContextProvider(DisplayContextProvider dcp) {
		myDCP = dcp;
	}

	@Override public DisplayContext getDisplayContext() {
		if (myDCP != null) {
			return myDCP.findDisplayContext(this);
		}
		return Utility.mainDisplayContext;
	}

	/**
	 * The box panel returned might be one that we "made" earlier, 
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
	 * This whole "kind" thing is a ruse allowing us to make some hardwired basic panel types
	 * without the conceptual bloat of yet another registry of named things.  The real generality
	 * named things. The real generality comes when you override this
	 * comes when you override this ScreenBoxImpl class, and provide your own OTHER kind of panel.
	 * When these mechanisms mature, we will expand to a proper GUI component type registry.
	 * @param kind
	 * @return 
	 */
	protected ScreenBoxPanel makeBoxPanel(ScreenBoxPanel.Kind kind) {
		ScreenBoxPanel bp = makeBoxPanelForKind(kind);
		if (bp != null) {
			// Subclasses might do something fancier to share panels among
			// instances.
			putBoxPanel(kind, bp);
		}
		return bp;
	}

	protected ScreenBoxPanel makeBoxPanelForKind(Kind kind) {
		if (kind == Kind.MATRIX)
			return new ModelMatrixPanel();
		if (kind == Kind.REPO_MANAGER)
			return new RepoManagerPanel();
		if (kind == Kind.DB_MANAGER)
			return new DatabaseManagerPanel();
		if (kind == Kind.OTHER)
			return makeOtherPanel();
		throw new RuntimeException("Found unexpected ScreenBoxPanelKind: " + kind);
	}

	/**
	/** Override this to create an app-specific ScreenBoxPanel kind, and configure
	 * your app to request a panel of kind "OTHER", using BrowseTabFuncs.openBoxPanelAndFocus,
	 * BrowseTabFuncs.openBoxPanelAndFocus, PanelTriggers.OpenTrigger, or your
	 * PanelTriggers.OpenTrigger, or your own mechanism.  Note that your ScreenBoxPanel
	 * may be able to display any number of boxes, by responding to the focusOnBox method.
	 * If those boxes are screen boxes, you may want to tell them to 
	 * putBoxPanel() the one currently displaying them, in case they are later asked
	 * to findBoxPanel themselves.
	 * 
	 * @return
	 */
	protected ScreenBoxPanel makeOtherPanel() {
		theLogger.warn("Default implementation of makeOtherPanel() for {} is returning null", getShortLabel());
		return null;
	}

	public void dump() {
		theLogger.info("DUMP-DUMP-DE-DUMP");
	}

	@Override public <T> T[] getObjects(Class<T> type) {
		HashSet<Object> objs = new HashSet<Object>();
		if (this.canConvert(type)) {
			T one = convertTo(type);
			objs.add(one);
		}
		for (Object o : getObjects()) {
			if (type.isInstance(o)) {
				objs.add(o);
			}
		}
		return objs.toArray((T[]) Array.newInstance(type, objs.size()));
	}
}
