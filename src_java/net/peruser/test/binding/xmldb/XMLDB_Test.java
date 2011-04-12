package net.peruser.test.binding.xmldb;

import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.XMLResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class XMLDB_Test {
	
	private static Log 		theLog = LogFactory.getLog(XMLDB_Test.class);

	private static String 	DRIVER_CLASS_NAME = "org.exist.xmldb.DatabaseImpl";
	private static String	SERVER_URI = "xmldb:exist://radius:8080/exist/xmlrpc/";
	
	private static String  	COLLECTION_URI = SERVER_URI + "db/shakespeare/plays";
	private static String	RESOURCE_NAME = "macbeth.xml";
	
	public static void main(String[] args) {
		theLog.info("XMLDB_Test - gears are spinning up!");
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
				theLog.error("Null resource returned for name " + RESOURCE_NAME + " by collection " + col);
			} else {
				theLog.info(res.getContent());
			}
			// We can get this far with just the exist.jar + xmldb.jar, but the output is encoded
			// wrong until we add the "patched" version of xmlrpc from eXist.  JWhich from peruser
			// is currently broken?   Grrr.
			
			// XMLResource document = (XMLResource)col.createResource(f.getName(), "XMLResource");

		} catch (Throwable t) {
			theLog.error("XMLDB_Test caught ", t);
		}
		theLog.info("XMLDB_Test - gears are spinning down!");
	}
	
}
