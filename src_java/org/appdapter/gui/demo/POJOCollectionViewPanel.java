package org.appdapter.gui.demo;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import org.appdapter.gui.pojo.POJOCollectionListener;
import org.appdapter.gui.pojo.POJOCollectionWithBoxContext;
import org.appdapter.gui.pojo.POJOSwizzler;
import org.appdapter.gui.pojo.ScreenBoxedPOJO;
import org.appdapter.gui.pojo.ScreenBoxedPOJORef;
import org.appdapter.gui.swing.VerticalLayout;
import org.appdapter.gui.swing.impl.JJPanel;

/**
 * A GUI component showing the list of objects in the collection
 */
public class POJOCollectionViewPanel extends JJPanel implements POJOCollectionListener {

	POJOCollectionWithBoxContext context;
	JPanel panel;
	JScrollPane scroll;
	Border defaultScrollBorder;

	public POJOCollectionViewPanel(POJOCollectionWithBoxContext context) {
		super();
		this.context = context;
		initGUI();
		context.addListener(this);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(130, 200);
		// return Utility.getMaxDimension(new Dimension(250, 200),
		// super.getPreferredSize());
	}

	@Override
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	@Override
	public void pojoAdded(Object obj) {
		// @optimize
		reloadContents();
		invalidate();
		validate();
		repaint();
	}

	@Override
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

		for (POJOSwizzler object : context.getCollectionWithSwizzler().getSwizzlers()) {
			ScreenBoxedPOJO view = new ScreenBoxedPOJORef(context, object.getObject(), true, true, true);
			panel.add(view);
		}
		invalidate();
		validate();
		repaint();
	}
}