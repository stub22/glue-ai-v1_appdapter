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

package org.appdapter.peru.test.core.name;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import net.peruser.core.vocabulary.SubstrateVocabulary;

/**
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public class AddressTest {
	
	private static Logger 		theLogger = LoggerFactory.getLogger(AddressTest.class);
	
	public static void main(String[] args) {
		theLogger.info("AddressTest - gears are spinning up!");
		try {
			// Address linkMarkerPropAddress = Address.parseAddress(":linkMarker");		
			// Address provaScriptParamAddress = new Address(SubstrateVocabulary.PARAM_PROVA_SCRIPT);
		} catch (Throwable t) {
			theLogger.error("AddressTest caught ", t);
		}
		theLogger.info("AddressTest - gears are spinning down!");
	}
	
}
