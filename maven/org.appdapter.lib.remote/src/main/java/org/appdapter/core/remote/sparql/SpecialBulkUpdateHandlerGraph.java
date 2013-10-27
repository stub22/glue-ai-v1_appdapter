/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package org.appdapter.core.remote.sparql;

import com.hp.hpl.jena.graph.BulkUpdateHandler;
import com.hp.hpl.jena.graph.Capabilities;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphEventManager;
import com.hp.hpl.jena.graph.GraphStatisticsHandler;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.TransactionHandler;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.shared.AddDeniedException;
import com.hp.hpl.jena.shared.DeleteDeniedException;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import ext.com.hp.hpl.jena.graph.Reifier;
import ext.com.hp.hpl.jena.graph.impl.SimpleReifier;
import ext.com.hp.hpl.jena.graph.query.QueryHandler;
import ext.com.hp.hpl.jena.graph.query.SimpleQueryHandler;

public class SpecialBulkUpdateHandlerGraph extends JenaWrappedGraph implements Graph {

	private Graph g;
	private BulkUpdateHandler b;

	public SpecialBulkUpdateHandlerGraph(Graph g, BulkUpdateHandler b) {
		this.g = g;
		this.b = b;
	}

	@Override public void add(Triple arg0) throws AddDeniedException {
		g.add(arg0);
	}

	@Override public void close() {
		g.close();
	}

	@Override public boolean contains(Node arg0, Node arg1, Node arg2) {
		return g.contains(arg0, arg1, arg2);
	}

	@Override public boolean contains(Triple arg0) {
		return g.contains(arg0);
	}

	@Override public void delete(Triple arg0) throws DeleteDeniedException {
		g.delete(arg0);
	}

	@Override public boolean dependsOn(Graph arg0) {
		return g.dependsOn(arg0);
	}

	@Override public ExtendedIterator<Triple> find(Node arg0, Node arg1, Node arg2) {
		return g.find(arg0, arg1, arg2);
	}

	@Override public ExtendedIterator<Triple> find(TripleMatch arg0) {
		return g.find(arg0);
	}

	@Override public BulkUpdateHandler getBulkUpdateHandler() {
		return b;
	}

	@Override public Capabilities getCapabilities() {
		return g.getCapabilities();
	}

	@Override public GraphEventManager getEventManager() {
		return g.getEventManager();
	}

	@Override public PrefixMapping getPrefixMapping() {
		return g.getPrefixMapping();
	}

	@Override public Reifier getReifier() {
		return SimpleReifier.findOrCreate(g);
	}

	@Override public GraphStatisticsHandler getStatisticsHandler() {
		return g.getStatisticsHandler();
	}

	@Override public TransactionHandler getTransactionHandler() {
		return g.getTransactionHandler();
	}

	@Override public boolean isClosed() {
		return g.isClosed();
	}

	@Override public boolean isEmpty() {
		return g.isEmpty();
	}

	@Override public boolean isIsomorphicWith(Graph arg0) {
		return g.isIsomorphicWith(arg0);
	}

	@Override public QueryHandler queryHandler() {
		return SimpleQueryHandler.findOrCreate(g);
	}

	@Override public int size() {
		return g.size();
	}

}
