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

import org.appdapter.core.name.Ident

import org.appdapter.impl.store.{FancyRepo};

trait RepoClient {
  
	/** Returns the current cached FancyRepo
	 *
	 * @return The test sheet FancyRepo
	 */
	def getRepo: FancyRepo
	
	/**
	 * The most general case
	 */
	def queryIndirectForAllSolutions(queryModelQName : String, queryQName: String, qInitBinding: InitialBinding) : SolutionList
	/**
	 * More common case where we assume the queryModelName is known.
	 */
	def queryIndirectForAllSolutions(queryQName: String, qInitBinding: InitialBinding) : SolutionList
	/**
	 * Assume a single input binding, which is an Ident.
	 */
	def queryIndirectForAllSolutions(queryQName: String, soleVarName : String, soleVarIdent : Ident) : SolutionList
	/*
	 * Assume a single input binding, which is the target graph.  Assume the name of that variable is known.
	 */
	def queryIndirectForAllSolutions(queryQName: String, targetGraphIdent : Ident) : SolutionList
	/*
	 * Assume a single input binding, which is a string literal.
	 */
	def queryIndirectForAllSolutions(queryQName: String, soleVarName : String, soleVarVal : String) : SolutionList
	/**
	 * Assume two bindings:  One for the target graph, and one other ident binding.
	 * This case is used a lot.
	 */
	def queryIndirectForAllSolutions(queryQName: String, targetGraphIdent : Ident, otherVarName : String, otherVarIdent : Ident) : SolutionList
	/** 
	 * Produce an InitialBinding that knows how to bind resources and literals for use in our repo.
	 */
	def makeInitialBinding : InitialBinding;
}
