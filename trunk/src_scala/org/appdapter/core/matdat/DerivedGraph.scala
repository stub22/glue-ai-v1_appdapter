/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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

package org.appdapter.core.matdat
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.name.Ident
import org.appdapter.core.name.Ident
import org.appdapter.core.store.Repo
import org.appdapter.help.repo.RepoClient
import org.appdapter.help.repo.SolutionList

/**
 * @author Stu B. <www.texpedient.com>
 */

class DerivedGraphSpec(val myTargetID: Ident, val myOp: String, var myInGraphIDs: List[Ident]) extends BasicDebugger {
  override def toString(): String = {
    "DerivedGraphSpec[targetID=" + myTargetID + ", inGraphs=" + myInGraphIDs + "]";
  }

  def makeDerivedModel(sourceRepo: Repo): Model = {
    // TODO : match on myOp
    // TODO : when upgrading use  ModelFactory.createUnion();
    var cumUnionModel = ModelFactory.createDefaultModel();
    for (srcGraphID <- myInGraphIDs) {
      val srcGraph = sourceRepo.getNamedModel(srcGraphID)
      cumUnionModel = cumUnionModel.union(srcGraph)
    }
    cumUnionModel
  }
}

class DerivedGraph extends BasicDebugger {

}

object DerivedGraphSpecReader extends BasicDebugger {

  /**
   * pplnQueryQN: The QName of a query in the presumed "Queries" model/tab
   * pplnGraphQN:  The QName of a graph = model = tab, as registered with dset and/or dirModel
   */
  def queryDerivedGraphSpecs(rc: RepoClient, pplnQueryQN: String, pplnGraphQN: String): Set[DerivedGraphSpec] = {
    val solList: SolutionList = rc.queryIndirectForAllSolutionsJ(pplnQueryQN, pplnGraphQN).asInstanceOf[SolutionList];

    val resultMMap = new scala.collection.mutable.HashMap[Ident, DerivedGraphSpec]()
    val resultJMap = new java.util.HashMap[Ident, DerivedGraphSpec]();
    import scala.collection.JavaConversions._
    val solJList = solList.javaList
    getLogger().info("Got dgSpec-piece solJList: {}", solJList)
    solJList foreach (psp => {
      val pipeID = psp.getIdentResultVar("pipeID")
      val sourceID = psp.getIdentResultVar("sourceID")
      val pipeSpec = if (resultMMap.contains(pipeID)) {
        resultMMap.get(pipeID).get
      } else {
        val opCode = "UNKNOWN"
        val freshPipeSpec = new DerivedGraphSpec(pipeID, opCode, List());
        resultMMap.put(pipeID, freshPipeSpec)
        freshPipeSpec
      }
      pipeSpec.myInGraphIDs = sourceID :: pipeSpec.myInGraphIDs
    })
    resultMMap.values.toSet
  }
}