package org.appdapter.gui.repo;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.TriggerImpl;

public class ScreenGraphTrigger extends TriggerImpl /*
														   * with FullTrigger<GraphBox>
														   */{

	final String myDebugName;

	public ScreenGraphTrigger(String myDebugNym) {
		myDebugName = myDebugNym;
	}

	@Override public String toString() {
		return getClass().getName() + "[name=" + myDebugName + "]";
	}

	@Override public void fire(Box targetBox) {
		getLogger().debug(this.toString() + " firing on " + targetBox.toString());

	}
}