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

package org.appdapter.help.repo

import org.appdapter.core.name.{Ident, FreeIdent}
import org.appdapter.core.store.{Repo}
import org.appdapter.impl.store.{FancyRepo, QueryHelper, ResourceResolver};
import org.appdapter.core.matdat.{SheetRepo}
import com.hp.hpl.jena.rdf.model.{Model, Statement, Resource, Property, Literal, RDFNode, ModelFactory, InfModel}

import com.hp.hpl.jena.query.{Query, QueryFactory, QueryExecution, QueryExecutionFactory, QuerySolution, QuerySolutionMap, Syntax};

import com.hp.hpl.jena.query.{ResultSet, ResultSetFormatter, ResultSetRewindable, ResultSetFactory};

import scala.collection.immutable.StringOps
import scala.collection.mutable.HashMap
import scala.collection.JavaConversions._

/**
 * @author Ryan Biggs
 A still-under-construction API layer for the handling of query-based config data.

 <br/>2012-09-29 Stu:  This class (+ associated interface) has some design issues
 which we need to work through.  It explodes the API into a cross-product that is probably
 not warranted.  However, its capture in this location is helpful for us to track the
 use of these patterns throughout higher level code (e.g. the Cogchar-render and -PUMA projects), 
 and so it is serving a useful purpose at present.  
 
 In particular the customized approach to query-variable replacement (using the nonstandard
 "!!" syntax) should be dropped in favor of standardized SPARQL pre-solution bindings, as 
 implemented in BasicRepoImpl, and exposed by the initBinding parameters of these methods  
 of  org.appdapter.core.store.QueryProcessor
 
public <ResType> ResType processQuery(Query parsedQuery, QuerySolution initBinding, JenaArqResultSetProcessor<ResType> resProc);

public List<QuerySolution> findAllSolutions(Query parsedQuery, QuerySolution initBinding);

As far as all the Java "SolutionMap" and "SolutionList" stuff, and particular type accessors:
Those methods and types are useful for java clients - OK for now - but they should be in some 
adapter classes, rather than bundled in as core pieces of the query-config interface.

 The extensive repetition of code in these method impls is bad Scala style.
 If this is really the API we want, it should probably be coded in Java.
 Scala isn't doing much for us here.
 
 *
 */

class RepoClientImpl(val myRepo : FancyRepo, val myQueryVarName : String, val myQuerySheetName : String ) extends RepoClient {
  
  private def ensureRepo {
	  // TODO: check repo health, log errors if any problems found
	//if (QueryTester.repo == null) {
	//  QueryTester.repo = QueryTester.loadSheetRepo
	//}
  }

  
  /** Returns the current cached SheetRepo
   *
   * @return The test sheet SheetRepo
   */
  def getRepo : FancyRepo = {
	ensureRepo
	myRepo
  }
  

	def getQueryVarName = myQueryVarName ; // QueryTester.GRAPH_QUERY_VAR
	
	def getQuerySheetName = myQuerySheetName; // QueryTester.QUERY_SHEET;
  
  /** Queries the configuration spreadsheet (with query sheet URI currently set in code as final val QUERY_SHEET).
   *
   * @param queryUri The QName of the query which should be run, found on the query sheet currently set in code
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getQueryResultMap(queryUri:String, keyVarName:String): SolutionMap[Ident]={
	ensureRepo
	getQueryResultMap(queryUri, keyVarName, getRepo)
  }
  
  /** Queries the configuration spreadsheet (with query sheet URI currently set in code as final val QUERY_SHEET).
   *  Updates the query specified by queryUri with the graph ident in qGraph
   *
   * @param queryUri The QName of the query which should be run, found on the query sheet currently set in code
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @param qGraph The Ident of the graph to be used in query
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getQueryResultMap(queryUri:String, keyVarName:String, qGraph:Ident): SolutionMap[Ident] = {
	val qText = getCompletedQueryFromTemplate(queryUri, getQueryVarName, qGraph)
	getTextQueryResultMap(qText, keyVarName)
  }
  
  /** Queries the provided SheetRepo (with query sheet URI currently set in code as final val QUERY_SHEET).
   *
   * @param queryUri The QName of the query which should be run, found on the query sheet currently set in code
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @param sr The SheetRepo to be referenced for query
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getQueryResultMap(queryUri:String, keyVarName:String, fr:FancyRepo): SolutionMap[Ident]={
	  val qText = fr.resolveIndirectQueryText(getQuerySheetName, queryUri)
	getTextQueryResultMap(qText, keyVarName, fr)
  }
  
  /** Queries the configuration spreadsheet with a provided query
   *
   * @param qText The text of the query to be run
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getTextQueryResultMap(qText:String, keyVarName:String): SolutionMap[Ident]={
	ensureRepo
	getTextQueryResultMap(qText, keyVarName, getRepo)
  }
  
  /** Queries the configuration spreadsheet with a provided query and specified qGraph
   *
   * @param qText The text of the query to be run
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @param qGraph The Ident of the graph to be used in query
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getTextQueryResultMap(qText:String, keyVarName:String, qGraph:Ident): SolutionMap[Ident] = {
	val query = setQueryVar(qText, getQueryVarName, qGraph)
	getTextQueryResultMap(query, keyVarName)
  }
  
  /** Queries the provided SheetRepo with a provided query
   *
   * @param qText The text of the query to be run
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @param sr The SheetRepo to be referenced for query
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getTextQueryResultMap(qText:String, keyVarName:String, fr:FancyRepo): SolutionMap[Ident]={
	val solutionMap = new SolutionMap[Ident]
	val parsedQ = fr.parseQueryText(qText);
	val solnList : scala.collection.mutable.Buffer[QuerySolution] = fr.findAllSolutions(parsedQ, null);
	solnList.foreach(solution => {
		if (solution contains keyVarName) {
		  solutionMap.map(new FreeIdent(solution.getResource(keyVarName).getURI, solution.getResource(keyVarName).getLocalName)) = new Solution(solution)
		}
	  })
	solutionMap
  }
  
  /** Queries the provided SheetRepo with a provided query
   *
   * @param qText The text of the query to be run
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @param sr The SheetRepo to be referenced for query
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getTextQueryResultMapByStringKey(qText:String, keyVarName:String, fr:FancyRepo): SolutionMap[String]={
	val solutionMap = new SolutionMap[String]
	val parsedQ = fr.parseQueryText(qText);
	val solnList : scala.collection.mutable.Buffer[QuerySolution] = fr.findAllSolutions(parsedQ, null);
	solnList.foreach(solution => {
		if (solution contains keyVarName) {
		  solutionMap.map(solution.getLiteral(keyVarName).getString) = new Solution(solution) 
		}
	  })
	solutionMap
  }
  
  /** Queries the configuration spreadsheet with a provided query and specified qGraph
   *
   * @param qText The text of the query to be run
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @param qGraph The Ident of the graph to be used in query
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getTextQueryResultMapByStringKey(qText:String, keyVarName:String, qGraph:Ident): SolutionMap[String] = {
	val query = setQueryVar(qText, getQueryVarName, qGraph)
	getTextQueryResultMapByStringKey(query, keyVarName)
  }
  
  /** Queries the configuration spreadsheet with a provided query
   *
   * @param qText The text of the query to be run
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getTextQueryResultMapByStringKey(qText:String, keyVarName:String): SolutionMap[String]={
	ensureRepo
	getTextQueryResultMapByStringKey(qText, keyVarName, getRepo);
  }
  
  /** Queries the configuration spreadsheet (with query sheet URI currently set in code as final val QUERY_SHEET).
   *
   * @param queryUri The QName of the query which should be run, found on the query sheet currently set in code
   * @return A SolutionList of QuerySolutions
   */
  def getQueryResultList(queryUri:String): SolutionList={
	ensureRepo
	getQueryResultList(queryUri, getRepo)
  }
  
  /** Queries the configuration spreadsheet (with query sheet URI currently set in code as final val QUERY_SHEET)
   *  with graph Ident
   *
   * @param queryUri The QName of the query which should be run, found on the query sheet currently set in code
   * @param qGraph The Ident of the graph to be used in query
   * @return A SolutionList of QuerySolutions
   */
  def getQueryResultList(queryUri:String, qGraph:Ident): SolutionList = {
	val qText = getCompletedQueryFromTemplate(queryUri, getQueryVarName, qGraph)
	getTextQueryResultList(qText);
  }
  
  /** Queries the provided SheetRepo (with query sheet URI currently set in code as final val QUERY_SHEET).
   *
   * @param queryUri The QName of the query which should be run, found on the query sheet currently set in code
   * @param sr The SheetRepo to be referenced for query
   * @return A SolutionList of QuerySolutions
   */
  def getQueryResultList(queryUri:String, fr:FancyRepo): SolutionList={
	val qText = fr.resolveIndirectQueryText(getQuerySheetName, queryUri)
	getTextQueryResultList(qText, fr);
  }
  
  /** Queries the configuration spreadsheet with a provided query
   *
   * @param qText The text of the query to be run
   * @return A SolutionList of QuerySolutions
   */
  def getTextQueryResultList(qText:String): SolutionList={
	ensureRepo
	getTextQueryResultList(qText, getRepo)
  }
  
  /** Queries the configuration spreadsheet with a provided query and graph Ident
   *
   * @param qText The text of the query to be run
   * @param qGraph The Ident of the graph to be used in query
   * @return A SolutionList of QuerySolutions
   */
  def getTextQueryResultList(qText:String, qGraph:Ident): SolutionList = {
	val query = setQueryVar(qText, getQueryVarName, qGraph)
	getTextQueryResultList(query);
  }
  
  /** Queries the provided SheetRepo with a provided query
   *
   * @param qText The text of the query to be run
   * @param sr The SheetRepo to be referenced for query
   * @return A SolutionList of QuerySolutions
   */
  def getTextQueryResultList(qText: String, fr:FancyRepo): SolutionList = {
	val solutionList = new SolutionList
	val parsedQ = fr.parseQueryText(qText);
	val nativeSolutionList: scala.collection.mutable.Buffer[QuerySolution] = fr.findAllSolutions(parsedQ, null)
	nativeSolutionList.foreach( solution => solutionList.list += new Solution(solution))
	solutionList
  }
 
  /** Gets a query from the query sheet page (specfied in code as QUERY_SHEET) of the test sheet
   *
   * @param queryUri The QName of the query which should be returned, found on the query sheet currently set in code
   * @return String containing query
   */
  def getQuery(queryUri:String):String={
	ensureRepo
	getRepo.resolveIndirectQueryText(getQuerySheetName, queryUri);
  }
  
  /** Gets a query from the query sheet page (specfied in code as QUERY_SHEET) of the provided SheetRepo
   *
   * @param queryUri The QName of the query which should be returned, found on the query sheet currently set in code
   * @param sr The sheet repo in which the queries are located
   * @return String containing query
   */
  def getQuery(queryUri:String, fr:FancyRepo):String = {
	fr.resolveIndirectQueryText(getQuerySheetName, queryUri);
  }
  
  /** A convenience method to replace a query variable (indicated with "!!" prefix) in a query with its String value
   *
   * @param query The text of the query to be modified
   * @param queryVarName The name of the query variable (with "!!" prefix omitted)
   * @param value The value of the query variable
   * @return New query with variable replaced with literal
   */
  def setQueryVar(query:String, queryVarName: String, value: String) = {
	var queryOps = new StringOps(query) // wasn't working implicitly for some reason
	queryOps.replaceAll("!!" + queryVarName, value)
  }
  
  /** A convenience method to replace a query variable (indicated with "!!" prefix) in a query with its URI value
   *
   * @param query The text of the query to be modified
   * @param queryVarName The name of the query variable (with "!!" prefix omitted)
   * @param value The value of the query variable
   * @return New query with variable replaced with literal
   */
  def setQueryVar(query:String, queryVarName: String, value: Ident): String = {
	var newQuery = ""
	if (query != null) {
	  newQuery = query.replaceAll("!!" + queryVarName, "<" + value.getAbsUriString + ">")
	}
	newQuery
  }
  
  def getCompletedQueryFromTemplate(templateUri:String, queryVarName: String, queryVarValue: Ident) = {
	val query = getQuery(templateUri)
	setQueryVar(query, queryVarName, queryVarValue)
  }
  

}
