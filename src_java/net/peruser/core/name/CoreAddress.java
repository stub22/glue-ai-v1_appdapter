package net.peruser.core.name;

import net.peruser.core.document.SentenceValue;
import net.peruser.core.config.Config;

/**
 * CoreAddress is an implementation of Address with no dependencies on bindings to external tools. 
 * @author      Stu Baurmann
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
