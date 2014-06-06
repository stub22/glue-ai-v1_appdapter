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

package org.appdapter.core.repo.test

import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.name.Ident
import org.appdapter.core.store.{ InitialBinding, Repo }
import com.hp.hpl.jena.query.QuerySolution
import com.hp.hpl.jena.rdf.model.Model
import org.appdapter.core.repo.DatabaseRepo
import org.appdapter.core.repo.DatabaseRepoLoader
import org.appdapter.core.repo.DirectRepo
import org.appdapter.core.repo.FancyRepo
import org.appdapter.core.repo.OfflineXlsSheetRepoSpec
import org.appdapter.core.repo.OnlineSheetRepoSpec
import org.appdapter.core.repo.XLSXSheetRepoLoader

/**
 * @author Stu B. <www.texpedient.com>
 */

object RepoTester_TESTS_ONLY extends BasicDebugger {
  // Modeled on GoogSheetRepo.loadTestSheetRepo
  def loadGoogSheetRepo(sheetKey: String, namespaceSheetNum: Int, dirSheetNum: Int,
    fileModelCLs: java.util.List[ClassLoader]): FancyRepo = {
    // Read the namespaces and directory sheets into a single directory model.
    //val dirModel: Model = GoogSheetRepoLoader.readDirectoryModelFromGoog(sheetKey, namespaceSheetNum, dirSheetNum)
    // Construct a repo around that directory        
    // 2013-05-28: Stu temp restored old version of loader		
    val spec = new OnlineSheetRepoSpec(sheetKey, namespaceSheetNum, dirSheetNum, fileModelCLs);
    // Doug's locally testing this replacement [and comitted about April 25, on purpose?]
    val shRepo = spec.makeRepo; // new GoogSheetRepo(spec, "goog:" + sheetKey + "/" + namespaceSheetNum + "/" + dirSheetNum, dirModel, fileModelCLs);//

    // Load the rest of the repo's initial *sheet* models, as instructed by the directory.
    getLogger().debug("Loading Sheet Models")
    shRepo.getMainQueryDataset();
    // if shRepo is an GoogSheetRepo, this results in a call to ensureUpdated(), which does a lot of stuff.
    //shRepo.loadSheetModelsIntoMainDataset()
    // Load the rest of the repo's initial *file/resource* models, as instructed by the directory.
    // 2013-05-28: Stu temp restored old version of loader
    // getLogger().debug("Loading File Models")
    // unnecessary if shRepo is an GoogSheetRepo
    //  shRepo.loadDerivedModelsIntoMainDataset(fileModelCLs)
    shRepo
  }

  // Modeled on GoogSheetRepo.loadTestSheetRepo
  def loadXLSXSheetRepoTest(sheetLocation: String, namespaceSheetName: String, dirSheetName: String,
    fileModelCLs: java.util.List[ClassLoader]): FancyRepo = {
    // Read the namespaces and directory sheets into a single directory model.
    val dirModel: Model = XLSXSheetRepoLoader.readDirectoryModelFromXLSX(sheetLocation, namespaceSheetName, dirSheetName, fileModelCLs)
    // Construct a repo around that directory
    //val shRepo = new XLSXSheetRepo(dirModel, fileModelCLs);   
    // Doug's locally testing this replacement   
    val spec = new OfflineXlsSheetRepoSpec(sheetLocation, namespaceSheetName, dirSheetName, fileModelCLs);
    val shRepo = new DirectRepo(spec, "xlsx:" + sheetLocation + "/" + namespaceSheetName + "/" + dirSheetName, dirModel, fileModelCLs)
    // Load the rest of the repo's initial *sheet* models, as instructed by the directory.
    getLogger().debug("Loading Sheet Models")
    shRepo.getMainQueryDataset();

    //    shRepo.loadSheetModelsIntoMainDataset()
    // Load the rest of the repo's initial *file/resource* models, as instructed by the directory.
    //getLogger().debug("Loading File Models")
    shRepo.loadSheetModelsIntoMainDataset();
    shRepo.loadDerivedModelsIntoMainDataset(fileModelCLs)
    shRepo.loadFileModelsIntoMainDataset(fileModelCLs)
    shRepo
  }

  def loadDatabaseRepo(configPath: String, optConfigResolveCL: ClassLoader, dirGraphID: Ident): DatabaseRepo = {
    val dbRepo = DatabaseRepoLoader.makeDatabaseRepo(configPath, optConfigResolveCL, dirGraphID)
    dbRepo;
  }

  def testRepoDirect(repo: Repo.WithDirectory, querySheetQName: String, queryQName: String, tgtGraphSparqlVN: String, tgtGraphQName: String): Unit = {
    // Here we manually set up a binding, as you would usually allow RepoClient
    // to do for you, instead:
    val qib: InitialBinding = repo.makeInitialBinding
    qib.bindQName(tgtGraphSparqlVN, tgtGraphQName)

    // Run the resulting fully bound query, and print the results.		
    val solnJavaList: java.util.List[QuerySolution] = repo.queryIndirectForAllSolutions(querySheetQName, queryQName, qib.getQSMap);

    println("Found solutions for " + queryQName + " in " + tgtGraphQName + " : " + solnJavaList)
  }

  /*  Commented out to ensure not using (ever) 
 *   def copyAllRepoModels(sourceRepo: Repo.WithDirectory, targetRepo: Repo.WithDirectory): Unit = {
  }*/

  final private[matdat] def loadTestXLSXSheetRepo(): FancyRepo = {
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

    val sr: FancyRepo = loadTestXLSXSheetRepo()
    val qib = sr.makeInitialBinding

    qib.bindQName(lightsGraphVarName, lightsGraphQName)

    val solnJavaList: java.util.List[QuerySolution] = sr.queryIndirectForAllSolutions(querySheetQName, queryQName, qib.getQSMap);

    println("Found solutions: " + solnJavaList)
  }

  private def testSemSheet(args: Array[String]): Unit = {
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
  final private[core] def loadXLSXSheetRepo(sheetLocation: String, namespaceSheetName: String, dirSheetName: String,
    fileModelCLs: java.util.List[ClassLoader], repoSpec: RepoSpec): FancyRepo = {
    // Read the namespaces and directory sheets into a single directory model.
    val dirModel: Model = XLSXSheetRepoLoader.readDirectoryModelFromXLSX(sheetLocation, namespaceSheetName, dirSheetName, fileModelCLs: java.util.List[ClassLoader])
    FancyRepoLoader.makeRepoWithDirectory(repoSpec, dirModel);
  }

}
