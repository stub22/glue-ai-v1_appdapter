/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package heaven.piece

import com.appdapter.gui.box.{Box, BoxContext, BoxImpl, BoxTreeNode, MutableBox, Trigger, TriggerImpl};
import com.appdapter.test.{TestBrowse, TestNavigatorCtrl};

object GoFish {
  def main(args: Array[String]) :Unit = {
	  	println("heaven.piece.GoFish sez:  yo");
		println("Mapping stuff to other stuff, blending dynamic approximation and ironic detachment.");
		val time = java.lang.System.currentTimeMillis();
		println("The time is: " + time);
		TestBrowse.pretendToBeAwesome();
		//TestBrowse.main(args);
		val tnc = makeTNC(args);
		tnc.launchFrame("GoFish");

	}
	def makeTNC(args: Array[String]) : TestNavigatorCtrl = {
		val tnc = TestBrowse.makeTestNavigatorCtrl(args);
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
