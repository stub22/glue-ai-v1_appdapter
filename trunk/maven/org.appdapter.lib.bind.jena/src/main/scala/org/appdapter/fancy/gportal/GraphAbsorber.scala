/*
 *  Copyright 2014 by The Friendularity Project (www.friendularity.org).
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

package org.appdapter.fancy.gportal

/**
 * @author Stu B. <www.texpedient.com>
 */

import com.hp.hpl.jena.rdf.model.{ Model, Resource, Literal }
import com.hp.hpl.jena.query.{DatasetAccessor, DatasetAccessorFactory}
import com.hp.hpl.jena.query.{ Dataset, DatasetFactory, ReadWrite }
import org.appdapter.fancy.log.VarargsLogging

trait GraphAbsorber extends GraphPortal {
	// These options are possible with both remote and local graph hosts.
	// add = HTTP "post"
	def addStatementsToNamedModel(graphURI : String, srcModel : Model);
	// replace = HTTP "put"
	def replaceNamedModel(graphURI : String, srcModel : Model);
}
/* TODO:  Since this is local we may need TX support.
 * For the moment, assume it is handled by caller.
 */
trait DsaccGraphAbsorber extends GraphAbsorber with DsaccGraphPortal {
	
	override def addStatementsToNamedModel(graphURI : String, srcModel : Model) {
		getDatasetAccessor.add(graphURI, srcModel)
	}
	override def replaceNamedModel(graphURI : String, srcModel : Model) {
		getDatasetAccessor.putModel(graphURI, srcModel)
	}
}
trait LocalGraphAbsorber extends DsaccGraphAbsorber with LocalGraphPortal 
/* {
	lazy val myDacc : DatasetAccessor = DatasetAccessorFactory.create(myDataset)
	override def getDatasetAccessor = myDacc
} */
trait RemoteGraphAbsorber extends DsaccGraphAbsorber with RemoteGraphPortal 
/*{
	lazy val myDacc : DatasetAccessor = DatasetAccessorFactory.createHTTP(myServiceURL);
	override def getDatasetAccessor = myDacc
} 
*/
/*
object AbsorberFuncs {
	def copyRepoGraphsToLocalDset_OneTx(inputRepo : Repo.WithDirectory, tgtUriChooser : Repo.GraphStat => Option[String],
									tgtDataset : Dataset, replaceInTgt : Boolean) { 
		// TODO:  add respect for existing xact.
		if (!tgtDataset.supportsTransactions) {
			throw new RuntimeException("This method expects a transaction-aware target dataset")
		}
		tgtDataset.begin(ReadWrite.WRITE);
		val locAbsorber = new LocalGraphAbsorber(tgtDataset);
		
		val inputMQDset = inputRepo.getMainQueryDataset
		inputMQDset.begin(ReadWrite.READ);
		try {
			copyRepoGraphsToAbsorber(inputRepo, tgtUriChooser, locAbsorber, replaceInTgt)
			tgtDataset.commit
		} catch {
			case t : Throwable => {
				getLogger.error("Caught error, rolling back", t)
				tgtDataset.abort()
			}
		} finally {
			// If we haven't committed or aborted, this will show:
			// 21571 [main] WARN TDB  - Transaction not commited or aborted: Transaction: 2 : Mode=WRITE : State=ACTIVE : --mem--/
			// ...and implicity abort.
			tgtDataset.end()
			inputMQDset.end();
		}
	}
	
}
*/