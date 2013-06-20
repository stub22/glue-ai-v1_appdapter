package org.appdapter.gui.browse;

import org.appdapter.api.trigger.Box;

public interface HasFocusOnBox<T extends Box> {
	public void focusOnBox(T b);
}
