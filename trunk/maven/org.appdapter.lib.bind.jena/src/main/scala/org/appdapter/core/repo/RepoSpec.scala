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

package org.appdapter.core.repo

import java.util.HashMap
import org.appdapter.core.boot.ClassLoaderUtils
import org.appdapter.core.matdat.{ GoogSheetRepoSpec, OnlineSheetRepoSpec }
import org.appdapter.core.store.Repo
import org.appdapter.demo.DemoBrowserUI
import org.appdapter.help.repo.RepoClientImpl
import org.appdapter.impl.store.FancyRepo
import org.osgi.framework.BundleContext
import com.hp.hpl.jena.rdf.model.Model
import org.appdapter.core.matdat.OfflineXlsSheetRepoSpec
import org.appdapter.core.store.InstallableSpecReader

/**
 * @author Stu B. <www.texpedient.com>
 */

// todo move me!
/*
class DatabaseRepoSpec_REPEATED(configPath: String, optConfResCL: ClassLoader, dirGraphID: Ident) extends RepoSpec {
  def this(cPath: String, optCL: ClassLoader, dirGraphUriPrefix: String, dirGraphLocalName: String) = this(cPath, optCL, new FreeIdent(dirGraphUriPrefix + dirGraphLocalName, dirGraphLocalName))
  override def makeRepo() = loadDatabaseRepo(configPath, optConfResCL, dirGraphID)
}*/

abstract class RepoSpecForDirectory extends RepoSpecScala {
  override def makeRepo: FancyRepo = {
    FancyRepoLoader.makeRepoWithDirectory(this, getDirectoryModel(), null);
  }
  def getDirectoryModel(): Model;
}
abstract class RepoSpecScala extends RepoSpec {

  def makeRepo(): Repo.WithDirectory;

  def getDirectoryModel(): Model = {
    makeRepo.getDirectoryModel();
  }

  def makeRepoClient(repo: Repo.WithDirectory, getDfltQrySrcGraphQName: String): RepoClientImpl = {
    new RepoClientImpl(repo, getDfltTgtGraphSparqlVarName, getDfltQrySrcGraphQName);
  }

  //depricated("uses hardcoded query sheet like ccrt:qry_sheet_77")
  def makeRepoClient(repo: Repo.WithDirectory): RepoClientImpl = {
    new RepoClientImpl(repo, getDfltTgtGraphSparqlVarName, getDfltQrySrcGraphQName);
  }

  def getDfltQrySrcGraphQName = RepoSpecDefaultNames.DFLT_QRY_SRC_GRAPH_TYPE;
  def getDfltTgtGraphSparqlVarName: String = RepoSpecDefaultNames.DFLT_TGT_GRAPH_SPARQL_VAR;
}

object URLRepoSpec {
  val loaderMap = new HashMap[String, InstallableRepoReader]();
}
/**
 * Takes a directory model and uses Goog, Xlsx, Pipeline,CSV,.ttl,rdf sources and loads them
 */
class URLRepoSpec(var dirModelURL: String, var fileModelCLs: java.util.List[ClassLoader] = null)
  extends RepoSpecScala {

  def trimString(str: String, outers: String*): String = {
    var tmp = str;
    for (s0 <- outers) {
      var s: String = s0
      while (tmp.startsWith(s)) {
        tmp = tmp.substring(s.length);
      }
      while (tmp.endsWith(s)) {
        tmp = tmp.substring(0, tmp.length - s.length);
      }
    }
    tmp
  }
  def detectedRepoSpec: RepoSpec = {
    import scala.collection.JavaConversions._
    var orig = dirModelURL.trim();
    var multis: Array[String] = orig.split(",");
    if (multis.length > 1) {
      if (!orig.startsWith("mult:")) {
        orig = "mult://[" + orig + "]"
      }
    }
    var dirModelURLParse = orig.replace("//", "/").trim();
    val colon = dirModelURLParse.indexOf(":/");
    val proto = trimString(dirModelURLParse.substring(0, colon + 1), "/", ":", " ")
    val path = trimString(dirModelURLParse.substring(colon + 1), "/", " ")
    val v3: Array[String] = (path + "//").split('/')
    if (proto.equals("goog")) {
      (new GoogSheetRepoSpec(v3(0), v3(1).toInt, v3(2).toInt, fileModelCLs))
    } else if (proto.equals("xlsx")) {
      (new OfflineXlsSheetRepoSpec(v3(0), v3(1), v3(3), fileModelCLs))
    } else if (proto.equals("scan")) {
      (new ScanURLDirModelRepoSpec(v3(0), fileModelCLs))
    } else if (proto.equals("mult")) {
      (new MultiRepoSpec(path, fileModelCLs))
    } else {
      val dirModelLoaders: java.util.List[InstallableSpecReader] = FancyRepoLoader.getSpecLoaders
      val dirModelLoaderIter = dirModelLoaders.listIterator
      while (dirModelLoaderIter.hasNext()) {
        val irr = dirModelLoaderIter.next
        try {
          val ext = irr.getExt;
          if (ext != null && ext.equalsIgnoreCase(proto)) {
            val spec = Some(irr.makeRepoSpec(path, v3, fileModelCLs))
            if (!spec.isEmpty) return spec.get;
          }
        } catch {
          case except: Throwable =>
            except.printStackTrace
          //getLogger().error("Caught loading error in {}", Array[Object](irr, except))
        }
      }
      new URLDirModelRepoSpec(dirModelURL, fileModelCLs)
    }
  }
  override def getDirectoryModel(): Model = detectedRepoSpec.getDirectoryModel
  override def makeRepo(): Repo.WithDirectory = detectedRepoSpec.makeRepo
  override def toString = dirModelURL
}

object RepoSpecDefaultNames {

  // These 2 string constants establish repo-client wrapper defaults, giving a default
  // query context to easily fetch from.
  // Either value may be bypassed/overidden using either
  //      A) Overrides of the RepoSpec methods below
  //   or B) The more general forms of queryIndirect_.
  // 1) Default query *source* graph QName used in directory model (Sheet or RDF).
  // We read SPARQL text from this graph, which we use to query *other* graphs.
  // This graph is typically not used as a regular data graph by  other low-order
  // query operations, although there is no prohibition or protection from doing so
  // at this time.   This query source graph may be overridden using the more general
  // forms of queryIndirect_.

  val DFLT_QRY_SRC_GRAPH_TYPE = "ccrt:qry_sheet_22"

  // 2) default variable name for a single target graph in a SPARQL query.
  // This is used in the convenience forms of queryIndirect that handle many common
  // use cases, wherein the query needs a single graph to operate on that is switched
  // by application logic or user selection.

  val DFLT_TGT_GRAPH_SPARQL_VAR = "qGraph"

  //===============================================================================================
  //  Constants below are for test cases outside of appdapter (so we can test inside appdater)
  // they will be moved!
  //===============================================================================================

  // Formal prefix for Robokind 2012 runtime
  val RKRT_NS_PREFIX = "urn:ftd:robokind.org:2012:runtime#"
  // Formal prefix for Cogchar 2012 runtime
  val NS_CCRT_RT = "urn:ftd:cogchar.org:2012:runtime#"
  // Formal prefix for Cogchar 2012 goody
  val GOODY_NS = "urn:ftd:cogchar.org:2012:goody#"
  // Formal prefix for Cogchar Web
  //public static String WEB_NS = "http://www.cogchar.org/lift/config#";
  // Formal prefix for Cogchar 2012 runtime
  val NS_CC = "http://www.cogchar.org/schema/crazyRandom#"

  // Less formal web prefix still widely used:
  val NSP_Root = "http://www.cogchar.org/"

  val PIPELINE_GRAPH_TYPE = NS_CC + "PipelineModel"; /// "csi:pipeline_sheet_77";
  //val PIPE_QUERY_QN = "ccrt:find_pipes_77";
  //val PIPE_SOURCE_QUERY_QN = "ccrt:find_pipe_sources_78";
  //val PIPE_ATTR_QQN = PIPE_QUERY_QN;
  //val PIPE_SOURCE_QQN = PIPE_SOURCE_QUERY_QN;

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
    val repoSpec = new OnlineSheetRepoSpec(RepoSpecDefaultNames.BMC_SHEET_KEY, RepoSpecDefaultNames.BMC_NAMESPACE_SHEET_NUM, RepoSpecDefaultNames.BMC_DIRECTORY_SHEET_NUM, fileResModelCLs);
    val repo = repoSpec.makeRepo //.asInstanceOf[GoogSheetRepo];

    print("Starting Whackamole");
    import org.appdapter.demo.DemoBrowserUI
    val repoNav = DemoBrowserUI.makeDemoNavigatorCtrl(args);
    repoNav.addObject(null, repo, true, false);
    java.lang.Thread.sleep(60000000);
  }

  def makeBMC_RepoSpec(ctx: BundleContext): OnlineSheetRepoSpec = {
    val fileResModelCLs: java.util.List[ClassLoader] =
      ClassLoaderUtils.getFileResourceClassLoaders(ctx, ClassLoaderUtils.ALL_RESOURCE_CLASSLOADER_TYPES);
    makeBMC_RepoSpec(fileResModelCLs);
  }
  def makeBMC_RepoSpec(fileResModelCLs: java.util.List[ClassLoader]): OnlineSheetRepoSpec = {
    new OnlineSheetRepoSpec(BMC_SHEET_KEY, BMC_NAMESPACE_SHEET_NUM, BMC_DIRECTORY_SHEET_NUM, fileResModelCLs);
  }

  def makeBMC_OfflineRepoSpec(ctx: BundleContext): OfflineXlsSheetRepoSpec = {
    val fileResModelCLs: java.util.List[ClassLoader] =
      ClassLoaderUtils.getFileResourceClassLoaders(ctx, ClassLoaderUtils.ALL_RESOURCE_CLASSLOADER_TYPES);
    makeBMC_OfflineRepoSpec(fileResModelCLs);
  }

  def makeBMC_OfflineRepoSpec(fileResModelCLs: java.util.List[ClassLoader]): OfflineXlsSheetRepoSpec = {
    new OfflineXlsSheetRepoSpec(BMC_WORKBOOK_PATH, DFLT_NAMESPACE_SHEET_NAME, DFLT_DIRECTORY_SHEET_NAME, fileResModelCLs);
  }

}

