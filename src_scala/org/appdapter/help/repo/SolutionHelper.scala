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

import scala.collection.immutable.StringOps
import scala.collection.mutable.HashMap
import scala.collection.JavaConversions._

/**
 * @author Ryan Biggs
 * @author Stu B. <www.texpedient.com>
 * (Stu moved Ryan's stateless solution functions from QueryEmitter into here, and shortened method names)
 */

class SolutionHelper {
  /** Gets a string literal from a query solution located in a SolutionMap and keyed by a selector URI
   *
   * @param solutionMap The SolutionMap in which the desired solution is located
   * @param selectorUri The key URI which selects the desired solution
   * @param variableName The query variable name for the string literal desired
   * @return The selected String literal
   */
  def pullString(solutionMap:SolutionMap[Ident], selectorUri:Ident, variableName:String) = {
	var literal: String = null
	if (solutionMap.map contains selectorUri) {
	  literal = solutionMap.map(selectorUri).solution.getLiteral(variableName).getString
	}
	literal
  }
  
  /** Gets a string literal from a query solution located in a SolutionMap and keyed by a selector String
   *
   * @param solutionMap The SolutionMap in which the desired solution is located
   * @param selector The key String which selects the desired solution
   * @param variableName The query variable name for the string literal desired
   * @return The selected String literal
   */
  def pullString(solutionMap:SolutionMap[String], selector:String, variableName:String) = {
	var literal: String = null
	if (solutionMap.map contains selector) {
	  literal = solutionMap.map(selector).solution.getLiteral(variableName).getString
	}
	literal
  }
  
  /** Gets a string literal from a single query solution
   *
   * @param solution The Solution in which the desired solution is located
   * @param variableName The query variable name for the string literal desired
   * @return The selected string literal
   */
  def pullString(solution:Solution, variableName:String): String = {
	pullString(solution, variableName, null)
  }
   
  /** Gets a string literal from a single query solution with a provided default if solution variable is not found
   *
   * @param solution The Solution in which the desired solution is located
   * @param variableName The query variable name for the string literal desired
   * @param default The String to return if the query variable is not found in solution
   * @return The selected string literal
   */
  def pullString(solution:Solution, variableName:String, default:String): String = {
	var literal: String = default
	if (solution.solution.contains(variableName)) {
	  literal = solution.solution.getLiteral(variableName).getString
	}
	literal
  }
  
  /** Gets (an ArrayBuffer?) of string literals from each of the query solutions located in a SolutionList
   *
   * @param solutionList The SolutionList in which the desired solutions are located
   * @param variableName The query variable name for the string literals desired
   * @return The selected string literals
   */
  def pullStrings(solutionList:SolutionList, variableName:String) = {
	for (i <- 0 until solutionList.list.length) yield solutionList.list(i).solution.getLiteral(variableName).getString
  }
  
  def pullStringsAsJava(solutionList:SolutionList, variableName:String): java.util.List[String] 
		= pullStrings(solutionList, variableName)
  
  def pullIdent(solution:Solution, variableName:String) = {
	var ident:Ident = null;
	if (solution.solution.contains(variableName)) {
	  ident = new FreeIdent(solution.solution.getResource(variableName).getURI, solution.solution.getResource(variableName).getLocalName)
	}
	ident
  }
  
  def pullIdents(solutionList:SolutionList, variableName:String) = {
	val identList = new scala.collection.mutable.ArrayBuffer[Ident];
	solutionList.list.foreach(solution => {
		identList += new FreeIdent(solution.solution.getResource(variableName).getURI, solution.solution.getResource(variableName).getLocalName)
	  })
	identList
  }
  
  def pullIdentsAsJava(solutionList:SolutionList, variableName:String): java.util.List[Ident] = pullIdents(solutionList, variableName)
  
  def pullFloat(solutionMap:SolutionMap[Ident], selector:Ident, variableName:String) = {
	var literal: Float = Float.NaN
	if (solutionMap.map contains selector) {
	  literal = solutionMap.map(selector).solution.getLiteral(variableName).getFloat
	}
	literal
  }
  
  def pullFloat(solution:Solution, variableName:String, default:Float) = {
	var literal: Float = default
	if (solution.solution.contains(variableName)) {
	  literal = solution.solution.getLiteral(variableName).getFloat
	}
	literal
  }
  
  def pullDouble(solutionMap:SolutionMap[Ident], selector:Ident, variableName:String) = {
	var literal: Double = Double.NaN
	if (solutionMap.map contains selector) {
	  literal = solutionMap.map(selector).solution.getLiteral(variableName).getDouble
	}
	literal
  }
  
  def pullDouble(solutionMap:SolutionMap[String], selector:String, variableName:String) = {
	var literal: Double = Double.NaN
	if (solutionMap.map contains selector) {
	  literal = solutionMap.map(selector).solution.getLiteral(variableName).getDouble
	}
	literal
  }
  
   /** Gets a double literal from a single query solution with a provided default if solution variable is not found
   *
   * @param solution The Solution in which the desired double is located
   * @param variableName The query variable name for the double literal desired
   * @param default The double to return if the query variable is not found in solution
   * @return The selected double literal
   */
  def pullDouble(solution:Solution, variableName:String, default:Double): Double = {
	var literal: Double = default
	if (solution.solution.contains(variableName)) {
	  literal = solution.solution.getLiteral(variableName).getDouble
	}
	literal
  }
  
  def pullInteger(solutionMap:SolutionMap[Ident], selector:Ident, variableName:String) = {
	// I'd really prefer to set this to null to result in NPE in subsequent Java code if it's not found in solution
	// But Scala won't allow that for Int (or Float), and use of an Option seems inappropriate when this will be often called from Java code
	var literal: Int = 0
	if (solutionMap.map contains selector) {
	  literal = solutionMap.map(selector).solution.getLiteral(variableName).getInt
	}
	literal
  }
  
  /** Gets a boolean literal from a query solution located in a SolutionMap and keyed by a selector Ident
   *
   * @param solutionMap The SolutionMap in which the desired literal is located
   * @param selector The key Ident which selects the desired solution
   * @param variableName The query variable name for the boolean literal desired
   * @return The selected boolean literal
   */
  def pullBoolean(solutionMap:SolutionMap[Ident], selector:Ident, variableName:String): Boolean = {
	var literal: Boolean = false
	if (solutionMap.map contains selector) {
	  literal = solutionMap.map(selector).solution.getLiteral(variableName).getBoolean
	}
	literal
  }
 
  /** Gets a boolean literal from a single query solution
   *
   * @param solution The Solution in which the desired solution is located
   * @param variableName The query variable name for the boolean literal desired
   * @return The selected boolean literal
   */
  def pullBoolean(solution:Solution, variableName:String) = {
	var literal: Boolean = false
	if (solution.solution contains variableName) {
	  literal = solution.solution.getLiteral(variableName).getBoolean
	}
	literal
  }
}
