package org.appdapter.core.store;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphUtil;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphQuad;
import com.hp.hpl.jena.sparql.core.Quad;

abstract public class DatasetGraphQuadProc extends DatasetGraphQuad implements DatasetGraph {

	@Override public void addGraph(Node graphName, Graph graph) {
		Graph g = getGraph(graphName);
		GraphUtil.addInto(g, graph);
	}

	@Override public void removeGraph(Node graphName) {
		deleteAny(graphName, Node.ANY, Node.ANY, Node.ANY);
	}

	@Override public void setDefaultGraph(Graph g) {
		throw new UnsupportedOperationException("DatasetGraph.setDefaultGraph");
	}

	@Override public void add(Quad quad) {
		throw new UnsupportedOperationException("DatasetGraph.add(Quad)");
	}

	@Override public void delete(Quad quad) {
		throw new UnsupportedOperationException("DatasetGraph.delete(Quad)");
	}

	@Override public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
		// TODO Auto-generated method stub
		if (true)
			throw new UnsupportedOperationException("DatasetGraph...");
		return null;
	}

	@Override public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
		return findNG(g, s, p, o);
	}
	/*
		@Override public Graph getDefaultGraph() {
			// TODO Auto-generated method stub
			if (true)
				throw new UnsupportedOperationException("DatasetGraph...");
			return null;
		}

		@Override public Graph getGraph(Node graphNode) {
			// TODO Auto-generated method stub
			if (true)
				throw new UnsupportedOperationException("DatasetGraph...");
			return null;
		}*/
}
