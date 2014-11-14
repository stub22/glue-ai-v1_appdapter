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

package org.appdapter.bind.rdf.jena.query;

import java.util.ArrayList;
import java.util.List;

import org.appdapter.bind.rdf.jena.model.JenaFileManagerUtils;
import org.appdapter.core.log.BasicDebugger;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class JenaArqQueryFuncs {
	public static BasicDebugger theDbg = new BasicDebugger();

	/**
	 * 
	 * @param inlineQueryText - query text to be parsed
	 * @param prefixMapping - set of extra RDF namespace abbreviations available to the query text (note that any
	 *			Jena Model can be used as a PrefixMapping)
	 * @return - a parsed Jena ARQ query, ready for execution.
	 */
	public static Query parseQueryText(String inlineQueryText, PrefixMapping pmap) {
		String qBaseURI = null;
		Query query = new Query();
		// Query prefixes must be applied before the query is parsed.
		if (pmap != null) {
			query.setPrefixMapping(pmap);
		}
		try {
			QueryFactory.parse(query, inlineQueryText, qBaseURI, Syntax.syntaxSPARQL);
			return query;
		} catch (Throwable t) {
			t.printStackTrace();
			return query;
		}
	}

	/**
	 * 
	 * @param resolvedQueryURL - URL to the query text
	 * @return  - parsed Jena ARQ query, ready for execution.
	 */
	public static Query parseQueryURL(String resolvedQueryURL, ClassLoader optResourceCL) {
		Query parsedQuery = null;
		try {
			// String resolvedQueryURL = DemoResources.QUERY_PATH;
			// DemoResources.resolveResourcePathToURL_WhichJenaCantUseInCaseOfJarFileRes(DemoResources.QUERY_PATH);
			//   JenaArqQueryFuncs.class.getClassLoader()
			if (optResourceCL != null) {
				theDbg.logInfo("Registering classLoader for this package with JenaFM");
				JenaFileManagerUtils.ensureClassLoaderRegisteredWithDefaultJenaFM(optResourceCL);
			}
			parsedQuery = QueryFactory.read(resolvedQueryURL);
		} catch (Throwable t) {
			theDbg.logError("problem in parseQueryURL", t);
		}
		return parsedQuery;
	}

	protected static <ResType> ResType processQueryExecution(QueryExecution qe, JenaArqResultSetProcessor<ResType> resProc) {
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
			theDbg.logError("problem in processQueryExecution", t);
		}
		return result;
	}

	protected static <ResType> ResType processDatasetQuery(Dataset ds, Query parsedQuery, QuerySolution initBinding, JenaArqResultSetProcessor<ResType> resProc) {
		QueryExecution qe = QueryExecutionFactory.create(parsedQuery, ds, initBinding);
		return processQueryExecution(qe, resProc);
	}

	/**
	 * Now marked protected, because we want user to access through _TxAware variant.
	 * @param ds
	 * @param parsedQuery
	 * @param initBinding
	 * @return 
	 */
	protected static List<QuerySolution> findAllSolutions(Dataset ds, Query parsedQuery, QuerySolution initBinding) {
		JenaArqResultSetProcessor<List<QuerySolution>> resProc = makeResultGulpingProc();
		return processDatasetQuery(ds, parsedQuery, initBinding, resProc);
	}
	public static JenaArqResultSetProcessor<List<QuerySolution>> makeResultGulpingProc() {
		JenaArqResultSetProcessor<List<QuerySolution>> resProc = new JenaArqResultSetProcessor<List<QuerySolution>>() {
			@Override public List<QuerySolution> processResultSet(ResultSet rset) {
				List<QuerySolution> solnList = new ArrayList<QuerySolution>();
				while (rset.hasNext()) {
					QuerySolution qsoln = rset.next();
					solnList.add(qsoln);
				}
				return solnList;
			}
		};
		return resProc;
	}
	public static String dumpResultSetToXML(ResultSet rs) {
		ResultSetRewindable rsr = ResultSetFactory.makeRewindable(rs);
		// Does this print to console in table format? 
		ResultSetFormatter.out(rsr);
		rsr.reset();
		String resultXML = ResultSetFormatter.asXMLString(rsr);
		return resultXML;
	}
}
