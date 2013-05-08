package org.appdapter.gui.objbrowser;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Event;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.NotSerializableException;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import org.appdapter.gui.objbrowser.model.POJOCollection;
import org.appdapter.gui.objbrowser.model.POJOCollectionImpl;
import org.appdapter.gui.objbrowser.model.Settings;
import org.appdapter.gui.objbrowser.model.Utility;
import org.appdapter.gui.swing.TriggersMenu;
import org.appdapter.gui.swing.impl.JBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The top-level Test for the POJOCollection code. It also contains the
 * main(...) method.
 * 
 * 
 */
@SuppressWarnings("serial")
public class ObjectNavigator extends JFrame implements PropertyChangeListener {

	static public class AsApplet extends JApplet {
		public void init() {
			JBox box = new JBox(BoxLayout.Y_AXIS);
			getContentPane().setLayout(new BorderLayout());
			getContentPane().add("Center", box);

			try {
				// setLayout(new BorderLayout());
				// add("Center", new POJOCollectionPanel());

				box.add(new JLabel("Opening ObjectNavigator in a new window..."));
				(new ObjectNavigator()).show();
				// setVisible(false);
				// setSize(0, 0);
			} catch (Exception err) {
				JTextArea text = new JTextArea();
				text.setEditable(false);
				text.setText("Darn, an error occurred!\nPlease email this to henrik@kniberg.com, thanks!\n\n"
						+ err.toString());
				box.add(text);
			}
		}
	}

	// ==== Static variables ===========
	static ObjectNavigator defaultFrame = null;
	static Logger theLogger = LoggerFactory.getLogger(ObjectNavigator.class);

	// ==== Instance variables ==========
	POJOCollection collection;
	ScreenBoxedPOJOCollectionContextWithNavigator context;

	// The currently opened ObjectNavigator file (may be null)
	File file = null;

	// ==== GUI elements ===================
	JMenuBar menuBar;
	FileMenu fileMenu;
	POJOCollectionViewPanelWithContext panel;
	JToolBar toolbar;
	// JButton aboutButton;
	TriggersMenu selectedMenu;

	// ==== Actions =============================
	Action saveAction = new SaveAction();
	Action openAction = new OpenAction();
	Action saveAsAction = new SaveAsAction();
	Action newAction = new NewAction();
	Action searchAction = new SearchAction();

	// ==== Main method ==========================
	public static void main(String[] args) {
	
		theLogger.info("Starting ObjectNavigator...");

		try {
			ObjectNavigator frame = new ObjectNavigator();
			Utility.setInstancesOfObjects(frame.getChildCollectionWithContext());
			// frame.pack();

			frame.setSize(800, 600);
			org.appdapter.gui.objbrowser.model.Utility.centerWindow(frame);
			frame.show();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			theLogger.info("ObjectNavigator is now running!");
		} catch (Exception err) {
			theLogger.error("ObjectNavigator could not be started", err);
		}
	}

	// ======== Constructors =============================0

	/**
	 * Creates a new ObjectNavigator that shows the given collection
	 */
	public ObjectNavigator(POJOCollection collection) {
		String version = ObjectNavigator.class.getPackage()
				.getImplementationVersion();
		if (version == null) {
			setTitle("ObjectNavigator");
		} else {
			setTitle("ObjectNavigator version " + version);
		}

		try {
			setIconImage(Icons.loadImage("mainFrame.gif"));
		} catch (Throwable err) {
		}
		this.context = new ScreenBoxedPOJOCollectionContextWithNavigator(this);
		setPOJOs(collection);
	}

	/**
	 * Creates a new ObjectNavigator that shows a new POJOCollection
	 */
	public ObjectNavigator() {
		this(new POJOCollectionImpl());
	}

	// ====== Property getters ==============

	public POJOCollectionViewPanelWithContext getPOJOCollectionPanel() {
		return panel;
	}

	public JDesktopPane getDesk() {
		return panel.getDesk();
	}

	public static ObjectNavigator getDefaultFrame() {
		return defaultFrame;
	}

	public ScreenBoxedPOJOCollectionContextWithNavigator getChildCollectionWithContext() {
		return context;
	}

	/**
	 * The current ObjectNavigator being displayed
	 */
	public POJOCollection getDisplayedCollection() {
		return collection;
	}

	/**
	 * Sets the collection to be displayed
	 */
	private void setPOJOs(POJOCollection newCollection) {
		POJOCollection oldCollection = collection;
		if (newCollection != oldCollection) {
			this.collection = newCollection;
			this.panel = new POJOCollectionViewPanelWithContext(context);
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

	// ==== Property notification methods ===============

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == collection) {
			if (evt.getPropertyName().equals("selected")) {
				updateSelectedMenu();
			}
		}
	}

	protected void processEvent(AWTEvent e) {
		if (e.getID() == Event.WINDOW_DESTROY) {
			theLogger.info("Shutting down ObjectNavigator...");
			try {
				Settings.saveToFile();
			} catch (Exception err) {
				theLogger.warn(
						"Warning - failed to save settings: "
								+ err.getMessage(), err);
			}
			removeAll();
			dispose();
			theLogger.info("ObjectNavigator is now shut down!");
		}
		super.processEvent(e);
	}

	// ==== Action execution methods =======================
	private void makeRepoNav() {
		try {
			Class.forName("org.cogchar.gui.demo.RepoNavigator")
					.getMethod("mainly_not_here", new Class[] { String.class })
					.invoke(new String[0]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	void openCollection() {
		FileDialog dialog = new FileDialog(this, "Load ObjectNavigator",
				FileDialog.LOAD);
		dialog.show();
		String fileName = dialog.getFile();
		String directory = dialog.getDirectory();
		if (fileName != null) {
			openCollection(new File(directory, fileName));
		}
	}

	void openCollection(File file) {
		if (file.exists()) {
			try {
				setPOJOs(POJOCollectionImpl.load(file));
				Settings.addRecentFile(file);
				fileMenu.refreshRecentFileList();
			} catch (Exception err) {
				context.showError("Opening failed", err);
			}
		} else {
			context.showError("File does not exist: " + file.getPath(), null);
		}
	}

	void newCollection() {
		// @feature ask save changes?
		setPOJOs(new POJOCollectionImpl());
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
		FileDialog dialog = new FileDialog(this, "Save ObjectNavigator",
				FileDialog.SAVE);
		dialog.setFile("mycollection.ser");
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
		// if (file.exists()) {
		this.file = file;
		try {
			collection.save(file);
		} catch (NotSerializableException err) {
			context.showError(
					"This collection contains an unserializable object", err);
		} catch (Exception err) {
			context.showError("Saving failed", err);
		}
		checkControls();
		// } else {
		// showError("File does not exist: " + file.getPath());
		// }
	}

	// ==== Private methods ===================

	private void updateSelectedMenu() {
		if (selectedMenu != null) {
			menuBar.remove(selectedMenu);
			selectedMenu = null;
		}

		Object selected = collection.getSelectedBean();
		if (selected != null) {
			selectedMenu = new TriggersMenu(selected);
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
	 * Creates and initialized the GUI components within the ObjectNavigator.
	 * Should only be called once.
	 */
	private void initGUI() {
		if (defaultFrame == null) {
			defaultFrame = this;
		}
		getContentPane().setLayout(new BorderLayout());
		panel = new POJOCollectionViewPanelWithContext(context);

		menuBar = new JMenuBar();
		fileMenu = new FileMenu();
		menuBar.add(fileMenu);
		setJMenuBar(menuBar);

		toolbar = new MyToolBar();
		toolbar.setFloatable(true);

		// JPanel northPanel = new JPanel();
		// northPanel.setLayout(new BorderLayout());
		// northPanel.add("Center", toolbar);

		// aboutButton = new ActionButton(aboutAction);
		// northPanel.add("East", aboutButton);

		getContentPane().add("Center", panel);
		getContentPane().add("North", toolbar);
		checkControls();
	}

	// ==== Action classes =================================

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

	class SearchAction extends AbstractAction {
		SearchAction() {
			super("Search ObjectNavigator...", Icons.search);
		}

		public void actionPerformed(ActionEvent evt) {
			makeRepoNav();
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

	// ==== GUI component inner classes ===========

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
			add(searchAction);
		}
	}
}
