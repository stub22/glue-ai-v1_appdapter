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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.appdapter.core.store.dataset.DatasetGraphQuadProc;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.shared.LockMRSW;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.resultset.ResultSetMem;
import com.hp.hpl.jena.util.iterator.SingletonIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

public class SparqlDatasetGraph extends DatasetGraphQuadProc implements DatasetGraph {

	public static SparqlDatasetGraph SINGLETON = new SparqlDatasetGraph(null);
	private String endpointURI;
	private SparqlEndpointClient repository;
	private Lock lock = new LockMRSW();

	public SparqlDatasetGraph(String endpointURI) {
		this.endpointURI = endpointURI;
		this.repository = new SparqlEndpointClient(endpointURI);
	}

	private Graph getGraphFor(Quad q) {
		return getGraphFor(q.getGraph());
	}

	private Graph getGraphFor(Node g) {
		return (g == Node.ANY) ? new SparqlGraph(endpointURI) : new SparqlGraph(endpointURI, g.getURI());
	}

	@Override public void add(Quad arg0) {
		getGraphFor(arg0).add(new Triple(arg0.getSubject(), arg0.getPredicate(), arg0.getObject()));
	}

	@Override public boolean contains(Quad arg0) {
		return getGraphFor(arg0).contains(new Triple(arg0.getSubject(), arg0.getPredicate(), arg0.getObject()));
	}

	@Override public boolean contains(Node arg0, Node arg1, Node arg2, Node arg3) {
		return getGraphFor(arg0).contains(arg1, arg2, arg3);
	}

	@Override public void delete(Quad arg0) {
		getGraphFor(arg0).delete(new Triple(arg0.getSubject(), arg0.getPredicate(), arg0.getObject()));
	}

	@Override public void deleteAny(Node arg0, Node arg1, Node arg2, Node arg3) {
		// TODO check this
		getGraphFor(arg0).delete(new Triple(arg1, arg2, arg3));
	}

	@Override public Iterator<Quad> find() {
		return find(Node.ANY, Node.ANY, Node.ANY, Node.ANY);
	}

	@Override public Iterator<Quad> find(Quad arg0) {
		return find(arg0.getSubject(), arg0.getPredicate(), arg0.getObject(), arg0.getGraph());
	}

	@Override public Iterator<Quad> find(Node graph, Node subject, Node predicate, Node object) {
		if (!isVar(subject) && !isVar(predicate) && !isVar(object) && !isVar(graph)) {
			if (contains(subject, predicate, object, graph)) {
				return new SingletonIterator(new Triple(subject, predicate, object));
			} else {
				return WrappedIterator.create(Collections.EMPTY_LIST.iterator());
			}
		}
		StringBuffer findQuery = new StringBuffer("SELECT * WHERE { \n");
		String graphURI = !isVar(graph) ? graph.getURI() : null;
		findQuery.append("  GRAPH ");
		if (graphURI != null) {
			findQuery.append("  <" + graphURI + ">");
		} else {
			findQuery.append("?g");
		}
		findQuery.append(" { ");
		findQuery.append(SparqlGraph.sparqlNode(subject, "?s")).append(" ").append(SparqlGraph.sparqlNode(predicate, "?p")).append(" ").append(SparqlGraph.sparqlNode(object, "?o"));
		findQuery.append("  } ");
		findQuery.append("\n}");

		//log.info(findQuery.toString());
		ResultSet rs = execSelect(findQuery.toString());
		//rs = execSelect(findQuery.toString());
		//rs = execSelect(findQuery.toString());

		List<Quad> quadlist = new ArrayList<Quad>();
		while (rs.hasNext()) {
			QuerySolution soln = rs.nextSolution();
			Quad q = new Quad(isVar(graph) ? soln.get("?g").asNode() : graph, isVar(subject) ? soln.get("?s").asNode() : subject, isVar(predicate) ? soln.get("?p").asNode() : predicate,
					isVar(object) ? soln.get("?o").asNode() : object);
			//log.info(t);
			quadlist.add(q);
		}
		//log.info(triplist.size() + " results");
		return WrappedIterator.create(quadlist.iterator());
	}

	@Override public Iterator<Quad> findNG(Node arg0, Node arg1, Node arg2, Node arg3) {
		// TODO check this
		return find(arg0, arg1, arg2, arg3);
	}

	@Override public Graph getDefaultGraph() {
		return new SparqlGraph(endpointURI);
	}

	@Override public Graph getGraph(Node arg0) {
		return new SparqlGraph(endpointURI, arg0.getURI());
	}

	@Override public Lock getLock() {
		return lock;
	}

	@Override public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override public Iterator<Node> listGraphNodes() {
		List<Node> graphNodeList = new ArrayList<Node>();
		try {
			SparqlEndpointClient conn = getConnection();
			try {
				List<Resource> conResult = conn.getContextIDs();

				for (Resource con : conResult) {
					graphNodeList.add(con.asNode());
				}
			} finally {

			}
		} catch (Exception re) {
			throw new RuntimeException(re);
		}
		return graphNodeList.iterator();
	}

	private SparqlEndpointClient getConnection() {
		try {
			return this.repository.getConnection();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private boolean isVar(Node node) {
		return (node == null || node.isVariable() || node == Node.ANY);
	}

	private ResultSet execSelect(String queryStr) {
		if (true)
			return getConnection().execRemoteSparqlSelect(endpointURI, queryStr);

		//      long startTime1 = System.currentTimeMillis();
		//      try {
		//
		//          RepositoryConnection conn = getConnection();
		//          try {
		//              GraphQuery q = conn.prepareGraphQuery(QueryLanguage.SPARQL, queryStr);
		//              q.evaluate();
		//          } catch (MalformedQueryException e) {
		//              throw new RuntimeException(e);
		//          } finally {
		//              conn.close();
		//          }
		//      } catch (Exception re) {
		//          //log.info(re,re);
		//      }

		//      log.info((System.currentTimeMillis() - startTime1) + " to execute via sesame");

		long startTime = System.currentTimeMillis();
		Query askQuery = QueryFactory.create(queryStr);
		QueryExecution qe = QueryExecutionFactory.sparqlService(endpointURI, askQuery);
		try {
			return new ResultSetMem(qe.execSelect());
		} finally {
			//log.info((System.currentTimeMillis() - startTime) + " to execute via Jena");
			qe.close();
		}
	}

	public SparqlGraph createGraph(String string) {
		throw new AbstractMethodError("createGraph");
	}

	public SparqlDatasetGraph createDataset(String string) {
		throw new AbstractMethodError("createDataset");
	}

	public Dataset getRemoteDataset(String shareName) {
		throw new AbstractMethodError("getRemoteDataset");
	}

	public Dataset toDataset() {
		return new SparqlDataset(this);
	}
}
