/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.org).
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
package org.appdapter.core.store.dataset;

import org.appdapter.core.store.StatementSync;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetImpl;

/**
 * @author Logicmoo. <www.logicmoo.org>
 *
 * Handling for a local *or* some 'remote'/'shared' model/dataset impls.
 *
 */
public class JenaSDBWrappedDatasetFactory extends AbstractDatasetFactory implements UserDatasetFactory {

	/** Create a dataset with the given model as the default graph
	 * @param model
	 * @return Dataset
	 */
	public Dataset create(Model model) {
		Dataset remote = createRemotePeer();

		Model remoteModel = remote.getDefaultModel();
		if (remoteModel != null) {
			StatementSync.syncTwoModels(remoteModel, model);
		} else {
			remoteModel = model;
			remote.setDefaultModel(model);
		}
		return remote;
	}

	/** Wrap a datasetgraph to make a mutable dataset
	 * @param dataset DatasetGraph
	 * @return Dataset
	 */
	public Dataset create(DatasetGraph dataset) {
		return DatasetImpl.wrap(dataset);
	}

	@Override public String getDatasetType() {
		return "instance";
	}

	public Dataset createDefault() {
		if (true)
			return createRemotePeer();
		return create(createMem());
	}

	public Dataset create(Dataset peer) {
		Dataset remote = createRemotePeer();
		RepoDatasetFactory.addDatasetSync(peer, remote);
		return peer;
	}

	@Override public Dataset createType(String typeOf, String sharedNameIgnoredPresently) {
		return createDefault();
	}

	@Override public Dataset createRemotePeer() {
		Store store = SDBFactory.connectStore(RepoDatasetFactory.STORE_CONFIG_PATH);
		Dataset ds = SDBFactory.connectDataset(store);
		return ds;
	}

	@Override public Model createModelOfType(String typeOf, String sharedNameIgnoredPresently) throws Throwable {
		return createModelOfType(typeOf, null, sharedNameIgnoredPresently);
	}

	@Override public Model createModelOfType(String typeOf, String modelName, String shareName) {
		Store store = SDBFactory.connectStore(RepoDatasetFactory.STORE_CONFIG_PATH);
		return SDBFactory.connectNamedModel(store, RepoDatasetFactory.getGlobalName(modelName , shareName));
	}

}