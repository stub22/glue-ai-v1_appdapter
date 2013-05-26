package org.appdapter.gui.editors;

import java.util.Collection;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.appdapter.gui.pojo.ScreenBoxedPOJOWithPropertiesPanel;
import org.appdapter.gui.swing.CollectionContentsPanel;
import org.appdapter.gui.swing.ErrorPanel;

public class CollectionCustomizer extends ScreenBoxedPOJOWithPropertiesPanel

implements ChangeListener {
	CollectionContentsPanel contents;

	@Override protected void initSubClassGUI() {
		try {
			contents = new CollectionContentsPanel((Collection) getObject());
			tabs.insertTab("Contents", null, contents, null, 0);
			tabs.addChangeListener(this);
		} catch (Exception err) {
			tabs.insertTab("Contents", null, new ErrorPanel("The contents of " + getObject() + " could not be shown", err), null, 0);
		}
	}

	@Override public void stateChanged(ChangeEvent evt) {
		if (tabs.getSelectedIndex() == 0) {
			if (contents != null) {
				contents.reloadContents();
			}
		}
	}
}