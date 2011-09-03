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

import org.appdapter.bind.sql.h2.DatabaseConnector;
import org.appdapter.demo.DemoDatabase;
import org.appdapter.gui.box.Box;
import org.appdapter.gui.box.TriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class DatabaseTriggers {
	static Logger theLogger = LoggerFactory.getLogger(DatabaseTriggers.class);
	
	public enum Kind {
		OPEN,
		CLOSE
	}

	public static class InitTrigger<BT extends Box<TriggerImpl<BT>>> extends  TriggerImpl<BT> {
		@Override public void fire(BT targetBox) {
			theLogger.info(toString() + "-initing");
			DatabaseConnector dbc = DemoDatabase.initConnector();
		}
	}
}
