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

import org.appdapter.core.store.Repo
import org.appdapter.fancy.rspec.{RepoSpec}


/**
 * @author Stu B. <www.texpedient.com>
 * 
 * This class is a workaround to handle the situation where we are tightly associating a repo-Client with a 
 * known-local repo.   In general this is a clumsy practice to be discarded as the repo-Client becomes more truly
 * independent.
 */

class EnhancedRepoClient(val myRepoSpec: RepoSpec, repo: Repo.WithDirectory, dfltTgtGraphVarName: String, dfltQrySrcGrphName: String)
  extends LocalRepoClientImpl(repo, dfltTgtGraphVarName, dfltQrySrcGrphName) {

  def reloadRepoAndClient(): EnhancedRepoClient = {
    val reloadedRepo = myRepoSpec.getOrMakeRepo()
    val reloadedClient = new EnhancedRepoClient(myRepoSpec, reloadedRepo, dfltTgtGraphVarName, dfltQrySrcGrphName)
    reloadedClient
  }

  override def toString(): String = {
    getClass.getName + "[repoSpec=" + myRepoSpec + ", dfltTgtGraphVar=" + dfltTgtGraphVarName +
      ", dfltQrySrcGrphName=" + dfltQrySrcGrphName + ", repo=" + repo + "]"
  }
}
