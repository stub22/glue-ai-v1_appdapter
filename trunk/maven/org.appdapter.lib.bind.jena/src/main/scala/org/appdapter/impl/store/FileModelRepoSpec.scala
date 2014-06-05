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

package org.appdapter.core.repo

import java.io.File
import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.store.ExtendedFileLoading.Paths
import org.appdapter.core.store.dataset.SpecialRepoLoader
import com.hp.hpl.jena.query.{ Dataset, QuerySolution }
import com.hp.hpl.jena.rdf.model.{ Literal, Model, Resource }
import org.appdapter.impl.store.QueryHelper
/**
 * @author Stu B. <www.texpedient.com>
 * @author Douglas R. Miles <www.logicmoo.org>
 *
 * This is a FileModel Loader it contains the InstallableRepoReader and Loader class with static methods for loading FileRepos
 */
class ScanURLDirModelRepoSpec(var dirModelURL: String, fileModelCLs: java.util.List[ClassLoader]) extends MultiRepoSpec(null, fileModelCLs) {
  def createURIFromBase(fileDirMask: String) = "scan:/" + fileDirMask;

  //toStringName = createURIFromBase(dirModelURL);

  def populateDirModel(dir0: String) {
    var dir = dir0
    val all = new java.util.ArrayList[RepoSpec]
    val fileFilter = new Paths();
    if (new File(dir).isDirectory()) {
      while (dir.endsWith("\\")) {
        dir = dir.substring(0, dir.length() - 1);
      }
      while (dir.endsWith("/")) {
        dir = dir.substring(0, dir.length() - 1);
      }
      fileFilter.glob(dir + "/", "dir.ttl");
      fileFilter.glob(dir + "/**", "dir.ttl");
    } else {
      fileFilter.glob(".", dir);
    }
    var paths = fileFilter.getFiles();
    for (f <- paths.toArray(new Array[java.io.File](0))) {
      all.add(new URLDirModelRepoSpec(f.getPath(), fileModelCLs));
    }
  }

  override def getDirectoryModel() = {
    populateDirModel(dirModelURL)
    super.getDirectoryModel
  }

  override def toString = if (toStringName != null) toStringName else createURIFromBase(dirModelURL)
}

class URLDirModelRepoSpecReader extends InstallableSpecReader {
  override def getExt = "dir"
  override def makeRepoSpec(path: String, args: Array[String], cLs: java.util.List[ClassLoader]) = new URLDirModelRepoSpec(path, cLs)
}

class ScanURLDirModelRepoSpecReader extends InstallableSpecReader {
  override def getExt = "scandir"
  override def makeRepoSpec(path: String, args: Array[String], cLs: java.util.List[ClassLoader]) = new ScanURLDirModelRepoSpec(path, cLs)
}

class URLModelRepoSpecReader extends InstallableSpecReader {
  override def getExt = "ttl"
  override def makeRepoSpec(path: String, args: Array[String], cLs: java.util.List[ClassLoader]) = new URLDirModelRepoSpec(path, cLs)
}

class ScanURLModelRepoSpecReader extends InstallableSpecReader {
  override def getExt = "scanttl"
  override def makeRepoSpec(path: String, args: Array[String], cLs: java.util.List[ClassLoader]) = new ScanURLDirModelRepoSpec(path, cLs)
}

class URLDirModelRepoSpec(dirModelURL: String, fileModelCLs: java.util.List[ClassLoader]) extends RepoSpecForDirectory {
  //override def makeRepo = FancyRepoLoader.loadDetectedFileSheetRepo(dirModelURL, null, fileModelCLs, this)
  override def getDirectoryModel = FancyRepoLoader.readDirectoryModelFromURL(dirModelURL, null, fileModelCLs)
  override def toString = dirModelURL
}

/// this is a registerable loader
class FileModelRepoLoader extends InstallableRepoReader {
  override def makeRepoSpec(path: String, args: Array[String], cLs: java.util.List[ClassLoader]) = new URLDirModelRepoSpec(path, cLs)
  override def getExt = "ttl"
  override def getContainerType() = "ccrt:FileRepo"
  override def getSheetType() = "ccrt:FileModel"
  override def loadModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset, dirModel: Model, fileModelCLs: java.util.List[ClassLoader]) {
    FileModelRepoLoader.loadSheetModelsIntoTargetDataset(repo, mainDset, dirModel, fileModelCLs)
  }
}

object FileModelRepoLoader extends BasicDebugger {

  def loadSheetModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset,
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
