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
import org.appdapter.core.store.{Repo}
import org.appdapter.impl.store.{FancyRepo};

import scala.collection.JavaConversions._

/**
 * @author Ryan Biggs
 * @author Stu B. <www.texpedient.com>
 */

class RepoClientImpl(val myRepo : FancyRepo, val myQueryVarName : String, val myQuerySheetName : String ) extends RepoClient {
  
	private val mySH = new SolutionHelper()
	
	private def ensureRepo {
		// TODO: check repo health, log errors if any problems found
		//if (QueryTester.repo == null) {
		//  QueryTester.repo = QueryTester.loadSheetRepo
		//}
	}

	/** Returns the current cached SheetRepo
	 *
	 * @return The test sheet SheetRepo
	 */
	def getRepo : FancyRepo = {
		ensureRepo
		myRepo
	}
  
	def getQueryVarName = myQueryVarName ; // QueryTester.GRAPH_QUERY_VAR
	
	def getQuerySheetName = myQuerySheetName; // QueryTester.QUERY_SHEET;
  
	
	def queryIndirectForAllSolutions(qSrcGraphQN : String, queryQName: String, qInitBinding: InitialBinding) : SolutionList = {
		val r = getRepo;
		val javaSL = r.queryIndirectForAllSolutions(qSrcGraphQN, queryQName, qInitBinding.getQSMap)
		mySH.makeSolutionList(javaSL)
	}
	def queryIndirectForAllSolutions( queryQName: String, qInitBinding: InitialBinding) : SolutionList = {
		val querySheetQName = getQuerySheetName
		queryIndirectForAllSolutions(querySheetQName, queryQName, qInitBinding)
	}
	def queryIndirectForAllSolutions(queryQName: String, soleVarName : String, soleVarIdent : Ident) : SolutionList	= {
		val r = getRepo;
		val qib = r.makeInitialBinding
		qib.bindIdent(soleVarName, soleVarIdent)
		queryIndirectForAllSolutions(queryQName, qib)	
	}
	def queryIndirectForAllSolutions( queryQName: String, targetGraphIdent : Ident) : SolutionList = {
		val qSoleVarName = getQueryVarName
		queryIndirectForAllSolutions(queryQName,qSoleVarName, targetGraphIdent)
	}
	def queryIndirectForAllSolutions(queryQName: String, soleVarName : String, soleVarLiteralString : String) : SolutionList = {
		val r = getRepo;
		val qib = r.makeInitialBinding		
		qib.bindLiteralString(soleVarName, soleVarLiteralString)
		queryIndirectForAllSolutions(queryQName, qib)
	}
	
	def queryIndirectForAllSolutions(queryQName: String, targetGraphIdent : Ident, otherVarName : String, otherVarIdent : Ident) : SolutionList ={
		val r = getRepo;
		val qib = r.makeInitialBinding
		val tgtGraphVN = getQueryVarName
		qib.bindIdent(tgtGraphVN, targetGraphIdent)
		qib.bindIdent(otherVarName, otherVarIdent)
		queryIndirectForAllSolutions(queryQName, qib)
	}
	
	def makeInitialBinding : InitialBinding = getRepo.makeInitialBinding
}
