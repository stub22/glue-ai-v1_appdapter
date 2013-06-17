package org.appdapter.gui.browse;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import org.appdapter.api.trigger.NamedObjectCollection;
import org.appdapter.api.trigger.POJOCollectionListener;
import org.appdapter.gui.swing.SmallObjectView;
import org.appdapter.gui.swing.VerticalLayout;

/**
 * A GUI component showing the list of objects in the collection
 */
public class LargeObjectChooser extends JPanel implements POJOCollectionListener {

	public Object getValue() {
		return getObject();
	}

	NamedObjectCollection context;
	JPanel panel;
	JScrollPane scroll;
	Border defaultScrollBorder;

	public LargeObjectChooser(NamedObjectCollection context0) {
		super(false);
		this.context = context0;
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

		//setBorder(new TitledBorder(new , "XYZ"));
		setLayout(new BorderLayout());
		add("Center", scroll);

		reloadContents();
	}

	public void reloadContents() {
		panel.removeAll();

		Iterator it = context.getObjects();
		while (it.hasNext()) {
			Object value = it.next();
			SmallObjectView view = new SmallObjectView(context, value, true, true, true) {
				@Override public void valueChanged(Object oldValue, Object newValue) {
					replaceInContext(context, oldValue, newValue);
					super.valueChanged(oldValue, newValue);
				}
			};
			view.setRemoveListener(new SmallObjectView.RemoveListener() {
				@Override public void objectRemoved(Object oldValue, Collection parent) {
					replaceInContext(context, oldValue, null);
				}
			});
			panel.add(view);
		}
		invalidate();
		validate();
		repaint();
	}

	protected void replaceInContext(NamedObjectCollection context2, Object oldValue, Object newValue) {
		context2.findOrCreateBox(oldValue).setValue(newValue);
	}

	public Object getObject() {
		return context.getSelectedObject();
	}

}