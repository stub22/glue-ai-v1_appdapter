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

package org.appdapter.core.store;

import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;

import com.hp.hpl.jena.shared.PrefixMapping;
import java.util.ArrayList;
import java.util.List;

import org.appdapter.core.log.BasicDebugger;
/**
 * @author Stu B. <www.texpedient.com>
 */

public abstract class BasicQueryProcessorImpl extends BasicDebugger implements QueryProcessor {
	// All the stateless utility features below are now static methods in JenaArqQueryFuncs
	/*
	public Query parseQueryText(String inlineQueryText, PrefixMapping pmap) { 
		String qBaseURI = null;
		Query query = new Query();
		// Query prefixes must be applied before the query is parsed.
		query.setPrefixMapping(pmap);
		QueryFactory.parse(query, inlineQueryText, qBaseURI, Syntax.syntaxSPARQL); 
		return query;
	}
	
	public Query parseQueryURL(String resolvedQueryURL) {
		Query parsedQuery = null;
		try {
			// String resolvedQueryURL = DemoResources.QUERY_PATH;
			// DemoResources.resolveResourcePathToURL_WhichJenaCantUseInCaseOfJarFileRes(DemoResources.QUERY_PATH);
			logInfo("Registering classLoader with JenaFM");  // Because it is used 
			AssemblerUtils.ensureClassLoaderRegisteredWithJenaFM(getClass().getClassLoader());
			parsedQuery = QueryFactory.read(resolvedQueryURL);
		} catch (Throwable t) {
			logError("problem in parseQueryURL", t);
		}
		return parsedQuery;
	}
	public <ResType> ResType processQueryExecution(QueryExecution qe, Repo.ResultSetProc<ResType> resProc) {
		ResType result = null;
		
		try {

			try {
				// ResultSet does not have a close() method
				ResultSet rs = qe.execSelect();
				result = resProc.processResultSet(rs);
			} finally {
				qe.close();
			}
		} catch (Throwable t) {
			logError("problem in QueryTrigger", t);
		}
		return result;
	}
	@Override public <ResType> ResType processDatasetQuery(Dataset ds, Query parsedQuery, QuerySolution initBinding, Repo.ResultSetProc<ResType> resProc) {
		QueryExecution qe = QueryExecutionFactory.create(parsedQuery, ds, initBinding);
		return processQueryExecution(qe, resProc);
	}
	@Override public List<QuerySolution> findAllSolutions(Dataset ds, Query parsedQuery, QuerySolution initBinding) {
		Repo.ResultSetProc<List<QuerySolution>> resProc = new Repo.ResultSetProc<List<QuerySolution>>() {
			@Override public List<QuerySolution> processResultSet(ResultSet rset) {
				List<QuerySolution> solnList = new ArrayList<QuerySolution>();
				while (rset.hasNext()) {
					QuerySolution qsoln = rset.next();
					solnList.add(qsoln);
				}
				return solnList;
			}
		};
		return processDatasetQuery(ds, parsedQuery, initBinding, resProc);
	}	
	public static String dumpResultSetToXML(ResultSet rs) {
		ResultSetRewindable rsr = ResultSetFactory.makeRewindable(rs);
		// Does this print to console in table format? 
		ResultSetFormatter.out(rsr);
		rsr.reset();
		String resultXML = ResultSetFormatter.asXMLString(rsr);
		return resultXML;
	}
	*
	*/ 
}
