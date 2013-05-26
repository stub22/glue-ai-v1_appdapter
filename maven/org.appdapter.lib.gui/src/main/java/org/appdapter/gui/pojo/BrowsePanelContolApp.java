package org.appdapter.gui.pojo;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.BeanInfo;
import java.beans.Customizer;
import java.beans.IntrospectionException;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.appdapter.api.trigger.BoxPanelSwitchableView;
import org.appdapter.demo.ObjectNavigatorGUI;
import org.appdapter.gui.box.ScreenBoxImpl;
import org.appdapter.gui.demo.ObjectNavigator.Icons;
import org.appdapter.gui.demo.RenameDialog;
import org.appdapter.gui.editors.ClassCustomizer;
import org.appdapter.gui.swing.ErrorDialog;
import org.appdapter.gui.util.Debuggable;
import org.appdapter.gui.util.PairTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A POJOCollectionContext implementation that uses a ObjectNavigator.
 * 
 * 
 */
public class BrowsePanelContolApp implements POJOApp, POJOCollection {

	// ==== Static variables =================
	private static boolean ALLOW_MULTIPLE_WINDOWS = false;
	private static Logger theLogger = LoggerFactory.getLogger(ScreenBoxedPOJORefPanel.class);
	// ==== Instance variables ================
	ObjectNavigatorGUI gui;
	PairTable<Object, JComponent> objectFrames = new PairTable();
	PairTable objectGUIs = new PairTable();
	JInternalFrame classBrowser = null;
	Adapter listener = new Adapter();

	// ==== Constructors ===================

	static int countOF = 0;

	/**
	 * Creates a new context linked to the given GUI. All operations will use
	 * either the given GUI or the ObjectNavigator that it represents
	 */
	public BrowsePanelContolApp(ObjectNavigatorGUI gui) {
		Utility.pojoApp = this;
		if (countOF > 0) {
			throw new NullPointerException("Tryin to make too many ScreenBoxedPOJOCollectionContextWithNavigator!");
		}
		this.gui = gui;
		if (gui == null) {
			throw new NullPointerException("The ObjectNavigator GUI cannot be null for a POJOCollectionContext");
		}
		gui.getCollectionWithSwizzler().toString();
		countOF++;
	}

	// ==== Property getters and setters ==================

	@Override public POJOCollection getCollection() {
		return gui.getCollectionWithSwizzler();
	}

	@Override public NamedObjectCollection getPOJOSession() {
		return gui.getCollectionWithSwizzler();
	}

	@Override public int getPOJOCount() {
		return getCollection().getPOJOCount();
	}

	// ==== Implementation of POJOCollectionContext interface ============

	@Override public Collection getPOJOCollectionOfType(Class c) {
		return getCollection().getPOJOCollectionOfType(c);
	}

	@Override public boolean containsPOJO(Object o) {
		return getCollection().containsPOJO(o);
	}

	@Override public void addListener(POJOCollectionListener l) {
		getCollection().addListener(l);
	}

	@Override public void removeListener(org.appdapter.gui.pojo.POJOCollectionListener l) {
		getCollection().removeListener(l);
	}

	@Override public Object findPOJO(String name) {
		return getCollection().findPOJO(name);
	}

	@Override public String getPOJOName(Object o) {
		POJOBox wrapper = getPOJOSession().findOrCreateBox(o);
		if (wrapper == null) {
			return "" + o;
		} else {
			return wrapper.getName();
		}
	}

	/**
	 * Adds a new object, if it wasn't already there
	 * 
	 * @returns true if the object was added, false if the object was already
	 *          there
	 */
	@Override public boolean addPOJO(Object object) {
		return getCollection().addPOJO(object);
	}

	/**
	 * Removes a object, if it is there
	 * 
	 * @returns true if the object was removed, false if that object wasn't in
	 *          this context
	 */
	@Override public boolean removePOJO(Object object) {
		return getCollection().removePOJO(object);
	}

	/**
	 * Returns all actions that can be carried out on the given object
	 */
	@Override public Collection getActions(Object object) {
		Collection actions = new LinkedList();
		if (getCollection().containsPOJO(object)) {
			actions.add(new RenameAction(object));
			actions.add(new RemoveAction(object));
		} else {
			actions.add(new AddAction(object));
		}
		if (object instanceof Component) {
			actions.add(new ViewAction(object));
		}
		actions.add(new PropertiesAction(object));
		return actions;
	}

	private void showPanel(Component existing) {
		existing.setVisible(true);
		existing.show();
		if (existing instanceof Frame) {
			Frame frame = (Frame) existing;
			frame.toFront();
			return;
		}
		if (existing instanceof JInternalFrame) {
			JInternalFrame frame = (JInternalFrame) existing;
			frame.toFront();
			return;
		}
		if (existing instanceof JPanel) {
			JPanel frame = (JPanel) existing;
			return;
		}
	}

	/**

	 * @param wrapper
	 * @param view
	 * @return 
	 */
	private Component asPanel(String name, Class c, Component view) {

		if (view instanceof JPanel) {
			setPanelSize((JPanel) view);
			BoxPanelSwitchableView bsv = Utility.getBoxPanelTabPane();
			bsv.add(name, view);
			return view;
		}
		//Object object = wrapper.getObject();
		// Create an internal frame to hold the GUI
		Component frame = getFrame(name, c, view);

		// Make the size correct

		// Add the frame to the desk and bring it to the front
		frame.setVisible(true);
		return frame;
	}

	/**
	 * @param view
	 * @return
	 */
	private void setPanelSize(Component view) {
		BoxPanelSwitchableView bsv = Utility.getBoxPanelTabPane();
		Dimension deskSize = bsv.getSize();
		Dimension preferred = view.getPreferredSize();
		Dimension deskMinsize = new Dimension(Math.max(100, deskSize.width), Math.max(100, deskSize.height));

		Dimension size = new Dimension(Math.min(preferred.width, deskSize.width), Math.min(preferred.height, deskSize.height));
		Dimension minsize = new Dimension(Math.max(100, size.width), Math.max(100, size.height));

		view.setSize(minsize);
	}

	/**
	 * @param object
	 * @return
	 */
	private Component getFrame(String name, Class c, Component view) {
		if (view instanceof JInternalFrame)
			return (JInternalFrame) view;
		if (view instanceof Frame)
			return (Frame) view;
		if (true)
			return createJInternalFrame(name, c, view);
		return createJInternalFrame(name, c, view);
	}

	// ==== Other public methods =========================

	private JInternalFrame createJInternalFrame(String name, Class c, Component view) {
		JInternalFrame frame = new JInternalFrame(name, true, true, true, true);
		frame.setResizable(true);
		try {
			// Get an icon for the object
			Icon icon;
			icon = getIcon(Utility.getBeanInfo(c));
			frame.setFrameIcon(icon);
		} catch (IntrospectionException e) {
		}
		// Put the GUI and icon in the frame
		frame.getContentPane().add(view);
		// Listen to the frame, so we notice if it closes
		frame.addInternalFrameListener(listener);
		BoxPanelSwitchableView bsv = Utility.getBoxPanelTabPane();
		bsv.add(name, frame);
		return frame;
	}

	private Window createFrame(String name, Class c, Component view) {
		JFrame frame = new JFrame();
		frame.setResizable(true);
		try {
			// Get an icon for the object
			Icon icon;
			icon = getIcon(Utility.getBeanInfo(c));
			//frame.setIconImage((icon));

		} catch (IntrospectionException e) {
		}
		// Put the GUI and icon in the frame
		frame.getContentPane().add(view);
		return frame;
	}

	public ObjectNavigatorGUI getGUI() {
		return gui;
	}

	/**
	 * Opens up a GUI to show the details of the given object
	 */
	@Override public void showScreenBox(Object object) {
		if (false && object instanceof Component) {
			Component comp = (Component) object;
			try {
				showObjectGUI(comp.getName(), comp.getClass(), comp);
				return;
			} catch (IntrospectionException e) {
			}
		}
		Component existing = (Component) objectFrames.findBrother(object);
		String name = null;
		if (existing == null || ALLOW_MULTIPLE_WINDOWS) {

			if (object instanceof String) {
				gui.showMessage("RESULT:" + object);
				return;
			}
			// Get a wrapper for the object, or create a temporary wrapper if
			// necessary
			POJOBox wrapper = getPOJOSession().findOrCreateBox(object);
			if (wrapper == null) {
				wrapper = new ScreenBoxImpl(object);
			}
			object = wrapper.getObject();

			Class objClass = wrapper.getPOJOClass();

			// Get the object info and descriptor
			//BeanInfo objectInfo = wrapper.getBeanInfo();

			// Create the GUI for the object
			Component view;
			Class<? extends Customizer> customizerClass = Utility.findCustomizerClass(objClass);
			if (objClass == Class.class && customizerClass != ClassCustomizer.class) {
				Debuggable.warn("Broken Class Cusomizer = " + customizerClass);
				customizerClass = Utility.findCustomizerClass(objClass);
				customizerClass = ClassCustomizer.class;
			} else {
				customizerClass = BasicObjectCustomizer.class;
			}
			Customizer customizer;
			try {
				customizer = Utility.newInstance(customizerClass);
			} catch (Throwable e) {
				customizer = new BasicObjectCustomizer(this, object);
			}
			customizer.setObject(object);
			if (customizer instanceof Component)
				view = (Component) customizer;
			else {
				theLogger.warn("customizer is not a Component " + customizer);
				view = new BasicObjectCustomizer(this, object);
			}
			if (name == null)
				name = getPOJOName(object);
			existing = asPanel(name, objClass, view);
			// If necessary, add this to the list of object frames
			// to allow reuse of this window if the same object is to be viewed
			// again
			if (!ALLOW_MULTIPLE_WINDOWS) {
				objectFrames.add(object, existing);
			}
		} else {
			if (name == null)
				name = getPOJOName(object);
			existing = asPanel(name, object.getClass(), existing);
		}
		showPanel(existing);
	}

	/**
	 * For objects that happen to be Components, this method can be used to
	 * cause the object to be drawn as a component.
	 * @throws IntrospectionException 
	 */
	private void showObjectGUI(String name, Class objClass, Component object) throws IntrospectionException {
		Window existing = (Window) objectGUIs.findBrother(object);

		if (existing == null || ALLOW_MULTIPLE_WINDOWS) {

			if (object instanceof JInternalFrame) {
				JInternalFrame f = (JInternalFrame) object;
				if (!ALLOW_MULTIPLE_WINDOWS)
					objectGUIs.add(object, f);
				f.addInternalFrameListener(listener);
				Utility.tabbedPanels.add(f.getTitle(), f);
				f.toFront();
				f.show();

			} else if (object instanceof JComponent) {
				JInternalFrame f = new JInternalFrame(name, true, true, true, true);
				f.setFrameIcon(getIcon(Utility.getBeanInfo(objClass)));
				f.getContentPane().add(object);
				if (!ALLOW_MULTIPLE_WINDOWS)
					objectGUIs.add(object, f);
				f.addInternalFrameListener(listener);
				f.pack();
				Utility.tabbedPanels.add(f.getTitle(), f);
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
				Utility.tabbedPanels.add(f.getTitle(), f);
				f.toFront();
				f.show();
			}
		} else {
			existing.show();
			existing.toFront();
		}
	}

	/**
	 * Returns an Icon for this object, determined using BeanInfo. If no icon
	 * was found a default icon will be returned.
	 */
	static public Icon getIcon(BeanInfo info) {
		Icon icon;
		try {
			Image image;
			image = info.getIcon(BeanInfo.ICON_COLOR_16x16);
			if (image == null)
				image = info.getIcon(BeanInfo.ICON_MONO_16x16);

			if (image == null)
				icon = new UnknownIcon();
			else
				icon = new ImageIcon(image);
		} catch (Exception err) {
			icon = new UnknownIcon();
		}
		return icon;
	}

	// ===== Inner classes ==========================
	/**
	 * A rather ugly but workable default icon used in cases where there is no
	 * known icon for the object.
	 */
	static class UnknownIcon implements Icon, java.io.Serializable {
		@Override public int getIconWidth() {
			return 16;
		}

		@Override public int getIconHeight() {
			return 16;
		}

		@Override public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(Color.blue);
			g.setFont(new Font("serif", Font.BOLD, 12));
			g.drawString("@", x, y + 12);
		}
	}

	@Override public void showError(String msg, Throwable error) {
		try {
			if (error == null) {
				new ErrorDialog(msg, error).show();
			} else {
				showScreenBox(error); // @temp
			}
		} catch (Throwable err) {
			new ErrorDialog("A new error occurred while trying to display the original error '" + error + "'!", err).show();
		}
	}

	abstract class AbstractActionTrigger extends AbstractAction {

		public AbstractActionTrigger(String name) {
			super(name);
		}

		public AbstractActionTrigger(String name, Icon icon) {
			super(name, icon);
		}

	}

	// ==== Action classes ====================================

	class RenameAction extends AbstractActionTrigger {
		Object object;

		RenameAction(Object object) {
			super("Change name");
			this.object = object;
		}

		@Override public void actionPerformed(ActionEvent evt) {
			POJOBox wrapper = Utility.findOrCreateBox(object);
			if (wrapper != null) {
				RenameDialog dialog = new RenameDialog(BrowsePanelContolApp.this, wrapper);
				dialog.show();
			}
		}
	}

	class RemoveAction extends AbstractActionTrigger {
		Object object;

		RemoveAction(Object object) {
			super("Remove from collection", Icons.removeFromCollection);
			this.object = object;
		}

		@Override public void actionPerformed(ActionEvent evt) {
			removePOJO(object);
		}
	}

	class ViewAction extends AbstractActionTrigger {
		Object object;

		ViewAction(Object object) {
			super("View", Icons.viewBean);
			this.object = object;
		}

		@Override public void actionPerformed(ActionEvent evt) {
			showScreenBox(object);
		}
	}

	class AddAction extends AbstractActionTrigger {
		Object object;

		AddAction(Object object) {
			super("Add to collection", Icons.addToCollection);
			this.object = object;
		}

		@Override public void actionPerformed(ActionEvent evt) {
			addPOJO(object);
		}
	}

	class PropertiesAction extends AbstractActionTrigger {
		Object object;

		PropertiesAction(Object object) {
			super("Properties", Icons.properties);
			this.object = object;
		}

		@Override public void actionPerformed(ActionEvent evt) {
			try {
				showScreenBox(object);
			} catch (Throwable err) {
				showError(null, err);
			}
		}
	}

	// ===== Event adapter classes ==================================

	/**
	 * Window event adapter class, used to find out when child windows close
	 */
	class Adapter extends WindowAdapter implements InternalFrameListener {
		@Override public void windowClosing(WindowEvent e) {
			Object source = e.getSource();
			if (source == classBrowser) {
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
			if (source == classBrowser) {
				classBrowser.removeInternalFrameListener(this);
				classBrowser = null;
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

	@Override public void reload() {
		throw new NotImplementedException("reload this screenbox");

	}

}
