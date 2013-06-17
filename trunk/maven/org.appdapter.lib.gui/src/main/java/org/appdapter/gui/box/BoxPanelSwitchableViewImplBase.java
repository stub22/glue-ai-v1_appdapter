package org.appdapter.gui.box;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.IntrospectionException;
import java.beans.PropertyEditor;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.tree.TreePath;

import org.appdapter.api.trigger.AppGUIWithTabsAndTrees;
import org.appdapter.api.trigger.BT;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxPanelSwitchableView;
import org.appdapter.api.trigger.BrowserPanelGUI;
import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.api.trigger.DisplayType;
import org.appdapter.api.trigger.GetObject;
import org.appdapter.api.trigger.NamedObjectCollection;
import org.appdapter.api.trigger.ScreenBox;
import org.appdapter.api.trigger.UserResult;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.ComponentHost;
import org.appdapter.gui.api.GetSetObject;
import org.appdapter.gui.api.PairTable;
import org.appdapter.gui.api.Utility;
import org.appdapter.gui.browse.BrowsePanel;
import org.appdapter.gui.rimpl.TriggerMenuFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
abstract public class BoxPanelSwitchableViewImplBase

implements BoxPanelSwitchableView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Window event adapter class, used to find out when child windows close
	 */
	class Adapter extends WindowAdapter implements InternalFrameListener {
		public void internalFrameActivated(InternalFrameEvent e) {
		}

		public void internalFrameClosed(InternalFrameEvent e) {
		}

		public void internalFrameClosing(InternalFrameEvent e) {
			Object source = e.getSource();
			if (source == classBrowser_Unused) {
				((JInternalFrame) classBrowser_Unused).removeInternalFrameListener(this);
				classBrowser_Unused = null;
			} else if (source instanceof JInternalFrame) {
				JInternalFrame window = (JInternalFrame) source;
				window.removeInternalFrameListener(this);
				objectFrames.remove(window);
				objectGUIs.remove(window);
				window.dispose();
			}
		}

		public void internalFrameDeactivated(InternalFrameEvent e) {
		}

		public void internalFrameDeiconified(InternalFrameEvent e) {
		}

		public void internalFrameIconified(InternalFrameEvent e) {
		}

		public void internalFrameOpened(InternalFrameEvent e) {
		}

		public void windowClosing(WindowEvent e) {
			Object source = e.getSource();
			if (source == classBrowser_Unused) {
				// classBrowser.removeWindowListener(this);
				// classBrowser = null;
			} else if (source instanceof Window) {
				Window window = (Window) source;
				window.removeWindowListener(this);
				objectFrames.remove(window);
				objectGUIs.remove(window);
				window.dispose();
			}
		}
	};

	private static boolean ALLOW_MULTIPLE_WINDOWS = false;;

	private static Logger theLogger = LoggerFactory.getLogger(BoxPanelSwitchableViewImpl.class);

	Object classBrowser_Unused = null;

	WeakHashMap<Object, ScreenBoxImpl> indexedPairs = new WeakHashMap<Object, ScreenBoxImpl>();

	Adapter listener = new Adapter();

	PairTable<Object, JComponent> objectFrames = new PairTable();

	//private Component selected;

	PairTable objectGUIs = new PairTable();

	NamedObjectCollection savedInPOJOColection;

	public BoxPanelSwitchableViewImplBase(NamedObjectCollection savedInPOJOCol) {
		this.savedInPOJOColection = savedInPOJOCol;
	}

	public ScreenBoxImpl addComponent(String title, Component view, DisplayType attachType) {
		ScreenBoxImpl pair = findBoxByComponent(view, attachType);
		if (pair != null && pair.m_is_added)
			return pair;
		if (title == null) {
			title = savedInPOJOColection.getTitleOf(view);
		}
		pair = findBoxByName(title, attachType);
		if (pair == null) {
			Object anyObject = findObjectByComponent(view);
			NamedObjectCollection noc = getNamedObjectCollectionForDisplayType(attachType);
			Container holder = guessContainerByComponent(view, attachType, true);
			pair = new ScreenBoxImpl(noc, title, anyObject);//, view, attachType, holder, this);
			registerPair(pair, true);
			return pair;
		}
		registerPair(title, pair);
		return pair;
	}

	public ScreenBoxImpl addObject(String title, Object anyObject, DisplayType attachType, boolean showASAP) {

		ScreenBoxImpl box = findBoxByObject(anyObject, attachType);
		if (box == null) {
			box = findBoxByName(title, attachType);
			if (box != null) {
				Object fnd = findObjectByComponent(box.m_view);
				if (fnd != box.getObject()) {
					box = null;
				}
			}
		}
		if (box == null) {
			Container holder = getContainer(attachType);
			Component view = findComponentByObject(anyObject, attachType);
			NamedObjectCollection noc = getNamedObjectCollectionForDisplayType(attachType);
			if (view == null) {
				if (title == null)
					title = Utility.generateUniqueName(anyObject, noc.getNameToBoxIndex());
				view = createComponentByObject(noc, title, anyObject, attachType, showASAP);
			}
			if (title == null)
				title = TriggerMenuFactory.getLabel(view, 1);
			box = new ScreenBoxImpl(noc, title, anyObject);//, view, attachType, holder, this);
			registerPair(box, showASAP);
		}
		if (showASAP)
			setSelectedComponent(box.m_view);
		return box;
	}

	private NamedObjectCollection getNamedObjectCollectionForDisplayType(DisplayType attachType) {
		if (savedInPOJOColection != null)
			return savedInPOJOColection;
		return Utility.uiObjects;
	}

	public boolean containsComponent(Component view) {
		ScreenBoxImpl pair = findBoxByComponent(view, DisplayType.ANY);
		return pair != null;
	}

	public boolean containsObject(Object box, DisplayType attachType) {
		ScreenBoxImpl pair = findBoxByObject(box, attachType);
		return pair != null;
	}

	private Component createComponentByObject(NamedObjectCollection noc, String title, Object anyObject, DisplayType attachType, boolean showASAP) {
		if (anyObject instanceof ScreenBox) {
			ScreenBox sb = (ScreenBox) anyObject;
			anyObject = sb.getComponent();
		}
		if (anyObject instanceof Component) {
			Component c = (Component) anyObject;
			DisplayType maybe = getDisplayType(c);
			if (attachType == maybe) {
				return c;
			}
			if (maybe == DisplayType.PANEL || maybe == DisplayType.FRAME) {
				return ComponentHost.asPanel(c, anyObject);
			}
		}

		Component boxo = Utility.getPropertiesPanel(anyObject);
		if (boxo == null) {
			Debuggable.warn("Null POJO");
			boxo = Utility.getPropertiesPanel(anyObject);
		}
		return boxo;
	}

	private ScreenBoxImpl findBoxByComponent(Component view, DisplayType attachType) {
		Object laterWillBeInterface = view;
		if (laterWillBeInterface instanceof ScreenBoxImpl) {
			return (ScreenBoxImpl) laterWillBeInterface;
		}

		ScreenBoxImpl pair = indexedPairs.get(view);
		if (pair != null) {
			return pair;
		}

		Container holder = guessContainerByComponent(view, attachType, false);
		if (holder != null) {
			Object box = findObjectByComponent(view);
			String title = TriggerMenuFactory.getLabel(view, 1);
			NamedObjectCollection noc = getNamedObjectCollectionForDisplayType(attachType);
			ScreenBoxImpl npair = new ScreenBoxImpl(noc, title, box);//, view, attachType, holder, this);
			registerPair(npair, false);
			return npair;
		}
		return null;
	}

	private ScreenBoxImpl findBoxByName(String title) {
		return findBoxByName(title, DisplayType.ANY);
	}

	private ScreenBoxImpl findBoxByName(String title, DisplayType attachType) {
		Object laterWillBeInterface = title;
		if (laterWillBeInterface instanceof ScreenBoxImpl) {
			return (ScreenBoxImpl) laterWillBeInterface;
		}
		if (indexedPairs != null) {
			ScreenBoxImpl pair = indexedPairs.get(title);
			return pair;
		}
		NamedObjectCollection noc = getNamedObjectCollectionForDisplayType(attachType);
		if (noc != null) {
			BT pojo = noc.findBoxByName(title);
			if (pojo instanceof ScreenBoxImpl)
				return (ScreenBoxImpl) pojo;
			if (pojo != null) {
				Debuggable.warn("Dupicate namess! " + title + " " + pojo.getClass() + " " + pojo);
			}
		}
		for (Component view : getSubComponents(attachType)) {
			String ctitle = TriggerMenuFactory.getLabel(view, 1);
			if (intersects(ctitle, title)) {
				Object box = findObjectByComponent(view);
				Container holder = getContainer(attachType);
				ScreenBoxImpl pair = new ScreenBoxImpl(noc, title, box);//, view);//, attachType, holder, this);
				registerPair(pair, false);
				return pair;
			}
		}
		return null;
	}

	private ScreenBoxImpl findBoxByObject(Object box, DisplayType attachType) {
		if (box instanceof String) {
			return findBoxByName((String) box, attachType);
		}
		ScreenBoxImpl pair = (ScreenBoxImpl) savedInPOJOColection.findBoxByObject(box);
		if (pair != null) {
			return pair;
		}
		if (box instanceof Component) {
			pair = findBoxByComponent((Component) box, attachType);
			if (pair != null) {
				return pair;
			}
		}
		pair = indexedPairs.get(box);
		if (pair != null) {
			return pair;
		}

		if (box instanceof ScreenBoxImpl) {
			return (ScreenBoxImpl) box;
		}
		Component holder = findComponentByObject(box, attachType);
		return findBoxByComponent(holder, attachType);
	}

	public Component findComponentByObject(Object box, DisplayType attachType) {
		for (Component view : getSubComponents(attachType)) {
			if (intersects(findObjectByComponent(view), box)) {
				return view;
			}
		}
		return null;
	}

	public DisplayContext findDisplayContext(Box box) {
		return findBoxByObject(box, DisplayType.ANY).getDisplayContext();
	}

	public Object findObjectByComponent(Component view) {
		Object obj = findObjectByComponent0(view);
		if (obj == view) {
			return obj;
		}
		if (obj != null) {
			return obj;
		}
		return null;
	}

	private Object findObjectByComponent0(Component view) {
		if (view instanceof GetObject) {
			return ((GetObject) view).getValue();
		}
		if (view instanceof GetSetObject) {
			return ((GetSetObject) view).getValue();
		}
		if (view instanceof PropertyEditor) {
			return ((PropertyEditor) view).getValue();
		}
		BT pair = indexedPairs.get(view);
		if (pair != null) {
			Object obj = pair.getObject();
			if (obj != null)
				return obj;
		}
		return null;
	}

	/**
	 * 
	 * Return an Application UI capable of supporting the display type
	 * 
	 * @param attachType
	 * @return
	 */
	public abstract AppGUIWithTabsAndTrees getAppGUI(DisplayType attachType);

	/*
	abstract AppGUIWithTabsAndTrees getAppGUI(DisplayType attachType) {
		if (true)
			throw new AbstractMethodError("Not completed");
		return (AppGUIWithTabsAndTrees) getComponent();
	}
	*/
	public BoxPanelSwitchableView getBoxPanelTabPane() {
		return this;
	}

	public Component getComponent() {
		if (true)
			throw new AbstractMethodError("Not completed");
		return getAppGUI(DisplayType.TOSTRING).getGenericContainer();
	}

	private Component getComponent(AppGUIWithTabsAndTrees ch) {
		final JDesktopPane jdp = ch.getDesktopPane();
		final java.awt.Container jif = ch.getGenericContainer();
		final JTabbedPane jtp = ch.getTabbedPane();
		final JTree jtree = ch.getTree();

		if (jtp != null) {
			return jtp.getParent();
		}
		if (jtree != null) {
			return jtree.getParent();
		}
		if (jdp != null) {
			return jdp.getParent();
		}
		if (jif != null) {
			return jif.getParent();
		}
		return null;
	}

	private Container getContainer(DisplayType attachType) {
		AppGUIWithTabsAndTrees ch = getAppGUI(attachType);
		final JDesktopPane jdp = ch.getDesktopPane();
		final java.awt.Container jif = ch.getGenericContainer();
		final JTabbedPane jtp = ch.getTabbedPane();
		final JTree jtree = ch.getTree();
		switch (attachType) {
		case ANY: {
			return null;
		}
		case FRAME: {
			if (jdp != null) {
				return jdp;
			}
		}
		case PANEL: {
			if (jtp != null) {
				return jtp;
			}
		}
		case TREE: {
			if (jtree != null) {
				return jtree;
			}
		}
		case TOSTRING: {
		}
		}
		if (jdp != null) {
			return jdp;
		}
		if (jif != null) {
			return jif;
		}
		if (jtp != null) {
			return jtp;
		}
		if (jtree != null) {
			return jtree;
		}
		return null;
	}

	private DisplayType getDisplayType(Component view) {

		ScreenBoxImpl box = indexedPairs.get(view);
		if (box != null) {
			DisplayType typ = box.m_displayType;
			if (typ != DisplayType.ANY)
				return typ;
		}

		if (view == null)
			return DisplayType.ANY;
		if (view instanceof JTree)
			return DisplayType.TREE;
		if (view instanceof JPanel)
			return DisplayType.PANEL;
		if (view instanceof JInternalFrame)
			return DisplayType.FRAME;
		if (view instanceof Window)
			return DisplayType.FRAME;
		if (view instanceof Container)
			return DisplayType.TREE;
		return null;
	}

	// ===== Event adapter classes ==================================

	private TreePath getPathOf(Component view) {
		if (view instanceof JTree) {
			return ((JTree) view).getLeadSelectionPath();
		}
		return null;
	}

	public ScreenBoxImpl getSelectedObject(DisplayType attachType) {
		return indexedPairs.get(attachType);
	}

	public Dimension getSize(DisplayType attachType) {
		return getContainer(attachType).getSize();
	}

	private Component[] getSubComponents(DisplayType attachType) {
		ArrayList<Component> subs = new ArrayList<Component>();
		Container holder = getContainer(attachType);
		if (holder != null) {
			return holder.getComponents();
		}
		AppGUIWithTabsAndTrees ch = getAppGUI(attachType);
		final JDesktopPane jdp = ch.getDesktopPane();
		final java.awt.Container jif = ch.getGenericContainer();
		final JTabbedPane jtp = ch.getTabbedPane();
		final JTree jtree = ch.getTree();

		if (jdp != null) {
			Collections.addAll(subs, jdp.getComponents());
		}
		if (jif != null) {
			Collections.addAll(subs, jif.getComponents());
		}
		if (jtp != null) {
			Collections.addAll(subs, jtp.getComponents());
		}
		if (jtree != null) {
			Collections.addAll(subs, jtree.getComponents());
		}
		return subs.toArray(new Component[subs.size()]);
	}

	public Iterable<DisplayType> getSupportedAttachTypes() {
		ArrayList<DisplayType> lst = new ArrayList<DisplayType>();
		for (DisplayType dt : new DisplayType[] { DisplayType.HIDDEN, DisplayType.TREE, DisplayType.PANEL, DisplayType.TOSTRING, DisplayType.FRAME, DisplayType.ANY }) {
			lst.add(dt);
		}
		return lst;
	}

	public String getTitleOf(Object view) {
		return getTitleOf(view, DisplayType.ANY);
	}

	public String getTitleOf(Object box, DisplayType attachType) {
		ScreenBoxImpl pair = findBoxByObject(box, attachType);
		if (pair == null)
			return NamedObjectCollection.MISSING_COMPONENT;
		return pair.getShortLabel();
	}

	private Container guessContainerByComponent(Component view, DisplayType attachType, boolean useGuess) {
		if (view != null)
			return guessContainerByComponent0(view, attachType, useGuess);
		if (!useGuess)
			return null;
		return getContainer(attachType);
	}

	private Container guessContainerByComponent0(Component view, DisplayType attachType, boolean useGuess) {

		AppGUIWithTabsAndTrees ch = getAppGUI(attachType);
		final JDesktopPane jdp = ch.getDesktopPane();
		final java.awt.Container jif = ch.getGenericContainer();
		final JTabbedPane jtp = ch.getTabbedPane();
		final JTree jtree = ch.getTree();

		if (jdp != null) {
			int i = jdp.getIndexOf(view);
			if (i != -1) {
				return jdp;
			}
		}
		if (jif != null) {
			for (Component c0 : jif.getComponents()) {
				if (view.equals(c0)) {
					return jif;
				}
			}
		}
		if (jtp != null) {
			int i = jtp.indexOfComponent(view);
			if (i != -1) {
				return jtp;
			}
		}
		if (jtree != null) {
			for (Component c0 : jtree.getComponents()) {
				if (view.equals(c0)) {
					return jtree;
				}
			}
		}
		if (!useGuess)
			return null;
		return getContainer(attachType);
	}

	private boolean intersects(Object objectByComponent, Object box) {
		return objectByComponent == box;
	}

	private void registerPair(Object key, BT pair) {
		if (key == null) {
			return;
		}
		indexedPairs.put(key, (ScreenBoxImpl) pair);
	}

	public void rename(Component view, String title) {
		DisplayType attachType = getDisplayType(view);
		ScreenBoxImpl pair = findBoxByComponent(view, attachType);
		indexedPairs.put(title, pair);
	}

	public void registerPair(BT pair0, boolean insertChild) {
		ScreenBoxImpl pair = (ScreenBoxImpl) pair0;
		registerPair(pair.m_title, pair);
		registerPair(pair.m_obj, pair);
		registerPair(pair.m_displayType, pair);
		registerPair(pair.m_view, pair);
		registerPair(pair.m_box, pair);
		if (insertChild) {
			addChildToParent(pair, pair.m_title, pair.m_view, pair.m_displayType, pair.m_parent_component, insertChild);
		}
	}

	public void setSelectedComponent(Component view) {
		if (!containsComponent(view)) {
			Object o2 = Utility.dref(view, null);
			Debuggable.warn("ERRROR", view, DisplayType.ANY);
			addComponent("ERRROR", view, DisplayType.ANY);
		}
		setSelectedComponent0(view);
		view.setVisible(true);
		view.show();
	}

	private boolean addChildToParent(ScreenBoxImpl pair, String m_title, Component m_view, DisplayType m_displayType, Container m_parent_component, boolean showASAP) {
		if (pair != null && !pair.m_is_added) {
			return true;
		}
		setChild(m_title, m_view, m_parent_component, true, showASAP, false, true);

		if (pair != null) {
			pair.m_is_added = true;
		}
		return true;
	}

	private void setSelectedComponent0(Component view) {
		setChild(null, view, view.getParent(), false, true, false, true);
	}

	public void setSelectedObject(Object object) throws java.beans.PropertyVetoException {
		Component view = findComponentByObject(object, DisplayType.ANY);
		if (view != null) {
			setSelectedComponent0(view);
		} else {
			Debuggable.warn("Missiong object " + object);
		}
	}

	private DisplayType setChild(String title, Component view, Container typ, boolean add, boolean select, boolean remove, boolean onlyFirst) {
		AppGUIWithTabsAndTrees ch = getAppGUI(getDisplayType(view));
		JDesktopPane jdp = ch.getDesktopPane();
		java.awt.Container jif = ch.getGenericContainer();
		JTabbedPane jtp = ch.getTabbedPane();
		JTree jtree = ch.getTree();

		boolean didSomething = false;
		DisplayType didSomewhere;// = DisplayType.HIDDEN;

		ScreenBoxImpl box = indexedPairs.get(view);
		if (box != null && typ == null) {
			typ = box.m_parent_component;
		}
		if (typ != null) {
			jdp = null;
			jif = null;
			jtp = null;
			jtree = null;
			if (typ instanceof JTabbedPane) {
				jtp = (JTabbedPane) typ;
			} else if (typ instanceof JDesktopPane) {
				jdp = (JDesktopPane) typ;
			} else if (typ instanceof JTree) {
				jtree = (JTree) typ;
			} else {
				jif = typ;
			}
		}

		if (jtp != null) {
			{
				JTabbedPane jp = jtp;
				int i = jp.indexOfTabComponent(view);
				if (remove) {
					if (i != -1) {
						jp.removeTabAt(i);
						didSomething = true;
						didSomewhere = DisplayType.PANEL;
						if (onlyFirst)
							return didSomewhere;
					}
				}
				if (select && i == -1) {
					add = true;
				}
				if (add) {
					if (i == -1) {
						if (title != null)
							jp.addTab(title, view);
						else
							jp.addTab(getTitleOf(view), view);
						i = jp.indexOfTabComponent(view);
						didSomething = true;
					}
				}
				if (select) {
					jp.setSelectedIndex(i);
					didSomething = true;
				}

				if (onlyFirst)
					return DisplayType.PANEL;
			}
		}

		if (jdp != null) {
			{
				JDesktopPane jp = jdp;
				int i = jp.getIndexOf(view);
				if (remove) {
					if (i != -1) {
						didSomething = true;
						jp.remove(i);
						if (onlyFirst)
							return DisplayType.FRAME;
					}
				}
				if (select && i == -1) {
					add = true;
				}
				if (add) {
					if (i == -1) {
						if (title != null)
							jp.add(title, view);
						else
							jp.add(view);
						didSomething = true;
						i = jp.getIndexOf(view);
					}
				}
				if (select) {
					jp.moveToFront(view);
					didSomething = true;

				}

				if (onlyFirst)
					return DisplayType.FRAME;
			}
		}
		if (jtree != null) {
			jif = jtree;

			TreePath tp = getPathOf(view);
			if (tp != null && select) {
				jtree.setSelectionPath(tp);
				didSomething = true;
				return DisplayType.TREE;
			}

		}

		if (jif != null) {

			Component[] comps = jif.getComponents();
			Container jp = jtree;
			int i = findComp(view, comps);
			if (remove) {
				if (i != -1) {
					jp.remove(i);
					didSomething = true;
				}
				jp.remove(view);
				didSomething = true;
				if (onlyFirst)
					return DisplayType.TREE;

			}
			if (select && i == -1) {
				add = true;
			}
			if (add) {
				if (i == -1) {
					if (title != null)
						jp.add(title, view);
					else
						jp.add(view);
					didSomething = true;
					comps = jif.getComponents();
					i = findComp(view, comps);
				}
			}
			if (select) {
				jp.setVisible(true);
				view.setVisible(true);
				comps[i].setVisible(true);
				comps[i].show(true);
				didSomething = true;
			}
		}
		if (!didSomething) {
			Debuggable.warn("didnt do anyhting!?");
			return DisplayType.ANY; // @todo .VISIBLE;
		}
		return DisplayType.ANY;
	}

	int findComp(Object view, Component[] inside) {
		int i = -1;
		for (Component c0 : inside) {
			i++;
			if (intersects(c0, view)) {
				return i;
			}
		}
		return -1;
	}

	public void removeComponent(Component view) {
		BT pair = findBoxByComponent(view, DisplayType.ANY);
		removePair(pair);
	}

	public void removeObject(Object box, DisplayType attachType) {
		ScreenBoxImpl pair = findBoxByObject(box, attachType);
		removePair(pair);
	}

	private void removePair(Object key, BT pair) {
		if (key == null) {
			return;
		}
		if (indexedPairs.get(key) == pair) {
			indexedPairs.remove(key);
		}
	}

	private void removePair(BT pair0) {
		ScreenBoxImpl pair = (ScreenBoxImpl) pair0;
		if (pair == null)
			return;
		if (pair.m_is_added) {
			pair.m_parent_component.remove(pair.m_view);
			pair.m_is_added = false;
		}
		removePair(pair.m_title, pair);
		removePair(pair.m_parent_component, pair);
		removePair(pair.m_toplvl, pair);
		removePair(pair.m_obj, pair);
		removePair(pair.m_displayType, pair);
		removePair(pair.m_view, pair);
		removePair(pair.m_box, pair);
	}

	public JPanel showScreenBox(Box<?> box, DisplayType attachType) {
		return showScreenBox(null, (Object) box, attachType);

	}

	/**
	 * For objects that happen to be Components, this method can be used to
	 * cause the object to be drawn as a component.
	 * @throws IntrospectionException 
	 */
	private JPanel showScreenBoxGUIFrame(String name, Component object, DisplayType attachType) throws IntrospectionException {
		Window existing = (Window) objectGUIs.findBrother(object);

		if (existing == null || ALLOW_MULTIPLE_WINDOWS) {

			if (object instanceof JInternalFrame) {
				JInternalFrame f = (JInternalFrame) object;
				if (!ALLOW_MULTIPLE_WINDOWS)
					objectGUIs.add(object, f);
				f.addInternalFrameListener(listener);
				addComponent(f.getTitle(), f, attachType);
				f.toFront();
				f.show();

			} else if (object instanceof JComponent) {
				JInternalFrame f = new JInternalFrame(name, true, true, true, true);
				f.getContentPane().add(object);
				if (!ALLOW_MULTIPLE_WINDOWS)
					objectGUIs.add(object, f);
				f.addInternalFrameListener(listener);
				f.pack();
				addComponent(f.getTitle(), f, attachType);
				f.toFront();
				f.show();

			} else if (object instanceof Window) {
				Window window = (Window) object;
				if (!ALLOW_MULTIPLE_WINDOWS)
					objectGUIs.add(object, window);
				window.addWindowListener(listener);
				window.setSize(window.getPreferredSize());
				org.appdapter.gui.api.Utility.centerWindow(window);
				window.show();

			} else {
				JInternalFrame f = new JInternalFrame(name, true, true, true, true);
				f.getContentPane().add(object);
				f.setSize(f.getPreferredSize());
				if (!ALLOW_MULTIPLE_WINDOWS)
					objectGUIs.add(object, f);
				f.addInternalFrameListener(listener);
				f.pack();
				// f.setSize(f.getPreferredSize());
				// Utility.centerWindow(f);
				// f.show();
				addComponent(f.getTitle(), f, attachType);
				f.toFront();
				f.show();
			}
		} else {
			existing.show();
			existing.toFront();
		}
		return null;
	}

	public BT addObject(String title, Object obj) throws PropertyVetoException {
		return addObject(title, obj, DisplayType.ANY, false);
	}

	public JPanel showScreenBox(Object object, boolean showASAP) {
		return addObject(object, true).getPropertiesPanel();
	}

	public void reload() {
		Debuggable.notImplemented();
		Debuggable.notImplemented();
	}

	public JPanel showScreenBox(String title, Object child, DisplayType attachType) {
		return addObject(title, child, attachType, true).getPropertiesPanel();
	}

	public NamedObjectCollection getNamedObjectCollection() {
		if (savedInPOJOColection != null)
			return savedInPOJOColection;
		BrowserPanelGUI m_toplevel = (BrowserPanelGUI) getDisplayContextNoLoop();
		return m_toplevel.getLocalBoxedChildren();
	}

	public Iterable<ScreenBoxImpl> getScreenBoxes() {
		Debuggable.notImplemented();
		return null;
	}

	public Object getSelectedComponent() {
		Debuggable.notImplemented();
		return null;
	}

	public Set<BT> findComponentsByPredicate(Comparator<BT> cursor, DisplayType attachTyp) {
		Debuggable.notImplemented();
		return null;
	}

	public BT addObject(Object object, boolean showASAP) {
		return addObject(null, object, DisplayType.ANY, showASAP);
	}

	public DisplayContext getDisplayContext() {
		Debuggable.notImplemented();
		return (DisplayContext) getDisplayContextNoLoop();
	}

	public UserResult showScreenBox(Object value) throws Exception {
		DisplayContext m_toplevel = (DisplayContext) getBrowsePanelGUI();
		return m_toplevel.showScreenBox(value);
	}

	private BrowsePanel getDisplayContextNoLoop() {
		Debuggable.notImplemented();
		return Utility.browserPanel;
	}

	private BrowsePanel getBrowsePanelGUI() {
		return Utility.browserPanel;
	}

	public Collection getTriggersFromUI(Object object) {
		BrowsePanel m_toplevel = getBrowsePanelGUI();
		return m_toplevel.getTriggersFromUI(object);
	}

	public UserResult showError(String msg, Throwable error) {
		BrowsePanel m_toplevel = getBrowsePanelGUI();
		return m_toplevel.showError(msg, error);
	}

	public UserResult showMessage(String msg) {
		BrowsePanel m_toplevel = getBrowsePanelGUI();
		return m_toplevel.showMessage(msg);
	}

	@Override public void addTab(String title, JComponent thing) {
		Utility.browserPanel.addTab(title, thing);
	}

	@Override public Dimension getPreferredChildSize() {
		return Utility.browserPanel.getPreferredSize();
	}
}
