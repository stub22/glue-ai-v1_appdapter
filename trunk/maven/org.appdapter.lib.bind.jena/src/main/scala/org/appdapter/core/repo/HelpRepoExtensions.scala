/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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

package org.appdapter.core.repo

import org.appdapter.help.repo.Solution

// This class contains supplements to Solution/QueryEmitter/QueryInterface in org.appdapter.core.repo
// which should be relocated there as soon as possible.
object HelpRepoExtensions {

  // In Solution.scala in class SolutionMap[T], we should add this line:
  // lazy val javaMap: java.util.Map[T, Solution] = map
  // This method provides that functionality for now
  import scala.collection.JavaConversions._
  def convertToJavaMap[T](scalaMap: scala.collection.mutable.HashMap[T, Solution]): java.util.Map[T, Solution] = scalaMap
}
