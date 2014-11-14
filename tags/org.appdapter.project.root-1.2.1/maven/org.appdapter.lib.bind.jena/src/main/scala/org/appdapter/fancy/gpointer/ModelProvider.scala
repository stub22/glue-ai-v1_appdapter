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

package org.appdapter.fancy.gpointer

import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils
import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.name.Ident
import org.appdapter.core.store.{ Repo }
import org.appdapter.core.model.{ RdfNodeTranslator }
import org.appdapter.fancy.model.{ModelClientImpl}
import com.hp.hpl.jena.rdf.model.Model
import org.appdapter.fancy.rclient.RepoClient

/**
 * @author Stu B. <www.texpedient.com>
 */

trait NamedModelProvider {
	def getNamedModelReadonly(graphNameID: Ident): Model
	def makePointerToTypedGraph(graphID: Ident): PointerToTypedGraph = {
		val mciReadonly = getModelClientImplReadonly(graphID)
		makePointerToTypedGraphFromMC(graphID, mciReadonly)
	}
  
	protected def getModelClientImplReadonly(graphID: Ident) : ModelClientImpl = {
		val readonlyModel = getNamedModelReadonly(graphID)
		new ModelClientImpl(readonlyModel)
	}
	protected def makePointerToTypedGraphFromMC(graphID: Ident, someModelClient: RdfNodeTranslator): PointerToTypedGraph = {
	
		val typedGraphID: TypedResrc = TypedResrcFactory.exposeTypedResrc(graphID, Set(), someModelClient)
		new BasicPointerToTypedGraph(typedGraphID, this)
	}
}

class ServerModelProvider(mySrcRepo: Repo.WithDirectory) extends NamedModelProvider {
	// Depending on our interp of "readonly", this should possibly be a copy
	override def getNamedModelReadonly(graphID: Ident): Model = mySrcRepo.getNamedModel(graphID)
}

class ClientModelProvider(myRepoClient: RepoClient) extends NamedModelProvider {
	override def getNamedModelReadonly(graphID: Ident): Model = myRepoClient.getNamedModelReadonly(graphID)
  
}
