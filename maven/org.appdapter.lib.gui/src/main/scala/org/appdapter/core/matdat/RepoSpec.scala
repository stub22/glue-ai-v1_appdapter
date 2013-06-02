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

import org.appdapter.core.name.{Ident, FreeIdent}
import org.appdapter.core.store.{RepoSpec, Repo}
import org.appdapter.help.repo.{RepoLoader, RepoClientImpl}

/**
 * @author Stu B. <www.texpedient.com>
 */

object DefaultRepoSpecDefaultNames {
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

  final val DFLT_QRY_SRC_GRAPH_QN = "ccrt:qry_sheet_22"

  // 2) default variable name for a single target graph in a SPARQL query.
  // This is used in the convenience forms of queryIndirect that handle many common
  // use cases, wherein the query needs a single graph to operate on that is switched
  // by application logic or user selection.

  final val DFLT_TGT_GRAPH_SPARQL_VAR = "qGraph"
}
/*
abstract class RepoSpec  {

    def makeRepo(): Repo.WithDirectory;
    def makeRepoClient(repo: Repo.WithDirectory): RepoClientImpl;
}
*/
abstract class RepoSpecScala extends RepoSpec {

  def makeRepo(): Repo.WithDirectory;

  def makeRepoClient(repo: Repo.WithDirectory): RepoClientImpl = {
    new RepoClientImpl(repo, getDfltTgtGraphSparqlVarName, getDfltQrySrcGraphQName);
  }

  def getDfltQrySrcGraphQName = DefaultRepoSpecDefaultNames.DFLT_QRY_SRC_GRAPH_QN;
  def getDfltTgtGraphSparqlVarName: String = DefaultRepoSpecDefaultNames.DFLT_TGT_GRAPH_SPARQL_VAR;
}

class OnlineSheetRepoSpec(sheetKey: String, namespaceSheetNum: Int, dirSheetNum: Int,
  fileModelCLs: java.util.List[ClassLoader] = null) extends RepoSpecScala {
  override def makeRepo(): SheetRepo = {
    GoogSheetRepoLoader.makeGoogSheetRepo(sheetKey, namespaceSheetNum, dirSheetNum, fileModelCLs, this)
  }
  override def toString: String = "goog:/" + sheetKey + "/" + namespaceSheetNum + "/" + dirSheetNum
}

class GoogSheetRepoSpec(sheetKey: String, namespaceSheetNum: Int, dirSheetNum: Int,
  fileModelCLs: java.util.List[ClassLoader] = null) extends RepoSpecScala {
  override def makeRepo(): SheetRepo = {
    GoogSheetRepoLoader.makeGoogSheetRepo(sheetKey, namespaceSheetNum, dirSheetNum, fileModelCLs, this)
  }
  override def toString: String = "goog:/" + sheetKey + "/" + namespaceSheetNum + "/" + dirSheetNum
}

class OfflineXlsSheetRepoSpec(sheetLocation: String, namespaceSheet: String, dirSheet: String,
  fileModelCLs: java.util.List[ClassLoader] = null) extends RepoSpecScala {
  override def makeRepo(): SheetRepo = {
    XLSXSheetRepoLoader.loadXLSXSheetRepo(sheetLocation, namespaceSheet, dirSheet, fileModelCLs, this)
  }
  override def toString: String = "xlsx:/" + sheetLocation + "/" + namespaceSheet + "/" + dirSheet
}

class CSVFileRepoSpec(sheetLocation: String, namespaceSheet: String, dirSheet: String,
  fileModelCLs: java.util.List[ClassLoader] = null) extends RepoSpecScala {
  override def makeRepo(): SheetRepo = {
    XLSXSheetRepoLoader.loadXLSXSheetRepo(sheetLocation, namespaceSheet, dirSheet, fileModelCLs, this)
  }
  override def toString: String = "xlsx:/" + sheetLocation + "/" + namespaceSheet + "/" + dirSheet
}

class DatabaseRepoSpec(configPath: String, optConfResCL: ClassLoader, dirGraphID: Ident) extends RepoSpecScala {
  def this(cPath: String, optCL: ClassLoader, dirGraphUriPrefix: String, dirGraphLocalName: String) = this(cPath, optCL, new FreeIdent(dirGraphUriPrefix + dirGraphLocalName, dirGraphLocalName))
  override def makeRepo(): Repo.WithDirectory = {
    RepoLoader.loadDatabaseRepo(configPath, optConfResCL, dirGraphID)
  }
}

/**
 * Takes a directory model and uses Goog, Xlsx, Pipeline,CSV,.ttl,rdf sources and loads them
 */
class FromURLishRepoSpec(var myDebugName: String, var dirModelURI: String)
  extends RepoSpecScala {

  override def makeRepo(): SheetRepo = {
    val colon = dirModelURI.indexOf(":/");
    val proto = dirModelURI.substring(0, colon);
    val path = dirModelURI.substring(colon + 1);
    val v3: Array[String] = path.split('/')
    val fileModelCLs: java.util.List[ClassLoader] = null;

    if (proto.equals("xlsx")) {
      null
      //(new OfflineXlsSheetRepoSpec(v3[0],v3[1],v3[2])).makeRepo;
      //RepoLoader.loadXLSXSheetRepo(v3[0], v3[1], v3[2], fileModelCLs)
    } else if (proto.equals("goog")) {
      //RepoLoader.loadXLSXSheetRepo(v3[0],v3[1].toInt,Integer.parseInt(v3[2]), fileCls)     
      //(new OnlineXlsSheetRepoSpec(v3[0],v3[1].toInt,v3[2].toInt)).makeRepo;
      null
    } else {
      null
    }
  }
}