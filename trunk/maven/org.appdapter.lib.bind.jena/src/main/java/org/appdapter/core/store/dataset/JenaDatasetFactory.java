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

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Logicmoo. <www.logicmoo.org>
 * 
 *         Handling for a local *or* some 'remote'/'shared' model/dataset impls.
 * 
 */
public class JenaDatasetFactory extends AbstractDatasetFactory implements UserDatasetFactory {
	private static Logger theLogger = LoggerFactory.getLogger(JenaDatasetFactory.class);
	@Override public String getDatasetType() {
		return RepoDatasetFactory.DFF_Memory; // "memory";
	}

	@Override public Dataset createDefault() {
		return createMemFixed();
	}

	/** Create an in-memory, modifiable Dataset */
	@Override public Dataset createMem() {
		Dataset dset = create(DatasetGraphFactory.createMem());
		theLogger.warn("Dataset created with DatasetGraphFactory.createMem(), supportsTransactions={}", dset.supportsTransactions());
		return dset;
	}

	/**
	 * Create an in-memory, modifiable Dataset. New graphs must be explicitly added using .addGraph.
	 */
	@Override public Dataset createMemFixed() {
		Dataset dset = create(DatasetGraphFactory.createMemFixed());
		theLogger.warn("Dataset created with DatasetGraphFactory.createMemFixed(), supportsTransactions={}", dset.supportsTransactions());
		return dset;
	}

	private Dataset createMemFixedDS() {
		Dataset dset = create(DatasetFactory.createMemFixed());
		return dset;
	}

	/**
	 * Create a dataset with the given model as the default graph
	 * 
	 * @param model
	 * @return Dataset
	 */
	@Override public Dataset create(Model model) {
		return new DatasetImpl(model);
	}

	/**
	 * Create a dataset
	 * 
	 * @param dataset
	 * @return Dataset
	 */
	@Override public Dataset create(Dataset dataset) {
		if (RepoDatasetFactory.datasetNoDeleteModels) {
			Dataset dset = new CheckedDataset(dataset);
			theLogger.warn("CheckedDataset created as wrapper, supportsTransactions={}", dset.supportsTransactions());
			return dset;
		}
		return new DatasetImpl(dataset);
	}

	/**
	 * Wrap a datasetgraph to make a mutable dataset
	 * 
	 * @param dataset
	 *            DatasetGraph
	 * @return Dataset
	 */
	@Override public Dataset create(DatasetGraph dataset) {
		if (RepoDatasetFactory.datasetNoDeleteModels) {
			Dataset dset = new CheckedDataset(dataset);
			theLogger.warn("CheckedDataset created as wrapper, supportsTransactions={}", dset.supportsTransactions());
			return dset;
		}
		return DatasetImpl.wrap(dataset);
	}

	@Override public Dataset createRemotePeer() {
		Store store = SDBFactory.connectStore(RepoDatasetFactory.STORE_CONFIG_PATH);
		Dataset ds = SDBFactory.connectDataset(store);
		return ds;
	}

	@Override public Dataset createType(String typeOf, String sharedNameIgnoredPresently) {
		return createDefault();
	}

	@Override public Model createModelOfType(String typeOf, String sharedNameIgnoredPresently) throws Throwable {
		return createModelOfType(typeOf, null, sharedNameIgnoredPresently);
	}

	@Override public Model createModelOfType(String typeOf, String modelName, String shareName) {
		return RepoDatasetFactory.createDefaultModel();
	}

}