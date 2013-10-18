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

import java.util.ArrayList

import org.appdapter.bind.rdf.jena.model.JenaFileManagerUtils
import org.appdapter.core.boot.ClassLoaderUtils
import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.name.Ident
import org.appdapter.core.store.{ Repo, RepoOper }
import org.appdapter.impl.store.{ DirectRepo, QueryHelper }
import org.appdapter.core.store.{ FileStreamUtils }

import com.hp.hpl.jena.query.{ Dataset, QuerySolution }
import com.hp.hpl.jena.rdf.model.{ Literal, Model, Resource }

/**
 * @author Stu B. <www.texpedient.com>
 *
 * We implement a DirectRepo with some sheet loading
 */

object SheetRepo extends BasicDebugger {
  var initedOnce = false;
  val dirModelLoaders: java.util.List[InstallableRepoReader] = new java.util.ArrayList();
  def getDirModelLoaders(): java.util.List[InstallableRepoReader] = {
    dirModelLoaders.synchronized {
      if (!initedOnce) {
        dirModelLoaders.add(new GoogSheetRepoLoader());
        dirModelLoaders.add(new XLSXSheetRepoLoader());
        dirModelLoaders.add(new CsvFileSheetLoader());
        dirModelLoaders.add(new FileModelRepoLoader());
        //this should be made to work so far Doug hadnt done it
        //dirModelLoaders.add(new DerivedModelLoader());
        // the next is loaded loadDerivedModelsIntoMainDataset (which are pipeline models)
        //dirModelLoaders.add(new PipelineModelLoader());

      }
      return new ArrayList[InstallableRepoReader](dirModelLoaders);
    }
  }

}

abstract class SheetRepo(directoryModel: Model, var fileModelCLs: java.util.List[ClassLoader] = null)
  extends DirectRepo(directoryModel) {

  /**  For All Subclasses    */
  // RESUME DIFF
  def this() =
    this(null, null)

  def this(directoryModel: Model) =
    this(directoryModel, null)
  // BEGIN NEXT DIFF

  final def loadDerivedModelsIntoMainDataset(clList: java.util.List[ClassLoader]): Unit = {
    val repoLoader = getRepoLoader();
    repoLoader.setSynchronous(true)
    val mainDset: Dataset = getMainQueryDataset().asInstanceOf[Dataset];
    var clListG = this.getClassLoaderList(clList)
    if (myDirectoryModel.size == 0) return ;
    //FileModelRepoLoader.loadSheetModelsIntoTargetDataset(this, mainDset, myDirectoryModel, clListG)
    //DerivedModelLoader.loadSheetModelsIntoTargetDataset(this, mainDset, myDirectoryModel, clListG)
    //PipelineModelLoader.loadSheetModelsIntoTargetDataset(this, mainDset, myDirectoryModel, clListG)
  }

  def loadSheetModelsIntoMainDataset() {
    ensureUpdatedPrivate();
  }

  final def loadFileModelsIntoMainDataset(clList: java.util.List[ClassLoader]) = {
    val repoLoader = getRepoLoader();
    repoLoader.setSynchronous(true)
    loadDerivedModelsIntoMainDataset(clList);
  }

  var myBasePath0: String = null;
  var myIdent0: Ident = null;

  def reloadAllModelsFromDir() = {
    val oldDataset = getMainQueryDataset();
    val oldDirModel = getDirectoryModel();
    val repo = myRepoSpecForRef.makeRepo();
    val myNewDirectoryModel = repo.getDirectoryModel();
    val myPNewMainQueryDataset = repo.getMainQueryDataset();
    val repoLoader = getRepoLoader();
    repoLoader.reset();
    RepoOper.replaceModelElements(oldDirModel, myNewDirectoryModel)
    RepoOper.replaceDatasetElements(oldDataset, myPNewMainQueryDataset)
    repoLoader.setLastJobSubmitted();
    //reloadMainDataset();
  }

  def getClassLoaderList(clList: java.util.List[ClassLoader] = null): java.util.List[ClassLoader] = {
    ClassLoaderUtils.getFileResourceClassLoaders(ClassLoaderUtils.ALL_RESOURCE_CLASSLOADER_TYPES, fileModelCLs, clList);
  }
  override def toString(): String = {
    if (myDebugNameToStr != null) return myDebugNameToStr;
    val dm = getDirectoryModel();
    var dmstr = "noDirModel";
    if (dm != null) dmstr = "dir=" + dm.size();
    if (isLoadingLocked || !isLoadingStarted) {
      getClass.getSimpleName + "[name=" + myDebugNameToStr + "  " + dmstr + " setof=Loading...]";
    } else {
      getClass.getSimpleName + "[name=" + myDebugNameToStr + "  " + dmstr + " setof=" + RepoOper.setOF(getMainQueryDataset.listNames) + "]";
    }
  }

  def reloadSingleModelByName(modelName: String) = {
    val repo = myRepoSpecForRef.makeRepo();
    val oldDataset = getMainQueryDataset();
    val myPNewMainQueryDataset = repo.getMainQueryDataset();
    getLogger.info("START: Trying to do reloading of model named.. " + modelName)
    RepoOper.replaceDatasetElements(oldDataset, myPNewMainQueryDataset, modelName)
    getLogger.info("START: Trying to do reloading of model named.. " + modelName)
  }

  var myRepoSpecForRef: RepoSpec = null;
  var myDebugNameToStr: String = null;

  override def callLoadingInLock(): Unit = {
    // Load the rest of the repo's initial *sheet* models, as instructed by the directory.
    getLogger().debug("Loading Sheet Models")
    loadSheetModelsIntoMainDataset()
    // Load the rest of the repo's initial *file/resource* models, as instructed by the directory.
    getLogger().debug("Loading File Models")
    loadDerivedModelsIntoMainDataset(fileModelCLs)
    getLogger().debug("Done loading")
  }

  def ensureUpdatedPrivate() = {
    {
      //this.synchronized
      {
        beginLoading();
        finishLoading();
        if (!this.isUpdatedFromDirModel) {
          trace("Loading OnmiRepo to make UpToDate")
          this.isUpdatedFromDirModel = true;
          var dirModelSize = getDirectoryModel().size;
          // only load from non empty dir models
          // this is because we need to have non initalized repos at times
          if (dirModelSize > 0) updateFromDirModel

          var newModelSize = getDirectoryModel().size;
          if (newModelSize != dirModelSize) {
            trace("OnmiRepo Dir.size changed!  " + dirModelSize + " -> " + newModelSize)
            this.isUpdatedFromDirModel = false;
          }
        } else {
          //traceHere("OnmiRepo was UpToDate")
        }
      }
    }
  }

  def updateFromDirModel() {
    val repoLoader = getRepoLoader();
    val mainDset: Dataset = getMainQueryDataset().asInstanceOf[Dataset];
    val dirModel = getDirectoryModel;
    val fileModelCLs: java.util.List[ClassLoader] = this.getClassLoaderList(this.fileModelCLs);
    val dirModelLoaders: java.util.List[InstallableRepoReader] = SheetRepo.getDirModelLoaders();
    val dirModelLoaderIter = dirModelLoaders.listIterator();
    repoLoader.setSynchronous(false)
    while (dirModelLoaderIter.hasNext()) {
      val irr = dirModelLoaderIter.next();
      trace("Loading ... " + irr.getContainerType + "/" + irr.getSheetType);
      try {
        if (irr.isDerivedLoader) {
          // this means what we are doing might need previous requests to complete
          repoLoader.setSynchronous(true)
        } else {
          repoLoader.setSynchronous(false)
        }
      irr.loadModelsIntoTargetDataset(this, mainDset, dirModel, fileModelCLs);
      } catch {
        case except: Throwable =>
          getLogger().error("Caught loading error in {}", Array[Object](irr, except))
      }
    }
    // not done until the last task completes
          repoLoader.setSynchronous(true)
  }

  def trace(fmt: String, args: Any*) = {
    try {
      getLogger.warn(fmt, args);
    } catch {
      case except: Throwable =>
    }
  }
  def warn(fmt: String, args: Any*) = {
    try {
      getLogger.warn(fmt, args);
    } catch {
      case except: Throwable =>
    }
  }
  def error(fmt: String, args: Any*) = {
    try {
      getLogger.error(fmt, args);
    } catch {
      case except: Throwable =>
    }
  }
}
