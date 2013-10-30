package org.appdapter.core.remote.sparql;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphEvents;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.shared.DeleteDeniedException;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import ext.com.hp.hpl.jena.graph.Reifier;
import ext.com.hp.hpl.jena.graph.impl.SimpleReifier;
import ext.com.hp.hpl.jena.graph.query.QueryHandler;
import ext.com.hp.hpl.jena.graph.query.SimpleQueryHandler;

abstract public class JenaWrappedGraph implements Graph {

	@Override public void clear() {

		ExtendedIterator<Triple> allTriples = getMatches(Node.ANY, Node.ANY, Node.ANY);
		for (Triple t : allTriples.toList()) {
			this.delete(t);
		}
		getEventManager().notifyEvent(this, GraphEvents.removeAll);
	}

	@Override public void remove(Node s, Node p, Node o) {
		ExtendedIterator<Triple> allTriples = find(s, p, o);
		for (Triple t : allTriples.toList()) {
			this.delete(t);
		}
		getEventManager().notifyEvent(this, GraphEvents.remove(s, p, o));
	}

	public ExtendedIterator<Triple> getMatches(Node s, Node p, Node o) {
		return find(s, p, o);
	}

	protected QueryHandler queryHandler;
	protected Reifier reifier = null;// new EmptyReifier(this);

	public QueryHandler queryHandler() {
		if (queryHandler == null) {
			queryHandler = SimpleQueryHandler.findOrCreate(this);
		}
		return queryHandler;
	}

	public Reifier getReifier() {
		if (reifier == null) {
			reifier = SimpleReifier.findOrCreate(this, ReificationStyle.Standard);
		}
		return reifier;
	}

	abstract @Override public void delete(Triple t) throws DeleteDeniedException;

	abstract @Override public ExtendedIterator<Triple> find(Node s, Node p, Node o);
	/*
	@Override public boolean dependsOn(Graph other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override public TransactionHandler getTransactionHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override @Deprecated public BulkUpdateHandler getBulkUpdateHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public Capabilities getCapabilities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public GraphEventManager getEventManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public GraphStatisticsHandler getStatisticsHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public PrefixMapping getPrefixMapping() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public void add(Triple t) throws AddDeniedException {
		// TODO Auto-generated method stub

	}

	@Override public void delete(Triple t) throws DeleteDeniedException {
		// TODO Auto-generated method stub

	}

	@Override public ExtendedIterator<Triple> find(TripleMatch m) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public boolean isIsomorphicWith(Graph g) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override public boolean contains(Node s, Node p, Node o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override public boolean contains(Triple t) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override public void close() {
		// TODO Auto-generated method stub

	}

	@Override public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override public boolean isClosed() {
		// TODO Auto-generated method stub
		return false;
	}
	*/
}
