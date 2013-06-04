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

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.gui.browse.DisplayContext;
import org.appdapter.gui.browse.TriggerMenuFactory;
import org.appdapter.gui.pojo.DisplayType;
import org.appdapter.gui.pojo.GetSetObject;
import org.appdapter.gui.pojo.NamedObjectCollection;
import org.appdapter.gui.pojo.POJOBox;
import org.appdapter.gui.pojo.POJOCollection;
import org.appdapter.gui.pojo.POJOCollectionImpl;
import org.appdapter.gui.pojo.PairTable;
import org.appdapter.gui.pojo.Utility;
import org.appdapter.gui.swing.POJOAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class BoxPanelSwitchableViewImpl

extends POJOCollectionImpl implements BoxPanelSwitchableView, POJOCollection, NamedObjectCollection {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Window event adapter class, used to find out when child windows close
	 */
	class Adapter extends WindowAdapter implements InternalFrameListener {
		@Override public void internalFrameActivated(InternalFrameEvent e) {
		}

		@Override public void internalFrameClosed(InternalFrameEvent e) {
		}

		@Override public void internalFrameClosing(InternalFrameEvent e) {
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

		@Override public void internalFrameDeactivated(InternalFrameEvent e) {
		}

		@Override public void internalFrameDeiconified(InternalFrameEvent e) {
		}

		@Override public void internalFrameIconified(InternalFrameEvent e) {
		}

		@Override public void internalFrameOpened(InternalFrameEvent e) {
		}

		@Override public void windowClosing(WindowEvent e) {
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

	public BoxPanelSwitchableViewImpl() {
		// TODO Auto-generated constructor stub
	}

	public BoxPanelSwitchableViewImpl(MutableBox rootBox) {
		super(rootBox);
	}

	@Override public ScreenBoxImpl addComponent(String title, Component view, DisplayType attachType) {
		ScreenBoxImpl pair = findBoxByComponent(view, attachType);
		if (pair != null && pair.m_is_added)
			return pair;
		pair = findBoxByName(title, attachType);
		if (pair == null) {
			Object box = findObjectByComponent(view);
			Container holder = guessContainerByComponent(view, attachType, true);
			pair = new ScreenBoxImpl(title, box, view, attachType, holder, this);
			registerPair(pair, true);
			return pair;
		}
		registerPair(title, pair);
		return pair;
	}

	@Override public ScreenBoxImpl addObject(Object box) {
		return findBoxByObject(box, DisplayType.ANY);
	}

	@Override public ScreenBoxImpl addObject(String title, Object box, DisplayType attachType, boolean showAsap) {
		ScreenBoxImpl pair = findBoxByObject(box, attachType);
		if (pair == null) {
			pair = findBoxByName(title, attachType);
			if (pair != null) {
				Object fnd = findObjectByComponent(pair.m_view);
				if (fnd != pair.getObject()) {
					pair = null;
				}
			}
		}
		if (pair == null) {
			Container holder = getContainer(attachType);
			Component view = findComponentByObject(box, attachType);
			if (view == null) {
				view = createComponentByObject(box, attachType);
			}
			if (title == null)
				title = TriggerMenuFactory.getLabel(view, 1);
			pair = new ScreenBoxImpl(title, box, view, attachType, holder, this);
			registerPair(pair, showAsap);
		}
		if (showAsap)
			setSelectedComponent(pair.m_view);
		return pair;
	}

	@Override public boolean containsComponent(Component view) {
		DisplayContext pair = findBoxByComponent(view, DisplayType.ANY);
		return pair != null;
	}

	@Override public boolean containsObject(Object box, DisplayType attachType) {
		DisplayContext pair = findBoxByObject(box, attachType);
		return pair != null;
	}

	private Component createComponentByObject(Object box, DisplayType attachType) {
		if (box instanceof ScreenBox) {
			ScreenBox sb = (ScreenBox) box;
			return sb.getComponent();
		}
		return Utility.addObject(box).getComponent();
	}

	public ScreenBoxImpl findBoxByComponent(Component view, DisplayType attachType) {
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
			ScreenBoxImpl npair = new ScreenBoxImpl(title, box, view, attachType, holder, this);
			registerPair(npair, false);
			return npair;
		}
		return null;
	}

	public ScreenBoxImpl findBoxByName(String title) {
		return findBoxByName(title, DisplayType.ANY);
	}

	public ScreenBoxImpl findBoxByName(String title, DisplayType attachType) {
		Object laterWillBeInterface = title;
		if (laterWillBeInterface instanceof ScreenBoxImpl) {
			return (ScreenBoxImpl) laterWillBeInterface;
		}
		if (indexedPairs != null) {
			ScreenBoxImpl pair = indexedPairs.get(title);
			return pair;
		}
		for (Component view : getSubComponents(attachType)) {
			String ctitle = TriggerMenuFactory.getLabel(view, 1);
			if (intersects(ctitle, title)) {
				Object box = findObjectByComponent(view);
				Container holder = getContainer(attachType);
				ScreenBoxImpl pair = new ScreenBoxImpl(title, box, view, attachType, holder, this);
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
		if (box instanceof Component) {
			ScreenBoxImpl pair = findBoxByComponent((Component) box, attachType);
			if (pair != null) {
				return pair;
			}
		}
		ScreenBoxImpl pair = indexedPairs.get(box);
		if (pair != null) {
			return pair;
		}

		if (box instanceof ScreenBoxImpl) {
			return (ScreenBoxImpl) box;
		}
		Component holder = findComponentByObject(box, attachType);
		return findBoxByComponent(holder, attachType);
	}

	@Override public Component findComponentByObject(Object box, DisplayType attachType) {
		for (Component view : getSubComponents(attachType)) {
			if (intersects(findObjectByComponent(view), box)) {
				return view;
			}
		}
		return null;
	}

	@Override public DisplayContext findDisplayContext(Box box) {
		return findBoxByObject(box, DisplayType.ANY);
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

	public Object findObjectByComponent0(Component view) {
		if (view instanceof GetSetObject) {
			return ((GetSetObject) view).getValue();
		}
		if (view instanceof PropertyEditor) {
			return ((PropertyEditor) view).getValue();
		}
		POJOBox pair = indexedPairs.get(view);
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
	public AppGUIWithTabsAndTrees getAppGUI(DisplayType attachType) {
		if (true)
			throw new AbstractMethodError("Not completed");
		return (AppGUIWithTabsAndTrees) getComponent();
	}

	@Override public BoxPanelSwitchableView getBoxPanelTabPane() {
		return this;
	}

	@Override public Component getComponent() {
		if (true)
			throw new AbstractMethodError("Not completed");
		return getAppGUI(DisplayType.TOSTRING).getGenericContainer();
	}

	public Component getComponent(AppGUIWithTabsAndTrees ch) {
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

	@Override public ScreenBoxImpl getSelectedObject(DisplayType attachType) {
		return indexedPairs.get(attachType);
	}

	@Override public Dimension getSize(DisplayType attachType) {
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

	@Override public Iterable<DisplayType> getSupportedAttachTypes() {
		ArrayList<DisplayType> lst = new ArrayList<DisplayType>();
		for (DisplayType dt : new DisplayType[] { DisplayType.HIDDEN, DisplayType.TREE, DisplayType.PANEL, DisplayType.TOSTRING, DisplayType.FRAME, DisplayType.ANY }) {
			lst.add(dt);
		}
		return lst;
	}

	public String getTitleOf(Object view) {
		return getTitleOf(view, DisplayType.ANY);
	}

	@Override public String getTitleOf(Object box, DisplayType attachType) {
		ScreenBoxImpl pair = findBoxByObject(box, attachType);
		if (pair == null)
			return MISSING_COMPONENT;
		return pair.getShortLabel();
	}

	public Container guessContainerByComponent(Component view, DisplayType attachType, boolean useGuess) {

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

	private void registerPair(Object key, POJOBox pair) {
		if (key == null) {
			return;
		}
		indexedPairs.put(key, (ScreenBoxImpl) pair);
	}

	public void registerPair(POJOBox pair0, boolean insertChild) {
		ScreenBoxImpl pair = (ScreenBoxImpl) pair0;
		registerPair(pair.m_title, pair);
		registerPair(pair.m_obj, pair);
		registerPair(pair.m_displayType, pair);
		registerPair(pair.m_view, pair);
		registerPair(pair.m_box, pair);
		if (insertChild) {
			if (!pair.m_is_added) {
				pair.m_parent_component.add(pair.m_title, pair.m_view);
				pair.m_is_added = true;
			}
		}
	}

	@Override public void removeComponent(Component view) {
		POJOBox pair = findBoxByComponent(view, DisplayType.ANY);
		removePair(pair);
	}

	@Override public void removeObject(Object box, DisplayType attachType) {
		ScreenBoxImpl pair = findBoxByObject(box, attachType);
		removePair(pair);
	}

	private void removePair(Object key, POJOBox pair) {
		if (key == null) {
			return;
		}
		if (indexedPairs.get(key) == pair) {
			indexedPairs.remove(key);
		}
	}

	private void removePair(POJOBox pair0) {
		ScreenBoxImpl pair = (ScreenBoxImpl) pair0;
		if (pair == null)
			return;
		if (pair.m_is_added) {
			pair.m_parent_component.remove(pair.m_view);
			pair.m_is_added = false;
		}
		removePair(pair.m_title, pair);
		removePair(pair.m_parent_component, pair);
		removePair(pair.m_toplevel, pair);
		removePair(pair.m_obj, pair);
		removePair(pair.m_displayType, pair);
		removePair(pair.m_view, pair);
		removePair(pair.m_box, pair);
	}

	public void rename(Component view, String title) {
		DisplayType attachType = getDisplayType(view);
		ScreenBoxImpl pair = findBoxByComponent(view, attachType);
		indexedPairs.put(title, pair);
	}

	@Override public void setSelectedComponent(Component view) {
		if (!containsComponent(view)) {
			addComponent("ERRROR", view, DisplayType.ANY);
		}
		setSelectedComponent0(view);
		view.setVisible(true);
		view.show();
	}

	public synchronized void setSelectedComponent(Object object) throws java.beans.PropertyVetoException {
	}

	void setSelectedComponent0(Component view) {
		AppGUIWithTabsAndTrees ch = getAppGUI(getDisplayType(view));
		final JDesktopPane jdp = ch.getDesktopPane();
		final java.awt.Container jif = ch.getGenericContainer();
		final JTabbedPane jtp = ch.getTabbedPane();
		final JTree jtree = ch.getTree();

		if (jtp != null) {
			jtp.setSelectedComponent(view);
		}

		if (jdp != null) {
			jdp.moveToFront(view);
			int i = jdp.getIndexOf(view);
			if (i != -1) {
				return;
			}
		}
		if (jtree != null) {
			TreePath tp = getPathOf(view);
			if (tp != null) {
				jtree.setSelectionPath(tp);
			}
		}
		if (jif != null) {
			for (Component c0 : jif.getComponents()) {
				if (intersects(c0, view)) {
					c0.setVisible(true);
					return;
				}
			}
		}
	}

	public void setSelectedPOJO(Object object) throws java.beans.PropertyVetoException {
	}

	public ScreenBoxPanel showScreenBox(Box<?> box, DisplayType attachType) {
		return showScreenBox((Object) box, attachType);

	}

	public ScreenBoxPanel showScreenBox(Object box, DisplayType attachType) {
		ScreenBoxImpl pair = findBoxByObject((Object) box, attachType);
		if (pair == null) {
			Container holder = getContainer(attachType);
			Component view = findComponentByObject(box, attachType);
			if (view == null) {
				view = createComponentByObject(box, attachType);
			}
			String title = TriggerMenuFactory.getLabel(view, 1);
			pair = new ScreenBoxImpl(title, box, view, attachType, holder, this);
			registerPair(pair, true);
		}
		return pair.getPropertiesPanel();
	}

	/**
	 * For objects that happen to be Components, this method can be used to
	 * cause the object to be drawn as a component.
	 * @throws IntrospectionException 
	 */
	private ScreenBoxPanel showScreenBoxGUIFrame(String name, Component object, DisplayType attachType) throws IntrospectionException {
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
				org.appdapter.gui.pojo.Utility.centerWindow(window);
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

	@Override public POJOBox addObject(String title, Object obj) throws PropertyVetoException {
		return addObject(title, obj, DisplayType.ANY, false);
	}

	@Override public Set<POJOBox> findComponentsByPredicate(Comparator<POJOBox> cursor, DisplayType attachTyp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public Iterable<ScreenBoxImpl> getScreenBoxes() {
		return super.getScreenBoxes(DisplayType.ANY);
	}

	@Override public Collection getTriggersFromUI(Object object) {
		return super.getTriggersFromUI(object);
	}

	@Override public void showError(String msg, Throwable err) {
		showScreenBox(err, true);
	}

	@Override public ScreenBoxPanel showScreenBox(Object object) {
		return showScreenBox(object, true);
	}

	@Override public ScreenBoxPanel showScreenBox(Object object, boolean asap) {
		// TODO Auto-generated method stub
		return findBoxByObject(object, DisplayType.ANY).getPropertiesPanel();
	}

	@Override public void reload() {
		// TODO Auto-generated method stub

	}

	@Override public ScreenBoxPanel showMessage(String string) {
		getPOJOAppContext().showError(string, null);
		return null;
	}

	@Override public ScreenBoxPanel showScreenBox(String title, Object child, DisplayType attachType) {
		return addObject(title, child, attachType, true).getPropertiesPanel();
	}

	@Override public POJOAppContext getPOJOAppContext() {
		return Utility.getPOJOAppContext();
	}

}
