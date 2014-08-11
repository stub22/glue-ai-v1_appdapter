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

package org.appdapter.fancy.rspec

import java.util.HashMap
import org.osgi.framework.BundleContext

import org.appdapter.core.boot.ClassLoaderUtils
import org.appdapter.core.store.{ Repo }
import org.appdapter.demo.DemoBrowserUI
import org.appdapter.fancy.rclient.{RepoClientImpl, LocalRepoClientImpl}
import org.appdapter.fancy.loader.{FancyRepoLoader, InstallableRepoLoader}
import org.appdapter.fancy.repo.{ FancyRepo }
import com.hp.hpl.jena.rdf.model.Model
import org.appdapter.core.log.BasicDebugger


abstract class RepoClientSpec extends BasicDebugger {
/**
 * Provide the name of a graph that can supply us with the text of queries to use, in the form of a
 * String QName, assumed resolvable against some "good" prefix set, presumably fetched from some "good"
 * model the client knows about.  Somewhat loosey-goosey as a "spec", however.
 */
  def getDfltQrySrcGraphQName = RepoSpecDefaultNames.DFLT_QRY_SRC_GRAPH_TYPE;
  /**
   * This name tells us the name of a SPARQL "target graph" variable name usable in many common queries.
   */
  def getDfltTgtGraphSparqlVarName: String = RepoSpecDefaultNames.DFLT_TGT_GRAPH_SPARQL_VAR;
  
}

abstract class LocalRepoClientSpec extends RepoClientSpec {
  def makeRepoClient(repo: Repo.WithDirectory, dfltQrySvcGraphQN: String): RepoClientImpl = {
    new LocalRepoClientImpl(repo, getDfltTgtGraphSparqlVarName, dfltQrySvcGraphQN);
  }
  //deprecated("uses hardcoded query sheet like ccrt:qry_sheet_77")
  def makeRepoClient(repo: Repo.WithDirectory): RepoClientImpl = {
    new LocalRepoClientImpl(repo, getDfltTgtGraphSparqlVarName, getDfltQrySrcGraphQName);
  }	
}

/**
 * Could be either the Repo itself or the Dir-model that is found/created first.
 */
abstract class RepoSpec extends LocalRepoClientSpec {

	/**
	 * This might call getOrMakeDirectoryModel.
	 */
  protected def makeRepo(): Repo.WithDirectory;

  def getOrMakeRepo() : Repo.WithDirectory = makeRepo()
  
	/**
	 * This might call getOrMakeRepo.
	 */
  protected def makeDirectoryModel(): Model 
  
  def getOrMakeDirectoryModel(): Model = makeDirectoryModel() 
}

/**
 *  In the case of RepoSpecForDirectory, assume that a dir-model will be found first,
 *  and we will build the repo around it.
 */
abstract class RepoSpecForDirectory extends RepoSpec {
  override protected def makeRepo: FancyRepo = {
    FancyRepoLoader.makeRepoWithDirectory(this, getOrMakeDirectoryModel(), null);
  }
   override def getOrMakeRepo: FancyRepo =  makeRepo
  
}
/**
 grep -rin    --include=*.{java,scala,xml,ttl,owl,html} --exclude="*\.svn*" --exclude-dir=target RepoSpecForDir appda
pter_trunk/maven/ cogchar_trunk/maven/ friendularity_trunk/maven/ 

appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/rspec/CSVFileRepoSpec.scala:35:  fi
leModelCLs: java.util.List[ClassLoader] = null) extends RepoSpecForDirectory {

appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/rspec/GoogSheetRepoSpec.scala:21://
 import org.appdapter.fancy.rspec.{RepoSpec, RepoSpecForDirectory}
 
appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/rspec/GoogSheetRepoSpec.scala:37:
fileModelCLs: java.util.List[ClassLoader]) extends RepoSpecForDirectory {

appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/rspec/GoogSheetRepoSpec.scala:43:
fileModelCLs: java.util.List[ClassLoader]) extends RepoSpecForDirectory {

appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/rspec/MultiRepoSpec.scala:27:  exte
nds RepoSpecForDirectory {

appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/rspec/OfflineXlsSheetRepoSpec.scala
:21:  fileModelCLs: java.util.List[ClassLoader] = null) extends RepoSpecForDirectory {

appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/rspec/RepoSpec.scala:72:abstract cl
ass RepoSpecForDirectory extends RepoSpec {

appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/rspec/UrlDirModelRepoSpec.scala:73:
class URLDirModelRepoSpec(dirModelURL: String, fileModelCLs: java.util.List[ClassLoader]) extends RepoSpecForDirectory {


 */


