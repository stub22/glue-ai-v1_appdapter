/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.com).
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

/*
 *  Some software in this file is copyright by Hewlett Packard Company, LP 
 *  See important notices at bottom of this file.
 */

package org.appdapter.bind.rdf.jena.query;

import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.graph.GraphFactory;
import com.hp.hpl.jena.sparql.util.DatasetUtils;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Certain static funcs, many pulled  from com/hp/hpl/jena/query/junit/QueryTest.java
 *
 * @author      Stu B. <www.texpedient.com>, using code by Andy Seaborne of HP.
 * @version     @PERUSER_VERSION@
 * @copyright   derivative(HP, Appdapter)
 */
public class SPARQL_Utils {
	private static Logger theLogger = LoggerFactory.getLogger(SPARQL_Utils.class);

	public static boolean isNonemptyList(List L) {
		return ((L != null) && (L.size() > 0));
	}

	public static boolean doesQueryHaveDataset(Query query) {
		return isNonemptyList(query.getGraphURIs()) || isNonemptyList(query.getNamedGraphURIs());
	}

	public static Dataset createDataset(List defaultGraphURIs, List namedGraphURIs) {
		return DatasetUtils.createDataset(defaultGraphURIs, namedGraphURIs, null, null);
	}

	public static Resource nonBnodeValue(QuerySolution o, String v1, String v2) {
		Resource value1 = o.getResource(v1);
		if (value1 != null && !value1.isAnon() && value1.isURIResource())
			return value1;
		Resource value2 = o.getResource(v2);
		if (value2 != null && !value2.isAnon() && value2.isURIResource())
			return value2;
		return null;
	}

	public static Model resultSetToModel(ResultSet rs) {
		Model m = GraphFactory.makeDefaultModel();
		ResultSetFormatter.asRDF(m, rs);
		/*  Removed from Appdapter 2013-07-13, presumed unused.
		if ( m.getNsPrefixURI("rs") == null ) {
		    m.setNsPrefix("rs", com.hp.hpl.jena.vocabulary.ResultSet.getURI() ) ;
		}
		* 
		*/
		if (m.getNsPrefixURI("rdf") == null) {
			m.setNsPrefix("rdf", RDF.getURI());
		}
		if (m.getNsPrefixURI("xsd") == null) {
			m.setNsPrefix("xsd", XSDDatatype.XSD + "#");
		}
		return m;
	}

	static public boolean resultSetEquivalent(ResultSet rs1, ResultSet rs2) {
		Model model2 = resultSetToModel(rs2);
		return resultSetMatchesModel(rs1, model2);
	}

	static public boolean resultSetMatchesModel(ResultSet rs1, Model model2) {
		Model model1 = resultSetToModel(rs1);
		return model1.isIsomorphicWith(model2);
	}

	public static String execQueryToProduceXML(Model m, String qryString) {
		String resultXml;
		ResultSet rs = execQueryToProduceResultSet(m, qryString);
		ResultSetRewindable lastResultSet = ResultSetFactory.makeRewindable(rs);
		resultXml = ResultSetFormatter.asXMLString(lastResultSet);

		return resultXml;
	}

	public static ResultSet execQueryToProduceResultSet(Model m, String qryString) {
		Query qry = QueryFactory.create(qryString);
		QueryExecution qe = null;
		// 3rd arg is initial binding, currently unused.
		qe = QueryExecutionFactory.create(qry, m, null);
		ResultSet rs = qe.execSelect();

		return rs;
	}

	public static String executeQueryFromFiles(String queryFileURL, String modelURL, String modelFormat, String modelBaseURI) throws Throwable {
		FileInputStream modelInputStream = new FileInputStream(modelURL);
		Model baseModel = ModelFactory.createDefaultModel();

		baseModel.read(modelInputStream, modelBaseURI, modelFormat);
		return executeQueryFromFile(queryFileURL, baseModel);
	}

	public static String executeQueryFromFile(String queryFileURL, Model inputModel) throws Throwable {

		Query parsedQuery = QueryFactory.read(queryFileURL, null, Syntax.syntaxSPARQL); // , queryBaseURI, queryFileSyntax);
		QueryExecution qe = QueryExecutionFactory.create(parsedQuery, inputModel, null);

		ResultSet rs = qe.execSelect();
		ResultSetRewindable lastResultSet = ResultSetFactory.makeRewindable(rs);
		String resultXML = ResultSetFormatter.asXMLString(lastResultSet);
		return resultXML;
	}

	public static void dumpDatasetNames(Dataset dataset) throws Throwable {
		Iterator dsNameIterator = dataset.listNames();
		while (dsNameIterator.hasNext()) {
			String name = (String) dsNameIterator.next();
			theLogger.debug("dataset contains model named: " + name);
		}
	}

	public static String runQueryOverDataset(String queryFileURL, Dataset inputDataset) throws Throwable {

		Query parsedQuery = QueryFactory.read(queryFileURL, null, Syntax.syntaxSPARQL); // , queryBaseURI, queryFileSyntax);
		QueryExecution qe = QueryExecutionFactory.create(parsedQuery, inputDataset, null);

		ResultSet rs = qe.execSelect();
		ResultSetRewindable lastResultSet = ResultSetFactory.makeRewindable(rs);
		String resultXML = ResultSetFormatter.asXMLString(lastResultSet);
		return resultXML;
	}
}

/**
 *  Note:  Some software in this file is copyright by Hewlett Packard Company, LP 
 *  and is redistributed in MODIFIED form according to the terms of the following 
 *  license. 
 */

/*
* (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in the
*    documentation and/or other materials provided with the distribution.
* 3. The name of the author may not be used to endorse or promote products
*    derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
* IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
* IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
* INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
* NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
* DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
* THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
* THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

