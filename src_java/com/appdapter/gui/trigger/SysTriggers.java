/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.gui.trigger;

import com.appdapter.gui.box.Box;
import com.appdapter.gui.box.TriggerImpl;
import com.appdapter.gui.box.BoxImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author winston
 */
public class SysTriggers {
	static Logger theLogger = LoggerFactory.getLogger(SysTriggers.class);
	public enum Kind {
		QUIT,
		DUMP
	}
	public static class QuitTrigger<BT extends Box<TriggerImpl<BT>>> extends  TriggerImpl<BT> {
		@Override public void fire(BT targetBox) {
			theLogger.info(toString() + "-firing, program exiting");
			System.exit(0);
		}
	}
	// Example of the shorter, less-safe, raw typing style.
	public static class DumpTrigger extends TriggerImpl {
		@Override public void fire(Box targetBox) {
			theLogger.info(toString() + "-dumping");
			((BoxImpl) targetBox).dump();
		}
	}

}
