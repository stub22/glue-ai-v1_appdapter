/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.gui.box;

import com.appdapter.core.item.Ident;

/**
 * @author winston
 *  A known box is an interactable entity tied to a modeled representation of that entity (in some repo).
 */
public interface KnownComponent {
	public Ident getIdent();

	public String getDescription();

	public String getShortLabel();
}
