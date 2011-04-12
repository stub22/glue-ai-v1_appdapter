package net.peruser.module.selector;

import net.peruser.core.name.Address;
import net.peruser.core.name.CoreAddress;

import net.peruser.core.vocabulary.SubstrateAddressConstants;

/**
 * These constants are used by the SelectorCommand to lookup values in its configuration model.
 *
 * @author      Stu Baurmann
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
