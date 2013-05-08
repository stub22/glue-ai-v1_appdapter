package org.appdapter.gui.objbrowser;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import org.appdapter.gui.objbrowser.model.POJOCollectionListener;
import org.appdapter.gui.objbrowser.model.POJOSwizzler;
import org.appdapter.gui.pojo.ScreenBoxedPOJO;
import org.appdapter.gui.pojo.ScreenBoxedPOJORef;
import org.appdapter.gui.swing.VerticalLayout;
import org.appdapter.gui.swing.impl.JJPanel;

/**
 * A GUI component showing the list of objects in the collection
 */
public class POJOCollectionViewPanel extends JJPanel implements
		POJOCollectionListener {
	ScreenBoxedPOJOCollectionContextWithNavigator context;
	JPanel panel;
	JScrollPane scroll;
	Border defaultScrollBorder;

	public POJOCollectionViewPanel(ScreenBoxedPOJOCollectionContextWithNavigator context) {
		super();
		this.context = context;
		initGUI();
		context.addListener(this);
	}

	public Dimension getPreferredSize() {
		return new Dimension(130, 200);
		// return Utility.getMaxDimension(new Dimension(250, 200),
		// super.getPreferredSize());
	}

	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	public void pojoAdded(Object obj) {
		// @optimize
		reloadContents();
		invalidate();
		validate();
		repaint();
	}

	public void pojoRemoved(Object obj) {
		// @optimize
		reloadContents();
		invalidate();
		validate();
		repaint();
	}

	public void setTitle(String title) {
		// scroll.setBorder(new TitledBorder(defaultScrollBorder, title));
	}

	private void initGUI() {
		panel = new JPanel();
		panel.setLayout(new VerticalLayout());

		scroll = new JScrollPane(panel);
		defaultScrollBorder = scroll.getBorder();

		// setBorder(new TitledBorder(new , "XYZ"));
		setLayout(new BorderLayout());
		add("Center", scroll);

		reloadContents();
	}

	public void reloadContents() {
		panel.removeAll();

		Iterator<POJOSwizzler> it = context.getPOJOs().getSwizzlers()
				.iterator();
		while (it.hasNext()) {
			POJOSwizzler object = it.next();
			ScreenBoxedPOJO view = new ScreenBoxedPOJORef(context,
					object.getObject(), true, true, true);
			panel.add(view);
		}
		invalidate();
		validate();
		repaint();
	}

}