/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.com).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.appdapter.peru.module.projector;

import java.util.Iterator;

/**
 *  When we want to "fetch a tree of objects" from an RDF-like model along a particular axis, 
 *  this is the form we want to get back.
 *
 * @todo 		Make these iterators strongly typed using JDK1.5 templates, after we've validated JDK1.5 on Linux
 * @author      Stu B. <www.texpedient.com>
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
