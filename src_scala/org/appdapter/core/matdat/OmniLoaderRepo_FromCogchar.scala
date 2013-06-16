/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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

//import org.appdapter.api.registry.VerySimpleRegistry;
//import org.appdapter.osgi.registry.RegistryServiceFuncs;

import org.appdapter.core.name.{ Ident, FreeIdent }

import org.appdapter.api.trigger.{
  Box,
  BoxContext,
  BoxImpl,
  MutableBox,
  Trigger,
  TriggerImpl
};

import org.appdapter.core.name.{ Ident, FreeIdent }
import org.appdapter.core.store.{ Repo, InitialBinding }
import org.appdapter.help.repo.{ RepoClient, RepoClientImpl, InitialBindingImpl } 
import org.appdapter.impl.store.{ FancyRepo };
//import org.appdapter.core.matdat.{ SheetRepo }
import com.hp.hpl.jena.query.{ QuerySolution } // Query, QueryFactory, QueryExecution, QueryExecutionFactory, , QuerySolutionMap, Syntax};
import com.hp.hpl.jena.rdf.model.{ Model }
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.boot.ClassLoaderUtils;
import org.osgi.framework.BundleContext;

import org.appdapter.gui.demo.{ DemoNavigatorCtrl };
import org.appdapter.core.store.RepoOper;

import org.appdapter.scafun.{ Boxy, GoFish, FullBox, FullTrigger }

import org.appdapter.core.log.{ BasicDebugger, Loggable };

import org.appdapter.core.name.{ Ident, FreeIdent };
import org.appdapter.core.item.{ Item };

import scala.collection.mutable.HashMap;


//import org.cogchar.blob.emit.{ RepoFabric, RepoSpec, OnlineSheetRepoSpec, DatabaseRepoSpec, FabricBox, RepoClientTester };
import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.bind.rdf.jena.model.{ JenaFileManagerUtils };
import scala.collection.JavaConversions;

import org.appdapter.core.name.Ident

import org.appdapter.core.log.{ BasicDebugger }
import org.appdapter.core.name.{ Ident, FreeIdent }
import org.appdapter.core.store.{ Repo, InitialBinding }
import org.appdapter.help.repo.{ RepoClient, RepoClientImpl, InitialBindingImpl }
import org.appdapter.impl.store.{ FancyRepo }
//import org.appdapter.core.matdat.{ SheetRepo, GoogSheetRepo, XLSXSheetRepo }
import com.hp.hpl.jena.rdf.model.{
  Model,
  Statement,
  Resource,
  Property,
  Literal,
  RDFNode,
  ModelFactory,
  InfModel
}
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
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory

/**
 * Takes a directory model and uses Goog, Xlsx, Pipeline,CSV,.ttl,rdf sources and loads them
 */
class OmniLoaderSpec_UnusedForNow(var myDebugName: String, var dirModelURI: String)
  extends RepoSpec {

  override def makeRepo(): OmniLoaderRepo = {
    if (dirModelURI.startsWith("xlsx:")) {
      var stuff = dirModelURI.substring(5)
      var v3: Array[String] = stuff.split('/')
      //(new OfflineXlsSheetRepoSpec(v3[0],v3[1],v3[2])).makeRepo;     
      null

    } else if (dirModelURI.startsWith("goog:")) {
      var stuff = dirModelURI.substring(5)
      var v3: Array[String] = stuff.split('/')
      //(new OfflineXlsSheetRepoSpec(v3[0],v3[1].toInt,v3[2].toInt)).makeRepo;     
      null
    } else {
      null
    }
  }
}

class OmniLoaderRepo_FromCogchar(var myRepoSpec: RepoSpec, var myDebugName: String, directoryModel: Model, fmcls: java.util.List[ClassLoader])
  extends XLSXSheetRepo(directoryModel: Model, fmcls: java.util.List[ClassLoader])
  with RepoOper.Reloadable {

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
    getClass.getSimpleName + "[name=" + myDebugName + ", dirModelSize=" + dm.size() + " mainDsetGraphNames=" + RepoOper.setOF(getMainQueryDataset.listNames) + "]";
  }

  var isUpdated = false

  override def loadSheetModelsIntoMainDataset() = {
    ensureUpdated;
  }
  def ensureUpdated() = {
    //OmniLoaderRepo.synchronized 
    {
      this.synchronized {
        if (!this.isUpdated) {
          traceHere("Loading OnmiRepo to make UpToDate")
          this.isUpdated = true;
          traceHere("Loading Sheet Models")
          var dirModelSize = getDirectoryModel().size;
          // efectivelty emulates super.loadSheetModelsIntoMainDataset();
          loadGoogSheetModelsIntoMainDataset();
          loadSheetModelsIntoMainDatasetCsvFiles(fileModelCLs);
          loadSheetModelsIntoMainDatasetXlsWorkBooks;
          traceHere("Loading File Models")
          loadFileModelsIntoMainDataset(fileModelCLs)
          traceHere("Loading Derived Models")
          loadDerivedModelsIntoMainDataset();
          var newModelSize = getDirectoryModel().size;
          if (newModelSize != dirModelSize) {
            traceHere("OnmiRepo Dir.size changed!  " + dirModelSize + " -> " + newModelSize)
          }
        } else {
          //traceHere("OnmiRepo was UpToDate")
        }
        //if (OmniLoaderRepo.popupWackamole) addToWhackmole()
      }
    }
  }

  var wasAdded = false;/*
  def addToWhackmole() = {

    var doIt = false;
    this.synchronized {
      if (!wasAdded) {
        wasAdded = true;
        doIt = true;
      }
    }
    if (doIt) {
      nonfabricWay()
    }
  }

  def nonfabricWay() = {
    traceHere("NonfabricWay");
    val wm = OmniLoaderRepo.ensureWhackamole()
    wm.addRepo("OmniRepo ", this);
    // val bstf = new BootstrapTriggerFactory;
    // var bc = fb.attachTrigger(new ReloadTrigger())

    traceHere("Done");
  }

  def fabricWay() = {
    traceHere("Make RepoFabric");
    val rf = new RepoFabric();
    traceHere("Make FabricBox");
    val fb = new FabricBox(rf);
    fb.setShortLabel("Short Label")
    traceHere("Ensure Whackamole");
    // Add this as an "entry" in the RepoFabric 
    traceHere("Add to Entry");
    rf.addEntry(new SimplistSpec(this))
    traceHere("Resync");
    //fb.resyncChildrenToTree
    val wm = OmniLoaderRepo.ensureWhackamole()
    traceHere("Add to box " + wm);
    wm.addBoxToRoot(fb, false);
    // val bstf = new BootstrapTriggerFactory;
    // var bc = fb.attachTrigger(new ReloadTrigger())

    traceHere("Done");
  }*/

  def traceHere(str: String) {
    getLogger().debug(str)
    // println("*!*!*! OmniLoaderRepo: " + str)
  }
  override def getDirectoryModel(): Model = {
    //myNewDirectoryModel;
    super.getDirectoryModel
  }

  override def getMainQueryDataset(): Dataset = {
    ensureUpdated;
    super.getMainQueryDataset();
  }

  def loadDerivedModelsIntoMainDataset() = {
    val mainDset: DataSource = getMainQueryDataset().asInstanceOf[DataSource];

    val nsJavaMap: java.util.Map[String, String] = myDirectoryModel.getNsPrefixMap()

    val msqText = """
			select ?model 
				{
					?model a ccrt:PipelineModel;
				}
		"""

    val msRset = QueryHelper.execModelQueryWithPrefixHelp(myDirectoryModel, msqText);
    import scala.collection.JavaConversions._;
    while (msRset.hasNext()) {
      val qSoln: QuerySolution = msRset.next();

      //val repoRes : Resource = qSoln.getResource("repo");
      val modelRes = qSoln.get("model");
      val modelName = modelRes.asResource().asNode().getURI

      val dbgArray = Array[Object](modelRes, modelName);
      loadPipeline(modelName)
      getLogger.warn("DerivedModelsIntoMainDataset modelRes={}, modelName={}", dbgArray);
      //val msRset = QueryHelper.execModelQueryWithPrefixHelp(mainDset.getNamedModel(modelName), msqText2);

      // DerivedGraphSpecReader.queryDerivedGraphSpecs(getRepoClient,DerivedGraphSpecReader.PIPELINE_QUERY_QN,modelName)
    }
  }

  def loadPipeline(pplnGraphQN: String) = {

    val mainDset: DataSource = getMainQueryDataset().asInstanceOf[DataSource];
    val rc = new RepoClientImpl(this, RepoSpecDefaultNames.DFLT_TGT_GRAPH_SPARQL_VAR, RepoSpecDefaultNames.DFLT_QRY_SRC_GRAPH_QN)
    val pqs = new PipelineQuerySpec(RepoSpecDefaultNames.PIPE_ATTR_QQN, RepoSpecDefaultNames.PIPE_SOURCE_QQN, pplnGraphQN);
    val dgSpecSet: Set[DerivedGraphSpec] = DerivedGraphSpecReader.queryDerivedGraphSpecs(rc, pqs);

    for (dgSpec <- dgSpecSet) {
      val derivedModelProvider = dgSpec.makeDerivedModelProvider(this);
      val derivedModel = derivedModelProvider.getModel()
      mainDset.replaceNamedModel(pplnGraphQN, derivedModel)
    }
  }
  class SimplistSpec(val wd: Repo.WithDirectory) extends RepoSpec {
    override def makeRepo(): Repo.WithDirectory = {
      wd;
    }
    override def toString(): String = {
      "SimplestSpec[" + wd + "]";
    }
  }
}

object ReloadingRepoTest {
  def main(args: Array[String]) = {
    val fileResModelCLs: java.util.List[ClassLoader] =
      ClassLoaderUtils.getFileResourceClassLoaders(null, ClassLoaderUtils.ALL_RESOURCE_CLASSLOADER_TYPES);
    val repoSpec = new OnlineSheetRepoSpec(RepoSpecDefaultNames.BMC_SHEET_KEY, RepoSpecDefaultNames.BMC_NAMESPACE_SHEET_NUM, RepoSpecDefaultNames.BMC_DIRECTORY_SHEET_NUM, fileResModelCLs);
    val repo: OmniLoaderRepo = repoSpec.makeRepo.asInstanceOf[OmniLoaderRepo];
  //  repo.addToWhackmole();
    java.lang.Thread.sleep(60000000);
  }
}
object OmniLoaderRepo_FromCogchar {

  var popupWackamole = false;
/*
  def ensureWhackamole(): RepoNavigator = {
    OmniLoaderRepo.synchronized {
      if (!isWhackamoleStarted) {
        isWhackamoleStarted = true;
        theWhackamole = RepoNavigator.makeRepoNavigatorCtrl(new Array[String](0))
        theWhackamole.launchFrame("Whackamolopy " + theWhackamole);
        theWhackamole
      }
    }
    theWhackamole;
  }*/
  var isWhackamoleStarted = false;
  //var theWhackamole: RepoNavigator = null;
}
