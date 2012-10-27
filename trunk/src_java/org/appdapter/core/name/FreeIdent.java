/*
 *  Copyright 2012 by The Appdapter Project (www.appdapter.org).
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
package org.appdapter.core.name;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class FreeIdent implements Ident {
	private	String		myAbsUri;
	private String		myLocalName;
	
	public FreeIdent(Ident src) {
		// Will fail if src is a Jena blank-node
		this(src.getAbsUriString(), src.getLocalName());
	}
	public FreeIdent (String absUri, String localName) {
		if (!absUri.endsWith(localName)) {
			throw new RuntimeException("Uri[" + absUri + "] does not end with LocalName[" + localName + "]");
		}
		myAbsUri = absUri;
		myLocalName = localName;
	}
	public FreeIdent(String absUriWithOneHash) {
		int len = absUriWithOneHash.length();
		int hashIndex = absUriWithOneHash.indexOf('#');
		if ((hashIndex < 0) || (hashIndex > len - 2)) {
			throw new RuntimeException("Uri does not contain text after hash '#' [" + absUriWithOneHash + "]");
		}
		myAbsUri = absUriWithOneHash;
		myLocalName = absUriWithOneHash.substring(hashIndex + 1);
	}
	@Override public String getAbsUriString() {
		return myAbsUri;
	}

	@Override public String getLocalName() {
		return myLocalName;
	}
	@Override public boolean equals(Object o) {
		if ((o != null) && (o instanceof Ident)) {
			String otherAbsUri = ((Ident) o).getAbsUriString();
			return myAbsUri.equals(otherAbsUri);
		} else {
			return false;
		}
	}
	@Override public int hashCode() {
		return myAbsUri.hashCode();
	}

	@Override public String toString() {
		return "FreeIdent[absUri=" + myAbsUri + "]";
	}	
}
