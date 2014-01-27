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
import org.appdapter.impl.store.QueryHelper
import com.hp.hpl.jena.query.Dataset
import com.hp.hpl.jena.query.QuerySolution
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.Resource
import org.appdapter.core.store.RepoOper

/**
 * @author Douglas R. Miles <www.logicmoo.org>
 *
 * This is a entire repo serialized to a .ttl file used by the GUI
 *  Each model is separated via
 *
 *     @base http://repo/model_1
 *         <a1> <b1> <c1> .
 *     @base http://repo/model_1
 *         <a2> <b2> <c2> .
 *
 * This is a DatasetFileRepo Loader being an InstallableRepoReader can be additionly used
 *    in all the legal places that can provide a single file path
 */

/// this is a registerable loader
class PipelineSnapLoader extends InstallableRepoReader {
  override def getContainerType() = "cc:PipelineModel"
  override def getSheetType() = "cc:UnionModel"
  override def isDerivedLoader() = true
  override def loadModelsIntoTargetDataset(repo: Repo.WithDirectory, mainDset: Dataset, dirModel: Model, fileModelCLs: java.util.List[ClassLoader]) {
    PipelineSnapLoader.loadSheetModelsIntoTargetDataset(repo, mainDset, dirModel, fileModelCLs)
  }
}

//================ LOADER

object PipelineSnapLoader extends BasicDebugger {

  def loadSheetModelsIntoTargetDataset(repo: Repo.WithDirectory, mainDset: Dataset, myDirectoryModel: Model, fileModelCLs: java.util.List[ClassLoader]) = {

    val nsJavaMap: java.util.Map[String, String] = myDirectoryModel.getNsPrefixMap()

    val msqText = """
			select ?model
				{
					?model a cc:PipelineModel.
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
      getLogger.warn("PipelineSnapLoader modelRes={}, modelName={}", dbgArray);

      val msqText2 = """
			select ?model ?targetmodel
				{
					?targetmodel ccrt:sourceModel ?model.
				}
		"""
      val pipelineModel = mainDset.getNamedModel(modelName);
      val msRset2 = QueryHelper.execModelQueryWithPrefixHelp(pipelineModel, msqText2);
      while (msRset2.hasNext()) {
        val qSoln: QuerySolution = msRset2.next();

        //val repoRes : Resource = qSoln.getResource("repo");
        val modelRes2 = qSoln.get("model");
        val targetmodelRes2 = qSoln.get("targetmodel");
        val modelName2 = modelRes2.asResource().asNode().getURI
        val targetmodelName2 = targetmodelRes2.asResource().asNode().getURI

        val dbgArray2 = Array[Object](modelRes2, targetmodelRes2);
        getLogger.warn("PipelineSnapLoader modelRes={}, targetmodelRes={}", dbgArray2);

        RepoOper.addUnionModel(mainDset, modelName2, targetmodelName2);

        //val msRset = QueryHelper.execModelQueryWithPrefixHelp(mainDset.getNamedModel(modelName), msqText2);

        // DerivedGraphSpecReader.queryDerivedGraphSpecs(getRepoClient,DerivedGraphSpecReader.PIPELINE_QUERY_QN,modelName)
      }

    }

  }
  /*
  def loadPipeline(pplnGraphQN: String, repo: Repo.WithDirectory, mainDset: Dataset, myDirectoryModel: Model, fileModelCLs: java.util.List[ClassLoader]) = {

    val mainDset: Dataset = repo.getMainQueryDataset().asInstanceOf[Dataset];

    val rc = new org.appdapter.help.repo.RepoClientImpl(repo, RepoSpecDefaultNames.DFLT_TGT_GRAPH_SPARQL_VAR, OmniLoaderRepoTest.QUERY_SOURCE_GRAPH_QN_FOR_TEST)
    val pqs = new PipelineQuerySpec(OmniLoaderRepoTest.PIPELINE_QUERY_QN_FOR_TEST,
      OmniLoaderRepoTest.PIPELINE_GRAPH_QN_FOR_TEST, pplnGraphQN);
    val solList = DerivedGraphSpecReader.queryDerivedGraphSpecs(rc, pqs);

    for (solC <- solList) {
      val pipeSpec = solC
      val model = pipeSpec.makeDerivedModelProvider(repo).getModel
      mainDset.replaceNamedModel(pplnGraphQN, model)
    }
  }
  */
}