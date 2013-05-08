package org.appdapter.gui.editors;


import java.util.Collection;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.appdapter.gui.pojo.ScreenBoxedPOJOWithProperties;
import org.appdapter.gui.swing.CollectionContentsPanel;
import org.appdapter.gui.swing.ErrorPanel;

public class CollectionCustomizer extends ScreenBoxedPOJOWithProperties implements ChangeListener {
  CollectionContentsPanel contents;

  protected void initGUI() {
    super.initGUI();
    try {
      contents = new CollectionContentsPanel((Collection) getPOJO());
      tabs.insertTab("Contents", null, contents, null, 0);
      tabs.addChangeListener(this);
    } catch (Exception err) {
      tabs.insertTab("Contents", null, new ErrorPanel("The contents of " + getPOJO() + " could not be shown", err), null, 0);
    }
  }

  public void stateChanged(ChangeEvent evt) {
    if (tabs.getSelectedIndex() == 0) {
      if (contents != null) {
        contents.reloadContents();
      }
    }
  }
}