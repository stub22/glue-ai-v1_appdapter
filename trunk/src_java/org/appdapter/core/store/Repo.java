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

import java.util.List;
import com.hp.hpl.jena.sdb.Store;
import java.util.List;
import org.appdapter.core.store.Repo;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

/**
 * @author Stu B. <www.texpedient.com>
 */
public interface Repo extends QueryProcessor {

	public Dataset getMainQueryDataset();
	
	public List<GraphStat> getGraphStats();

	public static class GraphStat {

		public String graphURI;

		public long statementCount;
	}

	public static interface Stored extends Repo {

		public Store getStore();

		public void setStore(Store store);

		public void mountStoreUsingFileConfig(String storeConfigPath);
		
	}
	public static interface Mutable extends Repo {

		public void importGraphFromURL(String tgtGraphName, String sourceURL, boolean replaceTgtFlag);

		// uploadHomePath is just a UI config helper ... looking for its proper place in java-land
		public String getUploadHomePath();

		public void formatRepoIfNeeded();
	}
}
