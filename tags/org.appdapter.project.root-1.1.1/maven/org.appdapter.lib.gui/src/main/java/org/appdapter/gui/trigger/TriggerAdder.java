package org.appdapter.gui.trigger;

import java.util.List;

import org.appdapter.gui.api.DisplayContext;

public interface TriggerAdder {
	public <TrigType> void addTriggersForObjectInstance(DisplayContext ctx, Class cls, List<TrigType> tgs, Object poj, TriggerFilter rulesOfAdd, String menuFmt);
}

