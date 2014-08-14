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

package org.appdapter.fancy.loader
import org.appdapter.core.loader.{SpecialRepoLoader, ExtendedFileStreamUtils}
import org.appdapter.fancy.repo.FancyRepo
import org.appdapter.fancy.rspec.{RepoSpec, RepoSpecReader}
import com.hp.hpl.jena.query.{Dataset, QuerySolution}
import com.hp.hpl.jena.rdf.model.{Model, Resource}
/*
 * @author logicmoo
 */


abstract class InstallableRepoLoader extends RepoSpecReader {
  //override def getExt(): String = null
  override def makeRepoSpec(path: String, args: Array[String], cLs: java.util.List[ClassLoader]): RepoSpec = null
  def getContainerType(): String
  def getSheetType(): String
  def isDerivedLoader(): Boolean = false
  def loadModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset, dirModel: Model, fileModelCLs: java.util.List[ClassLoader],
		optUrlPrefix : String)
}
