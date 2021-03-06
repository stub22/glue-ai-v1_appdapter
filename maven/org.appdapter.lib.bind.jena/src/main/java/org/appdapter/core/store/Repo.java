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

import org.appdapter.core.model.RdfNodeTranslator;
import org.appdapter.core.query.InitialBinding;
import org.appdapter.bind.rdf.jena.query.QueryProcessor;
import org.appdapter.core.share.ShareSpec;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.appdapter.core.name.Ident;


import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QuerySolution;

import com.hp.hpl.jena.rdf.model.Model;


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
	 * Get named graph as Jena "Model" object, for direct API access.
	 * @param graphNameIdent
	 * @return
	 */
	public Model getNamedModel(Ident graphNameIdent);

	/**
	 * Use the Jena "assembler" vocabulary to build a set of objects from a given model.
	 * @param graphNameIdent
	 * @return
	 */
	public Set<Object> assembleRootsFromNamedModel(Ident graphNameIdent);

	/* The GraphStat is a very simple piece of return data with no live pointers */
	public static class GraphStat {

		public GraphStat(String gURI, long stmtCnt) {
			graphURI = gURI;
			statementCount = stmtCnt;
		}
		public String graphURI;
		public long statementCount;
		public String toString() { 
			return "[GraphStat uri=" + graphURI + ", stmtCnt=" + statementCount + "]";
		}
		public long getStatementCount() { 
			return statementCount;
		}
	}

	// for loading operations does not claim persistence
	public static interface Updatable extends Repo {

		// this merges the new model into
		public void addNamedModel(Ident modelID, Model model);

		// this is like Add but clears the old first
		public void replaceNamedModel(Ident modelID, Model model);

	}

	public static interface Mutable extends Repo, Updatable {

		public void importGraphFromURL(String tgtGraphName, String sourceURL, boolean replaceTgtFlag);

		// uploadHomePath is just a UI config helper ... looking for its proper place in java-land
		public String getUploadHomePath();

		public void formatRepoIfNeeded();
	}

	public static interface WithFallbackRdfNodeTranslator extends Repo, RdfNodeTranslator {
		public RdfNodeTranslator getFallbackRdfNodeTranslator();
	}

	public static interface WithDirectory extends WithFallbackRdfNodeTranslator, Updatable {
		public Model getDirectoryModel();

		public RdfNodeTranslator getDefaultRdfNodeTranslator();

		public InitialBinding makeInitialBinding();

		public List<QuerySolution> queryIndirectForAllSolutions(Ident qSrcGraphIdent, Ident queryIdent, QuerySolution qInitBinding);

		public List<QuerySolution> queryIndirectForAllSolutions(String qSrcGraphQN, String queryQN, QuerySolution qInitBinding);

		public List<QuerySolution> queryDirectForAllSolutions(String qText, QuerySolution qInitBinding);

	}
	// 	public void addLoadTask(String str, Runnable r);
}
