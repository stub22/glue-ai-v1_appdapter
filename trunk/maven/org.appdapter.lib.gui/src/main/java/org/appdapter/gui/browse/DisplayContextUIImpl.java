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
import java.util.Hashtable;
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
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxPanelSwitchableView;
import org.appdapter.api.trigger.BrowserPanelGUI;
import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.api.trigger.DisplayType;
import org.appdapter.api.trigger.IShowObjectMessageAndErrors;
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

import arq.trig;

/**
 * A POJOCollectionContext implementation that uses a ObjectNavigator.
 * 
 * 
 */
public class DisplayContextUIImpl implements BrowserPanelGUI, POJOCollection {

	public BrowserPanelGUI getLocalTreeAPI() {
		return this;
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
				boxPanels.remove(window);
				objectWindows.remove(window);
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
				boxPanels.remove(window);
				objectWindows.remove(window);
				window.dispose();
			}
		}
	}

	class AddAction extends AbstractTriggerAction {
		Object value;

		AddAction(BT box, Object val) {
			super("Add to " + currentCollection.getName(), Icons.addToCollection);
			super.boxed = box;
			this.value = val;
		}

		@Override public void actionPerformed(ActionEvent evt) {
			try {
				currentCollection.findOrCreateBox(value);
			} catch (Exception e) {
				showError(toString(), e);
			}
		}
	}

	class PropertiesAction extends AbstractTriggerAction {
		Object object;

		PropertiesAction(BT box, Object object) {
			super("Properties", getIcon("properties"));
			super.boxed = box;
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

		RemoveAction(BT box, Object object) {
			super("Remove from " + currentCollection.getName(), getIcon("removeFromCollection"));
			this.object = object;
			super.boxed = box;
		}

		@Override public void actionPerformed(ActionEvent evt) {
			getCurrentCollection().removeObject(object);
		}
	}

	class RenameAction extends AbstractTriggerAction {
		Object object;

		RenameAction(BT box, Object object) {
			super("Change label");
			super.boxed = box;
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

		ViewAction(BT box, Object value) {
			super("View", Icons.viewObject);
			super.boxed = box;
			this.value = value;
		}

		@Override public void actionPerformed(ActionEvent evt) {
			showObjectGUI((Component) value);
		}
	}

	// ==== Static variables =================
	private static boolean ALLOW_MULTIPLE_WINDOWS = false;

	// ==== Other public methods =========================

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
	IShowObjectMessageAndErrors globalUI;

	Adapter listener = new Adapter();

	// ==== Action classes ====================================

	// ==== Instance variables ================
	BoxPanelSwitchableView tabsUI;

	Hashtable<Box, JPanel> boxPanels = new Hashtable<Box, JPanel>();

	PairTable<Object, Window> objectWindows = new PairTable();

	/**
	 * Creates a new context linked to the given GUI. All operations will use
	 * either the given GUI or the ObjectNavigator that it represents
	 */
	public DisplayContextUIImpl(BoxPanelSwitchableView gui, IShowObjectMessageAndErrors site, NamedObjectCollection col) {
		Utility.controlApp = this;
		this.tabsUI = gui;
		this.currentCollection = col;
		this.globalUI = site;
		if (site == null) {
			throw new NullPointerException("The DisplayContextSite cannot be null");
		}
		if (col == null) {
			throw new NullPointerException("The NamedObjectCollection cannot be null");
		}
		if (gui == null) {
			throw new NullPointerException("The BoxPanelSwitchableView cannot be null");
		}
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
			adjustChldSize(view);
			return new ComponentHost(view, boxed);
			//BoxPanelSwitchableView bsv = Utility.getBoxPanelTabPane();
			//bsv.add(name, view);
			//return view;
		}

		if (view instanceof JComponent) {
			adjustChldSize(view);
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

	@Override public BoxPanelSwitchableView getBoxPanelTabPane() {
		return Utility.theBoxPanelDisplayContext;
	}

	public NamedObjectCollection getCurrentCollection() {
		if (currentCollection == null) {
			Debuggable.notImplemented();
		}
		return currentCollection;
	}

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
		return Utility.getTreeBoxCollection();
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
	@Override public Collection getTriggersFromUI(BT box, Object object) {
		Collection actions = new LinkedList();
		if (object == null)
			return actions;
		if (currentCollection != null) {
			if (object == null) {

			}
			if (currentCollection.containsObject(object)) {
				actions.add(new RenameAction(box, object));
				actions.add(new RemoveAction(box, object));
			} else {
				actions.add(new AddAction(box, object));
			}
		}
		if (object instanceof Component) {
			actions.add(new ViewAction(box, (Component) object));
		}
		actions.add(new PropertiesAction(box, object));
		return actions;
	}

	private JPanel objectToPanel(Object object, boolean attachToUIAsap) {

		if (object instanceof JPanel) {
			return (JPanel) object;
		}

		if (object == null) {
			return null;
		}
		return getCurrentCollection().findOrCreateBox(object).getPropertiesPanel();
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
	private void adjustChldSize(Component view) {
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
		return Utility.browserPanel.showMessage(msg);
	}

	/**
	 * For objects that happen to be Components, this method
	 * can be used to cause the value to be drawn as a component.
	 */
	public void showObjectGUI(Component value) {
		showObjectGUI(value.getName(), value, value.getClass());
	}

	public void showObjectGUI(String label, Component value, Class clz) {
		final BoxPanelSwitchableView ui = getBoxPanelTabPane();

		Object existing = objectWindows.findBrother(value);
		if (existing != null && !DisplayContextUIImpl.ALLOW_MULTIPLE_WINDOWS) {
			if (existing instanceof Window) {
				Window window = (Window) existing;
				window.setVisible(true);
				window.toFront();
				return;
			}
		}

		if (value instanceof JPanel) {
			JPanel f = (JPanel) value;
			if (!DisplayContextUIImpl.ALLOW_MULTIPLE_WINDOWS) {
				objectWindows.add(value, f);
			}
			if (label == null)
				label = f.getName();
			ui.addTab(label, f);
			ui.setSelectedComponent(f);
			f.show();
			return;
		}

		if (value instanceof JInternalFrame) {
			JInternalFrame f = (JInternalFrame) value;
			if (!DisplayContextUIImpl.ALLOW_MULTIPLE_WINDOWS) {
				objectWindows.add(value, f);
			}
			f.addInternalFrameListener(listener);
			ui.addTab(f.getTitle(), f);
			f.toFront();
			f.show();
			return;

		} else if (value instanceof JComponent) {
			JInternalFrame f = new JInternalFrame(value.getName(), true, true, true, true);
			try {
				f.setFrameIcon(POJOBoxImpl.getIcon(POJOBoxImpl.getBeanInfo(clz)));
			} catch (IntrospectionException e) {
			}
			f.getContentPane().add(value);
			if (!DisplayContextUIImpl.ALLOW_MULTIPLE_WINDOWS) {
				objectWindows.add(value, f);
			}
			f.addInternalFrameListener(listener);
			f.pack();
			ui.addTab(f.getTitle(), f);
			f.toFront();
			f.show();
			return;

		} else if (value instanceof Window) {
			Window window = (Window) value;
			if (!DisplayContextUIImpl.ALLOW_MULTIPLE_WINDOWS) {
				objectWindows.add(value, window);
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
				objectWindows.add(value, f);
			}
			f.addInternalFrameListener(listener);
			f.pack();
			//f.setSize(f.getPreferredSize());
			//com.netbreeze.util.Utility.centerWindow(f);
			//f.show();
			ui.addTab(f.getTitle(), f);
			f.toFront();
			f.show();
			return;
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
			return Utility.browserPanel.showMessage("RESULT:" + object);
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
		return Utility.asUserResult(pnl);
	}

	/**
	 * Opens up a GUI to show the details of the given value
	 */
	@Override public UserResult showScreenBox(Object value) throws Exception {
		return showScreenBox(value, null);
	}

	public UserResult showScreenBox(Object value, Class trigType) throws Exception {
		return attachChildUI(null, value, trigType);
	}

	public UserResult attachChildUI(String label, Object value, boolean showASAP) throws Exception {
		return attachChildUI(label, value, null);
	}

	public UserResult attachChildUI(String label, Object valueIn, Class trigType) throws Exception {

		Object value = valueIn;
		final NamedObjectCollection collection = getLocalBoxedChildren();
		BT wrapper = collection.findOrCreateBox(value);
		value = wrapper.getValueOrThis();
		if (trigType == null) {
			trigType = wrapper.getObjectClass();
		}
		if (label==null) label = wrapper.getUniqueName();
		JPanel view = wrapper.getPropertiesPanel();
		showObjectGUI(label, view, trigType);
		return UserResult.SUCCESS;
	}

	public BoxPanelSwitchableView getTabUI() {
		return Utility.theBoxPanelDisplayContext;
	}

	/**
	 * For objects that happen to be Components, this method can be used to
	 * cause the object to be drawn as a component.
	 * @return 
	 * @throws IntrospectionException 
	 */
	private void showScreenBoxGUI(String name, Class objClass, Component object) throws IntrospectionException {
		Window existing = (Window) objectWindows.findBrother(object);

		if (existing == null || DisplayContextUIImpl.ALLOW_MULTIPLE_WINDOWS) {
			BoxPanelSwitchableView boxPanelDisplayContext = getBoxPanelTabPane();

			if (object instanceof JInternalFrame) {
				JInternalFrame f = (JInternalFrame) object;
				if (!DisplayContextUIImpl.ALLOW_MULTIPLE_WINDOWS) {
					objectWindows.add(object, f);
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
					objectWindows.add(object, f);
				}
				f.addInternalFrameListener(listener);
				f.pack();
				boxPanelDisplayContext.addComponent(f.getTitle(), f, DisplayType.FRAME);
				f.toFront();
				f.show();

			} else if (object instanceof Window) {
				Window window = (Window) object;
				if (!DisplayContextUIImpl.ALLOW_MULTIPLE_WINDOWS) {
					objectWindows.add(object, window);
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
					objectWindows.add(object, f);
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

}
