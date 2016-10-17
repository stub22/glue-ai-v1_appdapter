/*
 *  Copyright 2012 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.bind.rdf.jena.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.appdapter.core.store.RepoOper;
import org.appdapter.core.store.dataset.RepoDatasetFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphUtil;
import com.hp.hpl.jena.graph.Node;
// import com.hp.hpl.jena.graph.Reifier;
import com.hp.hpl.jena.graph.Triple;
// import com.hp.hpl.jena.graph.TripleMatch;
// import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.graph.impl.LiteralLabelFactory;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.PrefixMapping;
// import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.util.CollectionFactory;
import com.hp.hpl.jena.util.IteratorCollection;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

/**
 * 
 * Code copied and modified from Jena test class: com.hp.hpl.jena.rdf.model.test.ModelTestBase
 */

public class ModelStuff {

	protected static Model aModel = extendedModel();

	protected static final Model empty = RepoOper.makeReadOnly(RepoDatasetFactory.createPrivateMemModel());

	public static void fail(String failMsg) {
		System.out.println("ModelStuff-FAIL[" + failMsg + "]");
		throw new RuntimeException(failMsg);
	}

	public static void assertTrue(String failMsg, boolean cond) {
		if (!cond) {
			fail(failMsg);
		}
	}

	public static void assertTrue(boolean cond) {
		assertTrue(null, cond);
	}

	public static void assertFalse(String failMsg, boolean cond) {
		assertTrue(failMsg, !cond);
	}

	/**
	 * Returns a Node described by the string, primarily for testing purposes. The string represents a URI, a numeric literal, a string literal, a bnode label, or a variable.
	 * <ul>
	 * <li>'some text' :: a string literal with that text
	 * <li>'some text'someLanguage:: a string literal with that text and language
	 * <li>'some text'someURI:: a typed literal with that text and datatype
	 * <li>digits :: a literal [OF WHAT TYPE] with that [numeric] value
	 * <li>_XXX :: a bnode with an AnonId built from _XXX
	 * <li>?VVV :: a variable with name VVV
	 * <li>&PPP :: to be done
	 * <li>name:stuff :: the URI; name may be expanded using the Extended map
	 * </ul>
	 * 
	 * @param x
	 *            the string describing the node
	 * @return a node of the appropriate type with the appropriate label
	 */
	public static Node create(String x)
	{
		return create(PrefixMapping.Extended, x);
	}

	/**
	 * Returns a Node described by the string, primarily for testing purposes. The string represents a URI, a numeric literal, a string literal, a bnode label, or a variable.
	 * <ul>
	 * <li>'some text' :: a string literal with that text
	 * <li>'some text'someLanguage:: a string literal with that text and language
	 * <li>'some text'someURI:: a typed literal with that text and datatype
	 * <li>digits :: a literal [OF WHAT TYPE] with that [numeric] value
	 * <li>_XXX :: a bnode with an AnonId built from _XXX
	 * <li>?VVV :: a variable with name VVV
	 * <li>&PPP :: to be done
	 * <li>name:stuff :: the URI; name may be expanded using the Extended map
	 * </ul>
	 * 
	 * @param pm
	 *            the PrefixMapping for translating pre:X strings
	 * @param x
	 *            the string encoding the node to create
	 * @return a node with the appropriate type and label
	 */
	public static Node create(PrefixMapping pm, String x)
	{
		if (x.equals(""))
			throw new JenaException("Node.create does not accept an empty string as argument");
		char first = x.charAt(0);
		if (first == '\'' || first == '\"')
			return Node.createLiteral(newString(pm, first, x));
		if (Character.isDigit(first))
			return Node.createLiteral(x, "", XSDDatatype.XSDinteger);
		if (first == '_')
			return Node.createAnon(new AnonId(x));
		if (x.equals("??"))
			return Node.ANY;
		if (first == '?')
			return Node.createVariable(x.substring(1));
		if (first == '&')
			return Node.createURI("q:" + x.substring(1));
		int colon = x.indexOf(':');
		String d = pm.getNsPrefixURI("");
		return colon < 0
				? Node.createURI((d == null ? "eh:/" : d) + x)
				: Node.createURI(pm.expandPrefix(x));
	}

	public static String unEscape(String spelling)
	{
		if (spelling.indexOf('\\') < 0)
			return spelling;
		StringBuffer result = new StringBuffer(spelling.length());
		int start = 0;
		while (true)
		{
			int b = spelling.indexOf('\\', start);
			if (b < 0)
				break;
			result.append(spelling.substring(start, b));
			result.append(unEscape(spelling.charAt(b + 1)));
			start = b + 2;
		}
		result.append(spelling.substring(start));
		return result.toString();
	}

	public static char unEscape(char ch)
	{
		switch (ch)
		{
		case '\\':
		case '\"':
		case '\'':
			return ch;
		case 'n':
			return '\n';
		case 's':
			return ' ';
		case 't':
			return '\t';
		default:
			return 'Z';
		}
	}

	public static LiteralLabel literal(PrefixMapping pm, String spelling, String langOrType)
	{
		String content = unEscape(spelling);
		int colon = langOrType.indexOf(':');
		return colon < 0
				? LiteralLabelFactory.create(content, langOrType, false)
				: LiteralLabelFactory.createLiteralLabel(content, "", Node.getType(pm.expandPrefix(langOrType)));
	}

	public static LiteralLabel newString(PrefixMapping pm, char quote, String nodeString)
	{
		int close = nodeString.lastIndexOf(quote);
		return literal(pm, nodeString.substring(1, close), nodeString.substring(close + 1));
	}

	/**
	 * Utility factory as for create(String), but allowing the PrefixMapping to be specified explicitly.
	 */
	public static Triple createTriple(PrefixMapping pm, String fact)
	{
		StringTokenizer st = new StringTokenizer(fact);
		Node sub = create(pm, st.nextToken());
		Node pred = create(pm, st.nextToken());
		Node obj = create(pm, st.nextToken());
		return Triple.create(sub, pred, obj);
	}

	/**
	 * Utility factory method for creating a triple based on the content of an "S P O" string. The S, P, O are processed by Node.create, see which for details of the supported syntax. This method exists to support test code. Nodes are interpreted using the Standard prefix mapping.
	 */

	public static Triple createTriple(String fact)
	{
		return createTriple(PrefixMapping.Standard, fact);
	}

	public static Node node(String x)
	{
		return create(x);
	}

	/**
	 * Answer a set containing the elements from the iterator <code>it</code>; a shorthand for <code>IteratorCollection.iteratorToSet(it)</code>, which see.
	 */
	public static <T> Set<T> iteratorToSet(Iterator<? extends T> it)
	{
		return IteratorCollection.iteratorToSet(it);
	}

	/**
	 * Answer a list containing the elements from the iterator <code>it</code>, in order; a shorthand for <code>IteratorCollection.iteratorToList(it)</code>, which see.
	 */
	public static <T> List<T> iteratorToList(Iterator<? extends T> it)
	{
		return IteratorCollection.iteratorToList(it);
	}

	protected static Model extendedModel()
	{
		Model result = RepoDatasetFactory.createDefaultModel();
		result.setNsPrefixes(PrefixMapping.Extended);
		return result;
	}

	protected static String nice(RDFNode n)
	{
		return nice(n.asNode());
	}

	public static Triple triple(String fact)
	{
		return createTriple(fact);
	}

	/**
	 * Answer a triple described by the three space-separated node descriptions in <code>fact</code>, using prefix-mappings from <code>pm</code>; a shorthand for <code>Triple.create(pm, fact)</code>, which see.
	 */
	public static Triple triple(PrefixMapping pm, String fact)
	{
		return createTriple(pm, fact);
	}

	/**
	 * Answer an array of triples; each triple is described by one of the semi-separated substrings of <code>facts</code>, as per <code>triple</code> with prefix-mapping <code>Extended</code>.
	 */
	public static Triple[] tripleArray(String facts)
	{
		ArrayList<Triple> al = new ArrayList<Triple>();
		StringTokenizer semis = new StringTokenizer(facts, ";");
		while (semis.hasMoreTokens())
			al.add(triple(PrefixMapping.Extended, semis.nextToken()));
		return al.toArray(new Triple[al.size()]);
	}

	/**
	 * Answer a set of triples where the elements are described by the semi-separated substrings of <code>facts</code>, as per <code>triple</code>.
	 */
	public static Set<Triple> tripleSet(String facts)
	{
		Set<Triple> result = new HashSet<Triple>();
		StringTokenizer semis = new StringTokenizer(facts, ";");
		while (semis.hasMoreTokens())
			result.add(triple(semis.nextToken()));
		return result;
	}

	/**
	 * Answer a list of nodes, where the nodes are described by the space-separated substrings of <code>items</code> as per <code>node()</code>.
	 */
	public static List<Node> nodeList(String items)
	{
		ArrayList<Node> nl = new ArrayList<Node>();
		StringTokenizer nodes = new StringTokenizer(items);
		while (nodes.hasMoreTokens())
			nl.add(node(nodes.nextToken()));
		return nl;
	}

	/**
	 * Answer an array of nodes, where the nodes are described by the space-separated substrings of <code>items</code> as per
	 */
	public static Node[] nodeArray(String items)
	{
		List<Node> nl = nodeList(items);
		return nl.toArray(new Node[nl.size()]);
	}

	/**
	 * Answer the graph <code>g</code> after adding to it every triple encoded in <code>s</code> in the fashion of <code>tripleArray</code>, a semi-separated sequence of space-separated node descriptions.
	 */
	public static Graph graphAdd(Graph g, String s)
	{
		StringTokenizer semis = new StringTokenizer(s, ";");
		while (semis.hasMoreTokens())
			g.add(triple(PrefixMapping.Extended, semis.nextToken()));
		return g;
	}

	/**
	 * Answer a new memory-based graph with Extended prefixes.
	 */
	public static Graph newGraph()
	{
		Graph result = Factory.createGraphMem();
		result.getPrefixMapping().setNsPrefixes(PrefixMapping.Extended);
		return result;
	}

	/**
	 * Answer a new memory-based graph with initial contents as described by <code>s</code> in the fashion of <code>graphAdd()</code>. Not over-ridable; do not use for abstraction.
	 */
	public static Graph graphWith(String s)
	{
		return graphAdd(newGraph(), s);
	}

	/**
	 * Assert that the graph <code>g</code> is isomorphic to the graph described by <code>template</code> in the fashion of <code>graphWith</code>.
	 */
	public static void assertEqualsTemplate(String title, Graph g, String template)
	{
		assertIsomorphic(title, graphWith(template), g);
	}

	/**
	 * Assert that the supplied graph <code>got</code> is isomorphic with the the desired graph <code>expected</code>; if not, display a readable description of both graphs.
	 */
	public static void assertIsomorphic(String title, Graph expected, Graph got)
	{
		if (!expected.isIsomorphicWith(got))
		{
			Map<Node, Object> map = CollectionFactory.createHashedMap();
			fail(title + ": wanted " + nice(expected, map) + "\nbut got " + nice(got, map));
		}
	}

	/**
	 * Answer a string which is a newline-separated list of triples (as produced by niceTriple) in the graph <code>g</code>. The map <code>bnodes</code> maps already-seen bnodes to their "nice" strings.
	 */
	public static String nice(Graph g, Map<Node, Object> bnodes)
	{
		StringBuffer b = new StringBuffer(g.size() * 100);
		ExtendedIterator<Triple> it = GraphUtil.findAll(g);
		while (it.hasNext())
			niceTriple(b, bnodes, it.next());
		return b.toString();
	}

	/**
	 * Append to the string buffer <code>b</code> a "nice" representation of the triple <code>t</code> on a new line, using (and updating) <code>bnodes</code> to supply "nice" strings for any blank nodes.
	 */
	protected static void niceTriple(StringBuffer b, Map<Node, Object> bnodes, Triple t)
	{
		b.append("\n    ");
		appendNode(b, bnodes, t.getSubject());
		appendNode(b, bnodes, t.getPredicate());
		appendNode(b, bnodes, t.getObject());
	}

	/**
	 * A counter for new bnode strings; it starts at 1000 so as to make the bnode strings more uniform (at least for the first 9000 bnodes).
	 */
	protected static int bnc = 1000;

	/**
	 * Append to the string buffer <code>b</code> a space followed by the "nice" representation of the node <code>n</code>. If <code>n</code> is a bnode, re-use any existing string for it from <code>bnodes</code> or make a new one of the form <i>_bNNNN</i> with NNNN a new integer.
	 */
	protected static void appendNode(StringBuffer b, Map<Node, Object> bnodes, Node n)
	{
		b.append(' ');
		if (n.isBlank())
		{
			Object already = bnodes.get(n);
			if (already == null)
				bnodes.put(n, already = "_b" + bnc++);
			b.append(already);
		}
		else
			b.append(nice(n));
	}

	/**
	 * Answer the "nice" representation of this node, the string returned by <code>n.toString(PrefixMapping.Extended,true)</code>.
	 */
	protected static String nice(Node n)
	{
		return n.toString(PrefixMapping.Extended, true);
	}

	/**
	 * Assert that the computed graph <code>got</code> is isomorphic with the desired graph <code>expected</code>; if not, fail with a default message (and pretty output of the graphs).
	 */
	public static void assertIsomorphic(Graph expected, Graph got)
	{
		assertIsomorphic("graphs must be isomorphic", expected, got);
	}

	/**
	 * Assert that the graph <code>g</code> must contain the triple described by <code>s</code>; if not, fail with pretty output of both graphs and a message containing <code>name</code>.
	 */
	public static void assertContains(String name, String s, Graph g)
	{
		assertTrue(name + " must contain " + s, g.contains(triple(s)));
	}

	/**
	 * Assert that the graph <code>g</code> contains all the triples described by the string <code>s</code; if not, fail with a message containing <code>name</code>.
	 */
	public static void assertContainsAll(String name, Graph g, String s)
	{
		StringTokenizer semis = new StringTokenizer(s, ";");
		while (semis.hasMoreTokens())
			assertContains(name, semis.nextToken(), g);
	}

	/**
	 * Assert that the graph <code>g</code> does not contain the triple described by <code>s<code>; if it does, fail with a message containing
        <code>name</code>.
	 */
	public static void assertOmits(String name, Graph g, String s)
	{
		assertFalse(name + " must not contain " + s, g.contains(triple(s)));
	}

	/**
	 * Assert that the graph <code>g</code> contains none of the triples described by <code>s</code> in the usual way; otherwise, fail with a message containing <code>name</code>.
	 */
	public static void assertOmitsAll(String name, Graph g, String s)
	{
		StringTokenizer semis = new StringTokenizer(s, ";");
		while (semis.hasMoreTokens())
			assertOmits(name, g, semis.nextToken());
	}

	/**
	 * Assert that <code>g</code> contains the triple described by <code>fact</code> in the usual way.
	 */
	public static boolean contains(Graph g, String fact)
	{
		return g.contains(triple(fact));
	}

	/**
	 * Assert that <code>g</code> contains every triple in <code>triples</code>.
	 */
	public void testContains(Graph g, Triple[] triples)
	{
		for (int i = 0; i < triples.length; i += 1)
			assertTrue("contains " + triples[i], g.contains(triples[i]));
	}

	/**
	 * Assert that <code>g</code> contains every triple in <code>triples</code>.
	 */
	public void testContains(Graph g, List<Triple> triples)
	{
		for (int i = 0; i < triples.size(); i += 1)
			assertTrue(g.contains(triples.get(i)));
	}

	/**
	 * Assert that <code>g</code> contains every triple in <code>it</code>.
	 */
	public void testContains(Graph g, Iterator<Triple> it)
	{
		while (it.hasNext())
			assertTrue(g.contains(it.next()));
	}

	/**
	 * Assert that <code>g</code> contains every triple in <code>other</code>.
	 */
	public void testContains(Graph g, Graph other)
	{
		testContains(g, GraphUtil.findAll(other));
	}

	/**
	 * Assert that <code>g</code> contains none of the triples in <code>triples</code>.
	 */
	public void testOmits(Graph g, Triple[] triples)
	{
		for (int i = 0; i < triples.length; i += 1)
			assertFalse("", g.contains(triples[i]));
	}

	/**
	 * Assert that <code>g</code> contains none of the triples in <code>triples</code>.
	 */
	public void testOmits(Graph g, List<Triple> triples)
	{
		for (int i = 0; i < triples.size(); i += 1)
			assertFalse("", g.contains(triples.get(i)));
	}

	/**
	 * Assert that <code>g</code> contains none of the triples in <code>it</code>.
	 */
	public void testOmits(Graph g, Iterator<Triple> it)
	{
		while (it.hasNext())
			assertFalse("", g.contains(it.next()));
	}

	/**
	 * Assert that <code>g</code> contains none of the triples in <code>other</code>.
	 */
	public void testOmits(Graph g, Graph other)
	{
		testOmits(g, GraphUtil.findAll(other));
	}

	/**
	 * Answer an instance of <code>graphClass</code>. If <code>graphClass</code> has a constructor that takes a <code>ReificationStyle</code> argument, then that constructor is run on <code>style</code> to get the instance. Otherwise, if it has a # constructor that takes an argument of <code>wrap</code>'s class before the <code>ReificationStyle</code>, that constructor is used; this allows non-static inner classes to be used for <code>graphClass</code>, with <code>wrap</code> being the outer class instance. If no suitable constructor exists, a JenaException is thrown.
	 * 
	 * @param wrap
	 *            the outer class instance if graphClass is an inner class
	 * @param graphClass
	 *            a class implementing Graph
	 * @param style
	 *            the reification style to use
	 * @return an instance of graphClass with the given style
	 * @throws RuntimeException
	 *             or JenaException if construction fails
	 */
	/*
	public static Graph getGraph(Object wrap, Class<? extends Graph> graphClass, ReificationStyle style)
	{
		try
		{
			Constructor<?> cons = getConstructor(graphClass, new Class[] { ReificationStyle.class });
			if (cons != null)
				return (Graph) cons.newInstance(new Object[] { style });
			Constructor<?> cons2 = getConstructor(graphClass, new Class[] { wrap.getClass(), ReificationStyle.class });
			if (cons2 != null)
				return (Graph) cons2.newInstance(new Object[] { wrap, style });
			throw new JenaException("no suitable graph constructor found for " + graphClass);
		} catch (RuntimeException e)
		{
			throw e;
		} catch (Exception e)
		{
			throw new JenaException(e);
		}
	}
	*/
	/*
	    protected static Graph getReificationTriples( final Reifier r )
	        {
	        return new GraphBase( ReificationStyle.Minimal )
	            {
	            @Override public ExtendedIterator<Triple> graphBaseFind( TripleMatch m )
	                { return r.find( m ); }
	            };
	        }

		*/

	/**
	 * create a Statement in a given Model with (S, P, O) extracted by parsing a string.
	 * 
	 * @param m
	 *            the model the statement is attached to
	 * @param an
	 *            "S P O" string.
	 * @return m.createStatement(S, P, O)
	 */

	public static Statement statement(Model m, String fact)
	{
		StringTokenizer st = new StringTokenizer(fact);
		Resource sub = resource(m, st.nextToken());
		Property pred = property(m, st.nextToken());
		RDFNode obj = rdfNode(m, st.nextToken());
		return m.createStatement(sub, pred, obj);
	}

	public static Statement statement(String fact)
	{
		return statement(aModel, fact);
	}

	public static RDFNode rdfNode(Model m, String s)
	{
		return m.asRDFNode(create(m, s));
	}

	public static <T extends RDFNode> T rdfNode(Model m, String s, Class<T> c)
	{
		return rdfNode(m, s).as(c);
	}

	protected static Resource resource()
	{
		return ResourceFactory.createResource();
	}

	public static Resource resource(String s)
	{
		return resource(aModel, s);
	}

	public static Resource resource(Model m, String s)
	{
		return (Resource) rdfNode(m, s);
	}

	public static Property property(String s)
	{
		return property(aModel, s);
	}

	public static Property property(Model m, String s)
	{
		return rdfNode(m, s).as(Property.class);
	}

	public static Literal literal(Model m, String s)
	{
		return rdfNode(m, s).as(Literal.class);
	}

	/**
	 * Create an array of Statements parsed from a semi-separated string.
	 * 
	 * @param m
	 *            a model to serve as a statement factory
	 * @param facts
	 *            a sequence of semicolon-separated "S P O" facts
	 * @return a Statement[] of the (S P O) statements from the string
	 */
	public static Statement[] statements(Model m, String facts)
	{
		ArrayList<Statement> sl = new ArrayList<Statement>();
		StringTokenizer st = new StringTokenizer(facts, ";");
		while (st.hasMoreTokens())
			sl.add(statement(m, st.nextToken()));
		return sl.toArray(new Statement[sl.size()]);
	}

	/**
	 * Create an array of Resources from a whitespace-separated string
	 * 
	 * @param m
	 *            a model to serve as a resource factory
	 * @param items
	 *            a whitespace-separated sequence to feed to resource
	 * @return a RDFNode[] of the parsed resources
	 */
	public static Resource[] resources(Model m, String items)
	{
		ArrayList<Resource> rl = new ArrayList<Resource>();
		StringTokenizer st = new StringTokenizer(items);
		while (st.hasMoreTokens())
			rl.add(resource(m, st.nextToken()));
		return rl.toArray(new Resource[rl.size()]);
	}

	/**
	 * Answer the set of resources given by the space-separated <code>items</code> string. Each resource specification is interpreted as per <code>resource</code>.
	 */
	public static Set<Resource> resourceSet(String items)
	{
		Set<Resource> result = new HashSet<Resource>();
		StringTokenizer st = new StringTokenizer(items);
		while (st.hasMoreTokens())
			result.add(resource(st.nextToken()));
		return result;
	}

	/**
	 * add to a model all the statements expressed by a string.
	 * 
	 * @param m
	 *            the model to be updated
	 * @param facts
	 *            a sequence of semicolon-separated "S P O" facts
	 * @return the updated model
	 */
	public static Model modelAdd(Model m, String facts)
	{
		StringTokenizer semis = new StringTokenizer(facts, ";");
		while (semis.hasMoreTokens())
			m.add(statement(m, semis.nextToken()));
		return m;
	}

	/**
	 * makes a model initialised with statements parsed from a string.
	 * 
	 * @param facts
	 *            a string in semicolon-separated "S P O" format
	 * @return a model containing those facts
	 */
	public static Model modelWithStatements(String facts)
	{
		throw new RuntimeException("This method needs to be updated for Jena 2.13, which no longer supports ReificationStyle");
		// return modelWithStatements(ReificationStyle.Standard, facts);
	}

	/**
	 * makes a model with a given reiifcation style, initialised with statements parsed from a string.
	 * 
	 * @param style
	 *            the required reification style
	 * @param facts
	 *            a string in semicolon-separated "S P O" format
	 * @return a model containing those facts
	 */
	/*
	public static Model modelWithStatements(ReificationStyle style, String facts)
	{
		return modelAdd(createModel(style), facts);
	}
*/
	/**
	 * make a model with a given reification style, give it Extended prefixes
	 */
	/*
	public static Model createModel(ReificationStyle style)
	{
		// This method  no longer exists in Jena 2.13, but there is perhaps still some place we can plug in the style...
		// Model result = ModelFactory.createDefaultModel(style);
		Model result = ModelFactory.createDefaultModel();
		result.setNsPrefixes(PrefixMapping.Extended);
		return result;
	}
*/
	/**
	 * Answer a default model; it exists merely to abbreviate the rather long explicit invocation.
	 * 
	 * @return a new default [aka memory-based] model
	 */
	public static Model createMemModel()
	{
		return RepoDatasetFactory.createDefaultModel();
	}

	/**
	 * test that two models are isomorphic and fail if they are not.
	 * 
	 * @param title
	 *            a String appearing at the beginning of the failure message
	 * @param wanted
	 *            the model value that is expected
	 * @param got
	 *            the model value to check
	 * @exception if
	 *                the models are not isomorphic
	 */
	public static void assertIsoModels(String title, Model wanted, Model got) {
		if (wanted.isIsomorphicWith(got) == false)
		{
			Map<Node, Object> map = CollectionFactory.createHashedMap();
			fail(title + ": expected " + nice(wanted.getGraph(), map) + "\n but had " + nice(got.getGraph(), map));
		}
	}

	/**
	 * Fail if the two models are not isomorphic. See assertIsoModels(String,Model,Model).
	 */
	public static void assertIsoModels(Model wanted, Model got) {
		assertIsoModels("models must be isomorphic", wanted, got);
	}

	public static void assertDiffer(String title, Object x, Object y)
	{
		if (x == null ? y == null : x.equals(y))
			fail((title == null ? "objects should be different, but both were: " : title) + x);
	}

	/**
	 * assert that the two objects must be unequal according to .equals().
	 * 
	 * @param x
	 *            an object to test; the subject of a .equals()
	 * @param y
	 *            the other object; the argument of the .equals()
	 */
	public static void assertDiffer(Object x, Object y)
	{
		assertDiffer(null, x, y);
	}

	/**
	 * assert that the object <code>x</code> must be of the class <code>expected</code>.
	 */
	public static void assertInstanceOf(Class<?> expected, Object x)
	{
		if (x == null)
			fail("expected instance of " + expected + ", but had null");
		if (!expected.isInstance(x))
			fail("expected instance of " + expected + ", but had instance of " + x.getClass());
	}

	/**
	 * Answer a Set formed from the elements of the List <code>L</code>.
	 */
	public static <T> Set<T> listToSet(List<T> L)
	{
		return CollectionFactory.createHashedSet(L);
	}

	/**
	 * Answer a List of the substrings of <code>s</code> that are separated by spaces.
	 */
	public static List<String> listOfStrings(String s)
	{
		List<String> result = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(s);
		while (st.hasMoreTokens())
			result.add(st.nextToken());
		return result;
	}

	/**
	 * Answer a Set of the substrings of <code>s</code> that are separated by spaces.
	 */
	public static Set<String> setOfStrings(String s)
	{
		Set<String> result = new HashSet<String>();
		StringTokenizer st = new StringTokenizer(s);
		while (st.hasMoreTokens())
			result.add(st.nextToken());
		return result;
	}

	/**
	 * Answer a list containing the single object <code>x</code>.
	 */
	public static <T> List<T> listOfOne(T x)
	{
		List<T> result = new ArrayList<T>();
		result.add(x);
		return result;
	}

	/**
	 * Answer a Set containing the single object <code>x</code>.
	 */
	public static <T> Set<T> setOfOne(T x)
	{
		Set<T> result = new HashSet<T>();
		result.add(x);
		return result;
	}

	/**
	 * Answer a fresh list which is the concatenation of <code>L</code> then <code>R</code>. Neither <code>L</code> nor <code>R</code> is updated.
	 */
	public static <T> List<T> append(List<? extends T> L, List<? extends T> R)
	{
		List<T> result = new ArrayList<T>(L);
		result.addAll(R);
		return result;
	}

	/**
	 * Answer an iterator over the space-separated substrings of <code>s</code>.
	 */
	protected static ExtendedIterator<String> iteratorOfStrings(String s)
	{
		return WrappedIterator.create(listOfStrings(s).iterator());
	}

	/**
	 * Do nothing; a way of notating that a test has succeeded, useful in the body of a catch-block to silence excessively [un]helpful disgnostics.
	 */
	public static void pass()
	{
	}

	/**
	 * Answer the constructor of the class <code>c</code> which takes arguments of the type(s) in <code>args</code>, or <code>null</code> if there isn't one.
	 */
	public static Constructor<?> getConstructor(Class<?> c, Class<?>[] args)
	{
		try {
			return c.getConstructor(args);
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	/**
	 * Answer true iff the method <code>m</code> is a public method which fits the pattern of being a test method, ie, test*() returning void.
	 */
	public static boolean isPublicTestMethod(Method m)
	{
		return Modifier.isPublic(m.getModifiers()) && isTestMethod(m);
	}

	/**
	 * Answer true iff the method <code>m</code> has a name starting "test", takes no arguments, and returns void; must catch junit tests, in other words.
	 */
	public static boolean isTestMethod(Method m)
	{
		return m.getName().startsWith("test")
				&& m.getParameterTypes().length == 0
				&& m.getReturnType().equals(Void.TYPE);
	}

	/**
	 * Answer true iff <code>subClass</code> is the same class as <code>superClass</code>, if its superclass <i>is</i> <code>superClass</code>, or if one of its interfaces hasAsInterface that class.
	 */
	public static boolean hasAsParent(Class<?> subClass, Class<?> superClass)
	{
		if (subClass == superClass || subClass.getSuperclass() == superClass)
			return true;
		Class<?>[] is = subClass.getInterfaces();
		for (int i = 0; i < is.length; i += 1)
			if (hasAsParent(is[i], superClass))
				return true;
		return false;
	}

	/**
	 * Fail unless <code>subClass</code> has <code>superClass</code> as a parent, either a superclass or an implemented (directly or not) interface.
	 */
	public static void assertHasParent(Class<?> subClass, Class<?> superClass) {
		if (hasAsParent(subClass, superClass) == false) {
			fail("" + subClass + " should have " + superClass + " as a parent");
		}
	}

}
