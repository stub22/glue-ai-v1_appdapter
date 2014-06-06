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

package org.appdapter.core.repo

import org.appdapter.bind.rdf.jena.sdb.SdbStoreFactory
import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.name.FreeIdent
import org.appdapter.core.name.Ident
import com.hp.hpl.jena.query.{ ResultSet, Dataset, QuerySolution, QuerySolutionMap }
import com.hp.hpl.jena.rdf.model.{ Model, Resource, Literal }
import com.hp.hpl.jena.sdb.{ Store, SDBFactory }
import com.hp.hpl.jena.shared.PrefixMapping
import com.hp.hpl.jena.sparql.modify.request.{ UpdateCreate, UpdateLoad }
import com.hp.hpl.jena.sparql.sse.SSE
import com.hp.hpl.jena.update.{ GraphStore, GraphStoreFactory, UpdateAction, UpdateRequest }
import org.appdapter.core.store.dataset.SpecialRepoLoader
import org.appdapter.core.store.BasicStoredMutableRepoImpl
import org.appdapter.core.store.Repo
import org.appdapter.impl.store.QueryHelper
import org.appdapter.impl.store.FancyRepo

/**
 * @author Stu B. <www.texpedient.com>
 * @author Douglas R. Miles <www.logicmoo.org>
 *
 * This is a DatabaseRepo (registerable) Loader it contains static methods for loading DatabaseRepos
 *   from any other repo with a dir Model
 */

/////////////////////////////////////////
/// this is a registerable loader
/////////////////////////////////////////

class DatabaseRepoSpec(configPath: String, optConfResCL: ClassLoader, dirGraphID: Ident) extends RepoSpec {
  def this(cPath: String, optCL: ClassLoader, dirGraphUriPrefix: String, dirGraphLocalName: String) = this(cPath, optCL, new FreeIdent(dirGraphUriPrefix + dirGraphLocalName, dirGraphLocalName))
  override def makeRepo() = DatabaseRepoFactoryLoader.makeDatabaseRepo(configPath, optConfResCL, dirGraphID)
}

class DatabaseRepoLoader extends InstallableRepoReader {
  override def getExt = null
  override def getContainerType() = "ccrt:DatabaseRepo"
  override def getSheetType() = "ccrt:DatabaseSheet"
  override def loadModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset, dirModel: Model, fileModelCLs: java.util.List[ClassLoader]) {
    DatabaseRepoFactoryLoader.loadSheetModelsIntoTargetDataset(repo, mainDset, dirModel, fileModelCLs)
  }
}

class DatabaseRepo_BROKER(store: Store, val myDirGraphID: Ident)
  extends BasicStoredMutableRepoImpl(store) with FancyRepo with Repo.Mutable with Repo.Stored {

  formatRepoIfNeeded();

  override def getDirectoryModel: Model = getNamedModel(myDirGraphID);

}

object DatabaseRepoFactoryLoader extends org.appdapter.core.log.BasicDebugger {
  def makeDatabaseRepo(repoConfResPath: String, optCL: ClassLoader, dirGraphID: Ident): Repo.WithDirectory = {
    val s: Store = SdbStoreFactory.connectSdbStoreFromResPath(repoConfResPath, optCL);
    new DatabaseRepo_BROKER(s, dirGraphID);
  }

  /////////////////////////////////////////
  /// Make a Repo.WithDirectory
  /////////////////////////////////////////
  //def makeSdbDirectoryRepo(repoConfResPath: String, optCL: ClassLoader, dirGraphID: Ident): Repo.WithDirectory = {
    // Read the namespaces and directory sheets into a single directory model.
    //FancyRepoLoader.makeRepoWithDirectory(null, readDirectoryModelFromDatabase(repoConfResPath, optCL, dirGraphID));
  //}

  /////////////////////////////////////////
  /// Read dir model
  /////////////////////////////////////////
  def readDirectoryModelFromDatabase(repoConfResPath: String, optCL: ClassLoader, dirGraphID: Ident): Model = {
    // Read the single directory sheets into a single directory model.
    val s: Store = SdbStoreFactory.connectSdbStoreFromResPath(repoConfResPath, optCL);
    new DatabaseRepo_BROKER(s, dirGraphID).getDirectoryModel
  }

  /////////////////////////////////////////
  /// Read sheet models
  /////////////////////////////////////////
  def loadSheetModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset,
    myDirectoryModel: Model, clList: java.util.List[ClassLoader]): Unit = {

    if (myDirectoryModel.size == 0) return
    val nsJavaMap: java.util.Map[String, String] = myDirectoryModel.getNsPrefixMap()

    val msqText = """
			select ?repo ?repoPath ?model ?modelPath ?unionOrReplace
				{
					?repo  a ccrt:DatabaseRepo; ccrt:configPath ?configPath.
					?model a ccrt:DatabaseModel; ccrt:repo ?repo.
      				OPTIONAL { ?model a ?unionOrReplace. FILTER (?unionOrReplace = ccrt:UnionModel) }
				}
		"""

    val msRset = QueryHelper.execModelQueryWithPrefixHelp(myDirectoryModel, msqText);
    import scala.collection.JavaConversions._;
    while (msRset.hasNext()) {
      val qSoln: QuerySolution = msRset.next();

      val repoRes: Resource = qSoln.getResource("repo");
      val modelRes: Resource = qSoln.getResource("model");
      val unionOrReplaceRes: Resource = qSoln.getResource("unionOrReplace");
      val configPath_Lit: Literal = qSoln.getLiteral("configPath")
      val dbgArray = Array[Object](repoRes, configPath_Lit, modelRes);
      getLogger.warn("repo={}, configPath={}, model={}", dbgArray);

      val configPath = configPath_Lit.getString();
      val modelURI = modelRes.getURI();

      getLogger().warn("Ready to read database from [{}] / [{}]", Array[Object](configPath, modelURI));

      val modelIdent = new FreeIdent(modelURI);
      repo.addLoadTask(configPath + "/" + modelURI, new Runnable() {
        def run() {
          try {
            val graphURI = modelRes.getURI();
            val databaseModel = makeDatabaseRepo(configPath, null, modelIdent).getNamedModel(modelIdent);
            getLogger.warn("Read databaseModel: {}", databaseModel)
            FancyRepoLoader.replaceOrUnion(mainDset, unionOrReplaceRes, graphURI, databaseModel);
          } catch {
            case except: Throwable => getLogger().error("Caught error loading database [{}] / [{}]", Array[Object](configPath, modelURI))
          }
        }
      })

    }
  }

}

/////////////////////////////////////////
/// These are tests below  
/////////////////////////////////////////

object DatabaseRepoLoaderTest {

  val configPath = "database connetion string/config path";
  val dirGraphID = "dirGraph Ident";

  private def loadTestSdbDirectoryRepo(): Repo.WithDirectory = {
    val spec = new DatabaseRepoSpec(configPath, null, new FreeIdent(dirGraphID))
    val sr = spec.makeRepo
    //  sr.loadSheetModelsIntoMainDataset()
    // sr.loadDerivedModelsIntoMainDataset(null)
    sr
  }

  import scala.collection.immutable.StringOps

  def main(args: Array[String]): Unit = {
    //BasicConfigurator.configure();
    //Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);

    // Find a query with this info
    val querySheetQName = "ccrt:qry_sheet_22";
    val queryQName = "ccrt:find_lights_99"

    // Plug a parameter in with this info
    val lightsGraphVarName = "qGraph"
    val lightsGraphQName = "ccrt:lights_camera_sheet_22"

    // Run the resulting fully bound query, and print the results.
    val sr = loadTestSdbDirectoryRepo

    val qib = sr.makeInitialBinding

    qib.bindQName(lightsGraphVarName, lightsGraphQName)

    val solnJavaList: java.util.List[QuerySolution] = sr.queryIndirectForAllSolutions(querySheetQName, queryQName, qib.getQSMap);

    println("Found solutions: " + solnJavaList)
  }

}

class BetterDatabaseRepo(sdbStore: Store, dirGraphID: Ident) extends DatabaseRepo_BROKER(sdbStore, dirGraphID) {
  //	Current docs for GraphStoreFactory (more recent than the code version we're using) say,
  //	regarding   GraphStoreFactory. reate(Dataset dataset)
  //	 Create a GraphStore from a dataset so that updates apply to the graphs in the dataset.
  //	 Throws UpdateException (an ARQException) if the GraphStore can not be created. This 
  //	 is not the way to get a GraphStore for SDB or TDB - an SDB Store object is a GraphStore 
  //	 no conversion necessary.
  //	 
  //	 
  def graphStoreStuff() = {
    val graphName = "http://example/namedGraph";

    // Create an empty GraphStore (has an empty default graph and no named graphs) 
    // val graphStore : GraphStore  = GraphStoreFactory.create() ;
    // 

    val readStore: Store = getStore();
    val sdbUpdateGraphStore: GraphStore = SDBFactory.connectGraphStore(readStore);

    // A sequence of operations
    val upSpec: UpdateRequest = new UpdateRequest();

    // Create a named graph
    val creReq: UpdateCreate = new UpdateCreate(graphName);

    // Load a file into a named graph - NB order of arguments (both strings).
    val loadReq: UpdateLoad = new UpdateLoad("etc/update-data.ttl", graphName);

    // Add the two operations and execute the request
    //@SuppressWarnings Deprecated
    upSpec.add(creReq)
    upSpec.add(loadReq)

    // Execute 
    UpdateAction.execute(upSpec, sdbUpdateGraphStore);

    // Print it out (format is SSE <http://jena.hpl.hp.com/wiki/SSE>)
    // used to represent a dataset.
    // Note the empty default, unnamed graph
    SSE.write(sdbUpdateGraphStore);
  }
}