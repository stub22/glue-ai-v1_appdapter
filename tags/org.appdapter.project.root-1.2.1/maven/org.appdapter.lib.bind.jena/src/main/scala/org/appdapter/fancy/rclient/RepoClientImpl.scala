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

package org.appdapter.fancy.rclient

import com.hp.hpl.jena.rdf.model.Model

import org.appdapter.core.name.Ident

import org.appdapter.core.model.{RdfNodeTranslator}
import org.appdapter.core.query.{InitialBinding}
import org.appdapter.core.store.{ Repo}

import org.appdapter.fancy.model.{ModelClientCore, ModelClientImpl}

import org.appdapter.fancy.query.{SolutionList, SolutionHelper}

/**
 * @author Ryan Biggs
 * @author Stu B. <www.texpedient.com>
 */

abstract class RepoClientImpl( private val myDefltTgtGraphVarName: String, private val myDefltQrySrcGrphQName: String)
  extends RepoClient { // with ModelClientCore {

	override def getDefaultRdfNodeTranslator: org.appdapter.core.model.RdfNodeTranslator = ???
	override def getNamedModelReadonly(graphID: org.appdapter.core.name.Ident): com.hp.hpl.jena.rdf.model.Model = ???
	// override def makeIdentForQName(qn: String): org.appdapter.core.name.Ident = ???
	
  protected val mySH = new SolutionHelper()


  // Allows us to implement ModelClient API via delegation to the repo's directory Model.
 // protected def getModel: Model = getRepo.getDirectoryModel

  private def getQuerySheetQName = myDefltQrySrcGrphQName;

  private def getQueryVarName = myDefltTgtGraphVarName;


  override def queryIndirectForAllSolutions(queryQName: String, qInitBinding: InitialBinding): SolutionList = {
    val querySheetQName = getQuerySheetQName
    queryIndirectForAllSolutions(querySheetQName, queryQName, qInitBinding)
  }
  override def queryIndirectForAllSolutions(queryQName: String, soleSPARQL_VN: String, soleVarIdent: Ident): SolutionList = {
  //  val r = getRepo;
    val qib = makeInitialBinding
    qib.bindIdent(soleSPARQL_VN, soleVarIdent)
    queryIndirectForAllSolutions(queryQName, qib)
  }
  override def queryIndirectForAllSolutions(queryQName: String, targetGraphIdent: Ident, otherSPARQL_VN: String, otherVarIdent: Ident): SolutionList = {
   // val r = getRepo;
    val qib = makeInitialBinding
    val tgtGraphVN = getQueryVarName
    qib.bindIdent(tgtGraphVN, targetGraphIdent)
    qib.bindIdent(otherSPARQL_VN, otherVarIdent)
    queryIndirectForAllSolutions(queryQName, qib)
  }
  override def queryIndirectForAllSolutions(queryQName: String, targetGraphIdent: Ident): SolutionList = {
    val qSoleVarName = getQueryVarName
    queryIndirectForAllSolutions(queryQName, qSoleVarName, targetGraphIdent)
  }
  // The rest of the variants of this method below are just sugary wrappers that resolve more QNames.
  override def queryIndirectForAllSolutions(queryQName: String, soleSPARQL_VN: String, soleVarQN: String): SolutionList = {
    val soleVarIdent = getDefaultRdfNodeTranslator.makeIdentForQName(soleVarQN);
    queryIndirectForAllSolutions(queryQName, soleSPARQL_VN, soleVarIdent)
  }
  def queryIndirectForAllSolutions(queryQN: String, targetGraphQN: String): SolutionList = {
    val targetGraphIdent = getDefaultRdfNodeTranslator.makeIdentForQName(targetGraphQN)
    queryIndirectForAllSolutions(queryQN, targetGraphIdent)
  }
  override def queryIndirectForAllSolutions(queryQN: String, targetGraphQN: String, otherSPARQL_VN: String, otherValQN: String): SolutionList = {
    val targetGraphID = getDefaultRdfNodeTranslator.makeIdentForQName(targetGraphQN)
    val otherValID = getDefaultRdfNodeTranslator.makeIdentForQName(targetGraphQN)
    queryIndirectForAllSolutions(queryQN, targetGraphID, otherSPARQL_VN, otherValID)
  }

  // Stu 2012-09-30 : Think this is not used yet, might not be worth the space.
  override def queryIndirectForAllSolutionsWithStringBinding(queryQName: String, soleVarName: String, soleVarLiteralString: String): SolutionList = {
 //   val r = getRepo;
    val qib = makeInitialBinding
    qib.bindLiteralString(soleVarName, soleVarLiteralString)
    queryIndirectForAllSolutions(queryQName, qib)
  }

  override def assembleRootsFromNamedModel(graphQName: String): java.util.Set[Object] = {
    val graphID = getDefaultRdfNodeTranslator.makeIdentForQName(graphQName)
    assembleRootsFromNamedModel(graphID);
  }

}

