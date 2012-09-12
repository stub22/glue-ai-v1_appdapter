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
import scala.collection.JavaConversions._

import com.hp.hpl.jena.query.QuerySolution

/**
 * @author Ryan Biggs
 */

// A very simple class so the Solution can be handed to external classes without them needing to depend directly on Jena
class Solution(var solution: QuerySolution) {}


// A very simple class so the SolutionList can be handed to external classes without Scala-Java conversion concerns
class SolutionList {
  var list: scala.collection.mutable.Buffer[Solution] = new scala.collection.mutable.ArrayBuffer[Solution]
  lazy val javaList: java.util.List[Solution] = list
}



// A very simple class so the SolutionMap can be handed to external classes without Scala-Java conversion concerns
class SolutionMap[T] {
  val map = new scala.collection.mutable.HashMap[T, Solution]
  
  def getJavaIterator: java.util.Iterator[T] = map.keysIterator
}

