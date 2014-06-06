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

package org.appdapter.core.matdat

import java.io.Reader
import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.store.ExtendedFileStreamUtils
import org.appdapter.core.store.dataset.{ RepoDatasetFactory, SpecialRepoLoader }
import com.hp.hpl.jena.query.{ Dataset, QuerySolution, ResultSet, ResultSetFactory }
import com.hp.hpl.jena.rdf.model.{ Literal, Model, RDFNode, Resource }
import org.appdapter.impl.store.QueryHelper
import org.appdapter.impl.store.FancyRepo
import org.appdapter.core.repo.InstallableRepoReader
import org.appdapter.core.repo._
import org.appdapter.core.repo.RepoSpec
import org.appdapter.core.repo.FancyRepoLoader

/**
 * @author Stu B. <www.texpedient.com>
 * @author LogicMoo B. <www.logicmoo.com>
 *
 * We implement a Excel Spreedsheet reader  backed Appdapter "repo" (read-only, but reloadable from updated source data).
 *   (easier to Save a Google Doc to a Single XLSX Spreadsheet than several .Csv files!)
 *   Uses Apache POI (@see http://poi.apache.org/)
 */
class OfflineXlsSheetRepoSpec(sheetLocation: String, namespaceSheet: String, dirSheet: String,
  fileModelCLs: java.util.List[ClassLoader] = null) extends RepoSpecForDirectory {
  override def getDirectoryModel = XLSXSheetRepoLoader.readDirectoryModelFromXLSX(sheetLocation, namespaceSheet, dirSheet, fileModelCLs: java.util.List[ClassLoader])
  //override def makeRepo(): GoogSheetRepo = XLSXSheetRepoLoader.loadXLSXSheetRepo(sheetLocation, namespaceSheet, dirSheet, fileModelCLs, this)
  override def toString: String = "xlsx:/" + sheetLocation + "/" + namespaceSheet + "/" + dirSheet
}

/// this is a registerable loader
class XLSXSheetRepoLoader extends InstallableRepoReader {
  override def makeRepoSpec(path: String, v3: Array[String], cLs: java.util.List[ClassLoader]) = new OfflineXlsSheetRepoSpec(v3(0), v3(1), v3(3), cLs)
  override def getExt = "xlsx"
  override def getContainerType() = "ccrt:XlsxWorkbookRepo"
  override def getSheetType() = "ccrt:XlsxSheet"
  override def loadModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset, dirModel: Model, fileModelCLs: java.util.List[ClassLoader]) {
    XLSXSheetRepoLoader.loadSheetModelsIntoTargetDataset(repo, mainDset, dirModel, fileModelCLs)
  }
}

object XLSXSheetRepoLoader extends BasicDebugger {

  private[core] def loadSheetModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset, myDirectoryModel: Model, fileModelCLs: java.util.List[ClassLoader]) = {

    val nsJavaMap: java.util.Map[String, String] = myDirectoryModel.getNsPrefixMap()

    val msqText = """
			select ?container ?key ?sheet ?name ?unionOrReplace
				{
					?container  a ccrt:XlsxWorkbookRepo; ccrt:key ?key.
					?sheet a ccrt:XlsxSheet; ccrt:sourcePath ?name; ccrt:repo ?container.
      				OPTIONAL { ?sheet  a ?unionOrReplace. FILTER (?unionOrReplace = ccrt:UnionModel) }
				}
		"""

    val msRset = QueryHelper.execModelQueryWithPrefixHelp(myDirectoryModel, msqText);
    import scala.collection.JavaConversions._;
    while (msRset.hasNext()) {
      val qSoln: QuerySolution = msRset.next();

      val containerRes: Resource = qSoln.getResource("container");
      val sheetRes: Resource = qSoln.getResource("sheet");
      val unionOrReplaceRes: Resource = qSoln.getResource("unionOrReplace");
      val sheetName_Lit: Literal = qSoln.getLiteral("name")
      val sheetLocation_Lit: Literal = qSoln.getLiteral("key")
      getLogger.debug("containerRes=" + containerRes + ", sheetRes=" + sheetRes + ", name="
        + sheetName_Lit + ", key=\"" + sheetLocation_Lit + "\", union= " + unionOrReplaceRes)

      val sheetName = sheetName_Lit.getString();
      repo.addLoadTask(sheetName, new Runnable() {
        def run() {
          val sheetLocation = sheetLocation_Lit.getString();
          val sheetModel: Model = readModelSheetXLSX(sheetLocation, sheetName, nsJavaMap, fileModelCLs);
          getLogger.debug("Read sheetModel: {}", sheetModel)
          val graphURI = sheetRes.getURI();
          FancyRepoLoader.replaceOrUnion(mainDset, unionOrReplaceRes, graphURI, sheetModel);
        }
      })
    }

  }

  final private[core] def readModelSheetXLSX(sheetLocation: String, sheetName: String, nsJavaMap: java.util.Map[String, String], fileModelCLs: java.util.List[ClassLoader]): Model = {
    val tgtModel: Model = RepoDatasetFactory.createPrivateMemModel
    tgtModel.setNsPrefixes(nsJavaMap)
    val modelInsertProc = new SemSheet.ModelInsertSheetProc(tgtModel);
    val efsu = new ExtendedFileStreamUtils()
    val reader: Reader = efsu.getWorkbookSheetCsvReaderAt(sheetLocation, sheetName, fileModelCLs);
    MatrixData.processSheetR(reader, modelInsertProc.processRow);
    getLogger.debug("tgtModel=" + tgtModel)
    tgtModel;
  }

  final private[core] def readDirectoryModelFromXLSX(sheetLocation: String, namespaceSheetName: String, dirSheetName: String, fileModelCLs: java.util.List[ClassLoader] = null): Model = {
    getLogger.debug("readDirectoryModelFromXLSX - start")
    val efsu = new ExtendedFileStreamUtils()
    val namespaceSheetReader = efsu.getWorkbookSheetCsvReaderAt(sheetLocation, namespaceSheetName, fileModelCLs);
    val nsJavaMap: java.util.Map[String, String] = MatrixData.readJavaMapFromSheetR(namespaceSheetReader);
    getLogger.debug("Got NS map: " + nsJavaMap)
    val dirModel: Model = readModelSheetXLSX(sheetLocation, dirSheetName, nsJavaMap, fileModelCLs);
    dirModel;
  }

  // Modeled on GoogSheetRepo.loadTestSheetRepo
  final private[core] def loadXLSXSheetRepo(sheetLocation: String, namespaceSheetName: String, dirSheetName: String,
    fileModelCLs: java.util.List[ClassLoader]): FancyRepo = {
    // Read the namespaces and directory sheets into a single directory model.
    val dirModel: Model = XLSXSheetRepoLoader.readDirectoryModelFromXLSX(sheetLocation, namespaceSheetName, dirSheetName, fileModelCLs)
    // Construct a repo around that directory
    //val shRepo = new XLSXSheetRepo(dirModel, fileModelCLs);   
    // Doug's locally testing this replacement   
    val spec = new OfflineXlsSheetRepoSpec(sheetLocation, namespaceSheetName, dirSheetName, fileModelCLs);
    val shRepo = new DirectRepo(spec, "xlsx:" + sheetLocation + "/" + namespaceSheetName + "/" + dirSheetName, dirModel, fileModelCLs)
    // Load the rest of the repo's initial *sheet* models, as instructed by the directory.
    getLogger().debug("Loading Sheet Models")
    shRepo.getMainQueryDataset();

    //    shRepo.loadSheetModelsIntoMainDataset()
    // Load the rest of the repo's initial *file/resource* models, as instructed by the directory.
    //getLogger().debug("Loading File Models")
    shRepo.loadSheetModelsIntoMainDataset();
    //shRepo.loadDerivedModelsIntoMainDataset(fileModelCLs)
    //shRepo.loadFileModelsIntoMainDataset(fileModelCLs)
    shRepo
  }

}