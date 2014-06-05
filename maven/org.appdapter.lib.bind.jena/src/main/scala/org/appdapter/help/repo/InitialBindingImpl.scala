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
import org.appdapter.core.store.{InitialBinding, ModelClient}

import com.hp.hpl.jena.query.QuerySolutionMap
import com.hp.hpl.jena.rdf.model.RDFNode
/**
 * @author Stu B. <www.texpedient.com>
 */

class InitialBindingImpl(private val myModelClient: ModelClient) extends InitialBinding {
  private val mySolutionMap = new QuerySolutionMap();

  def getQSMap: QuerySolutionMap = mySolutionMap

  def bindNode(vName: String, node: RDFNode) = {
    mySolutionMap.add(vName, node)
  }
  def bindQName(vName: String, resQName: String): Unit = {
    bindNode(vName, myModelClient.makeResourceForQName(resQName))
  }
  def bindURI(vName: String, resURI: String): Unit = {
    bindNode(vName, myModelClient.makeResourceForURI(resURI))
  }
  def bindIdent(vName: String, id: Ident): Unit = {
    bindNode(vName, myModelClient.makeResourceForIdent(id))
  }
  def bindLiteralString(vName: String, litString: String): Unit = {
    bindNode(vName, myModelClient.makeStringLiteral(litString))
  }

}
