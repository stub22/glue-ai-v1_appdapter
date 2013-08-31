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
        // dirModelLoaders.add(new PipelineRepoLoader());
        dirModelLoaders.add(new DerivedRepoLoader());

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
    repoLoader.setSynchronous(true)
    val mainDset: Dataset = getMainQueryDataset().asInstanceOf[Dataset];
    var clListG = this.getClassLoaderList(clList)
    if (myDirectoryModel.size == 0) return ;
    FileModelRepoLoader.loadSheetModelsIntoTargetDataset(this, mainDset, myDirectoryModel, clListG)
    DerivedRepoLoader.loadSheetModelsIntoTargetDataset(this, mainDset, myDirectoryModel, clListG)
  }

  def loadSheetModelsIntoMainDataset() {
    ensureUpdatedPrivate();
  }

  final def loadFileModelsIntoMainDataset(clList: java.util.List[ClassLoader]) = {
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
    val dm = getDirectoryModel();
    if (isLoadingLocked) {
      getClass.getSimpleName + "[name=" + myDebugNameToStr + ", dir=" + dm.size() + " setof=Loading...]";
    } else {
      getClass.getSimpleName + "[name=" + myDebugNameToStr + ", dir=" + dm.size() + " setof=" + RepoOper.setOF(getMainQueryDataset.listNames) + "]";
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
    val mainDset: Dataset = getMainQueryDataset().asInstanceOf[Dataset];
    val dirModel = getDirectoryModel;
    val fileModelCLs: java.util.List[ClassLoader] = this.getClassLoaderList(this.fileModelCLs);
    val dirModelLoaders: java.util.List[InstallableRepoReader] = SheetRepo.getDirModelLoaders();
    val dirModelLoaderIter = dirModelLoaders.listIterator();
    repoLoader.setSynchronous(false)
    while (dirModelLoaderIter.hasNext()) {
      val irr = dirModelLoaderIter.next();
      trace("Loading ... " + irr.getContainerType + "/" + irr.getSheetType);
      irr.loadModelsIntoTargetDataset(this, mainDset, dirModel, fileModelCLs);
    }
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

/**
 * @author Stu B. <www.texpedient.com>
 * @author Douglas R. Miles <www.logicmoo.org>
 *
 * This is a DirModel Loader it contains static method for loading FileRepos
 */

/// this is a registerable loader
class FileModelRepoLoader extends InstallableRepoReader {
  override def getContainerType() = "ccrt:FileRepo"
  override def getSheetType() = "ccrt:FileModel"
  override def loadModelsIntoTargetDataset(repo: Repo.WithDirectory, mainDset: Dataset, dirModel: Model, fileModelCLs: java.util.List[ClassLoader]) {
    FileModelRepoLoader.loadSheetModelsIntoTargetDataset(repo, mainDset, dirModel, fileModelCLs)
  }
}

object FileModelRepoLoader extends BasicDebugger {

  def loadSheetModelsIntoTargetDataset(repo: Repo.WithDirectory, mainDset: Dataset,
    myDirectoryModel: Model, clList: java.util.List[ClassLoader]): Unit = {

    if (myDirectoryModel.size == 0) return
    val nsJavaMap: java.util.Map[String, String] = myDirectoryModel.getNsPrefixMap()

    val msqText = """
			select ?repo ?repoPath ?model ?modelPath ?unionOrReplace
				{
					?repo  a ccrt:FileRepo; ccrt:sourcePath ?repoPath.
					?model a ccrt:FileModel; ccrt:sourcePath ?modelPath; ccrt:repo ?repo.
      				OPTIONAL { ?model a ?unionOrReplace. FILTER (?unionOrReplace = ccrt:UnionModel) }
				}
		"""

    val msRset = QueryHelper.execModelQueryWithPrefixHelp(myDirectoryModel, msqText);
    import scala.collection.JavaConversions._;
    while (msRset.hasNext()) {
      val qSoln: QuerySolution = msRset.next();

      val repoRes: Resource = qSoln.getResource("repo");
      val modelRes: Resource = qSoln.getResource("model");
      val unionOrReplaceRes: Resource = qSoln.getResource("unionOrReplace");
      val repoPath_Lit: Literal = qSoln.getLiteral("repoPath")
      val modelPath_Lit: Literal = qSoln.getLiteral("modelPath")
      val dbgArray = Array[Object](repoRes, repoPath_Lit, modelRes, modelPath_Lit);
      getLogger.warn("repo={}, repoPath={}, model={}, modelPath={}", dbgArray);

      val rPath = repoPath_Lit.getString();
      val mPath = modelPath_Lit.getString();

      getLogger().warn("Ready to read from [{}] / [{}]", Array[Object](rPath, mPath));
      val rdfURL = rPath + mPath;

      repo.addLoadTask(rdfURL, new Runnable() {
        def run() {
          try {
            val graphURI = modelRes.getURI();
            val fileModel = readModelSheetFromURL(rdfURL, nsJavaMap, clList);
            getLogger.warn("Read fileModel: {}", fileModel)
            PipelineRepoLoader.replaceOrUnion(mainDset, unionOrReplaceRes, graphURI, fileModel);
          } catch {
            case except: Throwable => getLogger().error("Caught error loading file {}", Array[Object](rdfURL, except))
          }
        }
      })

    }
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
      } else FileModelRepoLoader.readModelSheetFromURL(rdfURL, nsJavaMap, fileModelCLs);
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
    val shRepo = SpecialRepoLoader.makeSheetRepo(repoSpec, dirModel, fileModelCLs)
    // Load the rest of the repo's initial *sheet* models, as instructed by the directory.
    shRepo.loadSheetModelsIntoMainDataset()
    // Load the rest of the repo's initial *file/resource* models, as instructed by the directory.
    shRepo.loadDerivedModelsIntoMainDataset(fileModelCLs)
    shRepo
  }

  // END NEXT DIFF

}
