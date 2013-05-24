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
import org.appdapter.impl.store.{FancyRepo, QueryHelper, ResourceResolver};
import com.hp.hpl.jena.query.{Query, QueryFactory, QueryExecution, QueryExecutionFactory, QuerySolution, QuerySolutionMap, Syntax};

/**
 * @author Stu B. <www.texpedient.com>
 */

class RepoHelper {
	val mySH = new SolutionHelper();
	
    def findSolutionsAsSolutionList(fr:FancyRepo, parsedQ: Query, qInitBinding: QuerySolution) : SolutionList = {
		import scala.collection.JavaConversions._
		val natSL : scala.collection.mutable.Buffer[QuerySolution] = fr.findAllSolutions(parsedQ, qInitBinding);
		mySH.makeSolutionList(natSL);
	}
}