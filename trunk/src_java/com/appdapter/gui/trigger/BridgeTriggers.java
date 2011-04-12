/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.gui.trigger;

import com.appdapter.binding.jena.model.AssemblerUtils;
import com.appdapter.gui.box.Box;
import com.appdapter.gui.box.BoxContext;
import com.appdapter.gui.box.MutableBox;
import com.appdapter.gui.box.TriggerImpl;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author winston
 */
public class BridgeTriggers {
	static Logger theLogger = LoggerFactory.getLogger(BridgeTriggers.class);
	public static class MountSubmenuFromTriplesTrigger extends TriggerImpl {
		@Override public void fire(Box targetBox) {
			theLogger.info(toString() + "-mounting-submenu");
			BoxContext bc = targetBox.getBoxContext();

			String triplesURL = "testconf/app/boxtest/boxy_001.ttl";
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
