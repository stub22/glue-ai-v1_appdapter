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

import java.util.HashMap
import org.appdapter.core.boot.ClassLoaderUtils
import org.appdapter.core.store.{ Repo }
import org.appdapter.demo.DemoBrowserUI
import org.appdapter.fancy.rclient.{RepoClientImpl, LocalRepoClientImpl}
import org.appdapter.fancy.loader.{FancyRepoLoader, InstallableRepoLoader}
import org.appdapter.fancy.repo.{ FancyRepo }
import org.osgi.framework.BundleContext
import com.hp.hpl.jena.rdf.model.Model
import org.appdapter.core.log.BasicDebugger


object URLRepoSpec {
  val loaderMap = new HashMap[String, InstallableRepoLoader]();
}
/**
 * Takes a directory model and uses Goog, Xlsx, Pipeline,CSV,.ttl,rdf sources and loads them
 */
class URLRepoSpec(var dirModelURL: String, var fileModelCLs: java.util.List[ClassLoader] = null)
  extends RepoSpec {

  def trimString(str: String, outers: String*): String = {
    var tmp = str;
    for (s0 <- outers) {
      var s: String = s0
      while (tmp.startsWith(s)) {
        tmp = tmp.substring(s.length);
      }
      while (tmp.endsWith(s)) {
        tmp = tmp.substring(0, tmp.length - s.length);
      }
    }
    tmp
  }
  def detectedRepoSpec: RepoSpec = {
    import scala.collection.JavaConversions._
    var orig = dirModelURL.trim();
    var multis: Array[String] = orig.split(",");
    if (multis.length > 1) {
      if (!orig.startsWith("mult:")) {
        orig = "mult://[" + orig + "]"
      }
    }
    var dirModelURLParse = orig.replace("//", "/").trim();
    val colon = dirModelURLParse.indexOf(":/");
    val proto = trimString(dirModelURLParse.substring(0, colon + 1), "/", ":", " ")
    val path = trimString(dirModelURLParse.substring(colon + 1), "/", " ")
    val v3: Array[String] = (path + "//").split('/')
    if (proto.equals("goog")) {
      (new GoogSheetRepoSpec(v3(0), v3(1).toInt, v3(2).toInt, fileModelCLs))
    } else if (proto.equals("xlsx")) {
      (new OfflineXlsSheetRepoSpec(v3(0), v3(1), v3(3), fileModelCLs))
    } else if (proto.equals("scan")) {
      (new ScanURLDirModelRepoSpec(v3(0), fileModelCLs))
    } else if (proto.equals("mult")) {
      (new MultiRepoSpec(path, fileModelCLs))
    } else {
      val dirModelLoaders: java.util.List[InstallableSpecReader] = FancyRepoLoader.getSpecLoaders
      val dirModelLoaderIter = dirModelLoaders.listIterator
      while (dirModelLoaderIter.hasNext()) {
        val irr = dirModelLoaderIter.next
        try {
          val ext = irr.getExt;
          if (ext != null && ext.equalsIgnoreCase(proto)) {
            val spec = Some(irr.makeRepoSpec(path, v3, fileModelCLs))
            if (!spec.isEmpty) return spec.get;
          }
        } catch {
          case except: Throwable =>
            except.printStackTrace
          //getLogger.error("Caught loading error in {}", Array[Object](irr, except))
        }
      }
      new URLDirModelRepoSpec(dirModelURL, fileModelCLs)
    }
  }
  override def getDirectoryModel(): Model = detectedRepoSpec.getDirectoryModel
  override def makeRepo(): Repo.WithDirectory = detectedRepoSpec.makeRepo
  override def toString = dirModelURL
}
