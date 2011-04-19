/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.com).
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

package heaven.piece
import  com.appdapter.gui.box.{Box, BoxContext, MutableBox, Trigger, BoxImpl, TriggerImpl};

object Boxy {
	
	class FullBox[FT <:  FullTrigger[_ <: FullBox[FT]]] extends BoxImpl[FT] {
		/*
		def getOpenKidFullBoxes(bc : BoxContext) : Seq[FullBox] = {
			val rawtypeOpenChildrenJL : java.util.List[FriendBox] = bc.getOpenChildBoxesNarrowed(this, classOf[FT], classOf[FriendTrig]);
			val rawtypeKidSeq : Seq[FriendBox] = scala.collection.JavaConversions.asBuffer(rawtypeOpenChildrenJL) ;
			rawtypeKidSeq;
		}
		*/
	}
	trait FullTrigger[FB <:  FullBox[_ <: FullTrigger[FB]]] extends TriggerImpl[FB] {}

	// class BoxOne extends FullBox[FullTrigger[BoxOne]] { }
	class BoxOne extends FullBox[TriggerOne] {
		def getOpenKidBoxes(bc : BoxContext) : Seq[BoxOne] = {
			val kidBoxJL  = bc.getOpenChildBoxesNarrowed(this, classOf[BoxOne], classOf[TriggerOne]);
			val kidBoxSeq : Seq[BoxOne] = scala.collection.JavaConversions.asScalaBuffer(kidBoxJL) ;
			kidBoxSeq;
		}
		def foodleDoodle(bc : BoxContext) : Seq[BoxOne] = {
			val kidBoxJL  = bc.getOpenChildBoxesNarrowed(this, classOf[BoxOne], classOf[TriggerOne]);
			val kidBoxSeq : Seq[BoxOne] = scala.collection.JavaConversions.asScalaBuffer(kidBoxJL) ;
			kidBoxSeq;
		}
	}
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
