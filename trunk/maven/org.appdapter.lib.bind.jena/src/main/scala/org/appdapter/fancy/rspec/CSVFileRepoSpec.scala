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
import java.io.{ InputStreamReader, Reader }

import org.appdapter.core.log.BasicDebugger
import org.appdapter.fancy.loader.{ CsvFileSheetLoader }



/**
 * @author Stu B. <www.texpedient.com>
 * @author LogicMoo B. <www.logicmoo.com>
 *
 * We implement a Excel Spreedsheet reader  backed Appdapter "repo" (read-only, but reloadable from updated source data).
 *   (easier to Save a Google Doc to a Single CsvFile Spreadsheet than several .Csv files!)
 *   Uses Apache POI (@see http://poi.apache.org/)
 */

class CSVFileRepoSpec(dirSheet: String, namespaceSheet: String = null,
  fileModelCLs: java.util.List[ClassLoader] = null) extends RepoSpecForDirectory {
  override def getDirectoryModel = CsvFileSheetLoader.readDirectoryModelFromCsvFile(dirSheet, fileModelCLs, namespaceSheet)
  override def toString: String = dirSheet
}
