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
import org.appdapter.core.store.{ Repo}

import org.appdapter.fancy.model.{ModelClientCore, ModelClientImpl}

import org.appdapter.fancy.query.{SolutionList, SolutionHelper}
import org.appdapter.fancy.gportal.{DelegatingPortal}

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * Beyond the capabilities of a dataset, the repo client has these presumed capabilities:
 *		1) Ability to resolve QNames, using some prefix mapping, such as that supplied by a particular model 
 *		(typically presumed to be the "dir" model of the repo)
 *		
 *		2) Ability to look up SPARQL query texts by name, from some presumed Query model.
 *			The queryName supplied may be resolved using process #1 above.
 *			These texts may optionally contain a targetGraph variable for use in #3 below.
 *		   
 *		3) Ability to apply those query texts against a particular graph, by assuming that a well known "target graph"
 *		variable occurs in the query text and can thus be bound.  (Other variables may also be bound).
 *				This "graphVariable" approach is largely equivalent to a SPARQL "FROM NAMED" clause, which we may
 *				switch to at some future point. 
 */

/* Repo.WithDirectory defines these interface methods.
 * 		public Model getDirectoryModel();
 *		public RdfNodeTranslator getDefaultRdfNodeTranslator();
 *		public InitialBinding makeInitialBinding();
 *		public List<QuerySolution> queryIndirectForAllSolutions(Ident qSrcGraphIdent, Ident queryIdent, QuerySolution qInitBinding);
 *		public List<QuerySolution> queryIndirectForAllSolutions(String qSrcGraphQN, String queryQN, QuerySolution qInitBinding);
 *		public List<QuerySolution> queryDirectForAllSolutions(String qText, QuerySolution qInitBinding);
 *	... which are impemented in
*/
class GraphPortalRepoClient (protected val myPortal : DelegatingPortal, val myDfltResGraphID : Ident,  
								dTgtGraphVarName: String, dQrySrcGrphQName:String) 
		extends RepoClientImpl(dTgtGraphVarName, dQrySrcGrphQName) {

	override def getDefaultRdfNodeTranslator: org.appdapter.core.model.RdfNodeTranslator = ???
	override def getNamedModelReadonly(graphID: org.appdapter.core.name.Ident): com.hp.hpl.jena.rdf.model.Model = ???
	
  override def postNamedModel(graphID:  Ident,contents:  Model): Unit = {
	  myPortal.getAbsorber.addStatementsToNamedModel(graphID, contents)
  }
  override def putNamedModel(graphID: Ident,contents: Model): Unit = ???  
  override def clearNamedModel(graphID: Ident): Unit = ???  
  
	override def makeInitialBinding: InitialBinding = getRepoIfLocal.makeInitialBinding 
		//   new InitialBindingImpl(getFallbackRdfNodeTranslator)
	
	override def queryIndirectForAllSolutions(qSrcGraphIdent: Ident, queryIdent: Ident, qInitBinding: InitialBinding): SolutionList = {
		val r = getRepoIfLocal;
		val javaSL = r.queryIndirectForAllSolutions(qSrcGraphIdent, queryIdent, qInitBinding.getQSMap)
 //   val qText = resolveIndirectQueryText(qSrcGraphIdent, queryIdent)
 //   checkQueryText(qText, qSrcGraphIdent, queryIdent, true)
// val javaSL : java.util.List[QuerySolution] =    queryDirectForAllSolutions(qText, qInitBinding)		
		mySH.makeSolutionList(javaSL)
	}
	override def queryIndirectForAllSolutions(qSrcGraphQN: String, queryQName: String, qInitBinding: InitialBinding): SolutionList = {
		val r = getRepoIfLocal;
		val javaSL = r.queryIndirectForAllSolutions(qSrcGraphQN, queryQName, qInitBinding.getQSMap)
		mySH.makeSolutionList(javaSL)
	}  

/*
 *   override def queryDirectForAllSolutions(qText: String, qInitBinding: QuerySolution): java.util.List[QuerySolution] = {
    import scala.collection.immutable.StringOps
    val qTextOps = new StringOps(qText);
    val fixedQTxt = qTextOps.replaceAll("!!", "?") // Remove this as soon as app code is updated
    val parsedQ = parseQueryText(fixedQTxt);

    findAllSolutions(parsedQ, qInitBinding);
  }
 */
	override def assembleRootsFromNamedModel(graphNameIdent: Ident): java.util.Set[Object] = {
		getRepoIfLocal.assembleRootsFromNamedModel(graphNameIdent);  
	}	
}
/*
 *
 *	@Override public List<QuerySolution> findAllSolutions(Query parsedQuery, QuerySolution initBinding) {
		Dataset ds = getMainQueryDataset();
		return  JenaArqQueryFuncs_TxAware.findAllSolutions_TX(ds, parsedQuery, initBinding);
	}
 *
 *  def parseQueryText(queryText: String): Query = {
    val dirModel = getDirectoryModel;
    var ds = getMainQueryDataset
    JenaArqQueryFuncs.parseQueryText(queryText, dirModel);
  }

 *
 *  def resolveIndirectQueryText(querySourceGraphID: Ident, queryID: Ident): String = {
    val ib = makeInitialBinding
    ib.bindIdent(QUERY_QUERY_GRAPH_INPUT_VAR, querySourceGraphID)
    ib.bindIdent(QUERY_QUERY_URI_RESULT_VAR, queryID)
    val qText = resolveIndirectQueryText(ib)
    checkQueryText(qText, querySourceGraphID, queryID, true)
    qText
  }

 *   private def resolveIndirectQueryText(queryResIB: InitialBinding): String = {
    val qInitBinding: QuerySolutionMap = queryResIB.getQSMap

    val parsedQQ: Query = myQueryResQuery

    val possSoln: Option[QuerySolution] = findSingleQuerySolution(parsedQQ, qInitBinding);

    val qText: String = if (possSoln.isDefined) {
      val qSoln = possSoln.get;
      val qtxt_Lit: Literal = qSoln.getLiteral("queryTxt");
      qtxt_Lit.getString()
    } else "";

    qText
  }
  def findSingleQuerySolution(parsedQQ: Query, qInitBinding: QuerySolution): Option[QuerySolution] = {
    val solnJavaList: java.util.List[QuerySolution] = findAllSolutions(parsedQQ, qInitBinding);
    if (solnJavaList.ne(null)) {
      if (solnJavaList.size() == 1) {
        return Some(solnJavaList.get(0))
      }
    }
    None;
  }
  def checkQueryText(qText: String, qSrcGraphQN: Object, queryQN: Object, showStackTrace: Boolean): Unit = {
    if (qText == null || qText.length == 0) {
      val msg = "Unable to find Query Called " + queryQN + " in Model " + qSrcGraphQN;
      val rte = new RuntimeException(msg);
      logError(msg);
      if (showStackTrace) {
        rte.printStackTrace
        // maybe we should just throw now?
        // rather then letting subsequent calls fail with obscure EOFs
      }
    }
  }  
 * 
 */