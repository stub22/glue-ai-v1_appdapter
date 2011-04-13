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
		val tnc = TestBrowse.makeTestNavigatorCtrl(args);
		val box1 = Boxy.boxItUp();
		tnc.addBoxToRoot(box1);
		tnc.launchFrame("GoFish");

	}
/*
	def attachGBChild(parentBox : Box[_]) : GoodBox[] = {
		val bc : BoxContext = parentBox.getBoxContext();
		val gbChild : GoodBox = makeGoodBox();
		bc.contextualizeAndAttachChildBox(parentBox, gbChild);
		gbChild;
	}
	def makeGoodBox() :  GoodBox[_] = {
		val gb = new GoodBox[GoodTrigger]();
		val gt = new GoodTrigger();
		gt.setShortLabel("primo_good");
		gb.attachTrigger(gt);
		gb;
	}    // extends TriggerImpl[GBT] 

	class GoodTrigger [GBT <: Box[Trigger[GBT]]]() {
		def fire(targetBox : GBT) : Unit = {
			println("GoodTrigger is firing on box: " + targetBox);
		}
	}
*/
 // class GoodBox [GTT <: Trigger[Box[GTT]]] () extends BoxImpl[GTT]  {
	  /*
  	// public List<TrigType> getTriggers();
	// public BoxContext getBoxContext();
	// void attachTrigger(TrigType bt);
	// void setContext(BoxContext bc);
	// void setDisplayContextProvider(DisplayContextProvider dcp);
	  */
	 
 // }
}
