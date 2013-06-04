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

import com.hp.hpl.jena.query.Dataset
import com.hp.hpl.jena.rdf.model.Model
import org.appdapter.core.store.{ RepoSpec, RepoOper, Repo }
import org.appdapter.gui.demo.DemoBrowser
import org.appdapter.gui.repo.RepoBoxImpl
import org.appdapter.gui.box.ScreenBoxImpl
import com.hp.hpl.jena.query.QuerySolution
import org.appdapter.impl.store.QueryHelper
import org.appdapter.help.repo.RepoClientImpl
import com.hp.hpl.jena.query.DataSource
import org.appdapter.core.boot.ClassLoaderUtils
import org.osgi.framework.BundleContext
import org.appdapter.impl.store.DirectRepo
import org.appdapter.core.log.BasicDebugger

///import org.cogchar.impl.trigger.Whackamole
//import org.cogchar.name.behavior.{ MasterDemoNames };

class OmniLoaderRepo_1_1_1(myRepoSpecStart: RepoSpec, myDebugNameIn: String, myBasePathIn: String,
  directoryModel: Model, fmcls: java.util.List[ClassLoader])
  extends XLSXSheetRepo(directoryModel: Model, fmcls)
  with RepoOper.Reloadable {

  def this(myRepoSpecStart: RepoSpec, myDebugNameIn: String, directoryModel: Model, fmcls: java.util.List[ClassLoader]) = {
    this(myRepoSpecStart, myDebugNameIn, myDebugNameIn, directoryModel, fmcls)
  }

  myRepoSpecForRef = myRepoSpecStart
  myDebugNameToStr = myDebugNameIn
  myBasePath = myBasePathIn;

  /* 
 override def loadSheetModelsIntoMainDataset() {
    super.loadSheetModelsIntoMainDataset();
  }
  */

  override def getDirectoryModel(): Model = {
    super.getDirectoryModel
  }

  def reloadAllModels = reloadAllModelsFromDir

  def reloadSingleModel(n: String) = { reloadSingleModelByName(n) }

  override def getMainQueryDataset(): Dataset = {
    ensureUpdatedPrivate();
    super.getMainQueryDataset();
  }

  /*
    def loadDerivedModelsIntoMainDataset() = {
    val mainDset: DataSource = getMainQueryDataset().asInstanceOf[DataSource];
    val nsJavaMap: java.util.Map[String, String] = myDirectoryModel.getNsPrefixMap()
    val dirModel = getDirectoryModel;
    XLSXSheetRepoLoader.loadSheetModelsIntoTargetDataset(this, mainDset, dirModel, fileModelCLs);
    }
  */

  def loadPipeline(pplnGraphQN: String) = {
    val mainDset: DataSource = getMainQueryDataset().asInstanceOf[DataSource];
    PipelineRepoLoader.loadPipeline(pplnGraphQN, this, mainDset);
  }
}

/// this is a registerable loader
class PipelineRepoLoader extends InstallableRepoReader {
  override def getContainerType() = "cc:PipelineModel"
  override def getSheetType() = "ccrt:UnionModel"
  override def loadModelsIntoTargetDataset(repo: Repo.WithDirectory, mainDset: DataSource, dirModel: Model, fileModelCLs: java.util.List[ClassLoader]) {
    PipelineRepoLoader.loadSheetModelsIntoTargetDataset(repo, mainDset, dirModel, fileModelCLs)
  }
}

object PipelineRepoLoader extends BasicDebugger {

  def loadSheetModelsIntoTargetDataset(repo: Repo.WithDirectory, mainDset: DataSource, myDirectoryModel: Model, fileModelCLs: java.util.List[ClassLoader]) = {

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

  def loadPipeline(pplnGraphQN: String, repo: Repo.WithDirectory, mainDset: DataSource) = {

    val rc = new RepoClientImpl(repo, DefaultRepoSpecDefaultNames_1_1_1.DFLT_TGT_GRAPH_SPARQL_VAR, DefaultRepoSpecDefaultNames_1_1_1.DFLT_QRY_SRC_GRAPH_QN)

    val pqs = new PipelineQuerySpec_1_1_1(DefaultRepoSpecDefaultNames_1_1_1.PIPE_ATTR_QQN, DefaultRepoSpecDefaultNames_1_1_1.PIPE_SOURCE_QQN, pplnGraphQN);
    val dgSpecSet: Set[DerivedGraphSpec_1_1_1] = DerivedGraphSpecReader_1_1_1.queryDerivedGraphSpecs(rc, pqs);

    for (dgSpec <- dgSpecSet) {
      val model = dgSpec.makeDerivedModel(repo)
      mainDset.replaceNamedModel(pplnGraphQN, model)
    }
  }
}

class SimplistRepoSpec_1_1_1(val wd: Repo.WithDirectory) extends RepoSpec_1_1_1 {
  override def makeRepo(): Repo.WithDirectory = {
    wd;
  }
  override def toString(): String = {
    "SimplestSpec[" + wd + "]";
  }
}

object OmniLoaderRepoTest_1_1_1 {

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

  def main(args: Array[String]) = {
    print("Start Whackamole");
    val repoNav = DemoBrowser.makeDemoNavigatorCtrl(args);
    print("Create a Goog Sheet Spec");
    val repoSpec = new GoogSheetRepoSpec_1_1_1(OmniLoaderRepoTest_1_1_1.BMC_SHEET_KEY, OmniLoaderRepoTest_1_1_1.BMC_NAMESPACE_SHEET_NUM, OmniLoaderRepoTest_1_1_1.BMC_DIRECTORY_SHEET_NUM);
    val repo = repoSpec.makeRepo;
    repo.loadSheetModelsIntoMainDataset();
    repo.loadDerivedModelsIntoMainDataset(null);
    print("Make RepoFabric");
    val rf = new RepoFabric_1_1_1();
    print("Make FabricBox");
    val fb = new FabricBox_1_1_1(rf);
    fb.setShortLabel("Short Label")
    // Add this as an "entry" in the RepoFabric 
    print("Add to Entry");
    rf.addEntry(new SimplistRepoSpec_1_1_1(repo))
    print("Resync");
    val tp = repoNav.getBoxPanelTabPane()
    val boxed = new ScreenBoxImpl(repo.toString(), repo);
    tp.showScreenBox(boxed, boxed.getDisplayType());
    java.lang.Thread.sleep(60000000);
  }

}
