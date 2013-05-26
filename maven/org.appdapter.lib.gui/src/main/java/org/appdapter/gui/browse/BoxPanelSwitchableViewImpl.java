package org.appdapter.gui.browse;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JTabbedPane;

import org.appdapter.api.trigger.BoxPanelSwitchableView;
import org.appdapter.gui.pojo.Utility;

public class BoxPanelSwitchableViewImpl implements BoxPanelSwitchableView {

	JDesktopPane jdp = null;// new JDesktopPane();
	JInternalFrame jif = null;// new JDesktopPane();

	//private Component selected;

	public int indexOfComponent(Component c) {
		if (jdp != null) {
			return jdp.getIndexOf(c);
		}
		if (jif != null) {
			int i = 0;
			for (Component c0 : jif.getComponents()) {
				if (c.equals(c0))
					return i;
				i++;
			}
			return -1;
		}
		return getImpl().indexOfComponent(c);
	}

	public int indexOfTabComponent(Component c) {
		if (jif != null) {
			int i = 0;
			for (Component c0 : jdp.getComponents()) {
				if (c.equals(c0))
					return i;
				i++;
			}
			return -1;
		}
		return getImpl().indexOfTabComponent(c);
	}

	public String getTitleAt(int i) {
		if (jdp != null) {
			return "" + jdp.getComponent(i);
		}
		return getImpl().getTitleAt(i);
	}

	public void setSelectedComponent(Component c) {
		if (jdp != null) {
			jdp.moveToFront(c);
		}
		getImpl().setSelectedComponent(c);
	}

	public JTabbedPane getImpl() {
		return (JTabbedPane) Utility.mainDisplayContext.getRealPanelTabPane();
	}

	public JComponent getComponent() {
		if (jdp != null)
			return jdp;
		return Utility.mainDisplayContext.getRealPanelTabPane();
	}

	public void add(String n, Component c) {
		getComponent().add(n, c);
	}

	public void remove(int i) {
		getComponent().remove(i);

	}

	public void remove(Component i) {
		getComponent().remove(i);

	}

	@Override public Dimension getSize() {
		// TODO Auto-generated method stub
		return getComponent().getSize();
	}

}
