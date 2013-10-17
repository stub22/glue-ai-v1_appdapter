package org.appdapter.core.store;

import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.appdapter.core.store.BasicRepoImpl.TaskState;
import org.appdapter.core.store.dataset.RemoteDatasetProvider;
import org.appdapter.core.store.dataset.RemoteDatasetProviderSpec;
import org.appdapter.core.store.dataset.RepoDatasetFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;

public class ShareSpecImpl implements Runnable, ShareSpec {
	private TaskState taskState;
	private boolean isUnsharedAfterMerge;

	/**
	 * @param modelIDs   The Dataset's ModelIDs    such as: taChan_77, taChan_78 ....  null= ALL
	 * @param shareName    "robot01"    (this makes the remote share Effectively  robot01-taChan_77 if there was a global namespace)
	 * @param clearRemote  - upon call this will clear the remote Model
	 * @param clearLocal  - upon call this will clear the local Model
	 * @param mergeAfterClear - after remote or local is cleared there may be data on both ends.. this says to add the there is data to both ends
	 * @param isSharedAfterMerge - set true if the model will now be using what is on the remote end
	 * @param remoteSpec (new SparqlDatasetProvider("http://localhost:3030/sparql") or .. new SDBDatasetProvider("foo.ttl");  new RepoDatasetProvider(myOtherRepo); new RealDatasetProvider(DatsetFactory.createMem());

	 * @param taskStarted  true = task doesn't need to be started
	 * @param taskComplete  true = post-merge state
	 */
	public ShareSpecImpl(Ident modelID, String shareName, boolean clearRemote, boolean clearLocal, boolean mergedAfterClear, boolean isSharedAfterMerge, RemoteDatasetProviderSpec remoteSpec,
			TaskState taskState) {
		this.localModelID = modelID;
		this.shareName = shareName;
		this.clearRemote = clearRemote;
		this.clearLocal = clearLocal;
		this.mergedAfterClear = mergedAfterClear;
		this.isSharedAfterMerge = isSharedAfterMerge;
		this.isUnsharedAfterMerge = !isSharedAfterMerge;
		this.remoteSpec = remoteSpec;
		this.taskState = taskState;
	}

	Ident localModelID;
	private String shareName;
	boolean clearRemote;
	boolean clearLocal;
	boolean mergedAfterClear;
	boolean isSharedAfterMerge;
	RemoteDatasetProviderSpec remoteSpec;
	Runnable nextDoRunnable = null;
	Runnable beforeDoRunnable = null;
	private Model localModel;
	private Model remoteModel;
	private Ident remoteModelID;
	private RemoteDatasetProvider localSpec;

	public Runnable requiredWork(BasicRepoImpl basicStoredMutableRepoImpl, ShareSpec goalShareSpec, RemoteDatasetProviderSpec remoteDatasetProvider) {
		if (goalShareSpec.getTaskState() != TaskState.TaskNeedsStart)
			return null;
		if (goalShareSpec.sameOutcome(this)) {
			if (taskState != TaskState.TaskNeedsStart) {
				//working on goal
				return null;
			}
		}
		// request this to be ran via run();
		return this;
	}

	public boolean sameOutcome(ShareSpec shareSpec0) {
		if (shareSpec0 == null)
			return false;
		ShareSpecImpl shareSpec = (ShareSpecImpl) shareSpec0;
		if (shareSpec.isSharedAfterMerge != isSharedAfterMerge)
			return false;
		if (shareSpec.remoteSpec != remoteSpec)
			return false;
		if (shareSpec.mergedAfterClear != mergedAfterClear)
			return false;
		if (shareSpec.clearLocal != clearLocal)
			return false;
		if (shareSpec.clearRemote != clearRemote)
			return false;
		if (!getGlobalName().equals(shareSpec.getGlobalName())) {
			return false;
		}
		return true;
	}

	@Override public void run() {
		if (taskState != TaskState.TaskNeedsStart)
			return;
		taskState = TaskState.TaskStarting;
		if (beforeDoRunnable != null) {
			taskState = TaskState.TaskPretasks;
			beforeDoRunnable.run();
		}
		try {
			taskState = TaskState.TaskClearingCaches;
			if (clearLocal) {
				this.localModel = getLocalModel(false);
				if (localModel != null)
					localModel.removeAll();
			}

			if (clearRemote) {
				this.remoteModel = getRemoteModel(false);
				if (remoteModel != null)
					remoteModel.removeAll();
			}

			if (mergedAfterClear) {
				StatementSync.syncTwoModels(getLocalModel(true), getRemoteModel(true));
			}
			if (isSharedAfterMerge) {
				StatementSync.syncTwoModels(getLocalModel(true), getRemoteModel(true));
			}
			if (nextDoRunnable != null) {
				taskState = TaskState.TaskBranching;
				nextDoRunnable.run();
			}

		} finally {
			taskState = TaskState.TaskComplete;
		}
	}

	public String getGlobalName() {
		String plString = getProviderBase();
		return plString + "-" + shareName + "@" + localModelID.getAbsUriString();
	}

	public String getProviderBase() {
		if (remoteSpec == null)
			return "local";
		return remoteSpec.getProviderBase();
	}

	public Model getLocalModel(boolean createIfMissing) {
		String modelUriString = getLocalURI();
		if (this.localModel != null) {
			Dataset lds = getLocalDataset();
			this.localModel = lds.getNamedModel(modelUriString);
			if (localModel != null) {
				return localModel;
			}
			if (createIfMissing) {
				localModel = localSpec.getNamedModel(localModelID, createIfMissing);
			}
		}
		return localModel;

	}

	public String getLocalURI() {
		return localModelID.getAbsUriString();
	}

	public Dataset getLocalDataset() {
		return localSpec.getMainQueryDataset();
	}

	public Model createEmptyLocalModel() {
		return localSpec.getNamedModel(localModelID, true);
	}

	public String getDatasetType(Dataset localDataset) {
		return RepoDatasetFactory.getDatasetType(localDataset);
	}

	public Model getRemoteModel(boolean createIfMissing) {
		String modelUriString = getRemoteURI();
		if (this.remoteModel != null) {
			Dataset lds = getRemoteDataset();
			this.remoteModel = lds.getNamedModel(modelUriString);
			if (remoteModel != null) {
				return remoteModel;
			}
			if (createIfMissing) {
				remoteModel = createEmptyRemoteModel(remoteModelID);
			}
		}
		return remoteModel;

	}

	public String getRemoteURI() {
		return remoteModelID.getAbsUriString();
	}

	public Dataset getRemoteDataset() {
		return getRemoteDatasetProvider().getRemoteDataset(shareName);
	}

	private RemoteDatasetProvider getRemoteDatasetProvider() {
		return remoteSpec.getRemoteDatasetProvider();
	}

	public Model createEmptyRemoteModel(Ident remoteModelID) {
		String dsType = getDatasetType(getRemoteDataset());
		return getRemoteDatasetProvider().getNamedModel(new FreeIdent(remoteModelID), true);
	}

	@Override public TaskState getTaskState() {
		return taskState;
	}

	@Override public Ident getLocalModelId() {
		return localModelID;
	}

}