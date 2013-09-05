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

import java.util._

import org.appdapter.bind.rdf.jena.model.JenaFileManagerUtils
import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.name.Ident
import org.appdapter.core.store.{ InitialBinding, Repo }
import org.appdapter.core.store.FileStreamUtils
import org.appdapter.core.store.RepoOper

import com.hp.hpl.jena.query.{ Dataset, QuerySolution }
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.Resource
import org.appdapter.impl.store._
import org.appdapter.core.log.BasicDebugger

/**
 * @author Stu B. <www.texpedient.com>
 */

abstract class InstallableRepoReader {
  def getContainerType(): String
  def getSheetType(): String
  def isDerivedLoader(): Boolean = false
  def loadModelsIntoTargetDataset(repo: Repo.WithDirectory, mainDset: Dataset, dirModel: Model, fileModelCLs: java.util.List[ClassLoader])
}

object FancyRepoLoader extends BasicDebugger {

  def addInvisbleInfo(in: String, k: String, v: String): String = {
    in + "/*" + k + "=" + v + "*/"
  }

  def makeRepoWithDirectory(spec: RepoSpec, dirModel: Model, fileModelCLs: java.util.List[ClassLoader] = null, dirGraphID: Ident = null): OmniLoaderRepo = {
    val specURI = spec.toString();
    var serial = System.identityHashCode(this);
    var myDebugName = addInvisbleInfo(specURI, "time", "" + new Date());
    if (dirGraphID != null) {
      myDebugName = addInvisbleInfo(myDebugName, "id", "" + dirGraphID);
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

  /*def copyAllRepoModels(sourceRepo: Repo.WithDirectory, targetRepo: Repo.WithDirectory): Unit = {
    RepoOper.copyAllRepoModels(sourceRepo,targetRepo)    
  }*/

  def replaceOrUnion(mainDset: Dataset, unionOrReplaceRes: Resource, graphURI: String, sheetModel: Model) {
    RepoOper.replaceNamedModel(mainDset, graphURI, sheetModel, unionOrReplaceRes)
  }

  // Makes directory models (ussualy still unloaded)
  def readDirectoryModelFromURL(rdfURL: String, nsJavaMap: java.util.Map[String, String], fileModelCLs: java.util.List[ClassLoader]): Model = {
    try {
      getLogger.debug("readDirectoryModelFromURL - start {}", rdfURL)
      val ext: java.lang.String = FileStreamUtils.getFileExt(rdfURL);
      if (ext != null && (ext.equals("xlsx") || ext.equals("xls"))) {
        XLSXSheetRepoLoader.readDirectoryModelFromXLSX(rdfURL, "Nspc", "Dir", fileModelCLs);
      } else if (ext != null && (ext.equals("csv"))) {
        CsvFileSheetLoader.readModelSheet(rdfURL, nsJavaMap, fileModelCLs);
      } else readModelSheetFromURL(rdfURL, nsJavaMap, fileModelCLs);
    } catch {
      case except: Throwable => {
        getLogger().error("Caught error loading file {}", Array[Object](rdfURL, except))
        throw except
      }
    }
  }

  // Makes single Models from xlsx/cvs/jenaURLs
  def readModelSheetFromURL(rdfURL: String, nsJavaMap: java.util.Map[String, String], clList: java.util.List[ClassLoader]): Model = {
    try {
      val ext: java.lang.String = FileStreamUtils.getFileExt(rdfURL);
      if (ext != null && (ext.equals("xlsx") || ext.equals("xls"))) {
        XLSXSheetRepoLoader.loadXLSXSheetRepo(rdfURL, "Nspc", "Dir", clList, null).
          getMainQueryDataset().asInstanceOf[Dataset].getDefaultModel
      } else if (ext != null && (ext.equals("csv"))) {
        CsvFileSheetLoader.readModelSheet(rdfURL, nsJavaMap, clList);
      } else {
        import com.hp.hpl.jena.util.FileManager;
        val jenaFileMgr = JenaFileManagerUtils.getDefaultJenaFM
        JenaFileManagerUtils.ensureClassLoadersRegisteredWithJenaFM(jenaFileMgr, clList)
        jenaFileMgr.loadModel(rdfURL);
      }
    } catch {
      case except: Throwable => {
        getLogger().error("Caught error loading file {}", Array[Object](rdfURL, except))
        throw except
      }
    }
  }

  ///. Modeled on SheetRepo.loadTestSheetRepo
  def loadDetectedFileSheetRepo(rdfURL: String, nsJavaMap: java.util.Map[String, String], fileModelCLs: java.util.List[ClassLoader], repoSpec: RepoSpec): SheetRepo = {
    // Read the namespaces and directory sheets into a single directory model.
    val dirModel: Model = readDirectoryModelFromURL(rdfURL, nsJavaMap, fileModelCLs)
    // Construct a repo around that directory
    val shRepo = makeRepoWithDirectory(repoSpec, dirModel, fileModelCLs)
    // Load the rest of the repo's initial *sheet* models, as instructed by the directory.
    shRepo.loadSheetModelsIntoMainDataset()
    // Load the rest of the repo's initial *file/resource* models, as instructed by the directory.
    shRepo.loadDerivedModelsIntoMainDataset(fileModelCLs)
    shRepo
  }

}