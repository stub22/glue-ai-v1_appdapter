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


package org.appdapter.peru.core.config;

import java.util.List;

import org.appdapter.peru.core.name.Address;

import org.appdapter.peru.core.document.Doc;

import org.appdapter.peru.core.environment.Environment;


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
 * @author      Stu B. <www.texpedient.com>
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

