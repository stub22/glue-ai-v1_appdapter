/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.gui.box;

/**
 *
 * @author winston
 */
public interface MutableTrigger<BoxType extends Box<? extends MutableTrigger<BoxType>>> extends Trigger<BoxType> {

}
