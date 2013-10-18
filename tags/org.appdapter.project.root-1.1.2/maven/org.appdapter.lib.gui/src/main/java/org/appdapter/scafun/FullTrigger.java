package org.appdapter.scafun;

import org.appdapter.api.trigger.MutableTrigger;

//trait FullTrigger[FB <: FullBox[_ <: FullTrigger[FB]]] extends MutableTrigger[FB] {}
public interface FullTrigger<BoxType extends FullBox<? extends FullTrigger<BoxType>>> extends MutableTrigger<BoxType> {
	@Override public void fire(BoxType targetBox);

}
