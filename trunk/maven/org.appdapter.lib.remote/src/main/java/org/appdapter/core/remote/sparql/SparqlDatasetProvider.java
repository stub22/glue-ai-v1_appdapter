/*
 *  Copyright 2013 by The Appdapter Project (www.appdapter.org).
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
package org.appdapter.core.remote.sparql;

import java.util.UUID;

import org.apache.jena.atlas.data.ThresholdPolicy;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.store.RepoOper;
import org.appdapter.core.model.StatementSync;
import org.appdapter.core.store.dataset.JenaSDBWrappedDatasetFactory;
import org.appdapter.core.store.dataset.RepoDatasetFactory;
import org.appdapter.core.store.dataset.UserDatasetFactory;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.graph.GraphDistinctDataBag;

public class SparqlDatasetProvider extends JenaSDBWrappedDatasetFactory implements UserDatasetFactory {

	static SparqlDatasetProvider SINGLETON = new SparqlDatasetProvider();
/*
	public static void install() {

		RepoDatasetFactory.DEFAULT = SINGLETON;

		RepoDatasetFactory.globalDS = SINGLETON.createRemotePeer();
		RepoDatasetFactory.alwaysShareDataset = true;
		RepoOper.isMergeDefault = false;
		RepoDatasetFactory.registerDatasetFactory("default", SINGLETON);
		RepoOper.registerDatasetFactory("remote", SINGLETON);
		RepoOper.registerDatasetFactory("shared", SINGLETON);
		RepoOper.registerDatasetFactory("unshared", SINGLETON);
		RepoOper.registerDatasetFactory("memory", SINGLETON);
		RepoOper.registerDatasetFactory("private", SINGLETON);
		RepoOper.registerDatasetFactory("sparql", SINGLETON);
	}
*/
	/** Create a graph that is a Jena memory graph
	 * @see #createDefaultGraph
	 */
	static public SparqlGraph createGraphMem() {
		String newName = createNewName();
		return SparqlDatasetGraph.SINGLETON.createGraph("SparqlMemGraph-" + newName);
	}

	static long serialNumber = 666;

	static public String createNewName() {
		serialNumber++;
		if (true)
			return "S" + serialNumber;
		String newID = UUID.randomUUID().toString();
		String newName = (newID).replace('-', '_');
		return newName;
	}

	/** A graph backed by a DistinctDataBag&lt;Triple&gt;. */
	public Graph createDataBagGraph(ThresholdPolicy<Triple> thresholdPolicy) {
		return new GraphDistinctDataBag(thresholdPolicy);
	}

	public Dataset createRemotePeer() {
		String newID = createNewName();
		return SparqlDatasetGraph.SINGLETON.createDataset("DatasetRemote-" + newID).toDataset();
	}

	public static DatasetGraph asDatasetGraph(Dataset remoteDataset) {
		Debuggable.notImplemented("asDatasetGraph", remoteDataset);
		return remoteDataset.asDatasetGraph();
	}

	public static Model createDefaultModel() {
		return ModelFactory.createModelForGraph(createGraphMem());
	}

	/** Wrap a datasetgraph to make a mutable dataset
	 * @param dataset DatasetGraph
	 * @return Dataset
	 */
	public Dataset create(DatasetGraph dsg) {
		// TODO Auto-generated method stub
		Debuggable.notImplemented("create", this, dsg);
		return com.hp.hpl.jena.query.DatasetFactory.create(dsg);
	}

	public Dataset createMem() {
		String newID = createNewName();
		return SparqlDatasetGraph.SINGLETON.createDataset("Dataset" + newID).toDataset();
	}

	@Override public String getDatasetType() {
		return "sparql";
	}

	/** Create a dataset with the given model as the default graph
	 * @param model
	 * @return Dataset
	 */
	public Dataset create(Model model) {
		Debuggable.notImplemented("create", this, model);
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

	public Dataset createDefault() {
		return createRemotePeer();
	}

	public Dataset create(Dataset peer) {
		Debuggable.notImplemented("create", this, peer);
		return peer;
	}

	@Override public Model createModelOfType(String typeOf, String shareName) throws Throwable {
		Debuggable.notImplemented("createModelOfType", this, typeOf, shareName);
		return createModelOfType(typeOf, null, shareName);
	}

	@Override public Dataset createType(String typeOf, String shareName) {
		return SparqlDatasetGraph.SINGLETON.getRemoteDataset(shareName);
	}

	@Override public Model createModelOfType(String typeOf, String modelName, String shareName) {
		return SparqlDatasetGraph.SINGLETON.createGraph(RepoDatasetFactory.getGlobalName(modelName, shareName)).toModel();
	}
}
