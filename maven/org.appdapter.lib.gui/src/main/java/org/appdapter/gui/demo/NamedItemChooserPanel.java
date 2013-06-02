package org.appdapter.gui.demo;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.border.TitledBorder;

import org.appdapter.api.trigger.Box;
import org.appdapter.gui.box.ButtonTabComponent;
import org.appdapter.gui.box.POJOApp;
import org.appdapter.gui.box.ScreenBoxPanel;
import org.appdapter.gui.pojo.Utility;
import org.appdapter.gui.util.PromiscuousClassUtils;

public class NamedItemChooserPanel extends ScreenBoxPanel {

	//JLayeredPane desk;
	//JSplitPane split;
	LargeObjectChooser namedObjectListPanel;
	ClassChooserPanel classChooserPanel;
	POJOApp context;

	public NamedItemChooserPanel(POJOApp context) {
		Utility.namedItemChooserPanel = this;
		this.context = context;
		PromiscuousClassUtils.ensureInstalled();
		Utility.registerEditors();
		Utility.setBeanInfoSearchPath();
		initGUI();
	}

	void initGUI() {
		removeAll();

		adjustSize();
		namedObjectListPanel = new LargeObjectChooser(context);
		classChooserPanel = new ClassChooserPanel(context);
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
		// TODO Auto-generated method stub

	}

	@Override public Object getValue() {
		// TODO Auto-generated method stub
		return namedObjectListPanel.getObject();
	}

}
