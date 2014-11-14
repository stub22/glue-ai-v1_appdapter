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

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.appdapter.core.log.Debuggable;
import org.slf4j.Logger;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 *         TODO: This should extend BaseIdent, wherein it should share hashCode() + equals() impls with all other possible Idents (i.e. JenaResourceItems). These are only 2 impls extant as of 2013-06-01.
 */
public class FreeIdent implements SerIdent {
	final private String myAbsUri;
	final private String myLocalName;

	public FreeIdent(Ident src) {
		// Will fail if src is a Jena blank-node
		this(src.getAbsUriString(), src.getLocalName());
	}

	public FreeIdent(String absUri, String localName) {
		if (!absUri.endsWith(localName)) {
			throw new RuntimeException("Uri[" + absUri + "] does not end with LocalName[" + localName + "]");
		}
		myLocalName = localName;
		myAbsUri = previousURI(absUri);
	}

	public FreeIdent(String absUriWithOneHash) {
		int len = absUriWithOneHash.length();
		int hashIndex = absUriWithOneHash.indexOf('#');
		if ((hashIndex < 0) || (hashIndex > len - 2)) {
			throw new RuntimeException("Uri does not contain text after hash '#' [" + absUriWithOneHash + "]");
		}
		myLocalName = absUriWithOneHash.substring(hashIndex + 1);
		myAbsUri = previousURI(absUriWithOneHash);
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

	public Ident getIdent() {
		return this;
	}

	public static boolean THROW_ON_CHANGE = false;
	public static boolean AUTO_CORRECT_CHANGES = false;
	static Map<String, String> fragmentToURI = new HashMap<String, String>();
	static Map<String, String> autoCorrectedURI = new HashMap<String, String>();
	final static Map<String, Throwable> fragmentToCreationFrame = new HashMap<String, Throwable>();
	final static Logger theLogger = Debuggable.getLogger(FreeIdent.class);

	public static String previousURI(String uri) {
		synchronized (autoCorrectedURI) {
			String newSuggest = autoCorrectedURI.get(uri);
			if (AUTO_CORRECT_CHANGES && newSuggest != null) {
				return newSuggest;
			}
			newSuggest = correctURI(uri);
			if (newSuggest.equals(uri)) {
				return uri;
			}
			if (AUTO_CORRECT_CHANGES) {
				return newSuggest;
			}
			return uri;
		}
	}

	public static String correctURI(String uri) {
		synchronized (fragmentToCreationFrame) {
			try {
				URI good = URI.create(uri);
				String fragment = getFragmentKey(good, uri);
				if (fragment == null) {
					Debuggable.oldBug("(not a QNAME?) " + uri + " debuggable=" + Debuggable.getStackVars());
					return uri;
				}
				if (fragment.length() < 3) {
					if ("0123456789.".indexOf(fragment.charAt(0)) != -1) {
						Debuggable.showFrame(Debuggable.createFrame("(number) " + uri + " debuggable=" + Debuggable.getStackVars()));
						return fragment;
					} else {
						theLogger.debug("MAYBE BUG: (short name) " + uri + " debuggable=" + Debuggable.getStackVars());
					}
					return uri;

				}
				String old = fragmentToURI.get(fragment);
				Debuggable.oldBug("LocalName Prefix ", fragment, old, uri);
				if (old == null) {
					uri = uri.intern();
					fragmentToCreationFrame.put(fragment, Debuggable.createFrame("Creation frame for " + uri));
					fragmentToURI.put(fragment, uri);
					return uri;
				}
				return old;
			} catch (Throwable e) {
				Throwable rc = e.getCause();
				if (rc != e && rc != null) {
					e = rc;
				}
				Debuggable.oldBug("BAD JENA RESOURCE " + uri + " " + e);
				return uri;
			}

		}
	}

	private static String getFragmentKey(URI good, String uri) {
		String frag = uri;
		if (good != null) {
			frag = good.getFragment();
		}
		if (frag == null) {
			int li = uri.lastIndexOf('#');
			if (li > -1) {
				frag = uri.substring(li + 1);
			} else {
				li = uri.lastIndexOf('/');
				frag = uri.substring(li + 1);
			}
		}
		return frag;
	}

	public static boolean verifyURI(String uri) {
		try {
			if (uri.equals("#Property Name"))
				return false;
			previousURI(uri);
			return true;
		} catch (Exception e) {
			theLogger.error("OLD BUG:  BAD JENA RESOURCE " + uri + " " + e);
			return false;
		}
	}

	static public boolean checkChanged(String what, String localName, String prevURI, String uri) {
		if (prevURI != null) {
			if (!prevURI.equals(uri)) {
				String err = what + " Change: " + uri + " WAS " + prevURI;
				theLogger.error(err);
				if (Debuggable.isTesting()) {
					Debuggable.showFrame(fragmentToCreationFrame.get(localName));
					Debuggable.showFrame(Debuggable.createFrame(err));
				}
				if (THROW_ON_CHANGE)
					throw new RuntimeException(err);
				return false;
			}
		}
		return true;
	}
}
