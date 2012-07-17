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
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import java.util.List;
import org.appdapter.bind.rdf.jena.query.JenaArqQueryFuncs;
import org.appdapter.bind.rdf.jena.query.JenaArqResultSetProcessor;
/**
 * @author Stu B. <www.texpedient.com>
 */

public abstract class BasicRepoImpl extends BasicQueryProcessorImpl implements Repo {
	private	Dataset myMainQueryDataset;
	
	protected abstract Dataset makeMainQueryDataset();
	
	@Override public Dataset getMainQueryDataset() {
		if (myMainQueryDataset == null) {
			myMainQueryDataset = makeMainQueryDataset();
		}
		return myMainQueryDataset;
	}


	@Override public <ResType> ResType processQuery(Query parsedQuery, QuerySolution initBinding, JenaArqResultSetProcessor<ResType> resProc) {
		ResType result = null;
		try {
			Dataset ds = getMainQueryDataset();
			result = JenaArqQueryFuncs.processDatasetQuery(ds, parsedQuery, initBinding, resProc);
		} catch (Throwable t) {
			logError("problem in processQuery", t);
		}
		return result;
	}
	@Override public List<QuerySolution> findAllSolutions(Query parsedQuery, QuerySolution initBinding) {
		Dataset ds = getMainQueryDataset();
		return JenaArqQueryFuncs.findAllSolutions(ds, parsedQuery, initBinding);
	}
}
