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

import java.util.ArrayList
import scala.collection.immutable.StringOps
import org.appdapter.core.boot.ClassLoaderUtils
import org.appdapter.core.item.{ Item, JenaResourceItem }
import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.name.Ident
import org.appdapter.core.store.{ BasicRepoImpl, InitialBinding, Repo, RepoOper }
import org.appdapter.core.store.dataset.{ RepoDatasetFactory, SpecialRepoLoader }
import com.hp.hpl.jena.datatypes.RDFDatatype
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype
import com.hp.hpl.jena.query.{ Dataset, Query, QuerySolution, QuerySolutionMap }
import com.hp.hpl.jena.rdf.model.{ Literal, Model, Resource }
import org.appdapter.core.matdat.GoogSheetRepoLoader

// class GoogSheetRepo(val myDirectoryModel : Model) extends FancyRepo {

object DirectRepo extends BasicDebugger {
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
    val dirModelLoaders: java.util.List[InstallableRepoReader] = DirectRepo.getDirModelLoaders
    val dirModelLoaderIter = dirModelLoaders.listIterator
    repoLoader.setSynchronous(false)
    while (dirModelLoaderIter.hasNext()) {
      val irr = dirModelLoaderIter.next
      getLogger().trace("Loading ... " + irr.getContainerType + "/" + irr.getSheetType)
      try {
        if (irr.isDerivedLoader) {
          // this means what we are doing might need previous requests to complete
          repoLoader.setSynchronous(true)
        } else {
          repoLoader.setSynchronous(false)
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
}

class DirectRepo(myRepoSpecStart: RepoSpec, myDebugNameIn: String, myBasePathIn: String,
  directoryModel: Model, var fileModelCLs: java.util.List[ClassLoader] = null) extends BasicRepoImpl with FancyRepo with Repo.Updatable with Repo.WithDirectory with RepoOper.ReloadableDataset {

  val myRepoSpecForRef = myRepoSpecStart
  val myDebugNameToStr = myDebugNameIn
  //myBasePath = myBasePathIn

  def this(directoryModel: Model) =
    this(null, null, null, directoryModel, null)

  def this(directoryModel: Model, fmcls: java.util.List[ClassLoader]) =
    this(null, null, null, directoryModel, fmcls)

  def this(myRepoSpecStart: RepoSpec, myDebugNameIn: String, directoryModel: Model, fmcls: java.util.List[ClassLoader]) = {
    this(myRepoSpecStart, myDebugNameIn, myDebugNameIn, directoryModel, fmcls)
  }

  def reloadAllModels = reloadAllModelsFromDir

  def reloadSingleModel(n: String) = { reloadSingleModelByName(n) }

  override def getMainQueryDataset(): Dataset = {
    ensureUpdated();
    super.getMainQueryDataset();
  }

  /**  For All Subclasses    */
  // RESUME DIFF
  //def this() =     this(null, null)

  //def this(directoryModel: Model) =     this(directoryModel, null)
  // BEGIN NEXT DIFF

  var myBasePath0: String = null
  var myIdent0: Ident = null

  def reloadDirModel = {
    val myNewDirectoryModel = myRepoSpecForRef.getDirectoryModel
    val oldDirModel = getDirectoryModel
    RepoOper.replaceModelElements(oldDirModel, myNewDirectoryModel)
  }

  def reloadAllModelsFromDir = {
    if (myMainQueryDataset != null) {
      val oldDirModel = getDirectoryModel
      val newDS = makeDatasetFromDirModel(oldDirModel)
      val mainDS = myMainQueryDataset;
      RepoOper.replaceDatasetElements(mainDS, newDS)
    } else {
      myMainQueryDataset = makeMainQueryDataset
      val dirModel = getDirectoryModel
      updateDatasetFromDirModel(dirModel, myMainQueryDataset)
    }
  }

  def getClassLoaderList(clList: java.util.List[ClassLoader] = null): java.util.List[ClassLoader] = {
    ClassLoaderUtils.getFileResourceClassLoaders(ClassLoaderUtils.ALL_RESOURCE_CLASSLOADER_TYPES, fileModelCLs, clList)
  }
  override def toString(): String = {
    if (myDebugNameToStr != null) return myDebugNameToStr;
    val dm = getDirectoryModel
    var dmstr = "noDirModel";
    if (dm != null) dmstr = "dir=" + dm.size
    if (isLoadingLocked || !isLoadingStarted) {
      getClass.getSimpleName + "[name=" + myDebugNameToStr + "  " + dmstr + " setof=Loading...]";
    } else {
      getClass.getSimpleName + "[name=" + myDebugNameToStr + "  " + dmstr + " setof=" + RepoOper.setOF(getMainQueryDataset.listNames) + "]";
    }
  }

  def reloadSingleModelByName(modelName: String) = {
    val repo = myRepoSpecForRef.makeRepo
    val oldDataset = getMainQueryDataset
    val myPNewMainQueryDataset = repo.getMainQueryDataset
    getLogger.info("START: Trying to do reloading of model named.. " + modelName)
    RepoOper.replaceSingleDatasetModel(oldDataset, myPNewMainQueryDataset, modelName)
    getLogger.info("START: Trying to do reloading of model named.. " + modelName)
  }

  def ensureUpdated() {
    if (!isUpdatedFromDirModel) {
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
        if (dirModelSize == 0) {
          if (myMainQueryDataset != null) {
            RepoOper.clearAll(myMainQueryDataset)
            this.isUpdatedFromDirModel = true;
          }
        } else {
          if (this.myMainQueryDataset == null) {
            this.isUpdatedFromDirModel = true;
            this.myMainQueryDataset = makeDatasetFromDirModel(dirModel)
          } else {
            this.isUpdatedFromDirModel = true;
            reloadAllModelsFromDir
          }
          var newModelSize = dirModel.size;
          if (newModelSize != dirModelSize && dirModel == getDirectoryModel) {
            getLogger.warn("OnmiRepo Dir.size changed durring load!  " + dirModelSize + " -> " + newModelSize)
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

  def makeDatasetFromDirModel(dirModel: Model): Dataset = {
    val mainDset: Dataset = RepoDatasetFactory.createPrivateMem
    updateDatasetFromDirModel(dirModel, mainDset)
    mainDset
  }

  def includeDirModel(dirModel: Model) {
    if (myMainQueryDataset == null) {
      myMainQueryDataset = getMainQueryDataset
    }
    myDirectoryModel.add(dirModel)
    updateDatasetFromDirModel(dirModel, myMainQueryDataset)
  }

  def updateDatasetFromDirModel(dirModel: Model, mainDset: Dataset) {
    val repoLoader = getRepoLoader
    repoLoader.setSynchronous(false)
    val fileModelCLs: java.util.List[ClassLoader] = getClassLoaderList(this.fileModelCLs)
    DirectRepo.updateDatasetFromDirModel(repoLoader, mainDset, dirModel, fileModelCLs)
    // not done until the last task completes
    repoLoader.setSynchronous(true)
  }

  def loadSheetModelsIntoMainDataset(): Unit = {}
  def loadDerivedModelsIntoMainDataset(fileModelCLs: java.util.List[ClassLoader]): Unit = {}
  def loadFileModelsIntoMainDataset(fileModelCLs: java.util.List[ClassLoader]): Unit = {}
  val myDirectoryModel: Model = directoryModel;
  override def getDirectoryModel: Model = myDirectoryModel;

  override def makeMainQueryDataset(): Dataset = {
    val ds = super.makeMainQueryDataset;
    ds.addNamedModel("#dir", myDirectoryModel)
    ds
  }

  override def makeResourceForURI(uri: String): Resource = {
    getModel.createResource(uri)
  }
  override def makeResourceForQName(qName: String): Resource = {
    val expandedURI = getModel.expandPrefix(qName)
    makeResourceForURI(expandedURI)
  }
  override def makeResourceForIdent(id: Ident): Resource = {
    val uri: String = id.getAbsUriString
    makeResourceForURI(uri)
  }
  override def makeTypedLiteral(litString: String, dtype: RDFDatatype): Literal = {
    getModel.createTypedLiteral(litString, dtype);
  }
  override def makeStringLiteral(litString: String): Literal = {
    makeTypedLiteral(litString, XSDDatatype.XSDstring);
  }

  override def makeIdentForQName(qName: String): Ident = makeJenaResourceItemForQName(qName)
  override def makeItemForQName(qName: String): Item = makeJenaResourceItemForQName(qName)

  private def makeJenaResourceItemForQName(qName: String): JenaResourceItem = {
    val res = makeResourceForQName(qName)
    new JenaResourceItem(res)
  }
  override def makeIdentForURI(uri: String): Ident = {
    val res = makeResourceForURI(uri)
    new JenaResourceItem(res);
  }
  override def makeItemForIdent(id: Ident): Item = {
    id match {
      case itemAlready: Item => itemAlready
      case otherIdent: Ident => {
        val res = makeResourceForIdent(otherIdent);
        new JenaResourceItem(res)
      }
    }
  }

  private def resolveIndirectQueryText(queryResIB: InitialBinding): String = {
    val qInitBinding: QuerySolutionMap = queryResIB.getQSMap

    val parsedQQ: Query = myQueryResQuery

    val possSoln: Option[QuerySolution] = findSingleQuerySolution(parsedQQ, qInitBinding);

    val qText: String = if (possSoln.isDefined) {
      val qSoln = possSoln.get;
      val qtxt_Lit: Literal = qSoln.getLiteral("queryTxt");
      qtxt_Lit.getString()
    } else "";

    qText
  }
  override def queryIndirectForAllSolutions(qSrcGraphIdent: Ident, queryIdent: Ident, qInitBinding: QuerySolution): java.util.List[QuerySolution] = {
    val qText = resolveIndirectQueryText(qSrcGraphIdent, queryIdent)
    checkQueryText(qText, qSrcGraphIdent, queryIdent, true)
    queryDirectForAllSolutions(qText, qInitBinding)
  }

  override def queryIndirectForAllSolutions(qSrcGraphQN: String, queryQN: String, qInitBinding: QuerySolution): java.util.List[QuerySolution] = {

    val qText = resolveIndirectQueryText(qSrcGraphQN, queryQN)
    checkQueryText(qText, qSrcGraphQN, queryQN, true)
    queryDirectForAllSolutions(qText, qInitBinding)
  }
  override def checkQueryText(qText: String, qSrcGraphQN: Object, queryQN: Object, showStackTrace: Boolean): Unit = {
    if (qText == null || qText.length == 0) {
      val msg = "Unable to find Query Called " + queryQN + " in Model " + qSrcGraphQN;
      val rte = new RuntimeException(msg);
      logError(msg);
      if (showStackTrace) {
        rte.printStackTrace
        // maybe we should just throw now?
        // rather then letting subsequent calls fail with obscure EOFs
      }
    }
  }
  override def queryDirectForAllSolutions(qText: String, qInitBinding: QuerySolution): java.util.List[QuerySolution] = {
    import scala.collection.immutable.StringOps
    val qTextOps = new StringOps(qText);
    val fixedQTxt = qTextOps.replaceAll("!!", "?") // Remove this as soon as app code is updated
    val parsedQ = parseQueryText(fixedQTxt);

    findAllSolutions(parsedQ, qInitBinding);
  }
  private def parseQueryResolutionQuery: Query = {
    val parsedQQ = parseQueryText(QUERY_QUERY_TEXT);
    logDebug("Parsed QueryResolutionQuery as: " + parsedQQ)
    parsedQQ
  }

}

