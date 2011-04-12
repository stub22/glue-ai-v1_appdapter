package net.peruser.test.core.name;

import net.peruser.core.name.Address;
import net.peruser.core.name.Abbreviator;
import net.peruser.core.name.CoreAddress;
import net.peruser.core.name.CoreAbbreviator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// import net.peruser.core.vocabulary.SubstrateVocabulary;

/**
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class AddressTest {
	
	private static Log 		theLog = LogFactory.getLog(AddressTest.class);
	
	public static void main(String[] args) {
		theLog.info("AddressTest - gears are spinning up!");
		try {
			// Address linkMarkerPropAddress = Address.parseAddress(":linkMarker");		
			// Address provaScriptParamAddress = new Address(SubstrateVocabulary.PARAM_PROVA_SCRIPT);
		} catch (Throwable t) {
			theLog.error("AddressTest caught ", t);
		}
		theLog.info("AddressTest - gears are spinning down!");
	}
	
}
