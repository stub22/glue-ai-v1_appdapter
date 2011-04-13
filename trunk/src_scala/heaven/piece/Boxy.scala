/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package heaven.piece
import  com.appdapter.gui.box.{Box, BoxContext, MutableBox, Trigger, BoxImpl, TriggerImpl};

object Boxy {
	
	class FullBox[FT <:  FullTrigger[_ <: FullBox[FT]]] extends BoxImpl[FT] {}
	trait FullTrigger[FB <:  FullBox[_ <: FullTrigger[FB]]] extends TriggerImpl[FB] {}

	class BoxOne extends FullBox[FullTrigger[BoxOne]] { }
	class TriggerOne extends FullTrigger[BoxOne] {
		override def fire(box : BoxOne) : Unit = {
			println(this.toString() + " firing on " + box.toString());
		}
	}

	def boxItUp() : BoxOne = {
		val box1 = new BoxOne();
		box1.setShortLabel("boxOne-1")
		val trig1 = new TriggerOne();
		trig1.setShortLabel("trigOne-1");
		box1.attachTrigger(trig1);
		box1;
	}

}
