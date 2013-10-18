package org.appdapter.scafun;

import org.appdapter.gui.box.ScreenBoxImpl;

// class FullBox[FT <: FullTrigger[_ <: FullBox[FT]]] extends ScreenBoxImpl[FT] {}

public class FullBox<TrigType extends FullTrigger<? extends FullBox<TrigType>>> extends ScreenBoxImpl<TrigType> {

}
