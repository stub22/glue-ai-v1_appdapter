/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package heaven.piece
import  com.appdapter.gui.box.{Box, BoxContext, MutableBox, Trigger, BoxImpl, TriggerImpl};

class Boxy {
	
	class FullBox[FT <:  FullTrigger[_ <: FullBox[FT]]] extends BoxImpl[FT] {}
	trait FullTrigger[FB <:  FullBox[_ <: FullTrigger[FB]]] extends TriggerImpl[FB] {}

	class BoxOne extends FullBox[FullTrigger[BoxOne]] { }
	class TriggerOne extends FullTrigger[BoxOne] {
		override def fire(box : FullBox[TriggerOne]) : Unit = {
			println(this.toString() + " firing on " + box.toString());
		}
	}

	def boxItUp() : BoxOne = {
		val box1 = new BoxOne();
		val trig1 = new TriggerOne();
		trig1.setShortLabel("one");
		box1.attachTrigger(trig1);
		box1;
	}

}
