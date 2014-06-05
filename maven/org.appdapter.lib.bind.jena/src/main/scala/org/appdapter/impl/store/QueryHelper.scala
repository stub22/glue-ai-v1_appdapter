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

package org.appdapter.impl.store

import org.appdapter.core.log.BasicDebugger

import com.hp.hpl.jena.query.{ Query, QueryExecutionFactory, QueryFactory, QuerySolution, ResultSet, ResultSetFactory, ResultSetFormatter, Syntax }
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.shared.PrefixMapping

/**
 * @author Stu B. <www.texpedient.com>
 */

object QueryHelper extends BasicDebugger {

  def execModelQueryWithPrefixHelp(model: Model, qText: String): ResultSet = {

    val qBaseURI: String = null;
    val query = new Query();
    // Query prefixes must be applied before the query is parsed.
    val queryPrefixMapping: PrefixMapping = model;
    query.setPrefixMapping(queryPrefixMapping);
    QueryFactory.parse(query, qText, qBaseURI, Syntax.syntaxSPARQL);
    // getLogger().debug("Parsed Query {}", query);
    val qSolnInit: QuerySolution = null; // Initial binding is optional, currently unused.

    val qExec = QueryExecutionFactory.create(query, model, qSolnInit);
    val qrs: ResultSet = qExec.execSelect();
    qrs;
  }

  def buildQueryResultXML(qrs: ResultSet): String = {
    val qrsrw = ResultSetFactory.makeRewindable(qrs);
    val resultXML: String = ResultSetFormatter.asXMLString(qrsrw);
    resultXML;
  }

  def buildQueryResultMap(qrs: ResultSet, keyVar: String, valVar: String): Map[String, String] = {
    null;
  }
}
