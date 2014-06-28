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

import org.appdapter.bind.rdf.jena.query.JenaArqQueryFuncs_TxAware
import org.appdapter.core.name.Ident
import org.appdapter.core.store.{InitialBinding, ModelClient, Repo}

// import org.appdapter.core.repo.{FancyRepo};

// maybe trait RepoClientScala extends RepoClientJava {
trait RepoClient extends ModelClient {

  /**
   * @return currently cached Repo.WithDirectory
   */
  def getRepo: Repo.WithDirectory

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
		val assumedRepo = rc.getRepo;
		if (assumedRepo != null) {
			RepoQueryFuncs_TxAware.execReadTransCompatible(assumedRepo, onFailure, oper)
		} else {
			getLogger().warn("Could not find assumedRepo - remote repoClient?");
			onFailure;
		}
	}
	def  execWriteTransCompatible[RetType](rc : RepoClient, onFailure : RetType, oper : JenaArqQueryFuncs_TxAware.Oper[RetType]) : RetType = {
		val assumedRepo = rc.getRepo;
		if (assumedRepo != null) {
			RepoQueryFuncs_TxAware.execWriteTransCompatible(assumedRepo, onFailure, oper)
		} else {
			getLogger().warn("Could not find assumedRepo - remote repoClient?");
			onFailure;
		}
	}	
}