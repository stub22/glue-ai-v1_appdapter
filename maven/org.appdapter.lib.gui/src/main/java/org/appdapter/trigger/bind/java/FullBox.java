package org.appdapter.trigger.bind.java;

import org.appdapter.gui.box.ScreenBoxImpl;

// class FullBox[FT <: FullTrigger[_ <: FullBox[FT]]] extends ScreenBoxImpl[FT] {}

public class FullBox<TrigType extends FullTrigger<? extends FullBox<TrigType>>> extends ScreenBoxImpl<TrigType> {

}
