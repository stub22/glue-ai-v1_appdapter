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

package org.appdapter.core.repo

import java.util.{ArrayList, Date}

import org.appdapter.bind.rdf.jena.model.JenaFileManagerUtils
import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.matdat.{CsvFileSheetLoader, GoogSheetRepoLoader, XLSXSheetRepoLoader}
import org.appdapter.core.name.Ident
import org.appdapter.core.store.{ExtendedFileStreamUtils, InitialBinding, Repo, RepoOper}
import org.appdapter.core.store.dataset.SpecialRepoLoader
import org.appdapter.impl.store.FancyRepo

import com.hp.hpl.jena.query.{Dataset, QuerySolution}
import com.hp.hpl.jena.rdf.model.{Model, Resource}
/*
 * @author Stu B. <www.texpedient.com>
 * @author logicmoo
 */

abstract class InstallableSpecReader {
  def getExt(): String;
  def makeRepoSpec(path: String, args: Array[String], cLs: java.util.List[ClassLoader]): RepoSpec;
}
abstract class InstallableRepoReader extends InstallableSpecReader {
  //override def getExt(): String = null
  override def makeRepoSpec(path: String, args: Array[String], cLs: java.util.List[ClassLoader]): RepoSpec = null
  def getContainerType(): String
  def getSheetType(): String
  def isDerivedLoader(): Boolean = false
  def loadModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset, dirModel: Model, fileModelCLs: java.util.List[ClassLoader])
}

object FancyRepoLoader extends BasicDebugger {

  def addInvisbleInfo(in: String, k: String, v: String): String = {
    in + "/*" + k + "=" + v + "*/"
  }

  var initedOnce = false;
  val specLoaders: java.util.List[InstallableSpecReader] = new java.util.ArrayList
  val dirModelLoaders: java.util.List[InstallableRepoReader] = new java.util.ArrayList
  def getDirModelLoaders(): java.util.List[InstallableRepoReader] = {
    dirModelLoaders.synchronized {
      if (dirModelLoaders.size() < 4) {
        dirModelLoaders.add(new GoogSheetRepoLoader())
        dirModelLoaders.add(new XLSXSheetRepoLoader())
        dirModelLoaders.add(new CsvFileSheetLoader())
        dirModelLoaders.add(new DatabaseRepoLoader())
        dirModelLoaders.add(new FileModelRepoLoader())
        dirModelLoaders.add(new MultiRepoLoader())

        //this should be made to work so far Doug hadnt done it
        dirModelLoaders.add(new DerivedModelLoader())
        // the next is loaded loadDerivedModelsIntoMainDataset (which are pipeline models)
        //dirModelLoaders.add(new PipelineModelLoader())
        dirModelLoaders.add(new PipelineSnapLoader())
        //dirModelLoaders.add(new LastModelLoader())

      }
      return new ArrayList[InstallableRepoReader](dirModelLoaders)
    }
  }
  def getSpecLoaders(): java.util.List[InstallableSpecReader] = {
    specLoaders.synchronized {
      if (specLoaders.size < 3) {
        specLoaders.add(new ScanURLDirModelRepoSpecReader())
        specLoaders.add(new URLDirModelRepoSpecReader())
        specLoaders.add(new URLModelRepoSpecReader())
        specLoaders.add(new ScanURLModelRepoSpecReader())
        for (l <- getDirModelLoaders.toArray(new Array[InstallableRepoReader](0))) {
          specLoaders.add(l);
        }
      }
      return new ArrayList[InstallableSpecReader](specLoaders)
    }
  }

  def updateDatasetFromDirModel(repoLoader: SpecialRepoLoader, mainDset: Dataset, dirModel: Model, fileModelCLs: java.util.List[ClassLoader]) {
    val dirModelLoaders: java.util.List[InstallableRepoReader] = getDirModelLoaders
    val dirModelLoaderIter = dirModelLoaders.listIterator
    repoLoader.setSynchronous(false)
    while (dirModelLoaderIter.hasNext()) {
      val irr = dirModelLoaderIter.next
      getLogger().trace("Loading ... " + irr.getContainerType + "/" + irr)
      try {
        if (irr.isDerivedLoader) {
          // this means what we are doing might need previous requests to complete
          repoLoader.setSynchronous(true)
        } else {
          //repoLoader.setSynchronous(false)
          repoLoader.setSynchronous(true)
        }
        irr.loadModelsIntoTargetDataset(repoLoader, mainDset, dirModel, fileModelCLs)
      } catch {
        case except: Throwable =>
          except.printStackTrace
          getLogger().error("Caught loading error in {}", Array[Object](irr, except))
      }
    }
    // not done until the last task completes
    repoLoader.setSynchronous(true)
  }
  def makeRepoWithDirectory(spec: RepoSpec, dirModel: Model, fileModelCLs: java.util.List[ClassLoader] = null, dirGraphID: Ident = null): DirectRepo = {
    val specURI = spec.toString();
    var serial = System.identityHashCode(this);
    var myDebugName = addInvisbleInfo(specURI, "time", "" + new Date());
    if (dirGraphID != null) {
      myDebugName = addInvisbleInfo(myDebugName, "id", "" + dirGraphID);
    }
    // Construct a repo around that directory        
    val shRepo = new DirectRepo(spec, specURI, myDebugName, dirModel, fileModelCLs)
    //shRepo.beginLoading();
    // set to false to have concurrent background loading
    if (true) {
      //shRepo.finishLoading();
    }
    shRepo
  }

  def updateDatasetFromDirModel(dirModel: Model, mainDset: Dataset, fileModelCLs: java.util.List[ClassLoader], repoLoader: SpecialRepoLoader) {
    repoLoader.setSynchronous(false)
    FancyRepoLoader.updateDatasetFromDirModel(repoLoader, mainDset, dirModel, fileModelCLs)
    // not done until the last task completes
    repoLoader.setSynchronous(true)
  }

  //def loadDatabaseRepo(configPath: String, optConfigResolveCL: ClassLoader, dirGraphID: Ident): FancyRepo = {
  // val dbRepo = DatabaseRepoFactoryLoader.makeDatabaseRepo(configPath, optConfigResolveCL, dirGraphID)
  //// dbRepo;
  //}

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
    RepoOper.putNamedModel(mainDset, graphURI, sheetModel, unionOrReplaceRes)
  }

  // Makes directory models (ussualy still unloaded)
  def readDirectoryModelFromURL(rdfURL: String, nsJavaMap: java.util.Map[String, String], fileModelCLs: java.util.List[ClassLoader]): Model = {
    try {
      getLogger.debug("readDirectoryModelFromURL - start {}", rdfURL)
      val ext: java.lang.String = org.appdapter.fileconv.FileStreamUtils.getFileExt(rdfURL);
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
      val efsu = new ExtendedFileStreamUtils()
      val ext: java.lang.String = org.appdapter.fileconv.FileStreamUtils.getFileExt(rdfURL);
      if (ext != null && (ext.equals("xlsx") || ext.equals("xls"))) {
        XLSXSheetRepoLoader.loadXLSXSheetRepo(rdfURL, "Nspc", "Dir", clList).getMainQueryDataset().asInstanceOf[Dataset].getDefaultModel
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

  ///. Modeled on GoogSheetRepo.loadTestSheetRepo
  def loadDetectedFileSheetRepo(rdfURL: String, nsJavaMap: java.util.Map[String, String], fileModelCLs: java.util.List[ClassLoader], repoSpec: RepoSpec): FancyRepo = {
    // Read the namespaces and directory sheets into a single directory model.
    val dirModel: Model = readDirectoryModelFromURL(rdfURL, nsJavaMap, fileModelCLs)
    // Construct a repo around that directory
    val shRepo = makeRepoWithDirectory(repoSpec, dirModel, fileModelCLs)
    // Load the rest of the repo's initial *sheet* models, as instructed by the directory.
    //shRepo.loadSheetModelsIntoMainDataset()
    // Load the rest of the repo's initial *file/resource* models, as instructed by the directory.
    // shRepo.loadDerivedModelsIntoMainDataset(fileModelCLs)
    shRepo
  }

}