package org.appdapter.gui.editors;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.Customizer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.util.Collection;

import javax.swing.JLabel;

import org.appdapter.api.trigger.Box;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.AddTabFrames;
import org.appdapter.gui.api.AddTabFrames.SetTabTo;
import org.appdapter.gui.api.BoxPanelSwitchableView;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.GetSetObject;
import org.appdapter.gui.api.SetObject;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.swing.ConstructorsListPanel;
import org.appdapter.gui.swing.CollectionContentsPanel;
import org.appdapter.gui.swing.ErrorPanel;
import org.appdapter.gui.swing.*;
import org.appdapter.gui.swing.MethodsPanel;
import org.appdapter.gui.swing.ObjectTabsForTabbedView;
import org.appdapter.gui.swing.ObjectView;
import org.appdapter.gui.swing.PropertiesPanel;
import org.appdapter.gui.swing.StaticMethodsPanel;
import org.slf4j.LoggerFactory;

import com.jidesoft.swing.JideTabbedPane;

/**
 * A panel containing a complete GUI for a object, including properties,
 * methods, etc.
 * <p>
 * 
 * 
 */
@SuppressWarnings("serial")
final public class LargeObjectView<BoxType extends Box>

extends ObjectView<BoxType> implements Customizer, GetSetObject, ObjectPanelHost {
	static public abstract class TabPanelMaker implements AddTabFrames {

		@Override public int hashCode() {
			return getClass().hashCode();
		}

		@Override public boolean equals(Object o) {
			return getClass().hashCode() == o.getClass().hashCode();
		}

	}

	protected DisplayContext context;
	protected JideTabbedPane tabs;
	//protected Object objectValue;
	BoxPanelSwitchableView objTabs;

	///protected abstract void initSubClassGUI() throws Throwable;

	public LargeObjectView() {
		this(null);
	}

	public LargeObjectView(Object object) {
		this(Utility.getCurrentContext(), object);
	}

	public LargeObjectView(DisplayContext context, Object object) {
		super(false);
		this.objectValue = object;
		this.context = context;
		initGUISetupNewObject();
		initGUI();
	}

	@Override public String getName() {
		return Utility.getUniqueName(getValue());
	}

	final protected void objectValueChanged() {
		tabs.removeAll();
		Object bean = getValue();
		Class objClass = Utility.getClassNullOk(bean);
		if (bean != null) {
			for (AddTabFrames atf : getTabFrameAdders()) {
				atf.setTabs(objTabs, context, bean, objClass, SetTabTo.ADD);
			}
		} else {
			add(new JLabel("ERROR object is null!? " + bean));
		}

		for (Component c : tabs.getComponents()) {
			try {
				if (c instanceof SetObject) {
					SetObject gso = (SetObject) c;
					gso.setObject(bean);
					continue;
				}
				if (c instanceof PropertyEditor) {
					PropertyEditor gso = (PropertyEditor) c;
					gso.setValue(bean);
					continue;
				}
				if (c instanceof Customizer) {
					Customizer gso = (Customizer) c;
					gso.setObject(bean);
					continue;
				}

			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}

		}
	}

	private Collection<AddTabFrames> getTabFrameAdders() {
		return Utility.addTabFramers;

	}

	public boolean addTab(String title, Component view) {
		tabs.add(title, view);
		return true;
	}

	/**
	 * Return the live object in which we think we are updating
	 * 
	 * This can be 'this' object
	 * 
	 */
	@Override public Object getValue() {
		Object o = objectValue;
		if (o == this || o == null) {
			Debuggable.notImplemented("LargeObjectView " + getValue());
		}
		return o;
	}

	boolean initedGuiOnce = false;

	public final boolean initGUI() {
		synchronized (valueLock) {
			if (initedGuiOnce == true)
				return false;
			if (objectValue == null)
				return false;
			initedGuiOnce = true;
			try {
				objectValueChanged();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				initedGuiOnce = false;
			}
			return initedGuiOnce;
		}
	}

	public void initGUISetupNewObject() {
		removeAll();
		setLayout(new BorderLayout());
		tabs = new JideTabbedPane();
		add("Center", tabs);
		objTabs = new ObjectTabsForTabbedView(tabs);
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
	 * Delegates directly to setObject(...). This method is needed to conform to
	 * the Customizer interface. */
	public void setBean(Object obj) {
		setObject(obj);
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
	@Override public void objectValueChanged(Object oldValue, Object newValue) {
		newValue = Utility.dref(newValue);
		super.firePropertyChange("value", oldValue, newValue);
		reallySetValue(newValue);
	}

	@Override public void focusOnBox(Box b) {
		setObject(b);
		LoggerFactory.getLogger(getClass().getName()).info("Focusing on box: " + b);
	}

	static public class BasicObjectCustomizer extends TabPanelMaker {

		@Override public void setTabs(BoxPanelSwitchableView tabs, DisplayContext context, Object object, Class objClass, SetTabTo cmd) {
			setTabs0(tabs, context, object, objClass, cmd);
		}

		public void setTabs0(BoxPanelSwitchableView tabs, DisplayContext context, Object object, Class objClass, SetTabTo cmd) {
			String prefix = "";
			if (object instanceof Class) {
				prefix = "Class ";
			}
			if (cmd == SetTabTo.ADD) {
				PropertiesPanel props = new PropertiesPanel(context, object, objClass, false);
				tabs.addTab(prefix + "Properties", props);
			}
			if (cmd == SetTabTo.REMOVE) {
				tabs.removeTab(prefix + "Properties", null);
			}
			String ms = prefix + "Methods";
			try {
				if (cmd == SetTabTo.ADD) {
					MethodsPanel view = new MethodsPanel(context, object, objClass);
					tabs.addTab(ms, view);
				}
				if (cmd == SetTabTo.REMOVE) {
					tabs.removeTab(ms, null);
				}
			} catch (Exception err) {
				if (cmd == SetTabTo.ADD)
					tabs.addTab(ms, new ErrorPanel("Could not show view", err));
				if (cmd == SetTabTo.REMOVE) {
					tabs.removeTab(ms, null);
				}
			}
		}

	}

	static public class ClassCustomizer extends TabPanelMaker {
		@Override public void setTabs(BoxPanelSwitchableView tabs, DisplayContext context, Object object, Class objClass, SetTabTo cmds) {
			if (!(object instanceof Class)) {
				return;
			}
			Class clazz = (Class) object;
			if (cmds != SetTabTo.ADD)
				return;
			try {
				ConstructorsListPanel constructors = new ConstructorsListPanel(clazz);
				tabs.insertTab("Constructors", null, constructors, null, 0);
			} catch (Exception err) {
				tabs.insertTab("Constructors", null, new ErrorPanel("Could not show constructors", err), null, 0);
			}

			try {
				StaticMethodsPanel statics = new StaticMethodsPanel(clazz);
				tabs.insertTab("Static methods", null, statics, null, 1);
			} catch (Exception err) {
				tabs.insertTab("Static methods", null, new ErrorPanel("Could not show static methods", err), null, 1);
			}
			try {
				PropertiesPanel statics = new PropertiesPanel(context, null, clazz, true);
				tabs.insertTab("Static Properties", null, statics, null, 1);
			} catch (Exception err) {
				tabs.insertTab("Static Properties", null, new ErrorPanel("Could not show static Properties", err), null, 1);
			}
			try {
				LargeObjectChooser instances = new LargeObjectChooser(clazz, context.getLocalBoxedChildren());
				tabs.insertTab("InstancesOf", null, instances, null, 0);
			} catch (Exception err) {
				tabs.insertTab("InstancesOf", null, new ErrorPanel("Could not show Instances", err), null, 0);
			}
		}
	}

	static public class CollectionCustomizer extends TabPanelMaker {

		@Override public void setTabs(BoxPanelSwitchableView tabs, DisplayContext context, Object object, Class objClass, SetTabTo cmds) {
			if (!(object instanceof Collection)) {
				return;
			}
			String title = "The contents of " + object;
			if (cmds != SetTabTo.ADD)
				return;
			try {
				CollectionContentsPanel cc = new CollectionContentsPanel(context, title, (Collection) object, tabs);
				tabs.insertTab("Contents", null, cc, null, 0);
				tabs.addChangeListener(cc);
			} catch (Exception err) {
				tabs.insertTab("Contents", null, new ErrorPanel(title + " could not be shown", err), null, 0);
			}
		}
	}

	static public class ThrowableCustomizer extends TabPanelMaker {

		@Override public void setTabs(BoxPanelSwitchableView tabs, DisplayContext context, Object objct, Class objClass, SetTabTo cmds) {
			if (!(objct instanceof Throwable)) {
				return;
			}
			if (cmds != SetTabTo.ADD)
				return;
			Throwable object = (Throwable) objct;

			String name;
			if (object instanceof Error) {
				name = "Error";
			} else if (object instanceof RuntimeException) {
				name = "RuntimeException";
			} else if (object instanceof Exception) {
				name = "Exception";
			} else {
				name = "Throwable";
			}

			try {
				ErrorPanel errorPanel = new ErrorPanel(object);
				tabs.insertTab(name, null, errorPanel, null, 0);
			} catch (Exception err) {
				tabs.insertTab(name, null, new ErrorPanel("Could not show error info for " + object, err), null, 0);
			}
		}
	}

	@Override protected void reallySetValue(Object bean) {
		bean = Utility.dref(bean);
		if (objectValue == bean)
			return;
		objectValue = bean;
		objectValueChanged();
	}

	@Override public void setObject(Object bean) {
		bean = Utility.dref(bean);
		if (objectValue != bean) {
			objectValueChanged(objectValue, bean);
		}
	}
}