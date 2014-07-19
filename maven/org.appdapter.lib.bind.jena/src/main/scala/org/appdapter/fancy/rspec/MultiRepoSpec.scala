package org.appdapter.fancy.rspec

import org.appdapter.core.name.FreeIdent
import org.appdapter.core.store.{ RepoOper }
import org.appdapter.core.store.dataset.{ RepoDatasetFactory }
import org.appdapter.core.loader.{  SpecialRepoLoader }
import org.appdapter.fancy.loader.{  MultiRepoLoader }
import org.appdapter.fancy.query.{QueryHelper}
import com.hp.hpl.jena.query.{ Dataset, QuerySolution }
import com.hp.hpl.jena.rdf.model.{ Model, Resource }
import org.appdapter.core.log.Debuggable
import org.appdapter.core.log.BasicDebugger

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
    getLogger.debug("makeing S = '" + s + "'")
    for (rs <- s.split(",")) {
      getLogger.debug("makeing RS = " + rs)
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

