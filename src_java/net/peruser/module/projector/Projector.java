package net.peruser.module.projector;

import java.util.Set;

/**
 *  We use this to "fetch a tree of objects" from an RDF-like model along a particular axis.
 *  This interface does not account for how the Projector was initialized to point to a particular model.
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public interface Projector {

	/**
	 *  Return a node at URI with the given set of properties as the axis of projection 
	 */
	public ProjectedNode projectNode(String uriString, Set axisQueries) throws Throwable;

}
