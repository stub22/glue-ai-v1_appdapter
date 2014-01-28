/*
 *  Copyright 01 by The Appdapter Project (www.appdapter.org).
 *
 *  Licensed under the Apache License, Version .0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licens/LICENSE-.0
 *
 *  Unls required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTI OR CONDITIONS OF ANY KIND, either exprs or implied.
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
import com.hp.hpl.jena.rdf.model.ModelFactory

/**
 * @author Douglas R. Miles <www.logicmoo.org>
 *
 * This is a entire repo serialized to a .ttl file used by the GUI
 *  Each model is separated via
 *
 *     @base http://repo/model_1
 *         <a1> <b1> <c1> .
 *     @base http://repo/model_1
 *         <a> <b> <c> .
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

      val modelR = qSoln.get("model");
      val modelName = modelR.asResource().asNode().getURI

      val dbgArray = Array[Object](modelR, modelName);
      getLogger.warn("PipelinnapLoader modelR={}, modelName={}", dbgArray);

      val pipelineModel = mainDset.getNamedModel(modelName);

      loadPipelineSheets(repo, mainDset, myDirectoryModel, modelName, fileModelCLs);
      loadPipelineSheetTyp(repo, mainDset, myDirectoryModel, modelName, fileModelCLs);
    }

  }

  def loadPipelineSheets(repo: Repo.WithDirectory, mainDset: Dataset, myDirectoryModel: Model, modelName: String, fileModelCLs: java.util.List[ClassLoader]) = {

    val pipelineModel = mainDset.getNamedModel(modelName);

    val msqText = """
			select ?model ?targetmodel
				{
					?targetmodel <urn:ftd:cogchar.org:2012:runtime#sourceModel> ?model.
				}
		"""

    val unionAllModel = RepoOper.unionAll(mainDset, myDirectoryModel, mainDset.getDefaultModel());

    val msRset = QueryHelper.execModelQueryWithPrefixHelp(pipelineModel, msqText);

    while (msRset.hasNext()) {
      val qSoln: QuerySolution = msRset.next();

      //val repoR : Rource = qSoln.getRource("repo");
      val modelR = qSoln.get("model");
      val targetmodelR = qSoln.get("targetmodel");
      val modelName = modelR.asResource().asNode().getURI
      val targetmodelName = targetmodelR.asResource().asNode().getURI

      val dbgArray = Array[Object](modelR, targetmodelR);
      getLogger.warn("PipelinnapLoader modelR={}, targetmodelR={}", dbgArray);

      RepoOper.addUnionModel(mainDset, modelName, targetmodelName);

    }
  }

  def loadPipelineSheetTyp(repo: Repo.WithDirectory, mainDset: Dataset, unionAllModel: Model, modelName: String, fileModelCLs: java.util.List[ClassLoader]) = {

    val pipelineModel = mainDset.getNamedModel(modelName);

    val msqText = """
			select ?modeltype ?targetmodel
				{
					?targetmodel <urn:ftd:cogchar.org:2012:runtime#sourceModelType> ?modeltype;
				}
		"""
    val msRset = QueryHelper.execModelQueryWithPrefixHelp(pipelineModel, msqText);

    while (msRset.hasNext()) {
      val qSoln: QuerySolution = msRset.next();

      //val repoR : Rource = qSoln.getRource("repo");
      val modeltypeR = qSoln.get("modeltype");
      val targetmodelR = qSoln.get("targetmodel");
      val modeltype = modeltypeR.asResource().asNode().getURI
      val targetmodelName = targetmodelR.asResource().asNode().getURI

      val dbgArray = Array[Object](modeltypeR, targetmodelR);
      getLogger.warn("PipelinnapLoader modeltype={}, targetmodelR={}", dbgArray);

      loadPipelineSheetTypeInstanc(repo, mainDset, unionAllModel, targetmodelName, modeltype, fileModelCLs);

    }
  }

  def loadPipelineSheetTypeInstanc(repo: Repo.WithDirectory, mainDset: Dataset, unionAllModel: Model, targetmodelName: String, modeltype: String, fileModelCLs: java.util.List[ClassLoader]) = {

    val size = unionAllModel.size();

    val msqText = """
			select ?model
				{
					?model a <""" + modeltype + """>;
				}
		"""
    val msRset = QueryHelper.execModelQueryWithPrefixHelp(unionAllModel, msqText);
    while (msRset.hasNext()) {
      val qSoln: QuerySolution = msRset.next();

      val modelR = qSoln.get("model");
      val model = modelR.asResource().asNode().getURI

      val dbgArray = Array[Object](modelR, targetmodelName);
      getLogger.warn("PipelinnapLoader modelR={}, targetmodelR={}", dbgArray);
      RepoOper.addUnionModel(mainDset, model, targetmodelName);

    }
  }

}