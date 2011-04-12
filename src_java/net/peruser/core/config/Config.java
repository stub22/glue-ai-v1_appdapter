package net.peruser.core.config;

import java.util.List;

import net.peruser.core.name.Address;

import net.peruser.core.document.Doc;

import net.peruser.core.environment.Environment;


/**
 * The Config interface defines methods which the peruser Machine system uses to access and
 * update configuration information.
 * <BR/>
 * Methods for setting/getting values require the value to be identified by a combination
 * of Address objects.  The methods use these directions to navigate among "things" and 
 * "fields", ultimately getting/setting values which are either Strings or Addresses.
 * <BR/>
 * This Address/Thing/Field paradigm may be seen as a simplified abstraction of the 
 * URI/Resource/Property semantics of RDF, which is the most natural implementation.
 * <BR/>
 * The use of "Address" (rather than, say, "jena Resource") as the configuration 
 * denominator is an important firewall separating the conceptual definition of the
 * peruser configuration system from the implementation details, and allowing (we hope)
 * for smooth future integration with other configuration paradigms, as required. 
 * <BR/>
 * The additional semantics available through the interface currently are the
 * ideas of "optional" values, and the idea of a "backpointer" query for
 * values that point to a particular address.
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public interface Config {
	public MutableConfig makeMutableCloneConfig(Environment env) throws Throwable;
	
	/*
	public void addAddressValuedSentence(Address thingAddress, Address fieldAddress, Address valueAddress) throws Throwable;
	public void addStringValuedSentence(Address thingAddress, Address fieldAddress, String valueString) throws Throwable;
	public void addDocValuedSentence(Address thingAddress, Address fieldAddress, Doc valueDoc) throws Throwable;
	public void applyOverrides(Doc d) throws Throwable;
	
	public void clearValues(Address thingAddress, Address fieldAddress) throws Throwable;
	*/
	public List getFieldValues(Address thingAddress, Address fieldAddress) throws Throwable;
	public List getBackpointerFieldValues(Address thingAddress, Address fieldAddress) throws Throwable;

	public String getSingleString(Address thing, Address field) throws Throwable;
	public String getOptionalString(Address thing, Address field) throws Throwable;
	public Address getSingleAddress(Address thing, Address field) throws Throwable;
	public Address getOptionalAddress(Address thing, Address field) throws Throwable;
	public Address getSingleBackpointerAddress(Address thing, Address field) throws Throwable;
}

