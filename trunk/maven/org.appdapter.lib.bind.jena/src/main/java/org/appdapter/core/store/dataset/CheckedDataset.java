package org.appdapter.core.store.dataset;

import java.util.ArrayList;
import java.util.Iterator;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import com.hp.hpl.jena.sparql.core.DatasetImpl;

public class CheckedDataset extends DatasetImpl implements Dataset {

	private DatasetGraph dsg;

	public CheckedDataset() {
		this(DatasetGraphFactory.createMemFixed());
	}

	public CheckedDataset(DatasetGraph g) {
		super(g);
		this.dsg = g;
	}

	public CheckedDataset(Dataset g) {
		super(g);
		this.dsg = g.asDatasetGraph();
	}

	@Override public DatasetGraph asDatasetGraph() {
		if (dsg == null)
			return super.asDatasetGraph();
		return dsg;
	}

	@Override public void close() {
		asDatasetGraph().close();
	}

	@Override public boolean containsNamedModel(String n) {
		Node gn = RepoDatasetFactory.correctModelName(n);
		DatasetGraph g = asDatasetGraph();
		boolean contained = g.containsGraph(gn);
		return contained;
	}

	@Override public void addNamedModel(String n, Model m) {
		Node gn = RepoDatasetFactory.correctModelName(n);
		Model innerModel = null;
		if (!containsNamedModel(n)) {
			innerModel = RepoDatasetFactory.createDefaultModel();
		} else {
			innerModel = getNamedModel(n);
		}
		innerModel.add(m);
		RepoDatasetFactory.invalidateModel(m);
		DatasetGraph g = asDatasetGraph();
		g.addGraph(gn, innerModel.getGraph());
	}

	@Override public void removeNamedModel(String n) {
		Node gn = RepoDatasetFactory.correctModelName(n);
		RepoDatasetFactory.untested("remove named model + n");
		DatasetGraph g = asDatasetGraph();
		g.removeGraph(gn);
	}

	@Override public void replaceNamedModel(String n, Model m) {
		RepoDatasetFactory.untested("replaceNamedModel");
		removeNamedModel(n);
		addNamedModel(n, m);
	}

	@Override public Model getDefaultModel() {
		DatasetGraph g = asDatasetGraph();
		return RepoDatasetFactory.createModelForGraph(g.getDefaultGraph());
	}

	@Override public Lock getLock() {
		DatasetGraph g = asDatasetGraph();
		return g.getLock();
	}

	@Override public Model getNamedModel(String n) {
		Node gn = RepoDatasetFactory.correctModelName(n);
		DatasetGraph g = asDatasetGraph();
		Graph graph = g.getGraph(gn);
		if (graph == null)
			return null;
		return RepoDatasetFactory.createModelForGraph(graph);
	}

	@Override public Iterator<String> listNames() {
		ArrayList<String> nameList = new ArrayList<String>();
		DatasetGraph g = asDatasetGraph();
		Iterator<Node> nodeIt = g.listGraphNodes();
		while (nodeIt.hasNext()) {
			Node n = nodeIt.next();
			nameList.add(n.getURI());
		}
		return nameList.iterator();
	}

}