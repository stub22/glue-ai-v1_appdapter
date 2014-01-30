/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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

import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory

import org.appdapter.core.log.{ BasicDebugger };
import org.appdapter.core.name.{ Ident, FreeIdent };
import org.appdapter.core.store.{ Repo, InitialBinding, ModelClient }
import org.appdapter.help.repo.{ RepoClient, RepoClientImpl, InitialBindingImpl, SolutionList }

/**
 * @author Stu B. <www.texpedient.com>
 */

trait NamedModelProvider {
  def getNamedModel(graphNameID: Ident): Model
  def makeDirectBoundModelProvider(graphID: Ident): BoundModelProvider
  protected def makeDirectBoundModelProvider(graphID: Ident, dirModelClient: ModelClient): BoundModelProvider = {
    //  It's also not strictly true that we need the "Directory" model client, specifically,
    // to perform this exposeTypedResrc operation.  Actual requirement is an algebraic expression to be worked out.		
    val typedGraphID: TypedResrc = TypedResrcFactory.exposeTypedResrc(graphID, Set(), dirModelClient)
    new DirectRepoGraph(typedGraphID, this)
  }
}

class ServerModelProvider(mySrcRepo: Repo.WithDirectory) extends NamedModelProvider {
  override def getNamedModel(graphID: Ident): Model = mySrcRepo.getNamedModel(graphID)
  override def makeDirectBoundModelProvider(graphID: Ident): BoundModelProvider = {
    val dirModelClient = mySrcRepo.getDirectoryModelClient
    makeDirectBoundModelProvider(graphID, dirModelClient)
  }
}
class ClientModelProvider(myRepoClient: RepoClient) extends NamedModelProvider {
  override def getNamedModel(graphID: Ident): Model = myRepoClient.getRepo.getNamedModel(graphID)
  override def makeDirectBoundModelProvider(graphID: Ident): BoundModelProvider = {
    // TODO:  This line of code is an example of where RepoClient is not yet general enough in the
    // services it can provide to a true client which doesn't have the "server" repo so handy as 
    // we assumer here! 
    val dirModelClient = myRepoClient.getRepo.getDirectoryModelClient
    makeDirectBoundModelProvider(graphID, dirModelClient)
  }
}

trait BoundModelProvider {
  // This name + set of types tells us "everything important" about the model provided, including where it comes from 
  // and what kind of stuff it contains, to the extent known at time of (re-?)binding.
  def getTypedName(): TypedResrc
  def getModel(): Model
  import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
  def assembleModelRoots(): java.util.Set[Object] = AssemblerUtils.buildAllRootsInModel(getModel())
}

object ModelProviderFactory extends BasicDebugger {
  def makeOneDirectModelProvider(rc: RepoClient, graphID: Ident): BoundModelProvider = {
    val upstreamNMP = new ClientModelProvider(rc)
    upstreamNMP.makeDirectBoundModelProvider(graphID);
  }
  def makeOneDerivedModelProvider(rc: RepoClient, pqs: PipelineQuerySpec, outGraphID: Ident): BoundModelProvider = {
    try {
      val dgSpec = DerivedGraphSpecReader.findOneDerivedGraphSpec(rc, pqs, outGraphID)
      // Assume we want to read from same repo-client as was used to fetch the spec.
      dgSpec.makeDerivedModelProvider(rc)
    } catch {
      case except: Throwable => {
        getLogger().error("Caught error makeOneDerivedModelProvider {}", Array[Object](pqs, except))
        throw except
      }
    }
  }

}