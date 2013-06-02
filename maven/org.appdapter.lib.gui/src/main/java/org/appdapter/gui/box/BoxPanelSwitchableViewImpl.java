package org.appdapter.gui.box;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.IntrospectionException;
import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.Collections;
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
import org.appdapter.gui.browse.AbstractScreenBoxTreeNodeImpl;
import org.appdapter.gui.browse.DisplayContext;
import org.appdapter.gui.browse.TriggerMenuFactory;
import org.appdapter.gui.pojo.DisplayType;
import org.appdapter.gui.pojo.Utility;
import org.appdapter.gui.util.PairTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract public class BoxPanelSwitchableViewImpl

extends AbstractScreenBoxTreeNodeImpl implements BoxPanelSwitchableView {

	WeakHashMap<Object, DisplayPair> indexedPairs = new WeakHashMap<Object, DisplayPair>();

	/**
	 * 
	 * Return an Application UI capable of supporting the display type
	 * 
	 * @param attachType
	 * @return
	 */
	public abstract AppGUI getAppGUI(DisplayType attachType);

	@Override public Component getComponent() {
		if (true)
			throw new AbstractMethodError("Not completed");
		return getAppGUI(DisplayType.TOSTRING).getGenericContainer();
	}

	public Component getComponent(AppGUI ch) {
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

	//private Component selected;

	public BoxPanelSwitchableViewImpl(MutableBox rootBox) {
		super(rootBox);
	}

	@Override public BoxPanelSwitchableView getBoxPanelTabPane() {
		return this;
	}

	public BoxPanelSwitchableViewImpl() {
		// TODO Auto-generated constructor stub
	}

	@Override public DisplayPair addComponent(String title, Component view, DisplayType attachType) {
		DisplayPair pair = findDisplayContextByComponent(view, attachType);
		if (pair != null && pair.isAdded)
			return pair;
		pair = findDisplayContextByTitle(title, attachType);
		if (pair == null) {
			Object box = findObjectByComponent(view);
			Container holder = guessContainerByComponent(view, attachType, true);
			pair = new DisplayPair(title, box, view, attachType, holder, this);
			registerPair(pair, true);
			return pair;
		}
		registerPair(title, pair);
		return pair;
	}

	@Override public boolean containsComponent(Component view) {
		DisplayContext pair = findDisplayContextByComponent(view, DisplayType.ANY);
		return pair != null;
	}

	@Override public boolean containsObject(Object box, DisplayType attachType) {
		DisplayContext pair = findDisplayContextByObject(box, attachType);
		return pair != null;
	}

	private Component createComponentByObject(Object box, DisplayType attachType) {
		if (box instanceof ScreenBox) {
			ScreenBox sb = (ScreenBox) box;
			return sb.getComponent();
		}
		return Utility.findOrCreateBox(box).getComponent();
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
		return findDisplayContextByObject(box, DisplayType.ANY);
	}

	public DisplayPair findDisplayContextByComponent(Component view, DisplayType attachType) {
		Object laterWillBeInterface = view;
		if (laterWillBeInterface instanceof DisplayPair) {
			return (DisplayPair) laterWillBeInterface;
		}

		DisplayPair pair = indexedPairs.get(view);
		if (pair != null) {
			return pair;
		}

		Container holder = guessContainerByComponent(view, attachType, false);
		if (holder != null) {
			Object box = findObjectByComponent(view);
			String title = TriggerMenuFactory.getLabel(view, 1);
			DisplayPair npair = new DisplayPair(title, box, view, attachType, holder, this);
			registerPair(npair, false);
			return npair;
		}
		return null;
	}

	private DisplayPair findDisplayContextByObject(Object box, DisplayType attachType) {
		if (box instanceof String) {
			return findDisplayContextByTitle((String) box, attachType);
		}
		if (box instanceof Component) {
			DisplayPair pair = findDisplayContextByComponent((Component) box, attachType);
			if (pair != null) {
				return pair;
			}
		}
		DisplayPair pair = indexedPairs.get(box);
		if (pair != null) {
			return pair;
		}

		if (box instanceof DisplayPair) {
			return (DisplayPair) box;
		}
		Component holder = findComponentByObject(box, attachType);
		return findDisplayContextByComponent(holder, attachType);
	}

	public DisplayPair findDisplayContextByTitle(String title, DisplayType attachType) {
		Object laterWillBeInterface = title;
		if (laterWillBeInterface instanceof DisplayPair) {
			return (DisplayPair) laterWillBeInterface;
		}
		if (indexedPairs != null) {
			DisplayPair pair = indexedPairs.get(title);
			return pair;
		}
		for (Component view : getSubComponents(attachType)) {
			String ctitle = TriggerMenuFactory.getLabel(view, 1);
			if (intersects(ctitle, title)) {
				Object box = findObjectByComponent(view);
				Container holder = getContainer(attachType);
				DisplayPair pair = new DisplayPair(title, box, view, attachType, holder, this);
				registerPair(pair, false);
				return pair;
			}
		}
		return null;
	}

	@Override public DisplayContext showObject(Box<?> box, DisplayType attachType) {
		return findOrCreateDisplayContextByObject((Object) box, attachType);

	}

	@Override public DisplayContext showObject(Object box, DisplayType attachType) {
		return findOrCreateDisplayContextByObject((Object) box, attachType);

	}

	private DisplayPair findOrCreateDisplayContextByObject(Object box, DisplayType attachType) {
		DisplayPair pair = findDisplayContextByObject((Object) box, attachType);
		if (pair == null) {
			Container holder = getContainer(attachType);
			Component view = findComponentByObject(box, attachType);
			if (view == null) {
				view = createComponentByObject(box, attachType);
			}
			String title = TriggerMenuFactory.getLabel(view, 1);
			pair = new DisplayPair(title, box, view, attachType, holder, this);
			registerPair(pair, true);
		}
		return pair;
	}

	private Container getContainer(DisplayType attachType) {
		AppGUI ch = getAppGUI(attachType);
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

	private static boolean ALLOW_MULTIPLE_WINDOWS = false;
	private static Logger theLogger = LoggerFactory.getLogger(BoxPanelSwitchableViewImpl.class);
	PairTable<Object, JComponent> objectFrames = new PairTable();
	PairTable objectGUIs = new PairTable();
	Object classBrowser_Unused = null;
	Adapter listener = new Adapter();

	/**
	 * For objects that happen to be Components, this method can be used to
	 * cause the object to be drawn as a component.
	 * @throws IntrospectionException 
	 */
	private void showObjectGUIFrame(String name, Component object, DisplayType attachType) throws IntrospectionException {
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
	}

	// ===== Event adapter classes ==================================

	/**
	 * Window event adapter class, used to find out when child windows close
	 */
	class Adapter extends WindowAdapter implements InternalFrameListener {
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
		DisplayPair pair = indexedPairs.get(view);
		if (pair != null) {
			Object obj = pair.getObject();
			if (obj != null)
				return obj;
		}
		return null;
	}

	private TreePath getPathOf(Component view) {
		if (view instanceof JTree) {
			return ((JTree) view).getLeadSelectionPath();
		}
		return null;
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
		AppGUI ch = getAppGUI(attachType);
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

	@Override public String getTitleOf(Component view) {
		DisplayPair pair = findDisplayContextByComponent(view, DisplayType.ANY);
		if (pair == null) {
			return BoxPanelSwitchableView.MISSING_COMPONENT;
		}
		return pair.getTitle();
	}

	private boolean intersects(Object objectByComponent, Object box) {
		return objectByComponent == box;
	}

	void registerPair(DisplayPair pair, boolean insertChild) {
		registerPair(pair.m_title, pair);
		registerPair(pair.m_obj, pair);
		registerPair(pair.m_displayType, pair);
		registerPair(pair.m_view, pair);
		registerPair(pair.m_box, pair);
		if (insertChild) {
			if (!pair.isAdded) {
				pair.m_parent_component.add(pair.m_title, pair.m_view);
				pair.isAdded = true;
			}
		}
	}

	private void registerPair(Object key, DisplayPair pair) {
		if (key == null) {
			return;
		}
		indexedPairs.put(key, pair);
	}

	@Override public void removeComponent(Component view) {
		DisplayPair pair = findDisplayContextByComponent(view, DisplayType.ANY);
		removePair(pair);
	}

	private void removePair(DisplayPair pair) {
		if (pair == null)
			return;
		if (pair.isAdded) {
			pair.m_parent_component.remove(pair.m_view);
			pair.isAdded = false;
		}
		removePair(pair.m_title, pair);
		removePair(pair.m_parent_component, pair);
		removePair(pair.m_toplevel, pair);
		removePair(pair.m_obj, pair);
		removePair(pair.m_displayType, pair);
		removePair(pair.m_view, pair);
		removePair(pair.m_box, pair);
	}

	private void removePair(Object key, DisplayPair pair) {
		if (key == null) {
			return;
		}
		if (indexedPairs.get(key) == pair) {
			indexedPairs.remove(key);
		}
	}

	private void rename(Component view, String title) {
		DisplayType attachType = getDisplayType(view);
		DisplayPair pair = findDisplayContextByComponent(view, attachType);
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

	void setSelectedComponent0(Component view) {
		AppGUI ch = getAppGUI(getDisplayType(view));
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

	public Container guessContainerByComponent(Component view, DisplayType attachType, boolean useGuess) {

		AppGUI ch = getAppGUI(attachType);
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

	@Override public DisplayPair addObject(String title, Object box, DisplayType attachType, boolean showAsap) {
		DisplayPair pair = findDisplayContextByObject(box, attachType);
		if (pair == null) {
			pair = findDisplayContextByTitle(title, attachType);
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
			pair = new DisplayPair(title, box, view, attachType, holder, this);
			registerPair(pair, showAsap);
		}
		if (showAsap)
			setSelectedComponent(pair.m_view);
		return pair;
	}

	@Override public void removeObject(Object box, DisplayType attachType) {
		DisplayPair pair = findDisplayContextByObject(box, attachType);
		removePair(pair);
	}

	@Override public String getTitleOf(Object box, DisplayType attachType) {
		DisplayPair pair = findDisplayContextByObject(box, attachType);
		if (pair == null)
			return MISSING_COMPONENT;
		return pair.getTitle();
	}

	@Override public DisplayPair getSelectedObject(DisplayType attachType) {
		return indexedPairs.get(attachType);
	}

	@Override public DisplayPair findDisplayContext(Object box) {
		return findDisplayContextByObject(box, DisplayType.ANY);
	}

}
