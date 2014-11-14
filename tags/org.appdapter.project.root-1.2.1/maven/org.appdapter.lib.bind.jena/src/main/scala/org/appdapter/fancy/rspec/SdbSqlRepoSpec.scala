/*
 *  Copyright 2014 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.fancy.rspec
import org.appdapter.core.name.{Ident, FreeIdent}
import org.appdapter.fancy.loader.{SdbSqlRepoFactoryLoader}
import com.hp.hpl.jena.rdf.model.Model

class SdbSqlRepoSpec(configPath: String, optConfResCL: ClassLoader, dirGraphID: Ident) extends RepoSpec {
	
  def this(cPath: String, optCL: ClassLoader, dirGraphUriPrefix: String, dirGraphLocalName: String) =
	  this(cPath, optCL, new FreeIdent(dirGraphUriPrefix + dirGraphLocalName, dirGraphLocalName))
  
  override protected def makeRepo() = SdbSqlRepoFactoryLoader.makeSdbSqlRepo(configPath, optConfResCL, dirGraphID)
  
  override protected def   makeDirectoryModel(): Model  = getOrMakeRepo.getDirectoryModel
  
 
}