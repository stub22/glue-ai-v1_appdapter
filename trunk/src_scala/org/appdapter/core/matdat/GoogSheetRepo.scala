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

import com.hp.hpl.jena.rdf.model.{ Model, Statement, Resource, Property, Literal, RDFNode, ModelFactory, InfModel }
import com.hp.hpl.jena.query.{ Query, QueryFactory, QueryExecution, QueryExecutionFactory, QuerySolution, QuerySolutionMap, Syntax }
import com.hp.hpl.jena.query.{ Dataset, DatasetFactory, DataSource }
import com.hp.hpl.jena.query.{ ResultSet, ResultSetFormatter, ResultSetRewindable, ResultSetFactory }
import com.hp.hpl.jena.ontology.{ OntProperty, ObjectProperty, DatatypeProperty }
import com.hp.hpl.jena.datatypes.{ RDFDatatype, TypeMapper }
import com.hp.hpl.jena.datatypes.xsd.{ XSDDatatype }
import com.hp.hpl.jena.shared.{ PrefixMapping }
import com.hp.hpl.jena.rdf.listeners.{ ObjectListener }
import org.appdapter.core.log.BasicDebugger
import org.appdapter.bind.rdf.jena.model.{ ModelStuff, JenaModelUtils, JenaFileManagerUtils }
import org.appdapter.core.store.{ Repo, BasicQueryProcessorImpl, BasicRepoImpl, QueryProcessor }
import org.appdapter.impl.store.{ DirectRepo, QueryHelper, ResourceResolver }
import org.appdapter.help.repo.InitialBindingImpl
import org.appdapter.core.boot.ClassLoaderUtils
import org.appdapter.core.store.{ RepoSpec, RepoOper, RepoClient, RepoSpecJava, Repo, InitialBinding }
import org.appdapter.help.repo. { RepoClientImpl, RepoClientScala, InitialBindingImpl}


/**
 * @author Stu B. <www.texpedient.com>
 *
 * We implement a CSV (spreadsheet) backed Appdapter "repo"
 * (read-only, but reloadable from updated source data).
 * 
 * See 
 */


/*
abstract class GoogSheetRepoSpec(directoryModel: Model, fmcls: java.util.List[ClassLoader] = null) extends RepoSpec(directoryModel, fmcls) {


  def loadGoogSheetModelsIntoMainDataset() = {
    val mainDset: DataSource = getMainQueryDataset().asInstanceOf[DataSource];
    val dirModel = getDirectoryModel;
    val fileResModelCLs: java.util.List[ClassLoader] =
      ClassLoaderUtils.getFileResourceClassLoaders(ClassLoaderUtils.ALL_RESOURCE_CLASSLOADER_TYPES);
    GoogSheetRepoLoader.loadSheetModelsIntoTargetDataset(this, mainDset, dirModel, fileResModelCLs);
  }
}*/

/**
 * @author Stu B. <www.texpedient.com>
 * @author Douglas R. Miles <www.logicmoo.org>
 *
 * This is a DirModel Loader it contains static method for loading FileRepos
 */

/// this is a registerable loader
class GoogSheetRepoLoader {
  def getContainerType(): String = {
    return "ccrt:GoogSheetRepo";
  }
  def getSheetType(): String = {
    return "ccrt:GoogSheet";
  }
  def loadModelsIntoTargetDataset(repo: SheetRepo, mainDset: DataSource, dirModel: Model, fileModelCLs: java.util.List[ClassLoader]) {
    GoogSheetRepoLoader.loadSheetModelsIntoTargetDataset(repo, mainDset, dirModel, fileModelCLs)
  }
}

object GoogSheetRepoLoader extends BasicDebugger {

  def loadSheetModelsIntoTargetDataset(repo: SheetRepo, mainDset: DataSource, dirModel: Model, fileModelCLs: java.util.List[ClassLoader]) = {

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

  def readModelSheet(sheetKey: String, sheetNum: Int, nsJavaMap: java.util.Map[String, String]): Model = {
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
    RepoLoader.makeSheetRepo(repoSpec, dirModel);
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
    org.apache.log4j.BasicConfigurator.configure();
    org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);

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
      for (val n: String <- allVarNames) {
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