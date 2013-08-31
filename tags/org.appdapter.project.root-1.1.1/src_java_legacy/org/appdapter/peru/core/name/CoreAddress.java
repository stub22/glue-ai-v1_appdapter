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

package org.appdapter.peru.core.name;

/**
 * CoreAddress is an implementation of Address with no dependencies on bindings to external tools. 
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public class CoreAddress extends Address {
	/*
	private		String 			myPrefix, myIdent;
	*/
	private		String			myLongForm;
	private		String			myDebugString;
	
	public CoreAddress (String longForm) {
		myLongForm = longForm; 
	}
	
	public void setDebugString (String debug) {
		myDebugString = debug;
	}
	
	public String getLongFormString() {
		return myLongForm;
	}
	
	public String toString() {
		String rep = myLongForm;
		if (myDebugString != null) {
			rep = myDebugString;
		}
		return "CoreAddress[" + rep + "]";
	}
	
	/*

	public CoreAddress (Abbreviator a, String prefix, String ident) {
		super(a, null);
		myPrefix = prefix;
		myIdent = ident;
	}
	*/
	/*
	public CoreAddress (String resolvedPath) {
		super(null, null);
		myResolvedPath = resolvedPath;
	}
	
	
	public String getShortPrefix() {
		return myPrefix;
	}
	public String getIdent() {
		return myIdent;
	}
	
	public int unresolvedHashCode() {
		return myPrefix.hashCode() ^ myIdent.hashCode();
	}
	public boolean unresolvedEquals(Address a) {
		if (a instanceof CoreAddress) {
			CoreAddress ca = (CoreAddress) a;
			return myPrefix.equals(ca.getShortPrefix()) && myIdent.equals(ca.getIdent());
		}
	}
	public String unresolvedDebugString() {
		return myPrefix + ":" + myIdent ;
	}
	*/	
}
