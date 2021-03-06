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

package org.appdapter.xload.sheet

import org.appdapter.core.log.BasicDebugger

import org.appdapter.core.store.dataset.{ RepoDatasetFactory }
import org.appdapter.xload.fancy.{ FancyRepoLoader, InstallableRepoLoader}
import org.appdapter.core.loader.{  SpecialRepoLoader }
import org.appdapter.xload.repo.{ DirectRepo}
import org.appdapter.xload.matdat.{ SemSheet, WebSheet, MatrixData}
import org.appdapter.fancy.query.QueryHelper
import org.appdapter.fancy.rspec.{RepoSpec}
import org.appdapter.xload.rspec.{ GoogSheetRepoSpec}
import com.hp.hpl.jena.query.{ Dataset, QuerySolution }
import com.hp.hpl.jena.rdf.model.{ Literal, Model, Resource }
import org.appdapter.core.store.RepoOper
import org.appdapter.bind.rdf.jena.query.JenaArqResultSetProcessor
import org.appdapter.bind.rdf.jena.query.SPARQL_Utils

/////////////////////////////////////////
/// this is a registerable loader
/////////////////////////////////////////
class GoogSheetRepoLoader extends InstallableRepoLoader {
  override def makeRepoSpec(path: String, args: Array[String], cLs: java.util.List[ClassLoader]) = new GoogSheetRepoSpec(args(0), args(1).toInt, args(2).toInt, cLs)
  override def getExt = "goog";
  override def getContainerType() = "ccrt:GoogSheetRepo"
  override def getSheetType() = "ccrt:GoogSheet"
  override def loadModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset, dirModel: Model, 
								fileModelCLs: java.util.List[ClassLoader], optPrefixURL : String) {
    GoogSheetRepoLoader.loadSheetModelsIntoTargetDataset(repo, mainDset, dirModel, fileModelCLs)
  }
}

object GoogSheetRepoLoader extends BasicDebugger {

  /////////////////////////////////////////
  /// Make a Repo.WithDirectory from a Spec
  /////////////////////////////////////////
  def makeGoogSheetRepo(sheetLocation: String, namespaceSheetName: Int, dirSheetName: Int,
			fileModelCLs: java.util.List[ClassLoader], repoSpec: RepoSpec): DirectRepo = {
    // Read the namespaces and directory sheets into a single directory model.
    val dirModel: Model = readModelFromGoog(sheetLocation, namespaceSheetName, dirSheetName)
    FancyRepoLoader.makeRepoWithDirectory(repoSpec, dirModel);
  }

  /////////////////////////////////////////
  /// Read dir models
  /////////////////////////////////////////
  def readDirectoryModelFromGoog(sheetLocation: String, namespaceSheet: Int, dirSheet: Int): Model = {
	  getLogger().info("Reading *directory* model from Goog!");
    // Read the single directory sheets into a single directory model.
    readModelFromGoog(sheetLocation, namespaceSheet, dirSheet)
  }
  /////////////////////////////////////////
  /// Read sheet models
  /////////////////////////////////////////
  def loadSheetModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset, dirModel: Model, fileModelCLs: java.util.List[ClassLoader]) = {

    val nsJavaMap: java.util.Map[String, String] = dirModel.getNsPrefixMap()
    // getLogger.debug("Dir Model NS Prefix Map {} ", nsJavaMap)
    // getLogger.debug("Dir Model {}", dirModel)
    val msqText = """
			select ?container ?key ?sheet ?modelName ?num ?unionOrReplace
				{
					?container  a ccrt:GoogSheetRepo; ccrt:key ?key.
					?sheet a ccrt:GoogSheet; ccrt:sheetNumber ?num; ccrt:repo ?container.
         			OPTIONAL { ?sheet a ?unionOrReplace. FILTER (?unionOrReplace = ccrt:UnionModel) }
                    OPTIONAL { ?sheet dphys:hasGraphNameUri ?modelName }
          	        OPTIONAL { ?sheet owl:sameAs ?modelName }
      
				}
		"""

    val msRset = QueryHelper.execModelQueryWithPrefixHelp(dirModel, msqText);
    // getLogger.debug("Got  result set naming our input GoogSheets, from DirModel")
    import scala.collection.JavaConversions._;
    while (msRset.hasNext()) {

      val qSoln: QuerySolution = msRset.next();
      // getLogger.debug("Got apparent solution {}", qSoln);
      //val containerRes: Resource = qSoln.getResource("container");
      val sheetRes: Resource = SPARQL_Utils.nonBnodeValue(qSoln,"sheet","modelName");
      val sheetNum_Lit: Literal = qSoln.getLiteral("num")
      val sheetKey_Lit: Literal = qSoln.getLiteral("key")
      val unionOrReplaceRes: Resource = qSoln.getResource("unionOrReplace");
      getLogger.debug("Loading sheetRes=" + sheetRes + ", num=" + sheetNum_Lit + ", key=" + sheetKey_Lit)

      val sheetNum = sheetNum_Lit.getInt();
      val sheetKey = sheetKey_Lit.getString();
      val graphURI = sheetRes.getURI();
      repo.addLoadTask(graphURI, new Runnable() {
        def run() {
          val sheetModel: Model = readModelSheet(sheetKey, sheetNum, nsJavaMap);
          getLogger.debug("Read sheetModel: {}", sheetModel)
          FancyRepoLoader.replaceOrUnion(mainDset, unionOrReplaceRes, graphURI, sheetModel);
        }
      })
    }
  }

  def readModelSheet(sheetKey: String, sheetNum: Int, nsJavaMap: java.util.Map[String, String] = null): Model = {
    val tgtModel: Model = RepoDatasetFactory.createPrivateMemModel

    tgtModel.setNsPrefixes(nsJavaMap)

    val modelInsertProc = new SemSheet.ModelInsertSheetProc(tgtModel);
    val sheetURL = WebSheet.makeGdocSheetQueryURL(sheetKey, sheetNum, None);
	getLogger.info("About to read data from sheetURL: {}", sheetURL)
    MatrixData.processSheet(sheetURL, modelInsertProc.processRow);
    getLogger.debug("tgtModel={}", tgtModel)
    tgtModel;
  }

  def readModelFromGoog(sheetKey: String, namespaceSheetNum: Int, dirSheetNum: Int): Model = {
    getLogger.debug("readModelFromGoog - start")
    val namespaceSheetURL = WebSheet.makeGdocSheetQueryURL(sheetKey, namespaceSheetNum, None);
    getLogger.info("About to read from namespace Sheet URL: {} ", namespaceSheetURL);
    val nsJavaMap: java.util.Map[String, String] = MatrixData.readJavaMapFromSheet(namespaceSheetURL);
    getLogger.debug("Got NS map {} ", nsJavaMap)
    val dirModel: Model = readModelSheet(sheetKey, dirSheetNum, nsJavaMap);
    dirModel;
  }

}
