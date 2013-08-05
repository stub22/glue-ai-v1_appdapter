package org.appdapter.gui.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.GetSetObject;
import org.appdapter.gui.trigger.TriggerMenuFactory;

public class SafeJMenu extends JMenu implements UISwingReplacement, GetSetObject {

	//It also looks better if you're ignoring case sensitivity:
	protected static Comparator nodeComparator = new TriggerMenuFactory.TriggerSorter();

	ArrayList<Component> mcomps = new ArrayList<Component>();

	@UISalient
	public Object userObject;

	public SafeJMenu(boolean iamObject, String text, Object target) {
		super(text);
		userObject = target;
	}

	/**
	 * Creates a new menu item attached to the specified 
	 * <code>Action</code> object and appends it to the end of this menu.
	 *
	 * @param a the <code>Action</code> for the menu item to be added
	 * @see Action
	 */
	final public JMenuItem add(Action a) {
		JMenuItem mi = createActionComponent(a);
		mi.setAction(a);
		add(mi);
		return mi;
	}

	final @Override public Component add(Component c) {
		ensureUnequelyNamed(c);
		Component r = addSorted(c, -1);
		ensureFoundNamed(c);
		return r;
	}

	/** 
	 * Adds the specified component to this container at the given 
	 * position. If <code>index</code> equals -1, the component will
	 * be appended to the end.
	 * @param     c   the <code>Component</code> to add
	 * @param     index    the position at which to insert the component
	 * @return    the <code>Component</code> added
	 * @see	  #remove
	 * @see java.awt.Container#add(Component, int)
	 */
	final public Component add(Component c, int index) {
		ensureUnequelyNamed(c);
		Component r = super.add(c, index);
		ensureFoundNamed(c);
		return r;
	}

	/**
	 * Appends a menu item to the end of this menu. 
	 * Returns the menu item added.
	 *
	 * @param menuItem the <code>JMenuitem</code> to be added
	 * @return the <code>JMenuItem</code> added
	 */
	@Override public JMenuItem add(JMenuItem c) {
		ensureUnequelyNamed(c);
		JMenuItem r = addSorted(c, -1);
		ensureFoundNamed(c);
		return r;
	}

	/**
	 * Creates a new menu item with the specified text and appends
	 * it to the end of this menu.
	 *  
	 * @param s the string for the menu item to be added
	 */
	final public JMenuItem add(String s) {
		return add(new SafeJMenuItem(this, true, s));
	}

	protected void addImpl(Component comp, Object constraints, int index) {
		mcomps.add(index, comp);
		super.addImpl(comp, constraints, index);
	}

	@Override public void addSeparator() {
		ensureSafePopupMenuCreated();
		//super.addSeparator();
	}

	final public <T> T addSorted(Component newChild, int childIndex) {
		int last = getComponentCount();

		if (childIndex <= 0) {
			int newchildIndex = findBestLocation(newChild);
		} else {
		}
		return (T) addSuper((Component) newChild, childIndex);
	}

	private int findBestLocation(Object mi) {
		Component[] comps = getComponents();
		int max = comps.length;
		if (max == 0)
			return 0;

		int newchildIndex = 0;
		for (Component c : comps) {
			if (nodeComparator.compare(mi, c) > 1) {
				break;
			}
			newchildIndex++;

		}
		if (newchildIndex < max) {
			return newchildIndex;
		}
		return newchildIndex;
	}

	public Component addSuper(Component c, int index) {
		int size = mcomps.size();
		if (size < index) {
			index = size;
		}
		return super.add(c, index);
	}

	public Component insert(Component c, int index) {
		return addSorted(c, index);
	}

	public JMenuItem insert(JMenuItem mi, int pos) {
		int newchildIndex = findBestLocation(mi);
		return super.insert(mi, pos);
	}

	/**
	 * Appends a component to the end of this menu.
	 * Returns the component added.
	 *
	 * @param c the <code>Component</code> to add
	 * @return the <code>Component</code> added
	 */

	protected JMenuItem createActionComponent(Action a) {
		JMenuItem mi = new SafeJMenuItem(userObject, true) {
			protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
				PropertyChangeListener pcl = createActionChangeListener(this);
				if (pcl == null) {
					pcl = super.createActionPropertyChangeListener(a);
				}
				return pcl;
			}
		};
		mi.setHorizontalTextPosition(JButton.TRAILING);
		mi.setVerticalTextPosition(JButton.CENTER);
		return mi;
	}

	/**
	 * Creates a window-closing listener for the popup.
	 *
	 * @param p the <code>JPopupMenu</code>
	 * @return the new window-closing listener
	 *
	 * @see WinListener
	 */
	protected WinListener createWinListener(JPopupMenu p) {
		if (!(p instanceof UISwingReplacement)) {
			// complain complain
		}
		return new WinListener(p);
	}

	public void ensureFoundNamed(Component c) {
		String fnd = TriggerMenuFactory.getLabel(c, 1);
		Component found = TriggerMenuFactory.findChildNamed(this, true, fnd);
		if (found != null) {
			return;
		}
		found = TriggerMenuFactory.findChildNamed(this, true, fnd);
		Debuggable.mustBeSameStrings("found=" + found, fnd);
	}

	private void ensureSafePopupMenuCreated() {
		try {
			Field f = JMenu.class.getDeclaredField("popupMenu");
			f.setAccessible(true);
			JPopupMenu popupMenu = (JPopupMenu) f.get(this);
			if (!(popupMenu instanceof UISwingReplacement)) {
				SafeJPopupMenu safe;
				popupMenu = safe = new SafeJPopupMenu();
				safe.userObject = this;//.userObject our user object may not be populated yet 
				f.set(this, popupMenu);
				popupMenu.setInvoker(this);
				popupListener = createWinListener(popupMenu);
			}
		} catch (NoSuchFieldException t) {
			Debuggable.warn("Fields = " + Debuggable.toInfoStringA(JMenu.class.getDeclaredFields(), ",", 3));
		} catch (Throwable t) {
			throw Debuggable.reThrowable(t);
		}

	}

	public void ensureUnequelyNamed(Component c) {
		String fnd = TriggerMenuFactory.getLabel(c, 1);
		Component found = TriggerMenuFactory.findChildNamed(this, true, fnd);
		if (found == null) {
			Component p = getParent();
			return;
		}
		Debuggable.mustBeSameStrings("found=" + found, fnd);
	}

	protected void fireActionPerformed(ActionEvent event) {
		super.fireActionPerformed(event);
	}

	@Override public Component[] getComponents() {
		if (true)
			return mcomps.toArray(new Component[mcomps.size()]);
		return super.getMenuComponents();
	}

	public Component getMenuComponent(int n) {
		ensureSafePopupMenuCreated();
		return super.getMenuComponent(n);
	}

	@Override public String getText() {
		return super.getText();
	}

	public Object getValue() {
		return userObject;
	}

	/**
	 * Initializes the menu item with the specified text and icon.
	 *
	 * @param text the text of the <code>JMenuItem</code>
	 * @param icon the icon of the <code>JMenuItem</code>
	 */
	protected void init(String text, Icon icon) {
		ensureSafePopupMenuCreated();
		super.init(text, icon);
	}

	@Override public boolean isPopupMenuVisible() {
		return super.isPopupMenuVisible();
	}

	@Override public void removeAll() {
		mcomps.clear();
		super.removeAll();
	}

	public void setObject(Object object) {
		userObject = object;
	}

	@Override public void setPopupMenuVisible(boolean b) {
		super.setPopupMenuVisible(b);
	}

	@Override public String toString() {
		Component p = getParent();//.toString();
		if (p != null) {
			return "" + TriggerMenuFactory.getLabel(p, 1) + "->" + TriggerMenuFactory.getLabel(this, 1);
		}
		return TriggerMenuFactory.getLabel(this, 1);
	}

	/**
	 * Resets the UI property with a value from the current look and feel.
	 *
	 * @see JComponent#updateUI
	 */
	public void updateUI() {
		ensureSafePopupMenuCreated();
		try {
			super.updateUI();
		} catch (Throwable t) {

		}
	}
}
