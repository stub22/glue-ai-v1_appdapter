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
 *   (easier to Save a Google Doc to a Single CsvFiles Spreadsheet than several .csv files!)
 *   Uses Apache POI (@see http://poi.apache.org/)
 */

class CsvFilesSheetRepo(directoryModel: Model, fmcls: java.util.List[ClassLoader]) extends GoogSheetRepo(directoryModel) {

  val fileModelCLs: java.util.List[ClassLoader] = fmcls;

  override def loadSheetModelsIntoMainDataset() = {
     loadSheetModelsIntoMainDatasetByPath(fileModelCLs);
    super.loadSheetModelsIntoMainDataset();
  }

}

object CsvFilesSheetRepo extends BasicDebugger {

  def getCsvSheetAt(sheetLocation: String, sheetName: String, fileModelCLs: java.util.List[ClassLoader]): Reader = {
	  var ext: java.lang.String = FileStreamUtils.getFileExt(sheetName);
	  if (ext.equals("xlsx") || ext.equals("xls")) {
	    XLSXSheetRepo.getSheetAt(sheetLocation, sheetName, fileModelCLs);
	  }
	  var is = FileStreamUtils.openInputStream(sheetName, fileModelCLs);
	  if (is == null) is = FileStreamUtils.openInputStream(sheetLocation + sheetName, fileModelCLs);
	  if (is == null) return null;
	  return new InputStreamReader(is);         
  }

  ///. Modeled on SheetRepo.loadTestSheetRepo
  def loadCsvFilesSheetRepo(sheetLocation: String, nsSheetName: String, dirSheetName: String,
    fileModelCLs: java.util.List[ClassLoader]): SheetRepo = {
    // Read the namespaces and directory sheets into a single directory model.
    val dirModel: Model = readDirectoryModelFromCsvFiles(sheetLocation, nsSheetName, dirSheetName, fileModelCLs: java.util.List[ClassLoader])
    // Construct a repo around that directory
    val shRepo = new CsvFilesSheetRepo(dirModel, fileModelCLs)
    // Load the rest of the repo's initial *sheet* models, as instructed by the directory.
    shRepo.loadSheetModelsIntoMainDataset()
    // Load the rest of the repo's initial *file/resource* models, as instructed by the directory.
    shRepo.loadFileModelsIntoMainDataset(fileModelCLs)
    shRepo
  }


  def readModelSheet(sheetLocation: String, sheetName: String, nsJavaMap: java.util.Map[String, String], fileModelCLs: java.util.List[ClassLoader] ): Model = {
    val tgtModel: Model = ModelFactory.createDefaultModel();
    tgtModel.setNsPrefixes(nsJavaMap)
    val modelInsertProc = new SemSheet.ModelInsertSheetProc(tgtModel);
    val reader: Reader = getCsvSheetAt(sheetLocation, sheetName, fileModelCLs);
    MatrixData.processSheetR(reader, modelInsertProc.processRow);
    getLogger.debug("tgtModel=" + tgtModel)
    tgtModel;
  }

  def readDirectoryModelFromCsvFiles(sheetLocation: String, nsSheetName: String, dirSheetName: String, fileModelCLs: java.util.List[ClassLoader]): Model = {
    getLogger.debug("readDirectoryModelFromCsvFiles - start")
    val nsSheetReader = getCsvSheetAt(sheetLocation, nsSheetName, fileModelCLs);
    val nsJavaMap: java.util.Map[String, String] = MatrixData.readJavaMapFromSheetR(nsSheetReader);
    getLogger.debug("Got NS map: " + nsJavaMap)
    val dirModel: Model = readModelSheet(sheetLocation, dirSheetName, nsJavaMap, fileModelCLs);
    dirModel;
  }

    val nsSheetPath = "Nspc.csv";
    val dirSheetPath = "Dir.csv";

    private def loadTestCsvFilesSheetRepo(): CsvFilesSheetRepo = {
    val clList = new java.util.ArrayList[ClassLoader];
    val dirModel: Model = readDirectoryModelFromCsvFiles(SemSheet.keyForCSVFilesBootSheet22, nsSheetPath, dirSheetPath, clList)
    val sr = new CsvFilesSheetRepo(dirModel, clList)
    sr.loadSheetModelsIntoMainDataset()
    sr.loadFileModelsIntoMainDataset(clList)
    sr
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

    val sr: CsvFilesSheetRepo = loadTestCsvFilesSheetRepo()
    val qib = sr.makeInitialBinding

    qib.bindQName(lightsGraphVarName, lightsGraphQName)

    val solnJavaList: java.util.List[QuerySolution] = sr.queryIndirectForAllSolutions(querySheetQName, queryQName, qib.getQSMap);

    println("Found solutions: " + solnJavaList)
  }

  def testSemSheet(args: Array[String]): Unit = {
    println("SemSheet test ");
    val keyForCsvFilesBootSheet22 = SemSheet.keyForCSVFilesBootSheet22;
    val nsSheetURL = keyForCsvFilesBootSheet22 + nsSheetPath;
    println("Made Namespace Sheet URL: " + nsSheetURL);
    // val namespaceMapProc = new MapSheetProc(1);
    // MatrixData.processSheet (nsSheetURL, namespaceMapProc.processRow);
    // namespaceMapProc.getJavaMap
    val nsJavaMap: java.util.Map[String, String] = MatrixData.readJavaMapFromSheet(nsSheetURL);

    println("Got NS map: " + nsJavaMap)

    val fileModelCLs = new java.util.ArrayList[ClassLoader];

    val dirModel: Model = readModelSheet(keyForCsvFilesBootSheet22, dirSheetPath, nsJavaMap, fileModelCLs);

    val queriesSheetPath = "Qry"
    val queriesModel: Model = readModelSheet(keyForCsvFilesBootSheet22, queriesSheetPath, nsJavaMap, fileModelCLs);

    val tqText = "select ?sheet { ?sheet a ccrt:CsvFilesSheet }";

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

    /**
     *     		Set<Object> results = buildAllRootsInModel(Assembler.general, loadedModel, Mode.DEFAULT);
     *
     */
  }

}