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

package org.appdapter.bind.rdf.jena.query;

import java.util.List;
import org.appdapter.core.log.BasicDebugger;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ReadWrite;
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
import org.appdapter.bind.rdf.jena.model.JenaModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Stu B. <www.texpedient.com>
 *  These methods should work for all 3 of these cases:
	 *	1) Datset does not support transactions.
	 *  2) A transaction is already underway.
	 *  3) A transaction needs to be started.
 */


public class JenaArqQueryFuncs_TxAware {
	private static Logger theLogger = LoggerFactory.getLogger(JenaArqQueryFuncs_TxAware.class);
	
	public static List<QuerySolution> findAllSolutions_TX(Dataset ds, Query parsedQuery, QuerySolution initBinding) {
		List<QuerySolution> solns = null;
		Boolean supportsTrans = ds.supportsTransactions();
		Boolean alreadyInTrans = null;
		if (supportsTrans) {
			alreadyInTrans = ds.isInTransaction();
		}
		if (supportsTrans  && (!alreadyInTrans)) {
			try {
				theLogger.info("Bracketing for TRANSACTIONAL read on dataset {}", ds);
				ds.begin(ReadWrite.READ);
				solns = JenaArqQueryFuncs.findAllSolutions(ds, parsedQuery, initBinding);
				ds.commit(); // Same effect as abort unless we promoted to Write (which shouldn't happen in this case)
			} catch (Throwable t) {
				theLogger.error("Caught error during transactional query, aborting", t);
				ds.abort();
			} 
		} else {
			theLogger.info("Performing unbracketed read on dataset {}, supportsTrans={}, alreadyInTrans={}", ds, supportsTrans, alreadyInTrans);
			// If isInTransaction, then we don't need to worry about starting, committing, or rollback - 
			// so we can act just like (and share code with) a non-TX-aware consumer!
			solns = JenaArqQueryFuncs.findAllSolutions(ds, parsedQuery, initBinding);
		}
		return solns;
	}
	
	public static <ResType> ResType processDatasetQuery_TX(Dataset ds, Query parsedQuery, 
			QuerySolution initBinding, JenaArqResultSetProcessor<ResType> resProc) {
		ResType res = null;
		List<QuerySolution> solns = null;
		Boolean supportsTrans = ds.supportsTransactions();
		Boolean alreadyInTrans = null;
		if (supportsTrans) {
			alreadyInTrans = ds.isInTransaction();
		}	
		if (supportsTrans  && (!alreadyInTrans)) {
			theLogger.info("Bracketing for TRANSACTIONAL read on dataset {}", ds);
			try {
				ds.begin(ReadWrite.READ);
				res = JenaArqQueryFuncs.processDatasetQuery(ds, parsedQuery, initBinding, resProc);
				ds.commit(); // Same effect as abort unless we promoted to Write (which shouldn't happen in this case)
			} catch (Throwable t) {
				theLogger.error("Caught error during transactional query, aborting", t);
				ds.abort();
			} 
		} else {
			theLogger.info("Performing unbracketed read on dataset {}, supportsTrans={}, alreadyInTrans={}", ds, supportsTrans, alreadyInTrans);
			// If isInTransaction, then we don't need to worry about starting, committing, or rollback - 
			// so we can act just like (and share code with) a non-TX-aware consumer!
			res = JenaArqQueryFuncs.processDatasetQuery(ds, parsedQuery, initBinding, resProc);
		}
		return res;
	}

}
