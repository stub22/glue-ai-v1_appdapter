/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.gui.box;

/**
 *
 * @author winston
 */
public interface MutableBox<TrigType extends Trigger<? extends MutableBox<TrigType>>> extends Box<TrigType>  {

	void attachTrigger(TrigType bt);

	void setContext(BoxContext bc);

	void setDisplayContextProvider(DisplayContextProvider dcp);

}
