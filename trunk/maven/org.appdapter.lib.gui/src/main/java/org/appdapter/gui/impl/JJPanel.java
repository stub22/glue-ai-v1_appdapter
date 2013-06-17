package org.appdapter.gui.impl;

import java.awt.Component;
import java.awt.LayoutManager;
import java.util.Collection;

import javax.swing.JPanel;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxPanelSwitchableView;
import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.api.trigger.ITabUI;
import org.appdapter.api.trigger.NamedObjectCollection;
import org.appdapter.api.trigger.UserResult;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.Utility;
import org.appdapter.gui.rimpl.MutableRepoBox;

public class JJPanel extends JPanel implements DisplayContext {
	public JJPanel() {
		super();
	}

	public JJPanel(boolean predecorate) {
		super();
	}

	public JJPanel(LayoutManager layout) {
		super(layout);
	}

	public void focusOnBox(Box b) {
		Debuggable.notImplemented();

	}

	public void focusOnBox(MutableRepoBox b) {
		Debuggable.notImplemented();
	}

	public Object getValue() {
		Debuggable.notImplemented();
		return null;
	}

	@Override public String getName() {
		Debuggable.notImplemented();
		return null;
	}

	public static JPanel asPanel(Component customizer, Object val) {
		Debuggable.notImplemented();
		return null;
	}

	@Override public UserResult showError(String msg, Throwable error) {
		Debuggable.notImplemented();
		return null;
	}

	@Override public UserResult showScreenBox(Object value) throws Exception {
		Debuggable.notImplemented();
		return null;
	}

	@Override public UserResult showMessage(String string) {
		Debuggable.notImplemented();
		return null;
	}

	@Override public BoxPanelSwitchableView getBoxPanelTabPane() {
		Debuggable.notImplemented();
		return null;
	}

	@Override public NamedObjectCollection getLocalBoxedChildren() {
		Debuggable.notImplemented();
		return null;
	}

	@Override public Collection getTriggersFromUI(Object object) {
		Debuggable.notImplemented();
		return null;
	}

	@Override public UserResult attachChildUI(String title, Object value) throws Exception {
		Debuggable.notImplemented();
		return null;
	}

	@Override public String getTitleOf(Object value) {
		Debuggable.notImplemented();
		return null;
	}

	@Override public ITabUI getLocalCollectionUI() {
		Debuggable.notImplemented();
		return null;
	}

}
