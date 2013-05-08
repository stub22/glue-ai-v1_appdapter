package org.appdapter.gui.objbrowser;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Icons {
	static Logger theLogger = LoggerFactory.getLogger(Icon.class);

	static Icon saveCollection = new DummyIcon();
	static Icon saveCollectionAs = new DummyIcon();
	static Icon newCollection = new DummyIcon();
	static Icon openCollection = new DummyIcon();
	static Icon recentFile = new DummyIcon();
	static Icon search = new DummyIcon();

	static Icon addToCollection = new DummyIcon();
	static Icon removeFromCollection = new DummyIcon();
	static Icon viewBean = new DummyIcon();
	static Icon properties = new DummyIcon();

	static Icon mainFrame = new DummyIcon();

	private Icons() {
	}

	static {
		saveCollection = loadIcon("saveCollection.gif");
		saveCollectionAs = loadIcon("saveCollectionAs.gif");
		newCollection = loadIcon("newCollection.gif");
		openCollection = loadIcon("openCollection.gif");
		recentFile = loadIcon("recentFile.gif");
		search = loadIcon("search.gif");
		addToCollection = loadIcon("addToCollection.gif");
		removeFromCollection = loadIcon("removeFromCollection.gif");
		viewBean = loadIcon("viewObject.gif");
		properties = loadIcon("properties.gif");
		mainFrame = loadIcon("mainFrame.gif");
	}

	static Image loadImage(String filename) {
		Toolkit tk = Toolkit.getDefaultToolkit();
		Image i = null;
		try {
			i = tk.getImage(Icons.class.getResource("icons/" + filename));
		} catch (Exception err) {
			theLogger.warn("Warning - icon '" + filename + "' could not be loaded: "
					+ err, err);
		}
		return i;
	}

	static Icon loadIcon(String filename) {
		try {
			Object r = Icons.class.getResource(".");
			return new ImageIcon(Icons.class.getResource("icons/" + filename));
		} catch (Exception err) {
			theLogger.warn("Warning - icon '" + filename + "' could not be loaded: "
					+ err, err);
			return new DummyIcon();
		}
	}

	static class DummyIcon implements Icon, java.io.Serializable {
		public int getIconWidth() {
			return 16;
		}

		public int getIconHeight() {
			return 16;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(Color.blue);
			g.setFont(new Font("serif", Font.BOLD, 12));
			g.drawString("?", x, y + 12);
		}
	}

}