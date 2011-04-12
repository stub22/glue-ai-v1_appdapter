/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.gui.box;

import com.appdapter.core.item.Ident;

/**
 *
 * @author winston
 */
public interface MutableKnownComponent extends KnownComponent {
	public void setIdent(Ident id);
	public void setDescription(String description);
	public void setShortLabel(String shortLabel);


}
