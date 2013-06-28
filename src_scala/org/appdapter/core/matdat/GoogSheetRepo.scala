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

import com.hp.hpl.jena.query.{ ResultSet, ResultSetRewindable, ResultSetFactory, Dataset, DataSource, QuerySolution, QuerySolutionMap }
import com.hp.hpl.jena.rdf.model.{ Model, Resource, Literal, RDFNode, ModelFactory }
import com.hp.hpl.jena.shared.PrefixMapping
import org.appdapter.core.boot.ClassLoaderUtils
import org.appdapter.core.log.BasicDebugger
//import org.appdapter.core.store.RepoSpec
import org.appdapter.impl.store.QueryHelper
import annotation._
import org.appdapter.impl.store.DirectRepo
//import org.appdapter.api.trigger.RepoOper
import org.appdapter.core.store.Repo
/**
 * @author Stu B. <www.texpedient.com>
 * @author Douglas R. Miles <www.logicmoo.org>
 *
 * This is a DirModel Loader it contains static method for loading FileRepos
 */
@deprecated("Instead of making a Repo Object we will make a GoogSheetRepoSpec - since appdapter 1.1.1")
class GoogSheetRepo(directoryModel: Model, fmcls: java.util.List[ClassLoader] = null) extends SheetRepo(directoryModel, fmcls) {

  def this() =
    this(null, null)

  def this(directoryModel: Model) =
    this(directoryModel, null)

  /**   All the work gets done at SheetRepo
  def loadGoogSheetModelsIntoMainDataset() = {
    val mainDset: DataSource = getMainQueryDataset().asInstanceOf[DataSource];
    val dirModel = getDirectoryModel;
    val fileResModelCLs: java.util.List[ClassLoader] =
      ClassLoaderUtils.getFileResourceClassLoaders(ClassLoaderUtils.ALL_RESOURCE_CLASSLOADER_TYPES);
    GoogSheetRepo.loadSheetModelsIntoTargetDataset(this, mainDset, dirModel, fileResModelCLs);
  } */
}

/// this is a registerable loader
class GoogSheetRepoLoader extends InstallableRepoReader {
  override def getContainerType() = "ccrt:GoogSheetRepo" 
  override def getSheetType() = "ccrt:GoogSheet"
  override def loadModelsIntoTargetDataset(repo: Repo.WithDirectory, mainDset: DataSource, dirModel: Model, fileModelCLs: java.util.List[ClassLoader]) {
    GoogSheetRepo.loadSheetModelsIntoTargetDataset(repo, mainDset, dirModel, fileModelCLs)
  }
}

object GoogSheetRepo extends BasicDebugger {

  @deprecated
  def readDirectoryModelFromGoog(sheetLocation: String, namespaceSheet: Int, dirSheet: Int): Model = {
    // Read the single directory sheets into a single directory model.
    readModelFromGoog(sheetLocation, namespaceSheet, dirSheet)
  }

  def loadSheetModelsIntoTargetDataset(repo: Repo.WithDirectory, mainDset: DataSource, dirModel: Model, fileModelCLs: java.util.List[ClassLoader]) = {

    val nsJavaMap: java.util.Map[String, String] = dirModel.getNsPrefixMap()
    // getLogger().debug("Dir Model NS Prefix Map {} ", nsJavaMap)
    // getLogger().debug("Dir Model {}", dirModel)
    val msqText = """
			select ?container ?key ?sheet ?num 
				{
					?container  a ccrt:GoogSheetRepo; ccrt:key ?key.
					?sheet a ccrt:GoogSheet; ccrt:sheetNumber ?num; ccrt:repo ?container.
				}
		"""

    val msRset = QueryHelper.execModelQueryWithPrefixHelp(dirModel, msqText);
    // getLogger.debug("Got  result set naming our input GoogSheets, from DirModel")
    import scala.collection.JavaConversions._;
    while (msRset.hasNext()) {

      val qSoln: QuerySolution = msRset.next();
      // getLogger().debug("Got apparent solution {}", qSoln);
      val containerRes: Resource = qSoln.getResource("container");
      val sheetRes: Resource = qSoln.getResource("sheet");
      val sheetNum_Lit: Literal = qSoln.getLiteral("num")
      val sheetKey_Lit: Literal = qSoln.getLiteral("key")
      getLogger.debug("containerRes=" + containerRes + ", sheetRes=" + sheetRes + ", num=" + sheetNum_Lit + ", key=" + sheetKey_Lit)

      val sheetNum = sheetNum_Lit.getInt();
      val sheetKey = sheetKey_Lit.getString();
      val sheetModel: Model = readModelSheet(sheetKey, sheetNum, nsJavaMap);
      getLogger.debug("Read sheetModel: {}", sheetModel)
      val graphURI = sheetRes.getURI();
      mainDset.replaceNamedModel(graphURI, sheetModel)
    }
  }

  def readModelSheet(sheetKey: String, sheetNum: Int, nsJavaMap: java.util.Map[String, String] = null): Model = {
    val tgtModel: Model = ModelFactory.createDefaultModel();

    tgtModel.setNsPrefixes(nsJavaMap)

    val modelInsertProc = new SemSheet.ModelInsertSheetProc(tgtModel);
    val sheetURL = WebSheet.makeGdocSheetQueryURL(sheetKey, sheetNum, None);

    MatrixData.processSheet(sheetURL, modelInsertProc.processRow);
    getLogger.debug("tgtModel=" + tgtModel)
    tgtModel;
  }

  def readModelFromGoog(sheetKey: String, namespaceSheetNum: Int, dirSheetNum: Int): Model = {
    getLogger.debug("readDirectoryModelFromGoog - start")
    val namespaceSheetURL = WebSheet.makeGdocSheetQueryURL(sheetKey, namespaceSheetNum, None);
    getLogger.debug("Made Namespace Sheet URL: " + namespaceSheetURL);
    val nsJavaMap: java.util.Map[String, String] = MatrixData.readJavaMapFromSheet(namespaceSheetURL);
    getLogger.debug("Got NS map: " + nsJavaMap)
    val dirModel: Model = readModelSheet(sheetKey, dirSheetNum, nsJavaMap);
    dirModel;
  }

  def makeGoogSheetRepo(sheetLocation: String, namespaceSheetName: Int, dirSheetName: Int,
    fileModelCLs: java.util.List[ClassLoader], repoSpec: RepoSpec): SheetRepo = {
    // Read the namespaces and directory sheets into a single directory model.
    val dirModel: Model = readModelFromGoog(sheetLocation, namespaceSheetName, dirSheetName)
    SpecialRepoLoader.makeSheetRepo(repoSpec, dirModel);
  }

  private def loadTestGoogSheetRepo(): SheetRepo = {

    val spec = new GoogSheetRepoSpec(SemSheet.keyForGoogBootSheet22, nsSheetNum22, dirSheetNum22)
    val sr = spec.makeRepo
    sr.loadSheetModelsIntoMainDataset()
    sr.loadDerivedModelsIntoMainDataset(null)
    sr
  }

  import scala.collection.immutable.StringOps

  def main(args: Array[String]): Unit = {
    //BasicConfigurator.configure();
    //Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);

    // Find a query with this info
    val querySheetQName = "ccrt:qry_sheet_22";
    val queryQName = "ccrt:find_lights_99"

    // Plug a parameter in with this info
    val lightsGraphVarName = "qGraph"
    val lightsGraphQName = "ccrt:lights_camera_sheet_22"

    // Run the resulting fully bound query, and print the results.

    val sr: SheetRepo = loadTestGoogSheetRepo()
    val qib = sr.makeInitialBinding

    qib.bindQName(lightsGraphVarName, lightsGraphQName)

    val solnJavaList: java.util.List[QuerySolution] = sr.queryIndirectForAllSolutions(querySheetQName, queryQName, qib.getQSMap);

    println("Found solutions: " + solnJavaList)
  }

  val nsSheetNum22 = 9;
  val dirSheetNum22 = 8;

  def testSemSheet(args: Array[String]): Unit = {
    println("SemSheet test ");
    val keyForGoogBootSheet22 = SemSheet.keyForGoogBootSheet22;
    val namespaceSheetNum = nsSheetNum22;
    val namespaceSheetURL = WebSheet.makeGdocSheetQueryURL(keyForGoogBootSheet22, namespaceSheetNum, None);
    println("Made Namespace Sheet URL: " + namespaceSheetURL);
    // val namespaceMapProc = new MapSheetProc(1);
    // MatrixData.processSheet (namespaceSheetURL, namespaceMapProc.processRow);
    // namespaceMapProc.getJavaMap
    val nsJavaMap: java.util.Map[String, String] = MatrixData.readJavaMapFromSheet(namespaceSheetURL);

    println("Got NS map: " + nsJavaMap)

    val dirSheetNum = 8;
    val dirModel: Model = readModelSheet(keyForGoogBootSheet22, dirSheetNum, nsJavaMap);

    val queriesSheetNum = 12;
    val queriesModel: Model = readModelSheet(keyForGoogBootSheet22, queriesSheetNum, nsJavaMap);

    val tqText = "select ?sheet { ?sheet a ccrt:GoogSheet }";

    val trset = QueryHelper.execModelQueryWithPrefixHelp(dirModel, tqText);
    val trxml = QueryHelper.buildQueryResultXML(trset);

    println("Got repo-query-test result-XML: \n" + trxml);

    val qqText = "select ?qres ?qtxt { ?qres a ccrt:SparqlQuery; ccrt:queryText ?qtxt}";

    val qqrset: ResultSet = QueryHelper.execModelQueryWithPrefixHelp(queriesModel, qqText);
    val qqrsrw = ResultSetFactory.makeRewindable(qqrset);
    // Does not disturb the original result set
    val qqrxml = QueryHelper.buildQueryResultXML(qqrsrw);

    import scala.collection.JavaConversions._;

    println("Got query-query-test result-XML: \n" + qqrxml);
    qqrsrw.reset();
    val allVarNames: java.util.List[String] = qqrsrw.getResultVars();
    println("Got all-vars java-list: " + allVarNames);
    while (qqrsrw.hasNext()) {
      val qSoln: QuerySolution = qqrsrw.next();
      for (n: String <- allVarNames.toArray(new Array[String](0))) {
        val qvNode: RDFNode = qSoln.get(n);
        println("qvar[" + n + "]=" + qvNode);
      }

      val qtxtLit: Literal = qSoln.getLiteral("qtxt")
      val qtxtString = qtxtLit.getString();
      val zzRset = QueryHelper.execModelQueryWithPrefixHelp(dirModel, qtxtString);
      val zzRSxml = QueryHelper.buildQueryResultXML(zzRset);
      println("Query using qTxt got: " + zzRSxml)

      //		logInfo("Got qsoln" + qSoln + " with s=[" + qSoln.get("s") + "], p=[" + qSoln.get("p") + "], o=[" 
      //						+ qSoln.get("o") +"]");
    }

  }
}
