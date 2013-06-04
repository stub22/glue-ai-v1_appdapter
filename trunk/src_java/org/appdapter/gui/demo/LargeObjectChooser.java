package org.appdapter.gui.demo;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import org.appdapter.gui.box.ScreenBoxPanel;
import org.appdapter.gui.browse.DisplayContext;
import org.appdapter.gui.pojo.POJOBox;
import org.appdapter.gui.pojo.POJOCollection;
import org.appdapter.gui.pojo.POJOCollectionListener;
import org.appdapter.gui.pojo.ScreenBoxedPOJORefPanel;
import org.appdapter.gui.swing.POJOAppContext;
import org.appdapter.gui.swing.VerticalLayout;

/**
 * A GUI component showing the list of objects in the collection
 */
public class LargeObjectChooser extends ScreenBoxPanel implements POJOCollectionListener {

	@Override public DisplayContext getDisplayContext() {
		return context;
	}

	POJOCollection context;
	JPanel panel;
	JScrollPane scroll;
	Border defaultScrollBorder;

	public LargeObjectChooser(POJOAppContext context0) {
		super();
		this.context = context0.getNamedObjectCollection();
		initGUI();
		context.addListener(this);
	}

	@Override public Dimension getPreferredSize() {
		return new Dimension(130, 200);
		// return Utility.getMaxDimension(new Dimension(250, 200),
		// super.getPreferredSize());
	}

	@Override public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	@Override public void pojoAdded(Object obj) {
		// @optimize
		reloadContents();
		invalidate();
		validate();
		repaint();
	}

	@Override public void pojoRemoved(Object obj) {
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

		for (POJOBox object : context.getNamedObjectCollection().getScreenBoxes()) {
			ScreenBoxedPOJORefPanel view = new ScreenBoxedPOJORefPanel(context.getPOJOAppContext(), object.getValue(), true, true, true);
			panel.add(view);
		}
		invalidate();
		validate();
		repaint();
	}

	public Object getObject() {
		return context.getNamedObjectCollection();
	}
}