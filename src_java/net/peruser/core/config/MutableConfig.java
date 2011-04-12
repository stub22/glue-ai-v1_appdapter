package net.peruser.core.config;

import java.util.List;

import net.peruser.core.name.Address;

import net.peruser.core.document.Doc;

/**
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public interface MutableConfig extends Config {
	public void clearValues(Address thingAddress, Address fieldAddress) throws Throwable;
	
	public void addAddressValuedSentence(Address thingAddress, Address fieldAddress, Address valueAddress) throws Throwable;
	public void addStringValuedSentence(Address thingAddress, Address fieldAddress, String valueString) throws Throwable;
	public void addDocValuedSentence(Address thingAddress, Address fieldAddress, Doc valueDoc) throws Throwable;
	
	public void applyOverrides(Doc d) throws Throwable;
}

