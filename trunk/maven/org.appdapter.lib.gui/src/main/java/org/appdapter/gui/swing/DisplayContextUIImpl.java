package org.appdapter.gui.swing;

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
import java.beans.IntrospectionException;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.UserResult;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.BoxPanelSwitchableView;
import org.appdapter.gui.api.BrowserPanelGUI;
import org.appdapter.gui.api.DisplayType;
import org.appdapter.gui.api.IShowObjectMessageAndErrors;
import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.api.POJOCollection;
import org.appdapter.gui.api.POJOCollectionListener;
import org.appdapter.gui.api.WrapperValue;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.editors.RenameDialog;
import org.appdapter.gui.repo.Icons;
import org.appdapter.gui.trigger.AbstractTriggerAction;
import org.appdapter.gui.trigger.TriggerForInstance;
import org.appdapter.gui.util.PairTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

		AddAction(BT box, Object val, NamedObjectCollection noc) {
			super("Copy to " + noc.getName(), Icons.addToCollection);
			super.boxed = box;
			super.currentCollection = noc;
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

		PropertiesAction(BT box, Object object) {
			super("Properties", getIcon("properties"));
			super.boxed = box;
			this.value = object;
		}

		@Override public void actionPerformed(ActionEvent evt) {
			try {
				showScreenBox(Utility.getPropertiesPanel(value));
			} catch (Throwable err) {
				Utility.showError(null, null, err);
			}
		}
	}

	class RemoveAction extends AbstractTriggerAction {

		RemoveAction(BT box, Object object, NamedObjectCollection noc) {
			super("Remove from " + noc.getName(), getIcon("removeFromCollection"));
			this.value = object;
			super.boxed = box;
			currentCollection = noc;
		}

		@Override public void actionPerformed(ActionEvent evt) {
			getCurrentCollection().removeObject(value);
		}
	}

	class RenameAction extends AbstractTriggerAction {

		RenameAction(BT box, Object object, NamedObjectCollection noc) {
			super("Change label in " + noc.getName());
			super.boxed = box;
			this.value = object;
			currentCollection = noc;
		}

		@Override public void actionPerformed(ActionEvent evt) {
			BT wrapper = currentCollection.findBoxByObject(value);
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

	private NamedObjectCollection localCollection;

	//==== Instance variables ================
	IShowObjectMessageAndErrors globalUI;

	Adapter listener = new Adapter();

	// ==== Action classes ====================================

	// ==== Instance variables ================
	BoxPanelSwitchableView tabsUI;

	Hashtable<Box, JPanel> boxPanels = new Hashtable<Box, JPanel>();

	PairTable<Object, Component> objectWindows = new PairTable();

	/**
	 * Creates a new context linked to the given GUI. All operations will use
	 * either the given GUI or the ObjectNavigator that it represents
	 */
	public DisplayContextUIImpl(BoxPanelSwitchableView gui, IShowObjectMessageAndErrors site, NamedObjectCollection col) {
		Utility.controlApp = this;
		this.tabsUI = gui;
		this.localCollection = col;
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
		if (localCollection == null) {
			Debuggable.notImplemented();
		}
		return localCollection;
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
		if (localCollection != null)
			return localCollection;
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

		Object val = object;
		if (val == null) {
			if (box != null) {
				val = box.getValueOrThis();
			} else {

			}
		}
		if (box == null) {
			box = Utility.boxObject(val);
		}

		List actions = new LinkedList();
		if (object == null)
			return actions;

		NamedObjectCollection currentCollection = getCurrentCollection();
		addCollectionActions(box, object, actions, currentCollection);

		NamedObjectCollection clipboardCollection = Utility.getClipboard();
		addCollectionActions(box, object, actions, clipboardCollection);

		if (object instanceof Component) {
			actions.add(new ViewAction(box, (Component) object));
		}
		actions.add(new PropertiesAction(box, object));
		Class cls = val.getClass();
		TriggerForInstance.addClassLevelTriggers(getDisplayContext().getDisplayContext(), cls, actions, (WrapperValue) box);
		return actions;
	}

	private void addCollectionActions(BT box, Object object, List actions, final NamedObjectCollection col) {
		if (col != null) {
			if (col.containsObject(object)) {
				actions.add(new RenameAction(box, object, col));
				actions.add(new RemoveAction(box, object, col));
			} else {
				actions.add(new AddAction(box, object, col));
			}
			actions.add(new AbstractTriggerAction("Global", "Show " + col.getName()) {

				@Override public void actionPerformed(ActionEvent e) {
					try {
						getDisplayContext().showScreenBox(new LargeObjectChooser(null, col));
					} catch (Exception e1) {
						Debuggable.reThrowable(e1);
					}

				}
			});
		}
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
		try {
			showObjectGUI(null, value, true);
		} catch (Throwable e) {
			throw Debuggable.reThrowable(e);
		}
	}

	private void bringToFront(Component existing) {
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

	public UserResult showScreenBox(Object object) {
		return showScreenBoxAsResult(null, object);
	}

	/**
	 * Opens up a GUI to show the details of the given object
	 */
	public UserResult showScreenBoxAsResult(String title, Object object) {
		if (object == null) {
			return Utility.browserPanel.showMessage("RESULT: " + "null");
		}
		if (object instanceof String) {
			return Utility.browserPanel.showMessage("RESULT:" + object);
		}
		if (Utility.isToStringType(object.getClass())) {
			return Utility.browserPanel.showMessage(Utility.getDefaultName(object));
		}
		JPanel pnl = null;
		if (object instanceof Component) {
			Component comp = (Component) object;
			pnl = ComponentHost.asPanel(comp, object);
		} else {
			pnl = objectToPanel(object, true);
		}
		try {
			showObjectGUI(pnl.getName(), pnl, true);
		} catch (IntrospectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Utility.asUserResult(pnl);
	}

	/**
	 * Opens up a GUI to show the details of the given value
	 */
	public UserResult attachChildUI(String label, Object valueIn, boolean showASAP) throws Exception {

		Object value = valueIn;
		if (showASAP && valueIn instanceof Component) {
			showObjectGUI(label, (Component) valueIn, showASAP);
			return Utility.asUserResult(valueIn);
		}
		final NamedObjectCollection collection = getLocalBoxedChildren();
		BT wrapper = collection.findOrCreateBox(label, value);
		value = wrapper.getValueOrThis();
		if (label == null)
			label = wrapper.getUniqueName();
		JPanel view = wrapper.getPropertiesPanel();
		showObjectGUI(label, view, showASAP);
		return Utility.asUserResult(view);
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
	private void showObjectGUI(String name, Component object, boolean showASAP) throws IntrospectionException {
		Component existing = (Component) objectWindows.findBrother(Utility.drefO(object));

		if (existing != null) {
			bringToFront(existing);
			return;
		}

		BoxPanelSwitchableView boxPanelDisplayContext = getBoxPanelTabPane();

		if (object instanceof JInternalFrame) {
			JInternalFrame f = (JInternalFrame) object;
			if (!DisplayContextUIImpl.ALLOW_MULTIPLE_WINDOWS) {
				objectWindows.add(object, f);
			}
			f.addInternalFrameListener(listener);
			boxPanelDisplayContext.addComponent(f.getTitle(), f, DisplayType.FRAME);
			if (showASAP) {
				f.toFront();
				f.show();
			}
		} else if (object instanceof JPanel) {
			JPanel f = ComponentHost.asPanel(object, object);
			boxPanelDisplayContext.addComponent(name, f, DisplayType.PANEL);
			if (showASAP) {
				boxPanelDisplayContext.setSelectedComponent(f);
			}
			return;
		} else if (object instanceof JComponent) {
			JInternalFrame f = new JInternalFrame(name, true, true, true, true);
			f.setFrameIcon(getIcon(Utility.getBeanInfo(Utility.dref(object).getClass())));
			f.getContentPane().add(object);
			if (!DisplayContextUIImpl.ALLOW_MULTIPLE_WINDOWS) {
				objectWindows.add(object, f);
			}
			f.addInternalFrameListener(listener);
			f.pack();
			boxPanelDisplayContext.addComponent(f.getTitle(), f, DisplayType.FRAME);
			if (showASAP) {
				f.toFront();
				f.show();
			}

		} else if (object instanceof Window) {
			Window window = (Window) object;
			if (!DisplayContextUIImpl.ALLOW_MULTIPLE_WINDOWS) {
				objectWindows.add(object, window);
			}
			window.addWindowListener(listener);
			window.setSize(window.getPreferredSize());
			org.appdapter.gui.browse.Utility.centerWindow(window);
			if (showASAP) {
				window.show();
			}

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
			if (showASAP) {
				f.toFront();
				f.show();
			}
		}

	}

}
