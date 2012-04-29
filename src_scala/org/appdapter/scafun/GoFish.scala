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

import org.appdapter.api.trigger.{Box, BoxContext, BoxImpl, MutableBox, Trigger, TriggerImpl};
import org.appdapter.gui.demo.{DemoBrowser, DemoNavigatorCtrl};

object GoFish {
  def main(args: Array[String]) :Unit = {
	  println(this.getClass.getCanonicalName() + " sez:  yo");
		println("Mapping stuff to other stuff, blending dynamic approximation and ironic detachment.");
		val time = java.lang.System.currentTimeMillis();
		println("The time is: " + time);
		DemoBrowser.pretendToBeAwesome();
		//TestBrowse.main(args);
		val tnc = makeTNC(args);
		tnc.launchFrame("GoFish");

	}
	def makeTNC(args: Array[String]) : DemoNavigatorCtrl = {
		val tnc = DemoBrowser.makeDemoNavigatorCtrl(args);
		val box1 = Boxy.boxItUp();
		tnc.addBoxToRoot(box1, false);
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
