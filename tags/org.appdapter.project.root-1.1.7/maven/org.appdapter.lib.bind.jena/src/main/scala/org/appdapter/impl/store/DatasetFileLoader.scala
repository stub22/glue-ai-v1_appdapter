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

package org.appdapter.impl.store

import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.repo.InstallableRepoReader
import org.appdapter.core.store.RepoOper
import org.appdapter.core.store.dataset.SpecialRepoLoader

import com.hp.hpl.jena.query.{Dataset, QuerySolution}
import com.hp.hpl.jena.rdf.model.{Literal, Model, Resource}

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
class DatasetFileRepo extends InstallableRepoReader {
  override def getExt = null;
  override def getContainerType() = "ccrt:DatasetFileRepo"
  override def getSheetType() = "ccrt:DatasetFileRepo"
  override def loadModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset, dirModel: Model, fileModelCLs: java.util.List[ClassLoader]) {
    DatasetFileRepo.loadSheetModelsIntoTargetDataset(repo, mainDset, dirModel, fileModelCLs)
  }
}

object DatasetFileRepo extends BasicDebugger {

  private def loadSheetModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset,
    myDirectoryModel: Model, clList: java.util.List[ClassLoader]): Unit = {

    if (myDirectoryModel.size == 0) return
    val nsJavaMap: java.util.Map[String, String] = myDirectoryModel.getNsPrefixMap()

    val msqText = """
			select ?repo ?repoPath ?qrymodel ?dirmodel ?unionOrReplace
				{
					?repo  a ccrt:DatasetFileRepo; ccrt:sourcePath ?repoPath.
					?qrymodel a ccrt:QueryTxtModel; ccrt:repo ?repo.
					?dirmodel a ccrt:DirectoryModel; ccrt:repo ?repo.
      				OPTIONAL { ?model a ?unionOrReplace. FILTER (?unionOrReplace = ccrt:UnionModel) }
				}
      """

    val msRset = QueryHelper.execModelQueryWithPrefixHelp(myDirectoryModel, msqText);
    import scala.collection.JavaConversions._;
    while (msRset.hasNext()) {
      val qSoln: QuerySolution = msRset.next();

      val repoRes: Resource = qSoln.getResource("repo");
      val qrymodelRes: Resource = qSoln.getResource("qrymodel");
      val dirmodelRes: Resource = qSoln.getResource("dirmodel");
      val unionOrReplaceRes: Resource = qSoln.getResource("unionOrReplace");
      val repoPath_Lit: Literal = qSoln.getLiteral("repoPath")
      val dbgArray = Array[Object](repoRes, repoPath_Lit, dirmodelRes);
      getLogger.debug("repo={}, repoPath={}, defaultmodel={}", dbgArray);

      val rPath = repoPath_Lit.getString();

      getLogger.debug("Ready to read from [{}] / [{}]", Array[Object](rPath, dirmodelRes));

      // @todo set the Query and DirModels

      repo.addLoadTask(rPath + "/" + dirmodelRes, new Runnable() {
        def run() {
          try {
            val graphURI = dirmodelRes.getURI();
            getLogger.debug("Read fileModel: {}", rPath)
            RepoOper.readDatasetFromURL(rPath, mainDset, unionOrReplaceRes)
          } catch {
            case except: Throwable => getLogger.error("Caught error loading file {}", Array[Object](dirmodelRes, except))
          }
        }
      })
      // do the 2nd pass over the loaded dir now
      // FancyRepoLoader.makeDirectoryRepo(repo.getNAmedModel());

    }
  }
}
