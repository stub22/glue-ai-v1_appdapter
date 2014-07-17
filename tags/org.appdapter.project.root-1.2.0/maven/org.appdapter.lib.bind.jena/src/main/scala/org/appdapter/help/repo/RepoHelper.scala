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

package org.appdapter.help.repo

import scala.collection.JavaConversions.{ asScalaBuffer, bufferAsJavaList }

import org.appdapter.core.name.Ident
import org.appdapter.core.store.Repo

import com.hp.hpl.jena.query.{ Query, QuerySolution }

/**
 * @author Stu B. <www.texpedient.com>
 */

object ChannelsTriggersHelper {

  def queryCommands(rc: RepoClient): java.util.List[CommandRec] = {
    val cmdQueryQN = "ccrt:find_cmds_99" // The QName of a query in the "Queries" model/tab
    val cmdGraphQN = "ccrt:cmd_sheet_AZR50" // The QName of a graph = model = tab, as given by directory model.
    val solList = rc.queryIndirectForAllSolutions(cmdQueryQN, cmdGraphQN)
    val resultJList = new java.util.ArrayList[CommandRec]();
    import scala.collection.JavaConversions._
    val solJList = solList.javaList
    solJList foreach (cmd => {
      val cmdID = cmd.getIdentResultVar("cmdID")
      val boxID = cmd.getIdentResultVar("boxID")
      val trigID = cmd.getIdentResultVar("trigID")
      val trigFQCN = cmd.getStringResultVar("trigFQCN")
      val cRec = new CommandRec(cmdID, boxID, trigID, trigFQCN);
      resultJList.add(cRec);
    })
    resultJList
  }
  //import org.cogchar.impl.channel.FancyChannelSpec;
  def assembleChannelSpecs(rc: RepoClient): java.util.Set[Object] = {
    rc.assembleRootsFromNamedModel("ccrt:chan_sheet_AZR50")
  }

  val eventQueryQN = "ccrt:find_agentItemEvents_99" // The QName of a query in the "Queries" model/tab
  val eventGraphQN = "ccrt:inbox_sheet_AZR50" // The QName of a graph = model = tab, as given by directory model.   

  def queryInboxEvents(rc: RepoClient) = {

    val solList = rc.queryIndirectForAllSolutions(eventQueryQN, eventGraphQN)
    import scala.collection.JavaConversions._
    solList.javaList foreach (inboxEvent => {
      println("Got event-inbox soln: " + inboxEvent);
      val eventID = inboxEvent.getIdentResultVar("eventID");
      val agentID = inboxEvent.getIdentResultVar("agentID");
      val tstampMsec = inboxEvent.getStringResultVar("tstamp");
      val actionName = inboxEvent.getStringResultVar("action");
      println("eventID=" + eventID + ", agentID=" + agentID + ", tstampMsec=" + tstampMsec + ", action=" + actionName)
    })

  }

}
class CommandRec(val cmdID: Ident, val boxID: Ident, val trigID: Ident, val trigFQCN: String) {
  override def toString(): String = "[cmdID=" + cmdID + ", boxID=" + boxID + ", trigID=" + trigID + ", trigFQCN=" + trigFQCN + "]";
}

class RepoHelper_UNUSED_MAYBE {
  val mySH = new SolutionHelper();

  def findSolutionsAsSolutionList(fr: Repo.WithDirectory, parsedQ: Query, qInitBinding: QuerySolution): SolutionList = {
    import scala.collection.JavaConversions._
    val natSL: scala.collection.mutable.Buffer[QuerySolution] = fr.findAllSolutions(parsedQ, qInitBinding);
    mySH.makeSolutionList(natSL);
  }
}