package org.appdapter.gui.trigger;

import java.util.List;

import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.WrapperValue;

public interface TriggerAdder {
	public <TrigType> void addTriggersForObjectInstance(DisplayContext ctx, Class cls, List<TrigType> tgs, WrapperValue poj, TriggerFilter rulesOfAdd, String menuPrepend);
}
