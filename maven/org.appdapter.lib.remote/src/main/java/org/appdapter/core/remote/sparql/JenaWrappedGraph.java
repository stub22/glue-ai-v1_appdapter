package org.appdapter.core.remote.sparql;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphEvents;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Reifier;
import com.hp.hpl.jena.graph.impl.SimpleReifier;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.graph.query.SimpleQueryHandler;
import com.hp.hpl.jena.shared.ReificationStyle;

abstract public class JenaWrappedGraph implements Graph {

	@Override public void clear() {
		getBase().clear();
		getEventManager().notifyEvent(this, GraphEvents.removeAll);
	}

	@Override public void remove(Node s, Node p, Node o) {
		getBase().remove(s, p, o);
		getEventManager().notifyEvent(this, GraphEvents.remove(s, p, o));
	}

	protected QueryHandler queryHandler;
	protected Reifier reifier = null;// new EmptyReifier(this);

	public QueryHandler queryHandler() {
		Graph graph = getBase();
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

	protected Graph getBase() {
		return null;

	}

}
