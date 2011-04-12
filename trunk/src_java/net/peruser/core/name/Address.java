package net.peruser.core.name;

import net.peruser.core.document.SentenceValue;
import net.peruser.core.config.Config;

/**
 * Address is an immutable identifier, essentiallly equivalent to a URI.
 * The fully expanded form of the URI string is called the "long form" of the address.
 *
 * <br/>
 * We use Address simply to isolate our application code from dependence on any external Java implementations of URI (e.g. jena "Resource").
 * @author      Stu Baurmann
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
