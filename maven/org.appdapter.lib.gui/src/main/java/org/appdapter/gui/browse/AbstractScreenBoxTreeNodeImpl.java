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

import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.print.attribute.standard.Fidelity;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.appdapter.api.trigger.BT;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxPanelSwitchableView;
import org.appdapter.api.trigger.BrowserPanelGUI;
import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.api.trigger.DisplayContextProvider;
import org.appdapter.api.trigger.NamedObjectCollection;
import org.appdapter.api.trigger.ScreenBoxTreeNode;
import org.appdapter.api.trigger.UIProvider;
import org.appdapter.api.trigger.UserResult;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.GetSetObject;
import org.appdapter.gui.api.Utility;

/**
 * @author Stu B. <www.texpedient.com>
 */
abstract public class AbstractScreenBoxTreeNodeImpl extends DefaultMutableTreeNode implements GetSetObject, UIProvider, DisplayContextProvider, DisplayContext {
	protected DisplayContext myDisplayContext;
	public BoxPanelSwitchableView bsv;

	public BrowserPanelGUI getLocalTreeAPI() {
		Debuggable.notImplemented();
		return null;
	}

	public AbstractScreenBoxTreeNodeImpl(Box box, boolean allowsChildren0) {
		super(box, allowsChildren0);
	}

	final public DisplayContext getDisplayContext() {
		DisplayContext foundDC = myDisplayContext;
		if (foundDC == null) {

			ScreenBoxTreeNode parentNode = getScreenBoxTreeParent();
			if (parentNode != null) {
				foundDC = parentNode.getDisplayContext();
			}
			if (foundDC == null && bsv != null) {
				//foundDC = Utility. bsv.getDisplayContext();
			}
		}
		return foundDC;
	}

	/* (non-Javadoc)
	 * @see org.appdapter.gui.box.DisplayNode#getBox()
	 */
	final public Box getBox() {
		return (Box) getUserObject();
	}

	public abstract Iterable<BT> getTreeRepresentedChildren();

	public abstract Iterable<Object> getCollectionUnboxed();

	public abstract Object getTreeRepresentedObject();

	/* (non-Javadoc)
	 * @see org.appdapter.gui.box.DisplayNode#findDescendantNodeForBox(org.appdapter.api.trigger.Box)
	 */
	final public AbstractScreenBoxTreeNodeImpl findTreeNodeDisplayContext(Box b) {

		if (b == getUserObject()) {
			return this;
		} else {
			int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				AbstractScreenBoxTreeNodeImpl childNode = (AbstractScreenBoxTreeNodeImpl) getChildAt(i);
				AbstractScreenBoxTreeNodeImpl matchNode = childNode.findTreeNodeDisplayContext(b);
				if (matchNode != null) {
					return matchNode;
				}
			}
		}
		if (b instanceof AbstractScreenBoxTreeNodeImpl) {
			return (AbstractScreenBoxTreeNodeImpl) b;
		}
		return null;
	}

	final @Override public void add(MutableTreeNode childNode) {
		super.add((MutableTreeNode) childNode);

	}

	final public ScreenBoxTreeNodeImpl getScreenBoxTreeParent() {
		Object parent = getParent();
		if (parent == null || parent instanceof ScreenBoxTreeNode)
			return (ScreenBoxTreeNodeImpl) parent;
		return null;
	}

	final public void setDisplayContext(DisplayContext dc) {
		myDisplayContext = dc;
	}

	public ScreenBoxTreeNode findDescendantNodeForBox(Box b) {
		Debuggable.notImplemented();
		return (ScreenBoxTreeNode) findTreeNodeDisplayContext(b);
	}

	protected DisplayContext getDisplayContextNoLoop() {
		Debuggable.notImplemented();
		return Utility.getCurrentContext();
	}

	/*
		@Override public Component getComponent() {
			DisplayContext displayContext = getDisplayContextNoLoop();
			return null;//displayContext.getComponent();
		}*/

	@Override public NamedObjectCollection getLocalBoxedChildren() {
		return Utility.getTreeBoxCollection();
	}

	@Override public Collection getTriggersFromUI(BT box, Object object) {
		DisplayContext displayContext = getDisplayContextNoLoop();
		return displayContext.getTriggersFromUI(box, object);
	}

	/*
		@Override public UserResult showScreenBox(Object child) throws Exception {
			DisplayContext displayContext = getDisplayContextNoLoop();
			return displayContext.showScreenBox(child);
		}

		@Override public UserResult showError(String msg, Throwable error) {
			DisplayContext displayContext = getDisplayContextNoLoop();
			return displayContext.showError(msg, error);
		}

		@Override public UserResult showMessage(String msg) {
			DisplayContext displayContext = getDisplayContextNoLoop();
			return displayContext.showMessage(msg);

		}
	*/
	@Override public UserResult attachChildUI(String title, Object value, boolean showASAP) throws Exception {
		return Utility.asUserResult(attachChildObect(title, value));
	}

	public AbstractScreenBoxTreeNodeImpl detachChildObect(String title, Object value) {
		AbstractScreenBoxTreeNodeImpl before = findChildObject(title, value);
		if (before != null) {
			remove(before);
		}
		return before;
	}

	public AbstractScreenBoxTreeNodeImpl attachChildObect(String title, Object value) {
		AbstractScreenBoxTreeNodeImpl before = findChildObject(title, value);
		if (before != null)
			return before;
		NamedObjectCollection col = getLocalBoxedChildren();
		BT b;
		try {
			b = col.findOrCreateBox(title, value);
			ScreenBoxTreeNodeImpl newNode = new ScreenBoxTreeNodeImpl(bsv, b.asBox(), true);
			add(newNode);
			return newNode;
		} catch (PropertyVetoException e) {
			throw Debuggable.reThrowable(e);
		}
	}

	public AbstractScreenBoxTreeNodeImpl findChildObject(String title, Object value) {
		NamedObjectCollection col = getLocalBoxedChildren();
		BT b1 = null, b2 = null;
		if (title != null) {
			b1 = col.findBoxByName(title);
		}
		if (value != null) {
			b2 = col.findBoxByObject(value);
		}
		if (b1 == null && b2 == null)
			return null;
		if (b2 == null) {
			b2 = b1;
		}
		AbstractScreenBoxTreeNodeImpl before = findTreeNodeDisplayContext(b2.asBox());
		return before;
	}

	@Override public String getTitleOf(Object value) {
		return getLocalBoxedChildren().getTitleOf(value);
	}

}