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

package org.appdapter.core.repo

import org.appdapter.core.boot.ClassLoaderUtils
import org.appdapter.core.store.{ BasicRepoImpl, Repo, RepoOper }
import org.appdapter.core.store.dataset.RepoDatasetFactory
import org.appdapter.impl.store.FancyRepo
import com.hp.hpl.jena.query.Dataset
import com.hp.hpl.jena.rdf.model.Model
import org.appdapter.core.loader.{SpecialRepoLoader, LoadingRepoImpl}
import com.hp.hpl.jena.query.DatasetFactory

class DirectRepo(val myRepoSpecForRef: RepoSpec, val myDebugNameToStr: String, val myBasePath: String,
	val myDirectoryModel: Model, var fileModelCLs: java.util.List[ClassLoader] = null) 
	extends LoadingRepoImpl with FancyRepo with Repo.Updatable with Repo.WithDirectory with RepoOper.ReloadableDataset {

  def this(directoryModel: Model) =
    this(null, null, null, directoryModel, null)

  def this(directoryModel: Model, fmcls: java.util.List[ClassLoader]) =
    this(null, null, null, directoryModel, fmcls)

  def this(myRepoSpecStart: RepoSpec, myDebugNameIn: String, directoryModel: Model, fmcls: java.util.List[ClassLoader]) = {
    this(myRepoSpecStart, myDebugNameIn, myDebugNameIn, directoryModel, fmcls)
  }

  override def getDirectoryModel: Model = myDirectoryModel;

  override def getMainQueryDataset(): Dataset = {
    loadSheetModelsIntoMainDataset();
    super.getMainQueryDataset();
  }

  /*def reloadDirModel = {
    val myNewDirectoryModel = myRepoSpecForRef.getDirectoryModel
    val oldDirModel = getDirectoryModel
    RepoOper.replaceModelElements(oldDirModel, myNewDirectoryModel)
  }*/
	// Exposed via trait ReloadableDataset...for use by whom?
	 def reloadAllModels () {
		 reloadAllModelsNow
	 }
	/**
	 *  
	 */
  private def reloadAllModelsNow {
    val dirModel: Model = if (myRepoSpecForRef != null) myRepoSpecForRef.getDirectoryModel else getDirectoryModel
	val targetMainDS = getMainQueryDataset
    if (targetMainDS != null) {
		getLogger.info("Refreshing existing dataset at {}", this)
      val oldDirModel = getDirectoryModel
      val sourceDS = makeDatasetFromDirModel(dirModel);
      RepoOper.replaceDatasetElements(targetMainDS, sourceDS)
    } else {
		getLogger.info("Making fresh dataset at {}", this)
		val freshMainDSet = makeMainQueryDataset
		setMainQueryDataset(freshMainDSet)
		FancyRepoLoader.updateDatasetFromDirModel(dirModel, freshMainDSet, fileModelCLs, getRepoLoader)
    }
  }

  private def getClassLoaderList(clList: java.util.List[ClassLoader] = null): java.util.List[ClassLoader] = {
    ClassLoaderUtils.getFileResourceClassLoaders(ClassLoaderUtils.ALL_RESOURCE_CLASSLOADER_TYPES, fileModelCLs, clList)
  }

  override def toString(): String = {
    if (myDebugNameToStr != null) return myDebugNameToStr;
    val dm = getDirectoryModel
    var dmstr = "noDirModel";
    if (dm != null) dmstr = "dir=" + dm.size
	if (isLoadingLockedOrNotStarted()) { //   if (isLoadingLocked || !isLoadingStarted) {
      getClass.getSimpleName + "[name=" + myDebugNameToStr + "  " + dmstr + " setof=Loading...]";
    } else {
      getClass.getSimpleName + "[name=" + myDebugNameToStr + "  " + dmstr + " setof=" + RepoOper.setOF(getMainQueryDataset.listNames) + "]";
    }
  }

  def reloadSingleModel(modelName: String) = {
    val repo = myRepoSpecForRef.makeRepo
    val oldDataset = getMainQueryDataset
    val myPNewMainQueryDataset = repo.getMainQueryDataset
    getLogger.info("START: Trying to do reloading of model named.. " + modelName)
    RepoOper.replaceSingleDatasetModel(oldDataset, myPNewMainQueryDataset, modelName)
    getLogger.info("START: Trying to do reloading of model named.. " + modelName)
  }

  private[core] def loadSheetModelsIntoMainDataset() {
    if (!isUpdatedFromDirModel()) {
      beginLoading
    }
    finishLoading
  }
  override def callLoadingInLock {
    //this.synchronized
    {
      var reloadTries: Int = 2;
      val dirModel = getDirectoryModel
      while (!this.isUpdatedFromDirModel && reloadTries > 0) {
        if (reloadTries == 1) {
          getLogger.error("OLDBUG: Looping on Reloads!")
        }
        reloadTries = reloadTries - 1
        getLogger.trace("Loading OnmiRepo to make UpToDate")
        var dirModelSize = dirModel.size;
        // only load from non empty dir models
        // this is because we need to have non initialized repos at times
		val oldMainDSet = getMainQueryDataset
        if (dirModelSize == 0) {
          if (oldMainDSet != null) {
            RepoOper.clearAll(oldMainDSet)
			setUpdatedFromDirModel(true);
          }
        } else {
          if (oldMainDSet == null) {
            setUpdatedFromDirModel(true);
			getLogger.info("Making a new mainQueryDset for repo: {}", this)
            val replacingDSet = makeMainQueryDataset
			setMainQueryDataset(replacingDSet)
            FancyRepoLoader.updateDatasetFromDirModel(dirModel, replacingDSet, getClassLoaderList(fileModelCLs), getRepoLoader)
          } else {
            setUpdatedFromDirModel(true);
            reloadAllModelsNow
          }
          var newModelSize = dirModel.size;
          if (newModelSize != dirModelSize && dirModel == getDirectoryModel) {
            getLogger.warn("OnmiRepo Dir.size changed durring load!  " + dirModelSize + " -> " + newModelSize)
            setUpdatedFromDirModel(true);
            // we should just should call updateFromDirModel function again
            // but likely there is already a bug that cause the size changed
            // and we'd be in an infinate loop if we didn't use reloadTries
            // Not that this even is happening but could start with either a new feature or a new bug
          }
        }
      }
    }
  }

  private def makeDatasetFromDirModel(dirModel: Model): Dataset = {
    val newDS = DatasetFactory.create();
    FancyRepoLoader.updateDatasetFromDirModel(dirModel, newDS, fileModelCLs, getRepoLoader)
    newDS
  }
  //includeDirModel() appears to be unused
  /*
  private def includeDirModel(dirModel: Model) {
	// was accessing the now protected field as follows:
	// if (myMainQueryDataset == null) {
	//   myMainQueryDataset = getMainQueryDataset
	// }
	val mainDSet = getMainQueryDataset
    if (mainDSet == null) {
		val replacingDSet = makeMainQueryDataset  // should be what?
		setMainQueryDataset(replacingDSet)
    }
	val currDSet = getMainQueryDataset
    myDirectoryModel.add(dirModel)
    FancyRepoLoader.updateDatasetFromDirModel(dirModel, currDSet, fileModelCLs, getRepoLoader)
  }
  */
}

