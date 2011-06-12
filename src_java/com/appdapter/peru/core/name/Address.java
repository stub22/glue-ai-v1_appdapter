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

package com.appdapter.peru.core.name;

import com.appdapter.peru.core.document.SentenceValue;
import com.appdapter.peru.core.config.Config;

/**
 * Address is an immutable identifier, essentiallly equivalent to a URI.
 * The fully expanded form of the URI string is called the "long form" of the address.
 *
 * <br/>
 * We use Address simply to isolate our application code from dependence on any external Java implementations of URI (e.g. jena "Resource").
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public abstract class Address implements SentenceValue {
	/*
	private 	String				myCachedResolvedPath;
	private		Abbreviator			myAbbreviator;
		
	public Address (Abbreviator abb, String resolvedPath) {
		myAbbreviator = abb;
		myCachedResolvedPath = resolvedPath;
	}
	*/
	public abstract String getLongFormString();
	
	public String getResolvedPath() {
		return getLongFormString();
		
		/*
		if (myCachedResolvedPath == null) {
			if (myAbbreviator != null) {
				myCachedResolvedPath = myAbbreviator.resolveAddressToLongForm(this);
			}
		}
		return myCachedResolvedPath;
		*/
	}
	/*
	public Abbreviator getAbbreviator() {
		return myAbbreviator;
	}
	*/
	public boolean equals(Object other) {
		if ((other != null) && (other instanceof Address)) {
			Address otherAddress = (Address) other;
			return getResolvedPath().equals(otherAddress.getResolvedPath());
		} else {
			return false;
		}
	}
	public int hashCode() {
		return getResolvedPath().hashCode();
	}
	public String toString() {
		String resolved = getResolvedPath();
		/*
		if (resolved == null) {
			resolved = "UNRESOLVED[" + unresolvedDebugString() + "]";
		}
		*/
		return "ADDRESS[" + resolved + "]";
	}
	/*
	public abstract String unresolvedDebugString();
	public abstract int unresolvedHashCode();
	public abstract boolean unresolvedEquals(Address a);
	*/
	
	/** 
	*/

	public Object getCompatibleValue (Config conf) throws Throwable {
		
		return null;
		// return myAbbreviator.getCompatibleValueAtAddress(this, conf);
	}
	


}
