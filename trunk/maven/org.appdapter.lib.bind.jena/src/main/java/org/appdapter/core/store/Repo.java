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

import org.appdapter.bind.rdf.jena.query.QueryProcessor;
import org.appdapter.core.share.ShareSpec;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.appdapter.core.jvm.GetObject;
import org.appdapter.core.jvm.SetObject;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.appdapter.core.share.RemoteDatasetProviderSpec;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.Store;

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

	public static class GraphStat extends StatementListener implements Map.Entry<Ident, Model> {

		@Override public Ident getKey() {
			return new FreeIdent(graphURI);
		}

		public GraphStat(String uri, GetObject<Model> mdl) {
			this.graphURI = uri;
			model = mdl;
		}

		public String graphURI;
		private GetObject<Model> model;

		public String toString() {
			return "[GraphStat uri=" + graphURI + ", stmtCnt=" + getStatementCount() + "]";
		}

		@Override public Model getValue() {
			return model.getValue();
		}

		@Override public Model setValue(Model value) {
			Model mdl = model.getValue();
			if (mdl == value)
				return mdl;
			if (model instanceof SetObject) {
				try {
					((SetObject) model).setObject(value);
				} catch (InvocationTargetException e) {
					throw new RuntimeException(e.getCause());
				}
			} else {
				throw new UnsupportedOperationException("Cannot change the model in this graphStat to " + value);
			}
			return mdl;
		}

		public long getStatementCount() {
			return model.getValue().size();
		}

	}

	public static interface Stored extends Repo {

		public Store getStore();

		// public void setStore(Store store);

		// public void mountStoreUsingFileConfig(String storeConfigPath);

	}

	// for loading operations does not claim persistence
	public static interface Updatable extends Repo {

		// this merges the new model into
		public void addNamedModel(Ident modelID, Model model);

		// this is like Add but clears the old first
		public void replaceNamedModel(Ident modelID, Model model);

	}

	public interface DatasetProvider {
		public Dataset getMainQueryDataset();

		public boolean isRemote();

		public Model getNamedModel(Ident remoteModelID, boolean b);
	}

	// for sharing operations
	public static interface SharedModels extends Repo.DatasetProvider {
		/**
		 * @param modelIDs   The Dataset's ModelIDs    such as: taChan_77, taChan_78 ....  null= ALL
		 * @param shareName    "robot01"    (this makes the remote share Effectively  robot01-taChan_77 if there was a global namespace)
		 * @param clearRemote  - upon call this will clear the remote Model
		 * @param clearLocal  - upon call this will clear the local Model
		 * @param mergeAfterClear - after remote or local is cleared there may be data on both ends.. this says to add the theres data to both ends
		 * @param isSharedAfterMerge - set true if the model will now be using what is on the remote end
		 * @param remoteDatasetProviderSpec (new SparqlDatasetProvider("http://localhost:3030/sparql") or .. new SDBDatasetProvider("foo.ttl");  new RepoDatasetProvider(myOtherRepo); new RealDatasetProvider(DatsetFactory.createMem());
		 */
		public void setNamedModelShareType(List<Ident> modelIDs, String shareName, boolean clearRemote, boolean clearLocal, boolean mergeAfterClear, boolean isSharedAfterMerge,
				RemoteDatasetProviderSpec remoteDatasetProviderSpec);

		public void setNamedModelShareType(List<ShareSpec> shareSpecs, RemoteDatasetProviderSpec remoteDatasetProviderSpec);

		public Map<Ident, ShareSpec> getSharedModelSpecs();
	}

	public static interface Mutable extends Repo, Updatable {

		public void importGraphFromURL(String tgtGraphName, String sourceURL, boolean replaceTgtFlag);

		// uploadHomePath is just a UI config helper ... looking for its proper place in java-land
		public String getUploadHomePath();

		public void formatRepoIfNeeded();
	}

	public static interface WithFallbackModelClient extends Repo, ModelClient {
		public ModelClient getFallbackModelClient();
	}

	public static interface WithDirectory extends WithFallbackModelClient, Updatable {
		public Model getDirectoryModel();

		public ModelClient getDirectoryModelClient();

		public InitialBinding makeInitialBinding();

		public void addLoadTask(String str, Runnable r);

		public List<QuerySolution> queryIndirectForAllSolutions(Ident qSrcGraphIdent, Ident queryIdent, QuerySolution qInitBinding);

		public List<QuerySolution> queryIndirectForAllSolutions(String qSrcGraphQN, String queryQN, QuerySolution qInitBinding);

		public List<QuerySolution> queryDirectForAllSolutions(String qText, QuerySolution qInitBinding);

	}
}
