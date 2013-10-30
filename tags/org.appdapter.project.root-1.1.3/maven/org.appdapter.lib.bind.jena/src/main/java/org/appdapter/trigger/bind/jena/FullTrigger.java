package org.appdapter.trigger.bind.jena;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.MutableTrigger;

//trait FullTrigger[FB <: FullBox[_ <: FullTrigger[FB]]] extends MutableTrigger[FB] {}
public interface FullTrigger<BoxType extends Box<? extends MutableTrigger<BoxType>>> extends MutableTrigger<BoxType> {
	@Override public void fire(BoxType targetBox);

}
