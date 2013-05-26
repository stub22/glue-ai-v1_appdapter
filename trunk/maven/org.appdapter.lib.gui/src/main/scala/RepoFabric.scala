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

package org.appdapter.test.scala
import org.appdapter.api.trigger.{BoxContext, MutableBox, BoxImpl, TriggerImpl}
import org.appdapter.core.store.{Repo}
import org.appdapter.gui.repo.RepoModelBoxImpl;
import org.appdapter.gui.trigger.SysTriggers;

import org.appdapter.core.name.{Ident, FreeIdent}
import org.appdapter.core.store.{Repo, InitialBinding}
import org.appdapter.core.store.{ RepoSpec, RepoOper, RepoClient, RepoSpecJava, Repo, InitialBinding }
import org.appdapter.help.repo. { RepoClientImpl, RepoClientScala, InitialBindingImpl}
import org.appdapter.impl.store.{FancyRepo, DatabaseRepo, FancyRepoFactory}
import com.hp.hpl.jena.query.{QuerySolution}
import com.hp.hpl.jena.rdf.model.{Model}
import com.hp.hpl.jena.sparql.sse.SSE
import com.hp.hpl.jena.sparql.modify.request.{UpdateCreate, UpdateLoad}
import com.hp.hpl.jena.update.{GraphStore, GraphStoreFactory, UpdateAction, UpdateRequest}
import com.hp.hpl.jena.sdb.{Store, SDBFactory}
import org.appdapter.core.matdat.CsvFilesSheetRepoLoader
import org.appdapter.core.matdat.OmniLoaderRepo
import org.appdapter.core.matdat.SheetRepo

/**
 * @author Stu B. <www.texpedient.com>
 */


class RepoFabric {
	private	var		myEntries : List[Entry] = Nil;
	
	def addEntry(spec : RepoSpec) : Unit = {
		myEntries = myEntries ::: List(new Entry(spec));
	}
	def getEntries() : List[Entry] = myEntries;
	
	class Entry(val mySpec: RepoSpec) {
		lazy val	myRepo = mySpec.makeRepo();
	
		lazy val	myRepoClient = mySpec.makeRepoClient(myRepo);
		
		// private lazy val	myRepoBox = new org.appdapter.gui.demo.DemoBrowser$DemoRepoBoxImpl()
		
		def getMutableBox() = {
			new ScreenBoxForImmutableRepo(myRepo)
			// myRepoBox.setRepo(myRepo.asInstanceOf[Repo.Mutable]);
			// myRepoBox;
		}
	}
}
import org.appdapter.scafun.{FullBox, FullTrigger, BoxOne}
class GraphBox(val myURI : String) extends org.appdapter.scafun.FullBox[GraphTrigger] {
	setShortLabel("tweak-" + myURI);
}
class GraphTrigger extends TriggerImpl[GraphBox] with FullTrigger[GraphBox] {
	override def fire(box : GraphBox) : Unit = {
		println(this.toString() + " firing on " + box.toString());
	}	
}
class ScreenBoxForImmutableRepo(val myRepo : Repo) extends BoxOne {
	import scala.collection.JavaConversions._;	
	def resyncChildrenToTree() : Unit = {
		val ctx : BoxContext = getBoxContext();
		val graphStats : List[Repo.GraphStat] = myRepo.getGraphStats().toList;
		for (gs  <- graphStats) {
			val graphBox = new GraphBox(gs.graphURI);
			val gt  = new GraphTrigger();
			gt.setShortLabel("have-some-fun with " + gs)
			graphBox.attachTrigger(gt);				
			ctx.contextualizeAndAttachChildBox(this, graphBox)
		}
	}
}
class FabricBox(val myFabric : RepoFabric) extends org.appdapter.scafun.BoxOne {
	/*		BT result = CachingComponentAssembler.makeEmptyComponent(boxClass);
	 result.setShortLabel(label);
	 result.setDescription("full description for box with label: " + label);
	 */
	setShortLabel("Repo-Fabric-Box");
	setDescription("Repo-Fabric-Box for " + myFabric.toString);
  
	def resyncChildrenToTree() : Unit = {
		val ctx : BoxContext = getBoxContext();
		val ocb = ctx.getOpenChildBoxes(this);
		for (e <- myFabric.getEntries) {
			val repoScreenBox = e.getMutableBox()
			repoScreenBox.setShortLabel(e.mySpec.toString())
			ctx.contextualizeAndAttachChildBox(this, repoScreenBox)
			repoScreenBox.resyncChildrenToTree()
		}
	}
}
