package org.appdapter.gui.box;

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

import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.List;

import javax.swing.JPanel;

import org.appdapter.api.trigger.AnyOper.UIHidden;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxContext;
import org.appdapter.api.trigger.BrowserPanelGUI;
import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.api.trigger.DisplayContextProvider;
import org.appdapter.api.trigger.DisplayType;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.api.trigger.NamedObjectCollection;
import org.appdapter.api.trigger.POJOBoxImpl;
import org.appdapter.api.trigger.ScreenBox;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.api.trigger.UserResult;
import org.appdapter.core.component.MutableKnownComponent;
import org.appdapter.gui.api.GetSetObject;
import org.appdapter.gui.api.Utility;
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

@UIHidden
public class ScreenBoxImpl<TrigType extends Trigger<? extends ScreenBoxImpl<TrigType>>> extends POJOBoxImpl<TrigType>

implements ScreenBox<TrigType>, GetSetObject, UserResult {

	public static List<ScreenBox> boxctxGetOpenChildBoxesNarrowed(BoxContext oh, Object parent, Class boxClass, Class trigClass) {
		return oh.getOpenChildBoxesNarrowed((Box) parent, boxClass, trigClass);
	}

	public static void doAttachTrigger(Object box, Object bt) {
		((MutableBox) box).attachTrigger((Trigger) bt);
	}

	public static void doSetShortLabel(Object box, Object nym) {
		((MutableKnownComponent) box).setShortLabel((String) nym);
	}

	static Logger theLogger = LoggerFactory.getLogger(ScreenBoxImpl.class);
	// Because it's a "provider", we have an extra layer of indirection between
	// box and display, enabling independence.
	private DisplayContextProvider myDCP;

	public ScreenBoxImpl() {
		super();
	}

	public ScreenBoxImpl(NamedObjectCollection noc, String label, Object obj) {
		super(noc, label, obj);
	}

	public ScreenBoxImpl(String label, Object obj) {
		this(Utility.getToplevelBoxCollection(), label, obj);
	}

	/*
		public ScreenBoxImpl(NamedObjectCollection noc, String title, Object boxOrObj, Component vis, DisplayType displayType, Container parent, BoxPanelSwitchableView bpsv) {
			super(noc, title, boxOrObj, vis, displayType, parent, bpsv);
		}
	*/
	/**
	 * The box panel returned might be one that we "made" earlier, 
	 * or it might be one that someone "put" onto me.
	 * @param kind
	 * @return 
	 */
	@Override public JPanel findOrCreateBoxPanel(Object kind) {
		JPanel bp = findExistingBoxPanel(kind);
		if (bp == null) {
			bp = makeBoxPanelCustomized(kind);
		}
		return bp;
	}

	public void dump() {
		theLogger.info("DUMP-DUMP-DE-DUMP");
	}

	public JPanel findExistingBoxPanel(Kind kind) {
		return myPanelMap.get(toKey(kind));
	}

	public JPanel findExistingBoxPanel(Object kind) {
		return myPanelMap.get(toKey(kind));
	}

	@Override public DisplayContext getDisplayContext() {
		if (myDCP != null) {
			DisplayContext dc = myDCP.findDisplayContext(this);
			if (dc != null)
				return dc;
		}
		return Utility.browserPanel.getDisplayContext();
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
	protected JPanel makeBoxPanel(Kind kind) {
		JPanel bp = makeBoxPanelForKind(kind);
		if (bp != null) {
			// Subclasses might do something fancier to share panels among
			// instances.
			putBoxPanel(kind, bp);
		}
		return bp;
	}

	protected JPanel makeBoxPanelCustomized(Object customizer) {
		JPanel bp = makeBoxPanelForCustomizer(customizer);
		if (bp == null) {
			bp = makeOtherPanel();
		}
		if (bp != null) {
			// Subclasses might do something fancier to share panels among
			// instances.
			putBoxPanel(customizer, bp);
		}
		return bp;
	}

	public JPanel makeBoxPanelForCustomizer(Object customizer) {
		if (customizer instanceof Kind) {
			JPanel sbp = makeBoxPanel((Kind) customizer);
			if (sbp != null)
				return sbp;
		}
		return super.makeBoxPanelForCustomizer(customizer);
	}

	protected JPanel makeBoxPanelForKind(Kind kind) {
		if (kind == Kind.MATRIX)
			return new ModelMatrixPanel();
		if (kind == Kind.REPO_MANAGER)
			return new RepoManagerPanel();
		if (kind == Kind.DB_MANAGER)
			return new DatabaseManagerPanel();
		if (kind == Kind.OBJECT_PROPERTIES)
			return Utility.getPropertiesPanel(this);
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
	protected JPanel makeOtherPanel() {
		//theLogger.warn("Default implementation of makeOtherPanel() for {} is returning null", getShortLabel());
		return super.getPropertiesPanel();// Utility.getPropertiesPanel(this);
	}

	protected void putBoxPanel(Object kind, JPanel bp) {
		JPanel oldBP = findExistingBoxPanel(kind);
		if (oldBP != null) {
			//theLogger.warn("Replacing old ScreenBoxPanel link for " + getShortLabel() + " to {} with {} ", oldBP, bp);
		}
		myPanelMap.put(toKey(kind), bp);
	}

	public void setDisplayContextProvider(DisplayContextProvider dcp) {
		myDCP = dcp;
	}

	public DisplayType getDisplayType() {
		return m_displayType;
	}

	public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
		vetoSupport.fireVetoableChange(evt);

	}

	public static UserResult asResult(JPanel pnl) {
		return UserResult.SUCCESS;
	}
}
