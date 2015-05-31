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

package org.appdapter.xload.fancy

import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.store.RepoOper
import org.appdapter.core.loader.{SpecialRepoLoader}
import org.appdapter.fancy.query.QueryHelper
import com.hp.hpl.jena.query.{ Dataset, QuerySolution }
import com.hp.hpl.jena.rdf.model.Model
import org.appdapter.core.store.dataset.RepoDatasetFactory
import org.appdapter.bind.rdf.jena.query.SPARQL_Utils

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
 * This is a DatasetFileRepo Loader being an InstallableRepoLoader can be additionly used
 *    in all the legal places that can provide a single file path
 */
/// this is a registerable loader
/// this is a registerable loader
class DerivedModelLoader extends InstallableRepoLoader {
  override def getExt = null
  override def getContainerType() = "ccrt:DerivedModel"
  override def getSheetType() = "ccrt:UnionModel"
  override def isDerivedLoader() = true
  override def loadModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset, dirModel: Model, 
								fileModelCLs: java.util.List[ClassLoader], optPrefixURL : String) {
    //DerivedModelLoader.loadSheetModelsIntoTargetDataset(repo, mainDset, dirModel, fileModelCLs)
  }
}
//================ LOADER

object DerivedModelLoader extends BasicDebugger {

  final private def loadSheetModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset, myDirectoryModel: Model, fileModelCLs: java.util.List[ClassLoader]) = {

    val nsJavaMap: java.util.Map[String, String] = myDirectoryModel.getNsPrefixMap()

    // we use container so that we find only repo members
    val msqText = """
	select ?model ?modelName ?unionOrReplace
		{
			?model a ccrt:DerivedModel;
            OPTIONAL { ?model dphys:hasGraphNameUri ?modelName }
  	        OPTIONAL { ?model owl:sameAs ?modelName }

		}     
		"""

    val unionAllModel = RepoOper.unionAll(mainDset, myDirectoryModel, mainDset.getDefaultModel());

    val msRset = QueryHelper.execModelQueryWithPrefixHelp(unionAllModel, msqText);
    import scala.collection.JavaConversions._;
    while (msRset.hasNext()) {
      val qSoln: QuerySolution = msRset.next();

      //val repoRes : Resource = qSoln.getResource("repo");
      val modelRes = SPARQL_Utils.nonBnodeValue(qSoln,"model","modelName");
      val modelName = modelRes.asResource().asNode().getURI

      val dbgArray = Array[Object](modelRes, modelName);
      getLogger.debug("DerivedModelsIntoMainDataset modelRes={}, modelName={}", dbgArray);
      PipelineSnapLoader.loadPipelineSheets(repo, mainDset, myDirectoryModel, fileModelCLs)
      PipelineSnapLoader.loadPipelineSheetTyp(repo, mainDset, myDirectoryModel, fileModelCLs)
    }
  }
}




 