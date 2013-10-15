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

import org.appdapter.api.trigger.GetObject;
import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.bind.rdf.jena.query.JenaArqQueryFuncs;
import org.appdapter.bind.rdf.jena.query.JenaArqResultSetProcessor;
import org.appdapter.core.name.Ident;

// Removed in ARQ 2.9.3 - "Use Dataset" import com.hp.hpl.jena.query.DataSource;
// http://svn.apache.org/repos/asf/jena/trunk/jena-arq/ReleaseNotes.txt
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.Lock;

/**
 * @author Stu B. <www.texpedient.com>
 */

public abstract class BasicRepoImpl extends BasicQueryProcessorImpl implements Repo {

	protected SpecialRepoLoader repoLoader;

	protected BasicRepoImpl() {
		repoLoader = new SpecialRepoLoader(this);
	}

	Model repoEvents;

	public Model getEventsModel() {
		if (repoEvents == null) {
			repoEvents = ModelFactory.createDefaultModel();
			//repoEvents.add(getDirectoryModel());
		}
		return repoEvents;
	}

	protected boolean isUpdatedFromDirModel = false;

	public void addLoadTask(String nym, Runnable r) {
		repoLoader.addTask(nym, r);
	}

	public void addLoadTask(String nym, Runnable r, boolean forGround) {
		if (forGround) {
			r.run();
			return;
		}
		repoLoader.addTask(nym, r);
	}

	private Dataset myMainQueryDataset;
	final public Object loadingLock = new Object();
	public boolean isLoadingStarted = false;
	public boolean isLoadingLocked = false;

	public void replaceNamedModel(Ident modelID, Model jenaModel) {
		Dataset repoDset = getMainQueryDataset();
		Lock lock = repoDset.getLock();
		try {
			lock.enterCriticalSection(false);
			repoDset.replaceNamedModel(modelID.getAbsUriString(), jenaModel);
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

	protected Dataset makeMainQueryDataset() {
		Dataset ds = RepoDatasetFactory.createDefault(); // becomes   createMem() in later Jena versions.
		return ds;
	}

	public void setMyMainQueryDataset(Dataset myMainQueryDataset) {
		this.myMainQueryDataset = myMainQueryDataset;
	}

	@Override public Dataset getMainQueryDataset() {
		beginLoading();
		finishLoading();
		if (myMainQueryDataset == null) {
			myMainQueryDataset = makeMainQueryDataset();
		}
		return myMainQueryDataset;
	}

	// this will make sure that loadingLock is locked so all callers that want to use the repo will need to call finishLoading();
	// (getMainQueryDataset you'll notice calls finishLoading() )
	final public void beginLoading() {
		if (isLoadingStarted || isLoadingLocked)
			return;
		synchronized (this.loadingLock) {
			isLoadingStarted = true;
			isLoadingLocked = true;
			addLoadTask("directoryModel", new Runnable() {
				@Override public void run() {
					repoLoader.setSynchronous(false);
					synchronized (loadingLock) {
						callLoadingInLock();
					}
					isLoadingLocked = false;
					repoLoader.setLastJobSubmitted();
					repoLoader.waitUntilLastJobComplete();
				}
			}, true);
		}
	}

	final public void beginLoadingOldWay() {
		if (isLoadingStarted || isLoadingLocked)
			return;
		synchronized (this.loadingLock) {
			isLoadingStarted = true;
			isLoadingLocked = true;
			(new Thread("Loading " + this) {
				@Override public void run() {
					synchronized (loadingLock) {
						callLoadingInLock();
					}
					isLoadingLocked = false;
				}
			}).start();
		}
	}

	// this is meant to be overridden optionally
	public void callLoadingInLock() {

	}

	// call this and it will block untill load is complete
	final public void finishLoading() {
		synchronized (this.loadingLock) {
			if (false)
				return;
		}
		repoLoader.waitUntilLastJobComplete();
	}

	@Override public List<GraphStat> getGraphStats() {
		List<GraphStat> stats = new ArrayList<GraphStat>();
		final Dataset mainDset = getMainQueryDataset();
		Iterator<String> nameIt = mainDset.listNames();
		while (nameIt.hasNext()) {
			final String modelName = nameIt.next();
			Repo.GraphStat gs = new GraphStat(modelName, new GetObject<Model>() {
				@Override public Model getValue() {
					return mainDset.getNamedModel(modelName);
				}
			});
			stats.add(gs);
		}
		return stats;
	}

	@Override public <ResType> ResType processQuery(Query parsedQuery, QuerySolution initBinding, JenaArqResultSetProcessor<ResType> resProc) {
		ResType result = null;
		try {
			Dataset ds = getMainQueryDataset();
			result = JenaArqQueryFuncs.processDatasetQuery(ds, parsedQuery, initBinding, resProc);
		} catch (Throwable t) {
			getLogger().error("problem in processQuery [{}]", parsedQuery, t);
		}
		return result;
	}

	@Override public List<QuerySolution> findAllSolutions(Query parsedQuery, QuerySolution initBinding) {
		Dataset ds = getMainQueryDataset();
		return JenaArqQueryFuncs.findAllSolutions(ds, parsedQuery, initBinding);
	}

	@Override public Model getNamedModel(Ident graphNameIdent) {
		Dataset mqd = getMainQueryDataset();
		String absURI = graphNameIdent.getAbsUriString();
		return mqd.getNamedModel(absURI);
	}

	@Override public Set<Object> assembleRootsFromNamedModel(Ident graphNameIdent) {
		Model loadedModel = getNamedModel(graphNameIdent);
		if (loadedModel == null) {
			getLogger().error("No model found at {}", graphNameIdent);
			// We *could* return an empty set, instead.
			return null;
		}
		Set<Object> results = AssemblerUtils.buildAllRootsInModel(loadedModel);
		return results;
	}
}
