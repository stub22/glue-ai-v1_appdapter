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

import org.appdapter.bind.rdf.jena.model.AssemblerUtils;
import org.appdapter.gui.box.Box;
import org.appdapter.gui.box.BoxContext;
import org.appdapter.gui.box.MutableBox;
import org.appdapter.gui.box.TriggerImpl;
import java.util.Set;
import org.appdapter.demo.DemoResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BridgeTriggers {
	static Logger theLogger = LoggerFactory.getLogger(BridgeTriggers.class);

	public static class MountSubmenuFromTriplesTrigger<BT extends Box<TriggerImpl<BT>>> extends  TriggerImpl<BT> {
		@Override public void fire(BT targetBox) {
			theLogger.info(toString() + ".fire()");
			BoxContext bc = targetBox.getBoxContext();

			String triplesURL = DemoResources.MENU_ASSEMBLY_PATH; // DemoResources.resolveResourceURL(DemoResources.MENU_ASSEMBLY_PATH);
			triplesURL = "org/appdapter/democonf/app/boxdemo/boxy_001.ttl";
			System.out.println("Resolved triplesURL: " + triplesURL);
			AssemblerUtils.ensureClassLoaderRegisteredWithJenaFM(DemoResources.class.getClassLoader());
			theLogger.info("Loading triples file: " + triplesURL);
			Set<Object> loadedStuff = AssemblerUtils.buildAllObjectsInRdfFile(triplesURL);
			theLogger.info("Loaded " + loadedStuff.size() + " objects");
			for (Object o : loadedStuff) {
				if (o instanceof MutableBox) {
					MutableBox  loadedMutableBox = (MutableBox) o;
					bc.contextualizeAndAttachChildBox(targetBox, loadedMutableBox);
					theLogger.info("Loaded mutable box: " + loadedMutableBox);
				} else {
					theLogger.info("Loaded object which is not a mutable box: " + o);
				}
			}
		}
	}
}
