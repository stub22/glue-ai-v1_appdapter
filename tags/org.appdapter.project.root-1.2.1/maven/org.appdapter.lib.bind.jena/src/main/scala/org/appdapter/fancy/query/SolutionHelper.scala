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

package org.appdapter.fancy.query

import scala.collection.JavaConversions.{ asScalaBuffer, bufferAsJavaList, seqAsJavaList }

import org.appdapter.core.name.{FreeIdent, Ident}

import com.hp.hpl.jena.query.QuerySolution

/**
 * @author Ryan Biggs
 * @author Stu B. <www.texpedient.com>
 * (Stu moved Ryan's stateless solution functions from QueryEmitter into here, and shortened method names)
 */

class SolutionHelper {
  /**
   * Gets a string literal from a query solution located in a SolutionMap and keyed by a selector URI
   *
   * @param solutionMap The SolutionMap in which the desired solution is located
   * @param selectorUri The key URI which selects the desired solution
   * @param variableName The query variable name for the string literal desired
   * @return The selected String literal
   */
  def pullString(solutionMap: SolutionMap[Ident], selectorUri: Ident, variableName: String) = {
    var literal: String = null
    if (solutionMap.map contains selectorUri) {
      literal = solutionMap.map(selectorUri).getStringResultVar(variableName)
    }
    literal
  }

  /**
   * Gets a string literal from a query solution located in a SolutionMap and keyed by a selector String
   *
   * @param solutionMap The SolutionMap in which the desired solution is located
   * @param selector The key String which selects the desired solution
   * @param variableName The query variable name for the string literal desired
   * @return The selected String literal
   */
  def pullString(solutionMap: SolutionMap[String], selector: String, variableName: String) = {
    var literal: String = null
    if (solutionMap.map contains selector) {
      literal = solutionMap.map(selector).getStringResultVar(variableName)
    }
    literal
  }

  /**
   * Gets a string literal from a single query solution
   *
   * @param solution The Solution in which the desired solution is located
   * @param variableName The query variable name for the string literal desired
   * @return The selected string literal
   */
  def pullString(solution: Solution, variableName: String): String = {
    pullString(solution, variableName, null)
  }

  /**
   * Gets a string literal from a single query solution with a provided default if solution variable is not found
   *
   * @param solution The Solution in which the desired solution is located
   * @param variableName The query variable name for the string literal desired
   * @param default The String to return if the query variable is not found in solution
   * @return The selected string literal
   */
  def pullString(solution: Solution, variableName: String, default: String): String = {
    var literal: String = default
    if (solution.checkResultVar(variableName)) {
      literal = solution.getStringResultVar(variableName)
    }
    literal
  }

  /**
   * Gets (an ArrayBuffer?) of string literals from each of the query solutions located in a SolutionList
   *
   * @param solutionList The SolutionList in which the desired solutions are located
   * @param variableName The query variable name for the string literals desired
   * @return The selected string literals
   */
  def pullStrings(solutionList: SolutionList, variableName: String) = {
    for (i <- 0 until solutionList.list.length) yield solutionList.list(i).getStringResultVar(variableName)
  }

  def pullStringsAsJava(solnList: SolutionList, vname: String): java.util.List[String] = pullStrings(solnList, vname)

  def pullIdent(solution: Solution, vname: String) = {
    var ident: Ident = null;
    if (solution.checkResultVar(vname)) {
      ident = solution.getIdentResultVar(vname)
    }
    ident
  }

  def pullIdents(solutionList: SolutionList, vname: String) = {
    val identList = new scala.collection.mutable.ArrayBuffer[Ident];
    solutionList.list.foreach(solution => {
      identList += solution.getIdentResultVar(vname)
    })
    identList
  }

  def pullIdentsAsJava(sList: SolutionList, vName: String): java.util.List[Ident] = pullIdents(sList, vName)

  def pullFloat(solutionMap: SolutionMap[Ident], selector: Ident, vName: String) = {
    var literal: Float = Float.NaN
    if (solutionMap.map contains selector) {
      literal = solutionMap.map(selector).getFloatResultVar(vName)
    }
    literal
  }

  def pullFloat(solution: Solution, vName: String, default: Float) = {
    var literal: Float = default
    if (solution.checkResultVar(vName)) {
      literal = solution.getFloatResultVar(vName)
    }
    literal
  }

  def pullDouble(solutionMap: SolutionMap[Ident], selector: Ident, vName: String) = {
    var literal: Double = Double.NaN
    if (solutionMap.map contains selector) {
      literal = solutionMap.map(selector).getDoubleResultVar(vName)
    }
    literal
  }

  def pullDouble(solutionMap: SolutionMap[String], selector: String, vName: String) = {
    var literal: Double = Double.NaN
    if (solutionMap.map contains selector) {
      literal = solutionMap.map(selector).getDoubleResultVar(vName)
    }
    literal
  }

  /**
   * Gets a double literal from a single query solution with a provided default if solution variable is not found
   *
   * @param solution The Solution in which the desired double is located
   * @param variableName The query variable name for the double literal desired
   * @param default The double to return if the query variable is not found in solution
   * @return The selected double literal
   */
  def pullDouble(solution: Solution, vName: String, default: Double): Double = {
    var literal: Double = default
    if (solution.checkResultVar(vName)) {
      literal = solution.getDoubleResultVar(vName)
    }
    literal
  }

  def pullInteger(solutionMap: SolutionMap[Ident], selector: Ident, vName: String): java.lang.Integer = {
    // I'd really prefer to set this to null to result in NPE in subsequent Java code if it's not found in solution
    // But Scala won't allow that for Int (or Float), and use of an Option seems inappropriate when this will be often called from Java code
    var literal: java.lang.Integer = null
    if (solutionMap.map contains selector) {
      literal = solutionMap.map(selector).getIntegerResultVar(vName)
    }
    literal
  }

  /**
   * Gets a boolean literal from a query solution located in a SolutionMap and keyed by a selector Ident
   *
   * @param solutionMap The SolutionMap in which the desired literal is located
   * @param selector The key Ident which selects the desired solution
   * @param variableName The query variable name for the boolean literal desired
   * @return The selected boolean literal
   */
  def pullBoolean(solutionMap: SolutionMap[Ident], selector: Ident, vName: String): Boolean = {
    var literal: Boolean = false
    if (solutionMap.map contains selector) {
      literal = solutionMap.map(selector).getBooleanResultVar(vName)
    }
    literal
  }

  /**
   * Gets a boolean literal from a single query solution
   *
   * @param solution The Solution in which the desired solution is located
   * @param variableName The query variable name for the boolean literal desired
   * @return The selected boolean literal
   */
  def pullBoolean(solution: Solution, vName: String) = {
    var literal: Boolean = false
    if (solution.checkResultVar(vName)) {
      literal = solution.getBooleanResultVar(vName)
    }
    literal
  }

  def makeSolutionList(jList: java.util.List[QuerySolution]): SolutionList = {
    val natSL: scala.collection.mutable.Buffer[QuerySolution] = jList
    val solutionList = new SolutionList
    natSL.foreach(qsol => solutionList.list += new Solution(qsol))
    solutionList
  }
  def makeSolutionMap(jList: java.util.List[QuerySolution], keyVarName: String): SolutionMap[Ident] = {
    val solutionMap = new SolutionMap[Ident]
    val natSL: scala.collection.mutable.Buffer[QuerySolution] = jList
    natSL.foreach(qSoln => {
      if (qSoln contains keyVarName) {
        val res = qSoln.getResource(keyVarName)
        val keyFI = new FreeIdent(res.getURI, res.getLocalName)
        val sol = new Solution(qSoln)
        solutionMap.map(keyFI) = sol
      }
    })
    solutionMap
  }
}
