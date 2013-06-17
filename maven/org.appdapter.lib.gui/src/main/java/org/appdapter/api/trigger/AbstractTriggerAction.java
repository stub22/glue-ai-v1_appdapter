package org.appdapter.api.trigger;

import javax.swing.AbstractAction;
import javax.swing.Icon;

abstract public class AbstractTriggerAction extends AbstractAction {

	public AbstractTriggerAction(String name, Icon icon) {
		super(name, icon);
	}

	public AbstractTriggerAction(String name) {
		super(name);
	}

}
