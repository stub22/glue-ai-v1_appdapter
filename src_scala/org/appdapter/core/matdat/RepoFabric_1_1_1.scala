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
import org.appdapter.api.trigger.{ BoxContext, MutableBox, BoxImpl, TriggerImpl }
import org.appdapter.core.store.{ Repo }
import org.appdapter.gui.repo.RepoModelBoxImpl
import org.appdapter.gui.trigger.SysTriggers;
import org.appdapter.scafun.{ FullBox, FullTrigger, BoxOne }
import org.appdapter.core.store.RepoSpec
/**
 * @author Stu B. <www.texpedient.com>
 */

class RepoFabric_1_1_1 {
  private var myEntries: List[Entry] = Nil;

  def addEntry(spec: RepoSpec): Unit = {
    myEntries = myEntries ::: List(new Entry(spec));
  }
  def getEntries(): List[Entry] = myEntries;

  class Entry(val mySpec: RepoSpec) {
    lazy val myRepo = mySpec.makeRepo();

    lazy val myRepoClient = mySpec.makeRepoClient(myRepo);

    // private lazy val	myRepoBox = new org.appdapter.gui.demo.DemoBrowser$DemoRepoBoxImpl()

    def getMutableBox() = {
      new ScreenBoxForImmutableRepo_1_1_1(myRepo)
      // myRepoBox.setRepo(myRepo.asInstanceOf[Repo.Mutable]);
      // myRepoBox;
    }
  }
}

class GraphBox_1_1_1(val myURI: String) extends org.appdapter.scafun.FullBox[GraphTrigger_1_1_1] {
  setShortLabel("tweak-" + myURI);
}
class GraphTrigger_1_1_1 extends TriggerImpl[GraphBox_1_1_1] with FullTrigger[GraphBox_1_1_1] {
  override def fire(box: GraphBox_1_1_1): Unit = {
    println(this.toString() + " firing on " + box.toString());
  }
}
class ScreenBoxForImmutableRepo_1_1_1(val myRepo: Repo) extends BoxOne {
  import scala.collection.JavaConversions._;
  def resyncChildrenToTree(): Unit = {
    val ctx: BoxContext = getBoxContext();
    val graphStats: List[Repo.GraphStat] = myRepo.getGraphStats().toList;
    for (gs <- graphStats) {
      val graphBox = new GraphBox_1_1_1(gs.graphURI);
      val gt = new GraphTrigger_1_1_1();
      gt.setShortLabel("have-some-fun with " + gs)
      graphBox.attachTrigger(gt);
      ctx.contextualizeAndAttachChildBox(this, graphBox)
    }
  }
}
class FabricBox_1_1_1(val myFabric: RepoFabric_1_1_1) extends org.appdapter.scafun.BoxOne {
  /*		BT result = CachingComponentAssembler.makeEmptyComponent(boxClass);
	 result.setShortLabel(label);
	 result.setDescription("full description for box with label: " + label);
	 */
  setShortLabel("Repo-Fabric-Box");
  setDescription("Repo-Fabric-Box for " + myFabric.toString);

  def resyncChildrenToTree(): Unit = {
    val ctx: BoxContext = getBoxContext();
    val ocb = ctx.getOpenChildBoxes(this);
    for (e <- myFabric.getEntries) {
      val repoScreenBox = e.getMutableBox()
      repoScreenBox.setShortLabel(e.mySpec.toString())
      ctx.contextualizeAndAttachChildBox(this, repoScreenBox)
      repoScreenBox.resyncChildrenToTree()
    }
  }
}
