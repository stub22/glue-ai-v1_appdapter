/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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
 *  Copyright 2012 by The Appdapter Project (www.appdapter.org).
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

import org.appdapter.core.boot.ClassLoaderUtils
import org.appdapter.core.name.{Ident, FreeIdent}
import org.appdapter.core.store.{RepoSpec, Repo}
import org.appdapter.gui.demo.DemoBrowser
import org.appdapter.help.repo.RepoClientImpl
import org.osgi.framework.BundleContext

/**
 * @author Stu B. <www.texpedient.com>
 */

object DefaultRepoSpecDefaultNames_1_1_1 {
	// Formal prefix for Robokind 2012 runtime 
	val RKRT_NS_PREFIX = "urn:ftd:robokind.org:2012:runtime#"
	// Formal prefix for Cogchar 2012 runtime 
	val NS_CCRT_RT = "urn:ftd:cogchar.org:2012:runtime#"
	// Formal prefix for Cogchar 2012 goody
	val GOODY_NS = "urn:ftd:cogchar.org:2012:goody#"
	// Formal prefix for Cogchar Web
	//public static String WEB_NS = "http://www.cogchar.org/lift/config#";
	
	// Less formal web prefix still widely used:
	val NSP_Root = "http://www.cogchar.org/"
	
	// These 2 string constants establish repo-client wrapper defaults, giving a default 
  // query context to easily fetch from.
  // Either value may be bypassed/overidden using either 
  //      A) Overrides of the RepoSpec methods below 
  //   or B) The more general forms of queryIndirect_. 
  /**
   * // 1) Default query *source* graph QName used in directory model (Sheet or RDF).
   * // We read SPARQL text from this graph, which we use to query *other* graphs.
   * // This graph is typically not used as a regular data graph by  other low-order
   * // query operations, although there is no prohibition or protection from doing so
   * // at this time.   This query source graph may be overridden using the more general
   * // forms of queryIndirect_.
   */

  val DFLT_QRY_SRC_GRAPH_QN = "ccrt:qry_sheet_22"

  // 2) default variable name for a single target graph in a SPARQL query.
  // This is used in the convenience forms of queryIndirect that handle many common
  // use cases, wherein the query needs a single graph to operate on that is switched
  // by application logic or user selection.

  val DFLT_TGT_GRAPH_SPARQL_VAR = "qGraph"

  val PIPELINE_GRAPH_QN = "csi:pipeline_sheet_77";
  val PIPE_QUERY_QN = "ccrt:find_pipes_77";
  val PIPE_SOURCE_QUERY_QN = "ccrt:find_pipe_sources_78";
  val PIPE_ATTR_QQN = PIPE_QUERY_QN;
  val PIPE_SOURCE_QQN = PIPE_SOURCE_QUERY_QN;

  // These constants are used to test the ChanBinding model found in "GluePuma_BehavMasterDemo"
  //   https://docs.google.com/spreadsheet/ccc?key=0AlpQRNQ-L8QUdFh5YWswSzdYZFJMb1N6aEhJVWwtR3c
  final val BMC_SHEET_KEY = "0AlpQRNQ-L8QUdFh5YWswSzdYZFJMb1N6aEhJVWwtR3c"
  val BMC_NAMESPACE_SHEET_NUM = 4
  val BMC_DIRECTORY_SHEET_NUM = 3

  // These constants are used to test the ChanBinding model found in "GluePuma_BehavMasterDemo"
  //   https://docs.google.com/spreadsheet/ccc?key=0AlpQRNQ-L8QUdFh5YWswSzdYZFJMb1N6aEhJVWwtR3c
  // When exported to Disk
  final val BMC_WORKBOOK_PATH = "GluePuma_BehavMasterDemo.xlsx"
  val DFLT_NAMESPACE_SHEET_NAME = "Nspc"
  val DFLT_DIRECTORY_SHEET_NAME = "Dir"

  val TGT_GRAPH_SPARQL_VAR = DFLT_TGT_GRAPH_SPARQL_VAR; // "qGraph"

  def main(args: Array[String]) = {
    val fileResModelCLs: java.util.List[ClassLoader] =
      ClassLoaderUtils.getFileResourceClassLoaders(null, ClassLoaderUtils.ALL_RESOURCE_CLASSLOADER_TYPES);
    val repoSpec = new OnlineSheetRepoSpec_1_1_1(DefaultRepoSpecDefaultNames_1_1_1.BMC_SHEET_KEY, DefaultRepoSpecDefaultNames_1_1_1.BMC_NAMESPACE_SHEET_NUM, DefaultRepoSpecDefaultNames_1_1_1.BMC_DIRECTORY_SHEET_NUM, fileResModelCLs);
    val repo: OmniLoaderRepo_1_1_1 = repoSpec.makeRepo.asInstanceOf[OmniLoaderRepo_1_1_1];

    print("Starting Whackamole");
    val repoNav = DemoBrowser.makeDemoNavigatorCtrl(args);
    repoNav.showScreenBox(repo);
    java.lang.Thread.sleep(60000000);
  }

  def makeBMC_RepoSpec(ctx: BundleContext): OnlineSheetRepoSpec_1_1_1 = {
    val fileResModelCLs: java.util.List[ClassLoader] =
      ClassLoaderUtils.getFileResourceClassLoaders(ctx, ClassLoaderUtils.ALL_RESOURCE_CLASSLOADER_TYPES);
    makeBMC_RepoSpec(fileResModelCLs);
  }
  def makeBMC_RepoSpec(fileResModelCLs: java.util.List[ClassLoader]): OnlineSheetRepoSpec_1_1_1 = {
    new OnlineSheetRepoSpec_1_1_1(BMC_SHEET_KEY, BMC_NAMESPACE_SHEET_NUM, BMC_DIRECTORY_SHEET_NUM, fileResModelCLs);
  }

  def makeBMC_OfflineRepoSpec(ctx: BundleContext): OfflineXlsSheetRepoSpec_1_1_1 = {
    val fileResModelCLs: java.util.List[ClassLoader] =
      ClassLoaderUtils.getFileResourceClassLoaders(ctx, ClassLoaderUtils.ALL_RESOURCE_CLASSLOADER_TYPES);
    makeBMC_OfflineRepoSpec(fileResModelCLs);
  }

  def makeBMC_OfflineRepoSpec(fileResModelCLs: java.util.List[ClassLoader]): OfflineXlsSheetRepoSpec_1_1_1 = {
    new OfflineXlsSheetRepoSpec_1_1_1(BMC_WORKBOOK_PATH, DFLT_NAMESPACE_SHEET_NAME, DFLT_DIRECTORY_SHEET_NAME, fileResModelCLs);
  }

}

abstract class RepoSpec_1_1_1 extends RepoSpec {
 
  def makeRepo : Repo.WithDirectory
  
  override def makeRepoClient(repo: Repo.WithDirectory): RepoClientImpl = {
    new RepoClientImpl(repo, getDfltTgtGraphSparqlVarName, getDfltQrySrcGraphQName);
  }
  
  def getDfltQrySrcGraphQName = DefaultRepoSpecDefaultNames_1_1_1.DFLT_QRY_SRC_GRAPH_QN;
  def getDfltTgtGraphSparqlVarName: String = DefaultRepoSpecDefaultNames_1_1_1.DFLT_TGT_GRAPH_SPARQL_VAR;
}

class OnlineSheetRepoSpec_1_1_1(sheetKey: String, namespaceSheetNum: Int, dirSheetNum: Int,
  fileModelCLs: java.util.List[ClassLoader] = null) extends RepoSpec_1_1_1 {
  override def makeRepo(): SheetRepo = {
    GoogSheetRepo.makeGoogSheetRepo(sheetKey, namespaceSheetNum, dirSheetNum, fileModelCLs, this)
  }
  override def toString: String = "goog:/" + sheetKey + "/" + namespaceSheetNum + "/" + dirSheetNum
}

class GoogSheetRepoSpec_1_1_1(sheetKey: String, namespaceSheetNum: Int, dirSheetNum: Int,
  fileModelCLs: java.util.List[ClassLoader] = null) extends RepoSpec_1_1_1 {
  override def makeRepo(): SheetRepo = {
    GoogSheetRepo.makeGoogSheetRepo(sheetKey, namespaceSheetNum, dirSheetNum, fileModelCLs, this)
  }
  override def toString: String = "goog:/" + sheetKey + "/" + namespaceSheetNum + "/" + dirSheetNum
}

class OfflineXlsSheetRepoSpec_1_1_1(sheetLocation: String, namespaceSheet: String, dirSheet: String,
  fileModelCLs: java.util.List[ClassLoader] = null) extends RepoSpec_1_1_1 {
  override def makeRepo(): SheetRepo = {
    XLSXSheetRepoLoader.loadXLSXSheetRepo(sheetLocation, namespaceSheet, dirSheet, fileModelCLs, this)
  }
  override def toString: String = "xlsx:/" + sheetLocation + "/" + namespaceSheet + "/" + dirSheet
}

class CSVFileRepoSpec_1_1_1(sheetLocation: String, namespaceSheet: String, dirSheet: String,
  fileModelCLs: java.util.List[ClassLoader] = null) extends RepoSpec_1_1_1 {
  override def makeRepo(): SheetRepo = {
    XLSXSheetRepoLoader.loadXLSXSheetRepo(sheetLocation, namespaceSheet, dirSheet, fileModelCLs, this)
  }
  override def toString: String = "xlsx:/" + sheetLocation + "/" + namespaceSheet + "/" + dirSheet
}

class DatabaseRepoSpec_1_1_1(configPath: String, optConfResCL: ClassLoader, dirGraphID: Ident) extends RepoSpec_1_1_1 {
  def this(cPath: String, optCL: ClassLoader, dirGraphUriPrefix: String, dirGraphLocalName: String) = this(cPath, optCL, new FreeIdent(dirGraphUriPrefix + dirGraphLocalName, dirGraphLocalName))
  override def makeRepo(): Repo.WithDirectory = {
    SpecialRepoLoader.loadDatabaseRepo(configPath, optConfResCL, dirGraphID)
  }
}

/**
 * Takes a directory model and uses Goog, Xlsx, Pipeline,CSV,.ttl,rdf sources and loads them
 */
class FromURLishRepoSpec_1_1_1(var myDebugName: String, var dirModelURI: String)
  extends RepoSpec_1_1_1 {

  override def makeRepo(): SheetRepo = {
    val colon = dirModelURI.indexOf(":/");
    val proto = dirModelURI.substring(0, colon);
    val path = dirModelURI.substring(colon + 1);
    val v3: Array[String] = path.split('/')
    val fileModelCLs: java.util.List[ClassLoader] = null;

    if (proto.equals("xlsx")) {
      null
      //(new OfflineXlsSheetRepoSpec(v3[0],v3[1],v3[2])).makeRepo;
      //SpecialRepoLoader.loadXLSXSheetRepo(v3[0], v3[1], v3[2], fileModelCLs)
    } else if (proto.equals("goog")) {
      //SpecialRepoLoader.loadXLSXSheetRepo(v3[0],v3[1].toInt,Integer.parseInt(v3[2]), fileCls)     
      //(new OnlineXlsSheetRepoSpec(v3[0],v3[1].toInt,v3[2].toInt)).makeRepo;
      null
    } else {
      null
    }
  }
}