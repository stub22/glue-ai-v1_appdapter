

package org.appdapter.gui.swing;

import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.appdapter.gui.objbrowser.model.Utility;
import org.appdapter.gui.swing.impl.JJPanel;

/**
  A GUI widget that lets you select a class.
*/
public class ClassSelectionField extends JJPanel {
  JTextField text = new JTextField(20);
  Class selectedClass = null;
  PropertyChangeSupport propSupport = new PropertyChangeSupport(this);

  public ClassSelectionField() {
    setLayout(new BorderLayout());
    add(text);

    text.getDocument().addDocumentListener(
      new DocumentListener() {
        public void insertUpdate(DocumentEvent e) {
          checkControls();
        }

        public void removeUpdate(DocumentEvent e) {
          checkControls();
        }

        public void changedUpdate(DocumentEvent e) {
          checkControls();
        }
      }
    );
  }

  public void addPropertyChangeListener(PropertyChangeListener p) {
    propSupport.addPropertyChangeListener(p);
  }

  public void removePropertyChangeListener(PropertyChangeListener p) {
    propSupport.removePropertyChangeListener(p);
  }

  public Class getSelectedClass() {
    return selectedClass;
  }

  public void setSelectedClass(Class newValue) {
    setSelectedClass(newValue, true);
  }

  private void setSelectedClass(Class newValue, boolean updateTextField) {
    Class oldValue = selectedClass;
    if (!Utility.isEqual(oldValue, newValue)) {
      selectedClass = newValue;

      //Make sure the contents of the text field corresponds
      //to the selected class
      if (updateTextField) {
        if (selectedClass == null) {
          text.setText("");
        } else {
          text.setText(selectedClass.getName());
        }
      }

      propSupport.firePropertyChange("selectedClass", oldValue, newValue);
    }
  }

  private void checkControls() {
    try {
      setSelectedClass(Class.forName(text.getText()), false);
    } catch (Exception err) {
      setSelectedClass(null);
    }
  }
}
