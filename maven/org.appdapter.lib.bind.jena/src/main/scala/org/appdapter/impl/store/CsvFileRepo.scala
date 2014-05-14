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

import java.io.{ InputStreamReader, Reader }
import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.store.{ ExtendedFileStreamUtils, Repo }
import org.appdapter.impl.store.QueryHelper
import com.hp.hpl.jena.query.{ Dataset, QuerySolution, ResultSet, ResultSetFactory }
import com.hp.hpl.jena.rdf.model.{ Literal, Model, RDFNode, Resource }
import org.appdapter.core.store.dataset.RepoDatasetFactory
import org.appdapter.core.store.dataset.SpecialRepoLoader

/**
 * @author Stu B. <www.texpedient.com>
 * @author LogicMoo B. <www.logicmoo.com>
 *
 * We implement a Excel Spreedsheet reader  backed Appdapter "repo" (read-only, but reloadable from updated source data).
 *   (easier to Save a Google Doc to a Single CsvFile Spreadsheet than several .Csv files!)
 *   Uses Apache POI (@see http://poi.apache.org/)
 */

class CSVFileRepoSpec(dirSheet: String, namespaceSheet: String = null,
  fileModelCLs: java.util.List[ClassLoader] = null) extends RepoSpecForDirectory {
  override def getDirectoryModel = CsvFileSheetLoader.readDirectoryModelFromCsvFile(dirSheet, fileModelCLs, namespaceSheet)
  override def toString: String = dirSheet
}

/// this is a registerable loader
class CsvFileSheetLoader extends InstallableRepoReader {
  override def makeRepoSpec(path: String, args: Array[String], cLs: java.util.List[ClassLoader]) = new CSVFileRepoSpec(args(0), args(1), cLs)
  override def getExt = "csv"
  override def getContainerType() = "ccrt:CsvFileRepo"
  override def getSheetType() = "ccrt:CsvFileSheet"
  override def loadModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset, dirModel: Model, fileModelCLs: java.util.List[ClassLoader]) {
    CsvFileSheetLoader.loadSheetModelsIntoTargetDataset(repo, mainDset, dirModel, fileModelCLs)
  }
}

object CsvFileSheetLoader extends BasicDebugger {

  def loadSheetModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset,
    myDirectoryModel: Model, clList: java.util.List[ClassLoader]) = {

    val nsJavaMap: java.util.Map[String, String] = myDirectoryModel.getNsPrefixMap()

    val msqText = """
			select ?repo ?repoPath ?model ?modelPath ?unionOrReplace
				{
					?repo  a ccrt:CsvFileRepo; ccrt:sourcePath ?repoPath.
					?model a ccrt:CsvFileSheet; ccrt:sourcePath ?modelPath; ccrt:repo ?repo.
      				OPTIONAL { ?model a ?unionOrReplace. FILTER (?unionOrReplace = ccrt:UnionModel) }
				}
		"""

    val msRset = QueryHelper.execModelQueryWithPrefixHelp(myDirectoryModel, msqText);
    import scala.collection.JavaConversions._;
    while (msRset.hasNext()) {
      val qSoln: QuerySolution = msRset.next();

      val repoRes: Resource = qSoln.getResource("repo");
      val modelRes: Resource = qSoln.getResource("model");
      val unionOrReplaceRes: Resource = qSoln.getResource("unionOrReplace");
      val repoPath_Lit: Literal = qSoln.getLiteral("repoPath")
      val modelPath_Lit: Literal = qSoln.getLiteral("modelPath")
      val dbgArray = Array[Object](repoRes, repoPath_Lit, modelRes, modelPath_Lit);

      getLogger.info("repo={}, repoPath={}, model={}, modelPath={}", dbgArray);

      val rPath = repoPath_Lit.getString();
      val mPath = modelPath_Lit.getString();

      getLogger.info("Ready to read from [{}] / [{}]", Array[Object](rPath, mPath));
      val rdfURL = rPath + mPath;
      repo.addLoadTask(rdfURL, new Runnable() {
        def run() {
          try {
            val graphURI = modelRes.getURI();
            val fileModel = FancyRepoLoader.readModelSheetFromURL(rdfURL, nsJavaMap, clList);
            getLogger.info("Read fileModel: {}", Array[Object](fileModel))
            FancyRepoLoader.replaceOrUnion(mainDset, unionOrReplaceRes, graphURI, fileModel);
          } catch {
            case except: Throwable => getLogger.error("Caught error loading file {}", Array[Object](rdfURL, except))
          }
        }
      })
    }
  }

  /// Loads a CSV File into a dir model (this is weird since you might need to have a Namespace Map .. to understand your CSV )
  /*def loadCsvFileSheetRepo(dirSheet: String, nsSheetLocation: String, fileModelCLs: java.util.List[ClassLoader], repoSpec: RepoSpec): SheetRepo = {
    // Read the namespaces and directory sheets into a single directory model.
    val dirModel: Model = readDirectoryModelFromCsvFile(dirSheet, fileModelCLs, nsSheetLocation)
    // Construct a repo around that directory
    val shRepo = FancyRepoLoader.makeRepoWithDirectory(repoSpec, dirModel, fileModelCLs)
    // Load the rest of the repo's initial *sheet* models, as instructed by the directory.
    shRepo.loadSheetModelsIntoMainDataset()
    // Load the rest of the repo's initial *file/resource* models, as instructed by the directory.
    shRepo.loadDerivedModelsIntoMainDataset(fileModelCLs)
    shRepo
  }*/

  def getCsvReaderAt(dirSheet: String, fileModelCLs: java.util.List[ClassLoader]): Reader = {
    val efsu = new ExtendedFileStreamUtils();
    val is = efsu.openInputStreamOrNull(dirSheet, fileModelCLs);
    if (is == null) {
      getLogger.error("Cant get getCsvReaderAt =" + dirSheet)
      return null;
    } else new InputStreamReader(is);
  }

  def readModelSheet(dirSheet: String, nsJavaMap: java.util.Map[String, String], fileModelCLs: java.util.List[ClassLoader]): Model = {
    val tgtModel: Model = RepoDatasetFactory.createPrivateMemModel
    tgtModel.setNsPrefixes(nsJavaMap)
    val modelInsertProc = new SemSheet.ModelInsertSheetProc(tgtModel);
    val reader: Reader = getCsvReaderAt(dirSheet, fileModelCLs);
    MatrixData.processSheetR(reader, modelInsertProc.processRow);
    getLogger.debug("tgtModel=" + tgtModel)
    tgtModel;
  }

  def readDirectoryModelFromCsvFile(dirSheet: String, fileModelCLs: java.util.List[ClassLoader], nsSheetLocation: String = null): Model = {
    getLogger.debug("readDirectoryModelFromCsvFile - start")
    val nsJavaMap: java.util.Map[String, String] = new java.util.HashMap[String, String]();
    if (nsSheetLocation != null) {
      val nsSheetReader = getCsvReaderAt(nsSheetLocation, fileModelCLs)
      nsJavaMap.putAll(MatrixData.readJavaMapFromSheetR(nsSheetReader))
      getLogger.debug("Got NS map: " + nsJavaMap)
    }
    val dirModel: Model = readModelSheet(dirSheet, nsJavaMap, fileModelCLs);
    dirModel;
  }

  /////////////////////////////////////////
  /// These are tests below  
  /////////////////////////////////////////

  val nsSheetPath = "Nspc.Csv";
  val dirSheetPath = "Dir.Csv";
  val queriesSheetPath = "Qry.Csv"

  private def loadTestCsvFileSheetRepo(): SheetRepo = {
    val clList: java.util.ArrayList[ClassLoader] = null;
    val spec = new CSVFileRepoSpec(dirSheetPath, nsSheetPath, clList)
    val sr = spec.makeRepo
    sr.getMainQueryDataset()
    // sr.loadDerivedModelsIntoMainDataset(clList)
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

    val sr = loadTestCsvFileSheetRepo()
    val qib = sr.makeInitialBinding

    qib.bindQName(lightsGraphVarName, lightsGraphQName)

    val solnJavaList: java.util.List[QuerySolution] = sr.queryIndirectForAllSolutions(querySheetQName, queryQName, qib.getQSMap);

    println("Found solutions: " + solnJavaList)
  }

  def testSemSheet(args: Array[String]): Unit = {
    println("SemSheet test ");
    val nsSheetURL = nsSheetPath;
    println("Made Namespace Sheet URL: " + nsSheetURL);
    // val namespaceMapProc = new MapSheetProc(1);
    // MatrixData.processSheet (nsSheetURL, namespaceMapProc.processRow);
    // namespaceMapProc.getJavaMap
    val nsJavaMap: java.util.Map[String, String] = MatrixData.readJavaMapFromSheet(nsSheetURL);

    println("Got NS map: " + nsJavaMap)

    val fileModelCLs = new java.util.ArrayList[ClassLoader];

    val dirModel: Model = readModelSheet(dirSheetPath, nsJavaMap, fileModelCLs);

    val queriesSheetPath = "Qry"
    val queriesModel: Model = readModelSheet(queriesSheetPath, nsJavaMap, fileModelCLs);

    val tqText = "select ?sheet { ?sheet a ccrt:CsvFileSheet }";

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
