/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.gui.box;

import com.appdapter.gui.browse.DisplayContext;

/**
 *
 * @author winston
 */
public interface ViewableBox<TrigType extends Trigger<? extends ViewableBox<TrigType>>> extends Box<TrigType>, KnownComponent {

	public DisplayContext getDisplayContext();

	public BoxPanel findBoxPanel(BoxPanel.Kind kind);
}
