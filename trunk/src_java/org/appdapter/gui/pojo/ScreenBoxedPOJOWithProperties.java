package org.appdapter.gui.pojo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.Customizer;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import org.appdapter.api.trigger.Box;
import org.appdapter.gui.objbrowser.model.POJOCollectionWithBoxContext;
import org.appdapter.gui.objbrowser.model.Utility;
import org.appdapter.gui.swing.ErrorPanel;
import org.appdapter.gui.swing.MethodsPanel;
import org.appdapter.gui.swing.PropertiesPanel;

/**
 * A panel containing a complete GUI for a object, including properties,
 * methods, etc.
 * <p>
 * 
 * 
 */
public class ScreenBoxedPOJOWithProperties<BoxType extends Box> extends
		ScreenBoxedPOJO<BoxType> implements Customizer {
	protected POJOCollectionWithBoxContext context;
	protected JTabbedPane tabs;

	public ScreenBoxedPOJOWithProperties(
			POJOCollectionWithBoxContext context, Object object) {
		super(object);
		this.context = context;
		initGUI();
	}

	public ScreenBoxedPOJOWithProperties(Object object) {
		this(Utility.getCurrentInstances(), object);
	}

	public ScreenBoxedPOJOWithProperties() {
		this(null);
	}

	public Dimension getPreferredSize() {
		/*
		 * Dimension dim = super.getPreferredSize(); int w,h; w =
		 * Math.min(dim.width + 30, 350); h = Math.min(dim.height + 30, 500);
		 */
		return Utility.getConstrainedDimension(getMinimumSize(),
				super.getPreferredSize(), getMaximumSize());
	}

	public Dimension getMinimumSize() {
		return new Dimension(400, 350);
	}

	public Dimension getMaximumSize() {
		return new Dimension(800, 600); // Toolkit.getDefaultToolkit().getScreenSize();
	}

	/**
	 * Delegates directly to setBean(...). This method is needed to conform to
	 * the Customizer interface.
	 */
	public void setObject(Object o) {
		setBean(o);
	}

	/**
	 * This method is needed to conform to the Customizer interface. It doesn't
	 * do anything yet.
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
	}

	/**
	 * This method is needed to conform to the Customizer interface. It doesn't
	 * do anything yet.
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
	}

	protected void objectChanged(Object oldBean, Object newBean) {
		removeAll();
		initGUI();
	}

	protected void initGUI() {
		tabs = new JTabbedPane();

		Object object = getPOJO();

		if (object != null) {

			if (object instanceof Class) {
				PropertiesPanel props = new PropertiesPanel(context, object);
				tabs.addTab("Class Properties", props);

				try {
					MethodsPanel methods = new MethodsPanel(context, object);
					tabs.addTab("Class Methods", methods);
				} catch (Exception err) {
					tabs.addTab("Methods", new ErrorPanel(
							"Could not show methods", err));
				}
			} else {

				PropertiesPanel props = new PropertiesPanel(context, object);
				tabs.addTab("Properties", props);

				try {
					MethodsPanel methods = new MethodsPanel(context, object);
					tabs.addTab("Methods", methods);
				} catch (Exception err) {
					tabs.addTab("Methods", new ErrorPanel(
							"Could not show methods", err));
				}
			}
			setLayout(new BorderLayout());
			add("Center", tabs);
		} else {
			add(new JLabel("null"));
		}
	}
}