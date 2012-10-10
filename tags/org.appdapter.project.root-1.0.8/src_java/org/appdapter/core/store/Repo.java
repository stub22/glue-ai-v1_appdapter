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
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.Store;
import java.util.List;
import java.util.Set;
import org.appdapter.core.name.Ident;
import com.hp.hpl.jena.query.QuerySolution;

/**
 * @author Stu B. <www.texpedient.com>
 */
public interface Repo extends QueryProcessor {

	/**
	 * Access an arbitrary "main" Jena-ARQ dataset 
	 * @return 
	 */
	public Dataset getMainQueryDataset();
	
	/**
	 * Get summary information about the graphs in this repo.
	 * @return 
	 */
	public List<GraphStat> getGraphStats();
	
	/**
	 * Get named graph 
	 * @param graphNameIdent
	 * @return 
	 */
	public Model getNamedModel (Ident graphNameIdent);
	/**
	 * 
	 * @param graphNameIdent
	 * @return 
	 */
	public Set<Object> assembleRootsFromNamedModel(Ident graphNameIdent);

	public static class GraphStat {

		public String graphURI;
		public long statementCount;
		public String toString() { 
			return "[GraphStat uri=" + graphURI + ", stmtCnt=" + statementCount + "]";
		}
	}

	public static interface Stored extends Repo {

		public Store getStore();

		public void setStore(Store store);

		public void mountStoreUsingFileConfig(String storeConfigPath);
		
	}
	public static interface Mutable extends Repo {
		public void addNamedModel(Ident modelID, Model model) ;
		public void replaceNamedModel(Ident modelID, Model model) ;
		
		public void importGraphFromURL(String tgtGraphName, String sourceURL, boolean replaceTgtFlag);

		// uploadHomePath is just a UI config helper ... looking for its proper place in java-land
		public String getUploadHomePath();

		public void formatRepoIfNeeded();
	}
	public static interface WithFallbackModelClient extends Repo, ModelClient {
		public ModelClient getFallbackModelClient();
	}
	public static interface WithDirectory extends WithFallbackModelClient {
		public Model getDirectoryModel();
		public ModelClient getDirectoryModelClient();
		public InitialBinding makeInitialBinding();
		
		public List<QuerySolution> queryIndirectForAllSolutions( Ident qSrcGraphIdent, Ident queryIdent, QuerySolution qInitBinding ) ;
		public List<QuerySolution> queryIndirectForAllSolutions( String qSrcGraphQN, String queryQN, QuerySolution qInitBinding ) ;
		public List<QuerySolution> queryDirectForAllSolutions( String qText, QuerySolution qInitBinding);
	}
}
