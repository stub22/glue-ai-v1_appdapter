/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.gui.box;

import java.util.List;

/**
 * A Box is some entity displayable (perhaps in pieces) and interactable in GUI.
 * It is not necessarily a rectangular graphical area, although it might be.
 * Sometimes it is more like a wooden box of stuff, maybe a round one...
 *
 * @author winston
 */
public interface Box<TrigType extends Trigger<? extends Box<TrigType>>> {

	public BoxContext getBoxContext();

	public List<TrigType> getTriggers();

}
