/*
 *  Copyright 2013 by The Appdapter Project
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

package org.appdapter.test.repo

import com.hp.hpl.jena.query.QuerySolution
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.sdb.SDBFactory
import com.hp.hpl.jena.sdb.Store
import com.hp.hpl.jena.sparql.modify.request.UpdateCreate
import com.hp.hpl.jena.sparql.modify.request.UpdateLoad
import com.hp.hpl.jena.sparql.sse.SSE
import com.hp.hpl.jena.update.GraphStore
import com.hp.hpl.jena.update.UpdateAction
import com.hp.hpl.jena.update.UpdateRequest
import org.appdapter.core.matdat.CsvFilesSheetRepoLoader
import org.appdapter.core.matdat.{ SheetRepo, CSVFileRepoSpec }
import org.appdapter.core.name.Ident
import org.appdapter.core.store.InitialBinding
import org.appdapter.core.store.Repo
import org.appdapter.help.repo.RepoLoader
import org.appdapter.impl.store.DatabaseRepo
import org.appdapter.impl.store.FancyRepoFactory
/**
 * @author Stu B. <www.texpedient.com>
 */

object CsvFileRepoTester {
  // Modeled on SheetRepo.loadTestSheetRepo
  def loadSheetRepo(sheetLoc: String, namespaceSheet: String, dirSheet: String,
    fileModelCLs: java.util.List[ClassLoader]): SheetRepo = {
    // Read the namespaces and directory sheets into a single directory model.
    val dirModel: Model = CsvFilesSheetRepoLoader.readDirectoryModelFromCsvFiles(sheetLoc, namespaceSheet, dirSheet, fileModelCLs)
    // Construct a repo around that directory
    val spec = new CSVFileRepoSpec(sheetLoc, namespaceSheet, dirSheet, fileModelCLs);
    val shRepo = RepoLoader.makeSheetRepo(spec, dirModel, fileModelCLs)
    // Load the rest of the repo's initial *sheet* models, as instructed by the directory.
    shRepo.loadSheetModelsIntoMainDataset()
    // Load the rest of the repo's initial *file/resource* models, as instructed by the directory.
    shRepo.loadFileModelsIntoMainDataset(fileModelCLs)
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
// Currently we're on   Jena 2.6.4, ARQ 2.8.7, SDB 1.3.4

class CsvFileRepoTesterDontBreakSDB(sdbStore: Store, dirGraphID: Ident) extends DatabaseRepo(sdbStore, dirGraphID) {
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
    upSpec.addUpdate(creReq);
    upSpec.addUpdate(loadReq);

    // Execute 
    UpdateAction.execute(upSpec, sdbUpdateGraphStore);

    // Print it out (format is SSE <http://jena.hpl.hp.com/wiki/SSE>)
    // used to represent a dataset.
    // Note the empty default, unnamed graph
    SSE.write(sdbUpdateGraphStore);
  }
}
