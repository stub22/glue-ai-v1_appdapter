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
import java.io.File
import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.loader.ExtendedFileLoading.Paths
import org.appdapter.core.loader.SpecialRepoLoader

import com.hp.hpl.jena.query.{ Dataset, QuerySolution }
import com.hp.hpl.jena.rdf.model.{ Literal, Model, Resource }
import org.appdapter.fancy.query.QueryHelper
import org.appdapter.fancy.rspec.{URLDirModelRepoSpec, ScanURLDirModelRepoSpec, InstallableSpecReader}

class URLDirModelRepoSpecReader extends InstallableSpecReader {
  override def getExt = "dir"
  override def makeRepoSpec(path: String, args: Array[String], cLs: java.util.List[ClassLoader]) = new URLDirModelRepoSpec(path, cLs)
}

class ScanURLDirModelRepoSpecReader extends InstallableSpecReader {
  override def getExt = "scandir"
  override def makeRepoSpec(path: String, args: Array[String], cLs: java.util.List[ClassLoader]) = new ScanURLDirModelRepoSpec(path, cLs)
}
class URLModelRepoSpecReader extends InstallableSpecReader {
  override def getExt = "ttl"
  override def makeRepoSpec(path: String, args: Array[String], cLs: java.util.List[ClassLoader]) = new URLDirModelRepoSpec(path, cLs)
}

class ScanURLModelRepoSpecReader extends InstallableSpecReader {
  override def getExt = "scanttl"
  override def makeRepoSpec(path: String, args: Array[String], cLs: java.util.List[ClassLoader]) = new ScanURLDirModelRepoSpec(path, cLs)
}
