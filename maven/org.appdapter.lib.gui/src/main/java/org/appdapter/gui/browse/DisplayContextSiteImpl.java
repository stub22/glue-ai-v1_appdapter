package org.appdapter.gui.browse;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Event;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.NotSerializableException;
import java.util.Iterator;
import java.util.Vector;

import javax.accessibility.Accessible;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import org.appdapter.api.trigger.BrowserPanelGUI;
import org.appdapter.api.trigger.IShowObjectMessageAndErrors;
import org.appdapter.api.trigger.ITabUI;
import org.appdapter.api.trigger.NamedObjectCollection;
import org.appdapter.gui.api.NamedObjectCollectionImpl;
import org.appdapter.gui.api.Settings;
import org.appdapter.gui.api.Utility;
import org.appdapter.gui.impl.Icons;
import org.appdapter.gui.impl.JJPanel;
import org.appdapter.gui.rimpl.TriggerMenu;
import org.appdapter.gui.swing.ImageDisplayField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The top-level GUI for the Collection application.
 * It also contains the main(...) method.
 *
 * 
 */
public class DisplayContextSiteImpl extends JJPanel

implements PropertyChangeListener, WindowConstants, Accessible, IShowObjectMessageAndErrors {
	//==== Static variables ===========
	static JFrame defaultFrame = null;
	private static Logger theLogger = LoggerFactory.getLogger(BrowserPanelGUI.class);

	//==== Instance variables ==========
	NamedObjectCollection namedObjects = null;
	BrowserPanelGUI context;

	//The currently opened POJOCollection file (may be null)
	File file = null;

	//==== GUI elements ===================

	JMenuBar menuBar;
	FileMenu fileMenu;
	ITabUI panel;
	JToolBar toolbar;
	//JButton aboutButton;
	TriggerMenu selectedMenu;

	//==== Actions =============================
	Action saveAction = new SaveAction();
	Action openAction = new OpenAction();
	Action saveAsAction = new SaveAsAction();
	Action newAction = new NewAction();
	Action aboutAction = new AboutAction();

	//==== Main method ==========================
	public static void main(String[] args) {
		theLogger.info("Starting POJOCollection...");

		//	SplashWindow splash = new SplashWindow(Icons.loadIcon("splash.jpg"));
		//splash.show();

		try {
			defaultFrame = new JFrame();
			DisplayContextSiteImpl guiFrame = new DisplayContextSiteImpl();
			Utility.setDisplayContext(guiFrame.getDisplayContext());
			JFrame frame = defaultFrame;
			//frame.pack();
			frame.setSize(800, 600);
			Utility.centerWindow(frame);
			frame.show();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			theLogger.info("POJOCollection is now running!");
		} catch (Exception err) {
			theLogger.error("POJOCollection could not be started", err);
		}

		//splash.dispose();
	}

	//======== Constructors =============================0

	/**
	 * Creates a new DisplayContextSite that shows the given namedObjects
	 */
	public DisplayContextSiteImpl(NamedObjectCollection namedObjects) {
		setupJFrame(this);
		this.context = new DisplayContextUIImpl(null, this, namedObjects);
		setCollection(namedObjects);
	}

	static void setupJFrame(JPanel thiz) {
		String version = BrowserPanelGUI.class.getPackage().getImplementationVersion();
		if (version == null) {
			defaultFrame.setTitle("NamedObjects");
		} else {
			defaultFrame.setTitle("NamedObjects version " + version);
		}

		try {
			defaultFrame.setIconImage(Icons.loadImage("mainFrame.gif"));
		} catch (Throwable err) {
		}
		defaultFrame.setContentPane(thiz);
	}

	/**
	 * Creates a new DisplayContextSite that shows a new Collection
	 */
	public DisplayContextSiteImpl() {
		this(new NamedObjectCollectionImpl());
	}

	//====== Property getters ==============

	public ITabUI getLocalCollectionUI() {
		return panel;
	}

	public static JFrame getDefaultFrame() {
		return defaultFrame;
	}

	public BrowserPanelGUI getDisplayContext() {
		return context;
	}

	/**
	 * The current values being displayed
	 */
	public NamedObjectCollection getLocalBoxedChildren() {
		return namedObjects;
	}

	/**
	 * Sets the namedObjects to be displayed
	 */
	private void setCollection(NamedObjectCollection newCollection) {
		NamedObjectCollection oldCollection = namedObjects;
		if (newCollection != oldCollection) {
			this.namedObjects = newCollection;
			//this.context = new DisplayContext(this);
			this.panel = new WithDesktopObjectBrowserTab(context);
			getContentPane().removeAll();
			initGUI();
			invalidate();
			validate();
			if (oldCollection != null)
				oldCollection.removePropertyChangeListener(this);
			if (newCollection != null)
				newCollection.addPropertyChangeListener(this);
			updateSelectedMenu();
		}
	}

	//==== Property notification methods ===============

	public Container getContentPane() {
		return this;//defaultFrame.getContentPane();
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == namedObjects) {
			if (evt.getPropertyName().equals("selected")) {
				updateSelectedMenu();
			}
		}
	}

	protected void processEvent(AWTEvent e) {
		if (e.getID() == Event.WINDOW_DESTROY) {
			theLogger.info("Shutting down POJOCollection...");
			try {
				Settings.saveToFile();
			} catch (Exception err) {
				theLogger.warn("Warning - failed to save settings: " + err.getMessage(), err);
			}
			removeAll();
			defaultFrame.removeAll();
			defaultFrame.dispose();
			theLogger.info("POJOCollection is now shut down!");
		}
		defaultFrame.dispatchEvent(e);
	}

	//==== Action execution methods =======================

	void openCollection() {
		FileDialog dialog = new FileDialog(defaultFrame, "Load POJOCollection", FileDialog.LOAD);
		dialog.show();
		String fileName = dialog.getFile();
		String directory = dialog.getDirectory();
		if (fileName != null) {
			openCollection(new File(directory, fileName));
		}
	}

	void openCollection(File file1) {
		if (file1.exists()) {
			try {
				setCollection(NamedObjectCollectionImpl.load(file1));
				Settings.addRecentFile(file1);
				fileMenu.refreshRecentFileList();
			} catch (Exception err) {
				context.showError("Opening failed", err);
			}
		} else {
			context.showError("File does not exist: " + file1.getPath(), null);
		}
	}

	void newCollection() {
		//@feature ask save changes?
		setCollection(new NamedObjectCollectionImpl());
		file = null;
		checkControls();
	}

	void saveCollection() {
		if (file == null) {
			saveCollectionAs();
		} else {
			saveCollection(file);
		}
	}

	void saveCollectionAs() {
		FileDialog dialog = new FileDialog(defaultFrame, "Save POJOCollection", FileDialog.SAVE);
		dialog.setFile("mynamedObjects.ser");
		dialog.show();
		String fileName = dialog.getFile();
		String directory = dialog.getDirectory();
		theLogger.debug("fileName = " + fileName);
		theLogger.debug("directory = " + directory);
		if (fileName != null) {
			saveCollection(new File(directory, fileName));
		}
	}

	void saveCollection(File file) {
		theLogger.debug("saveCollection(" + file.getAbsoluteFile() + ")");
		//if (file.exists()) {
		this.file = file;
		try {
			namedObjects.save(file);
		} catch (NotSerializableException err) {
			context.showError("This namedObjects contains an unserializable object", err);
		} catch (Exception err) {
			context.showError("Saving failed", err);
		}
		checkControls();
		//} else {
		//  showError("File does not exist: " + file.getPath());
		//}
	}

	//==== Private methods ===================

	private void updateSelectedMenu() {
		if (selectedMenu != null) {
			menuBar.remove(selectedMenu);
			selectedMenu = null;
		}

		Object selected = namedObjects.getSelectedObject();
		if (selected != null) {
			selectedMenu = new TriggerMenu(null, null, selected);
			menuBar.add(selectedMenu);
		}
		invalidate();
		validate();
		repaint();
	}

	void checkControls() {
		saveAction.setEnabled(file != null);
	}

	/**
	 * Creates and initialized the GUI components
	 * within the DisplayContextSite. Should only be called once.
	 */
	private void initGUI() {
		if (defaultFrame == null) {
			defaultFrame = getDefaultFrame();
		}
		getContentPane().setLayout(new BorderLayout());
		panel = new WithDesktopObjectBrowserTab(context);

		menuBar = new JMenuBar();
		fileMenu = new FileMenu();
		menuBar.add(fileMenu);
		defaultFrame.setJMenuBar(menuBar);

		toolbar = new MyToolBar();
		toolbar.setFloatable(true);

		//JPanel northPanel = new JPanel();
		//northPanel.setLayout(new BorderLayout());
		//northPanel.add("Center", toolbar);

		//aboutButton = new ActionButton(aboutAction);
		//northPanel.add("East", aboutButton);

		add("Center", (Component) panel);
		add("North", toolbar);
		checkControls();
	}

	//==== Action classes =================================

	class SaveAction extends AbstractAction {
		SaveAction() {
			super("Save", Icons.saveCollection);
		}

		public void actionPerformed(ActionEvent evt) {
			saveCollection();
		}
	}

	class SaveAsAction extends AbstractAction {
		SaveAsAction() {
			super("Save as...", Icons.saveCollectionAs);
		}

		public void actionPerformed(ActionEvent evt) {
			saveCollectionAs();
		}
	}

	class NewAction extends AbstractAction {
		NewAction() {
			super("New", Icons.newCollection);
		}

		public void actionPerformed(ActionEvent evt) {
			newCollection();
		}
	}

	class OpenAction extends AbstractAction {
		OpenAction() {
			super("Open", Icons.openCollection);
		}

		public void actionPerformed(ActionEvent evt) {
			openCollection();
		}
	}

	class AboutAction extends AbstractAction {
		AboutAction() {
			super("Search...", Icons.search);
		}

		public void actionPerformed(ActionEvent evt) {
			if (true)
				return;
			setEnabled(false);
			ImageDisplayField splash = new ImageDisplayField(Icons.loadIcon("splash.jpg"), true);
			splash.show();
			splash.addWindowListener(new WindowAdapter() {
				public void windowClosed(WindowEvent e) {
					setEnabled(true);
				}
			});
		}
	}

	class RecentFileAction extends AbstractAction {
		File file;

		RecentFileAction(File file) {
			super(file.getName(), Icons.recentFile);
			this.file = file;
		}

		public void actionPerformed(ActionEvent evt) {
			openCollection(file);
		}
	}

	//==== GUI component inner classes ===========

	class FileMenu extends JMenu {
		Vector recentFiles = new Vector();

		FileMenu() {
			super("File");
			addItems();
		}

		private void addItems() {
			add(newAction);
			add(openAction);
			addSeparator();
			add(saveAction);
			add(saveAsAction);
			addSeparator();

			recentFiles = new Vector();
			Iterator it = Settings.getRecentFiles();
			while (it.hasNext()) {
				File file = (File) it.next();
				Action a = new RecentFileAction(file);
				recentFiles.addElement(a);
				add(a);
			}
		}

		public void refreshRecentFileList() {
			removeAll();
			addItems();
		}
	}

	class MyToolBar extends JToolBar {
		MyToolBar() {
			super();
			add(newAction);
			add(openAction);
			addSeparator();
			add(saveAction);
			add(saveAsAction);
			addSeparator();
			add(aboutAction);
		}
	}

	static public class CollectionApplet extends JApplet {
		public void init() {
			Box box = new Box(BoxLayout.Y_AXIS);
			getContentPane().setLayout(new BorderLayout());
			getContentPane().add("Center", box);

			try {
				//setLayout(new BorderLayout());
				//add("Center", new CollectionPanel());

				box.add(new JLabel("Opening Collection in a new window..."));
				(new DisplayContextSiteImpl()).show();
				//setVisible(false);
				//setSize(0, 0);
			} catch (Exception err) {
				JTextArea text = new JTextArea();
				text.setEditable(false);
				text.setText("Darn, an error occurred!\nPlease email this to henrik@kniberg.com, thanks!\n\n" + err.toString());
				box.add(text);
			}
		}
	}
}
