/*
 *  Copyright 2014 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.xload.fancy

import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.store.RepoOper
import org.appdapter.core.loader.{SpecialRepoLoader}
import org.appdapter.fancy.query.QueryHelper
import com.hp.hpl.jena.query.{ Dataset, QuerySolution }
import com.hp.hpl.jena.rdf.model.Model
import org.appdapter.core.store.dataset.RepoDatasetFactory
import org.appdapter.bind.rdf.jena.query.SPARQL_Utils


/// this is a registerable loader
class PipelineSnapLoader extends InstallableRepoLoader {
  override def getExt = null;
  override def getContainerType() = "cc:PipelineModel"
  override def getSheetType() = "cc:UnionModel"
  override def isDerivedLoader() = true
  override def loadModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset, dirModel: Model, 
									fileModelCLs: java.util.List[ClassLoader], optPrefixURL : String) {
    PipelineSnapLoader.loadSheetModelsIntoTargetDataset(repo, mainDset, dirModel, fileModelCLs)
  }
}

//================ LOADER

/*
 *  "Shared between Loader instances"
 *  Static/private/final to higher Likelyhood of JIT 
 */
object PipelineSnapLoader extends BasicDebugger {

  private final def loadSheetModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset, myDirectoryModel: Model, fileModelCLs: java.util.List[ClassLoader]) = {

    //val nsJavaMap: java.util.Map[String, String] = myDirectoryModel.getNsPrefixMap()
    /*
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
      getLogger.debug("PipelinnapLoader modelR={}, modelName={}", dbgArray);
 */
    //      val pipelineModel = mainDset.getNamedModel(modelName);
    //  }
    val unionAllModel = RepoOper.unionAll(mainDset, myDirectoryModel, mainDset.getDefaultModel());

    loadPipelineSheets(repo, mainDset, myDirectoryModel, fileModelCLs);
    loadPipelineSheetTyp(repo, mainDset, myDirectoryModel, fileModelCLs);

  }

  private[fancy] final def loadPipelineSheets(repo: SpecialRepoLoader, mainDset: Dataset, unionAllModel: Model, fileModelCLs: java.util.List[ClassLoader]) = {

    //val pipelineModel = RepoDatasetFactory.findOrCreateModel(mainDset, modelName)

    val msqText = """
			select ?srcmodel ?targetmodel ?srcmodelName ?targetmodelName
				{
					?targetmodel <urn:ftd:cogchar.org:2012:runtime#sourceModel> ?srcmodel.
                    OPTIONAL { ?targetmodel dphys:hasGraphNameUri ?targetmodelName }
          	        OPTIONAL { ?targetmodel owl:sameAs ?targetmodelName }
                    OPTIONAL { ?srcmodel dphys:hasGraphNameUri ?srcmodelName }
          	        OPTIONAL { ?srcmodel owl:sameAs ?srcmodelName }

				}
		"""

    val msRset = QueryHelper.execModelQueryWithPrefixHelp(unionAllModel, msqText);

    while (msRset.hasNext()) {
      val qSoln: QuerySolution = msRset.next();

      //val repoR : Rource = qSoln.getRource("repo");
      val srcmodelR = SPARQL_Utils.nonBnodeValue(qSoln,"srcmodel","srcmodelName");
      val targetmodelR = SPARQL_Utils.nonBnodeValue(qSoln,"targetmodel","targetmodelName");
      val srcmodelName = srcmodelR.asResource().asNode().getURI
      val targetmodelName = targetmodelR.asResource().asNode().getURI

      val dbgArray = Array[Object](srcmodelR, targetmodelR);
      getLogger.info("PipelineSnapLoader srcmodelR={}, targetmodelR={}", dbgArray);

      RepoOper.addUnionModel(mainDset, srcmodelName, targetmodelName);

    }
  }

  private[fancy] final def loadPipelineSheetTyp(repo: SpecialRepoLoader, mainDset: Dataset, unionAllModel: Model, fileModelCLs: java.util.List[ClassLoader]): Unit = {

    val msqText = """
			select ?targetmodel ?srcmodel
				{
					?targetmodel <urn:ftd:cogchar.org:2012:runtime#sourceModelType> ?modeltype.
    			    ?srcmodel a ?modeltype.
				}
		"""
    val msRset = QueryHelper.execModelQueryWithPrefixHelp(unionAllModel, msqText);

    while (msRset.hasNext()) {
      val qSoln: QuerySolution = msRset.next();

      //val repoR : Rource = qSoln.getRource("repo");
      val targetmodelR = qSoln.get("targetmodel");
      val srcmodelR = qSoln.get("srcmodel");
      val srcmodel = srcmodelR.asResource().asNode().getURI
      val targetmodelName = targetmodelR.asResource().asNode().getURI

      val dbgArray = Array[Object](srcmodelR, targetmodelR);
      getLogger.info("PipelineSnapLoader srcmodelR={}, targetmodelR={}", dbgArray);

      RepoOper.addUnionModel(mainDset, srcmodel, targetmodelName);

    }
  }
  /*
  private final def loadPipelineSheetTypeInstanc(repo: SpecialRepoLoader, mainDset: Dataset, unionAllModel: Model, targetmodelName: String, modeltype: String, fileModelCLs: java.util.List[ClassLoader]) = {

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
      getLogger.info("loadPipelineSheetTypeInstanc modelR={}, targetmodelR={}", dbgArray);
      RepoOper.addUnionModel(mainDset, model, targetmodelName);

    }
  }
*/
}