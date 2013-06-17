package org.appdapter.api.trigger;


import java.awt.Dimension;

import javax.swing.JComponent;



public interface ITabUI extends UIProvider {
	public void addTab(String title, JComponent thing);

	public Dimension getPreferredChildSize();
}
