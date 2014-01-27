package org.appdapter.core.store.dataset;

import org.appdapter.core.log.Debuggable;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;

public class CheckedModel extends ModelCom implements Model {
	private final Graph modelGraph;

	public CheckedModel(CheckedGraph modelGraph) {
		super(modelGraph);
		this.modelGraph = getGraphNoRemove().modelGraph;
	}

	public CheckedModel(Graph modelGraph, boolean makeNonAdd, boolean makeNonDelete, boolean makeNameSpaceChecked) {
		this(CheckedGraph.ensure(modelGraph, makeNonAdd, makeNonDelete, makeNameSpaceChecked));
	}

	private CheckedGraph getGraphNoRemove() {
		return (CheckedGraph) getGraph();

	}

	@Override public Resource createResource(String uri) {
		uri = RepoDatasetFactory.fixURI(uri);
		Resource r = super.createResource(uri);
		return r;
	}

	@Override public Resource getResource(String uri) {
		uri = RepoDatasetFactory.fixURI(uri);
		Resource r = super.getResource(uri);
		return r;
	}

	@Override public void close() {
		modelGraph.close();
	}

	@Override public boolean isClosed() {
		return modelGraph.isClosed();
	}

	@Override public boolean isEmpty() {
		return modelGraph.isEmpty();
	}

	@Override public long size() {
		return modelGraph.size();
	}
}