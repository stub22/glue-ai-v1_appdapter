
package org.appdapter.gui.editors;
import java.beans.PropertyEditorSupport;

public class StringEditor extends PropertyEditorSupport {
  public void setAsText(String s) {
    setValue(s);
  }

  public String getAsText() {
    return (String) getValue();
  }
}