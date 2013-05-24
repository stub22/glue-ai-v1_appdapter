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

package org.appdapter.core.matdat

import com.hp.hpl.jena.query.DataSource
import com.hp.hpl.jena.query.Dataset
import com.hp.hpl.jena.query.QuerySolution
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.name.Ident
import org.appdapter.core.store.{ Repo, SpecialRepoLoader }
import com.hp.hpl.jena.query.Dataset
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.sdb.Store
import java.util.List
import java.util.Set
import org.appdapter.core.name.Ident
import com.hp.hpl.jena.query.QuerySolution
import org.appdapter.help.repo.InitialBindingImpl
import org.appdapter.impl.store.DirectRepo
import scala.collection.JavaConversions.asScalaSet
import org.appdapter.impl.store.QueryHelper
import org.appdapter.help.repo.RepoClientImpl
import org.appdapter.core.store.{ RepoSpec, RepoOper, RepoClient, RepoSpecJava, Repo, InitialBinding }
import org.appdapter.help.repo. { RepoClientImpl, RepoClientScala, InitialBindingImpl}

/**
 * @author LogicMOO <www.logicmoo.org>
 */

// FIXME:  The srcRepo should really not be given to the RepoSpec, because it
// is not serializable specData.
class DerivedRepoSpec(val myDGSpecs: Set[DerivedGraphSpec], val mySrcRepo: Repo.WithDirectory) extends RepoSpecScala {
  override def toString(): String = {
    "PipelineRepoSpec[pipeSpecs= " + myDGSpecs + "]";
  }
  override def makeRepo(): DerivedRepo = {
    val emptyDirModel = ModelFactory.createDefaultModel();
    // TODO:  Copy over prefix-abbreviations from the source repo (need to confirm the line below does this correctly)
    emptyDirModel.setNsPrefixes(mySrcRepo.getDirectoryModel.getNsPrefixMap);
    val derivedRepo = new DerivedRepo(emptyDirModel, this)
    for (dgSpec <- myDGSpecs) {
      val derivedModel = dgSpec.makeDerivedModel(mySrcRepo)
      derivedRepo.replaceNamedModel(dgSpec.myTargetID, derivedModel)
    }
    derivedRepo
  }
}

// @TODO to be moved to org.appdapter.lib.core
class DerivedRepo(emptyDirModel: Model, val myRepoSpec: DerivedRepoSpec) extends DirectRepo(emptyDirModel, true) with RepoOper.Reloadable {

  def reloadAllModels() = {
    //myRepoSpec.makeRepo
    myRepoSpec.makeRepo
  }

  def loadSheetModelsIntoMainDataset() {
    
  }
  
  def reloadSingleModel(modelName: String) = {
    val repo = myRepoSpec.makeRepo();
    val oldDataset = getMainQueryDataset();
    val myPNewMainQueryDataset = repo.getMainQueryDataset();
    getLogger.info("START: Trying to do reloading of model named.. " + modelName)
    RepoOper.replaceDatasetElements(oldDataset, myPNewMainQueryDataset, modelName)
    getLogger.info("START: Trying to do reloading of model named.. " + modelName)
  }

  // TODO:  Move this method up to Appdapter.DirectRepo
  def replaceNamedModel(modelID: Ident, jenaModel: Model) {
    val repoDset: Dataset = getMainQueryDataset
    val repoDsource: DataSource = repoDset.asInstanceOf[DataSource];
    repoDsource.replaceNamedModel(modelID.getAbsUriString, jenaModel);
  }
}

class DerivedRepoLoader extends SpecialRepoLoader {

}
object DerivedRepoLoader {

  def loadSheetModelsIntoTargetDataset(repo: SheetRepo, mainDset: DataSource, myDirectoryModel: Model, fileModelCLs: java.util.List[ClassLoader]) = {

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
      loadPipeline(modelName, repo, mainDset, myDirectoryModel, fileModelCLs);
      repo.warn("DerivedModelsIntoMainDataset modelRes={}, modelName={}", dbgArray);
      //val msRset = QueryHelper.execModelQueryWithPrefixHelp(mainDset.getNamedModel(modelName), msqText2);

      // DerivedGraphSpecReader.queryDerivedGraphSpecs(getRepoClient,DerivedGraphSpecReader.PIPELINE_QUERY_QN,modelName)
    }
  }

  def loadPipeline(pplnGraphQN: String, repo: SheetRepo, mainDset: DataSource, myDirectoryModel: Model, fileModelCLs: java.util.List[ClassLoader]) = {

    val mainDset: DataSource = repo.getMainQueryDataset().asInstanceOf[DataSource];
    val rc = new RepoClientImpl(repo, RepoSpecDefaultNames.DFLT_TGT_GRAPH_SPARQL_VAR, OmniLoaderRepoTest.QUERY_SOURCE_GRAPH_QN)
    val solList = DerivedGraphSpecReader.queryDerivedGraphSpecs(rc, OmniLoaderRepoTest.PIPELINE_QUERY_QN, pplnGraphQN);

    for (solC <- solList) {
      val pipeSpec = solC
      val model = pipeSpec.makeDerivedModel(repo)
      mainDset.replaceNamedModel(pplnGraphQN, model)
    }
  }
}