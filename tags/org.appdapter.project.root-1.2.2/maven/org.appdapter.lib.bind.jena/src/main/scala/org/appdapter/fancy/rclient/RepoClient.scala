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

import org.appdapter.bind.rdf.jena.query.JenaArqQueryFuncs_TxAware
import org.appdapter.core.name.Ident
import org.appdapter.core.store.{ Repo}
import org.appdapter.core.model.{RdfNodeTranslator}
import org.appdapter.core.query.{InitialBinding}
import org.appdapter.fancy.query.{SolutionList}
import com.hp.hpl.jena.rdf.model.Model;
// import org.appdapter.core.repo.{FancyRepo};

// maybe trait RepoClientScala extends RepoClientJava {
trait RepoClient { // extends RdfNodeTranslator {

  /**
   * @return Repo.WithDirectory, *if* we have a local repo.
   */
  def getRepoIfLocal: Repo.WithDirectory = null

	/**
	 * Under what circumstances is this "readonly" model a copy of the original graph held in the source repo?
	 * - remote repo:  the local result is always a copy of what the remote repo supplied.
	 * - local repo:   it must either be a copy, or [Not currently implemented, but in theory:]  
	 *					we must be bracketed inside a read-trans, managed by the caller.
	 *     The latter is a potential performance optimization in (unusual, not expected) cases where the repo-graph is 
	 *     very large, and we don't want to use memory and time to copy it. Again, this case is NOT YET IMPLEMENTED
	 *     (as of 2014-10-06)
	 */
	def getNamedModelReadonly(graphID : Ident) : Model;
	// def makeIdentForQName(qn : String) : Ident
	
	// 2014-October new API methods added to allow RepoClient to expose more portal-style features, although we
	// recommend that new code be written against the Portal API when possible.
	// put=overwrite target, post=add/merge to target, clear=delete from target dataset
	def putNamedModel(graphID : Ident, contents : Model) : Unit
	def postNamedModel(graphID : Ident, contents : Model) : Unit
	def clearNamedModel(graphID : Ident) : Unit
	
	def getDefaultRdfNodeTranslator : RdfNodeTranslator
	// def getDirModelClientOrNull() : ModelClient
  /**
   * The most general case, using Idents for input.
   */
  def queryIndirectForAllSolutions(queryModelIdent: Ident, queryIdent: Ident, qInitBinding: InitialBinding): SolutionList
  /**
   * The most general case, this time using QNames.
   */
  def queryIndirectForAllSolutions(queryModelQName: String, queryQName: String, qInitBinding: InitialBinding): SolutionList
  /**
   * Common case where we assume the queryModelName is known.
   */
  def queryIndirectForAllSolutions(queryQName: String, qInitBinding: InitialBinding): SolutionList
  /**
   * Assume the queryModelName is known.
   * Assume a single input SPARQL variable binding, containing a URI via Ident.
   */
  def queryIndirectForAllSolutions(queryQName: String, soleSPARQL_VN: String, soleVarIdent: Ident): SolutionList

  def queryIndirectForAllSolutions(queryQName: String, soleSPARQL_VN: String, soleVarQN: String): SolutionList

  /* Assume the queryModelName is known.
	 * Assume a single input binding, which is the target graph. 
	 * Assume the name of that target graph SPARQL variable is known 
	 * (i.e. client will use default/currentvalue, e.g. "qGraph"),
	 * with value passed as an ident.
	 */
  def queryIndirectForAllSolutions(queryQName: String, targetGraphIdent: Ident): SolutionList

  /*
	 * Assume a single input binding, which is the target graph, given in this case by a String QName.  
	 * Assume the name of that SPARQL variable is known (i.e. client will use default/current value, e.g. "qGraph".
	 */
  def queryIndirectForAllSolutions(queryQN: String, targetGraphQN: String): SolutionList

  /**
   * Assume two bindings:  One for the target graph, and one other ident binding.
   * This case is used a lot.
   */
  def queryIndirectForAllSolutions(queryQN: String, targetGraphIdent: Ident, otherSPARQL_VN: String, otherVarIdent: Ident): SolutionList

  /**
   * Assume two bindings:  One for the target graph, and one other URI binding.
   * In this case, both values are supplied using QNames.
   * This case is used a lot.
   */
  def queryIndirectForAllSolutions(queryQN: String, targetGraphQN: String, otherSPARQL_VN: String, otherValQN: String): SolutionList

  /*
	 * Assume a single input binding, which is a string literal.
	 * This form does NOT provide for automatic setting of the qGraph, so it is a little different flavor
	 * from the others here.  Useful?
	 */
  def queryIndirectForAllSolutionsWithStringBinding(queryQName: String, soleVarName: String, soleVarVal: String): SolutionList

  /**
   * Produce an InitialBinding that knows how to bind resources and literals for use in our repo.
   */
  def makeInitialBinding: InitialBinding;

  def assembleRootsFromNamedModel(graphNameIdent: Ident): java.util.Set[Object];

  def assembleRootsFromNamedModel(graphQName: String): java.util.Set[Object];
  
	def assembleRootsFromNamedModel_TX(graphNameIdent: Ident): java.util.Set[Object] = {
		RepoClientFuncs_TxAware.execReadTransCompatible(this, null, new JenaArqQueryFuncs_TxAware.Oper[java.util.Set[Object]] {
			override def perform() :  java.util.Set[Object] = 	assembleRootsFromNamedModel(graphNameIdent)
		});
	}
	def assembleRootsFromNamedModel_TX(graphQName: String): java.util.Set[Object] = {
		RepoClientFuncs_TxAware.execReadTransCompatible(this, null, new JenaArqQueryFuncs_TxAware.Oper[java.util.Set[Object]] {
			override def perform() :  java.util.Set[Object] = 	assembleRootsFromNamedModel(graphQName)
		});
	}
	
}

import org.appdapter.bind.rdf.jena.query.JenaArqQueryFuncs_TxAware;
import org.appdapter.core.store.RepoQueryFuncs_TxAware;
import com.hp.hpl.jena.query.Dataset;
import org.appdapter.core.log.BasicDebugger

object RepoClientFuncs_TxAware extends BasicDebugger {
	def  execReadTransCompatible[RetType](rc : RepoClient, onFailure : RetType, oper : JenaArqQueryFuncs_TxAware.Oper[RetType]) : RetType = {
		val localRepoOrNull = rc.getRepoIfLocal;
		if (localRepoOrNull == null) {
			// For *reads* this is just an info, to be demoted to debug()...
			getLogger().debug("Could not find local repo, so will execute non-trans against presumed REMOTE repo");
		}
		RepoQueryFuncs_TxAware.execReadTransCompatible(localRepoOrNull, onFailure, oper)
	}
	def  execWriteTransCompatible[RetType](rc : RepoClient, onFailure : RetType, oper : JenaArqQueryFuncs_TxAware.Oper[RetType]) : RetType = {
		val localRepoOrNull = rc.getRepoIfLocal;
		if (localRepoOrNull == null) {
			// ...but for *writes*, it's a warn!
			getLogger().warn("Could not find local assumedRepo, so will execute non-trans against presumed REMOTE repo");
		}
		RepoQueryFuncs_TxAware.execWriteTransCompatible(localRepoOrNull, onFailure, oper)
	}	
}