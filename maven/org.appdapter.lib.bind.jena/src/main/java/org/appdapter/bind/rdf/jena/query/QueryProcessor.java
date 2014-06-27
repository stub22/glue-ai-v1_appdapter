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
import java.util.List;

import org.appdapter.bind.rdf.jena.query.JenaArqResultSetProcessor;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
/**
 * @author Stu B. <www.texpedient.com>
 * 
 * This interface is not fully absracted away from Jena's types and assumptions, 
 * but it's a start in that direction.
 */

public interface QueryProcessor {
		/** This query method is the most general, since it does not assume that we want to hold all solutions in
	 * memory in raw form.  Instead it allows us to pass in a solution processor, which presumably reduces
	 * the total amount of memory/objects required for the total required  information content.
	 * 
	 * @param <ResType>
	 * @param parsedQuery
	 * @param initBinding
	 * @param resProc - filter proc to be applied to each result
	 * @return 
	 */
	public <ResType> ResType processQuery(Query parsedQuery, QuerySolution initBinding, JenaArqResultSetProcessor<ResType> resProc);
	
	/**
	 * Pulls  all solutions into memory in raw form, which is simple but not necessarily efficient.
	 * @param parsedQuery
	 * @param initBinding
	 * @return 
	 */
	public List<QuerySolution> findAllSolutions(Query parsedQuery, QuerySolution initBinding);
	
}
