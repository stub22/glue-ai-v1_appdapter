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

import com.hp.hpl.jena.query.{ ResultSetFactory, ResultSet, QuerySolution, Dataset }
import com.hp.hpl.jena.rdf.model.{ Resource, RDFNode, ModelFactory, Model, Literal }
import java.io.Reader
import org.appdapter.core.store.{ ExtendedFileStreamUtils }
import org.appdapter.impl.store.QueryHelper
import scala.collection.JavaConversions.asScalaBuffer
import org.appdapter.impl.store.DirectRepo
import org.appdapter.core.store.Repo
import org.appdapter.core.log.BasicDebugger

/**
 * @author Stu B. <www.texpedient.com>
 * @author LogicMoo B. <www.logicmoo.com>
 *
 * We implement a Excel Spreedsheet reader  backed Appdapter "repo" (read-only, but reloadable from updated source data).
 *   (easier to Save a Google Doc to a Single XLSX Spreadsheet than several .Csv files!)
 *   Uses Apache POI (@see http://poi.apache.org/)
 */

/// this is a registerable loader
class XLSXSheetRepoLoader extends InstallableRepoReader {
  override def getContainerType() = "ccrt:XlsxWorkbookRepo"
  override def getSheetType() = "ccrt:XlsxSheet"
  override def loadModelsIntoTargetDataset(repo: Repo.WithDirectory, mainDset: Dataset, dirModel: Model, fileModelCLs: java.util.List[ClassLoader]) {
    XLSXSheetRepoLoader.loadSheetModelsIntoTargetDataset(repo, mainDset, dirModel, fileModelCLs)
  }
}

object XLSXSheetRepoLoader extends BasicDebugger {

  def loadSheetModelsIntoTargetDataset(repo: Repo.WithDirectory, mainDset: Dataset, myDirectoryModel: Model, fileModelCLs: java.util.List[ClassLoader]) = {

    val nsJavaMap: java.util.Map[String, String] = myDirectoryModel.getNsPrefixMap()

    val msqText = """
			select ?container ?key ?sheet ?name ?unionOrReplace
				{
					?container  a ccrt:XlsxWorkbookRepo; ccrt:key ?key.
					?sheet a ccrt:XlsxSheet; ccrt:sourcePath ?name; ccrt:repo ?container.
      				OPTIONAL { ?sheet  a ?unionOrReplace. FILTER (?unionOrReplace = ccrt:UnionModel) }
				}
		"""

    val msRset = QueryHelper.execModelQueryWithPrefixHelp(myDirectoryModel, msqText);
    import scala.collection.JavaConversions._;
    while (msRset.hasNext()) {
      val qSoln: QuerySolution = msRset.next();

      val containerRes: Resource = qSoln.getResource("container");
      val sheetRes: Resource = qSoln.getResource("sheet");
      val unionOrReplaceRes: Resource = qSoln.getResource("unionOrReplace");
      val sheetName_Lit: Literal = qSoln.getLiteral("name")
      val sheetLocation_Lit: Literal = qSoln.getLiteral("key")
      getLogger.debug("containerRes=" + containerRes + ", sheetRes=" + sheetRes + ", name="
        + sheetName_Lit + ", key=\"" + sheetLocation_Lit + "\", union= " + unionOrReplaceRes)

      val sheetName = sheetName_Lit.getString();
      repo.addLoadTask(sheetName, new Runnable() {
        def run() {
          val sheetLocation = sheetLocation_Lit.getString();
          val sheetModel: Model = readModelSheetX(sheetLocation, sheetName, nsJavaMap, fileModelCLs);
          getLogger.debug("Read sheetModel: {}", sheetModel)
          val graphURI = sheetRes.getURI();
          FancyRepoLoader.replaceOrUnion(mainDset, unionOrReplaceRes, graphURI, sheetModel);
        }
      })
    }

  }

  //in 2.10.1 we should:  import scala.language.implicitConversions
  class CoalesceStr[A <: String](a: A) { def ??(b: A) = if (a == null || a.length == 0) b else a }
  implicit def coalesce_string[A <: String](a: A) = new CoalesceStr(a)

  def loadXLSXSheetRepo(sheetLocation: String, namespaceSheetName: String, dirSheetName: String,
    fileModelCLs: java.util.List[ClassLoader], repoSpec: RepoSpec): SheetRepo = {
    // Read the namespaces and directory sheets into a single directory model.
    val dirModel: Model = readDirectoryModelFromXLSX(sheetLocation, namespaceSheetName ?? nsSheetName22, dirSheetName ?? dirSheetName22, fileModelCLs: java.util.List[ClassLoader])
    FancyRepoLoader.makeRepoWithDirectory(repoSpec, dirModel);
  }

  def readModelSheetX(sheetLocation: String, sheetName: String, nsJavaMap: java.util.Map[String, String], fileModelCLs: java.util.List[ClassLoader]): Model = {
    val tgtModel: Model = ModelFactory.createDefaultModel();
    tgtModel.setNsPrefixes(nsJavaMap)
    val modelInsertProc = new SemSheet.ModelInsertSheetProc(tgtModel);
	val efsu = new ExtendedFileStreamUtils()
    val reader: Reader = efsu.getWorkbookSheetCsvReaderAt(sheetLocation, sheetName, fileModelCLs);
    MatrixData.processSheetR(reader, modelInsertProc.processRow);
    getLogger.debug("tgtModel=" + tgtModel)
    tgtModel;
  }

  def readDirectoryModelFromXLSX(sheetLocation: String, namespaceSheetName: String, dirSheetName: String, fileModelCLs: java.util.List[ClassLoader] = null): Model = {
    getLogger.debug("readDirectoryModelFromXLSX - start")
	val efsu = new ExtendedFileStreamUtils()
    val namespaceSheetReader = efsu.getWorkbookSheetCsvReaderAt(sheetLocation, namespaceSheetName, fileModelCLs);
    val nsJavaMap: java.util.Map[String, String] = MatrixData.readJavaMapFromSheetR(namespaceSheetReader);
    getLogger.debug("Got NS map: " + nsJavaMap)
    val dirModel: Model = readModelSheetX(sheetLocation, dirSheetName, nsJavaMap, fileModelCLs);
    dirModel;
  }

  /////////////////////////////////////////
  /// These are tests below  
  /////////////////////////////////////////

  val keyForXLSXBootSheet22 = "file:GluePuma_HRKR50_TestFull.xlsx";
  val nsSheetName22 = "Nspc";
  val dirSheetName22 = "Dir";
  val queriesSheetName22 = "Qry";

  private def loadTestXLSXSheetRepo(): SheetRepo = {
    val clList = new java.util.ArrayList[ClassLoader];
    loadXLSXSheetRepo(keyForXLSXBootSheet22, nsSheetName22, dirSheetName22, clList, null)
  }
  import scala.collection.immutable.StringOps

  def main(args: Array[String]): Unit = {

    // Find a query with this info
    val querySheetQName = "ccrt:qry_sheet_22";
    val queryQName = "ccrt:find_lights_99"

    // Plug a parameter in with this info
    val lightsGraphVarName = "qGraph"
    val lightsGraphQName = "ccrt:lights_camera_sheet_22"

    // Run the resulting fully bound query, and print the results.

    val sr: SheetRepo = loadTestXLSXSheetRepo()
    val qib = sr.makeInitialBinding

    qib.bindQName(lightsGraphVarName, lightsGraphQName)

    val solnJavaList: java.util.List[QuerySolution] = sr.queryIndirectForAllSolutions(querySheetQName, queryQName, qib.getQSMap);

    println("Found solutions: " + solnJavaList)
  }

  def testSemSheet(args: Array[String]): Unit = {
    println("SemSheet test ");
    val namespaceSheetURL = keyForXLSXBootSheet22 + "!" + nsSheetName22;
    println("Made Namespace Sheet URL: " + namespaceSheetURL);
    // val namespaceMapProc = new MapSheetProc(1);
    // MatrixData.processSheet (namespaceSheetURL, namespaceMapProc.processRow);
    // namespaceMapProc.getJavaMap
    val nsJavaMap: java.util.Map[String, String] = MatrixData.readJavaMapFromSheet(namespaceSheetURL);

    println("Got NS map: " + nsJavaMap)

    val fileModelCLs = new java.util.ArrayList[ClassLoader];

    val dirModel: Model = readModelSheetX(keyForXLSXBootSheet22, dirSheetName22, nsJavaMap, fileModelCLs);

    val queriesModel: Model = readModelSheetX(keyForXLSXBootSheet22, queriesSheetName22, nsJavaMap, fileModelCLs);

    val tqText = "select ?sheet { ?sheet a ccrt:XLSXSheet }";

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

    /**
     *     		Set<Object> results = buildAllRootsInModel(Assembler.general, loadedModel, Mode.DEFAULT);
     *
     */
  }

}