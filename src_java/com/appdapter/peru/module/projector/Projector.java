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

package com.appdapter.peru.module.projector;

import java.util.Set;

/**
 *  We use this to "fetch a tree of objects" from an RDF-like model along a particular axis.
 *  This interface does not account for how the Projector was initialized to point to a particular model.
 *
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public interface Projector {

	/**
	 *  Return a node at URI with the given set of properties as the axis of projection 
	 */
	public ProjectedNode projectNode(String uriString, Set axisQueries) throws Throwable;

}
