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

import org.appdapter.core.log.{ BasicDebugger }
import org.appdapter.core.name.{ Ident, FreeIdent }
import org.appdapter.core.item.{ Item }
import org.appdapter.core.store.{ Repo, InitialBinding, ModelClient }
import org.appdapter.help.repo.{ RepoClient, RepoClientImpl, InitialBindingImpl, SolutionList }
import org.appdapter.impl.store.{ FancyRepo }
import com.hp.hpl.jena.rdf.model.Model
import org.appdapter.core.store.dataset.RepoDatasetFactory
import org.appdapter.core.log.Debuggable
import org.appdapter.core.store.dataset.RepoDatasetFactory

/**
 * @author Stu B. <www.texpedient.com>
 *
 */

case class PipelineQuerySpec(val pplnAttrQueryQN: String, val pplnSrcQueryQN: String, val pplnGraphQN: String)

object DerivedGraphNames {
  // val		OPCODE_UNION = "UNION";
  val V_pipeID = "pipeID"
  val V_typeID = "typeID"

  /* opTypeID is one of ccrt:UnionModel, 
	 * useTypeID is one of ccrt:BehaviorModel, 	 */
  //	import org.cogchar.name.dir.NamespaceDir;
  val T_union = new FreeIdent(RepoSpecDefaultNames.NS_CCRT_RT + "UnionModel");
  val P_sourceModel = new FreeIdent(RepoSpecDefaultNames.NS_CCRT_RT + "sourceModel");
}

class DerivedGraphSpec(val myTargetGraphTR: TypedResrc, var myInGraphIDs: Set[Ident]) extends BasicDebugger {
  override def toString(): String = {
    "DerivedGraphSpec[targetTR=" + myTargetGraphTR + ", inGraphs=" + myInGraphIDs + "]";
  }
  def isUnion(): Boolean = myTargetGraphTR.hasTypeMark(DerivedGraphNames.T_union)
  def getStructureTypeID(): Ident = {
    if (isUnion()) {
      DerivedGraphNames.T_union
    } else {
      DerivedGraphNames.T_union
    }
  }
  def makeDerivedModelProvider(sourceRC: RepoClient): BoundModelProvider = new DirectDerivedGraph(this, new ClientModelProvider(sourceRC))
  def makeDerivedModelProvider(srcRepo: Repo.WithDirectory): BoundModelProvider = new DirectDerivedGraph(this, new ServerModelProvider(srcRepo))
}

case class DirectRepoGraph(val myUpstreamGraphID: TypedResrc, val myUpstreamNMP: NamedModelProvider)
  extends BoundModelProvider {
  override def getModel() = myUpstreamNMP.getNamedModel(myUpstreamGraphID);
  override def getTypedName() = myUpstreamGraphID
}
case class IndirectDerivedGraph(myPipeQuerySpec: PipelineQuerySpec, val myPipeSpecRC: RepoClient,
  drvGraphID: Ident, val myUpstreamNMP: NamedModelProvider) extends BoundModelProvider {
  lazy val myDGSpec = DerivedGraphSpecReader.findOneDerivedGraphSpec(myPipeSpecRC, myPipeQuerySpec, drvGraphID)
  lazy val myDirectDG = new DirectDerivedGraph(myDGSpec, myUpstreamNMP)
  override def getModel() = myDirectDG.getModel
  override def getTypedName() = myDirectDG.getTypedName
}
case class DirectDerivedGraph(val mySpec: DerivedGraphSpec, val myUpstreamNMP: NamedModelProvider)
  extends BasicDebugger with BoundModelProvider {
  private var myCachedModel: Option[Model] = None
  override def getTypedName() = mySpec.myTargetGraphTR
  def getModel(): Model = {
    if (myCachedModel.isEmpty) {
      val m = makeModel
      myCachedModel = Some(m)
    }
    myCachedModel.get
  }
  private def makeModel(): Model = {
    mySpec.getStructureTypeID() match {
      case DerivedGraphNames.T_union => {
        var cumUnionModel = RepoDatasetFactory.createPrivateMemModel
        for (srcGraphID <- mySpec.myInGraphIDs) {
          val srcGraph = myUpstreamNMP.getNamedModel(srcGraphID)
          if (srcGraph == null) {
            getLogger().error("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% ERRORR!!!! %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% ERROR !! %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% \nUnknown srcGraphID = " + srcGraphID + " in " + myUpstreamNMP)
          } else {
            // TODO : when upgrading (to Jena v2.8?) use  ModelFactory.createUnion(..,..);
            cumUnionModel = cumUnionModel.union(srcGraph)
          }
        }
        cumUnionModel
      }
      case x => {
        getLogger().warn("Unknown structure type {}", x)
        RepoDatasetFactory.createPrivateMemModel()
      }
    }
  }
}

object DerivedGraphSpecReader extends BasicDebugger {

  // This form allows user to decide what repo/client to apply the spec against to yield actual DerivedGraph.
  def findOneDerivedGraphSpec(rc: RepoClient, pqs: PipelineQuerySpec, outGraphID: Ident): DerivedGraphSpec = {
    val dgSpecSet: Set[DerivedGraphSpec] = queryDerivedGraphSpecs(rc, pqs)    
    dgSpecSet.find(x => outGraphID.equals(x.myTargetGraphTR)).get
  }
  def makeAllDerivedModelProviders(rc: RepoClient, pqs: PipelineQuerySpec): Set[BoundModelProvider] = {
    val dgSpecSet = queryDerivedGraphSpecs(rc, pqs)
    dgSpecSet.map(_.makeDerivedModelProvider(rc))
  }
  def queryDerivedGraphSpecs(rc: RepoClient, pqs: PipelineQuerySpec): Set[DerivedGraphSpec] = {

    var pipeAttrSL: SolutionList = null;
    try {
      pipeAttrSL = rc.queryIndirectForAllSolutions(pqs.pplnAttrQueryQN, pqs.pplnGraphQN)
    } catch {
      case t: Throwable => {
        getLogger().error("Problem executing querySpec {} on repoClient {} ", Array[Object](pqs, rc))
        getLogger().error("Stack trace: ", t)
        return Set[DerivedGraphSpec]()
      }
    }

    val outPipeTypeSetsByID = new scala.collection.mutable.HashMap[Ident, Set[Ident]]()
    import scala.collection.JavaConversions._
    val pjl = pipeAttrSL.javaList
    getLogger().info("Got pipeAttribute list : {}", pjl)
    pjl foreach (psp => {
      // A pipe is the result of a single operation applied to a (poss. ordered by query) set of sources
      val outPipeID = psp.getIdentResultVar(DerivedGraphNames.V_pipeID)
      // Each pipe-spec will have one or more types, which may be viewed as classifiers of both how the pipe
      // is constructed (the type of pipe structure) and how its output is used (the type of pipe-outut contents).
      // For now we assume that it is viable to recognize the opTypeID and useTypeID during this read process.
      val aTypeID = psp.getIdentResultVar(DerivedGraphNames.V_typeID)
      val pipeTypeSet: Set[Ident] = if (outPipeTypeSetsByID.contains(outPipeID)) {
        outPipeTypeSetsByID.get(outPipeID).get + aTypeID
      } else {
        Set(aTypeID)
      }
      outPipeTypeSetsByID.put(outPipeID, pipeTypeSet)
    })
    val outDGSpecsByID = new scala.collection.mutable.HashMap[Ident, DerivedGraphSpec]()
    // In theory, there is no reason that we cannot use a FreeIdent, which is oft done elsewhere.
    val pipeSpecGraphID = rc.getRepo.makeIdentForQName(pqs.pplnGraphQN)
    val pipeSpecModel = rc.getRepo.getNamedModel(pipeSpecGraphID)
    var dgSpecSet = Set[DerivedGraphSpec]()
    for ((outPipeKeyID, typeSet) <- outPipeTypeSetsByID) {
      val outPipeDGSpecRes = pipeSpecModel.getResource(outPipeKeyID.getAbsUriString())
      val typedRes = new JenaTR(outPipeDGSpecRes, typeSet)
      val linkedPipeSrcItems = typedRes.getLinkedItemSet(DerivedGraphNames.P_sourceModel, Item.LinkDirection.FORWARD);
      // Note JavaConverters is not the same as JavaConversions
      import scala.collection.JavaConverters._
      val linkedUpstreamPipeIDSet: Set[Ident] = linkedPipeSrcItems.asScala.map(_.asInstanceOf[Ident]).toSet
      val dgSpec = new DerivedGraphSpec(typedRes, linkedUpstreamPipeIDSet)
      dgSpecSet = dgSpecSet + dgSpec
    }
    dgSpecSet
  }
}