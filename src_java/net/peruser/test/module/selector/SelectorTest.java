package net.peruser.test.module.selector;

import net.peruser.binding.jena.SPARQL_Utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static net.peruser.test.data.TestDataConstants.SPARQL_UnitTestConstants.*;

/**  SelectorTest is our unit test for the peruser "Selector" SPARQL query interface.
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class SelectorTest {
	private static Log 		theLog = LogFactory.getLog(SelectorTest.class);	
	public static void main(String[] args) {
		theLog.info("SPARQL Unit Test - inflating!");
		try {
			String resultXML = SPARQL_Utils.executeQueryFromFiles (SUT_queryFileURL, SUT_modelURL, SUT_modelFormat, SUT_modelBaseURI);
			theLog.debug(resultXML);		
		} catch (Throwable t) {
			theLog.error("SPARQL Unit Test caught exception: ", t);
		}
		theLog.info("SPARQL Unit Test - deflating!");
	}

}
