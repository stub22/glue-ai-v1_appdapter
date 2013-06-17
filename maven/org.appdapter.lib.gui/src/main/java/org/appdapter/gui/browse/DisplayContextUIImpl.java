package org.appdapter.gui.browse;

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
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.Customizer;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.appdapter.api.trigger.AbstractTriggerAction;
import org.appdapter.api.trigger.BT;
import org.appdapter.api.trigger.BoxPanelSwitchableView;
import org.appdapter.api.trigger.BrowserPanelGUI;
import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.api.trigger.DisplayType;
import org.appdapter.api.trigger.IShowObjectMessageAndErrors;
import org.appdapter.api.trigger.ITabUI;
import org.appdapter.api.trigger.NamedObjectCollection;
import org.appdapter.api.trigger.POJOBoxImpl;
import org.appdapter.api.trigger.POJOCollection;
import org.appdapter.api.trigger.POJOCollectionListener;
import org.appdapter.api.trigger.UserResult;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.ComponentHost;
import org.appdapter.gui.api.PairTable;
import org.appdapter.gui.api.Utility;
import org.appdapter.gui.box.ScreenBoxImpl;
import org.appdapter.gui.editors.ArrayContentsPanel;
import org.appdapter.gui.editors.LargeObjectView;
import org.appdapter.gui.editors.RenameDialog;
import org.appdapter.gui.impl.Icons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A POJOCollectionContext implementation that uses a ObjectNavigator.
 * 
 * 
 */
public class DisplayContextUIImpl implements BrowserPanelGUI, POJOCollection {

	public BrowserPanelGUI getLocalTreeAPI() {
		Debuggable.notImplemented();
		return null;
	}

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
	}

	class AddAction extends AbstractTriggerAction {
		final NamedObjectCollection collection = getLocalBoxedChildren();
		Object value;

		AddAction(Object value) {
			super("Add to Context", Icons.addToCollection);
			this.value = value;
		}

		@Override public void actionPerformed(ActionEvent evt) {
			try {
				collection.findOrCreateBox(value);
			} catch (Exception e) {
				showError(toString(), e);
			}
		}
	}

	class PropertiesAction extends AbstractTriggerAction {
		Object object;

		PropertiesAction(Object object) {
			super("Properties", getIcon("properties"));
			this.object = object;
		}

		@Override public void actionPerformed(ActionEvent evt) {
			try {
				showScreenBox(Utility.getPropertiesPanel(object));
			} catch (Throwable err) {
				Utility.showError(null, null, err);
			}
		}
	}

	class RemoveAction extends AbstractTriggerAction {
		Object object;

		RemoveAction(Object object) {
			super("Remove from collection", getIcon("removeFromCollection"));
			this.object = object;
		}

		@Override public void actionPerformed(ActionEvent evt) {
			getCurrentCollection().removeObject(object);
		}
	}

	class RenameAction extends AbstractTriggerAction {
		Object object;

		RenameAction(Object object) {
			super("Change label");
			this.object = object;
		}

		@Override public void actionPerformed(ActionEvent evt) {
			BT wrapper = currentCollection.findBoxByObject(object);
			if (wrapper != null) {
				RenameDialog dialog = new RenameDialog(DisplayContextUIImpl.this, wrapper);
				dialog.show();
			}
		}
	}

	// ===== Inner classes ==========================
	/**
	 * A rather ugly but workable default icon used in cases where there is no
	 * known icon for the object.
	 */
	static class UnknownIcon implements Icon, java.io.Serializable {
		@Override public int getIconHeight() {
			return 16;
		}

		@Override public int getIconWidth() {
			return 16;
		}

		@Override public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(Color.blue);
			g.setFont(new Font("serif", Font.BOLD, 12));
			g.drawString("@", x, y + 12);
		}
	}

	class ViewAction extends AbstractTriggerAction {
		Object value;

		ViewAction(Object value) {
			super("View", Icons.viewObject);
			this.value = value;
		}

		@Override public void actionPerformed(ActionEvent evt) {
			showObjectGUI((Component) value);
		}
	}

	// ==== Static variables =================
	private static boolean ALLOW_MULTIPLE_WINDOWS = false;

	// ==== Other public methods =========================

	static int countOF = 0;

	private static Logger theLogger = LoggerFactory.getLogger(DisplayContextUIImpl.class);

	/**
	 * Returns an Icon for this object, determined using BeanInfo. If no icon
	 * was found a default icon will be returned.
	 */
	static public Icon getIcon(BeanInfo info) {
		Icon icon;
		try {
			Image image;
			image = info.getIcon(BeanInfo.ICON_COLOR_16x16);
			if (image == null) {
				image = info.getIcon(BeanInfo.ICON_MONO_16x16);
			}

			if (image == null) {
				icon = new UnknownIcon();
			} else {
				icon = new ImageIcon(image);
			}
		} catch (Exception err) {
			icon = new UnknownIcon();
		}
		return icon;
	}

	public static ImageIcon getIcon(String string) {
		return new ImageIcon(getImage(Utility.getResource(string)));
	}

	public static Image getImage(URL uri) {
		try {
			return ImageIO.read(uri);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	JInternalFrame classBrowser = null;

	Object classBrowser_Unused = null;

	private NamedObjectCollection currentCollection;

	//==== Instance variables ================
	IShowObjectMessageAndErrors gui;

	Adapter listener = new Adapter();

	// ==== Action classes ====================================

	// ==== Instance variables ================
	BoxPanelSwitchableView mainGUI;

	PairTable<Object, JComponent> objectFrames = new PairTable();

	PairTable objectGUIs = new PairTable();

	/**
	 * Creates a new context linked to the given GUI. All operations will use
	 * either the given GUI or the ObjectNavigator that it represents
	 */
	public DisplayContextUIImpl(BoxPanelSwitchableView gui, IShowObjectMessageAndErrors site, NamedObjectCollection col) {
		Utility.controlApp = this;
		mainGUI = gui;
		currentCollection = col;
		this.gui = site;
		if (site == null) {
			throw new NullPointerException("The DisplayContextSite cannot be null for a DisplayContext");
		}
		if (DisplayContextUIImpl.countOF > 0) {
			throw new NullPointerException("Tryin to make too many ScreenBoxedPOJOCollectionContextWithNavigator!");
		}
		if (gui == null) {
			throw new NullPointerException("The ObjectNavigator GUI cannot be null for a POJOCollectionContext");
		}
		DisplayContextUIImpl.countOF++;
	}

	@Override public void addListener(POJOCollectionListener objectChoice) {
		final POJOCollection collection = getLocalBoxedChildren();
		collection.addListener(objectChoice);
	}

	/**

	 * @param wrapper
	 * @param view
	 * @return 
	 */
	private JPanel asPanel(String name, Class c, Component view, Object boxed) {

		if (view instanceof JPanel) {
			return (JPanel) view;
		}

		if (view instanceof JPanel) {
			setPanelSize(view);
			return new ComponentHost(view, boxed);
			//BoxPanelSwitchableView bsv = Utility.getBoxPanelTabPane();
			//bsv.add(name, view);
			//return view;
		}

		if (view instanceof JComponent) {
			setPanelSize(view);
			return new ComponentHost(view, boxed);
			//BoxPanelSwitchableView bsv = Utility.getBoxPanelTabPane();
			//bsv.add(name, view);
			//return view;
		}

		return new ComponentHost(view, boxed);
		//Object object = wrapper.getObject();
		// Create an internal frame to hold the GUI
		//ScreenBoxPanel frame = getFrame(name, c, view);

		// Make the size correct

		// Add the frame to the desk and bring it to the front
		//frame.setVisible(true);
		//return frame;
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
		bsv.addComponent(name, frame, DisplayType.FRAME);
		return frame;
	}

	private Class findCustomizerClass(Class c) {
		Class cust = null;
		try {
			cust = findCustomizerClass0(c);
		} catch (IntrospectionException e) {
		}
		if (cust != null) {
			return cust;
		}
		if (c.isArray()) {
			return ArrayContentsPanel.class;
		}
		return cust;
	}

	private Class findCustomizerClass0(Class c) throws IntrospectionException {
		BeanInfo objectInfo = Introspector.getBeanInfo(c);
		Class customizerClass = null;
		BeanDescriptor descriptor = objectInfo.getBeanDescriptor();
		if (descriptor != null) {
			customizerClass = descriptor.getCustomizerClass();
		}
		if (customizerClass == null) {
			if (c == Object.class) {
				return null;
			} else {
				return findCustomizerClass0(c.getSuperclass());
			}
		} else {
			return customizerClass;
		}
	}

	@Override public Object findObjectByName(String name) {
		final POJOCollection collection = getLocalBoxedChildren();
		return collection.findObjectByName(name);
	}

	@Override public Collection findObjectsByType(Class type) {
		final POJOCollection collection = getLocalBoxedChildren();
		return collection.findObjectsByType(type);
	}

	@Override public BT findOrCreateBox(Object value) {
		final POJOCollection collection = getLocalBoxedChildren();
		return collection.findOrCreateBox(value);
	}

	/*
		private void setCurrentCollection(NamedObjectCollection currentCollection) {
			this.currentCollection = currentCollection;
		}


		@Override public Component getComponent() {
			DisplayContext displayContext = getDisplayContextNoLoop();
			return displayContext.getComponent();
		}


	*/

	@Override public BoxPanelSwitchableView getBoxPanelTabPane() {
		return Utility.theBoxPanelDisplayContext;
	}

	public NamedObjectCollection getCurrentCollection() {
		if (currentCollection == null) {
			Debuggable.notImplemented();
		}
		return currentCollection;
	}

	//==== Implementation of DisplayContext interface ============

	protected DisplayContext getDisplayContextNoLoop() {
		Debuggable.notImplemented();
		return null;
	}

	/**
	 * Adds a new value, if it wasn't already there
	 *
	 * @returns true if the value was added, false if the value was already there
	 */
	/*
	public boolean addObject(Object value) {
	return collection.addObject(value);
	}*/

	@Override public BrowserPanelGUI getDisplayContext() {
		return this;
	}

	/**
	 * @param m_obj
	 * @return
	 */
	private Component getFrame(String name, Class c, Component view) {
		if (view instanceof JInternalFrame) {
			return view;
		}
		if (view instanceof Frame) {
			return view;
		}
		if (true) {
			return createJInternalFrame(name, c, view);
		}
		return createJInternalFrame(name, c, view);
	}

	//==== Other public methods =========================

	@Override public NamedObjectCollection getLocalBoxedChildren() {
		return Utility.getToplevelBoxCollection();
	}

	@Override public Iterator getObjects() {
		final POJOCollection collection = getLocalBoxedChildren();
		return collection.getObjects();
	}

	public String getTitleOf(Object object) {
		return getCurrentCollection().getTitleOf(object);
	}

	/**
	 * Returns all actions that can be carried out on the given object
	 */
	@Override public Collection getTriggersFromUI(Object object) {
		Collection actions = new LinkedList();
		if (currentCollection.containsObject(object)) {
			actions.add(new RenameAction(object));
			actions.add(new RemoveAction(object));
		} else {
			actions.add(new AddAction(object));
		}
		if (object instanceof Component) {
			actions.add(new ViewAction((Component) object));
		}
		actions.add(new PropertiesAction(object));
		return actions;
	}

	private JPanel objectToPanel(Object object, boolean attachToUIAsap) {
		if (object instanceof JPanel) {
			return (JPanel) object;
		}

		if (object == null) {
			return null;
		}

		JPanel existing = (JPanel) objectFrames.findBrother(object);
		String name = null;
		if (existing == null || DisplayContextUIImpl.ALLOW_MULTIPLE_WINDOWS) {
			NamedObjectCollection noc = getCurrentCollection();
			// Get a wrapper for the object, or create a temporary wrapper if
			// necessary
			BT wrapper = noc.findOrCreateBox(object);
			if (wrapper == null) {
				wrapper = new ScreenBoxImpl(noc, null, object);
			}
			object = wrapper.getValue();

			Class objClass = wrapper.getObjectClass();

			// Get the object info and descriptor
			//BeanInfo objectInfo = wrapper.getBeanInfo();

			// Create the GUI for the object
			Component view;
			view = Utility.getPropertiesPanel(object);
			if (name == null) {
				name = getTitleOf(object);
			}
			view.setName(name);
			existing = asPanel(name, objClass, view, object);

			// If necessary, add this to the list of object frames
			// to allow reuse of this window if the same object is to be viewed
			// again
			if (!DisplayContextUIImpl.ALLOW_MULTIPLE_WINDOWS) {
				objectFrames.add(object, existing);
			}
		} else {
			if (name == null) {
				name = getTitleOf(object);
			}
			existing = asPanel(name, object.getClass(), existing, object);
		}
		if (!attachToUIAsap) {
			return existing;
		}
		return existing;
	}

	public void reload() {
		Debuggable.notImplemented();
	}

	@Override public void renameObject(String oldName, String newName) throws PropertyVetoException {
		final POJOCollection collection = getLocalBoxedChildren();
		collection.renameObject(oldName, newName);
	}

	/**
	 * @param view
	 * @return
	 */
	private void setPanelSize(Component view) {
		BoxPanelSwitchableView bsv = Utility.getBoxPanelTabPane();
		Dimension deskSize = bsv.getSize(DisplayType.FRAME);
		Dimension preferred = view.getPreferredSize();
		Dimension deskMinsize = new Dimension(Math.max(100, deskSize.width), Math.max(100, deskSize.height));

		Dimension size = new Dimension(Math.min(preferred.width, deskSize.width), Math.min(preferred.height, deskSize.height));
		Dimension minsize = new Dimension(Math.max(100, size.width), Math.max(100, size.height));

		view.setSize(minsize);
	}

	//===== Event adapter classes ==================================

	@Override public UserResult showError(String msg, Throwable e) {
		return Utility.showError(null, msg, e);
	}

	@Override public UserResult showMessage(String msg) {
		return getBoxPanelTabPane().showMessage(msg);
	}

	/**
	 * For objects that happen to be Components, this method
	 * can be used to cause the value to be drawn as a component.
	 */
	public void showObjectGUI(Component value) {
		showObjectGUI(value, value.getClass());
	}

	public void showObjectGUI(Component value, Class clz) {
		Window existing = (Window) objectGUIs.findBrother(value);

		if (existing == null || DisplayContextUIImpl.ALLOW_MULTIPLE_WINDOWS) {
			final ITabUI ui = getBoxPanelTabPane();

			if (value instanceof JInternalFrame) {
				JInternalFrame f = (JInternalFrame) value;
				if (!DisplayContextUIImpl.ALLOW_MULTIPLE_WINDOWS) {
					objectGUIs.add(value, f);
				}
				f.addInternalFrameListener(listener);
				ui.addTab(f.getTitle(), f);
				f.toFront();
				f.show();

			} else if (value instanceof JComponent) {
				JInternalFrame f = new JInternalFrame(value.getName(), true, true, true, true);
				try {
					f.setFrameIcon(POJOBoxImpl.getIcon(POJOBoxImpl.getBeanInfo(clz)));
				} catch (IntrospectionException e) {
				}
				f.getContentPane().add(value);
				if (!DisplayContextUIImpl.ALLOW_MULTIPLE_WINDOWS) {
					objectGUIs.add(value, f);
				}
				f.addInternalFrameListener(listener);
				f.pack();
				ui.addTab(f.getTitle(), f);
				f.toFront();
				f.show();

			} else if (value instanceof Window) {
				Window window = (Window) value;
				if (!DisplayContextUIImpl.ALLOW_MULTIPLE_WINDOWS) {
					objectGUIs.add(value, window);
				}
				window.addWindowListener(listener);
				window.setSize(window.getPreferredSize());
				Utility.centerWindow(window);
				window.show();

			} else {
				JInternalFrame f = new JInternalFrame(value.getName(), true, true, true, true);
				f.getContentPane().add(value);
				f.setSize(f.getPreferredSize());
				if (!DisplayContextUIImpl.ALLOW_MULTIPLE_WINDOWS) {
					objectGUIs.add(value, f);
				}
				f.addInternalFrameListener(listener);
				f.pack();
				//f.setSize(f.getPreferredSize());
				//com.netbreeze.util.Utility.centerWindow(f);
				//f.show();
				ui.addTab(f.getTitle(), f);
				f.toFront();
				f.show();
			}
		} else {
			existing.show();
			existing.toFront();
		}
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
			BoxPanelSwitchableView bpsv = Utility.getBoxPanelTabPane();
			if (bpsv.containsComponent(frame)) {
				bpsv.setSelectedComponent(frame);
				return;
			}
			bpsv.addComponent(frame.getName(), frame, DisplayType.PANEL);
			return;
		}
		Debuggable.notImplemented();
	}

	/**
	 * Opens up a GUI to show the details of the given object
	 */
	public UserResult showScreenBoxAsResult(Object object) {
		if (object instanceof String) {
			return mainGUI.showMessage("RESULT:" + object);
		}
		JPanel pnl = null;
		if (object instanceof Component) {
			Component comp = (Component) object;
			pnl = ComponentHost.asPanel(comp, object);
		} else {
			pnl = objectToPanel(object, true);
		}
		try {
			showScreenBoxGUI(pnl.getName(), object.getClass(), pnl);
		} catch (IntrospectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ScreenBoxImpl.asResult(pnl);
	}

	/**
	 * Opens up a GUI to show the details of the given value
	 */
	@Override public UserResult showScreenBox(Object value) throws Exception {
		return showScreenBox(value, value.getClass());
	}

	public UserResult showScreenBox(Object value, Class trigType) throws Exception {
		return attachChildUI(null, value, trigType);
	}

	public UserResult attachChildUI(String label, Object value) throws Exception {
		return attachChildUI(label, value, value.getClass());
	}

	public UserResult attachChildUI(String label, Object value, Class trigType) throws Exception {
		JInternalFrame existing = (JInternalFrame) objectFrames.findBrother(value);

		if (existing == null || DisplayContextUIImpl.ALLOW_MULTIPLE_WINDOWS) {

			final NamedObjectCollection collection = getLocalBoxedChildren();
			//Get a wrapper for the value, or create a temporary wrapper if necessary
			BT wrapper = collection.findOrCreateBox(value);

			//Get the value info and descriptor
			BeanInfo objectInfo = POJOBoxImpl.getBeanInfo(trigType);

			//Create the GUI for the value
			Component view;
			Class customizerClass = findCustomizerClass(trigType);
			if (customizerClass != null) {
				Customizer customizer = null;
				try {
					customizer = (Customizer) customizerClass.newInstance();
					customizer.setObject(value);
					view = (Component) customizer;
				} catch (Exception e) {
					e.printStackTrace();
					view = new LargeObjectView(value);
				}
			} else {
				view = new LargeObjectView(value);
			}

			//Get an icon for the value
			Icon icon = POJOBoxImpl.getIcon(objectInfo);

			//Create an internal frame to hold the GUI
			JInternalFrame frame = new JInternalFrame(getTitleOf(value), true, true, true, true);
			frame.setResizable(true);

			//Put the GUI and icon in the frame
			frame.setFrameIcon(icon);
			frame.getContentPane().add(view);

			//Make the size correct
			ITabUI desk = getTabUI();
			Dimension preferred = frame.getPreferredSize();
			Dimension deskSize = desk.getPreferredChildSize();
			Dimension size = new Dimension(Math.min(preferred.width, deskSize.width), Math.min(preferred.height, deskSize.height));
			frame.setSize(size);

			//If necessary, add this to the list of value frames
			//to allow reuse of this window if the same value is to be viewed again
			if (!DisplayContextUIImpl.ALLOW_MULTIPLE_WINDOWS) {
				objectFrames.add(value, frame);
			}

			//Listen to the frame, so we notice if it closes
			frame.addInternalFrameListener(listener);

			//Add the frame to the desk and bring it to the front
			desk.addTab(frame.getTitle(), frame);
			frame.toFront();
			frame.show();

		} else {

			//There was an existing GUI. Just bring it to the front
			existing.show();
			existing.toFront();
		}
		if (existing instanceof UserResult) {
			return (UserResult) existing;
		}
		return UserResult.SUCCESS;
	}

	public ITabUI getTabUI() {
		return Utility.theBoxPanelDisplayContext;
	}

	/**
	 * For objects that happen to be Components, this method can be used to
	 * cause the object to be drawn as a component.
	 * @return 
	 * @throws IntrospectionException 
	 */
	private void showScreenBoxGUI(String name, Class objClass, Component object) throws IntrospectionException {
		Window existing = (Window) objectGUIs.findBrother(object);

		if (existing == null || DisplayContextUIImpl.ALLOW_MULTIPLE_WINDOWS) {
			BoxPanelSwitchableView boxPanelDisplayContext = getBoxPanelTabPane();
			if (object instanceof JInternalFrame) {
				JInternalFrame f = (JInternalFrame) object;
				if (!DisplayContextUIImpl.ALLOW_MULTIPLE_WINDOWS) {
					objectGUIs.add(object, f);
				}
				f.addInternalFrameListener(listener);
				boxPanelDisplayContext.addComponent(f.getTitle(), f, DisplayType.FRAME);
				f.toFront();
				f.show();
			} else if (object instanceof JPanel) {
				JPanel f = ComponentHost.asPanel(object, object);
				boxPanelDisplayContext.addComponent(name, f, DisplayType.PANEL);
				return;
			} else if (object instanceof JComponent) {
				JInternalFrame f = new JInternalFrame(name, true, true, true, true);
				f.setFrameIcon(getIcon(Utility.getBeanInfo(objClass)));
				f.getContentPane().add(object);
				if (!DisplayContextUIImpl.ALLOW_MULTIPLE_WINDOWS) {
					objectGUIs.add(object, f);
				}
				f.addInternalFrameListener(listener);
				f.pack();
				boxPanelDisplayContext.addComponent(f.getTitle(), f, DisplayType.FRAME);
				f.toFront();
				f.show();

			} else if (object instanceof Window) {
				Window window = (Window) object;
				if (!DisplayContextUIImpl.ALLOW_MULTIPLE_WINDOWS) {
					objectGUIs.add(object, window);
				}
				window.addWindowListener(listener);
				window.setSize(window.getPreferredSize());
				org.appdapter.gui.api.Utility.centerWindow(window);
				window.show();

			} else {
				JInternalFrame f = new JInternalFrame(name, true, true, true, true);
				f.getContentPane().add(object);
				f.setSize(f.getPreferredSize());
				if (!DisplayContextUIImpl.ALLOW_MULTIPLE_WINDOWS) {
					objectGUIs.add(object, f);
				}
				f.addInternalFrameListener(listener);
				f.pack();
				// f.setSize(f.getPreferredSize());
				// Utility.centerWindow(f);
				// f.show();
				boxPanelDisplayContext.addComponent(f.getTitle(), f, DisplayType.FRAME);
				f.toFront();
				f.show();
			}
		} else {
			existing.show();
			existing.toFront();
		}
	}

	@Override public ITabUI getLocalCollectionUI() {
		Debuggable.notImplemented();
		return null;
	}

}
