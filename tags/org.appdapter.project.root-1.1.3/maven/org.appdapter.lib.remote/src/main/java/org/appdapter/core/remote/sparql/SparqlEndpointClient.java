/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.appdapter.core.remote.sparql;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.log.Debuggable;
import org.slf4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Resource;

public class SparqlEndpointClient extends BasicDebugger {

	public String buildSparqlPrefixHeader(String repoServiceURL) {
		return Debuggable.notImplemented("buildSparqlPrefixHeader", repoServiceURL);
	}

	public List<Resource> getContextIDs() {
		return Debuggable.notImplemented("getContextIDs", this);
	}

	WebDataClient myWebDataClient = new WebDataClient();

	String endpointURI;

	public SparqlEndpointClient(String endpointString) {
		endpointURI = endpointString;
	}

	public SparqlEndpointClient getConnection() {
		return this;
	}

	public void executeUpdate(String updateString) {
		execRemoteSparqlUpdate(endpointURI, updateString, true);

	}

	public ResultSet execQuery(String queryText, boolean debugFlag) {
		return execRemoteSparqlSelect(endpointURI, queryText);
	}

	/**
	 * SPARQL query runs through the sparqlService facility of Jena/ARQ, which handles
	 * the HTTP client duties (using its own class named HttpQuery
	 * http://grepcode.com/file/repo1.maven.org/maven2/com.hp.hpl.jena/arq/2.8.7/com/hp/hpl/jena/sparql/engine/http/HttpQuery.java
	 * and returns us a regular jena.query.ResultSet, which we can iterate rowwise, dump as XML, etc.
	 *
	 * TODO:  Need to review the proper "close" semantics for these result sets.
	 *
	 * @param queryText
	 * @param svcUrl
	 */
	public ResultSet execRemoteSparqlSelect(String svcUrl, String queryText) {
		Logger log = getLogger();
		log.info("QueryUrl=[{}]  QueryText={}", svcUrl, queryText);
		// Create an ARQ parsed query object
		Query parsedQuery = QueryFactory.create(queryText);
		String queryBaseURI = null;
		Syntax queryFileSyntax = Syntax.syntaxSPARQL;
		// Query parsedQuery = QueryFactory.read(queryFileURL, null, Syntax.syntaxSPARQL); // , queryBaseURI, queryFileSyntax);

		QueryExecution qExc = QueryExecutionFactory.sparqlService(svcUrl, parsedQuery);
		ResultSet resSet = qExc.execSelect();
		return resSet;
	}

	/**
	 *  SPARQL-Update POST runs through the Apache Commons HttpClient library.
	 * @param updateText
	 * @param svcUrl
	 */
	public void execRemoteSparqlUpdate(String svcUrl, String updateText, boolean debugFlag) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("request", updateText));
		try {
			myWebDataClient.execPost(svcUrl, nvps, debugFlag);
		} catch (Throwable t) {
			// For some reason the 2-args form isn't printing stack traces from OutOf-Permgen exceptions.
			getLogger().error("Caught Exception: {} \n********************* Bonus Direct Stack Trace to STDERR", t);
			t.printStackTrace();
		}
	}

}
