package org.appdapter.gui.box;

import java.util.Enumeration;

import org.appdapter.api.trigger.Box;
import org.appdapter.gui.browse.DisplayContext;

public interface SmallComponentHost {

	public abstract void setDisplayContext(DisplayContext dc);

	public abstract DisplayContext getDisplayContext();

	public abstract Box getBox();

	public abstract SmallComponentHost findDescendantNodeForBox(Box b);

	public abstract Enumeration children();

	//public abstract DisplayContext findDisplayContext();

	public abstract void add(SmallComponentHost childNode);

	public abstract Object getParent();
	
	//public void reload(DisplayNode node);

	//public abstract DisplayNode getParent();

}