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
import org.appdapter.core.store.{ RepoSpec, RepoOper, RepoClient, RepoSpecJava, Repo, InitialBinding }
import org.appdapter.help.repo. { RepoClientImpl, InitialBindingImpl}

import org.appdapter.impl.store.{ FancyRepo, DatabaseRepo, FancyRepoFactory }
import com.hp.hpl.jena.query.{ QuerySolution }
import com.hp.hpl.jena.rdf.model.{ Model }
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.bind.rdf.jena.sdb.SdbStoreFactory
import com.hp.hpl.jena.sdb.Store
import java.util.Date
/**
 * @author Stu B. <www.texpedient.com>
 */

object RepoLoader extends BasicDebugger {
  def makeDatabaseRepo(repoConfResPath: String, optCL: ClassLoader, dirGraphID: Ident): DatabaseRepo = {
    val s: Store = SdbStoreFactory.connectSdbStoreFromResPath(repoConfResPath, optCL);
    new DatabaseRepo(s, dirGraphID);
  }

  def addInvisbleInfo(in: String, k: String, v: String): String = {
    in + "/*" + k + "=" + v + "*/"
  }
  def makeSheetRepo(spec: RepoSpec, dirModel: Model, fileModelCLs: java.util.List[ClassLoader] = null, dirGraphID: Ident = null): SheetRepo = {
    val specURI = spec.toString();
    var serial = System.identityHashCode(this);
    var myDebugName = RepoLoader.addInvisbleInfo(specURI, "time", "" + new Date());
    if (dirGraphID != null) {
      myDebugName = RepoLoader.addInvisbleInfo(myDebugName, "id", "" + dirGraphID);
    }
    // Construct a repo around that directory        
    val shRepo = new OmniLoaderRepo(spec, specURI, myDebugName, dirModel, fileModelCLs)
    // Load the rest of the repo's initial *sheet* models, as instructed by the directory.
    getLogger().debug("Loading Sheet Models")
    shRepo.loadSheetModelsIntoMainDataset()
    // Load the rest of the repo's initial *file/resource* models, as instructed by the directory.
    getLogger().debug("Loading File Models")
    shRepo.loadDerivedModelsIntoMainDataset(fileModelCLs)
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