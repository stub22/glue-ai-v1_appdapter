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

import com.hp.hpl.jena.query.DataSource
import com.hp.hpl.jena.query.QuerySolution
import com.hp.hpl.jena.rdf.model.Literal
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.Resource
import org.appdapter.core.matdat.RepoSpec
import org.appdapter.bind.rdf.jena.model.JenaFileManagerUtils
import org.appdapter.core.boot.ClassLoaderUtils
import org.appdapter.gui.demo.RepoOper
import org.appdapter.impl.store.{ DirectRepo, QueryHelper }
import org.appdapter.core.name.Ident

/**
 * @author Stu B. <www.texpedient.com>
 *
 * We implement a CSV (spreadsheet) backed Appdapter "repo" (read-only, but reloadable from updated source data).
 */

abstract class SheetRepo(directoryModel: Model, var fileModelCLs: java.util.List[ClassLoader]) extends DirectRepo(directoryModel, true) {

  /**  For All Subclasses    */
  final def loadDerivedModelsIntoMainDataset(clList: java.util.List[ClassLoader]) = {
    val mainDset: DataSource = getMainQueryDataset().asInstanceOf[DataSource];
    var clListG = this.getClassLoaderList(clList)
    FileModelRepoLoader.loadSheetModelsIntoTargetDataset(this, mainDset, myDirectoryModel, clListG)
    DerivedRepoLoader.loadSheetModelsIntoTargetDataset(this, mainDset, myDirectoryModel, clListG)
  }

  def loadSheetModelsIntoMainDataset() {
    ensureUpdated();
  }

  var myRepoSpec: RepoSpec = null;
  var myDebugName: String = null;
  var myBasePath: String = null;
  var myIdent: Ident = null;

  def reloadAllModels() = {
    val repo = myRepoSpec.makeRepo();
    val oldDataset = getMainQueryDataset();
    val oldDirModel = getDirectoryModel();
    val myNewDirectoryModel = repo.getDirectoryModel();
    val myPNewMainQueryDataset = repo.getMainQueryDataset();
    RepoOper.replaceModelElements(oldDirModel, myNewDirectoryModel)
    RepoOper.replaceDatasetElements(oldDataset, myPNewMainQueryDataset)
    //reloadMainDataset();
  }

  def reloadSingleModel(modelName: String) = {
    val repo = myRepoSpec.makeRepo();
    val oldDataset = getMainQueryDataset();
    val myPNewMainQueryDataset = repo.getMainQueryDataset();
    getLogger.info("START: Trying to do reloading of model named.. " + modelName)
    RepoOper.replaceDatasetElements(oldDataset, myPNewMainQueryDataset, modelName)
    getLogger.info("START: Trying to do reloading of model named.. " + modelName)
  }

  override def toString(): String = {
    val dm = getDirectoryModel();
    getClass.getSimpleName + "[name=" + myDebugName + ", dir=" + dm.size() + " setof=" + RepoOper.setOF(getMainQueryDataset.listNames) + "]";
  }

  var isUpdated = false

  def getClassLoaderList(clList: java.util.List[ClassLoader]): java.util.List[ClassLoader] = {
    if (clList != null) clList else if (fileModelCLs != null) fileModelCLs else
      ClassLoaderUtils.getFileResourceClassLoaders(ClassLoaderUtils.ALL_RESOURCE_CLASSLOADER_TYPES);
  }

  def ensureUpdated() = {
    //OmniLoaderRepo.synchronized 
    {
      this.synchronized {
        if (!this.isUpdated) {
          trace("Loading OnmiRepo to make UpToDate")
          this.isUpdated = true;
          var dirModelSize = getDirectoryModel().size;
          val mainDset: DataSource = getMainQueryDataset().asInstanceOf[DataSource];
          val dirModel = getDirectoryModel;
          val fileModelCLs: java.util.List[ClassLoader] = this.getClassLoaderList(this.fileModelCLs);
          trace("Loading Goog Models")
          GoogSheetRepoLoader.loadSheetModelsIntoTargetDataset(this, mainDset, dirModel, fileModelCLs);
          // efectivelty emulates super.loadSheetModelsIntoMainDataset();
          trace("Loading XLSX Models")
          XLSXSheetRepoLoader.loadSheetModelsIntoTargetDataset(this, mainDset, dirModel, fileModelCLs);
          trace("Loading CSV Models")
          CsvFilesSheetRepoLoader.loadSheetModelsIntoTargetDataset(this, mainDset, dirModel, fileModelCLs);
          trace("Loading File Models")
          FileModelRepoLoader.loadSheetModelsIntoTargetDataset(this, mainDset, dirModel, fileModelCLs);
          trace("Loading Derived Models")
          DerivedRepoLoader.loadSheetModelsIntoTargetDataset(this, mainDset, dirModel, fileModelCLs);
          var newModelSize = getDirectoryModel().size;
          if (newModelSize != dirModelSize) {
            trace("OnmiRepo Dir.size changed!  " + dirModelSize + " -> " + newModelSize)
          }
        } else {
          //traceHere("OnmiRepo was UpToDate")
        }
      }
    }
  }
  def trace(fmt: String, args: Any*) = {
    try {
      getLogger.warn(fmt, args);
    } catch {
      case except =>
    }
  }
  def warn(fmt: String, args: Any*) = {
    try {
      getLogger.warn(fmt, args);
    } catch {
      case except =>
    }
  }
  def error(fmt: String, args: Any*) = {
    try {
      getLogger.error(fmt, args);
    } catch {
      case except =>
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
class FileModelRepoLoader {
  def getContainerType(): String = {
    return "ccrt:FileRepo";
  }
  def getSheetType(): String = {
    return "ccrt:FileModel";
  }
  def loadModelsIntoTargetDataset(repo: SheetRepo, mainDset: DataSource, dirModel: Model, fileModelCLs: java.util.List[ClassLoader]) {
    FileModelRepoLoader.loadSheetModelsIntoTargetDataset(repo, mainDset, dirModel, fileModelCLs)
  }
}

object FileModelRepoLoader extends org.appdapter.core.store.SpecialRepoLoader {
  def loadSheetModelsIntoTargetDataset(repo: SheetRepo, mainDset: DataSource, myDirectoryModel: Model, clList: java.util.List[ClassLoader]) = {

    val nsJavaMap: java.util.Map[String, String] = myDirectoryModel.getNsPrefixMap()

    val msqText = """
			select ?repo ?repoPath ?model ?modelPath
				{
					?repo  a ccrt:FileRepo; ccrt:sourcePath ?repoPath.
					?model a ccrt:FileModel; ccrt:sourcePath ?modelPath; ccrt:repo ?repo.
				}
		"""

    val msRset = QueryHelper.execModelQueryWithPrefixHelp(myDirectoryModel, msqText);
    import scala.collection.JavaConversions._;
    while (msRset.hasNext()) {
      val qSoln: QuerySolution = msRset.next();

      val repoRes: Resource = qSoln.getResource("repo");
      val modelRes: Resource = qSoln.getResource("model");
      val repoPath_Lit: Literal = qSoln.getLiteral("repoPath")
      val modelPath_Lit: Literal = qSoln.getLiteral("modelPath")
      val dbgArray = Array[Object](repoRes, repoPath_Lit, modelRes, modelPath_Lit);
      repo.warn("repo={}, repoPath={}, model={}, modelPath={}", dbgArray);
      val rPath = repoPath_Lit.getString();
      val mPath = modelPath_Lit.getString();

      repo.warn("Ready to read from [{}] / [{}]", rPath, mPath);
      val rdfURL = rPath + mPath;

      import com.hp.hpl.jena.util.FileManager;
      val jenaFileMgr = JenaFileManagerUtils.getDefaultJenaFM
      JenaFileManagerUtils.ensureClassLoadersRegisteredWithJenaFM(jenaFileMgr, clList)
      try {
        val fileModel = jenaFileMgr.loadModel(rdfURL);

        repo.warn("Read fileModel: {}", fileModel)
        val graphURI = modelRes.getURI();
        mainDset.replaceNamedModel(graphURI, fileModel)
      } catch {
        case except => repo.error("Caught error loading file {}", rdfURL, except)
      }
    }
  }
}
