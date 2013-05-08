
package org.appdapter.gui.objbrowser;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.appdapter.gui.objbrowser.model.POJOCollection;
import org.appdapter.gui.objbrowser.model.Utility;
import org.appdapter.gui.swing.impl.JJPanel;

public class POJOCollectionViewPanelWithContext extends JJPanel implements ActionListener, DocumentListener {
  Class currentClass = null;

  JDesktopPane desk;
  JSplitPane split;
  POJOCollectionViewPanel list;
  JButton classBrowserButton;
  JTextField classField;
  ScreenBoxedPOJOCollectionContextWithNavigator context;

  public POJOCollectionViewPanelWithContext(ScreenBoxedPOJOCollectionContextWithNavigator context) {
    this.context = context;
    Utility.registerEditors();
    Utility.setBeanInfoSearchPath();
    initGUI();
  }

  public JDesktopPane getDesk() {
    return desk;
  }

  public POJOCollection getCollection() {
    return context.getPOJOs();
  }

  public void actionPerformed(ActionEvent evt){
    if (evt.getSource() == classField || evt.getSource() == classBrowserButton) {
      openClassBrowser();
    }
  }

  public POJOCollectionViewPanel getPOJOList() {
    return list;
  }

  private synchronized void openClassBrowser() {
    if (currentClass != null) {
      try {
        context.showScreenBox(currentClass);
      } catch (Throwable err) {
        context.showError(null, err);
      }
    }
  }

  private void initGUI() {
    desk = new JDesktopPane();
    list = new POJOCollectionViewPanel(context);

    classBrowserButton = new JButton("Examine...");
    classBrowserButton.setToolTipText("Opens a new window that lets you examine classes and create new object instances");
    classBrowserButton.addActionListener(this);
    classBrowserButton.setEnabled(false);
    //classBrowserButton.setActionCommand(COMMAND_CREATE_BEAN);

    classField = new JTextField(10);
    classField.addActionListener(this);
    classField.getDocument().addDocumentListener(this);

    JPanel classPanel = new JPanel();
    classPanel.setBorder(new TitledBorder("Class browser"));
    classPanel.setLayout(new BorderLayout());
    classPanel.add("North", new JLabel("Full class name:"));
    classPanel.add("Center", classField);
    classPanel.add("East", classBrowserButton);

    JPanel leftPanel = new JPanel();
    leftPanel.setLayout(new BorderLayout());
    //objectsPanel.setBorder(new TitledBorder("Object browser"));

    //list.setTitle("Object browser");
    list.setBorder(new TitledBorder("Object browser"));


  /*  JPanel listPanel = new JPanel();
    listPanel.setLayout(new BorderLayout());
    listPanel.add("North", tempPanel);
    listPanel.add("Center", list);*/

    split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, leftPanel, desk);
    split.setOneTouchExpandable(true);

    setLayout(new BorderLayout());
    add("Center", split);

    leftPanel.add("North", classPanel);
    leftPanel.add("Center", list);
  }

  private void classFieldChanged() {
    try {
      currentClass = Class.forName(classField.getText());
    } catch (Exception err) {
      currentClass = null;
    }
    classBrowserButton.setEnabled(currentClass != null);
  }

  public void insertUpdate(DocumentEvent e) {
    classFieldChanged();
  }

  public void removeUpdate(DocumentEvent e) {
    classFieldChanged();
  }

  public void changedUpdate(DocumentEvent e) {
    classFieldChanged();
  }


}
