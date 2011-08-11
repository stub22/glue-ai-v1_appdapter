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

package org.appdapter.peru.test.binding.xmldb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.modules.XMLResource;


/**
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public class XMLDB_Test {
	
	private static Logger 		theLogger = LoggerFactory.getLogger(XMLDB_Test.class);

	private static String 	DRIVER_CLASS_NAME = "org.exist.xmldb.DatabaseImpl";
	private static String	SERVER_URI = "xmldb:exist://radius:8080/exist/xmlrpc/";
	
	private static String  	COLLECTION_URI = SERVER_URI + "db/shakespeare/plays";
	private static String	RESOURCE_NAME = "macbeth.xml";
	
	public static void main(String[] args) {
		theLogger.info("XMLDB_Test - gears are spinning up!");
		try {
			
			// We can get this far (compiled) with just the xmldb.jar
			// Then we fail on the class lookup
			Class cl = Class.forName(DRIVER_CLASS_NAME);			
			Database database = (Database)cl.newInstance();
			// No, we don't want to run an eXist server inside this VM!
			// database.setProperty("create-database", "true");
			DatabaseManager.registerDatabase(database);
			
			Collection col = DatabaseManager.getCollection(COLLECTION_URI);
			
			XMLResource res = (XMLResource)col.getResource(RESOURCE_NAME);
			if (res == null) {
				theLogger.error("Null resource returned for name " + RESOURCE_NAME + " by collection " + col);
			} else {
				theLogger.info(res.getContent().toString());
			}
			// We can get this far with just the exist.jar + xmldb.jar, but the output is encoded
			// wrong until we add the "patched" version of xmlrpc from eXist.  JWhich from peruser
			// is currently broken?   Grrr.
			
			// XMLResource document = (XMLResource)col.createResource(f.getName(), "XMLResource");

		} catch (Throwable t) {
			theLogger.error("XMLDB_Test caught ", t);
		}
		theLogger.info("XMLDB_Test - gears are spinning down!");
	}
	
}
