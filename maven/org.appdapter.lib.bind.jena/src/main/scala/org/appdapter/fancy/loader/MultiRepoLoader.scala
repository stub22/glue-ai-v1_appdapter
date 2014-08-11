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

package org.appdapter.fancy.loader
import org.appdapter.core.name.{Ident, FreeIdent}
import org.appdapter.core.store.{RepoOper}
import org.appdapter.core.loader.SpecialRepoLoader
import org.appdapter.fancy.rspec.{RepoSpec, MultiRepoSpec, URLRepoSpec}
import org.appdapter.fancy.query.{QueryHelper}

import com.hp.hpl.jena.query.{ Dataset, QuerySolution }
import com.hp.hpl.jena.rdf.model.{ Literal, Model, Resource }
/**
 * @author Stu B. <www.texpedient.com>
 */

class MultiRepoLoader extends InstallableRepoLoader {
  override def makeRepoSpec(path: String, args: Array[String], cLs: java.util.List[ClassLoader]): RepoSpec = {
    new MultiRepoSpec(path, cLs);
  }
  override def getExt = MultiRepoLoader.PROTO
  override def getContainerType() = "ccrt:MultiRepo"
  override def getSheetType() = "ccrt:DirectoryModelSheet"
  override def loadModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset, dirModel: Model, fileModelCLs: java.util.List[ClassLoader]) {
    MultiRepoLoader.loadSheetModelsIntoTargetDataset(repo, mainDset, dirModel, fileModelCLs)
  }
}
/*

class MultiRepo(store: Store, val myDirGraphID: Ident)
  extends BasicStoredMutableRepoImpl(store) with Repo.Mutable with Repo.Stored {

  formatRepoIfNeeded();

  override def getDirectoryModel: Model = getNamedModel(myDirGraphID);

}
*/

object MultiRepoLoader extends org.appdapter.core.log.BasicDebugger {

  val PROTO = "multi"
  /*def makeMultiRepo(repoConfResPath: String, optCL: ClassLoader, dirGraphID: Ident): MultiRepo = {
    val s: Store = SdbStoreFactory.connectSdbStoreFromResPath(repoConfResPath, optCL);
    new MultiRepo(s, dirGraphID);
  }*/

  /////////////////////////////////////////
  /// Make a Repo.WithDirectory
  /////////////////////////////////////////
  /* def makeSdbDirectoryRepo(repoConfResPath: String, optCL: ClassLoader, dirGraphID: Ident): Repo.WithDirectory = {
    // Read the namespaces and directory sheets into a single directory model.
    FancyRepoLoader.makeRepoWithDirectory(null, readDirectoryModelFromMulti(repoConfResPath, optCL, dirGraphID));
  }
*/
  /////////////////////////////////////////
  /// Read dir model
  /////////////////////////////////////////
  /*  def readDirectoryModelFromMulti(repoConfResPath: String, optCL: ClassLoader, dirGraphID: Ident): Model = {
    // Read the single directory sheets into a single directory model.
    val s: Store = SdbStoreFactory.connectSdbStoreFromResPath(repoConfResPath, optCL);
    new MultiRepo(s, dirGraphID).getDirectoryModel
  }
*/
  /////////////////////////////////////////
  /// Read sheet models
  /////////////////////////////////////////
  def loadSheetModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset,
    myDirectoryModel: Model, clList: java.util.List[ClassLoader]): Unit = {

    if (myDirectoryModel.size == 0) return
    val nsJavaMap: java.util.Map[String, String] = myDirectoryModel.getNsPrefixMap()

    val msqText = """
			select ?dirModel ?modelPath ?unionOrReplace
				{
					?dirModel  a ccrt:DirectoryModel; ccrt:sourcePath ?modelPath.
      				OPTIONAL { ?dirModel a ?unionOrReplace. FILTER (?unionOrReplace = ccrt:UnionModel)}
    			}
		"""

    val msRset = QueryHelper.execModelQueryWithPrefixHelp(myDirectoryModel, msqText);
    import scala.collection.JavaConversions._;
    while (msRset.hasNext()) {
      val qSoln: QuerySolution = msRset.next();

      val dirModel = qSoln.getResource("dirModel");
      val modelPath = qSoln.getLiteral("modelPath");
      val unionOrReplace: Resource = qSoln.getResource("unionOrReplace");
      val dbgArray = Array[Object](dirModel, modelPath, unionOrReplace);
      getLogger.debug("dirModel={}, modelPath={}, model={}", dbgArray);

      val configPath = modelPath.getString();
      val modelURI = dirModel.getURI();

      getLogger.debug("Ready to read Multi from [{}] / [{}]", Array[Object](configPath, modelURI));

      val modelIdent = new FreeIdent(modelURI);
      repo.addLoadTask(configPath + "/" + modelURI, new Runnable() {
        def run() {
          try {
            val graphURI = modelURI
            val otherRepo = (new URLRepoSpec(configPath, clList)).getOrMakeRepo
            val src = otherRepo.getMainQueryDataset
            RepoOper.addOrReplaceDatasetElements(mainDset, src, unionOrReplace);
            //val MultiModel = FancyRepoLoader.loadDetectedFileSheetRepo(configPath, null, modelIdent).getNamedModel(modelIdent);
            // getLogger.warn("Read MultiModel: {}", MultiModel)
            //FancyRepoLoader.replaceOrUnion(mainDset, unionOrReplaceRes, graphURI, MultiModel);
          } catch {
            case except: Throwable => getLogger.error("Caught error loading Multi [{}] / [{}]", Array[Object](configPath, modelURI))
          }
        }
      })

    }
  }

}
