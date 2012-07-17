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
import com.hp.hpl.jena.query.*;
import org.appdapter.bind.rdf.jena.query.JenaArqResultSetProcessor;
/**
 * @author Stu B. <www.texpedient.com>
 * 
 * This interface is not fully absracted away from Jena's types and assumptions, 
 * but it's a start in that direction.
 */

public interface QueryProcessor {
	/*

	public Query parseQueryText(String inlineQueryText, PrefixMapping prefixMapping);


	public Query parseQueryURL(String resolvedQueryURL);

	public static interface ResultSetProc<ResType> {

		public ResType processResultSet(ResultSet rset);
	}

	
	
	public <ResType> ResType processDatasetQuery(Dataset ds, Query parsedQuery, QuerySolution initBinding, Repo.ResultSetProc<ResType> resProc);
	
	public List<QuerySolution> findAllSolutions(Dataset ds, Query parsedQuery, QuerySolution initBinding);
	* 
	*/
	
}
