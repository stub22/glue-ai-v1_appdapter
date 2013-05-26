package org.appdapter.gui.pojo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.Customizer;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import org.appdapter.api.trigger.Box;
import org.appdapter.gui.swing.ErrorPanel;
import org.appdapter.gui.swing.MethodsPanel;
import org.appdapter.gui.swing.PropertiesPanel;
import org.slf4j.LoggerFactory;

/**
 * A panel containing a complete GUI for a object, including properties,
 * methods, etc.
 * <p>
 * 
 * 
 */
@SuppressWarnings("serial")
abstract public class ScreenBoxedPOJOWithPropertiesPanel<BoxType extends Box>

extends AbstractScreenBoxedPOJOPanel<BoxType> implements Customizer, GetSetObject {
	protected POJOApp context;
	protected JTabbedPane tabs;
	private Object object;

	protected abstract void initSubClassGUI() throws Throwable;

	public ScreenBoxedPOJOWithPropertiesPanel() {
		this(null);
	}

	public ScreenBoxedPOJOWithPropertiesPanel(Object object) {
		this(Utility.getCurrentContext(), object);
	}

	public ScreenBoxedPOJOWithPropertiesPanel(POJOApp context, Object object) {
		this.object = object;
		this.context = context;
		initGUI();
	}

	boolean initedGuiOnce = false;

	public final void initGUI() {
		if (initedGuiOnce == true)
			return;
		if (object == null)
			return;
		initedGuiOnce = true;
		try {
			initGUI_super();
			initSubClassGUI();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			initedGuiOnce = false;
		}
	}

	@Override public Dimension getPreferredSize() {
		/*
		 * Dimension dim = super.getPreferredSize(); int w,h; w =
		 * Math.min(dim.width + 30, 350); h = Math.min(dim.height + 30, 500);
		 */
		return Utility.getConstrainedDimension(getMinimumSize(), super.getPreferredSize(), getMaximumSize());
	}

	@Override public Dimension getMinimumSize() {
		return new Dimension(400, 350);
	}

	@Override public Dimension getMaximumSize() {
		return new Dimension(800, 600); // Toolkit.getDefaultToolkit().getScreenSize();
	}

	/**
	 * Delegates directly to setBean(...). This method is needed to conform to
	 * the Customizer interface.
	@Override
	public void setObject(Object o) {
	
	}

	 *
	 */
	public void setBean(Object obj) {
		setObject(obj);
		initGUI();
	}

	/**
	 * This method is needed to conform to the Customizer interface. It doesn't
	 * do anything yet.
	 */
	@Override public void addPropertyChangeListener(PropertyChangeListener listener) {
	}

	/**
	 * This method is needed to conform to the Customizer interface. It doesn't
	 * do anything yet.
	 */
	@Override public void removePropertyChangeListener(PropertyChangeListener listener) {
	}

	/**
	 * Called whenever the pojo is switched. Caused the GUI to update to render
	 * the new pojObject instead.
	 */
	@Override protected void objectChanged(Object oldValue, Object newValue) {
		super.firePropertyChange("value", oldValue, newValue);
		removeAll();
		initGUI();
	}

	public void initGUI_super() throws Throwable {
		if (object == null) {
			return;
		}
		tabs = new JTabbedPane();

		Object object = getObject();

		if (object != null) {

			if (object instanceof Class) {
				PropertiesPanel props = new PropertiesPanel(context, object);
				tabs.addTab("Class Properties", props);

				try {
					MethodsPanel methods = new MethodsPanel(context, object);
					tabs.addTab("Class Methods", methods);
				} catch (Exception err) {
					tabs.addTab("Methods", new ErrorPanel("Could not show methods", err));
				}
			}
			{

				PropertiesPanel props = new PropertiesPanel(context, object);
				tabs.addTab("Properties", props);

				try {
					MethodsPanel methods = new MethodsPanel(context, object);
					tabs.addTab("Methods", methods);
				} catch (Exception err) {
					tabs.addTab("Methods", new ErrorPanel("Could not show methods", err));
				}
			}
			setLayout(new BorderLayout());
			add("Center", tabs);
		} else {
			add(new JLabel("ERROR object is null!? " + getObject()));
		}
	}

	@Override public void focusOnBox(Box b) {
		LoggerFactory.getLogger(getClass().getName()).info("Focusing on box: " + b);
	}

	/**
	 * Return the live object in which we think we are updating
	 * 
	 * This can be 'this' object
	 * 
	 */
	@Override public Object getObject() {
		return object;
	}

	public void setObject(Object newpojObject) {
		Object oldpojObject = getObject();
		if (oldpojObject != newpojObject) {
			objectChanged(oldpojObject, newpojObject);
		}
		object = newpojObject;
		initGUI();
	}

}