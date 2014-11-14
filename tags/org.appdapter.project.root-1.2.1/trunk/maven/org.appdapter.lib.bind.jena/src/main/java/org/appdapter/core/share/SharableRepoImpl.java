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

package org.appdapter.core.share;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.appdapter.core.loader.SpecialRepoLoader;
import org.appdapter.core.name.Ident;

import org.appdapter.core.store.RepoOper;
import org.appdapter.core.store.dataset.RepoDatasetFactory;
import org.appdapter.core.store.dataset.UserDatasetFactory;
import org.appdapter.core.loader.AgnosticRepoImpl;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;

/**
 */

public abstract class SharableRepoImpl extends AgnosticRepoImpl implements SharedModels {
	

	public enum TaskState {
		TaskNeedsStart, TaskStarting, TaskClearingCaches, TaskMerging, TaskBranching, TaskComplete, TaskPretasks
	}

	protected final Map<Ident, ShareSpec> sharedModelSpecs = new HashMap<Ident, ShareSpec>();

	protected SharableRepoImpl(SpecialRepoLoader srepoLoader) {
		super(srepoLoader);
	}

	protected SharableRepoImpl() {
		super();
	}
	protected SpecialRepoLoader getRepoLoader() {
		return mySpecialRepoLoader;
	}

	@Override public boolean isRemote() {
		return false;
	}
	

	private String getShareName() {
		return RepoDatasetFactory.DATASET_SHARE_NAME;
	}
	@Override public Model getNamedModel(Ident modelID, boolean createIfMissing) {
		Dataset repoDset = getMainQueryDataset();

		Lock lock = repoDset.getLock();
		String name = modelID.getAbsUriString();
		name = RepoOper.correctModelName(name);
		Model jenaModel = null;
		try {
			lock.enterCriticalSection(false);
			if (!repoDset.containsNamedModel(name)) {
				// This step is necessary iff the repo was created as "in-mem fixed"?
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
	// TODO:  Rename to indicate "share"
	protected Model createLocalNamedModel(Ident modelID) {
		String name = modelID.getAbsUriString();
		name = RepoOper.correctModelName(name);
		return RepoDatasetFactory.createModel(getDatasetType(), name, getShareName());
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


}
