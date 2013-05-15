package org.appdapter.gui.box;

import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import org.appdapter.api.trigger.Box;
import org.appdapter.gui.browse.DisplayContext;

public interface DisplayNode {

	public abstract void setDisplayContext(DisplayContext dc);

	public abstract DisplayContext getDisplayContext();

	public abstract Box getBox();

	public abstract DisplayNode findDescendantNodeForBox(Box b);

	public abstract Enumeration children();

	public abstract DisplayContext findDisplayContext();

	public abstract void add(DisplayNode childNode);

	public abstract Object getParent();
	
	//public void reload(DisplayNode node);

	//public abstract DisplayNode getParent();

}