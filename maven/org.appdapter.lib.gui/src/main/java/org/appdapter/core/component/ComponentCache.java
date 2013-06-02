/*
 *  Copyright 2012 by The Appdapter Project (www.appdapter.org).
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.appdapter.core.component;

import org.appdapter.core.name.Ident;

import org.appdapter.core.component.MutableKnownComponent;
import org.appdapter.gui.pojo.Utility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class ComponentCache<MKC extends MutableKnownComponent> {
	static Logger theLogger = LoggerFactory.getLogger(ComponentCache.class);
	private Map<Ident, MKC> myCompCache = new HashMap<Ident, MKC>();

	public MKC getCachedComponent(Ident id) {
		return myCompCache.get(id);
	}

	public void putCachedComponent(Ident id, MKC comp) {
		Utility.putCachedComponent(id, comp);
		myCompCache.put(id, comp);
	}
}
