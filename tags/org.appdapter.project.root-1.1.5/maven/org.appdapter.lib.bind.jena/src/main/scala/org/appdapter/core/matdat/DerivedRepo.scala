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

import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.store.Repo
import org.appdapter.core.store.dataset.SpecialRepoLoader
import org.appdapter.impl.store.QueryHelper

import com.hp.hpl.jena.query.Dataset
import com.hp.hpl.jena.query.QuerySolution
import com.hp.hpl.jena.rdf.model.Model

/**
 * @author LogicMOO <www.logicmoo.org>
 *
 */

// FIXME:  The srcRepo should really not be given to the RepoSpec, because it
// is not serializable specData.
/*

class DerivedRepoSpec(val myDGSpecs: Set[DerivedGraphSpec_UNUSED], val mySrcRepo: Repo.WithDirectory) extends RepoSpec {
  override def toString(): String = {
    "DerivedRepoSpec[pipeSpecs= " + myDGSpecs + "]";
  }
  override def makeRepo(): DerivedRepo = {
    val emptyDirModel = RepoDatasetFactory.createPrivateMemModel
    // TODO:  Copy over prefix-abbreviations from the source repo (need to confirm the line below does this correctly)
    emptyDirModel.setNsPrefixes(mySrcRepo.getDirectoryModel.getNsPrefixMap);
    val derivedRepo = new DerivedRepo(emptyDirModel, this)
    for (dgSpec <- myDGSpecs) {
      val derivedModelProvider = dgSpec.makeDerivedModelProvider(mySrcRepo);
      val derivedModel = derivedModelProvider.getModel()
      derivedRepo.replaceNamedModel(dgSpec.myTargetGraphTR, derivedModel)
    }
    derivedRepo
  }
}

// @TODO to be moved to org.appdapter.lib.core
class DerivedRepo(emptyDirModel: Model, val myRepoSpec: DerivedRepoSpec) extends DirectRepo(emptyDirModel) with RepoOper.ReloadableDataset {

  def reloadAllModels() = {
    //myRepoSpec.makeRepo
    myRepoSpec.makeRepo
  }

  def reloadSingleModel(modelName: String) = {
    val repo = myRepoSpec.makeRepo();
    val oldDataset = getMainQueryDataset();
    val myPNewMainQueryDataset = repo.getMainQueryDataset();
    getLogger.info("START: Trying to do reloading of model named.. " + modelName)
    RepoOper.replaceSingleDatasetModel(oldDataset, myPNewMainQueryDataset, modelName)
    getLogger.info("START: Trying to do reloading of model named.. " + modelName)
  }
}

/// this is a registerable loader
class PipelineModelLoader extends InstallableRepoReader {
  override def getContainerType() = "cc:PipelineModel"
  override def getSheetType() = "ccrt:UnionModel"
  override def isDerivedLoader() = true
  override def loadModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset, dirModel: Model, fileModelCLs: java.util.List[ClassLoader]) {
    PipelineModelLoader.loadSheetModelsIntoTargetDataset(repo, mainDset, dirModel, fileModelCLs)
  }
}

//================ LOADER
object PipelineModelLoader extends BasicDebugger {

  def loadSheetModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset, myDirectoryModel: Model, fileModelCLs: java.util.List[ClassLoader]) = {

    val nsJavaMap: java.util.Map[String, String] = myDirectoryModel.getNsPrefixMap()

    val msqText = """
			select ?model
				{
					?model a cc:PipelineModel;
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
      loadPipeline(modelName, repo, mainDset, myDirectoryModel, fileModelCLs);
      getLogger.warn("DerivedModelsIntoMainDataset modelRes={}, modelName={}", dbgArray);
      //val msRset = QueryHelper.execModelQueryWithPrefixHelp(mainDset.getNamedModel(modelName), msqText2);

      // DerivedGraphSpecReader.queryDerivedGraphSpecs(getRepoClient,DerivedGraphSpecReader.PIPELINE_QUERY_QN,modelName)
    }
  }
  
  

  def loadPipeline(pplnGraphQN: String, repo: Repo.WithDirectory, mainDset: Dataset, myDirectoryModel: Model, fileModelCLs: java.util.List[ClassLoader]) = {

    val mainDset: Dataset = repo.getMainQueryDataset().asInstanceOf[Dataset];
    val rc = new org.appdapter.help.repo.RepoClientImpl(repo, RepoSpecDefaultNames.DFLT_TGT_GRAPH_SPARQL_VAR, OmniLoaderRepoTest.QUERY_SOURCE_GRAPH_QN_FOR_TEST)
    val solList = DerivedGraphSpecReader.queryDerivedGraphSpecs(rc, new PipelineQuerySpec(OmniLoaderRepoTest.PIPELINE_QUERY_QN_FOR_TEST,
      OmniLoaderRepoTest.PIPELINE_GRAPH_QN_FOR_TEST, pplnGraphQN));

    for (solC <- solList) {
      val pipeSpec = solC
      val model = pipeSpec.makeDerivedModelProvider(repo).getModel
      mainDset.replaceNamedModel(pplnGraphQN, model)
    }
  }
}*/

/// this is a registerable loader
class DerivedModelLoader extends InstallableRepoReader {
  override def getExt = null
  override def getContainerType() = "ccrt:DerivedModel"
  override def getSheetType() = "ccrt:UnionModel"
  override def isDerivedLoader() = true
  override def loadModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset, dirModel: Model, fileModelCLs: java.util.List[ClassLoader]) {
    DerivedModelLoader.loadSheetModelsIntoTargetDataset(repo, mainDset, dirModel, fileModelCLs)
  }
}

object DerivedModelLoader extends BasicDebugger {

  def loadSheetModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset, myDirectoryModel: Model, fileModelCLs: java.util.List[ClassLoader]) = {

    val nsJavaMap: java.util.Map[String, String] = myDirectoryModel.getNsPrefixMap()

    val msqText = """
	select ?model ?unionOrReplace ?container ?key ?num
		{
			?container a ccrt:GoogSheetRepo; ccrt:key ?key.
			?model a ccrt:DerivedModel; a ccrt:GoogSheet; ccrt:sheetNumber ?num; ccrt:repo ?container.
			OPTIONAL { ?model a ?unionOrReplace. FILTER (?unionOrReplace = ccrt:UnionModel) }		
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
      getLogger.warn("DerivedModelsIntoMainDataset modelRes={}, modelName={}", dbgArray);
      PipelineSnapLoader.loadPipelineSheets(repo, mainDset, myDirectoryModel, modelName, fileModelCLs)
      PipelineSnapLoader.loadPipelineSheetTyp(repo, mainDset, myDirectoryModel, modelName, fileModelCLs)
    }
  }
}
/*
class SimplistRepoSpec(val wd: Repo.WithDirectory) extends RepoSpec {
  override def makeRepo(): Repo.WithDirectory = {
    wd;
  }
  override def toString(): String = {
    "SimplestSpec[" + wd + "]";
  }
}
*/
