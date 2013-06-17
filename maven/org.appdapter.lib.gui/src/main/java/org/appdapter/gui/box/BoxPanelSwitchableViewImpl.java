package org.appdapter.gui.box;

import org.appdapter.api.trigger.BoxPanelSwitchableView;
import org.appdapter.api.trigger.NamedObjectCollection;

@SuppressWarnings("unchecked")
abstract public class BoxPanelSwitchableViewImpl extends BoxPanelSwitchableViewImplBase implements BoxPanelSwitchableView {

	public BoxPanelSwitchableViewImpl(NamedObjectCollection savedInPOJOCol) {
		super(savedInPOJOCol);
	}

}
