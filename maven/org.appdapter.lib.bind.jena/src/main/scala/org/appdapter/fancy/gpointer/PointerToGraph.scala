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

package org.appdapter.fancy.gpointer
import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils
import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.name.Ident
import org.appdapter.core.store.{ Repo }
import org.appdapter.core.model.{ RdfNodeTranslator }
import com.hp.hpl.jena.rdf.model.Model
import org.appdapter.fancy.rclient.RepoClient
/**
 * @author Stu B. <www.texpedient.com>
 */

/*
class PointerToGraph {

}
*/
trait PointerToTypedGraph {
  // This name + set of types tells us "everything important" about the model provided, including where it comes from 
  // and what kind of stuff it contains, to the extent known at time of (re-?)binding.
  def getTypedName(): TypedResrc
  def getModel(): Model
  import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
  def assembleModelRoots(): java.util.Set[Object] = AssemblerUtils.buildAllRootsInModel(getModel())
}
/* "Direct" (as opposed to filtered) from sum upstream provider */
case class BasicPointerToTypedGraph(val myUpstreamGraphID: TypedResrc, val myUpstreamNMP: NamedModelProvider)
			extends PointerToTypedGraph {
	override def getModel() = myUpstreamNMP.getNamedModelReadonly(myUpstreamGraphID);
	override def getTypedName() = myUpstreamGraphID
}
object TypedGraphPointerFactory extends BasicDebugger {
  def makeOnePointerToTypedGraph(rc: RepoClient, graphID: Ident): PointerToTypedGraph = {
    val upstreamNMP = new ClientModelProvider(rc)
    upstreamNMP.makePointerToTypedGraph(graphID);
  }
  def makeOneDerivedModelPointer(rc: RepoClient, pqs: PipelineQuerySpec, outGraphID: Ident): PointerToTypedGraph = {
    try {
      val dgSpec = DerivedGraphSpecReader.findOneDerivedGraphSpec(rc, pqs, outGraphID)
      // Assume we want to read from same repo-client as was used to fetch the spec.
      dgSpec.makeDerivedModelProvider(rc)
    } catch {
      case except: Throwable => {
		// The only SLF4J logger methods that explicitly know about *Throwable* do not accept additional args for 
		// formatting.
        getLogger.error("Caught error makeOneDerivedModelProvider, pqs=" + pqs, except)
        throw except
      }
    }
  }

}