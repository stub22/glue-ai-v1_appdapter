
package org.appdapter.gui.editors;
import java.beans.PropertyEditorSupport;

public class StringEditor extends PropertyEditorSupport {
  @Override
public void setAsText(String s) {
    setValue(s);
  }

  @Override
public String getAsText() {
    return (String) getValue();
  }
}