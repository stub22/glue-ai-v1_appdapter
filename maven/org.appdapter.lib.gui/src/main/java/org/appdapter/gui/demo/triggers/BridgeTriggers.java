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

package org.appdapter.gui.demo.triggers;

import java.util.Set;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxContext;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.api.trigger.TriggerImpl;
import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.bind.rdf.jena.model.JenaFileManagerUtils;
import org.appdapter.demo.DemoResources;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BridgeTriggers {

	public static class MountSubmenuFromTriplesTrigger<BT extends Box<TriggerImpl<BT>>> extends TriggerImpl<BT> {
		@Override public void fire(BT targetBox) {
			logInfo(toString() + ".fire()");
			BoxContext bc = targetBox.getBoxContext();

			String triplesURL = DemoResources.MENU_ASSEMBLY_PATH;
			JenaFileManagerUtils.ensureClassLoaderRegisteredWithDefaultJenaFM(DemoResources.class.getClassLoader());
			logInfo("Loading triples from URL: " + triplesURL);
			try {
				Set<Object> loadedStuff = AssemblerUtils.buildAllObjectsInRdfFile(triplesURL);
				logInfo("Loaded " + loadedStuff.size() + " objects");
				for (Object o : loadedStuff) {
					if (o instanceof MutableBox) {
						MutableBox loadedMutableBox = (MutableBox) o;
						bc.contextualizeAndAttachChildBox(targetBox, loadedMutableBox);
						logInfo("Loaded mutable box: " + loadedMutableBox);
					} else {
						logInfo("Loaded object which is not a mutable box: " + o);
					}
				}
			} catch (Exception e) {

			}
		}
	}
}
