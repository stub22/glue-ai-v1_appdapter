package net.peruser.module.projector;

import java.util.Iterator;

/**
 *  When we want to "fetch a tree of objects" from an RDF-like model along a particular axis, 
 *  this is the form we want to get back.
 *
 * @todo 		Make these iterators strongly typed using JDK1.5 templates, after we've validated JDK1.5 on Linux
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public interface ProjectedNode {
	
	public Projector	getProjector() throws Throwable;
	
	public String		getUriString() throws Throwable;
	
	/**
	 * Get all the rdf:types for the node, with ordering possibly constrained by the projector
	 */
	public Iterator		getTypeUriStringIterator() throws Throwable;
	
	/**
	 * Get the string forms of all the values at the given property.
	 */
	public Iterator		getFieldValueStringIterator(String propertyURI) throws Throwable;
	
	/**
	 *  Get my children (as ProjectedNodes) along the "axis" defined by my projector
	 */
	public Iterator		getChildNodeIterator() throws Throwable;
	
	/*
	 * Return the name of the property that was used to navigate to this node from the "parent" node
	 * (which may in fact be a "child" in the model's sense, if the property was navigated "backwards").  .
	 */
	public String getStemPropertyURI() throws Throwable;
}
