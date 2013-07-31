package org.appdapter.gui.browse;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.MutableTrigger;
import org.appdapter.core.component.KnownComponent;

public interface KMCTrigger<BoxType extends Box<? extends MutableTrigger<BoxType>>> extends MutableTrigger<BoxType>, KnownComponent {

	public @Override void fire(Box targetBox);

}
