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


package org.appdapter.peru.binding.xmldb;

import org.appdapter.peru.core.name.Address;
import org.appdapter.peru.core.name.CoreAddress;

import org.appdapter.peru.core.config.Config;

import org.appdapter.peru.core.document.Doc;

import org.appdapter.peru.core.machine.DocProcessorMachine;

import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.XMLResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public class XMLDB_Processor extends DocProcessorMachine {
	
	private static Log 		theLog = LogFactory.getLog(XMLDB_Processor.class);
	
	private static Address	IA_STORE_DOC = new CoreAddress("peruser:STORE_THIS_DOC_PLEASE");
	private static Address	IA_CREATE_COLLECT = new CoreAddress("peruser:xmldb/CREATE_COLLECTION");
	private static Address	IA_STORE_BINARY = new CoreAddress("peruser:xmldb/STORE_BINARY");
	
	private static Address	PA_COLLECTION = new CoreAddress("peruser:prop/collection");
	private static Address	PA_DOCID = new CoreAddress("peruser:prop/docID");
	
	private static Address	PA_PARENT_COLLECTION = new CoreAddress("peruser:prop/parentCollection");
	private static Address	PA_CHILD_NAME = new CoreAddress("peruser:prop/childName");
	
	private static String 	DRIVER_CLASS_NAME = "org.exist.xmldb.DatabaseImpl";
	private static String	SERVER_URI = "xmldb:exist://radius:8080/exist/xmlrpc";
	
	private static String	XDB_USER = "admin";
	private static String	XDB_PASS = "";
	
	// private static String  	COLLECTION_URI = SERVER_URI + "db/shakespeare/plays";
	// private static String	RESOURCE_NAME = "macbeth.xml";

	protected Doc processDoc(Address instructAddr, Doc inputDoc) throws Throwable {
		Doc	resultD = null;
		
		org.w3c.dom.Document w3cInputDoc = inputDoc.getW3CDOM();
		
		initConnection(DRIVER_CLASS_NAME);
		
		theLog.info("processDoc(" + instructAddr + ")");
		
		if (instructAddr.equals(IA_STORE_DOC)) {
			theLog.info("matched doc storage instruction");
			Config	cc = getCurrentConfig();
			String	collectionPath = cc.getSingleString(IA_STORE_DOC, PA_COLLECTION);
			theLog.info("found collectionPath arg: " + collectionPath);
			String	docID  = cc.getSingleString(IA_STORE_DOC, PA_DOCID);
			theLog.info("found docID arg: " + docID);
			
			Collection targetCollection = findExistingCollection(SERVER_URI, collectionPath, XDB_USER, XDB_PASS);
			
			// Note: createResource can take a null docID and will auto-gen
			XMLResource targetXR = (XMLResource)targetCollection.createResource(docID, XMLResource.RESOURCE_TYPE);
			
			// Requires only a Node, not necessarily a Doc.
			targetXR.setContentAsDOM(w3cInputDoc);
			
			//  Authorization error doesn't occur until we try to store.
			//  org.xmldb.api.base.XMLDBException: org.exist.security.PermissionDeniedException: 
			//  User 'guest' not allowed to write to collection '/db/customerizer/amazon_snap_001'
			//  at org.exist.xmldb.RemoteCollection.store(RemoteCollection.java:523)
			
			//  creates OR updates the resource
			targetCollection.storeResource(targetXR);
			
			targetCollection.close();
			
			theLog.info("storage complete");
			
			resultD = inputDoc;
		} else if (instructAddr.equals(IA_CREATE_COLLECT)) {
			theLog.info("matched create collection instruction");
			Config	cc = getCurrentConfig();
			String	parentCollectionPath = cc.getSingleString(IA_CREATE_COLLECT, PA_PARENT_COLLECTION);
			theLog.info("found parent-collectionPath arg: " + parentCollectionPath);
			String	childName  = cc.getSingleString(IA_CREATE_COLLECT, PA_CHILD_NAME);
			theLog.info("found child-collectionName arg: " + childName);
			
			Collection  parentCollection = findExistingCollection(SERVER_URI, parentCollectionPath, XDB_USER, XDB_PASS);		
			
			Collection childCollection = addSubCollection(parentCollection, childName);
			
			theLog.info("create sub-collect operation complete");
			
			resultD = inputDoc;
		/*
		} else if (instructAddr.equals(IA_STORE_BINARY)) {
			theLog.info("matched store binary instruction");
			Config	cc = getCurrentConfig();
			String	parentCollectionPath = cc.getSingleString(IA_CREATE_COLLECT, PA_PARENT_COLLECTION);
			theLog.info("found parent-collectionPath arg: " + parentCollectionPath);
			String	childName  = cc.getSingleString(IA_CREATE_COLLECT, PA_CHILD_NAME);
			theLog.info("found child-collectionName arg: " + childName);
		*/
 		} else {
			throw new Exception("XMLDB_Processor received bad instruction: " + instructAddr);
			/*
			Collection sourceCollection = findExistingCollection(SERVER_URI, collectionPath);
			XMLResource xres = (XMLResource)col.getResource(RESOURCE_NAME);
			theLog.info("Fetched resource " + RESOURCE_NAME + ", contents: " + xres.getContent().toString());		
			
			org.w3c.dom.Node domNode = xres.getContentAsDOM();
			// Our faith is strong!
			org.w3c.dom.Document w3cOutDoc = (org.w3c.dom.Document) domNode;
			Doc outDoc = DocFactory.makeDocFromW3CDOM(w3cOutDoc);
			resultD = outDoc;
			*/
		}
		
		return resultD;
	}
	protected void initConnection(String driverClassName) throws Throwable  {
		Class cl = Class.forName(driverClassName);			
		Database database = (Database)cl.newInstance();
		DatabaseManager.registerDatabase(database);
	}
	/** note that .close() must be called when user is done with the returned collection 
	  */
	protected Collection findExistingCollection(String dbURI, String collectionPath, String user, String password) throws Throwable {
		Collection resultC = null;
		String fullCollectionURI = dbURI + collectionPath;
		resultC = DatabaseManager.getCollection(fullCollectionURI, user, password);	
		if (resultC == null) {
			throw new Exception("Can't find existing collection at " + fullCollectionURI);
		}
		return resultC;
	}
	protected Collection addSubCollection(Collection parent, String subcollName) throws Throwable {
		Collection resultC = null;
		CollectionManagementService mgtService = (CollectionManagementService) parent.getService("CollectionManagementService", "1.0");
		resultC = mgtService.createCollection(subcollName);
		return resultC;
	}
	/*
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
	}
	*/
	
}
