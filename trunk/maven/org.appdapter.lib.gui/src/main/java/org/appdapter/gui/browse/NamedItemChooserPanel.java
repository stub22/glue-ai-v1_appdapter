package org.appdapter.gui.browse;

import java.awt.BorderLayout;
import java.awt.Container;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.api.trigger.NamedObjectCollection;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.GetSetObject;
import org.appdapter.gui.api.FocusOnBox;
import org.appdapter.gui.api.Utility;
import org.appdapter.gui.impl.JJPanel;
import org.appdapter.gui.util.PromiscuousClassUtils;

public class NamedItemChooserPanel extends JJPanel implements GetSetObject, FocusOnBox<Box> {

	//JLayeredPane desk;
	//JSplitPane split;
	ClassChooserPanel classChooserPanel;
	LargeObjectChooser namedObjectListPanel;
	NamedObjectCollection namedObjects;
	DisplayContext context;

	public NamedItemChooserPanel(DisplayContext context0) {
		super(true);
		Utility.namedItemChooserPanel = this;
		this.context = context0;
		namedObjects = context0.getLocalBoxedChildren();
		PromiscuousClassUtils.ensureInstalled();
		Utility.registerEditors();
		Utility.setBeanInfoSearchPath();
		initGUI();
	}

	void initGUI() {
		removeAll();

		adjustSize();
		namedObjects = context.getLocalBoxedChildren();
		namedObjectListPanel = new LargeObjectChooser(null, namedObjects);
		classChooserPanel = new ClassChooserPanel(context);
		namedObjects.addListener(classChooserPanel);
		setLayout(new BorderLayout());
		namedObjectListPanel.setBorder(new TitledBorder("Object browser"));
		add("North", classChooserPanel);
		add("Center", namedObjectListPanel);
	}

	private void adjustSize() {
		Container p = getParent();
		if (p != null) {
			setSize(p.getSize());
		}
	}

	@Override public void focusOnBox(Box b) {

	}

	@Override public Object getValue() {
		return namedObjectListPanel.getObject();
	}

	@Override public void setObject(Object object) throws InvocationTargetException {
		try {
			namedObjectListPanel.setSelectedObject(object);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
			throw new InvocationTargetException(e);
		}

	}

}
