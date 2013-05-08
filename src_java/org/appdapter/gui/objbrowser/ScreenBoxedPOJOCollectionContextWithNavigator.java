package org.appdapter.gui.objbrowser;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.Customizer;
import java.beans.IntrospectionException;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.appdapter.gui.objbrowser.model.POJOCollection;
import org.appdapter.gui.objbrowser.model.POJOCollectionListener;
import org.appdapter.gui.objbrowser.model.POJOCollectionWithBoxContext;
import org.appdapter.gui.objbrowser.model.POJOSwizzler;
import org.appdapter.gui.objbrowser.model.Utility;
import org.appdapter.gui.pojo.ScreenBoxedPOJOWithProperties;
import org.appdapter.gui.swing.ErrorDialog;

/**
 * A POJOCollectionContext implementation that uses a ObjectNavigator.
 * 
 * 
 */
public class ScreenBoxedPOJOCollectionContextWithNavigator implements
		POJOCollectionWithBoxContext {
	// ==== Static variables =================
	private static boolean ALLOW_MULTIPLE_WINDOWS = false;

	// ==== Instance variables ================
	ObjectNavigator gui;
	PairTable objectFrames = new PairTable();
	PairTable objectGUIs = new PairTable();
	JInternalFrame classBrowser = null;
	Adapter listener = new Adapter();

	// ==== Constructors ===================

	/**
	 * Creates a new context linked to the given GUI. All operations will use
	 * either the given GUI or the ObjectNavigator that it represents
	 */
	public ScreenBoxedPOJOCollectionContextWithNavigator(ObjectNavigator gui) {
		this.gui = gui;
		if (gui == null) {
			throw new NullPointerException(
					"The ObjectNavigator GUI cannot be null for a POJOCollectionContext");
		}
	}

	// ==== Property getters and setters ==================

	public POJOCollection getPOJOs() {
		return gui.getDisplayedCollection();
	}

	// ==== Implementation of POJOCollectionContext interface ============

	public Collection getPOJOCollectionOfType(Class c) {
		return getPOJOs().getPOJOCollectionOfType(c);
	}

	public boolean containsPOJO(Object o) {
		return getPOJOs().containsPOJO(o);
	}

	public void addListener(POJOCollectionListener l) {
		getPOJOs().addListener(l);
	}

	public void removeListener(
			org.appdapter.gui.objbrowser.model.POJOCollectionListener l) {
		getPOJOs().removeListener(l);
	}

	public Object findPOJO(String name) {
		return getPOJOs().findPOJO(name);
	}

	public String getPOJOName(Object o) {
		POJOSwizzler wrapper = getPOJOs().getSwizzler(o);
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
	public boolean addPOJO(Object object) {
		return getPOJOs().addPOJO(object);
	}

	/**
	 * Removes a object, if it is there
	 * 
	 * @returns true if the object was removed, false if that object wasn't in
	 *          this context
	 */
	public boolean removePOJO(Object object) {
		return getPOJOs().removePOJO(object);
	}

	/**
	 * Returns all actions that can be carried out on the given object
	 */
	public Collection getActions(Object object) {
		Collection actions = new LinkedList();
		if (getPOJOs().containsPOJO(object)) {
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

	/**
	 * Opens up a GUI to show the details of the given object
	 */
	public void showScreenBox(Object object) throws Exception {
		JInternalFrame existing = (JInternalFrame) objectFrames
				.findBrother(object);

		if (existing == null || ALLOW_MULTIPLE_WINDOWS) {

			// Get a wrapper for the object, or create a temporary wrapper if
			// necessary
			POJOSwizzler wrapper = getPOJOs().getSwizzler(object);
			if (wrapper == null) {
				wrapper = new POJOSwizzler(object);
			}

			// Get the object info and descriptor
			BeanInfo objectInfo = wrapper.getBeanInfo();

			// Create the GUI for the object
			Component view;
			Class customizerClass = findCustomizerClass(object.getClass());
			if (customizerClass != null) {
				Customizer customizer = (Customizer) customizerClass
						.newInstance();
				customizer.setObject(object);
				view = (Component) customizer;
			} else {
				view = new ScreenBoxedPOJOWithProperties(this, object);
			}

			// Get an icon for the object
			Icon icon = getIcon(wrapper);// .getIcon();

			// Create an internal frame to hold the GUI
			JInternalFrame frame = new JInternalFrame(getPOJOName(object),
					true, true, true, true);
			frame.setResizable(true);

			// Put the GUI and icon in the frame
			frame.setFrameIcon(icon);
			frame.getContentPane().add(view);

			// Make the size correct
			JDesktopPane desk = gui.getDesk();
			Dimension preferred = frame.getPreferredSize();
			Dimension deskSize = desk.getSize();
			Dimension size = new Dimension(Math.min(preferred.width,
					deskSize.width),
					Math.min(preferred.height, deskSize.height));
			frame.setSize(size);

			// If necessary, add this to the list of object frames
			// to allow reuse of this window if the same object is to be viewed
			// again
			if (!ALLOW_MULTIPLE_WINDOWS) {
				objectFrames.add(object, frame);
			}

			// Listen to the frame, so we notice if it closes
			frame.addInternalFrameListener(listener);

			// Add the frame to the desk and bring it to the front
			desk.add(frame);
			frame.toFront();
			frame.show();

		} else {

			// There was an existing GUI. Just bring it to the front
			existing.show();
			existing.toFront();
		}
	}

	// ==== Other public methods =========================

	private Icon getIcon(POJOSwizzler wrapper) {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectNavigator getGUI() {
		return gui;
	}

	/**
	 * For objects that happen to be Components, this method can be used to
	 * cause the object to be drawn as a component.
	 */
	public void showObjectGUI(Component object) {
		Window existing = (Window) objectGUIs.findBrother(object);

		if (existing == null || ALLOW_MULTIPLE_WINDOWS) {

			if (object instanceof JInternalFrame) {
				JInternalFrame f = (JInternalFrame) object;
				if (!ALLOW_MULTIPLE_WINDOWS)
					objectGUIs.add(object, f);
				f.addInternalFrameListener(listener);
				gui.getDesk().add(f);
				f.toFront();
				f.show();

			} else if (object instanceof JComponent) {
				JInternalFrame f = new JInternalFrame(object.getName(), true,
						true, true, true);
				f.setFrameIcon(getIcon(new POJOSwizzler(object).getBeanInfo()));
				f.getContentPane().add((JComponent) object);
				if (!ALLOW_MULTIPLE_WINDOWS)
					objectGUIs.add(object, f);
				f.addInternalFrameListener(listener);
				f.pack();
				gui.getDesk().add(f);
				f.toFront();
				f.show();

			} else if (object instanceof Window) {
				Window window = (Window) object;
				if (!ALLOW_MULTIPLE_WINDOWS)
					objectGUIs.add(object, window);
				window.addWindowListener(listener);
				window.setSize(window.getPreferredSize());
				org.appdapter.gui.objbrowser.model.Utility.centerWindow(window);
				window.show();

			} else {
				JInternalFrame f = new JInternalFrame(object.getName(), true,
						true, true, true);
				f.getContentPane().add((Component) object);
				f.setSize(f.getPreferredSize());
				if (!ALLOW_MULTIPLE_WINDOWS)
					objectGUIs.add(object, f);
				f.addInternalFrameListener(listener);
				f.pack();
				// f.setSize(f.getPreferredSize());
				// Utility.centerWindow(f);
				// f.show();
				gui.getDesk().add(f);
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
		public int getIconWidth() {
			return 16;
		}

		public int getIconHeight() {
			return 16;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(Color.blue);
			g.setFont(new Font("serif", Font.BOLD, 12));
			g.drawString("@", x, y + 12);
		}
	}

	public void showError(String msg, Throwable error) {
		try {
			if (error == null) {
				new ErrorDialog(msg, error).show();
			} else {
				showScreenBox(error); // @temp
			}
		} catch (Throwable err) {
			new ErrorDialog(
					"A new error occurred while trying to display the original error '"
							+ error + "'!", err).show();
		}
	}

	private Class findCustomizerClass(Class c) throws IntrospectionException {
		BeanInfo objectInfo = Utility.getPOJOInfo(c);
		Class customizerClass = null;
		BeanDescriptor descriptor = objectInfo.getBeanDescriptor();
		if (descriptor != null) {
			customizerClass = descriptor.getCustomizerClass();
		}
		if (customizerClass == null) {
			if (c == Object.class) {
				return null;
			} else {
				return findCustomizerClass(c.getSuperclass());
			}
		} else {
			return customizerClass;
		}
	}

	// ==== Action classes ====================================

	class RenameAction extends AbstractAction {
		Object object;

		RenameAction(Object object) {
			super("Change name");
			this.object = object;
		}

		public void actionPerformed(ActionEvent evt) {
			POJOSwizzler wrapper = getPOJOs().getSwizzler(object);
			if (wrapper != null) {
				RenameDialog dialog = new RenameDialog(
						ScreenBoxedPOJOCollectionContextWithNavigator.this,
						wrapper);
				dialog.show();
			}
		}
	}

	class RemoveAction extends AbstractAction {
		Object object;

		RemoveAction(Object object) {
			super("Remove from collection", Icons.removeFromCollection);
			this.object = object;
		}

		public void actionPerformed(ActionEvent evt) {
			removePOJO(object);
		}
	}

	class ViewAction extends AbstractAction {
		Object object;

		ViewAction(Object object) {
			super("View", Icons.viewBean);
			this.object = object;
		}

		public void actionPerformed(ActionEvent evt) {
			showObjectGUI((Component) object);
		}
	}

	class AddAction extends AbstractAction {
		Object object;

		AddAction(Object object) {
			super("Add to collection", Icons.addToCollection);
			this.object = object;
		}

		public void actionPerformed(ActionEvent evt) {
			addPOJO(object);
		}
	}

	class PropertiesAction extends AbstractAction {
		Object object;

		PropertiesAction(Object object) {
			super("Properties", Icons.properties);
			this.object = object;
		}

		public void actionPerformed(ActionEvent evt) {
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
		public void windowClosing(WindowEvent e) {
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

		public void internalFrameActivated(InternalFrameEvent e) {
		}

		public void internalFrameClosed(InternalFrameEvent e) {
		}

		public void internalFrameClosing(InternalFrameEvent e) {
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

		public void internalFrameDeactivated(InternalFrameEvent e) {
		}

		public void internalFrameDeiconified(InternalFrameEvent e) {
		}

		public void internalFrameIconified(InternalFrameEvent e) {
		}

		public void internalFrameOpened(InternalFrameEvent e) {
		}
	}
}
