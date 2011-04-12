package net.peruser.module.projector;


import net.peruser.core.name.Address;
import net.peruser.core.name.CoreAddress;
// import net.peruser.core.name.CoreAbbreviator;
// import java.util.Map;
// import java.util.HashMap;

import net.peruser.core.vocabulary.SubstrateAddressConstants;

/**
 * These hardcoded constants are used by both .net.peruser.core and net.peruser.modules 
 * to read/write configuration sentences.
 *
 * <BR/>
 * We should probably separate the "modules" constants into their own file soon.
 *
 * <BR/>
 * Specifically, these constants are presently used by ProjectorCommand, X2, X3...
 * @author      Stu Baurmann
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
