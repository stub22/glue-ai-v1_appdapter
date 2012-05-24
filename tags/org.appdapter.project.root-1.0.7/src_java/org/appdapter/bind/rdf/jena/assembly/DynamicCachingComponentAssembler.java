/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.bind.rdf.jena.assembly;

import org.appdapter.core.component.ComponentAssemblyNames;
import org.appdapter.core.item.Ident;
import org.appdapter.core.item.Item;
import org.appdapter.core.component.MutableKnownComponent;
import org.appdapter.api.trigger.TriggerImpl;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Stu B. <www.texpedient.com>
 */
public abstract class DynamicCachingComponentAssembler<MKC extends MutableKnownComponent> extends CachingComponentAssembler<MKC> {
	
	public DynamicCachingComponentAssembler(Resource r) {
		super(r);
	}
	@Override protected Class<MKC> decideComponentClass(Ident componentID, Item componentConfigItem) {
		String jfqcn = getReader().readConfigValString(componentID, ComponentAssemblyNames.P_javaFQCN, componentConfigItem, null);
		if (jfqcn != null) {
			theLogger.info("Found component class name: " + jfqcn);
			Class<MKC> triggerClass = findClass(jfqcn);
			return triggerClass;
		} else {
			throw new RuntimeException("Cannot find class name for componentID: " + componentID);
		}
	}
	public Class<MKC> findClass(String btcFQCN) {
		Class c = null;
		try {
			c = Class.forName(btcFQCN);
		} catch (Throwable t) {
			theLogger.error("Problem looking up class " + btcFQCN, t);
		}
		return c;
	}
}
