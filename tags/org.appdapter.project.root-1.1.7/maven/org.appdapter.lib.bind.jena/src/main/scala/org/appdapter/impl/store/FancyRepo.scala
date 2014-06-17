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

package org.appdapter.impl.store

import scala.collection.immutable.StringOps
import org.appdapter.bind.rdf.jena.query.JenaArqQueryFuncs
import org.appdapter.core.log.{ BasicDebugger, Loggable }
import org.appdapter.core.name.Ident
import org.appdapter.core.repo.{ DatabaseRepo_BROKER, _ }
import org.appdapter.core.store.{ InitialBinding, ModelClient, Repo }
import org.appdapter.help.repo.InitialBindingImpl
import com.hp.hpl.jena.query.{ Query, QuerySolution, QuerySolutionMap }
import com.hp.hpl.jena.rdf.listeners.ObjectListener
import com.hp.hpl.jena.rdf.model.Literal
import org.appdapter.core.repo.DatabaseRepo_BROKER
import org.appdapter.core.repo.DatabaseRepoFactoryLoader

/**
 * @author Stu B. <www.texpedient.com>
 *
 * We implement a CSV (spreadsheet) backed Appdapter "repo" (read-only, but reloadable from updated source data).
 */

class RepoPrintinListener(val prefix: String) extends ObjectListener {
  override def added(x: Object): Unit = {
    println(prefix + " added: " + x);
  }
  override def removed(x: Object): Unit = {
    println(prefix + " removed: " + x);
  }

}
// abstract class FancyRepo() extends BasicRepoImpl with Repo.WithDirectory {
trait FancyRepo extends Repo.WithDirectory with ModelClientCore with Loggable {
  final val QUERY_QUERY_GRAPH_INPUT_VAR = "g";
  final val QUERY_QUERY_URI_RESULT_VAR = "qRes";

  final val QUERY_QUERY_TEXT = """
			SELECT ?g ?qRes ?queryTxt WHERE {
				GRAPH ?g {
					?qRes  a ccrt:SparqlQuery ; ccrt:queryText ?queryTxt .
				}
			}
		"""
  private var myOptCachedDirMC: Option[ModelClientImpl] = None

  private def getDirModelClientImpl: ModelClientImpl = {
    if (myOptCachedDirMC == None) {
      myOptCachedDirMC = Some(new ModelClientImpl(getDirectoryModel));
    }
    myOptCachedDirMC.get
  }
  override def getDirectoryModelClient: ModelClient = getDirModelClientImpl

  override def getFallbackModelClient: ModelClient = getDirectoryModelClient()

  // Allows us to implement the ModelClientCore API through delegation.
  override protected def getModel = getDirModelClientImpl.getModel;

  lazy val myQueryResQuery: Query = parseQueryResolutionQuery

  def findSingleQuerySolution(parsedQQ: Query, qInitBinding: QuerySolution): Option[QuerySolution] = {
    val solnJavaList: java.util.List[QuerySolution] = findAllSolutions(parsedQQ, qInitBinding);
    if (solnJavaList.ne(null)) {
      if (solnJavaList.size() == 1) {
        return Some(solnJavaList.get(0))
      }
    }
    None;
  }
  def parseQueryText(queryText: String): Query = {
    val dirModel = getDirectoryModel;
    var ds = getMainQueryDataset
    JenaArqQueryFuncs.parseQueryText(queryText, dirModel);
  }
  override def makeInitialBinding(): InitialBinding = {
    new InitialBindingImpl(getFallbackModelClient)
  }
  /**
   * These QName parameters are resolved against the repo's default namespace prefixes.
   */
  def resolveIndirectQueryText(querySourceGraphQName: String, queryParentQName: String): String = {
    // val mainDset : DataSource = getMainQueryDataset().asInstanceOf[DataSource];
    // val dirModel = getDirectoryModel;
    // val nsJavaMap : java.util.Map[String, String] = dirModel.getNsPrefixMap()

    val ib = makeInitialBinding
    ib.bindQName(QUERY_QUERY_GRAPH_INPUT_VAR, querySourceGraphQName)
    ib.bindQName(QUERY_QUERY_URI_RESULT_VAR, queryParentQName)
    val qText = resolveIndirectQueryText(ib)
    checkQueryText(qText, querySourceGraphQName, queryParentQName, false)
    qText
  }
  /**
   * Alternate form using full URIs, from any Ident.
   */
  def resolveIndirectQueryText(querySourceGraphID: Ident, queryID: Ident): String = {
    val ib = makeInitialBinding
    ib.bindIdent(QUERY_QUERY_GRAPH_INPUT_VAR, querySourceGraphID)
    ib.bindIdent(QUERY_QUERY_URI_RESULT_VAR, queryID)
    val qText = resolveIndirectQueryText(ib)
    checkQueryText(qText, querySourceGraphID, queryID, true)
    qText
  }

  private def resolveIndirectQueryText(queryResIB: InitialBinding): String = {
    val qInitBinding: QuerySolutionMap = queryResIB.getQSMap

    val parsedQQ: Query = myQueryResQuery

    val possSoln: Option[QuerySolution] = findSingleQuerySolution(parsedQQ, qInitBinding);

    val qText: String = if (possSoln.isDefined) {
      val qSoln = possSoln.get;
      val qtxt_Lit: Literal = qSoln.getLiteral("queryTxt");
      qtxt_Lit.getString()
    } else "";

    qText
  }
  override def queryIndirectForAllSolutions(qSrcGraphIdent: Ident, queryIdent: Ident, qInitBinding: QuerySolution): java.util.List[QuerySolution] = {
    val qText = resolveIndirectQueryText(qSrcGraphIdent, queryIdent)
    checkQueryText(qText, qSrcGraphIdent, queryIdent, true)
    queryDirectForAllSolutions(qText, qInitBinding)
  }

  override def queryIndirectForAllSolutions(qSrcGraphQN: String, queryQN: String, qInitBinding: QuerySolution): java.util.List[QuerySolution] = {

    val qText = resolveIndirectQueryText(qSrcGraphQN, queryQN)
    checkQueryText(qText, qSrcGraphQN, queryQN, true)
    queryDirectForAllSolutions(qText, qInitBinding)
  }
  def checkQueryText(qText: String, qSrcGraphQN: Object, queryQN: Object, showStackTrace: Boolean): Unit = {
    if (qText == null || qText.length == 0) {
      val msg = "Unable to find Query Called " + queryQN + " in Model " + qSrcGraphQN;
      val rte = new RuntimeException(msg);
      logError(msg);
      if (showStackTrace) {
        rte.printStackTrace
        // maybe we should just throw now?
        // rather then letting subsequent calls fail with obscure EOFs
      }
    }
  }
  override def queryDirectForAllSolutions(qText: String, qInitBinding: QuerySolution): java.util.List[QuerySolution] = {
    import scala.collection.immutable.StringOps
    val qTextOps = new StringOps(qText);
    val fixedQTxt = qTextOps.replaceAll("!!", "?") // Remove this as soon as app code is updated
    val parsedQ = parseQueryText(fixedQTxt);

    findAllSolutions(parsedQ, qInitBinding);
  }
  private def parseQueryResolutionQuery: Query = {
    val parsedQQ = parseQueryText(QUERY_QUERY_TEXT);
    logDebug("Parsed QueryResolutionQuery as: " + parsedQQ)
    parsedQQ
  }

}

// class DirectRepo(val myDirectoryModel : Model) extends FancyRepo {

/*abstract class DirectRepo(directoryModel: Model) extends BasicRepoImpl with FancyRepo with Repo.Updatable {
  def loadSheetModelsIntoMainDataset(): Unit = {}
  def loadDerivedModelsIntoMainDataset(fileModelCLs: java.util.List[ClassLoader]): Unit = {}
  def loadFileModelsIntoMainDataset(fileModelCLs: java.util.List[ClassLoader]): Unit = {}
  val myDirectoryModel: Model = directoryModel;
  override def getDirectoryModel: Model = myDirectoryModel;

}*/

// for appdapter 1.1.1 compatiblity
/// if kept, maybe make this a big make every kind of repo one stop shop
//object FancyRepoFactory extends BasicDebugger {
 // def makeDatabaseRepo(repoConfResPath: String, optCL: ClassLoader, dirGraphID: Ident): DatabaseRepo_BROKER = {
  //  DatabaseRepoFactoryLoader.makeDatabaseRepo(repoConfResPath, optCL, dirGraphID)
  //}
//}
