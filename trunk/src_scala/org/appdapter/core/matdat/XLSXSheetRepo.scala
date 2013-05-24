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

import java.io.Reader;
import java.io.StringReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.Iterator;
import java.lang.StringBuffer;
import java.io.FileInputStream;
import org.appdapter.bind.csv.datmat.TestSheetReadMain;
import au.com.bytecode.opencsv.CSVReader;

import org.appdapter.core.log.BasicDebugger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.streaming.{ SXSSFWorkbook };
import org.apache.poi.openxml4j.opc.{ OPCPackage };
import org.apache.poi.xssf.usermodel.{ XSSFWorkbook };
import org.apache.poi.ss.usermodel.{ Workbook, Row, Cell, Sheet, Header };
import org.appdapter.core.store.{ FileStreamUtils };

import com.hp.hpl.jena.rdf.model.{ Model, Statement, Resource, Property, Literal, RDFNode, ModelFactory, InfModel }

import com.hp.hpl.jena.query.{ Query, QueryFactory, QueryExecution, QueryExecutionFactory, QuerySolution, QuerySolutionMap, Syntax };
import com.hp.hpl.jena.query.{ Dataset, DatasetFactory, DataSource };
import com.hp.hpl.jena.query.{ ResultSet, ResultSetFormatter, ResultSetRewindable, ResultSetFactory };

import com.hp.hpl.jena.ontology.{ OntProperty, ObjectProperty, DatatypeProperty }
import com.hp.hpl.jena.datatypes.{ RDFDatatype, TypeMapper }
import com.hp.hpl.jena.datatypes.xsd.{ XSDDatatype }
import com.hp.hpl.jena.shared.{ PrefixMapping }

import com.hp.hpl.jena.rdf.listeners.{ ObjectListener };

import org.appdapter.core.log.BasicDebugger;

import org.appdapter.bind.rdf.jena.model.{ ModelStuff, JenaModelUtils, JenaFileManagerUtils };
// import org.appdapter.bind.rdf.jena.query.{JenaArqQueryFuncs, JenaArqResultSetProcessor};

import org.appdapter.core.store.{ Repo, BasicQueryProcessorImpl, BasicRepoImpl, QueryProcessor };

import org.appdapter.impl.store.{ DirectRepo, QueryHelper, ResourceResolver };
import org.appdapter.help.repo.InitialBindingImpl;

/**
 * @author Stu B. <www.texpedient.com>
 * @author LogicMoo B. <www.logicmoo.com>
 *
 * We implement a Excel Spreedsheet reader  backed Appdapter "repo" (read-only, but reloadable from updated source data).
 *   (easier to Save a Google Doc to a Single XLSX Spreadsheet than several .csv files!)
 *   Uses Apache POI (@see http://poi.apache.org/)
 */
/*
abstract class XLSXSheetRepo_Unused(directoryModel: Model, fmcls: java.util.List[ClassLoader]) extends CsvFilesSheetRepo_Unused(directoryModel, fmcls) {

  override def loadSheetModelsIntoMainDataset() = {
    super.loadSheetModelsIntoMainDataset();
    loadXlsWorkBookSheetModelsIntoMainDataset();
  }

  def loadXlsWorkBookSheetModelsIntoMainDataset() = {
    val mainDset: DataSource = getMainQueryDataset().asInstanceOf[DataSource];
    val dirModel = getDirectoryModel;
    XLSXSheetRepoLoader.loadSheetModelsIntoTargetDataset(this, mainDset, dirModel, fileModelCLs);
  }

}*/

/// this is a registerable loader
class XLSXSheetRepoLoader {
  def getContainerType(): String = {
    return "ccrt:XlsxWorkbookRepo";
  }
  def getSheetType(): String = {
    return "ccrt:XlsxSheet";
  }
  def loadModelsIntoTargetDataset(repo: SheetRepo, mainDset: DataSource, dirModel: Model, fileModelCLs: java.util.List[ClassLoader]) {
    XLSXSheetRepoLoader.loadSheetModelsIntoTargetDataset(repo, mainDset, dirModel, fileModelCLs)
  }
}

object XLSXSheetRepoLoader extends BasicDebugger {

  def loadSheetModelsIntoTargetDataset(repo: SheetRepo, mainDset: DataSource, myDirectoryModel: Model, fileModelCLs: java.util.List[ClassLoader]) = {

    val nsJavaMap: java.util.Map[String, String] = myDirectoryModel.getNsPrefixMap()

    val msqText = """
			select ?container ?key ?sheet ?name 
				{
					?container  a ccrt:XlsxWorkbookRepo; ccrt:key ?key.
					?sheet a ccrt:XlsxSheet; ccrt:sourcePath ?name; ccrt:repo ?container.
				}
		"""

    val msRset = QueryHelper.execModelQueryWithPrefixHelp(myDirectoryModel, msqText);
    import scala.collection.JavaConversions._;
    while (msRset.hasNext()) {
      val qSoln: QuerySolution = msRset.next();

      val containerRes: Resource = qSoln.getResource("container");
      val sheetRes: Resource = qSoln.getResource("sheet");
      val sheetName_Lit: Literal = qSoln.getLiteral("name")
      val sheetLocation_Lit: Literal = qSoln.getLiteral("key")
      getLogger.debug("containerRes=" + containerRes + ", sheetRes=" + sheetRes + ", name=" + sheetName_Lit + ", key=" + sheetLocation_Lit)

      val sheetName = sheetName_Lit.getString();
      val sheetLocation = sheetLocation_Lit.getString();
      var sheetModel: Model = XLSXSheetRepoLoader.readModelSheet(sheetLocation, sheetName, nsJavaMap, fileModelCLs);
      getLogger.debug("Read sheetModel: {}", sheetModel)
      val graphURI = sheetRes.getURI();
      mainDset.replaceNamedModel(graphURI, sheetModel)
    }
  }

  def getSheetAt(sheetLocation: String, sheetName: String, fileModelCLs: java.util.List[ClassLoader]): Reader = {
    var reader = FileStreamUtils.getSheetReaderAt(sheetLocation, sheetName, fileModelCLs);
    if (reader == null) {
      reader = CsvFilesSheetRepoLoader.getCsvSheetAt(sheetLocation, sheetName, fileModelCLs);
    }
    reader;
  }

  ///. Modeled on SheetRepo.loadTestSheetRepo
  def loadXLSXSheetRepo(sheetLocation: String, namespaceSheetName: String, dirSheetName: String,
    fileModelCLs: java.util.List[ClassLoader], repoSpec: RepoSpec): SheetRepo = {
    // Read the namespaces and directory sheets into a single directory model.
    val dirModel: Model = readDirectoryModelFromXLSX(sheetLocation, namespaceSheetName, dirSheetName, fileModelCLs: java.util.List[ClassLoader])
    RepoLoader.makeSheetRepo(repoSpec, dirModel);
  }

  def readModelSheet(sheetLocation: String, sheetName: String, nsJavaMap: java.util.Map[String, String], fileModelCLs: java.util.List[ClassLoader]): Model = {
    val tgtModel: Model = ModelFactory.createDefaultModel();
    tgtModel.setNsPrefixes(nsJavaMap)
    val modelInsertProc = new SemSheet.ModelInsertSheetProc(tgtModel);
    val reader: Reader = getSheetAt(sheetLocation, sheetName, fileModelCLs);
    MatrixData.processSheetR(reader, modelInsertProc.processRow);
    getLogger.debug("tgtModel=" + tgtModel)
    tgtModel;
  }

  def readDirectoryModelFromXLSX(sheetLocation: String, namespaceSheetName: String, dirSheetName: String, fileModelCLs: java.util.List[ClassLoader]): Model = {
    getLogger.debug("readDirectoryModelFromXLSX - start")
    val namespaceSheetReader = getSheetAt(sheetLocation, namespaceSheetName, fileModelCLs);
    val nsJavaMap: java.util.Map[String, String] = MatrixData.readJavaMapFromSheetR(namespaceSheetReader);
    getLogger.debug("Got NS map: " + nsJavaMap)
    val dirModel: Model = XLSXSheetRepoLoader.readModelSheet(sheetLocation, dirSheetName, nsJavaMap, fileModelCLs);
    dirModel;
  }

  private def loadTestXLSXSheetRepo(): SheetRepo = {
    val clList = new java.util.ArrayList[ClassLoader];
    loadXLSXSheetRepo(SemSheet.keyForXLSXBootSheet22, nsSheetName22, dirSheetName22, clList, null)
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

  val nsSheetName22 = "Nspc.csv";
  val dirSheetName22 = "Dir.csv";
  val queriesSheetName22 = "Qry.csv";

  def testSemSheet(args: Array[String]): Unit = {
    println("SemSheet test ");
    val keyForXLSXBootSheet22 = SemSheet.keyForXLSXBootSheet22;
    val namespaceSheetURL = keyForXLSXBootSheet22 + nsSheetName22;
    println("Made Namespace Sheet URL: " + namespaceSheetURL);
    // val namespaceMapProc = new MapSheetProc(1);
    // MatrixData.processSheet (namespaceSheetURL, namespaceMapProc.processRow);
    // namespaceMapProc.getJavaMap
    val nsJavaMap: java.util.Map[String, String] = MatrixData.readJavaMapFromSheet(namespaceSheetURL);

    println("Got NS map: " + nsJavaMap)

    val fileModelCLs = new java.util.ArrayList[ClassLoader];

    val dirModel: Model = readModelSheet(keyForXLSXBootSheet22, dirSheetName22, nsJavaMap, fileModelCLs);

    val queriesModel: Model = readModelSheet(keyForXLSXBootSheet22, queriesSheetName22, nsJavaMap, fileModelCLs);

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
      for (val n: String <- allVarNames) {
        val qvNode: RDFNode = qSoln.get(n);
        println("qvar[" + n + "]=" + qvNode);
      }

      val qtxtLit: Literal = qSoln.getLiteral("qtxt")
      val qtxtString = qtxtLit.getString();
      val zzRset = QueryHelper.execModelQueryWithPrefixHelp(dirModel, qtxtString);
      val zzRSxml = QueryHelper.buildQueryResultXML(zzRset);
      println("Query using qTxt got: " + zzRSxml);

      //		logInfo("Got qsoln" + qSoln + " with s=[" + qSoln.get("s") + "], p=[" + qSoln.get("p") + "], o=[" 
      //						+ qSoln.get("o") +"]");
    }

    /**
     *     		Set<Object> results = buildAllRootsInModel(Assembler.general, loadedModel, Mode.DEFAULT);
     *
     */
  }

}