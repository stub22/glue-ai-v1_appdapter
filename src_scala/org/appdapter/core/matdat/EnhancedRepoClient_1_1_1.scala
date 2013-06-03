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

import org.appdapter.core.store.Repo
import org.appdapter.help.repo.RepoClientImpl
//import org.appdapter.core.store.RepoSpec

/**
 * @author Stu B. <www.texpedient.com>
 */

class EnhancedRepoClient(val myRepoSpec: RepoSpec_1_1_1, repo: Repo.WithDirectory, dfltTgtGraphVarName: String, dfltQrySrcGrphName: String)
  extends RepoClientImpl(repo, dfltTgtGraphVarName, dfltQrySrcGrphName) {

  def reloadRepoAndClient(): EnhancedRepoClient = {
    val reloadedRepo = myRepoSpec.makeRepo()
    val reloadedClient = new EnhancedRepoClient(myRepoSpec, reloadedRepo, dfltTgtGraphVarName, dfltQrySrcGrphName)
    reloadedClient
  }

}
