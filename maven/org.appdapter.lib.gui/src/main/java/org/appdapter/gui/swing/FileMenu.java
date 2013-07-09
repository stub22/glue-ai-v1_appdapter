package org.appdapter.gui.swing;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Action;

import org.appdapter.gui.swing.CollectionEditorUtil.Settings;

import com.jidesoft.swing.JideMenu;

public class FileMenu extends JideMenu {
	/**
	 * 
	 */
	private final CollectionEditorUtil collectionEditorUtil;
	Vector recentFiles = new Vector();

	FileMenu(CollectionEditorUtil collectionEditorUtil) {
		super("File");
		this.collectionEditorUtil = collectionEditorUtil;
		try {
			addItems();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private void addItems() {
		add(this.collectionEditorUtil.newAction);
		add(this.collectionEditorUtil.openAction);
		addSeparator();
		add(this.collectionEditorUtil.saveAction);
		add(this.collectionEditorUtil.saveAsAction);
		addSeparator();

		recentFiles = new Vector();
		Iterator it = Settings.getRecentFiles();
		while (it.hasNext()) {
			File file = (File) it.next();
			Action a = this.collectionEditorUtil.new RecentFileAction(file);
			recentFiles.addElement(a);
			add(a);
		}
	}

	public void refreshRecentFileList() {
		removeAll();
		addItems();
	}
}