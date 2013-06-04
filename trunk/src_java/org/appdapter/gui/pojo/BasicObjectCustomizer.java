package org.appdapter.gui.pojo;

import org.appdapter.api.trigger.Box;
import org.appdapter.gui.box.BoxPanelSwitchableView;
import org.appdapter.gui.swing.POJOAppContext;

public class BasicObjectCustomizer extends ScreenBoxedPOJOWithPropertiesPanel<Box> {
	public BasicObjectCustomizer() {
	}

	public BasicObjectCustomizer(POJOAppContext app, Object object) {
		super(app, object);
	}

	protected void initSubClassGUI() {
	}

	@Override public Object getValue() {
		Object o = super.getValue();
		if (o == this) {
			throw new AbstractMethodError();
		}
		return o;
	}
}
