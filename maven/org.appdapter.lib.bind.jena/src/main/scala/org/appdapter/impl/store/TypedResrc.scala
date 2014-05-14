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

/**
 * @author Stu B. <www.texpedient.com>
 */
import com.hp.hpl.jena.rdf.model.Resource;
import org.appdapter.core.item.{Item, JenaResourceItem}
import org.appdapter.core.log.{BasicDebugger};
import org.appdapter.core.name.{Ident, FreeIdent};
import org.appdapter.core.store.{Repo, ModelClient }


trait TypedResrc extends Ident with Item {
	def hasTypeMark(typeID : Ident) : Boolean = false
	def getTypeIdents: Set[Ident] = Set()
}
trait ExtensiblyTypedResrc extends TypedResrc {
	// Consider:  This would be difficult for a virtually-backed TypedResrc to implement.
	def addTypeMarkings(moreTypeMarks : Set[Ident]) : TypedResrc
}
class JenaTR(r : Resource, private val myTypes : Set[Ident]) extends JenaResourceItem(r) with ExtensiblyTypedResrc {
	override def hasTypeMark(typeID : Ident) : Boolean = myTypes.contains(typeID)
	override def getTypeIdents: Set[Ident] = myTypes
	def addTypeMarkings(moreTypeMarks : Set[Ident]) : TypedResrc = {
		if (moreTypeMarks.subsetOf(myTypes)) {
			this
		} else {
			val unionOfTypes = myTypes.union(moreTypeMarks)
			val jres = getJenaResource()
			new JenaTR(jres, unionOfTypes)			
		}
	}
}

object TypedResrcFactory extends BasicDebugger {
	// Produce a TypedResource equal to anyID whose types are the union of:
	//	1) any types already associated with anyID (because anyID is already a TR)
	//	2) knownTypeIDs.
	//	3) Other type markings for anyID discoverable through modelCli.
	//		(But note that modelCli does not currently support access to the underlying model,
	//		so the answer is effectively "none", for the present.  TODO: Expand on this use case).
	//		
	// If anyID does not already contain a resource, use modelCli to produce the resource.

	def exposeTypedResrc(anyID : Ident, knownTypeIDs : Set[Ident], modelCli : ModelClient) : TypedResrc = {
		anyID match {
			case extensiblyTypedAlready : ExtensiblyTypedResrc => extensiblyTypedAlready.addTypeMarkings(knownTypeIDs)
			case otherTypedAlready : TypedResrc => { 
				throw new RuntimeException("Trying to add types " + knownTypeIDs + " to a non-extensible TypedResource " + otherTypedAlready);
			}
			case otherJRI : JenaResourceItem => new JenaTR(otherJRI.getJenaResource(), knownTypeIDs)
			case otherID : Ident => {
				val jres = modelCli.makeResourceForIdent(otherID)
				new JenaTR(jres, knownTypeIDs)
			}
			case _ => throw new RuntimeException ("Confused by anyID " + anyID);
		}
	}

}