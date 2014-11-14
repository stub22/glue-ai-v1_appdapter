/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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

package org.appdapter.fancy.rclient

import org.appdapter.core.name.Ident
import org.appdapter.core.store.Repo
import org.appdapter.fancy.rspec.{RepoSpec}

import org.appdapter.fancy.gportal.{DelegatingPortal}
/**
 * @author Stu B. <www.texpedient.com>
 * 
 * This class is a workaround to handle the situation where we are tightly associating a repo-Client with a 
 * known-local repo.   In general this is a clumsy practice to be discarded as the repo-Client becomes more truly
 * independent.
 */

trait EnhancedRepoClient extends RepoClient {
	 def reloadRepoAndClient(): EnhancedRepoClient = ???
}
case class EnhancedLocalRepoClient(val myRepoSpec: RepoSpec, repo: Repo.WithDirectory, dfltTgtGraphVarName: String, dfltQrySrcGrphName: String)
  extends LocalRepoClientImpl(repo, dfltTgtGraphVarName, dfltQrySrcGrphName) with EnhancedRepoClient {

  override def reloadRepoAndClient(): EnhancedRepoClient = {
    val reloadedRepo = myRepoSpec.getOrMakeRepo()
    val reloadedClient = new EnhancedLocalRepoClient(myRepoSpec, reloadedRepo, dfltTgtGraphVarName, dfltQrySrcGrphName)
    reloadedClient
  }
/** "case" takes care of generating toString method - but does it emit inherited fields as well?  
 * Probably not the "private" ones in RepoClientImpl, eh?
  override def toString(): String = {
    getClass.getName + "[repoSpec=" + myRepoSpec + ", dfltTgtGraphVar=" + dfltTgtGraphVarName +
      ", dfltQrySrcGrphName=" + dfltQrySrcGrphName + ", repo=" + repo + "]"
  }
  */
}

case class EnhancedPortalRepoClient(dgPortal : DelegatingPortal, dfltResGraphID : Ident, 
						dfltTgtGraphVarName: String, dfltQrySrcGrphName: String)
	extends GraphPortalRepoClient(dgPortal, dfltResGraphID, dfltTgtGraphVarName, dfltQrySrcGrphName) 
	 with EnhancedRepoClient
  {

  override def reloadRepoAndClient(): EnhancedRepoClient = ???
 /* {
    val reloadedRepo = myRepoSpec.getOrMakeRepo()
    val reloadedClient = new EnhancedLocalRepoClient(myRepoSpec, reloadedRepo, dfltTgtGraphVarName, dfltQrySrcGrphName)
    reloadedClient
  } */

}
