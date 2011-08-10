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
package org.appdapter.demo;

import org.appdapter.binding.h2.DatabaseConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class DemoDatabase {

	static Logger theLogger = LoggerFactory.getLogger(DemoDatabase.class);

	public static DatabaseConnector initConnector() {
		DatabaseConnector dbc = null;
		DatabaseConnector.Config dbcc = new DatabaseConnector.Config();
		dbcc.dbFilePath = "demodata/h2/db01";
		dbcc.dbUser = "sa";
		dbcc.dbPassword = "";
		dbcc.tcpPort = "9330";
		dbcc.webPort = "9331";
		dbc = new DatabaseConnector();

		try {
			dbc.init(dbcc);
			theLogger.info(DemoDatabase.class.getCanonicalName() + ".initConnector() appears to have succeeded, try a web connection using H2 console to: " + dbcc);
		} catch (Throwable t) {
			theLogger.error("Problem in " + DemoDatabase.class.getCanonicalName() + ".initConnector()", t);
			dbc = null;
		}
		
		return dbc;
	}
}
