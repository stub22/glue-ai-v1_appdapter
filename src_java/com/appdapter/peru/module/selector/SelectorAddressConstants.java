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

package com.appdapter.peru.module.selector;

import com.appdapter.peru.core.name.Address;
import com.appdapter.peru.core.name.CoreAddress;

import com.appdapter.peru.core.vocabulary.SubstrateAddressConstants;

/**
 * These constants are used by the SelectorCommand to lookup values in its configuration model.
 *
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public interface SelectorAddressConstants extends SubstrateAddressConstants {
	
	public static final String		SAC_NS = "http://www.peruser.net/2007/module/selector#";
	
	// New addresses added supporting Selector/FlatFetchConfig
	public static	Address queryFilePlacePropAddress = new CoreAddress(SAC_NS + "queryFilePlace");
	public static	Address inputModelPropAddress = new CoreAddress(SAC_NS + "inputModel");
	public static	Address inputNameRefPropAddress = new CoreAddress(SAC_NS + "inputNameRef");
	public static	Address inferenceConfigPropAddress = new CoreAddress(SAC_NS + "hasInferenceConfig");
	public static	Address modelPlacePropAddress = new CoreAddress(SAC_NS + "resolvedModelPlace");

/*	
	public static class Helper {
		public static	Address parseShortAddress(String shortForm) {
			return getAbbreviator().makeAddressFromShortForm(shortForm);
		}
		private static CoreAbbreviator	theAbbreviator = null;
		public static CoreAbbreviator getAbbreviator() {
			if (theAbbreviator == null) {
				HashMap<String,String> prefixMap = new HashMap<String,String> ();
				prefixMap.put(SubstrateAddressConstants.substrateAlias, SubstrateAddressConstants.substrateNS);
				theAbbreviator =  new CoreAbbreviator(prefixMap,  ":", null);  
			}
			return theAbbreviator;
		}
	}	
*/
}
