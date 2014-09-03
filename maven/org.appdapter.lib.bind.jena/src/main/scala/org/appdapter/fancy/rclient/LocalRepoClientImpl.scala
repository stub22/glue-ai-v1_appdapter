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

package org.appdapter.fancy.rclient
import com.hp.hpl.jena.rdf.model.Model

import org.appdapter.core.name.Ident

import org.appdapter.core.model.{RdfNodeTranslator}
import org.appdapter.core.query.{InitialBinding}
import org.appdapter.core.store.{ Repo, RepoQueryFuncs_TxAware}
import org.appdapter.bind.rdf.jena.query.JenaArqQueryFuncs_TxAware.Oper

import org.appdapter.fancy.model.{ModelClientCore, ModelClientImpl}

import org.appdapter.fancy.query.{SolutionList, SolutionHelper}

import com.hp.hpl.jena.rdf.model.ModelFactory;
/**
 * @author Stu B. <www.texpedient.com>
 */
class LocalRepoClientImpl(private val myRepo: Repo.WithDirectory,  dTgtGraphVarName: String, dQrySrcGrphName:String) 
		extends RepoClientImpl(dTgtGraphVarName, dQrySrcGrphName) {
  private def ensureRepo {
    // TODO: check repo health, log errors if any problems found
  }
  override def getRepoIfLocal: Repo.WithDirectory = {
    ensureRepo
    myRepo
  }
  var	myFixmeCachedModelClient : RdfNodeTranslator = null
  
  override def getDefaultRdfNodeTranslator: org.appdapter.core.model.RdfNodeTranslator = {
		if (myFixmeCachedModelClient == null) {
			val r = getRepoIfLocal
			if (r != null) {
				val dirModel = r.getDirectoryModel
				if (dirModel != null) {
					myFixmeCachedModelClient = new ModelClientImpl(dirModel)
				}
			}
		}
		myFixmeCachedModelClient
	}
	override def getNamedModelReadonly(graphID: org.appdapter.core.name.Ident): com.hp.hpl.jena.rdf.model.Model = {
		
		val r = getRepoIfLocal
		if (r != null) {
			// See comment about copies in the RepoClient trait.
			
			// Repo returns a handle to a local model (typically within a jena-arq-dataset).
			// We (usually) must be in a transaction when we make this call.
			// IF we are already in a transaction, then it would be permitted to return the handle, rather than a copy.
			// However, that would be purely as a performance optimization, and should not have any logical impact,
			// because the same data is contained in a read-locked model as in a copy of that model.
			// 
			// We are currently assuming such an optimization is not necessary, so we always make a copy.
			//  val futureOptFlag = in transaction already?
			val copiedModel : Model = RepoQueryFuncs_TxAware.execReadTransCompatible(r, null, new Oper[Model]() { 
				override def perform(): Model = {
					val modelHandleInTrans = r.getNamedModel(graphID)
					val copy = ModelFactory.createDefaultModel();
					// See notes about future possible optimization above.       if (futureOptFlag) then don't copy
					copy.add(modelHandleInTrans)
					copy
				}
			})
			copiedModel
		} else null
	}
  
  /* Fundamental form, with idents for the query lookup */
  override def queryIndirectForAllSolutions(qSrcGraphIdent: Ident, queryIdent: Ident, qInitBinding: InitialBinding): SolutionList = {
    val r = getRepoIfLocal;
    val javaSL = r.queryIndirectForAllSolutions(qSrcGraphIdent, queryIdent, qInitBinding.getQSMap)
    mySH.makeSolutionList(javaSL)
  }
  /* Fundamental form, with QNames for the query lookup */
  override def queryIndirectForAllSolutions(qSrcGraphQN: String, queryQName: String, qInitBinding: InitialBinding): SolutionList = {
    val r = getRepoIfLocal;
    val javaSL = r.queryIndirectForAllSolutions(qSrcGraphQN, queryQName, qInitBinding.getQSMap)
    mySH.makeSolutionList(javaSL)
  }  
  override def makeInitialBinding: InitialBinding = getRepoIfLocal.makeInitialBinding

//  private def getDirectoryModelClient = getRepo.getDirectoryModelClient

  override def assembleRootsFromNamedModel(graphNameIdent: Ident): java.util.Set[Object] = {
    getRepoIfLocal.assembleRootsFromNamedModel(graphNameIdent);  
  }
}