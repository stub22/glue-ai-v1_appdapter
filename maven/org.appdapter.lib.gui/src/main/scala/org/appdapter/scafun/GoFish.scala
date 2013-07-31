/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.scafun

import org.appdapter.demo.{DemoBrowserUI, DemoBrowserCtrl}
//import org.appdapter.gui.scafun.Boxy

object GoFish {
  def main(args: Array[String]): Unit = {
    println(this.getClass.getCanonicalName() + " sez:  yo");
    println("Mapping stuff to other stuff, blending dynamic approximation and ironic detachment.");
    val time = java.lang.System.currentTimeMillis();
    println("The time is: " + time);
    DemoBrowserUI.testLoggingSetup();
    //TestBrowse.main(args);
    val tnc = makeTNC(args);
    tnc.launchFrame("GoFish");

  }
  def makeTNC(args: Array[String]): DemoBrowserCtrl = {
    val tnc = DemoBrowserUI.makeDemoNavigatorCtrl(args);
    val box1 = Boxy.boxItUp();
    tnc.addObject(null, box1, true, false);
    tnc;
  }
  /*

  	// public List<TrigType> getTriggers();
	// public BoxContext getBoxContext();
	// void attachTrigger(TrigType bt);
	// void setContext(BoxContext bc);
	// void setDisplayContextProvider(DisplayContextProvider dcp);
	  */

  // }
}
