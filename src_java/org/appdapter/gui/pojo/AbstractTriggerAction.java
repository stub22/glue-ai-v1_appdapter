package org.appdapter.gui.pojo;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.Trigger;

abstract public class AbstractTriggerAction extends AbstractAction implements Trigger {

	public AbstractTriggerAction() {
		// TODO Auto-generated constructor stub
	}

	public AbstractTriggerAction(String string, Icon removeFromCollection) {
		// TODO Auto-generated constructor stub
	}

	@Override public void fire(Box targetBox) {
		actionPerformed(null);
	}

}
