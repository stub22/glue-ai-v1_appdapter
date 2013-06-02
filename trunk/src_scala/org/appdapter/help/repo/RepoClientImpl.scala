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

package org.appdapter.help.repo

import org.appdapter.core.name.{Ident}
import com.hp.hpl.jena.rdf.model.{Model}
import org.appdapter.core.store.{Repo, InitialBinding}
import org.appdapter.impl.store.{ModelClientCore}


import scala.collection.JavaConversions._

/**
 * @author Ryan Biggs
 * @author Stu B. <www.texpedient.com>
 */

class RepoClientImpl(private val myRepo : Repo.WithDirectory, private val myDefltTgtGraphVarName : String, private val myDefltQrySrcGrphName : String ) 
		extends RepoClient with ModelClientCore {
  
	private val mySH = new SolutionHelper()
	
	private def ensureRepo {
		// TODO: check repo health, log errors if any problems found
	}
	override def getRepo : Repo.WithDirectory = {
		ensureRepo
		myRepo
	}
	// Allows us to implement ModelClient API via delegation to the repo's directory Model.
	protected def getModel : Model = getRepo.getDirectoryModel
	
	private def getQuerySheetName = myDefltQrySrcGrphName; 
	
	private def getQueryVarName = myDefltTgtGraphVarName ; 
	
	/* Fundamental form, with idents for the query lookup */
	override  def queryIndirectForAllSolutions(qSrcGraphIdent : Ident, queryIdent: Ident, qInitBinding: InitialBinding) : SolutionList = {
		val r = getRepo;
		val javaSL = r.queryIndirectForAllSolutions(qSrcGraphIdent, queryIdent, qInitBinding.getQSMap)
		mySH.makeSolutionList(javaSL)
	}
	/* Fundamental form, with QNames for the query lookup */
	override def queryIndirectForAllSolutions(qSrcGraphQN : String, queryQName: String, qInitBinding: InitialBinding) : SolutionList = {
		val r = getRepo;
		val javaSL = r.queryIndirectForAllSolutions(qSrcGraphQN, queryQName, qInitBinding.getQSMap)
		mySH.makeSolutionList(javaSL)
	}
	override def queryIndirectForAllSolutions( queryQName: String, qInitBinding: InitialBinding) : SolutionList = {
		val querySheetQName = getQuerySheetName
		queryIndirectForAllSolutions(querySheetQName, queryQName, qInitBinding)
	}
	override def queryIndirectForAllSolutions(queryQName: String, soleSPARQL_VN : String, soleVarIdent : Ident) : SolutionList	= {
		val r = getRepo;
		val qib = r.makeInitialBinding
		qib.bindIdent(soleSPARQL_VN, soleVarIdent)
		queryIndirectForAllSolutions(queryQName, qib)	
	}
	override def queryIndirectForAllSolutions(queryQName: String, targetGraphIdent : Ident, otherSPARQL_VN : String, otherVarIdent : Ident) : SolutionList ={
		val r = getRepo;
		val qib = r.makeInitialBinding
		val tgtGraphVN = getQueryVarName
		qib.bindIdent(tgtGraphVN, targetGraphIdent)
		qib.bindIdent(otherSPARQL_VN, otherVarIdent)
		queryIndirectForAllSolutions(queryQName, qib)
	}
	override def queryIndirectForAllSolutions( queryQName: String, targetGraphIdent : Ident) : SolutionList = {
		val qSoleVarName = getQueryVarName
		queryIndirectForAllSolutions(queryQName,qSoleVarName, targetGraphIdent)
	}
	// The rest of the variants of this method below are just sugary wrappers that resolve more QNames.
	override def queryIndirectForAllSolutions(queryQName: String, soleSPARQL_VN : String, soleVarQN : String) : SolutionList = {
		val soleVarIdent = getDirectoryModelClient.makeIdentForQName(soleVarQN);
		queryIndirectForAllSolutions(queryQName, soleSPARQL_VN, soleVarIdent)
	}
	def queryIndirectForAllSolutions(queryQN: String, targetGraphQN : String) : SolutionList = {
		val targetGraphIdent = getDirectoryModelClient.makeIdentForQName(targetGraphQN)
		queryIndirectForAllSolutions(queryQN, targetGraphIdent)
	}
	override def queryIndirectForAllSolutions(queryQN: String, targetGraphQN : String, otherSPARQL_VN : String, otherValQN : String) : SolutionList = {
		val targetGraphID = getDirectoryModelClient.makeIdentForQName(targetGraphQN)
		val otherValID = getDirectoryModelClient.makeIdentForQName(targetGraphQN)
		queryIndirectForAllSolutions(queryQN, targetGraphID, otherSPARQL_VN, otherValID)
	}
	
	// Stu 2012-09-30 : Think this is not used yet, might not be worth the space.
	override def queryIndirectForAllSolutionsWithStringBinding(queryQName: String, soleVarName : String, soleVarLiteralString : String) : SolutionList = {
		val r = getRepo;
		val qib = r.makeInitialBinding		
		qib.bindLiteralString(soleVarName, soleVarLiteralString)
		queryIndirectForAllSolutions(queryQName, qib)
	}
	
	override def makeInitialBinding : InitialBinding = getRepo.makeInitialBinding
	
	private def getDirectoryModelClient  = getRepo.getDirectoryModelClient
	
	override def assembleRootsFromNamedModel(graphNameIdent : Ident) : java.util.Set[Object] = 
				getRepo.assembleRootsFromNamedModel(graphNameIdent);
			
	override def assembleRootsFromNamedModel(graphQName : String)  : java.util.Set[Object] = {
		val graphID = getDirectoryModelClient.makeIdentForQName(graphQName)
		assembleRootsFromNamedModel(graphID);
	}
			
}
