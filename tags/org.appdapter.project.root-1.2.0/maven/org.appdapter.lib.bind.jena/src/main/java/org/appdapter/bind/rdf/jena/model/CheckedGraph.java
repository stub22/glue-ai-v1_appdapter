package org.appdapter.bind.rdf.jena.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.appdapter.core.log.Debuggable;

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
import org.appdapter.core.store.dataset.RepoDatasetFactory;

public class CheckedGraph implements Graph, PrefixMapping {
	final Graph modelGraph;
	public boolean noAdd = false;
	public boolean noDelete = false;
	public boolean nameSpaceChecked = false;
	private PrefixMapping prefixMap;
	public String debuggingName;

	public void setName(String n) {
		this.debuggingName = n;
	}

	public Graph getDataGraph() {
		return modelGraph;
	}

	@Override public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(noAdd ? "-" : "+").append("A");
		sb.append(noDelete ? "-" : "+").append("R");
		sb.append(nameSpaceChecked ? "-" : "+").append("N");
		sb.append("=" + debuggingName);
		return sb.append(modelGraph.toString()).toString();
	}

	public CheckedGraph(Graph modelGraph, boolean makeNonAdd, boolean makeNonDelete, boolean makeNameSpaceChecked) {
		this.modelGraph = modelGraph;
		if (modelGraph instanceof CheckedGraph) {
			Debuggable.notImplemented("Wrapping " + modelGraph);
		}
		this.prefixMap = modelGraph.getPrefixMapping();
		this.noAdd = makeNonAdd;
		this.noDelete = makeNonDelete;
		this.nameSpaceChecked = makeNameSpaceChecked;
	}

	@Override public void add(Triple t) throws AddDeniedException {
		checkAdd();
		Triple t2 = visitURIs(t);
		modelGraph.add(t2);
	}

	public Triple visitURIs(Triple t) {
		visitURIs(t.getSubject());
		visitURIs(t.getPredicate());
		visitURIs(t.getObject());
		return t;
	}

	private void visitURIs(Node subject) {
		if (subject.isURI()) {
			RepoDatasetFactory.verifyURI(subject.toString());
		}
	}

	private void checkRemove() throws DeleteDeniedException {
		if (!noDelete)
			return;
		DeleteDeniedException ex = new DeleteDeniedException("" + Debuggable.notImplemented("cehckRemove=", this));
		ex.printStackTrace();
		throw ex;
	}

	private void checkAdd() throws AddDeniedException {
		if (!noAdd)
			return;
		AddDeniedException ex = new AddDeniedException("" + Debuggable.notImplemented("AddDeniedException=", this));
		ex.printStackTrace();
		throw ex;
	}

	@Override public void clear() {
		checkRemove();
		modelGraph.clear();
	}

	@Override public void close() {
		modelGraph.close();
	}

	@Override public boolean contains(Node s, Node p, Node o) {
		return modelGraph.contains(s, p, o);
	}

	@Override public boolean contains(Triple t) {
		return modelGraph.contains(t);
	}

	@Override public void delete(Triple t) throws DeleteDeniedException {
		checkRemove();
		modelGraph.delete(t);
	}

	@Override public boolean dependsOn(Graph g) {
		return modelGraph.dependsOn(g);
	}

	@Override public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
		return modelGraph.find(s, p, o);
	}

	@Override public ExtendedIterator<Triple> find(TripleMatch m) {
		return modelGraph.find(m);
	}

	@Override @Deprecated public BulkUpdateHandler getBulkUpdateHandler() {
		return modelGraph.getBulkUpdateHandler();
	}

	@Override public Capabilities getCapabilities() {
		return modelGraph.getCapabilities();
	}

	@Override public GraphEventManager getEventManager() {
		return modelGraph.getEventManager();
	}

	@Override public PrefixMapping getPrefixMapping() {
		return this;
	}

	@Override public GraphStatisticsHandler getStatisticsHandler() {
		return modelGraph.getStatisticsHandler();
	}

	@Override public TransactionHandler getTransactionHandler() {
		return modelGraph.getTransactionHandler();
	}

	@Override public boolean isClosed() {
		return modelGraph.isClosed();
	}

	@Override public boolean isEmpty() {
		return modelGraph.isEmpty();
	}

	@Override public boolean isIsomorphicWith(Graph n) {
		return modelGraph.isIsomorphicWith(n);
	}

	@Override public void remove(Node s, Node p, Node o) {
		if (s == null)
			checkRemove();
		else {
			checkRemove();
		}
		modelGraph.remove(s, p, o);
	}

	@Override public int size() {
		return modelGraph.size();
	}

	public void setNoDelete(boolean b) {
		noDelete = b;
	}

	public void setReadOnly(boolean b) {
		noDelete = b;
		noAdd = b;
	}

	public void checkPrefixChanged(String prevURI, String uri) {
		if (prevURI != null) {
			if (!prevURI.equals(uri)) {
				Debuggable.notImplemented("Prefix change: " + prevURI + " -> " + uri);
			}
		}
	}

	public void checkPrefix(String prefix) {
		if (prefix == null || prefix.equals("")) {
			throw new UnsupportedOperationException("Bad Prefix: " + prefix);
		}
		if (prefix.equals("cc")) {
			return;
		}
	}

	@Override public PrefixMapping setNsPrefix(String prefix, String uri) {
		checkPrefix(prefix);
		String prevURI = prefixMap.getNsPrefixURI(prefix);
		String prevPrefix = prefixMap.getNsURIPrefix(uri);
		checkPrefixChanged(prevURI, uri);
		checkPrefixChanged(prevPrefix, prefix);
		prefixMap.setNsPrefix(prefix, uri);
		return this;
	}

	@Override public PrefixMapping removeNsPrefix(String prefix) {
		prefixMap.removeNsPrefix(prefix);
		return this;
	}

	@Override public PrefixMapping setNsPrefixes(PrefixMapping other) {
		return setNsPrefixes(other.getNsPrefixMap());
	}

	@Override public PrefixMapping setNsPrefixes(Map<String, String> map) {
		Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
		List<String> removedPrefixes = new ArrayList<String>(getNsPrefixMap().keySet());
		while (it.hasNext())
		{
			Map.Entry<String, String> e = it.next();
			String prefix = e.getKey();
			String uri = e.getValue();
			removedPrefixes.remove(prefix);
			setNsPrefix(prefix, uri);
		}
		if (removedPrefixes.size() > 0) {
			StringBuffer sb = new StringBuffer("Removed " + removedPrefixes.size() + " prefix(s):");
			for (String prefix : removedPrefixes) {
				sb.append('\n');
				sb.append(prefix);
				sb.append("->");
				sb.append(getNsPrefixURI(prefix));
			}
			Debuggable.notImplemented(sb.toString());
		}
		return this;
	}

	@Override public PrefixMapping withDefaultMappings(PrefixMapping map)
	{
		Iterator<Map.Entry<String, String>> it = map.getNsPrefixMap().entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry<String, String> e = it.next();
			String prefix = e.getKey();
			String uri = e.getValue();
			if (getNsPrefixURI(prefix) == null && getNsURIPrefix(uri) == null)
				setNsPrefix(prefix, uri);
		}
		return this;
	}

	@Override public String getNsPrefixURI(String prefix) {
		return prefixMap.getNsPrefixURI(prefix);
	}

	@Override public String getNsURIPrefix(String uri) {
		return prefixMap.getNsURIPrefix(uri);
	}

	@Override public Map<String, String> getNsPrefixMap() {
		return Collections.unmodifiableMap(prefixMap.getNsPrefixMap());
	}

	@Override public String expandPrefix(String prefixed) {
		return prefixMap.expandPrefix(prefixed);
	}

	@Override public String shortForm(String uri) {
		return prefixMap.shortForm(uri);
	}

	@Override public String qnameFor(String uri) {
		return prefixMap.qnameFor(uri);
	}

	@Override public PrefixMapping lock() {
		return this;
	}

	@Override public boolean samePrefixMappingAs(PrefixMapping other) {
		return prefixMap.samePrefixMappingAs(other);
	}

	public void setPrefixCheck(boolean b) {
		this.nameSpaceChecked = b;

	}

	public void setNoAdd(boolean b) {
		this.noAdd = b;
	}

	public static CheckedGraph ensure(Graph modelGraph2, boolean makeNonAdd, boolean makeNonDelete, boolean makeNameSpaceChecked) {
		CheckedGraph gnr;
		if (!(modelGraph2 instanceof CheckedGraph)) {
			gnr = new CheckedGraph(modelGraph2, makeNonAdd, makeNonDelete, makeNameSpaceChecked);
		} else {
			gnr = (CheckedGraph) modelGraph2;
			gnr.setNoAdd(makeNonAdd);
			gnr.setNoDelete(makeNonDelete);
			gnr.setPrefixCheck(makeNameSpaceChecked);
		}
		return gnr;
	}
}