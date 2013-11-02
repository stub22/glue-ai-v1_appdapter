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

import java.util.ArrayList;
import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetImpl;

public class SparqlDataset extends DatasetImpl {

	private SparqlDatasetGraph g;

	public SparqlDataset(SparqlDatasetGraph g) {
		super(g);
		this.g = g;
	}

	@Override public DatasetGraph asDatasetGraph() {
		return g;
	}

	@Override public void close() {
		g.close();
	}

	@Override public boolean containsNamedModel(String arg0) {
		return g.containsGraph(Node.createURI(arg0));
	}

	@Override public Model getDefaultModel() {
		return ModelFactory.createModelForGraph(g.getDefaultGraph());
	}

	@Override public Lock getLock() {
		return g.getLock();
	}

	@Override public Model getNamedModel(String arg0) {
		return ModelFactory.createModelForGraph(g.getGraph(Node.createURI(arg0)));
	}

	@Override public Iterator<String> listNames() {
		ArrayList<String> nameList = new ArrayList<String>();
		Iterator<Node> nodeIt = g.listGraphNodes();
		while (nodeIt.hasNext()) {
			Node n = nodeIt.next();
			nameList.add(n.getURI());
		}
		return nameList.iterator();
	}

}
