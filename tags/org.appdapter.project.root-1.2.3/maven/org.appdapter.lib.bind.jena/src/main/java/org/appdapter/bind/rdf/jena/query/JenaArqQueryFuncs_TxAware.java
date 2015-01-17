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

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ReadWrite;

import com.hp.hpl.jena.query.QuerySolution;

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
	
	public static interface Oper<RT> {
		public	RT	perform();
	}
	/**
	 * 
	 * @param <RetType>
	 * @param ds - dataset supporting transactions
	 * @param privs
	 * @param onFailure
	 * @param oper
	 * @return 
	 */
	public static <RetType> RetType execBracketedTrans(Dataset ds, ReadWrite privs, RetType onFailure, Oper<RetType> oper) {
		RetType result = onFailure;
		try {
			ds.begin(privs);
			result = oper.perform();
			ds.commit(); // Same effect as abort unless we promoted to Write, somehow. 
		} catch (Throwable t) {
			theLogger.error("Caught error during transactional op of type " + privs + ", aborting", t);
			ds.abort();
		} finally {
			ds.end();   // Superfluous, as far as we know.
		}
		return result;			
	}
	
	public static <RetType> RetType execBracketedReadTrans(Dataset ds, RetType onFailure, Oper<RetType> oper) {
		return execBracketedTrans(ds, ReadWrite.READ, onFailure, oper);
	}
	public static <RetType> RetType execBracketedWriteTrans(Dataset ds, RetType onFailure, Oper<RetType> oper) {
		return execBracketedTrans(ds, ReadWrite.WRITE, onFailure, oper);
	}
	public static <RetType> RetType execTransCompatible(Dataset ds, ReadWrite privs, RetType onFailure, Oper<RetType> oper) {
		RetType result = onFailure;
		Boolean supportsTrans = (ds != null) && ds.supportsTransactions();
		Boolean alreadyInTrans = null;
		if (supportsTrans) {
			alreadyInTrans = ds.isInTransaction();
		}
		if (supportsTrans  && (!alreadyInTrans)) {
			theLogger.debug("Bracketing for TRANSACTIONAL {} on dataset {}", privs, ds);
			result = execBracketedTrans(ds, privs, onFailure, oper);
		} else {
			theLogger.debug("Performing UN-bracketed {} on dataset {} (where null implies 'remote' implies nontrans), supportsTrans={}, alreadyInTrans={}", privs, ds,
					supportsTrans, alreadyInTrans);
			try {
				result = oper.perform();
			} catch (Throwable t) {
				theLogger.error("Caught error during NON-transactional " + privs, t);
			}
		}
		return result;
	}
	public static <RetType> RetType execReadTransCompatible(Dataset ds, RetType onFailure, Oper<RetType> oper) {
		return execTransCompatible(ds, ReadWrite.READ, onFailure, oper);
	}
	public static <RetType> RetType execWriteTransCompatible(Dataset ds, RetType onFailure, Oper<RetType> oper) {
		return execTransCompatible(ds, ReadWrite.WRITE, onFailure, oper);
	}	
	public static List<QuerySolution> findAllSolutions_TX(final Dataset ds, final Query parsedQuery, final QuerySolution initBinding) {
		List<QuerySolution> solns = null;
		solns = execReadTransCompatible(ds, null, new Oper<List<QuerySolution>>() {
			@Override public List<QuerySolution> perform() {
				return JenaArqQueryFuncs.findAllSolutions(ds, parsedQuery, initBinding);
			}
		});
		return solns;
	}
	public static <ResType> ResType processDatasetQuery_TX(final Dataset ds, final Query parsedQuery, 
			final QuerySolution initBinding, final JenaArqResultSetProcessor<ResType> resProc) {
		ResType res = null;
		res = execReadTransCompatible(ds, null, new Oper<ResType>() {
			@Override public ResType perform() {
				return JenaArqQueryFuncs.processDatasetQuery(ds, parsedQuery, initBinding, resProc);
			}
		});
		return res;
	}

}
