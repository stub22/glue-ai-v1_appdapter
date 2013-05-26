package org.appdapter.gui.pojo;

import org.appdapter.api.trigger.Box;

public class BasicObjectCustomizer extends ScreenBoxedPOJOWithPropertiesPanel<Box> {
	public BasicObjectCustomizer() {
	}

	public BasicObjectCustomizer(POJOApp app, Object object) {
		super(app, object);
	}

	protected void initSubClassGUI() {
	}

	@Override public Object getObject() {
		Object o = super.getObject();
		if (o == this) {
			throw new AbstractMethodError();
		}
		return o;
	}
}
