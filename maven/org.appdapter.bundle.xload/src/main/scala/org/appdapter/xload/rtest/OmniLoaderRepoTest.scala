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

package org.appdapter.fancy.rtest

import org.appdapter.demo.DemoBrowserUI
import org.appdapter.xload.rspec.GoogSheetRepoSpec
/*
class this
  extends GoogSheetRepo(directoryModel, fmcls)
  with RepoOper.ReloadableDataset {

  myRepoSpecForRef = myRepoSpecStart
  myDebugNameToStr = myDebugNameIn
  //myBasePath = myBasePathIn

  def this(directoryModel: Model) =     this(null, null, null, directoryModel, null)

  def this(directoryModel: Model, fmcls: java.util.List[ClassLoader]) =
    this(null, null, null, directoryModel, fmcls)

  def this(myRepoSpecStart: RepoSpec, myDebugNameIn: String, directoryModel: Model, fmcls: java.util.List[ClassLoader]) = {
    this(myRepoSpecStart, myDebugNameIn, myDebugNameIn, directoryModel, fmcls)
  }

  def reloadAllModels = reloadAllModelsFromDir

  def reloadSingleModel(n: String) = { reloadSingleModelByName(n) }

  override def getMainQueryDataset(): Dataset = {
    ensureUpdated();
    super.getMainQueryDataset();
  }

}
*/ /*
class SimplistRepoSpec(val wd: Repo.WithDirectory) extends RepoSpec {
  override def getDirectoryModel = wd.getDirectoryModel
  override def makeRepo = wd
  override def toString(): String = {
    "SimplestSpec[" + wd + "]";
  }

}
*/
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

  final val CHAN_BIND_GRAPH_QN = "csi:chan_sheet_77";

  final val GROUP_KEY_CHAN_BIND = "ChannelBindingGroupId";
  final val CHAN_GROUP_QN = "csi:dm_chan_group_22";

  final val DIRECT_BEHAV_GRAPH_QN = "csi:behav_file_82";

  final val BEHAV_STEP_GRAPH_QN = "csi:behavStep_sheet_77";
  final val BEHAV_SCENE_GRAPH_QN = "csi:behavScene_sheet_77";
  final val DERIVED_BEHAV_GRAPH_QN = "csi:merged_model_5001";

  final val QUERY_SOURCE_GRAPH_QN_FOR_TEST = "ccrt:qry_sheet_77";
  final val PIPELINE_GRAPH_QN_FOR_TEST = "csi:pipeline_sheet_77";
  final val PIPELINE_QUERY_QN_FOR_TEST = "ccrt:find_pipes_77";

  final val GROUP_KEY_SCENE_SPEC = "SceneSpecGroupId";
  final val SCENE_GROUP_QN = "csi:scene_group_33";

  final val GROUP_KEY_THEATER = "TheaterGroupId";
  final val THEATER_GROUP_QN = "csi:theater_group_44";

  def main(args: Array[String]): Unit = {
    print("Start Whackamole");
    val repoNav = DemoBrowserUI.makeDemoNavigatorCtrl(args);
    print("Create a Goog Sheet Spec");
    val repoSpec = new GoogSheetRepoSpec(OmniLoaderRepoTest.BMC_SHEET_KEY, OmniLoaderRepoTest.BMC_NAMESPACE_SHEET_NUM, OmniLoaderRepoTest.BMC_DIRECTORY_SHEET_NUM);
    val repo = repoSpec.getOrMakeRepo;
    //repo.loadSheetModelsIntoMainDataset();
    //repo.loadDerivedModelsIntoMainDataset(null);
    repoNav.addObject(repo.toString(), repo, true, false);
  }

}

