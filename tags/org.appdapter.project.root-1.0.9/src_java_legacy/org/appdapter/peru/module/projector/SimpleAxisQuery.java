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

/**  navigation-direction-marker for a projector-module query-property. 
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public class SimpleAxisQuery {
	// Extend this to handle RDF Bag,Seq?
	public static int		PARENT_POINTS_TO_CHILD=0;
	public static int		CHILD_POINTS_TO_PARENT=1;
	
	private int		myDirection;
	private String	myPropertyURI;
	
	public SimpleAxisQuery (String propertyURI, int direction) {
		myDirection = direction;
		myPropertyURI = propertyURI;
	}
	public String	getPropertyURI() {
		return myPropertyURI;
	}
	public int getDirection() {
		return myDirection;
	}
}

