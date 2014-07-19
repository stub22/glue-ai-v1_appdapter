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

/**
 * 
 */
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

abstract class RepoSpec extends LocalRepoClientSpec {

  def makeRepo(): Repo.WithDirectory;

/* FIXME: The semantics here are bad - "getting" a directory model leads to "making" a repo first? */
	
  def getDirectoryModel(): Model = {
    makeRepo.getDirectoryModel();
  }

}

abstract class RepoSpecForDirectory extends RepoSpec {
  override def makeRepo: FancyRepo = {
    FancyRepoLoader.makeRepoWithDirectory(this, getDirectoryModel(), null);
  }
  def getDirectoryModel(): Model;
}



