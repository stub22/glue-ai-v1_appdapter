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

/*
 *  Some software in this file is copyright by Hewlett Packard Company, LP 
 *  See important notices at bottom of this file.
 */

package org.appdapter.peru.test.binding.jena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.sparql.expr.E_Function;
import com.hp.hpl.jena.sparql.expr.NodeValue;

import com.hp.hpl.jena.util.junit.Manifest;
import com.hp.hpl.jena.util.junit.ManifestItemHandler;
import com.hp.hpl.jena.util.junit.TestUtils;

import com.hp.hpl.jena.rdf.model.Resource;

import com.hp.hpl.jena.shared.JenaException;

import com.hp.hpl.jena.util.FileManager;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * JUnit-Test-Suite for ARQ (+inference), based on the Jena Junit/query code.
 *
 * @author      Stu B. <www.texpedient.com>, based on code by Andy Seaborne of HP.
 * @version     @PERUSER_VERSION@
 * @copyright   derivative(HP, Appdapter)
 */
public class PeruserARQTestSuite extends TestSuite {
	private static Logger 				theLogger = LoggerFactory.getLogger(PeruserARQTestSuite.class);
	private static final String 	testDirARQ = "app";
	private static final String 	rootPathTail = "/test_manifest.ttl";
	
	public static class ManifestTree {
		FileManager 			myFileManager = FileManager.get();
		List					myManifestNodes = new ArrayList();
		Map 					myManifestNodesByPathTail = new HashMap();
		Map 					myEntriesByURI = new HashMap();
		String					myDirectoryPath;
		String					myRootPathTail;
		
		public ManifestTree (String directoryPath, String rootPathTail) {
			myDirectoryPath = directoryPath;
			myRootPathTail = rootPathTail;
		}
		public void loadAllManifests () {
			processManifestFile (myDirectoryPath+myRootPathTail, myRootPathTail);
		}
		
		private void processManifestFile(String filename, String pathTail) {
			theLogger.debug("processManifestFile[" + filename + "]-START");
			Manifest m = null ;
			try {
				m = new Manifest(filename) ;
			} catch (JenaException ex)	{ 
				theLogger.warn("Failed to load: "+filename+"\n"+ex.getMessage()) ;
				return;
			}
			String manifestSafeName = TestUtils.safeName(m.getName()) ;
			theLogger.debug("Manifest safeName is " + manifestSafeName);
			
			ManifestNode mn = new ManifestNode(this, m, pathTail);
			
			myManifestNodes.add(mn);
			myManifestNodesByPathTail.put(pathTail,  mn);
			
			// m.apply() make callbacks to processManifestItem()
			m.apply(mn) ;
		   
			for (Iterator iter = m.includedManifests() ; iter.hasNext() ; )  {
				String nestedManifestFilename = (String) iter.next() ;
				int dirStart = nestedManifestFilename.indexOf(myDirectoryPath);
				int tailStart = dirStart + myDirectoryPath.length() + 1;
				String tail = nestedManifestFilename.substring (tailStart);
				theLogger.debug("Filename tail is:  " + tail);
				processManifestFile(nestedManifestFilename, tail) ;
			}	
			theLogger.debug("processManifestFile[" + filename + "]-END");
		}
		public ManifestNode findManifestNode (String pathTail) {
			return (ManifestNode) myManifestNodesByPathTail.get(pathTail);
		}
		// Node that the tail of the relativeURI must match our wacky underscore-encoding scheme!
		public ManifestEntry findManifestEntry (String relativeURI) {
			return (ManifestEntry) myEntriesByURI.get(relativeURI);
		}
		public void appendJUnitTestCases(TestSuite ts) {
			Iterator i = myManifestNodes.iterator();
			while (i.hasNext()) {
				ManifestNode mn = (ManifestNode) i.next();
				mn.appendJUnitTestCases(ts);
			}
		}
		public void runAndDumpAll() {
			Iterator i = myManifestNodes.iterator();
			while (i.hasNext()) {
				ManifestNode mn = (ManifestNode) i.next();
				mn.runAndDumpAll();
			}
		}
	}
	
	public static class ManifestNode implements ManifestItemHandler {
		ManifestTree	myManifestTree;
		Manifest		myManifest;
		String			myManifestPathTail;
		
		private		List 	myEntries = new ArrayList();
		
		public ManifestNode (ManifestTree tree, Manifest m, String pathTail) {
			myManifestTree = tree;
			myManifest = m;
			myManifestPathTail = pathTail;
		}
		public final boolean processManifestItem(Resource manifest, Resource item, String testName, 
						Resource action, Resource result) {

			ManifestEntry me = new ManifestEntry (this, manifest, item, testName, action, result);
			myEntries.add(me);
			
			ManifestEntry duplicate = (ManifestEntry) myManifestTree.myEntriesByURI.get(me.myURI);
			if (duplicate != null) {
				theLogger.error("Found duplicate ManifestEntry at URI <" + me.myURI + "> = [" + duplicate + "]");
			} else {
				myManifestTree.myEntriesByURI.put(me.myURI, me);
			}
			return true;
		}
		public void appendJUnitTestCases (TestSuite ts) {
			Iterator i = myEntries.iterator();
			while (i.hasNext()) {
				ManifestEntry me = (ManifestEntry) i.next();
				Test t = me.makeJUnitTestCase();
				if ( t != null ) {
					ts.addTest(t);
				}	
			}
		}
		public void runAndDumpAll() {
			Iterator i = myEntries.iterator();
			while (i.hasNext()) {
				ManifestEntry me = (ManifestEntry) i.next();
				PeruserARQ_Harness harness = me.makeHarness();
				if (harness != null) {
					harness.runAndDump();
				} else {
					theLogger.warn("Could not construct harness for " + me.myURI);
				}
			}
		}
	}
	public static class ManifestEntry {
		ManifestNode	myManifestNode;
		String			myURI, myTestName;
		Resource		myManifest, myItem, myAction, myResult;
		public ManifestEntry (ManifestNode mn, Resource manifest, Resource item, String testName, Resource action, Resource result) {
			myManifestNode = mn;
			myTestName = testName;
			myManifest = manifest; myItem = item; myAction = action; myResult = result;
			myURI = item.getURI();
			if (myURI == null) {
				String encodedItemName = encodeItemName(myTestName);
				theLogger.debug("Encoded '" + testName + "' as '" + encodedItemName + "'"); 
				myURI = myManifestNode.myManifestPathTail + "#" + encodedItemName; 
			} else {
				theLogger.debug("Found established URI: '" + myURI + "'"); 
			}
		}
		public static String encodeItemName (String name) {
			//  (is this mapping the reason we get name collision errors?)
			
			// Replace non-word characters (including forward-slash!?!) with underscores.
			String undies = name.replaceAll("[\\W]", "_");
			// Replace multiple underscores with single underscore. 
			String result = undies.replaceAll("_+", "_");
			return result;
		}
		public PeruserARQ_Harness makeHarness() {
			theLogger.debug("Creating harness for " + myTestName);
			PeruserARQ_Harness harness = PeruserARQ_Harness.makeFromManifestConfig(myURI, null, myManifestNode.myManifestTree.myFileManager, myManifest, 
						myItem, myTestName, myAction, myResult);
						
			return harness;
		}
		public Test makeJUnitTestCase () {
			Test tc = null;
			PeruserARQ_Harness harness = makeHarness();
			if (harness != null) {
				tc = new PeruserARQTestCase(harness);
			}
			return tc;
		}
	}
	
    static public TestSuite suite() {
        return new PeruserARQTestSuite();
    }

	private PeruserARQTestSuite()
	{
        super("ARQ");
        // There are only about 20 cache hits and they are
        // all local files (which are in the OS filing system cache).
        // The saving of parsing overhead is also small. 
        
        //FileManager.get().setModelCaching(false) ;
        
        // Tests should be silent.
        NodeValue.VerboseWarnings = false ;
        E_Function.WarnOnUnknownFunction = false ;
        
		/* *****************************
		 
        addTest(TS_Internal.suite() );
        addTest(TestExpressionsARQ.suite()) ;
        addTest(TS_Syntax.suite()) ;
        
        
        // The DAWG official tests (some may be duplicated in ARQ test suite
        // but this should be the untouched versions, just changes to
        // the manifests for rdfs:labels).
        addTest(TS_DAWG.suite()) ;
        
        // The RDQL engine
        addTest(TS_RDQL.suite()) ;
        
        addTest(TestMisc.suite()) ;

		***************/

		
        // Scripted tests for SPARQL and ARQ.
		
		ManifestTree tree = new ManifestTree(testDirARQ, rootPathTail);
		theLogger.info("**************************************Loading Manifests");
		tree.loadAllManifests();
		theLogger.info("**************************************Appending Junits");
        tree.appendJUnitTestCases(this);
		theLogger.info("**************************************TestSuite constructor finished");
        // Various others
        //addTest(miscSuite()) ;
    }
	
	
	public static void main(String args[]) throws Throwable {
		
		String entryRelativeURI = null;
		if (args.length == 1) {
			entryRelativeURI = args[0];
		}
		ManifestTree tree = new ManifestTree(testDirARQ, rootPathTail);
		theLogger.info("************************************** Start Loading Manifests");
		tree.loadAllManifests();
		theLogger.info("************************************** Done Loading Manifests");
		
		if (entryRelativeURI != null) {
			ManifestEntry me = tree.findManifestEntry(entryRelativeURI);
			theLogger.info("Found ManifestEntry at " + entryRelativeURI + " [" + me + "]"); 
			PeruserARQ_Harness ah = me.makeHarness();
			ah.runAndDump();
		} else {
			tree.runAndDumpAll();
		}
	}

}


/**
 *  Note:  Some software in this file is copyright by Hewlett Packard Company, LP 
 *  and is redistributed in MODIFIED form according to the terms of the following 
 *  license. 
 */
 
 /*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */





