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
import org.appdapter.core.boot.ClassLoaderUtils
import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.name.Ident
import org.appdapter.core.store.{ Repo, RepoOper }
import org.appdapter.impl.store.DirectRepo
import com.hp.hpl.jena.query.Dataset
import com.hp.hpl.jena.rdf.model.Model
import org.appdapter.core.store.dataset.RepoDatasetFactory
import org.appdapter.core.store.dataset.SpecialRepoLoader

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
        dirModelLoaders.add(new PipelineSnapLoader());

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

  def reloadDirModel = {
    val repo = myRepoSpecForRef.makeRepo();
    val myNewDirectoryModel = repo.getDirectoryModel();
    val oldDirModel = getDirectoryModel();
    RepoOper.replaceModelElements(oldDirModel, myNewDirectoryModel)
  }

  def reloadAllModelsFromDir() = {
    val newDS = makeDatasetFromDirModel();
    val mainDS = getMainQueryDataset();
    RepoOper.replaceDatasetElements(mainDS, newDS);
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
    RepoOper.replaceSingleDatasetModel(oldDataset, myPNewMainQueryDataset, modelName)
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
        var reloadTries: Int = 2;
        val dirModel = getDirectoryModel();
        while (!this.isUpdatedFromDirModel && reloadTries > 0) {
          if (reloadTries == 1) {
            getLogger.error("OLDBUG: Looping on Reloads!")
          }
          reloadTries = reloadTries - 1
          trace("Loading OnmiRepo to make UpToDate")
          var dirModelSize = dirModel.size;
          // only load from non empty dir models
          // this is because we need to have non initialized repos at times
          if (dirModelSize == 0) {
            if (myMainQueryDataset != null) {
              RepoOper.clearAll(myMainQueryDataset);
              this.isUpdatedFromDirModel = true;
            }
          } else {
            if (this.myMainQueryDataset == null) {
              this.isUpdatedFromDirModel = true;
              this.myMainQueryDataset = makeDatasetFromDirModel();
            } else {
              this.isUpdatedFromDirModel = true;
              reloadAllModelsFromDir
            }
            var newModelSize = dirModel.size;
            if (newModelSize != dirModelSize && dirModel == getDirectoryModel) {
              warn("OnmiRepo Dir.size changed durring load!  " + dirModelSize + " -> " + newModelSize)
              this.isUpdatedFromDirModel = false;
              // we should just should call updateFromDirModel function again
              // but likely there is already a bug that cause the size changed
              // and we'd be in an infinate loop if we didn't use reloadTries
              // Not that this even is happening but could start with either a new feature or a new bug
            }
          }
        }
      }
    }
  }

  def makeDatasetFromDirModel(): Dataset = {
    val repoLoader = getRepoLoader();
    val dirModelLoaders: java.util.List[InstallableRepoReader] = SheetRepo.getDirModelLoaders();
    val dirModelLoaderIter = dirModelLoaders.listIterator();
    val mainDset: Dataset = RepoDatasetFactory.createPrivateMem();
    repoLoader.setSynchronous(false)
    val dirModel = getDirectoryModel;
    val fileModelCLs: java.util.List[ClassLoader] = this.getClassLoaderList(this.fileModelCLs);
    updateDatasetFromDirModel(repoLoader, mainDset, dirModel, fileModelCLs);
    // not done until the last task completes
    repoLoader.setSynchronous(true)
    mainDset
  }

  def updateDatasetFromDirModel(repoLoader: SpecialRepoLoader, mainDset: Dataset, dirModel: Model, fileModelCLs: java.util.List[ClassLoader]) {
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
          except.printStackTrace
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
