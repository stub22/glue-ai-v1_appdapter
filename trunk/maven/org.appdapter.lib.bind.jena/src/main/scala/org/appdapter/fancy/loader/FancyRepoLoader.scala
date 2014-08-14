/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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

import java.util.{ArrayList, Date}

import org.appdapter.bind.rdf.jena.model.JenaFileManagerUtils
import org.appdapter.core.log.BasicDebugger
import org.appdapter.fancy.rspec.{RepoSpec, RepoSpecReader, RSpecReader_UrlDir_Dir, RSpecReader_UrlDir_Turtle,
			RSpecReader_FolderScan_Dir, RSpecReader_FolderScan_Turtle}
import org.appdapter.core.name.Ident
import org.appdapter.core.store.{Repo, RepoOper}
import org.appdapter.core.query.{InitialBinding}
import org.appdapter.core.loader.{SpecialRepoLoader, ExtendedFileStreamUtils}
import org.appdapter.fancy.repo.{DirectRepo, FancyRepo}
import com.hp.hpl.jena.query.{Dataset, QuerySolution}
import com.hp.hpl.jena.rdf.model.{Model, Resource}
/*
 * @author logicmoo
 */



object FancyRepoLoader extends BasicDebugger {

  def addInvisbleInfo(in: String, k: String, v: String): String = {
    in + "/*" + k + "=" + v + "*/"
  }

  var initedOnce = false;
  val myRSpecReaders: java.util.List[RepoSpecReader] = new java.util.ArrayList
  val dirModelLoaders: java.util.List[InstallableRepoLoader] = new java.util.ArrayList
  def getDirModelLoaders(): java.util.List[InstallableRepoLoader] = {
    dirModelLoaders.synchronized {
      if (dirModelLoaders.size() < 4) {
        dirModelLoaders.add(new GoogSheetRepoLoader())
        dirModelLoaders.add(new XLSXSheetRepoLoader())
        dirModelLoaders.add(new CsvFileSheetLoader())
        dirModelLoaders.add(new SdbSqlRepoLoader())
        dirModelLoaders.add(new FileModelRepoLoader())
        dirModelLoaders.add(new MultiRepoLoader())

        //this should be made to work so far Doug hadnt done it
        dirModelLoaders.add(new DerivedModelLoader())
        // the next is loaded loadDerivedModelsIntoMainDataset (which are pipeline models)
        //dirModelLoaders.add(new PipelineModelLoader())
        dirModelLoaders.add(new PipelineSnapLoader())
        //dirModelLoaders.add(new LastModelLoader())

      }
      return new ArrayList[InstallableRepoLoader](dirModelLoaders)
    }
  }
  def getRSpecReaders(): java.util.List[RepoSpecReader] = {
    myRSpecReaders.synchronized {
      if (myRSpecReaders.size < 3) {
        myRSpecReaders.add(new RSpecReader_FolderScan_Dir())
        myRSpecReaders.add(new RSpecReader_UrlDir_Dir())
        myRSpecReaders.add(new RSpecReader_UrlDir_Turtle())
        myRSpecReaders.add(new RSpecReader_FolderScan_Turtle())
        for (l <- getDirModelLoaders.toArray(new Array[InstallableRepoLoader](0))) {
          myRSpecReaders.add(l);
        }
      }
      return new ArrayList[RepoSpecReader](myRSpecReaders)
    }
  }

  def updateDatasetFromDirModel(repoLoader: SpecialRepoLoader, mainDset: Dataset, dirModel: Model, 
								fileModelCLs: java.util.List[ClassLoader], optUrlPrefix : String) {
    val dirModelLoaders: java.util.List[InstallableRepoLoader] = getDirModelLoaders
    val dirModelLoaderIter = dirModelLoaders.listIterator
    repoLoader.setSynchronous(false)
    while (dirModelLoaderIter.hasNext()) {
      val irr = dirModelLoaderIter.next
      getLogger.trace("Loading ... {}/{}",  Seq(irr.getContainerType, irr) :_*)
      try {
        if (irr.isDerivedLoader) {
          // this means what we are doing might need previous requests to complete
          repoLoader.setSynchronous(true)
        } else {
          //repoLoader.setSynchronous(false)
          repoLoader.setSynchronous(true)
        }
        irr.loadModelsIntoTargetDataset(repoLoader, mainDset, dirModel, fileModelCLs, optUrlPrefix)
      } catch {
        case except: Throwable =>
          except.printStackTrace
          getLogger.error("Caught loading error in {}", Array[Object](irr, except))
      }
    }
    // not done until the last task completes
    repoLoader.setSynchronous(true)
  }
  def makeRepoWithDirectory(spec: RepoSpec, dirModel: Model, fileModelCLs: java.util.List[ClassLoader] = null, dirGraphID: Ident = null): DirectRepo = {
   
    var serial = System.identityHashCode(this);
    var repoDebugName = addInvisbleInfo(spec.toString, "time", "" + new Date());
    if (dirGraphID != null) {
      repoDebugName = addInvisbleInfo(repoDebugName, "id", "" + dirGraphID);
    }
    // Construct a repo around that directory        
	val repoBasePath = spec.getBasePath();
    val shRepo = new DirectRepo(spec, repoDebugName, repoBasePath, dirModel, fileModelCLs)
    //shRepo.beginLoading();
    // set to false to have concurrent background loading
    if (true) {
      //shRepo.finishLoading();
    }
	getLogger().info("Finished making RepoWithDirectory with debugName {}", repoDebugName)
    shRepo
  }

  def updateDatasetFromDirModel(dirModel: Model, mainDset: Dataset, fileModelCLs: java.util.List[ClassLoader], optUrlPrefix : String, repoLoader: SpecialRepoLoader) {
    repoLoader.setSynchronous(false)
    FancyRepoLoader.updateDatasetFromDirModel(repoLoader, mainDset, dirModel, fileModelCLs, optUrlPrefix)
    // not done until the last task completes
    repoLoader.setSynchronous(true)
  }

  //def loadDatabaseRepo(configPath: String, optConfigResolveCL: ClassLoader, dirGraphID: Ident): FancyRepo = {
  // val dbRepo = DatabaseRepoFactoryLoader.makeDatabaseRepo(configPath, optConfigResolveCL, dirGraphID)
  //// dbRepo;
  //}

  def testRepoDirect(repo: Repo.WithDirectory, querySheetQName: String, queryQName: String, tgtGraphSparqlVN: String, tgtGraphQName: String): Unit = {
    // Here we manually set up a binding, as you would usually allow RepoClient
    // to do for you, instead:
    val qib: InitialBinding = repo.makeInitialBinding
    qib.bindQName(tgtGraphSparqlVN, tgtGraphQName)

    // Run the resulting fully bound query, and print the results.		
    val solnJavaList: java.util.List[QuerySolution] = repo.queryIndirectForAllSolutions(querySheetQName, queryQName, qib.getQSMap);

    println("Found solutions for " + queryQName + " in " + tgtGraphQName + " : " + solnJavaList)
  }

  /*def copyAllRepoModels(sourceRepo: Repo.WithDirectory, targetRepo: Repo.WithDirectory): Unit = {
    RepoOper.copyAllRepoModels(sourceRepo,targetRepo)    
  }*/

  def replaceOrUnion(mainDset: Dataset, unionOrReplaceRes: Resource, graphURI: String, sheetModel: Model) {
    RepoOper.putNamedModel(mainDset, graphURI, sheetModel, unionOrReplaceRes)
  }

  // Makes directory models (ussualy still unloaded)
  def readDirectoryModelFromURL(rdfURL: String, nsJavaMap: java.util.Map[String, String], fileModelCLs: java.util.List[ClassLoader]): Model = {
    try {
      getLogger.info("readDirectoryModelFromURL - start {}", rdfURL)
      val ext: java.lang.String = org.appdapter.fileconv.FileStreamUtils.getFileExt(rdfURL);
      if (ext != null && (ext.equals("xlsx") || ext.equals("xls"))) {
        XLSXSheetRepoLoader.readDirectoryModelFromXLSX(rdfURL, "Nspc", "Dir", fileModelCLs);
      } else if (ext != null && (ext.equals("csv"))) {
        CsvFileSheetLoader.readModelSheet(rdfURL, nsJavaMap, fileModelCLs);
      } else readRdfGraphFromURL(rdfURL, nsJavaMap, fileModelCLs);
    } catch {
      case except: Throwable => {
        getLogger.error("Caught error loading file {}", Array[Object](rdfURL, except))
        throw except
      }
    }
  }

  // Makes single Models from xlsx/cvs/jenaURLs
  // Caled from:
  // FileModelRepoLoader - task.run()
  // CsvFileSheetLoader - task.run()
  def readRdfGraphFromURL(rdfURL: String, nsJavaMap: java.util.Map[String, String], 
						clList: java.util.List[ClassLoader]): Model = {
    try {
		getLogger.info("Reading RDF graph from URL {} into a jena model.", rdfURL);
      val efsu = new ExtendedFileStreamUtils()
      val ext: java.lang.String = org.appdapter.fileconv.FileStreamUtils.getFileExt(rdfURL);
      if (ext != null && (ext.equals("xlsx") || ext.equals("xls"))) {
        XLSXSheetRepoLoader.loadXLSXSheetRepo(rdfURL, "Nspc", "Dir", clList).getMainQueryDataset().asInstanceOf[Dataset].getDefaultModel
      } else if (ext != null && (ext.equals("csv"))) {
        CsvFileSheetLoader.readModelSheet(rdfURL, nsJavaMap, clList);
      } else {
        import com.hp.hpl.jena.util.FileManager;
        val jenaFileMgr = JenaFileManagerUtils.getDefaultJenaFM
        JenaFileManagerUtils.ensureClassLoadersRegisteredWithJenaFM(jenaFileMgr, clList)
        jenaFileMgr.loadModel(rdfURL);
      }
    } catch {
      case except: Throwable => {
        getLogger.error("Error loading from URL=[" + rdfURL + "]", except);
        throw except
      }
    }
  }

  ///. Modeled on GoogSheetRepo.loadTestSheetRepo
  def loadDetectedFileSheetRepo(rdfURL: String, nsJavaMap: java.util.Map[String, String], 
								fileModelCLs: java.util.List[ClassLoader], repoSpec: RepoSpec): FancyRepo = {
    // Read the namespaces and directory sheets into a single directory model.
    val dirModel: Model = readDirectoryModelFromURL(rdfURL, nsJavaMap, fileModelCLs)
    // Construct a repo around that directory
    val shRepo = makeRepoWithDirectory(repoSpec, dirModel, fileModelCLs)
    // Load the rest of the repo's initial *sheet* models, as instructed by the directory.
    //shRepo.loadSheetModelsIntoMainDataset()
    // Load the rest of the repo's initial *file/resource* models, as instructed by the directory.
    // shRepo.loadDerivedModelsIntoMainDataset(fileModelCLs)
    shRepo
  }

}

/*
winston@topia ~/e_mount
$  grep -rin    --include=*.{java,scala,xml,ttl,owl,html} --exclude="*\.svn*" --exclude-dir=target readRdfGraphFromURL
appdapter_trunk/maven/ cogchar_trunk/maven/ friendularity_trunk/maven/ hrk_tools_trunk/maven/

appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/loader/CsvFileSheetLoader.scala:86:
            val fileModel = FancyRepoLoader.readRdfGraphFromURL(rdfURL, nsJavaMap, clList);
appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/loader/FancyRepoLoader.scala:166:
    } else readRdfGraphFromURL(rdfURL, nsJavaMap, fileModelCLs);
appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/loader/FancyRepoLoader.scala:179:
def readRdfGraphFromURL(rdfURL: String, nsJavaMap: java.util.Map[String, String], clList: java.util.List[ClassLoader]):
Model = {
appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/loader/FileModelRepoLoader.scala:80
:            val fileModel = FancyRepoLoader.readRdfGraphFromURL(rdfURL, nsJavaMap, clList);

winston@topia ~/e_mount
$  grep -rin   --include=*.{java,scala,xml,ttl,owl,html} --exclude="*\.svn*" --exclude-dir=target readDirectoryModelFro
mURL appdapter_trunk/maven/ cogchar_trunk/maven/ friendularity_trunk/maven/ hrk_tools_trunk/maven/

 appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/loader/FancyRepoLoader.scala:158:
def readDirectoryModelFromURL(rdfURL: String, nsJavaMap: java.util.Map[String, String], fileModelCLs: java.util.List[Cla
ssLoader]): Model = {

 appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/loader/FancyRepoLoader.scala:160:
    getLogger.info("readDirectoryModelFromURL - start {}", rdfURL)

 appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/loader/FancyRepoLoader.scala:205:
  val dirModel: Model = readDirectoryModelFromURL(rdfURL, nsJavaMap, fileModelCLs)

 appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/rspec/UrlDirModelRepoSpec.scala:75:
  override def getDirectoryModel = FancyRepoLoader.readDirectoryModelFromURL(dirModelURL, null, fileModelCLs)

 friendularity_trunk/maven/org.friendularity.lib.viz/src/main/scala/org/friendularity/ignore/nexjen/BehavTrix.scala:530:a
t org.appdapter.core.matdat.FancyRepoLoader$.readDirectoryModelFromURL(RepoLoader.scala:109)

winston@topia ~/e_mount
$  grep -rin    --include=*.{java,scala,xml,ttl,owl,html} --exclude="*\.svn*" --exclude-dir=target loadDetectedFileShee
tRepo appdapter_trunk/maven/ cogchar_trunk/maven/ friendularity_trunk/maven/ hrk_tools_trunk/maven/

appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/loader/FancyRepoLoader.scala:203:
def loadDetectedFileSheetRepo(rdfURL: String, nsJavaMap: java.util.Map[String, String], fileModelCLs: java.util.List[Cla
ssLoader], repoSpec: RepoSpec): FancyRepo = {
appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/loader/MultiRepoLoader.scala:119:
          //val MultiModel = FancyRepoLoader.loadDetectedFileSheetRepo(configPath, null, modelIdent).getNamedModel(model
Ident);
appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/rspec/UrlDirModelRepoSpec.scala:74:
  //override def makeRepo = FancyRepoLoader.loadDetectedFileSheetRepo(dirModelURL, null, fileModelCLs, this)

 */