/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.core.item;

/**
 *
 * @author winston
 *
 * We expect Ident impls to be well behaved w.r.t. hashCode() and equals()
 */
public interface Ident {
	public String getAbsUriString();
	// Typically either a variable name or a fragmentID.
	public String getLocalName();
}
