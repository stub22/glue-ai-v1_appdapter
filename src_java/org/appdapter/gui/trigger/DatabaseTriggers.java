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

package org.appdapter.gui.trigger;

import org.appdapter.binding.h2.DatabaseConnector;
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

			DatabaseConnector.Config dbcc = new DatabaseConnector.Config();
			dbcc.dbFilePath = "testdata/h2/td01";
			dbcc.dbUser = "sa";
			dbcc.dbPassword = "";
			dbcc.tcpPort = "9330";
			dbcc.webPort = "9331";
			DatabaseConnector dbc = new DatabaseConnector();

			try {
				dbc.init(dbcc);
			} catch (Throwable t) {
				theLogger.error("Problem in DatabaseConnector init", t);
			}
			theLogger.info("DatabaseConnector.init() appears to have succeeded, try a web connection.");
		}
	}
}
