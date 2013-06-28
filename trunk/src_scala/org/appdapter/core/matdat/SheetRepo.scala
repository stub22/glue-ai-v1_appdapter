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

import com.hp.hpl.jena.rdf.model.{ Model, Statement, Resource, Property, Literal, RDFNode, ModelFactory, InfModel }
import com.hp.hpl.jena.query.{ Query, QueryFactory, QueryExecution, QueryExecutionFactory, QuerySolution, QuerySolutionMap, Syntax }
import com.hp.hpl.jena.query.{ Dataset, DatasetFactory, DataSource }
import com.hp.hpl.jena.query.{ ResultSet, ResultSetFormatter, ResultSetRewindable, ResultSetFactory }
import com.hp.hpl.jena.ontology.{ OntProperty, ObjectProperty, DatatypeProperty }
import com.hp.hpl.jena.datatypes.{ RDFDatatype, TypeMapper }
import com.hp.hpl.jena.datatypes.xsd.{ XSDDatatype }
import com.hp.hpl.jena.shared.{ PrefixMapping }
import com.hp.hpl.jena.rdf.listeners.{ ObjectListener }
import org.appdapter.core.log.BasicDebugger
import org.appdapter.bind.rdf.jena.model.{ ModelStuff, JenaModelUtils, JenaFileManagerUtils }
import org.appdapter.core.store.{ Repo, BasicQueryProcessorImpl, BasicRepoImpl, QueryProcessor }
import org.appdapter.impl.store.{ DirectRepo, QueryHelper, ResourceResolver }
import org.appdapter.help.repo.InitialBindingImpl
import org.appdapter.core.boot.ClassLoaderUtils
import org.appdapter.core.store.RepoOper
//import org.appdapter.api.trigger.RepoOper
import org.appdapter.core.name.Ident
/**
 * @author Stu B. <www.texpedient.com>
 *
 * We implement a DirectRepo with some sheet loading
 */

object SheetRepo extends BasicDebugger {

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

  final def loadDerivedModelsIntoMainDataset(clList: java.util.List[ClassLoader]) = {
    val mainDset: DataSource = getMainQueryDataset().asInstanceOf[DataSource];
    var clListG = this.getClassLoaderList(clList)
    FileModelRepoLoader.loadSheetModelsIntoTargetDataset(this, mainDset, myDirectoryModel, clListG)
    DerivedRepoLoader.loadSheetModelsIntoTargetDataset(this, mainDset, myDirectoryModel, clListG)
  }

  def loadSheetModelsIntoMainDataset() {
    ensureUpdatedPrivate();
  }

  final def loadFileModelsIntoMainDataset(clList: java.util.List[ClassLoader]) = {
    loadDerivedModelsIntoMainDataset(clList);
  }

  var myBasePath: String = null;
  var myIdent: Ident = null;

  def reloadAllModelsFromDir() = {
    val repo = myRepoSpecForRef.makeRepo();
    val oldDataset = getMainQueryDataset();
    val oldDirModel = getDirectoryModel();
    val myNewDirectoryModel = repo.getDirectoryModel();
    val myPNewMainQueryDataset = repo.getMainQueryDataset();
    RepoOper.replaceModelElements(oldDirModel, myNewDirectoryModel)
    RepoOper.replaceDatasetElements(oldDataset, myPNewMainQueryDataset)
    //reloadMainDataset();
  }

  def getClassLoaderList(clList: java.util.List[ClassLoader] = null): java.util.List[ClassLoader] = {
      ClassLoaderUtils.getFileResourceClassLoaders(ClassLoaderUtils.ALL_RESOURCE_CLASSLOADER_TYPES, fileModelCLs, clList);
  }
  override def toString(): String = {
    val dm = getDirectoryModel();
    getClass.getSimpleName + "[name=" + myDebugNameToStr + ", dir=" + dm.size() + " setof=" + RepoOper.setOF(getMainQueryDataset.listNames) + "]";
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

  
  var isUpdatedFromDir = false
  

  def ensureUpdatedPrivate() = {
    //OmniLoaderRepo.synchronized 
    {
      this.synchronized {
        if (!this.isUpdatedFromDir) {
          trace("Loading OnmiRepo to make UpToDate")
          this.isUpdatedFromDir = true;
          var dirModelSize = getDirectoryModel().size;
          val mainDset: DataSource = getMainQueryDataset().asInstanceOf[DataSource];
          val dirModel = getDirectoryModel;
          val fileModelCLs: java.util.List[ClassLoader] = this.getClassLoaderList(this.fileModelCLs);
          trace("Loading Goog Models")
          GoogSheetRepo.loadSheetModelsIntoTargetDataset(this, mainDset, dirModel, fileModelCLs);
          // efectivelty emulates super.loadSheetModelsIntoMainDataset();
          trace("Loading XLSX Models")
          XLSXSheetRepoLoader.loadSheetModelsIntoTargetDataset(this, mainDset, dirModel, fileModelCLs);
          trace("Loading CSV Models")
          CsvFilesSheetLoader.loadSheetModelsIntoTargetDataset(this, mainDset, dirModel, fileModelCLs);
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

  @deprecated
  def loadGoogSheetModelsIntoMainDataset() {
    if (true) warn("Shjould be using appdapter 1.1.1 soon")
    val mainDset: DataSource = getMainQueryDataset().asInstanceOf[DataSource];
    val dirModel = getDirectoryModel;
    val fileModelCLs: java.util.List[ClassLoader] = this.getClassLoaderList(this.fileModelCLs);
    GoogSheetRepo.loadSheetModelsIntoTargetDataset(this, mainDset, dirModel, fileModelCLs);
  }

  @deprecated
  def loadSheetModelsIntoMainDatasetCsvFiles(clList: java.util.List[ClassLoader]) {
    if (true) warn("Shjould be using appdapter 1.1.1 soon")
    val mainDset: DataSource = getMainQueryDataset().asInstanceOf[DataSource];
    val dirModel = getDirectoryModel;
    val fileModelCLs: java.util.List[ClassLoader] = this.getClassLoaderList(this.fileModelCLs);
    CsvFilesSheetLoader.loadSheetModelsIntoTargetDataset(this, mainDset, dirModel, fileModelCLs);
  }

  @deprecated
  def loadSheetModelsIntoMainDatasetXlsWorkBooks() {
    if (true) warn("Shjould be using appdapter 1.1.1 soon")
    val mainDset: DataSource = getMainQueryDataset().asInstanceOf[DataSource];
    val dirModel = getDirectoryModel;
    val fileModelCLs: java.util.List[ClassLoader] = this.getClassLoaderList(this.fileModelCLs);
    XLSXSheetRepoLoader.loadSheetModelsIntoTargetDataset(this, mainDset, dirModel, fileModelCLs);
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
class FileModelRepoLoader extends InstallableRepoReader {
  override def getContainerType() = "ccrt:FileRepo"
  override def getSheetType() = "ccrt:FileModel"
  override def loadModelsIntoTargetDataset(repo: Repo.WithDirectory, mainDset: DataSource, dirModel: Model, fileModelCLs: java.util.List[ClassLoader]) {
    FileModelRepoLoader.loadSheetModelsIntoTargetDataset(repo, mainDset, dirModel, fileModelCLs)
  }
}

object FileModelRepoLoader extends BasicDebugger {

  def loadSheetModelsIntoTargetDataset(repo: Repo.WithDirectory, mainDset: DataSource,
    myDirectoryModel: Model, clList: java.util.List[ClassLoader]) = {

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
      getLogger.warn("repo={}, repoPath={}, model={}, modelPath={}", dbgArray);

      val rPath = repoPath_Lit.getString();
      val mPath = modelPath_Lit.getString();

      getLogger().warn("Ready to read from [{}] / [{}]", rPath, mPath);
      val rdfURL = rPath + mPath;

      import com.hp.hpl.jena.util.FileManager;
      val jenaFileMgr = JenaFileManagerUtils.getDefaultJenaFM
      JenaFileManagerUtils.ensureClassLoadersRegisteredWithJenaFM(jenaFileMgr, clList)
      try {
        val fileModel = jenaFileMgr.loadModel(rdfURL);

        getLogger.warn("Read fileModel: {}", fileModel)
        val graphURI = modelRes.getURI();
        mainDset.replaceNamedModel(graphURI, fileModel)
      } catch {
        case except => getLogger().error("Caught error loading file {}", rdfURL, except)
      }
    }
  }
// END NEXT DIFF

}
