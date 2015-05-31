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

package org.appdapter.xload.rspec

import org.appdapter.core.log.BasicDebugger
import org.appdapter.xload.fancy.{ FancyRepoLoader, InstallableRepoLoader}
import org.appdapter.xload.sheet.{ GoogSheetRepoLoader }
// import org.appdapter.fancy.rspec.{RepoSpec, RepoSpecForDirectory}

import org.appdapter.xload.repo.{ DirectRepo}
import org.appdapter.core.store.dataset.{ RepoDatasetFactory }
import org.appdapter.core.loader.{  SpecialRepoLoader }
import org.appdapter.fancy.query.QueryHelper
import com.hp.hpl.jena.query.{ Dataset, QuerySolution }
import com.hp.hpl.jena.rdf.model.{ Literal, Model, Resource }
/**
 * @author Stu B. <www.texpedient.com>
 * @author Douglas R. Miles <www.logicmoo.org>
 *
 * This is a DirModel Loader it contains static methods for loading Google Docs Speedsheets
 */

class GoogSheetRepoSpec(sheetKey: String, namespaceSheetNum: Int, dirSheetNum: Int,
		fileModelCLs: java.util.List[ClassLoader]) extends RepoSpecForDirectory {

  def this(sheetKey: String, namespaceSheetNum: Int, dirSheetNum: Int) = this(sheetKey, namespaceSheetNum, dirSheetNum, null);
  override protected def makeDirectoryModel = GoogSheetRepoLoader.readModelFromGoog(sheetKey, namespaceSheetNum, dirSheetNum)
  override def toString: String = "goog:/" + sheetKey + "/" + namespaceSheetNum + "/" + dirSheetNum
}
class OnlineSheetRepoSpec(sheetKey: String, namespaceSheetNum: Int, dirSheetNum: Int,
		fileModelCLs: java.util.List[ClassLoader]) extends RepoSpecForDirectory {
	
  def this(sheetKey: String, namespaceSheetNum: Int, dirSheetNum: Int) = this(sheetKey, namespaceSheetNum, dirSheetNum, null);
  override protected def makeDirectoryModel = GoogSheetRepoLoader.readModelFromGoog(sheetKey, namespaceSheetNum, dirSheetNum)
  override def toString: String = "goog:/" + sheetKey + "/" + namespaceSheetNum + "/" + dirSheetNum
}

