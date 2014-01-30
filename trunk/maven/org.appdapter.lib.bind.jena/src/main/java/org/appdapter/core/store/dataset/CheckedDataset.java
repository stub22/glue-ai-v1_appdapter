package org.appdapter.core.store.dataset;

import java.util.ArrayList;
import java.util.Iterator;

import org.appdapter.core.store.RepoOper;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import com.hp.hpl.jena.sparql.core.DatasetImpl;

public class CheckedDataset extends DatasetImpl implements Dataset {

	protected Model graph2model(Graph graph)
	{
		return super.graph2model(graph);
	}

	//private DatasetGraph dsg;

	public CheckedDataset() {
		this(DatasetGraphFactory.createMemFixed());
	}

	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode());
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

	boolean realPassedInModels = true;

	@Override public void addNamedModel(String n, Model m) {
		Node gn = RepoDatasetFactory.correctModelName(n);
		offerName(m, n);
		Model innerModel = null;
		boolean cm = containsNamedModel(n);
		if (!cm) {
			if (realPassedInModels) {
				super.addNamedModel(n, m);
				return;
			}
			innerModel = RepoDatasetFactory.createDefaultModel();
		} else {
			innerModel = getNamedModel(n);
			if (innerModel == m)
				return;
		}
		Graph mg = m.getGraph();
		innerModel.add(m);
		innerModel.withDefaultMappings(m);
		RepoDatasetFactory.invalidateModel(m);
		DatasetGraph g = asDatasetGraph();
		g.addGraph(gn, innerModel.getGraph());
	}

	private void offerName(Model m, String n) {
		if (m instanceof CheckedModel) {
			((CheckedModel) m).setName(n);
		}

	}

	@Override public Model getNamedModel(String n) {
		if (n == null)
			return super.getNamedModel(n);
		if (n.equals("#all")) {
			return modelFor(new MultiUnion(RepoOper.getAllGraphs(this)));
		}
		Node gn = RepoDatasetFactory.correctModelName(n);
		DatasetGraph g = asDatasetGraph();
		Graph graph = g.getGraph(gn);
		if (graph == null)
			return null;
		Model m = RepoDatasetFactory.createModelForGraph(graph);
		offerName(m, n);
		return m;
	}

	private Model modelFor(MultiUnion graph) {
		return RepoDatasetFactory.createModelForGraph(graph);
	}

	@Override public void removeNamedModel(String n) {
		Node gn = RepoDatasetFactory.correctModelName(n);
		RepoDatasetFactory.untested("remove named model + n");
		DatasetGraph g = asDatasetGraph();
		g.removeGraph(gn);
	}

	@Override public void replaceNamedModel(String n, Model m) {
		Node gn = RepoDatasetFactory.correctModelName(n);
		Model innerModel = null;
		offerName(m, n);
		boolean cm = containsNamedModel(n);
		if (!cm) {
			if (realPassedInModels) {
				super.replaceNamedModel(n, m);
				return;
			}
			innerModel = RepoDatasetFactory.createDefaultModel();
		} else {
			innerModel = getNamedModel(n);
			if (innerModel == m)
				return;
		}
		if (realPassedInModels) {
			super.replaceNamedModel(n, m);
			return;
		}
		RepoDatasetFactory.untested("replaceNamedModel");
		removeNamedModel(n);
		addNamedModel(n, m);
	}

	@Override public Model getDefaultModel() {
		//	Model m = super.getDefaultModel();
		DatasetGraph g = asDatasetGraph();
		Model m = RepoDatasetFactory.createModelForGraph(g.getDefaultGraph());
		setDefaultModel(m);
		return m;
	}

	@Override public Lock getLock() {
		DatasetGraph g = asDatasetGraph();
		return g.getLock();
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