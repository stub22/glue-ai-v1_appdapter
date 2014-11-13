package org.appdapter.core.remote;

import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.remote.sparql.SparqlEndpointClient;
import org.slf4j.Logger;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
//so we can see when the class is not a transitive dependacny

/**
 * Some of this code was copied-and-modified from our old web-client for Cogbot.
 */
public class TestOuterClientSOH extends BasicDebugger {
	// Fun data queries to DBPedia.
	public static String dbpediaServiceURL = "http://dbpedia.org/sparql";

	// Our test repository URLs.
	public static String repoBaseURL = "http://localhost:3030/";
	public static String repoBaseQryURL = repoBaseURL + "sparql/";
	public static String repoBaseUpdURL = repoBaseURL + "sparql-update/";
	public static String bookSvcURL = repoBaseQryURL + "books";
	public static String glueQryURL = repoBaseQryURL + "robot01";
	public static String glueUpdURL = repoBaseUpdURL + "robot01";

	SparqlEndpointClient myAgentRepoClient = new SparqlEndpointClient(glueUpdURL);

	public static void main(String[] args) {
		TestOuterClientSOH test = new TestOuterClientSOH();
		test.runTests();
	}

	public TestOuterClientSOH() {
		forceLog4jConfig();
	}

	public void runTests() {
		// Fun, but gives long result list
		// runRemoteSparqlSelectAndPrintResults (dbpediaServiceURL, dbpQ);
		runRemoteSparqlSelectAndPrintResults(bookSvcURL, bookQ);
		runRemoteSparqlSelectAndPrintResults(glueQryURL, glueQ);

		// Here we write to a remote SPARQL repo, adding triples to it.
		// This writing can effectively "command" the system embedding that repo to take action.
		myAgentRepoClient.execRemoteSparqlUpdate(glueUpdURL, glueUpSilly, true);
		// However, to see the effect of the action, we must monitor the state of the remote repo.
		// By convention, our writes are restricted to contain only INSERT (not DELETE), except
		// under special admin circumstances, under which we have less assurance of server
		// smoothness, and timeliness of updates.
	}

	public void runRemoteSparqlSelectAndPrintResults(String svcUrl, String queryTxt) {
		Logger log = getLogger();
		ResultSet resSet = myAgentRepoClient.execRemoteSparqlSelect(svcUrl, queryTxt);
		ResultSetRewindable rewindableResSet = ResultSetFactory.makeRewindable(resSet);
		String resultXML = ResultSetFormatter.asXMLString(rewindableResSet);
		log.info("ResultSet as XML: \n" + resultXML);
	}

	static String PREFIX_FOAF = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n", PREFIX_XSD = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n",
			PREFIX_DC = "PREFIX dc:      <http://purl.org/dc/elements/1.1/>\n", PREFIX_DBO = "PREFIX dbo: <http://dbpedia.org/ontology/>\n", PREFIX_DBP = "PREFIX : <http://dbpedia.org/resource/>\n",
			PREFIX_BOOKS = "PREFIX books:   <http://example.org/book/>\n", PREFIX_CCRT = "PREFIX ccrt:  <urn:ftd:cogchar.org:2012:runtime#>\n",
			PREFIX_UA = "PREFIX ua:    <http://www.cogchar.org/lift/user/config#>\n";

	// Regular SPARQL queries, sent through Jena/ARQ client infrastructure

	static String dbpQ = PREFIX_DBO + PREFIX_FOAF + PREFIX_XSD + PREFIX_DBP + "SELECT ?name ?birth ?death ?person WHERE {\n"
			+ "?person dbo:birthPlace :Berlin .   ?person dbo:birthDate ?birth .   ?person foaf:name ?name .\n" + "?person dbo:deathDate ?death .   FILTER (?birth < '1900-01-01'^^xsd:date) }";
	static String bookQ = PREFIX_BOOKS + PREFIX_DC + "SELECT ?book ?title  WHERE   { ?book dc:title ?title }";
	static String glueQ = PREFIX_CCRT + PREFIX_UA + "SELECT ?user ?password ?salt ?startPage WHERE { GRAPH ?qGraph { \n"
			+ "?user a ccrt:userConfig; ua:password ?password; ua:salt ?salt; ua:homePage ?startPage.}}";

	// Below is SPARQL-Update text, sent through HTTP post

	static String glueUpSilly = PREFIX_CCRT + PREFIX_UA + PREFIX_DC + "INSERT DATA {  GRAPH   ccrt:user_access_sheet_22 { \n"
			+ "<http://yeah/nutty> dc:title 'How to be revolting' ;    dc:creator 'Spart A. Cus'. }}\n";
}
