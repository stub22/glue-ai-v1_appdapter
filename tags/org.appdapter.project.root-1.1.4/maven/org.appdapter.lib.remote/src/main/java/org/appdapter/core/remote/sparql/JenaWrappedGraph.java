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

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphEvents;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.shared.DeleteDeniedException;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

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

	/*
		//protected QueryHandler queryHandler;
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
	*/
	abstract @Override public void delete(Triple t) throws DeleteDeniedException;

	abstract @Override public ExtendedIterator<Triple> find(Node s, Node p, Node o);
}
