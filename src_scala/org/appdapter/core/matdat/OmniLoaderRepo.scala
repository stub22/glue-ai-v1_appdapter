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

import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.store.{ Repo, RepoOper }
import org.appdapter.demo.DemoBrowserUI
import org.appdapter.help.repo.RepoClientImpl
import org.appdapter.impl.store.QueryHelper

import com.hp.hpl.jena.query.{ Dataset, QuerySolution, ResultSetFactory, ResultSet }
import com.hp.hpl.jena.rdf.model.{ Resource, RDFNode, ModelFactory, Model, Literal }
import java.io.Reader
import org.appdapter.core.store.{ FileStreamUtils }
import org.appdapter.impl.store.QueryHelper
import scala.collection.JavaConversions.asScalaBuffer
import org.appdapter.impl.store.DirectRepo
import org.appdapter.core.store.Repo
import org.appdapter.core.log.BasicDebugger

class OmniLoaderRepo(myRepoSpecStart: RepoSpec, myDebugNameIn: String, myBasePathIn: String,
  directoryModel: Model, fmcls: java.util.List[ClassLoader] = null)
  extends SheetRepo(directoryModel, fmcls)
  with RepoOper.Reloadable {

  myRepoSpecForRef = myRepoSpecStart
  myDebugNameToStr = myDebugNameIn
  myBasePath = myBasePathIn

  def this(directoryModel: Model) =
    this(null, null, null, directoryModel, null)

  def this(directoryModel: Model, fmcls: java.util.List[ClassLoader]) =
    this(null, null, null, directoryModel, fmcls)

  def this(myRepoSpecStart: RepoSpec, myDebugNameIn: String, directoryModel: Model, fmcls: java.util.List[ClassLoader]) = {
    this(myRepoSpecStart, myDebugNameIn, myDebugNameIn, directoryModel, fmcls)
  }

  override def loadSheetModelsIntoMainDataset() {
    super.loadSheetModelsIntoMainDataset();
  }

  override def getDirectoryModel(): Model = {
    super.getDirectoryModel
  }

  def reloadAllModels = reloadAllModelsFromDir

  def reloadSingleModel(n: String) = { reloadSingleModelByName(n) }

  override def getMainQueryDataset(): Dataset = {
    ensureUpdatedPrivate();
    super.getMainQueryDataset();
  }

  def loadPipeline(pplnGraphQN: String) = {
    val mainDset: Dataset = getMainQueryDataset().asInstanceOf[Dataset];
    PipelineRepoLoader.loadPipeline(pplnGraphQN, this, mainDset);
  }

}

/// this is a registerable loader
class PipelineRepoLoader extends InstallableRepoReader {
  override def getContainerType() = "cc:PipelineModel"
  override def getSheetType() = "ccrt:UnionModel"
  override def isDerivedLoader() = true
  override def loadModelsIntoTargetDataset(repo: Repo.WithDirectory, mainDset: Dataset, dirModel: Model, fileModelCLs: java.util.List[ClassLoader]) {
    PipelineRepoLoader.loadSheetModelsIntoTargetDataset(repo, mainDset, dirModel, fileModelCLs)
  }
}

object PipelineRepoLoader extends BasicDebugger {

  def loadSheetModelsIntoTargetDataset(repo: Repo.WithDirectory, mainDset: Dataset, myDirectoryModel: Model, fileModelCLs: java.util.List[ClassLoader]) = {

    val nsJavaMap: java.util.Map[String, String] = myDirectoryModel.getNsPrefixMap()

    val msqText = """
			select ?model 
				{
					?model a cc:PipelineModel;
				}
		"""

    val msRset = QueryHelper.execModelQueryWithPrefixHelp(myDirectoryModel, msqText);
    import scala.collection.JavaConversions._;
    while (msRset.hasNext()) {
      val qSoln: QuerySolution = msRset.next();

      //val repoRes : Resource = qSoln.getResource("repo");
      val modelRes = qSoln.get("model");
      val modelName = modelRes.asResource().asNode().getURI

      val dbgArray = Array[Object](modelRes, modelName);
      loadPipeline(modelName, repo, mainDset);
      getLogger.warn("DerivedModelsIntoMainDataset modelRes={}, modelName={}", dbgArray);
      //val msRset = QueryHelper.execModelQueryWithPrefixHelp(mainDset.getNamedModel(modelName), msqText2);

      // DerivedGraphSpecReader.queryDerivedGraphSpecs(getRepoClient,DerivedGraphSpecReader.PIPELINE_QUERY_QN,modelName)
    }
  }

  def loadPipeline(pplnGraphQN: String, repo: Repo.WithDirectory, mainDset: Dataset) = {

    val rc = new RepoClientImpl(repo, RepoSpecDefaultNames.DFLT_TGT_GRAPH_SPARQL_VAR, RepoSpecDefaultNames.DFLT_QRY_SRC_GRAPH_QN)
    val pqs = new PipelineQuerySpec(RepoSpecDefaultNames.PIPE_ATTR_QQN, RepoSpecDefaultNames.PIPE_SOURCE_QQN, pplnGraphQN);
    val dgSpecSet: Set[DerivedGraphSpec] = DerivedGraphSpecReader.queryDerivedGraphSpecs(rc, pqs);

    for (dgSpec <- dgSpecSet) {
      val derivedModelProvider = dgSpec.makeDerivedModelProvider(repo);
      val derivedModel = derivedModelProvider.getModel()
      // null for now
      replaceOrUnion(mainDset, null, pplnGraphQN, derivedModel)
    }
  }

  def replaceOrUnion(mainDset: Dataset, unionOrReplaceRes: Resource, graphURI: String, sheetModel: Model) {
    RepoOper.replaceNamedModel(mainDset, graphURI, sheetModel, unionOrReplaceRes)
  }
}

class SimplistRepoSpec(val wd: Repo.WithDirectory) extends RepoSpec {
  override def makeRepo(): Repo.WithDirectory = {
    wd;
  }
  override def toString(): String = {
    "SimplestSpec[" + wd + "]";
  }
}

object OmniLoaderRepoTest {

  // These constants are used to test the ChanBinding model found in "GluePuma_BehavMasterDemo"
  //   https://docs.google.com/spreadsheet/ccc?key=0AlpQRNQ-L8QUdFh5YWswSzdYZFJMb1N6aEhJVWwtR3c
  final val BMC_SHEET_KEY = "0AlpQRNQ-L8QUdFh5YWswSzdYZFJMb1N6aEhJVWwtR3c"
  final val BMC_NAMESPACE_SHEET_NUM = 4
  final val BMC_DIRECTORY_SHEET_NUM = 3

  // These constants are used to test the ChanBinding model found in "GluePuma_BehavMasterDemo"
  //   https://docs.google.com/spreadsheet/ccc?key=0AlpQRNQ-L8QUdFh5YWswSzdYZFJMb1N6aEhJVWwtR3c
  // When exported to Disk
  final val BMC_WORKBOOK_PATH = "GluePuma_BehavMasterDemo.xlsx"
  final val BMC_NAMESPACE_SHEET_NAME = "Nspc"
  final val BMC_DIRECTORY_SHEET_NAME = "Dir"

  final val QUERY_SOURCE_GRAPH_QN = "ccrt:qry_sheet_77";
  final val CHAN_BIND_GRAPH_QN = "csi:chan_sheet_77";

  final val GROUP_KEY_CHAN_BIND = "ChannelBindingGroupId";
  final val CHAN_GROUP_QN = "csi:dm_chan_group_22";

  final val DIRECT_BEHAV_GRAPH_QN = "csi:behav_file_82";

  final val BEHAV_STEP_GRAPH_QN = "csi:behavStep_sheet_77";
  final val BEHAV_SCENE_GRAPH_QN = "csi:behavScene_sheet_77";
  final val DERIVED_BEHAV_GRAPH_QN = "csi:merged_model_5001";

  final val PIPELINE_GRAPH_QN = "csi:pipeline_sheet_77";
  final val PIPELINE_QUERY_QN = "ccrt:find_pipes_77";

  final val GROUP_KEY_SCENE_SPEC = "SceneSpecGroupId";
  final val SCENE_GROUP_QN = "csi:scene_group_33";

  final val GROUP_KEY_THEATER = "TheaterGroupId";
  final val THEATER_GROUP_QN = "csi:theater_group_44";

  def main(args: Array[String]): Unit  = {
    print("Start Whackamole");
    val repoNav = DemoBrowserUI.makeDemoNavigatorCtrl(args);
    print("Create a Goog Sheet Spec");
    val repoSpec = new GoogSheetRepoSpec(OmniLoaderRepoTest.BMC_SHEET_KEY, OmniLoaderRepoTest.BMC_NAMESPACE_SHEET_NUM, OmniLoaderRepoTest.BMC_DIRECTORY_SHEET_NUM);
    val repo = repoSpec.makeRepo;
    repo.loadSheetModelsIntoMainDataset();
    repo.loadDerivedModelsIntoMainDataset(null);
    repoNav.addObject(repo.toString(), repo, true, false);
  }

}
