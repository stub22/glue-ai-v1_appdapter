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

import java.util.ArrayList

import org.appdapter.bind.rdf.jena.model.JenaFileManagerUtils
import org.appdapter.core.boot.ClassLoaderUtils
import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.name.Ident
import org.appdapter.core.store.{ Repo, RepoOper }
import org.appdapter.impl.store.{ DirectRepo, QueryHelper }
import org.appdapter.core.store.{ FileStreamUtils }

import com.hp.hpl.jena.query.{ Dataset, QuerySolution }
import com.hp.hpl.jena.rdf.model.{ Literal, Model, Resource }
/**
 * @author Stu B. <www.texpedient.com>
 * @author Douglas R. Miles <www.logicmoo.org>
 *
 * This is a FileModel Loader it contains the InstallableRepoReader and Loader class with static methods for loading FileRepos
 */

/// this is a registerable loader
class FileModelRepoLoader extends InstallableRepoReader {
  override def getContainerType() = "ccrt:FileRepo"
  override def getSheetType() = "ccrt:FileModel"
  override def loadModelsIntoTargetDataset(repo: Repo.WithDirectory, mainDset: Dataset, dirModel: Model, fileModelCLs: java.util.List[ClassLoader]) {
    FileModelRepoLoader.loadSheetModelsIntoTargetDataset(repo, mainDset, dirModel, fileModelCLs)
  }
}

object FileModelRepoLoader extends BasicDebugger {

  def loadSheetModelsIntoTargetDataset(repo: Repo.WithDirectory, mainDset: Dataset,
    myDirectoryModel: Model, clList: java.util.List[ClassLoader]): Unit = {

    if (myDirectoryModel.size == 0) return
    val nsJavaMap: java.util.Map[String, String] = myDirectoryModel.getNsPrefixMap()

    val msqText = """
			select ?repo ?repoPath ?model ?modelPath ?unionOrReplace
				{
					?repo  a ccrt:FileRepo; ccrt:sourcePath ?repoPath.
					?model a ccrt:FileModel; ccrt:sourcePath ?modelPath; ccrt:repo ?repo.
      				OPTIONAL { ?model a ?unionOrReplace. FILTER (?unionOrReplace = ccrt:UnionModel) }
				}
		"""

    val msRset = QueryHelper.execModelQueryWithPrefixHelp(myDirectoryModel, msqText);
    import scala.collection.JavaConversions._;
    while (msRset.hasNext()) {
      val qSoln: QuerySolution = msRset.next();

      val repoRes: Resource = qSoln.getResource("repo");
      val modelRes: Resource = qSoln.getResource("model");
      val unionOrReplaceRes: Resource = qSoln.getResource("unionOrReplace");
      val repoPath_Lit: Literal = qSoln.getLiteral("repoPath")
      val modelPath_Lit: Literal = qSoln.getLiteral("modelPath")
      val dbgArray = Array[Object](repoRes, repoPath_Lit, modelRes, modelPath_Lit);
      getLogger.warn("repo={}, repoPath={}, model={}, modelPath={}", dbgArray);

      val rPath = repoPath_Lit.getString();
      val mPath = modelPath_Lit.getString();

      getLogger().warn("Ready to read from [{}] / [{}]", Array[Object](rPath, mPath));
      val rdfURL = rPath + mPath;

      repo.addLoadTask(rdfURL, new Runnable() {
        def run() {
          try {
            val graphURI = modelRes.getURI();
            val fileModel = FancyRepoLoader.readModelSheetFromURL(rdfURL, nsJavaMap, clList);
            getLogger.warn("Read fileModel: {}", fileModel)
            FancyRepoLoader.replaceOrUnion(mainDset, unionOrReplaceRes, graphURI, fileModel);
          } catch {
            case except: Throwable => getLogger().error("Caught error loading file {}", Array[Object](rdfURL, except))
          }
        }
      })

    }
  }
}
