/*
 *  Copyright 2014 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.core.loader;

import org.appdapter.core.name.Ident;
import org.appdapter.core.store.BasicRepoImpl;
import org.appdapter.core.store.RepoOper;
import org.appdapter.core.store.dataset.RepoDatasetFactory;
import org.appdapter.core.store.dataset.UserDatasetFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;

/**

 */

public abstract class LoadingRepoImpl extends BasicRepoImpl {
	protected SpecialRepoLoader mySpecialRepoLoader;	
	protected LoadingRepoImpl(SpecialRepoLoader srepoLoader) {
		super();
		mySpecialRepoLoader = srepoLoader;
	}

	protected LoadingRepoImpl() {
		super();
		mySpecialRepoLoader = new SpecialRepoLoader(this);
		mySpecialRepoLoader.setSingleThreaded(loadSingleThread);
	}
	protected SpecialRepoLoader getRepoLoader() {
		return mySpecialRepoLoader;
	}
	private static boolean LOAD_SINGLE_THREADED = true;
	private boolean loadSingleThread = LOAD_SINGLE_THREADED;

	
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
	/*
	@Override public Dataset getMainQueryDataset() {
		beginLoading();
		if (myMainQueryDataset == null) {
			myMainQueryDataset = makeMainQueryDataset();			
		}
		if (!isLoadingFinished) {
			finishLoading();
		}
		return myMainQueryDataset;
	}	
	*/
	
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
