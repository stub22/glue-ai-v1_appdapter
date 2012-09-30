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

import org.appdapter.core.name.Ident

import org.appdapter.impl.store.{FancyRepo};

trait RepoClient {
  
  /** Returns the current cached SheetRepo
   *
   * @return The test sheet SheetRepo
   */
  def getRepo: FancyRepo
  
  /** Queries the configuration spreadsheet (with query sheet URI currently set in code as final val QUERY_SHEET).
   *  Updates the query specified by queryUri with the graph ident in qGraph
   *
   * @param queryUri The QName of the query which should be run, found on the query sheet currently set in code
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @param qGraph The Ident of the graph to be used in query
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getQueryResultMap(queryUri:String, keyVarName:String, qGraph:Ident): SolutionMap[Ident]
  
  /** Queries the configuration spreadsheet (with query sheet URI currently set in code as final val QUERY_SHEET).

   *
   * @param queryUri The QName of the query which should be run, found on the query sheet currently set in code
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getQueryResultMap(queryUri:String, keyVarName:String): SolutionMap[Ident]
  
  /** Queries the provided SheetRepo (with query sheet URI currently set in code as final val QUERY_SHEET).
   *
   * @param queryUri The QName of the query which should be run, found on the query sheet currently set in code
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @param sr The SheetRepo to be referenced for query
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getQueryResultMap(queryUri:String, keyVarName:String, sr:FancyRepo): SolutionMap[Ident]
  
  /** Queries the configuration spreadsheet with a provided query
   *
   * @param qText The text of the query to be run
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getTextQueryResultMap(qText:String, keyVarName:String): SolutionMap[Ident]
  
  /** Queries the configuration spreadsheet with a provided query and specified qGraph
   *
   * @param qText The text of the query to be run
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @param qGraph The Ident of the graph to be used in query
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getTextQueryResultMap(qText:String, keyVarName:String, qGraph:Ident): SolutionMap[Ident]
  
  /** Queries the provided SheetRepo with a provided query
   *
   * @param qText The text of the query to be run
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @param sr The SheetRepo to be referenced for query
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getTextQueryResultMap(qText:String, keyVarName:String, sr:FancyRepo): SolutionMap[Ident]
  
  /** Queries the provided SheetRepo with a provided query
   *
   * @param qText The text of the query to be run
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @param sr The SheetRepo to be referenced for query
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getTextQueryResultMapByStringKey(qText:String, keyVarName:String, sr:FancyRepo): SolutionMap[String]
  
  /** Queries the configuration spreadsheet with a provided query and specified qGraph
   *
   * @param qText The text of the query to be run
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @param qGraph The Ident of the graph to be used in query
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getTextQueryResultMapByStringKey(qText:String, keyVarName:String, qGraph:Ident): SolutionMap[String]
  
  /** Queries the configuration spreadsheet with a provided query
   *
   * @param qText The text of the query to be run
   * @param keyVarName The query variable name of resources by which the SolutionMap should be keyed
   * @return A SolutionMap of QuerySolutions with keys of the URIs of instances of keyVarName in solutions
   */
  def getTextQueryResultMapByStringKey(qText:String, keyVarName:String): SolutionMap[String]
  
  /** Queries the configuration spreadsheet (with query sheet URI currently set in code as final val QUERY_SHEET).
   *
   * @param queryUri The QName of the query which should be run, found on the query sheet currently set in code
   * @return A SolutionList of QuerySolutions
   */
  def getQueryResultList(queryUri:String): SolutionList
  
  /** Queries the configuration spreadsheet (with query sheet URI currently set in code as final val QUERY_SHEET)
   *  with graph Ident
   *
   * @param queryUri The QName of the query which should be run, found on the query sheet currently set in code
   * @param qGraph The Ident of the graph to be used in query
   * @return A SolutionList of QuerySolutions
   */
  def getQueryResultList(queryUri:String, qGraph:Ident): SolutionList
  
  /** Queries the provided SheetRepo (with query sheet URI currently set in code as final val QUERY_SHEET).
   *
   * @param queryUri The QName of the query which should be run, found on the query sheet currently set in code
   * @param sr The SheetRepo to be referenced for query
   * @return A SolutionList of QuerySolutions
   */
  def getQueryResultList(queryUri:String, sr:FancyRepo): SolutionList
  
  /** Queries the configuration spreadsheet with a provided query
   *
   * @param qText The text of the query to be run
   * @return A SolutionList of QuerySolutions
   */
  def getTextQueryResultList(qText:String): SolutionList
  
  /** Queries the configuration spreadsheet with a provided query and graph Ident
   *
   * @param qText The text of the query to be run
   * @param qGraph The Ident of the graph to be used in query
   * @return A SolutionList of QuerySolutions
   */
  def getTextQueryResultList(qText:String, qGraph:Ident): SolutionList
  
  /** Queries the provided SheetRepo with a provided query
   *
   * @param qText The text of the query to be run
   * @param sr The SheetRepo to be referenced for query
   * @return A SolutionList of QuerySolutions
   */
  def getTextQueryResultList(qText: String, sr:FancyRepo): SolutionList
 
  /** Gets a query from the query sheet page (specfied in code as QUERY_SHEET) of the test sheet
   *
   * @param queryUri The QName of the query which should be returned, found on the query sheet currently set in code
   * @return String containing query
   */
  def getQuery(queryUri:String): String
  
  /** Gets a query from the query sheet page (specfied in code as QUERY_SHEET) of the provided SheetRepo
   *
   * @param queryUri The QName of the query which should be returned, found on the query sheet currently set in code
   * @param sr The sheet repo in which the queries are located
   * @return String containing query
   */
  def getQuery(queryUri:String, sr:FancyRepo): String
  
  /** A convenience method to replace a query variable (indicated with "!!" prefix) in a query with its String value
   *
   * @param query The text of the query to be modified
   * @param queryVarName The name of the query variable (with "!!" prefix omitted)
   * @param value The value of the query variable
   * @return New query with variable replaced with literal
   */
  def setQueryVar(query:String, queryVarName: String, value: String): String
  
  /** A convenience method to replace a query variable (indicated with "!!" prefix) in a query with its URI value
   *
   * @param query The text of the query to be modified
   * @param queryVarName The name of the query variable (with "!!" prefix omitted)
   * @param value The value of the query variable
   * @return New query with variable replaced with literal
   */
  def setQueryVar(query:String, queryVarName: String, value: Ident): String
  
  /** A convenience method to retrieve a query template from sheet and replace a query variable
   * (indicated with "!!" prefix) with its URI value in a single step
   *
   * @param templateUri The URI of the query template to be retrieved
   * @param queryVarName The name of the query variable (with "!!" prefix omitted)
   * @param queryVarValue The value of the query variable
   * @return Completed query with variable replaced with literal
   */
  def getCompletedQueryFromTemplate(templateUri:String, queryVarName: String, queryVarValue: Ident): String
  

}
