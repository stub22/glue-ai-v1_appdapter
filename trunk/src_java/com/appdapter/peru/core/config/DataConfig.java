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


package com.appdapter.peru.core.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.appdapter.peru.core.name.Address;
import com.appdapter.peru.core.name.Abbreviator;

import com.appdapter.peru.core.process.Data;

import com.appdapter.peru.core.environment.Environment;

/**
 * A DataConfig is based on processor.Data
 * <br/>
 * Information in the model is contrued as "Frames" and "Slots".
 * <br/>
 * This class does not know what kind of Model it is using, or where it comes from.
 *
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public class DataConfig extends AbstractConfig {
	private static Log 		theLog = LogFactory.getLog(DataConfig.class);
	
	private		Data				myData;
	private		Abbreviator			myAbbreviator;
	

	public List getFieldValues (Address thingAddress, Address fieldAddress) throws Throwable {

		//Model actMod = getActiveJenaModel();
		List result = new ArrayList();
		/*
		Resource frameResource = resolveFrame(thingAddress);
		Property slotProperty = resolveSlot(fieldAddress);		
		theLog.debug("Fetching values for slot " + slotProperty + " in frame " + frameResource);		
		StmtIterator	matchIter = actMod.listStatements(frameResource, slotProperty, (RDFNode) null);
		while (matchIter.hasNext()) {
			Statement statement = (Statement) matchIter.next();
			RDFNode valNode = statement.getObject();
			theLog.debug("FOUND: " + valNode);
			if (valNode instanceof Resource) {
				Address valAddress = new JenaAddress((Resource) valNode);
				result.add(valAddress);
			} else {
				Literal lit = (Literal) valNode.as(Literal.class);
				// Decision - convert this literal into a Doc or not?
				result.add(lit.getString());
			}
		}
		*/
		return result;
	}
	public List getBackpointerFieldValues (Address thingAddress, Address fieldAddress) throws Throwable {
		// Model actMod = getActiveJenaModel();
		List result = new ArrayList();
		/*
		StmtIterator	matchIter = actMod.listStatements(null, resolveSlot(fieldAddress), resolveFrame(thingAddress));
		while (matchIter.hasNext()) {
			Statement statement = (Statement) matchIter.next();
			Address valAddress = new JenaAddress(statement.getSubject());
			result.add(valAddress);
		}
		*/
		return result;
	}
	/*
	public void applyOverrides (Doc d) throws Throwable {
		// CoreAbbreviator abb = CoreAbbreviator.makeCoreAbbreviator("WRONG", "NOPE", null, null);
		// Using "myAbbreviator" means that prefixes in the doc must be same as in our backing model.
		d.applyOverrides (this, myAbbreviator);
	}
	
	public void clearValues(Address thing, Address field) throws Throwable {
	}
	public void addAddressValuedSentence(Address thingAddress, Address fieldAddress, Address valueAddress) throws Throwable {
	}
	public void addStringValuedSentence(Address thingAddress, Address fieldAddress, String valueString) throws Throwable {
	}
	public void addDocValuedSentence(Address thingAddress, Address fieldAddress, Doc valueDoc) throws Throwable {
	} 
	*/
	public MutableConfig makeMutableCloneConfig(Environment env) throws Throwable {
		MutableConfig result = null;
		return result;
	}
}

