/*
 *  Copyright 2014 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.fancy.rtest

import org.appdapter.fancy.log.VarargsLogging

import org.appdapter.fancy.gportal._

/**
 * @author Stu B. <www.texpedient.com>
 */

object RemoteFusekiTest extends VarargsLogging {
	val remotePortalBaseURL = "http://lima.nodeset.com:4001/temp_major_L5SG_4001";
	val remoteGraphStoreURL = remotePortalBaseURL + "/data"
	val remoteQueryURL = remotePortalBaseURL + "/query"
	val remoteUpdateURL = remotePortalBaseURL + "/update"
	val remoteUploadURL = remotePortalBaseURL + "/upload"

	def main(args: Array[String]) : Unit = {
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);
		
		dumpGraphStats()
	}
	def dumpGraphStats() {
		val remotePortal = new LazyRemoteDelegatingPortal(remoteGraphStoreURL, remoteQueryURL, remoteUpdateURL)
		val rSupplier : GraphSupplier = remotePortal.getSupplier
		val rStats : List[SuppliedGraphStat] = rSupplier.fetchStats(None)
		info2("Got {} stats: {}", rStats.size : java.lang.Integer, rStats)
	}
				
}
