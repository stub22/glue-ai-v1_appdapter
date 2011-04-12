/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.gui.box;

/**
 *
 * @author winston
 */
public interface Trigger<BoxType extends Box<? extends Trigger<BoxType>>> {
	public abstract void fire(BoxType targetBox);
}
