package org.appdapter.core.repo

import org.appdapter.core.name.FreeIdent
import org.appdapter.core.store.{RepoOper}
import org.appdapter.core.store.dataset.{RepoDatasetFactory, SpecialRepoLoader}
import org.appdapter.impl.store.QueryHelper

import com.hp.hpl.jena.query.{Dataset, QuerySolution}
import com.hp.hpl.jena.rdf.model.{Model, Resource}

/**
 * @author Stu B. <www.texpedient.com>
 * @author Douglas R. Miles <www.logicmoo.org>
 *
 * This is a MultiRepo (registerable) Loader it contains static methods for loading MultiRepos
 *   from any other repo with a dir Model
 */

/////////////////////////////////////////
/// this is a registerable loader
/////////////////////////////////////////

class MultiRepoSpec(var many: String, protected var fileModelCLs: java.util.List[ClassLoader] = null)
  extends RepoSpecForDirectory {

  var toStringName = many

  addRepoSpec(many)

  def createURIFromBase(specs: Array[Object]) = {
    var s = MultiRepoLoader.PROTO + ":/[";
    var neeedComa = false;
    for (r <- specs) {
      if (neeedComa)
        s += ","
      s += "" + r
      neeedComa = true
    }
    s += "]"
    s
  }

  lazy val repoSpecs = new java.util.HashSet[RepoSpec]

  def addRepoSpec(s0: String): Unit = {
    var s = s0
    if (s == null) throw new RuntimeException("Null RepoSpec definition")
    if (s.startsWith("[") && s.endsWith("]")) s = s.substring(1, s.length() - 1)
    println("makeing S = '" + s + "'")
    for (rs <- s.split(",")) {
      println("makeing RS = " + rs)
      addRepoSpec(new URLRepoSpec(rs, fileModelCLs))
    }
  }

  def addRepoSpec(urlRepoSpec: URLRepoSpec) = {
    repoSpecs.add(urlRepoSpec)
  }

  override def getDirectoryModel(): Model = {
    var dirModel: Model = RepoDatasetFactory.createPrivateMemModel();
    for (d <- repoSpecs.toArray(new Array[RepoSpec](0))) {
      println("repoSpec = " + d)
      var model = d.getDirectoryModel;
      dirModel = dirModel.union(model)
      dirModel.withDefaultMappings(model)
    }
    var map = dirModel.getNsPrefixMap
    dirModel
  }

  override def toString() =
    if (toStringName != null)
      toStringName else createURIFromBase(repoSpecs.toArray());
}

class MultiRepoLoader extends InstallableRepoReader {
  override def makeRepoSpec(path: String, args: Array[String], cLs: java.util.List[ClassLoader]): RepoSpec = {
    new MultiRepoSpec(path, cLs);
  }
  override def getExt = MultiRepoLoader.PROTO
  override def getContainerType() = "ccrt:MultiRepo"
  override def getSheetType() = "ccrt:DirectoryModelSheet"
  override def loadModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset, dirModel: Model, fileModelCLs: java.util.List[ClassLoader]) {
    MultiRepoLoader.loadSheetModelsIntoTargetDataset(repo, mainDset, dirModel, fileModelCLs)
  }
}
/*

class MultiRepo(store: Store, val myDirGraphID: Ident)
  extends BasicStoredMutableRepoImpl(store) with Repo.Mutable with Repo.Stored {

  formatRepoIfNeeded();

  override def getDirectoryModel: Model = getNamedModel(myDirGraphID);

}
*/

object MultiRepoLoader extends org.appdapter.core.log.BasicDebugger {

  val PROTO = "multi"
  /*def makeMultiRepo(repoConfResPath: String, optCL: ClassLoader, dirGraphID: Ident): MultiRepo = {
    val s: Store = SdbStoreFactory.connectSdbStoreFromResPath(repoConfResPath, optCL);
    new MultiRepo(s, dirGraphID);
  }*/

  /////////////////////////////////////////
  /// Make a Repo.WithDirectory
  /////////////////////////////////////////
  /* def makeSdbDirectoryRepo(repoConfResPath: String, optCL: ClassLoader, dirGraphID: Ident): Repo.WithDirectory = {
    // Read the namespaces and directory sheets into a single directory model.
    FancyRepoLoader.makeRepoWithDirectory(null, readDirectoryModelFromMulti(repoConfResPath, optCL, dirGraphID));
  }
*/
  /////////////////////////////////////////
  /// Read dir model
  /////////////////////////////////////////
  /*  def readDirectoryModelFromMulti(repoConfResPath: String, optCL: ClassLoader, dirGraphID: Ident): Model = {
    // Read the single directory sheets into a single directory model.
    val s: Store = SdbStoreFactory.connectSdbStoreFromResPath(repoConfResPath, optCL);
    new MultiRepo(s, dirGraphID).getDirectoryModel
  }
*/
  /////////////////////////////////////////
  /// Read sheet models
  /////////////////////////////////////////
  def loadSheetModelsIntoTargetDataset(repo: SpecialRepoLoader, mainDset: Dataset,
    myDirectoryModel: Model, clList: java.util.List[ClassLoader]): Unit = {

    if (myDirectoryModel.size == 0) return
    val nsJavaMap: java.util.Map[String, String] = myDirectoryModel.getNsPrefixMap()

    val msqText = """
			select ?dirModel ?modelPath ?unionOrReplace
				{
					?dirModel  a ccrt:DirectoryModel; ccrt:sourcePath ?modelPath.
      				OPTIONAL { ?dirModel a ?unionOrReplace. FILTER (?unionOrReplace = ccrt:UnionModel)}
    			}
		"""

    val msRset = QueryHelper.execModelQueryWithPrefixHelp(myDirectoryModel, msqText);
    import scala.collection.JavaConversions._;
    while (msRset.hasNext()) {
      val qSoln: QuerySolution = msRset.next();

      val dirModel = qSoln.getResource("dirModel");
      val modelPath = qSoln.getLiteral("modelPath");
      val unionOrReplace: Resource = qSoln.getResource("unionOrReplace");
      val dbgArray = Array[Object](dirModel, modelPath, unionOrReplace);
      getLogger.warn("dirModel={}, modelPath={}, model={}", dbgArray);

      val configPath = modelPath.getString();
      val modelURI = dirModel.getURI();

      getLogger().warn("Ready to read Multi from [{}] / [{}]", Array[Object](configPath, modelURI));

      val modelIdent = new FreeIdent(modelURI);
      repo.addLoadTask(configPath + "/" + modelURI, new Runnable() {
        def run() {
          try {
            val graphURI = modelURI
            val otherRepo = (new URLRepoSpec(configPath, clList)).makeRepo
            val src = otherRepo.getMainQueryDataset
            RepoOper.addOrReplaceDatasetElements(mainDset, src, unionOrReplace);
            //val MultiModel = FancyRepoLoader.loadDetectedFileSheetRepo(configPath, null, modelIdent).getNamedModel(modelIdent);
            // getLogger.warn("Read MultiModel: {}", MultiModel)
            //FancyRepoLoader.replaceOrUnion(mainDset, unionOrReplaceRes, graphURI, MultiModel);
          } catch {
            case except: Throwable => getLogger().error("Caught error loading Multi [{}] / [{}]", Array[Object](configPath, modelURI))
          }
        }
      })

    }
  }

}
