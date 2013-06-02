package org.appdapter.gui.pojo;

import org.appdapter.api.trigger.Box;
import org.appdapter.gui.box.POJOApp;

public class BasicObjectCustomizer extends ScreenBoxedPOJOWithPropertiesPanel<Box> {
	public BasicObjectCustomizer() {
	}

	public BasicObjectCustomizer(POJOApp app, Object object) {
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
