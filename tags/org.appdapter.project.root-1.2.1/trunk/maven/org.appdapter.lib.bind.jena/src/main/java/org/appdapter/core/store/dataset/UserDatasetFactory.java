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

import java.util.List;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.util.FileManager;

/**
 * @author Logicmoo. <www.logicmoo.org>
 *
 * Repo loading in parallel
 * Handling for a local *or* some 'remote'/'shared' model/dataset impl.
 *
 */
public interface UserDatasetFactory {

	/** Create an in-memory, modifiable Dataset */
	public Dataset createMem();

	Dataset createRemotePeer();

	/** Create an in-memory, modifiable Dataset.
	 * New graphs must be explicitly added using .addGraph.
	 */
	public Dataset createMemFixed();

	/** Create a dataset with the given model as the default graph
	 * @param model
	 * @return Dataset
	 */
	public Dataset create(Model model);

	/** Create a dataset
	 * @param dataset
	 * @return Dataset
	 */
	public Dataset create(Dataset dataset);

	/** Wrap a datasetgraph to make a mutable dataset
	 * @param dataset DatasetGraph
	 * @return Dataset
	 */
	public Dataset create(DatasetGraph dataset);

	/** Create a dataset based on a list of URIs : these are merged into the default graph of the dataset.
	 *
	 * @param uriList   URIs merged to form the default dataset
	 * @return Dataset
	 */

	public Dataset create(List<String> uriList);

	/** Create a dataset with a default graph and no named graphs
	 *
	 * @param uri   URIs merged to form the default dataset
	 * @return Dataset
	 */

	public Dataset create(String uri);

	/** Create a dataset based on a list of URIs : these are merged into the default graph of the dataset.
	 *
	 * @param uriList   URIs merged to form the default dataset
	 * @param fileManager
	 * @return Dataset
	 */

	public Dataset create(List<String> uriList, FileManager fileManager);

	public Dataset createDefault();

	public boolean canCreateType(String typeOf, String shareName);

	public Dataset createType(String typeOf, String shareName);

	public boolean canCreateModelOfType(String typeOf, String shareName);

	public Model createModelOfType(String typeOf, String modelName) throws Throwable;

	public Model createModelOfType(String typeOf, String modelName, String shareName);

	public String getDatasetType();

}
