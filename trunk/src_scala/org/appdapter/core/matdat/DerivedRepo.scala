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

package org.appdapter.core.matdat

import com.hp.hpl.jena.query.DataSource
import com.hp.hpl.jena.query.Dataset
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.name.Ident
import org.appdapter.core.store.Repo
import org.appdapter.core.store.RepoOper;
import org.appdapter.help.repo.{ RepoClient, RepoClientImpl, InitialBindingImpl } 

import org.appdapter.impl.store.DirectRepo
import scala.collection.JavaConversions.asScalaSet

/**
 * @author LogicMOO <www.logicmoo.org>
 */


// FIXME:  The srcRepo should really not be given to the RepoSpec, because it
// is not serializable specData.
class DerivedRepoSpec(val myDGSpecs: Set[DerivedGraphSpec], val mySrcRepo: Repo.WithDirectory) extends RepoSpec {
  override def toString(): String = {
    "PipelineRepoSpec[pipeSpecs= " + myDGSpecs + "]";
  }
  override def makeRepo(): DerivedRepo = {
    val emptyDirModel = ModelFactory.createDefaultModel();
    // TODO:  Copy over prefix-abbreviations from the source repo (need to confirm the line below does this correctly)
    emptyDirModel.setNsPrefixes(mySrcRepo.getDirectoryModel.getNsPrefixMap);
    val derivedRepo = new DerivedRepo(emptyDirModel, this)
    for (dgSpec <- myDGSpecs) {
		val derivedModelProvider = dgSpec.makeDerivedModelProvider(mySrcRepo);
		val derivedModel = derivedModelProvider.getModel()
		derivedRepo.replaceNamedModel(dgSpec.myTargetGraphTR, derivedModel)
    }
    derivedRepo
  }
}

// @TODO to be moved to org.appdapter.lib.core
class DerivedRepo(emptyDirModel: Model, val myRepoSpec: DerivedRepoSpec) extends DirectRepo(emptyDirModel) with RepoOper.Reloadable {

  def reloadAllModels() = {
    //myRepoSpec.makeRepo
    myRepoSpec.makeRepo
  }

  def reloadSingleModel(modelName: String) = {
    val repo = myRepoSpec.makeRepo();
    val oldDataset = getMainQueryDataset();
    val myPNewMainQueryDataset = repo.getMainQueryDataset();
    getLogger.info("START: Trying to do reloading of model named.. " + modelName)
    RepoOper.replaceDatasetElements(oldDataset, myPNewMainQueryDataset, modelName)
    getLogger.info("START: Trying to do reloading of model named.. " + modelName)
  }


}
