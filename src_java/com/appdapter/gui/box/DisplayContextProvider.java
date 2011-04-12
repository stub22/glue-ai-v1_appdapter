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
public interface DisplayContextProvider {
	public	DisplayContext		 findDisplayContext(Box b);
}
