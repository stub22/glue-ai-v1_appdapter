package net.peruser.core.vocabulary;

import net.peruser.core.name.Address;
import net.peruser.core.name.CoreAddress;

/*
import net.peruser.core.name.CoreAbbreviator;

import java.util.Map;
import java.util.HashMap;
*/

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
public interface SubstrateAddressConstants {
	public static 	String	substrateNS = "http://www.peruser.net/2007/command#";
	public static 	String	substrateAlias = "perc";
	
	//		theDefaultPrefixMapping.setNsPrefix("pkonf", "http://www.customerizer.com/2007/picky/conf#");
	
	// public static 	Address instructionAddress = new CoreAddress(substrateNS + "instruction");
	
	// public static	Address opConfigRefPropAddress = new CoreAddress(substrateNS + "opConfigRef");
	
	public static 	Address handlerConfigPropAddress = new CoreAddress(substrateNS + "handlerConfig");
	public static	Address javaClassNamePropAddress = new CoreAddress(substrateNS + "fullJavaClassName");
	
	public static 	Address transformConfigPropAddress = new CoreAddress(substrateNS + "transformConfig");
	public static	Address transformPathPropAddress = new CoreAddress(substrateNS + "transformPath");

	public static	Address identPropAddress = new CoreAddress(substrateNS + "identString");
	public static	Address spacePropAddress = new CoreAddress(substrateNS + "space");
	
	public static	Address boundSpacePropAddress = new CoreAddress(substrateNS + "boundSpace");
	
	// Which one of these kids is not like the others? 
	// public static	Address boundSpacePropAddress = Address.parseAddress(net.peruser._generated.SubstrateJenaConstants.boundSpace);
	
	// ":boundSpace");
	public static	Address locationPathPropAddress = new CoreAddress(substrateNS + "locationPath");	
	public static	Address repositoryPropAddress = new CoreAddress(substrateNS + "hasRepository");
	public static	Address urlPropAddress = new CoreAddress(substrateNS + "hasURL");
	
	public static	Address inferenceMarkerPropAddress = new CoreAddress(substrateNS + "inferenceMarker");
	public static	Address jenaRdfsInferenceMarkerAddress = new CoreAddress(substrateNS + "JENA_RDFS_INFERENCE");
	public static	Address noInferenceMarkerAddress = new CoreAddress(substrateNS + "NO_INFERENCE");
/*
	public static class Helper {
		public static	Address parseShortAddress(String shortForm) {
			return getAbbreviator().makeAddressFromShortForm(shortForm);
		}
		private static CoreAbbreviator	theAbbreviator = null;
		private static CoreAbbreviator getAbbreviator() {
			if (theAbbreviator == null) {
				HashMap<String,String> prefixMap = new HashMap<String,String> ();
				prefixMap.put(SubstrateAddressConstants.substrateAlias, SubstrateAddressConstants.substrateNS);
				theAbbreviator =  new CoreAbbreviator(prefixMap,  ":",  null);  
			}
			return theAbbreviator;
		}
	}
*/
}
