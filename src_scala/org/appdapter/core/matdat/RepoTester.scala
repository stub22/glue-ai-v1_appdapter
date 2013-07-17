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
import org.appdapter.core.name.{ Ident, FreeIdent }
import org.appdapter.core.store.{ Repo, InitialBinding }
import org.appdapter.help.repo.{ RepoClient, RepoClientImpl, InitialBindingImpl }
import org.appdapter.impl.store.{ FancyRepo, DatabaseRepo, FancyRepoFactory }
import com.hp.hpl.jena.query.{ QuerySolution }
import com.hp.hpl.jena.rdf.model.{ Model }
import org.appdapter.core.log.BasicDebugger;
// Currently we're on   Jena 2.6.4, ARQ 2.8.7, SDB 1.3.4
import com.hp.hpl.jena.sparql.sse.SSE
import com.hp.hpl.jena.sparql.modify.request.{ UpdateCreate, UpdateLoad }
import com.hp.hpl.jena.update.{ GraphStore, GraphStoreFactory, UpdateAction, UpdateRequest }
import com.hp.hpl.jena.sdb.{ Store, SDBFactory };
import com.sun.org.apache.bcel.internal.classfile.Deprecated
/**
 * @author Stu B. <www.texpedient.com>
 */

object RepoTester extends BasicDebugger {
  // Modeled on SheetRepo.loadTestSheetRepo
  def loadGoogSheetRepo(sheetKey: String, namespaceSheetNum: Int, dirSheetNum: Int,
    fileModelCLs: java.util.List[ClassLoader]): SheetRepo = {
    // Read the namespaces and directory sheets into a single directory model.
    val dirModel: Model = GoogSheetRepoLoader.readDirectoryModelFromGoog(sheetKey, namespaceSheetNum, dirSheetNum)
    // Construct a repo around that directory        
    // 2013-05-28: Stu temp restored old version of loader		
    val spec = new OnlineSheetRepoSpec(sheetKey, namespaceSheetNum, dirSheetNum, fileModelCLs);
    // Doug's locally testing this replacement [and comitted about April 25, on purpose?]
    val shRepo = spec.makeRepo; // new OmniLoaderRepo(spec, "goog:" + sheetKey + "/" + namespaceSheetNum + "/" + dirSheetNum, dirModel, fileModelCLs);//

    // Load the rest of the repo's initial *sheet* models, as instructed by the directory.
    getLogger().debug("Loading Sheet Models")
    // if shRepo is an OmniLoaderRepo, this results in a call to ensureUpdated(), which does a lot of stuff.
    shRepo.loadSheetModelsIntoMainDataset()
    // Load the rest of the repo's initial *file/resource* models, as instructed by the directory.
    // 2013-05-28: Stu temp restored old version of loader
    getLogger().debug("Loading File Models")
    // unnecessary if shRepo is an OmniLoaderRepo
    shRepo.loadFileModelsIntoMainDataset(fileModelCLs)
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
    shRepo.loadSheetModelsIntoMainDataset()
    // Load the rest of the repo's initial *file/resource* models, as instructed by the directory.
    //getLogger().debug("Loading File Models")
    //shRepo.loadFileModelsIntoMainDataset(fileModelCLs)
    shRepo
  }

  def loadDatabaseRepo(configPath: String, optConfigResolveCL: ClassLoader, dirGraphID: Ident): DatabaseRepo = {
    val dbRepo = FancyRepoFactory.makeDatabaseRepo(configPath, optConfigResolveCL, dirGraphID)
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

  def copyAllRepoModels(sourceRepo: Repo.WithDirectory, targetRepo: Repo.WithDirectory): Unit = {
  }
}
class BetterDatabaseRepo(sdbStore: Store, dirGraphID: Ident) extends DatabaseRepo(sdbStore, dirGraphID) {
  //	Current docs for GraphStoreFactory (more recent than the code version we're using) say,
  //	regarding   GraphStoreFactory. reate(Dataset dataset)
  //	 Create a GraphStore from a dataset so that updates apply to the graphs in the dataset.
  //	 Throws UpdateException (an ARQException) if the GraphStore can not be created. This 
  //	 is not the way to get a GraphStore for SDB or TDB - an SDB Store object is a GraphStore 
  //	 no conversion necessary.
  //	 
  //	 
  def graphStoreStuff() = {
    val graphName = "http://example/namedGraph";

    // Create an empty GraphStore (has an empty default graph and no named graphs) 
    // val graphStore : GraphStore  = GraphStoreFactory.create() ;
    // 

    val readStore: Store = getStore();
    val sdbUpdateGraphStore: GraphStore = SDBFactory.connectGraphStore(readStore);

    // A sequence of operations
    val upSpec: UpdateRequest = new UpdateRequest();

    // Create a named graph
    val creReq: UpdateCreate = new UpdateCreate(graphName);

    // Load a file into a named graph - NB order of arguments (both strings).
    val loadReq: UpdateLoad = new UpdateLoad("etc/update-data.ttl", graphName);

    // Add the two operations and execute the request
    //@SuppressWarnings Deprecated
    upSpec.add(creReq)
    upSpec.add(loadReq)

    // Execute 
    UpdateAction.execute(upSpec, sdbUpdateGraphStore);

    // Print it out (format is SSE <http://jena.hpl.hp.com/wiki/SSE>)
    // used to represent a dataset.
    // Note the empty default, unnamed graph
    SSE.write(sdbUpdateGraphStore);
  }
}
