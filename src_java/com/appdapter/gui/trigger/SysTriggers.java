/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.com).
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

package com.appdapter.gui.trigger;

import com.appdapter.gui.box.Box;
import com.appdapter.gui.box.TriggerImpl;
import com.appdapter.gui.box.BoxImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
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
