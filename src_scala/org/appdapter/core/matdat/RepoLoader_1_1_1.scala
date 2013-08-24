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

import java.util.Date

import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.name.Ident
import org.appdapter.core.store.{ InitialBinding, Repo }
import org.appdapter.impl.store.{ DatabaseRepo, FancyRepoFactory }

import com.hp.hpl.jena.query.{ Dataset, QuerySolution }
import com.hp.hpl.jena.rdf.model.Model
/**
 * @author Stu B. <www.texpedient.com>
 */

abstract class InstallableRepoReader extends SpecialRepoLoader {
  def getContainerType(): String
  def getSheetType(): String
  def isDerivedLoader(): Boolean = false
  def loadModelsIntoTargetDataset(repo: Repo.WithDirectory, mainDset: Dataset, dirModel: Model, fileModelCLs: java.util.List[ClassLoader])
}

abstract class SpecialRepoLoader extends BasicDebugger {
}

object SpecialRepoLoader extends BasicDebugger {

  def addInvisbleInfo(in: String, k: String, v: String): String = {
    in + "/*" + k + "=" + v + "*/"
  }

  def makeSheetRepo(spec: RepoSpec, dirModel: Model, fileModelCLs: java.util.List[ClassLoader] = null, dirGraphID: Ident = null): OmniLoaderRepo = {
    val specURI = spec.toString();
    var serial = System.identityHashCode(this);
    var myDebugName = SpecialRepoLoader.addInvisbleInfo(specURI, "time", "" + new Date());
    if (dirGraphID != null) {
      myDebugName = SpecialRepoLoader.addInvisbleInfo(myDebugName, "id", "" + dirGraphID);
    }
    // Construct a repo around that directory        
    val shRepo = new OmniLoaderRepo(spec, specURI, myDebugName, dirModel, fileModelCLs)
    shRepo.beginLoading();
    // set to false to have concurrent background loading
    if (true) {
      shRepo.finishLoading();
    }
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