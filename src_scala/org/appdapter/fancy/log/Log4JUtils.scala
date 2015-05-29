/*
 *  Copyright 2015 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.fancy.log



object Log4JUtils {
	def setupScanTestLogging() : Unit = { 
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);		
		setLogLevelToInfoForClz(classOf[org.ontoware.rdf2go.impl.jena.TypeConversion])
		setLogLevelToInfoForClz(classOf[org.ontoware.rdfreactor.runtime.RDFReactorRuntime])
		setLogLevelToInfoForClz(classOf[org.ontoware.rdfreactor.runtime.ReactorRuntimeEntity])
		setLogLevelToInfoForClz(classOf[com.hp.hpl.jena.shared.LockMRSW])
		// These 2 are *packages*, so we can't use the classOf trick.
		setLogLevelToInfoForPkg("com.hp.hpl.jena.tdb.transaction")
		setLogLevelToInfoForPkg("org.apache.jena.info")
	}
	private def setLogLevelToInfoForClz(clz: Class[_]) {
		org.apache.log4j.Logger.getLogger(clz).setLevel(org.apache.log4j.Level.INFO)
	}
	private def setLogLevelToInfoForPkg(pkgName : String) {
		org.apache.log4j.Logger.getLogger(pkgName).setLevel(org.apache.log4j.Level.INFO)
	}
}
