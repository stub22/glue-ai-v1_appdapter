/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.gui.box;

import com.appdapter.gui.browse.DisplayContext;
import java.util.List;

/**
 *
 * @author winston
 */
public interface BoxContext {
	public Box getRootBox();
	public Box getParentBox(Box child);
	public List<Box> getOpenChildBoxes(Box parent);
	public DisplayContext	findDisplayContext(Box viewable);

	public void contextualizeAndAttachChildBox(Box<?> parentBox, MutableBox<?> childBox);
}
