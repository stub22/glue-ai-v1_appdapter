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
import org.appdapter.core.matdat.FancyRepo

trait QueryInterface {
  
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
  
  /** Gets a string literal from a query solution located in a SolutionMap and keyed by a selector URI
   *
   * @param solutionMap The SolutionMap in which the desired literal is located
   * @param selectorUri The key URI which selects the desired solution
   * @param variableName The query variable name for the string literal desired
   * @return The selected String literal
   */
  def getStringFromSolution(solutionMap:SolutionMap[Ident], selectorUri:Ident, variableName:String): String
  
  /** Gets a string literal from a query solution located in a SolutionMap and keyed by a selector String
   *
   * @param solutionMap The SolutionMap in which the desired literal is located
   * @param selector The key String which selects the desired solution
   * @param variableName The query variable name for the string literal desired
   * @return The selected String literal
   */
  def getStringFromSolution(solutionMap:SolutionMap[String], selector:String, variableName:String): String
  
  /** Gets a string literal from a single query solution
   *
   * @param solution The Solution in which the desired string is located
   * @param variableName The query variable name for the string literal desired
   * @return The selected string literal
   */
  def getStringFromSolution(solution:Solution, variableName:String): String
   
  /** Gets a string literal from a single query solution with a provided default if solution variable is not found
   *
   * @param solution The Solution in which the desired string is located
   * @param variableName The query variable name for the string literal desired
   * @param default The String to return if the query variable is not found in solution
   * @return The selected string literal
   */
  def getStringFromSolution(solution:Solution, variableName:String, default:String): String
  
  /** Gets an IndexedSeq of string literals from each of the query solutions located in a SolutionList
   *
   * @param solutionList The SolutionList in which the desired strings are located
   * @param variableName The query variable name for the string literals desired
   * @return The selected string literals
   */
  def getStringsFromSolution(solutionList:SolutionList, variableName:String): scala.collection.immutable.IndexedSeq[String]
  
  /** Gets a Java list of string literals from each of the query solutions located in a SolutionList
   *
   * @param solutionList The SolutionList in which the desired strings are located
   * @param variableName The query variable name for the string literals desired
   * @return The selected string literals
   */
  def getStringsFromSolutionAsJava(solutionList:SolutionList, variableName:String): java.util.List[String]
  
  /** Gets a resource in the form of an Ident from a single query solution
   *
   * @param solution The Solution in which the desired resource is located
   * @param variableName The query variable name for the resource desired
   * @return The selected resource
   */
  def getIdentFromSolution(solution:Solution, variableName:String): Ident
  
  /** Gets an ArrayBuffer of resources in the form of Idents from each of the query solutions located in a SolutionList
   *
   * @param solutionList The SolutionList in which the desired resources are located
   * @param variableName The query variable name for the resources desired
   * @return The selected resources
   */
  def getIdentsFromSolution(solutionList:SolutionList, variableName:String): scala.collection.mutable.ArrayBuffer[Ident]
  
  /** Gets a Java list of resources in the form of Idents from each of the query solutions located in a SolutionList
   *
   * @param solutionList The SolutionList in which the desired resources are located
   * @param variableName The query variable name for the resources desired
   * @return The selected resources
   */
  def getIdentsFromSolutionAsJava(solutionList:SolutionList, variableName:String): java.util.List[Ident]
  
  /** Gets a float literal from a query solution located in a SolutionMap and keyed by a selector String
   *
   * @param solutionMap The SolutionMap in which the desired literal is located
   * @param selector The key Ident which selects the desired solution
   * @param variableName The query variable name for the float literal desired
   * @return The selected Float literal
   */
  def getFloatFromSolution(solutionMap:SolutionMap[Ident], selector:Ident, variableName:String): Float

  /** Gets a float literal from a single query solution with a provided default if solution variable is not found
   *
   * @param solution The Solution in which the desired float is located
   * @param variableName The query variable name for the float literal desired
   * @param default The Float to return if the query variable is not found in solution
   * @return The selected float literal
   */
  def getFloatFromSolution(solution:Solution, variableName:String, default:Float): Float
  
  /** Gets a double literal from a query solution located in a SolutionMap and keyed by a selector Ident
   *
   * @param solutionMap The SolutionMap in which the desired literal is located
   * @param selector The key Ident which selects the desired solution
   * @param variableName The query variable name for the double literal desired
   * @return The selected Double literal
   */
  def getDoubleFromSolution(solutionMap:SolutionMap[Ident], selector:Ident, variableName:String): Double
  
  /** Gets a double literal from a query solution located in a SolutionMap and keyed by a selector string
   *
   * @param solutionMap The SolutionMap in which the desired literal is located
   * @param selector The key String which selects the desired solution
   * @param variableName The query variable name for the double literal desired
   * @return The selected Double literal
   */
  def getDoubleFromSolution(solutionMap:SolutionMap[String], selector:String, variableName:String): Double
  
   /** Gets a double literal from a single query solution with a provided default if solution variable is not found
   *
   * @param solution The Solution in which the desired double is located
   * @param variableName The query variable name for the double literal desired
   * @param default The double to return if the query variable is not found in solution
   * @return The selected double literal
   */
  def getDoubleFromSolution(solution:Solution, variableName:String, default:Double): Double
  
  /** Gets an integer literal from a query solution located in a SolutionMap and keyed by a selector Ident
   *
   * @param solutionMap The SolutionMap in which the desired literal is located
   * @param selector The key Ident which selects the desired solution
   * @param variableName The query variable name for the integer literal desired
   * @return The selected integer literal
   */
  def getIntegerFromSolution(solutionMap:SolutionMap[Ident], selector:Ident, variableName:String): Int
 
  /** Gets a boolean literal from a query solution located in a SolutionMap and keyed by a selector Ident
   *
   * @param solutionMap The SolutionMap in which the desired literal is located
   * @param selector The key Ident which selects the desired solution
   * @param variableName The query variable name for the boolean literal desired
   * @return The selected boolean literal
   */
  def getBooleanFromSolution(solutionMap:SolutionMap[Ident], selector:Ident, variableName:String): Boolean
  
  /** Gets a boolean literal from a single query solution
   *
   * @param solution The Solution in which the desired solution is located
   * @param variableName The query variable name for the boolean literal desired
   * @return The selected boolean literal
   */
  def getBooleanFromSolution(solution:Solution, variableName:String): Boolean
  
}
