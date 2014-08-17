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

package org.appdapter.fancy.gportal

import com.hp.hpl.jena.query.{Query, QueryExecution, QueryExecutionFactory, Dataset, ResultSet, QuerySolution}
import org.appdapter.bind.rdf.jena.query.JenaArqQueryFuncs_TxAware.Oper
import org.appdapter.bind.rdf.jena.query.{JenaArqResultSetProcessor, JenaArqQueryFuncs}

/**
 * @author Stu B. <www.texpedient.com>
 */

trait GraphQuerier extends GraphPortal {
	// We eschew initialBinding approach (even for local queries), because it doesn't work with remote datasets.
	// (You can still call setInitialBinding on this returned QueryExecution, but it won't work (throws exception?)
	// if the exec is for a remote service.   Preferred approach to binding is now to modify the query itself before 
	// creating this execution.    That can be done programmatically through Query API or through 
	// ParameterizedSparqlString.   See also the VALUES clause in SPARQL.
	// http://article.gmane.org/gmane.comp.apache.jena.user/2534/match=values+block
	def makeQueryExec(query : Query) : QueryExecution
	// This closes the queryExec, leaving it not reusable
	protected def execSelectQuery_ReadTransCompatible[ResType](queryExec : QueryExecution, rProc : JenaArqResultSetProcessor[ResType], onFail : ResType ) : ResType = {
		val op = new Oper[ResType] {
			override def perform(): ResType = {
				// Javadoc shows this method, but can't find it in any recent Jena versions.
				// if (queryExec.isClosed()) {
				// 	throw new Exception("Cannot use a QueryExecution that has been closed.")
				// }
				val arqResultSet : ResultSet = queryExec.execSelect();
				val res : ResType = rProc.processResultSet(arqResultSet)
				queryExec.close()
				res
			}
		}
		execReadTransCompatible(op, onFail)
	}
	import scala.collection.JavaConversions._
	protected def gulpingSelect_ReadTransCompatible(queryExec : QueryExecution) : List[QuerySolution] = {
		val gulpingProc = JenaArqQueryFuncs.makeResultGulpingProc();
		val jList : java.util.List[QuerySolution] = execSelectQuery_ReadTransCompatible(queryExec, gulpingProc, null)
		jList.toList
	}
	
}
// See also   org.apache.jena.riot.system.PrefixMap, PrefixMapFactory
import com.hp.hpl.jena.shared.{PrefixMapping}

object QueryParseHelper {
	def parseQuery(qtxt : String) : Query = {
		val noPrefixMapping : PrefixMapping = null
		val parsedQuery : Query = JenaArqQueryFuncs.parseQueryText(qtxt, noPrefixMapping)	
		parsedQuery
	}	
}

trait LocalGraphQuerier extends GraphQuerier with LocalGraphPortal {

	override def makeQueryExec(query : Query) : QueryExecution = {
		val dset = getLocalDataset
		makeLocalQueryExec(dset, query)
	}
	def makeLocalQueryExec(dset : Dataset, query : Query) : QueryExecution = {
		QueryExecutionFactory.create(query, dset)
	}
	
}
trait RemoteGraphQuerier extends GraphQuerier with RemoteGraphPortal {
	
	override def makeQueryExec(query : Query) : QueryExecution = {
		val svcUrl = getRemoteQueryServiceURL
		makeRemoteQueryExec(svcUrl, query)
	}
	def makeRemoteQueryExec(serviceURL : String, query : Query) : QueryExecution = {
		QueryExecutionFactory.sparqlService(serviceURL, query)
	}
}


/*
 * http://answers.semanticweb.com/questions/17106/can-multiple-values-blocks-appear-in-same-graph-pattern
 *
 * Each VALUES block is a separate data table that is combined, by join, with all the other patterns in the group. 
 * two VALUES are allowed - they are separate data tables. In your special case (one row each, no shared variables) 
 * they are are the same but in general they are not.

VALUES ?x { :A, :B } VALUES ?y { :Z } would be VALUES (?x ?y) { (:A :Z) (:B :Z) }

VALUES ?x { :A } VALUES ?x { :Z } would be VALUES ?x  { } i.e. no rows

VALUES ?x { :A } VALUES ?x { :A } would be VALUES ?x  { :A } i.e. one rows
 */	
