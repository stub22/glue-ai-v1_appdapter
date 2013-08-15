package org.appdapter.gui.demo;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeModel;

import org.appdapter.api.trigger.BoxContext;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.api.trigger.UserResult;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.DisplayContextProvider;
import org.appdapter.gui.api.ScreenBoxTreeNode;
import org.appdapter.gui.browse.Utility;

public class DemoNavigatorCtrl extends BaseDemoNavigatorCtrl implements DisplayContext, org.appdapter.demo.DemoBrowserCtrl {

	public DemoNavigatorCtrl() {
		super();
	}

	public DemoNavigatorCtrl(BoxContext bc, TreeModel tm, ScreenBoxTreeNode rootBTN, DisplayContextProvider dcp) {
		super(bc, tm, rootBTN, dcp);
	}

	@Override public void launchFrame(final String title) {
		try {
			Utility.invokeLater(new Runnable() {
				@Override public void run() {
					DemoNavigatorCtrl.super.launchFrame(title);
				}
			});
		} catch (Throwable e) {
		}
	}

	public void addBoxToRoot(MutableBox childBox, boolean reload) {
		super.addBoxToRoot(childBox, reload);
	}

	@Override public void addRepo(String title, Object anyObject) {
		super.addRepo(title, anyObject);
	}

	public UserResult addObject(String title, Object anyObject, boolean showASAP) {
		return super.addObject(title, anyObject, showASAP);
	}

	@Override public UserResult addObject(String title, Object anyObject, boolean showASAP, boolean expandChildren) {
		return super.addObject(title, anyObject, showASAP, expandChildren);
	}

	public void addObject(String title, Object anyObject) {
		addObject(title, anyObject, false);
	}

	public JFrame getFrame() {
		return super.getFrame();
	}

	@Override public void show() {
		launchFrame(null);
	}
}
