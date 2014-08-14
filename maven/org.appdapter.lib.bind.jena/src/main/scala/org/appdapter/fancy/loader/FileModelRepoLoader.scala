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

import java.io.File
import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.loader.ExtendedFileLoading.Paths
import org.appdapter.core.loader.{SpecialRepoLoader}
import com.hp.hpl.jena.query.{ Dataset, QuerySolution }
import com.hp.hpl.jena.rdf.model.{ Literal, Model, Resource }
import org.appdapter.fancy.query.QueryHelper
import org.appdapter.fancy.rspec.{URLDirModelRepoSpec}


/// this is a registerable loader
class FileModelRepoLoader extends InstallableRepoLoader {
	override def makeRepoSpec(path: String, args: Array[String], cLs: java.util.List[ClassLoader]) = new URLDirModelRepoSpec(path, cLs)
	override def getExt = "ttl"
	override def getContainerType() = "ccrt:FileRepo"
	override def getSheetType() = "ccrt:FileModel"
	override def loadModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset, dirModel: Model, 
											 fileModelCLs: java.util.List[ClassLoader], optPrefixURL : String) {
		FileModelRepoLoader.loadFileModelsIntoTargetDataset(repo, mainDset, dirModel, fileModelCLs, optPrefixURL)
	}
}

object FileModelRepoLoader extends BasicDebugger {

	def loadFileModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset,
										directoryModel: Model, clList: java.util.List[ClassLoader], optPrefixURL : String): Unit = {

		if (directoryModel.size == 0) return
		val nsJavaMap: java.util.Map[String, String] = directoryModel.getNsPrefixMap()

		val msqText = """
			select ?repo ?repoPath ?model ?modelPath ?unionOrReplace
				{
					?repo  a ccrt:FileRepo; ccrt:sourcePath ?repoPath.
					?model a ccrt:FileModel; ccrt:sourcePath ?modelPath; ccrt:repo ?repo.
      				OPTIONAL { ?model a ?unionOrReplace. FILTER (?unionOrReplace = ccrt:UnionModel) }
				}
		"""

		val msRset = QueryHelper.execModelQueryWithPrefixHelp(directoryModel, msqText);
		import scala.collection.JavaConversions._;
		while (msRset.hasNext()) {
			val qSoln: QuerySolution = msRset.next();

			val repoRes: Resource = qSoln.getResource("repo");
			val modelRes: Resource = qSoln.getResource("model");
			val unionOrReplaceRes: Resource = qSoln.getResource("unionOrReplace");
			val repoPath_Lit: Literal = qSoln.getLiteral("repoPath")
			val modelPath_Lit: Literal = qSoln.getLiteral("modelPath")
			val dbgSeq = Seq(repoRes, repoPath_Lit, modelRes, modelPath_Lit);
			getLogger.info("repo={}, repoPathLit={}, modelRes={}, modelPath_Lit={}", dbgSeq :_*);

			val rPath = repoPath_Lit.getString();
			val mPath = modelPath_Lit.getString();

			
			// Check to see if the rPath is a partial tail-match for optPrefixURL.
	  
			val folderPath = if (optPrefixURL != null) {
				// TODO:  Make this work with/without trailing slashes.
				if (optPrefixURL.endsWith(rPath)) {
					optPrefixURL
				} else {
					if (rPath.startsWith("/") || rPath.contains(':')) {
						// rPath appears to be "absolute", so let's pass it through untouched
						rPath
					} else {
						//rPath appears to be relative, let's prefix it!
						optPrefixURL + rPath
					}
				}
			} else {
				rPath
			}	  
			val rdfURL = folderPath + mPath;
			getLogger.info("Computed URL as [{}] based on repoPath=[{}] / modelPath=[{}], using opt prefix [{}]", 
						   Seq(rdfURL, rPath, mPath, optPrefixURL) :_*);

			repo.addLoadTask(rdfURL, new Runnable() {
					def run() {
						try {
							val graphURI = modelRes.getURI();
							val fileModel = FancyRepoLoader.readRdfGraphFromURL(rdfURL, nsJavaMap, clList);
							getLogger.debug("Read fileModel contents: {}", fileModel)
							FancyRepoLoader.replaceOrUnion(mainDset, unionOrReplaceRes, graphURI, fileModel);
						} catch {
							case except: Throwable => getLogger.error("Caught error loading file {}", Seq(rdfURL, except) :_*)
						}
					}
				})

		}
	}
}
