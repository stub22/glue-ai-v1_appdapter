package org.appdapter.gui.pojo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.appdapter.gui.box.BoxPanelSwitchableView;
import org.appdapter.gui.box.ScreenBoxImpl;
import org.appdapter.gui.box.ScreenBoxPanel;
import org.appdapter.gui.browse.BrowsePanel;
import org.appdapter.gui.browse.DisplayContext;
import org.appdapter.gui.swing.POJOAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * A POJOCollectionContext implementation that uses a ObjectNavigator.
 * 
 * 
 */
public class BrowsePanelControlApp implements DisplayContext, POJOAppContext {
	abstract class AbstractActionTrigger extends AbstractAction {

		public AbstractActionTrigger(String name) {
			super(name);
		}

		public AbstractActionTrigger(String name, Icon icon) {
			super(name, icon);
		}

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

	class AddAction extends AbstractActionTrigger {
		Object object;

		AddAction(Object object) {
			super("Add to collection", getIcon("addToCollection"));

			this.object = object;
		}

		@Override public void actionPerformed(ActionEvent evt) {
			getCurrentCollection().addObject(object);
		}
	}

	class PropertiesAction extends AbstractActionTrigger {
		Object object;

		PropertiesAction(Object object) {
			super("Properties", getIcon("properties"));
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

	class RemoveAction extends AbstractActionTrigger {
		Object object;

		RemoveAction(Object object) {
			super("Remove from collection", getIcon("removeFromCollection"));
			this.object = object;
		}

		@Override public void actionPerformed(ActionEvent evt) {
			getBoxPanelTabPane().removeObject(object);
		}
	}

	class RenameAction extends AbstractActionTrigger {
		Object object;

		RenameAction(Object object) {
			super("Change name");
			this.object = object;
		}

		@Override public void actionPerformed(ActionEvent evt) {
			POJOBox wrapper = Utility.addObject(object);
			if (wrapper != null) {
				RenameDialog dialog = new RenameDialog(BrowsePanelControlApp.this, wrapper);
				dialog.show();
			}
		}
	}

	public class RenameDialog extends JFrame {
		public JButton cancelButton = new JButton("Cancel");
		POJOAppContext context;
		public JTextField nameField = new JTextField(10);
		POJOBox object;
		public JButton okButton = new JButton("OK");

		public RenameDialog(POJOAppContext context, POJOBox object) {
			super("Rename");
			this.context = context;
			setIconImage(getImage("mainFrame.gif"));
			this.object = object;

			JPanel top = new JPanel(new FlowLayout());
			top.add(new JLabel("Rename " + object.getUniqueName() + " to: "));
			top.add(nameField);
			nameField.setText(object.getUniqueName());
			nameField.selectAll();

			JPanel bottom = new JPanel(new FlowLayout());
			bottom.add(cancelButton);
			bottom.add(okButton);

			getContentPane().setLayout(new BorderLayout());

			Box box = new Box(BoxLayout.Y_AXIS);
			box.add(top);
			box.add(bottom);
			getContentPane().add("Center", box);
			pack();
			org.appdapter.gui.pojo.Utility.centerWindow(this);

			nameField.getDocument().addDocumentListener(new DocumentListener() {
				@Override public void changedUpdate(DocumentEvent evt) {
					checkControls();
				}

				@Override public void insertUpdate(DocumentEvent evt) {
					checkControls();
				}

				@Override public void removeUpdate(DocumentEvent evt) {
					checkControls();
				}
			});

			nameField.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent evt) {
					okPressed();
				}
			});

			cancelButton.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent evt) {
					dispose();
				}
			});

			okButton.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent evt) {
					okPressed();
				}
			});

			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		}

		private void checkControls() {
			String newName = nameField.getText();
			okButton.setEnabled(isNameValid(newName));
		}

		private synchronized boolean isNameValid(String n) {
			if (n == null || n.equals("")) {
				return false;
			} else {
				return context.getNamedObjectCollection().findBoxByName(n) == null;
			}
		}

		private void okPressed() {
			String name = nameField.getText();
			if (isNameValid(name)) {
				try {
					object.setUniqueName(name);
					context.getNamedObjectCollection().reload();
				} catch (Exception err) {
					context.showError(null, err);
				}
				dispose();
			} else {
				context.showError("Invalid name - there is already another object named '" + name + "'", null);
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

	class ViewAction extends AbstractActionTrigger {
		Object object;

		ViewAction(Object object) {
			super("View", getIcon("viewBean"));
			this.object = object;
		}

		@Override public void actionPerformed(ActionEvent evt) {
			showScreenBox(object);
		}
	}

	// ==== Static variables =================
	private static boolean ALLOW_MULTIPLE_WINDOWS = false;

	// ==== Constructors ===================

	static int countOF = 0;

	private static Logger theLogger = LoggerFactory.getLogger(BrowsePanelControlApp.class);

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

	public static ImageIcon getIcon(String string) {
		return new ImageIcon(getImage(string));
	}

	public static Image getImage(String string) {
		try {
			return ImageIO.read(new File(string));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	Object classBrowser_Unused = null;

	// ==== Instance variables ================
	BrowsePanel mainGUI;

	// ==== Other public methods =========================

	Adapter listener = new Adapter();

	PairTable<Object, JComponent> objectFrames = new PairTable();

	PairTable objectGUIs = new PairTable();

	/**
	 * Creates a new context linked to the given GUI. All operations will use
	 * either the given GUI or the ObjectNavigator that it represents
	 */
	public BrowsePanelControlApp(BrowsePanel gui) {
		Utility.controlApp = this;
		if (countOF > 0) {
			throw new NullPointerException("Tryin to make too many ScreenBoxedPOJOCollectionContextWithNavigator!");
		}
		this.mainGUI = gui;
		if (gui == null) {
			throw new NullPointerException("The ObjectNavigator GUI cannot be null for a POJOCollectionContext");
		}
		countOF++;
	}

	/**

	 * @param wrapper
	 * @param view
	 * @return 
	 */
	private ScreenBoxPanel asPanel(String name, Class c, Component view) {

		if (view instanceof ScreenBoxPanel) {
			return (ScreenBoxPanel) view;
		}

		if (view instanceof JPanel) {
			setPanelSize((JPanel) view);
			return new ComponentHost(view);
			//BoxPanelSwitchableView bsv = Utility.getBoxPanelTabPane();
			//bsv.add(name, view);
			//return view;
		}
		return new ComponentHost(view);
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

	/**
	 * @param m_obj
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

	public BrowsePanel getGUI() {
		return mainGUI;
	}

	/**
	 * Returns all actions that can be carried out on the given object
	 */
	public Collection getTriggersFromUI(Object object) {
		Collection actions = new LinkedList();
		if (getCurrentCollection().containsObject(object)) {
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

	// ==== Action classes ====================================

	public void reload() {
		throw new NotImplementedException();

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

	public void showError(String msg, Throwable error) {
		try {
			if (error == null) {
				new org.appdapter.gui.swing.ErrorDialog(msg, error).show();
			} else {
				showScreenBox(error); // @temp
			}
		} catch (Throwable err) {
			new org.appdapter.gui.swing.ErrorDialog("A new error occurred while trying to display the original error '" + error + "'!", err).show();
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
			Utility.boxPanelDisplayContext.setSelectedComponent(frame);
			return;
		}
	}

	/**
	 * Opens up a GUI to show the details of the given object
	 */
	public ScreenBoxPanel showScreenBox(Object object) {
		if (false && object instanceof Component) {
			Component comp = (Component) object;
			try {
				return showScreenBoxGUI(comp.getName(), comp.getClass(), comp);

			} catch (IntrospectionException e) {
			}
		}
		if (object instanceof String) {
			return mainGUI.showMessage("RESULT:" + object);
		}
		Component existing = showScreenBox(object, true);
		showPanel(existing);
		return (ScreenBoxPanel) existing;
	}

	// ===== Event adapter classes ==================================

	public ScreenBoxPanel showScreenBox(Object object, boolean attachToUIAsap) {
		if (object instanceof ScreenBoxPanel)
			return (ScreenBoxPanel) object;

		if (object == null)
			return (ScreenBoxPanel) null;

		ScreenBoxPanel existing = (ScreenBoxPanel) objectFrames.findBrother(object);
		String name = null;
		if (existing == null || ALLOW_MULTIPLE_WINDOWS) {

			// Get a wrapper for the object, or create a temporary wrapper if
			// necessary
			POJOBox wrapper = getCurrentCollection().addObject(object);
			if (wrapper == null) {
				wrapper = new ScreenBoxImpl(null, object);
			}
			object = wrapper.getValue();

			Class objClass = wrapper.getPOJOClass();

			// Get the object info and descriptor
			//BeanInfo objectInfo = wrapper.getBeanInfo();

			// Create the GUI for the object
			Component view;
			view = Utility.getPropertiesPanel(object);
			if (name == null)
				name = getTitleOf(object);
			existing.setName(name);
			if (!attachToUIAsap)
				return existing;
			existing = asPanel(name, objClass, view);
			// If necessary, add this to the list of object frames
			// to allow reuse of this window if the same object is to be viewed
			// again
			if (!ALLOW_MULTIPLE_WINDOWS) {
				objectFrames.add(object, existing);
			}
		} else {
			if (!attachToUIAsap)
				return existing;
			if (name == null)
				name = getTitleOf(object);
			existing = asPanel(name, object.getClass(), existing);
		}
		return existing;
	}

	private String getTitleOf(Object object) {
		// TODO Auto-generated method stub
		return Utility.boxPanelDisplayContext.getTitleOf(object);
	}

	public POJOCollection getCurrentCollection() {
		return Utility.boxPanelDisplayContext;
	}

	/**
	 * For objects that happen to be Components, this method can be used to
	 * cause the object to be drawn as a component.
	 * @return 
	 * @throws IntrospectionException 
	 */
	private ScreenBoxPanel showScreenBoxGUI(String name, Class objClass, Component object) throws IntrospectionException {
		Window existing = (Window) objectGUIs.findBrother(object);

		if (existing == null || ALLOW_MULTIPLE_WINDOWS) {

			if (object instanceof JInternalFrame) {
				JInternalFrame f = (JInternalFrame) object;
				if (!ALLOW_MULTIPLE_WINDOWS)
					objectGUIs.add(object, f);
				f.addInternalFrameListener(listener);
				Utility.boxPanelDisplayContext.addComponent(f.getTitle(), f, DisplayType.FRAME);
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
				Utility.boxPanelDisplayContext.addComponent(f.getTitle(), f, DisplayType.FRAME);
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
				Utility.boxPanelDisplayContext.addComponent(f.getTitle(), f, DisplayType.FRAME);
				f.toFront();
				f.show();
			}
		} else {
			existing.show();
			existing.toFront();
		}
		return null;
	}

	@Override public BoxPanelSwitchableView getBoxPanelTabPane() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public Component getComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public NamedObjectCollection getNamedObjectCollection() {
		return (NamedObjectCollection) getCurrentCollection();
	}

}
