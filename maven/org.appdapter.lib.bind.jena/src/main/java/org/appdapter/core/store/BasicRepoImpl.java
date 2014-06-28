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

import org.appdapter.core.share.ShareSpec;
import org.appdapter.core.share.ShareSpecImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.bind.rdf.jena.query.JenaArqQueryFuncs_TxAware;
import org.appdapter.bind.rdf.jena.query.JenaArqResultSetProcessor;
import org.appdapter.core.jvm.GetObject;
import org.appdapter.core.name.Ident;
import org.appdapter.core.share.RemoteDatasetProviderSpec;
import org.appdapter.core.store.dataset.RepoDatasetFactory;
import org.appdapter.core.loader.SpecialRepoLoader;
import org.appdapter.core.store.dataset.UserDatasetFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;
// Removed in ARQ 2.9.3 - "Use Dataset" import com.hp.hpl.jena.query.DataSource;
// http://svn.apache.org/repos/asf/jena/trunk/jena-arq/ReleaseNotes.txt

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * TODO:
 * The goal of a BasicRepoImpl is just to provide common dataset-wrapper functionality,
 * needed by the end-consumers of that Dataset (after it's loaded), not to be the pivot of a concurrent 
 * data loading system for arbitrary ways of *creating* datasets.  We need to move
 * all that loading/locking state into separate classes.
 * 
 * The SharedModels-impl part should be at least one level further down the inheritance tree.
 */

public abstract class BasicRepoImpl extends BasicQueryProcessorImpl implements Repo, Repo.SharedModels, Repo.DatasetProvider {
	private Dataset myMainQueryDataset;

	protected BasicRepoImpl(SpecialRepoLoader srepoLoader) {
		mySpecialRepoLoader = srepoLoader;
	}

	protected BasicRepoImpl() {
		mySpecialRepoLoader = new SpecialRepoLoader(this);
		mySpecialRepoLoader.setSingleThreaded(loadSingleThread);
	}
	
	protected void setMainQueryDataset(Dataset dset) { 
		myMainQueryDataset = dset;
	}

	@Override public Dataset getMainQueryDataset() {
		/* This is far too sophisticated an impl for BasicRepoImpl.
		 * It should be quite possible for me to treat any wrapped dataset as an impl.
		 * But here, we embed the unavoidable assumption that there "may" always be loading and locking,
		 * hidden behind the basic public "getDataset" method.
		 */
		beginLoading();
		if (myMainQueryDataset == null) {
			myMainQueryDataset = makeMainQueryDataset();			
		}
		if (!isLoadingFinished) {
			finishLoading();
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
		// DataSource repoDsource = (DataSource) repoDset;
		Lock lock = repoDset.getLock();
		String name = modelID.getAbsUriString();
		name = RepoOper.correctModelName(name);
		Model jenaModel = null;
		try {
			lock.enterCriticalSection(false);
			if (!repoDset.containsNamedModel(name)) {
				jenaModel = createLocalNamedModel(modelID);
				repoDset.addNamedModel(name, jenaModel);
			} else {
				jenaModel = repoDset.getNamedModel(name);
			}
		} finally {
			lock.leaveCriticalSection();
		}
		return jenaModel;
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
/**
 * Uses transactions (existing or new) if appropriate.
 * @param <ResType>
 * @param parsedQuery
 * @param initBinding
 * @param resProc
 * @return 
 */
	@Override public <ResType> ResType processQuery(Query parsedQuery, QuerySolution initBinding, JenaArqResultSetProcessor<ResType> resProc) {
		ResType result = null;
		try {
			Dataset ds = getMainQueryDataset();
			// result = JenaArqQueryFuncs.processDatasetQuery(ds, parsedQuery, initBinding, resProc);
			result = JenaArqQueryFuncs_TxAware.processDatasetQuery_TX(ds, parsedQuery, initBinding, resProc);
		} catch (Throwable t) {
			getLogger().error("problem in processQuery [{}]", parsedQuery, t);
		}
		return result;
	}
/**
 *  * Uses transactions (existing or new) if appropriate.
 * @param parsedQuery
 * @param initBinding
 * @return 
 */
	@Override public List<QuerySolution> findAllSolutions(Query parsedQuery, QuerySolution initBinding) {
		Dataset ds = getMainQueryDataset();
		// return JenaArqQueryFuncs.findAllSolutions(ds, parsedQuery, initBinding);
		// Using a transaction in this read protects us from concurrent writes (if those writes are properly transactional).
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
 * This *can* be done in a proper xactional context.
 * TODO:  Make it happen.
 * @param graphNameIdent
 * @return 
 */
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
/*  Above here is actual BasicRepoImpl, below here is not.
 * ************************************************************************************
 * Graph construction features - Presumed factory wrappers - move out of base-impl.
 * 
 * TODO:  These belong out a layer - they are not "BasicRepoImpl" concepts.
 */	
	protected Model createLocalNamedModel(Ident modelID) {
		String name = modelID.getAbsUriString();
		name = RepoOper.correctModelName(name);
		return RepoDatasetFactory.createModel(getDatasetType(), name, getShareName());
	}

	private String getShareName() {
		return RepoDatasetFactory.DATASET_SHARE_NAME;
	}

	protected String getDatasetType() {
		if (myDatasetProvider != null)
			return myDatasetProvider.getDatasetType();
		return datasetType;
	}

	protected Dataset makeMainQueryDataset() {
		if (myDatasetProvider != null) {
			getLogger().info("Using datasetProvider {}", myDatasetProvider);
			return myDatasetProvider.createDefault();
		}
		if (datasetType != null) {
			getLogger().warn("Would use datasetType ?{} if we knew how, but we don't, and the datasetProvider is null!", datasetType);
			// Stu found this dead code layin here, would produce NPE every time, right?
			// return datasetProvider.createDefault();
		}
		getLogger().info("Using RepoDatasetFactory.createDefault()", myDatasetProvider);
		Dataset ds = RepoDatasetFactory.createDefault(); // becomes   createMem() in later Jena versions.
		return ds;
	}

	
	/**** TODO - move these sharing features (and the "implements Repo.SharedModels" designation
	 * into a subclass (or stackable trait) - SharingRepoImpl or somesuch.
	 * 
	 */
	public enum TaskState {
		TaskNeedsStart, TaskStarting, TaskClearingCaches, TaskMerging, TaskBranching, TaskComplete, TaskPretasks
	}

	protected final Map<Ident, ShareSpec> sharedModelSpecs = new HashMap<Ident, ShareSpec>();

	@Override public boolean isRemote() {
		return false;
	}

	public void setNamedModelShareType(List<Ident> modelIDs, String shareName, boolean clearRemote, boolean clearLocal, boolean mergeAfterClear, boolean isSharedAfterMerge,
			RemoteDatasetProviderSpec remoteDatasetProviderSpec) {
		List<ShareSpec> shareSpecs = new ArrayList<ShareSpec>();
		for (Ident modelId : modelIDs) {
			shareSpecs.add(new ShareSpecImpl(modelId, shareName, clearRemote, clearLocal, mergeAfterClear, isSharedAfterMerge, remoteDatasetProviderSpec, TaskState.TaskNeedsStart));
		}
		setNamedModelShareType(shareSpecs, remoteDatasetProviderSpec.getRemoteDatasetProvider());
	}

	public void setNamedModelShareType(List<ShareSpec> shareSpecs, RemoteDatasetProviderSpec remoteDatasetProvider) {
		finishLoading();
		Map<Ident, ShareSpec> prev = getSharedModelSpecs();
		for (ShareSpec shareSpec : shareSpecs) {
			ShareSpec shareSpecBefore = prev.get(shareSpec.getLocalModelId());
			Runnable work = shareSpecBefore.requiredWork(this, shareSpec, remoteDatasetProvider);
			if (work == null)
				continue;
			addLoadTask("setNamedModelShareType " + work, work);
		}
		finishLoading();
	}

	public Map<Ident, ShareSpec> getSharedModelSpecs() {
		return this.sharedModelSpecs;
	}

	/* This loader-task stuff is probably useful, but locating it here obscures the intent of the BasicRepoImpl.
	 * TODO:  Move it out into adapter/oper traits.
	 * Ration: oper is repo backwards, and loader is an oper.
	 */
	private static boolean LOAD_SINGLE_THREADED = true;
	private boolean loadSingleThread = LOAD_SINGLE_THREADED;
	private SpecialRepoLoader mySpecialRepoLoader;

	protected SpecialRepoLoader getRepoLoader() {
		return mySpecialRepoLoader;
	}

	
	private Object loadingLock = new Object();
	private boolean isLoadingStarted = false;
	private boolean isLoadingLocked = false;
	private boolean isLoadingFinished = false;
	private boolean myFlag_updatedFromDirModel = false;
	
	// What is the semantic meaning of this condition, used in DirectRepo toString()?
	protected boolean isLoadingLockedOrNotStarted() {
		return (isLoadingLocked || !isLoadingStarted);
	}
	protected boolean isUpdatedFromDirModel() { 
		return myFlag_updatedFromDirModel;
	}
	protected void setUpdatedFromDirModel(boolean updatedFlag) { 
		myFlag_updatedFromDirModel = updatedFlag;
	}
	public void addLoadTask(String nym, Runnable r) { // used from SpecialRepoLoader
		final SpecialRepoLoader repoLoader = getRepoLoader();
		repoLoader.addTask(nym, r);
	}
	private void addLoadTask(String nym, Runnable r, boolean forGround) {
		if (forGround) {
			r.run();
			return;
		}
		final SpecialRepoLoader repoLoader = getRepoLoader();
		repoLoader.addTask(nym, r);
	}
	
	
	// this will make sure that loadingLock is locked so all callers that want to use the repo will need to call finishLoading();
	// (getMainQueryDataset you'll notice calls finishLoading() )
	final public void beginLoading() {
		if (isLoadingStarted || isLoadingLocked)
			return;
		synchronized (this.loadingLock) {
			isLoadingStarted = true;
			isLoadingLocked = true;
			isLoadingFinished = false;
			addLoadTask("beginLoading", new Runnable() {
				@Override public void run() {
					final SpecialRepoLoader repoLoader = getRepoLoader();
					repoLoader.setSynchronous(false);
					synchronized (loadingLock) {
						// must submit all loading jobs now at least
						callLoadingInLock();
					}
					isLoadingLocked = false;
					repoLoader.setLastJobSubmitted();
					repoLoader.waitUntilLastJobComplete();
				}
			}, true);
		}
	}

	// this is meant to be overridden optionally
	abstract public void callLoadingInLock();

	// call this and it will block untill load is complete
	final public void finishLoading() {
		synchronized (this.loadingLock) {
			// this block is the block on the lock
			if (false)
				return;
		}
		final SpecialRepoLoader repoLoader = getRepoLoader();
		repoLoader.waitUntilLastJobComplete();
		isLoadingFinished = true;
	}	
	
	
	private UserDatasetFactory myDatasetProvider = RepoDatasetFactory.getDefaultUserDF();
	private String datasetType;
	private	Model myRepoEvents;

	/**
	 * Called from SpecialRepoLoader, in pursuit of maximum entropy!
	 * @return 
	 */
	public Model getEventsModel() {
		if (myRepoEvents == null) {
			getLogger().info("Creating repoEventsModel for repo {}", this);
			myRepoEvents = RepoDatasetFactory.createDefaultModel();
			//repoEvents.add(getDirectoryModel());
		}
		return myRepoEvents;
	}	
}
