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

package com.appdapter.peru.module.projector;


import com.appdapter.peru.core.name.Address;
import com.appdapter.peru.core.name.CoreAddress;
// import net.peruser.core.name.CoreAbbreviator;
// import java.util.Map;
// import java.util.HashMap;

import com.appdapter.peru.core.vocabulary.SubstrateAddressConstants;

/**
 * These hardcoded constants are used by both .net.peruser.core and net.peruser.modules 
 * to read/write configuration sentences.
 *
 * <BR/>
 * We should probably separate the "modules" constants into their own file soon.
 *
 * <BR/>
 * Specifically, these constants are presently used by ProjectorCommand, X2, X3...
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public interface ProjectorAddressConstants extends SubstrateAddressConstants {
	
	public static final String  PAC_NS = "http://www.peruser.net/2007/module/projector#";

	public static final	Address rootPropAddress = new CoreAddress(PAC_NS + "rootThingRef");
	public static final	Address linkConfigPropAddress = new CoreAddress(PAC_NS + "linkConfig");
	public static final	Address linkPropRefPropAddress = new CoreAddress(PAC_NS + "linkPropertyRef");
	public static final	Address linkMarkerPropAddress = new CoreAddress(PAC_NS + "linkMarker");
	public static final	Address fieldConfigPropAddress = new CoreAddress(PAC_NS + "fieldConfig");
	public static final	Address fieldPropRefPropAddress = new CoreAddress(PAC_NS + "fieldPropertyRef");
	public static final	Address fieldLabelPropAddress = new CoreAddress(PAC_NS + "fieldLabel");	

	public static final Address depthAddress		 = new CoreAddress(PAC_NS + "maxDepth");
	public static final Address forwardMarkerAddress = new CoreAddress(PAC_NS + "PARENT_POINTS_TO_CHILD");
	public static final Address reverseMarkerAddress = new CoreAddress(PAC_NS + "CHILD_POINTS_TO_PARENT");	

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
