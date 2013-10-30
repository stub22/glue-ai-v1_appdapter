package org.appdapter.trigger.bind.jena;

import org.appdapter.api.trigger.Trigger;

// class FullBox[FT <: FullTrigger[_ <: FullBox[FT]]] extends ScreenBoxImpl[FT] {}

public class FullBox<TrigType extends Trigger<? extends BoxImpl<TrigType>>> extends BoxImpl<TrigType> //
{

}
