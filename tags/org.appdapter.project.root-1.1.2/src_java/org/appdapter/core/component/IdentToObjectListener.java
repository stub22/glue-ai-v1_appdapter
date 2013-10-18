package org.appdapter.core.component;

import org.appdapter.core.name.Ident;

public interface IdentToObjectListener {
	
	void registerURI(Ident id, Object value);

	void deregisterURI(Ident id, Object value);
}
