package org.appdapter.gui.demo;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;

import javax.swing.border.TitledBorder;

import org.appdapter.api.trigger.Box;
import org.appdapter.gui.box.ButtonTabComponent;
import org.appdapter.gui.box.POJOApp;
import org.appdapter.gui.box.ScreenBoxPanel;
import org.appdapter.gui.pojo.Utility;
import org.appdapter.gui.util.PromiscuousClassUtils;

public class ComponentHost extends ScreenBoxPanel {

	//JLayeredPane desk;
	//JSplitPane split;
	public Component componet;

	//OJOApp context;

	public ComponentHost(Component hostMe) {
		this.componet = hostMe;
		//this.context = context;
		initGUI();
	}

	void initGUI() {
		removeAll();
		adjustSize();
		setLayout(new BorderLayout());
		add(componet);
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
		return componet;
	}

}
