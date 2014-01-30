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

package org.appdapter.core.matdat
import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.name.Ident
import org.appdapter.core.store.{ Repo, InitialBinding }
import org.appdapter.impl.store._

import com.hp.hpl.jena.query.{ QuerySolution }
import com.hp.hpl.jena.rdf.model.{ Model }

/**
 * @author Stu B. <www.texpedient.com>
 */

object RepoTester_TESTS_ONLY extends BasicDebugger {
  // Modeled on SheetRepo.loadTestSheetRepo
  def loadGoogSheetRepo(sheetKey: String, namespaceSheetNum: Int, dirSheetNum: Int,
    fileModelCLs: java.util.List[ClassLoader]): SheetRepo = {
    // Read the namespaces and directory sheets into a single directory model.
    //val dirModel: Model = GoogSheetRepoLoader.readDirectoryModelFromGoog(sheetKey, namespaceSheetNum, dirSheetNum)
    // Construct a repo around that directory        
    // 2013-05-28: Stu temp restored old version of loader		
    val spec = new OnlineSheetRepoSpec(sheetKey, namespaceSheetNum, dirSheetNum, fileModelCLs);
    // Doug's locally testing this replacement [and comitted about April 25, on purpose?]
    val shRepo = spec.makeRepo; // new OmniLoaderRepo(spec, "goog:" + sheetKey + "/" + namespaceSheetNum + "/" + dirSheetNum, dirModel, fileModelCLs);//

    // Load the rest of the repo's initial *sheet* models, as instructed by the directory.
    getLogger().debug("Loading Sheet Models")
    shRepo.getMainQueryDataset();
    // if shRepo is an OmniLoaderRepo, this results in a call to ensureUpdated(), which does a lot of stuff.
    //shRepo.loadSheetModelsIntoMainDataset()
    // Load the rest of the repo's initial *file/resource* models, as instructed by the directory.
    // 2013-05-28: Stu temp restored old version of loader
    // getLogger().debug("Loading File Models")
    // unnecessary if shRepo is an OmniLoaderRepo
    //  shRepo.loadDerivedModelsIntoMainDataset(fileModelCLs)
    shRepo
  }

  // Modeled on SheetRepo.loadTestSheetRepo
  def loadXLSXSheetRepo(sheetLocation: String, namespaceSheetName: String, dirSheetName: String,
    fileModelCLs: java.util.List[ClassLoader]): SheetRepo = {
    // Read the namespaces and directory sheets into a single directory model.
    val dirModel: Model = XLSXSheetRepoLoader.readDirectoryModelFromXLSX(sheetLocation, namespaceSheetName, dirSheetName, fileModelCLs)
    // Construct a repo around that directory
    //val shRepo = new XLSXSheetRepo(dirModel, fileModelCLs);   
    // Doug's locally testing this replacement   
    val spec = new OfflineXlsSheetRepoSpec(sheetLocation, namespaceSheetName, dirSheetName, fileModelCLs);
    val shRepo = new OmniLoaderRepo(spec, "xlsx:" + sheetLocation + "/" + namespaceSheetName + "/" + dirSheetName, dirModel, fileModelCLs)
    // Load the rest of the repo's initial *sheet* models, as instructed by the directory.
    getLogger().debug("Loading Sheet Models")
    shRepo.getMainQueryDataset();

    //    shRepo.loadSheetModelsIntoMainDataset()
    // Load the rest of the repo's initial *file/resource* models, as instructed by the directory.
    //getLogger().debug("Loading File Models")
    //shRepo.loadFileModelsIntoMainDataset(fileModelCLs)
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
}
