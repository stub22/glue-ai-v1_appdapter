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

package org.appdapter.peru.binding.jena;

import org.appdapter.peru.core.name.Address;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 *
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
 
public class JenaAddress extends Address {
	Resource	myJenaResource;
	
	public JenaAddress(Resource r) {
		myJenaResource = r;
	}
	public JenaAddress(String longForm) {
		myJenaResource = ModelUtils.makeUnattachedResource(longForm);
	}
	public String getLongFormString() {
		// Will return null if myJenaResource is a bnode.
		return myJenaResource.getURI();
	}
	public Resource getJenaResource() {
		return myJenaResource;
	}
	/*
	public JenaAddress (Abbreviator abb, Resource jenaResource) {
		super(abb, jenaResource.getURI());
		myJenaResource = jenaResource;
	}
	*/
	/*
	public int unresolvedHashCode() {
		return myJenaResource.hashCode();
	}
	public boolean unresolvedEquals(Address a) {
		if (a instanceof JenaAddress) {
			return myJenaResource.equals(((JenaAddress) a).myJenaResource);
		} else {
			return false;
		}
	}
	public String unresolvedDebugString() {
		return myJenaResource.toString();
	}
	*/	
}
