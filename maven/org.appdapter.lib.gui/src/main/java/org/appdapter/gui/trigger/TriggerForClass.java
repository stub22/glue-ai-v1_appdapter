package org.appdapter.gui.trigger;

import org.appdapter.api.trigger.Trigger;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.WrapperValue;

public interface TriggerForClass {

	boolean appliesTarget(Class cls, Object example);

	Trigger createTrigger(String menuFmt, DisplayContext ctx, WrapperValue poj);

}
