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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.bind.rdf.jena.query.JenaArqQueryFuncs_TxAware;
import org.appdapter.bind.rdf.jena.query.JenaArqResultSetProcessor;
import org.appdapter.core.name.Ident;
import org.appdapter.core.store.dataset.RepoDatasetFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;


/**
 * 
 * The goal of a BasicRepoImpl is just to provide common dataset-wrapper functionality,
 * needed by the end-consumers of that Dataset (after it's loaded), not to be the pivot of a concurrent 
 * data loading system for arbitrary ways of *creating* datasets.  
 * 
 */

public abstract class BasicRepoImpl extends BasicQueryProcessorImpl implements Repo, DatasetProvider { // , Repo.SharedModels
	private Dataset myMainQueryDataset;

	protected BasicRepoImpl() {
	}
	@Override public boolean isRemote() {
		return false;
	}
	
	protected void setMainQueryDataset(Dataset dset) { 
		myMainQueryDataset = dset;
	}

	@Override public Dataset getMainQueryDataset() {
		if (myMainQueryDataset == null) {
			myMainQueryDataset = makeMainQueryDataset();			
		}
		return myMainQueryDataset;
	}

	// This is not an Override, but may match the mixin  Repo.Updatable 
	public void replaceNamedModel(Ident modelID, Model jenaModel) {
		Dataset repoDset = getMainQueryDataset();
		Lock lock = repoDset.getLock();
		String name = modelID.getAbsUriString();
		name = RepoOper.correctModelName(name);
		try {
			lock.enterCriticalSection(false);
			repoDset.replaceNamedModel(name, jenaModel);
		} finally {
			lock.leaveCriticalSection();
		}
	}

	// A bit like database's addNamedModel (but this is not implmentation of Mutable.. unless a subclass claims it is)
	public void addNamedModel(Ident modelID, Model jenaModel) {
		Dataset repoDset = getMainQueryDataset();
		// DataSource repoDsource = (DataSource) repoDset;
		Lock lock = repoDset.getLock();
		try {
			lock.enterCriticalSection(false);
			String name = modelID.getAbsUriString();
			name = RepoOper.correctModelName(name);
			if (!repoDset.containsNamedModel(name)) {
				repoDset.addNamedModel(name, jenaModel);
			} else {
				Model before = repoDset.getNamedModel(name);
				jenaModel.add(before);
				repoDset.replaceNamedModel(name, jenaModel);
			}
		} finally {
			lock.leaveCriticalSection();
		}
	}

	@Override public Model getNamedModel(Ident modelID, boolean createIfMissing) {
		Dataset repoDset = getMainQueryDataset();
		String name = modelID.getAbsUriString();
		String fixedName = RepoOper.correctModelName(name);
		return repoDset.getNamedModel(fixedName);
	}


	@Override public List<GraphStat> getGraphStats() {
		return RepoQueryFuncs_TxAware.execReadTransCompatible(this, null, new JenaArqQueryFuncs_TxAware.Oper<List<GraphStat>>() {
			@Override public List<GraphStat> perform() {
				return getGraphStats_Raw();
			}
		});	
		
	}
	private List<GraphStat> getGraphStats_Raw() {		
		List<GraphStat> stats = new ArrayList<GraphStat>();
		final Dataset mainDset = getMainQueryDataset();
		Iterator<String> nameIt = mainDset.listNames();
		while (nameIt.hasNext()) {
			final String modelName = nameIt.next();
			Model m = mainDset.getNamedModel(modelName);
			long cnt = m.size();
			Repo.GraphStat gs = new GraphStat(modelName, cnt);
				/*new GetObject<Model>() {
				@Override public Model getValue() {
					return mainDset.getNamedModel(modelName);
				}
			});*/
			stats.add(gs);
		}
		return stats;
	}
/**
 * Uses transactions (existing or new) if appropriate.
 * @param <ResType>
 * @param parsedQuery
 * @param initBinding
 * @param resProc
 * @return 
 */
	@Override public <ResType> ResType processQuery(Query parsedQuery, QuerySolution initBinding, JenaArqResultSetProcessor<ResType> resProc) {
		Dataset ds = getMainQueryDataset();
		return JenaArqQueryFuncs_TxAware.processDatasetQuery_TX(ds, parsedQuery, initBinding, resProc);
	}
/**
 *  * Uses transactions (existing or new) if appropriate.
 * @param parsedQuery
 * @param initBinding
 * @return 
 */
	@Override public List<QuerySolution> findAllSolutions(Query parsedQuery, QuerySolution initBinding) {
		Dataset ds = getMainQueryDataset();
		return  JenaArqQueryFuncs_TxAware.findAllSolutions_TX(ds, parsedQuery, initBinding);
	}
/**
 * Should only be used in a proper transactional context, and hence should not be public.<br/>
 * TODO:  Make this method protected, allow users to ship in a transact to exec against named models.
 * 
 * @param graphNameIdent
 * @return 
 */
	@Override public Model getNamedModel(Ident graphNameIdent) {
		Dataset mqd = getMainQueryDataset();
		String absURI = graphNameIdent.getAbsUriString();
		absURI = RepoOper.correctModelName(absURI);
		return mqd.getNamedModel(absURI);
	}
/**
 * Uses transactions (existing or new), if the underlying dataset is transactional.
 * @param graphNameIdent
 * @return 
 */

	@Override public Set<Object> assembleRootsFromNamedModel(final Ident graphNameIdent) {
		return RepoQueryFuncs_TxAware.execReadTransCompatible(this, null, new JenaArqQueryFuncs_TxAware.Oper<Set<Object>>() {
			@Override public Set<Object> perform() {
				return assembleRootsFromNamedModel_Raw(graphNameIdent);
			}
		});		
	}
	private Set<Object> assembleRootsFromNamedModel_Raw(Ident graphNameIdent) {
		Model loadedModel = getNamedModel(graphNameIdent);
		if (loadedModel == null) {
			getLogger().error("No model found at {}", graphNameIdent);
			// We *could* return an empty set, instead.
			return null;
		}
		Set<Object> results = AssemblerUtils.buildAllRootsInModel(loadedModel);
		return results;
	}
	protected Dataset makeMainQueryDataset() {	
		getLogger().info("Using RepoDatasetFactory.createDefault()");
		Dataset ds = RepoDatasetFactory.createDefault(); // becomes   createMem() in later Jena versions.
		return ds;
	}
	
}
