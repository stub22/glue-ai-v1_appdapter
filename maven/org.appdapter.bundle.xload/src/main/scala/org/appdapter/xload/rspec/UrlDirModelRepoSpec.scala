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

import java.io.File
import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.loader.ExtendedFileLoading.Paths
import org.appdapter.core.loader.SpecialRepoLoader
import org.appdapter.xload.fancy.{FancyRepoLoader}
// import org.appdapter.fancy.rspec.{InstallableSpecReader}
import org.appdapter.fancy.query.{QueryHelper}
import com.hp.hpl.jena.query.{ Dataset, QuerySolution }
import com.hp.hpl.jena.rdf.model.{ Literal, Model, Resource }


/**
 * @author Douglas R. Miles <www.logicmoo.org>
 * @author Stu B. <www.texpedient.com>
 *
 */



class URLDirModelRepoSpec(dirModelURL: String, fileModelCLs: java.util.List[ClassLoader]) extends RepoSpecForDirectory {
  //override def makeRepo = FancyRepoLoader.loadDetectedFileSheetRepo(dirModelURL, null, fileModelCLs, this)
  override protected def makeDirectoryModel = FancyRepoLoader.readDirectoryModelFromURL(dirModelURL, null, fileModelCLs)
  override def toString = dirModelURL
  
	override def getBasePath : String = guessBasePath(dirModelURL)

}


/*
 * 	 
appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/loader/FileModelRepoLoader.scala:31
:  override def makeRepoSpec(path: String, args: Array[String], cLs: java.util.List[ClassLoader]) = new URLDirModelRepoS
pec(path, cLs)

appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/rspec/UrlDirModelRepoSpec.scala:59:
      all.add(new URLDirModelRepoSpec(f.getPath(), fileModelCLs));
	  
appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/rspec/UrlRepoSpec.scala:72:      (n
ew ScanURLDirModelRepoSpec(v3(0), fileModelCLs))

appdapter_trunk/maven/org.appdapter.lib.bind.jena/src/main/scala/org/appdapter/fancy/rspec/UrlRepoSpec.scala:92:      ne
w URLDirModelRepoSpec(dirModelURL, fileModelCLs)
 */