package org.appdapter.gui.swing;

import org.appdapter.api.trigger.AnyOper.HRKRefinement;
import org.appdapter.api.trigger.AnyOper.UIProvider;

public interface UISwingReplacement extends UIProvider, HRKRefinement {
	public void updateUI();

}