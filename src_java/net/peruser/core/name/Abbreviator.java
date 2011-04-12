package net.peruser.core.name;

import net.peruser.core.document.SentenceValue;
import net.peruser.core.config.Config;

/**
 * Abbrevitors are able to translate Addresses between short form and long form.
 * <br/>
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public interface Abbreviator {

	/*
	public abstract Object lookupCompatibleValueAtAddress (Config conf, Address address) throws Throwable;
	*/
	
	public abstract Address makeAddressFromLongForm(String longForm) throws Throwable;
	
	public abstract String resolveAddressToLongForm(Address a) throws Throwable;
	
	public abstract	Address makeAddressFromShortForm(String shortForm) throws Throwable;
	
	public abstract	String makeShortFormFromAddress(Address addr) throws Throwable;

}
