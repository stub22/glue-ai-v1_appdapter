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
import org.appdapter.api.trigger.{ BoxContext, MutableTrigger, TriggerImpl }
import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils
import org.appdapter.bind.rdf.jena.model.JenaFileManagerUtils
import org.appdapter.demo.DemoResources
import org.appdapter.gui.box.ScreenBoxImpl
import scala.collection.JavaConverters.asScalaBufferConverter

class FullBox[FT <: FullTrigger[_ <: FullBox[FT]]] extends org.appdapter.gui.box.ScreenBoxImpl[FT] {}

trait FullTrigger[FB <: FullBox[_ <: FullTrigger[FB]]] extends MutableTrigger[FB] {}

class BoxOne extends FullBox[TriggerOne] {
  import collection.JavaConverters._
  def getOpenKidBoxes(bc: BoxContext): Seq[BoxOne] = {
    val kidBoxJL = ScreenBoxImpl.boxctxGetOpenChildBoxesNarrowed(bc,this, classOf[BoxOne], classOf[TriggerOne]).asInstanceOf[java.util.List[BoxOne]];
    //		val kidBoxSeq : Seq[BoxOne] = scala.collection.JavaConversions.asBuffer(kidBoxJL) ;
    val kidBoxSeq: Seq[BoxOne] = kidBoxJL.asScala;
    kidBoxSeq;
  }
}

class TriggerOne extends TriggerImpl[BoxOne] with FullTrigger[BoxOne] {
  override def fire(box: BoxOne): Unit = {
    println(this.toString() + " firing on " + box.toString());
  }
}


object Boxy {

  def boxItUp(): BoxOne = {
    val box1 = new BoxOne();
    ScreenBoxImpl.doSetShortLabel(box1, "boxOne-1")
    val trig1 = new TriggerOne();
    trig1.setShortLabel("trigOne-1");
    ScreenBoxImpl.doAttachTrigger(box1, trig1);
    box1;
  }
  def main(args: Array[String]): Unit = {
    println(this.getClass.getCanonicalName() + " sez:  we like rectangles!  Beginning test RDF load of boxes.");
    val triplesPath = DemoResources.MENU_ASSEMBLY_PATH;
    JenaFileManagerUtils.ensureClassLoaderRegisteredWithDefaultJenaFM(this.getClass().getClassLoader());
    println("Loading triples from URL: " + triplesPath);
    // Set<Object> 
    val loadedStuff = AssemblerUtils.buildAllObjectsInRdfFile(triplesPath);
    println("Loaded objects: " + loadedStuff);
  }

}
