package org.appdapter.gui.trigger;

import javax.swing.AbstractButton;

import org.appdapter.api.trigger.Box;

public interface ButtonFactory {

	AbstractButton makeMenuItem(Box b);

}
