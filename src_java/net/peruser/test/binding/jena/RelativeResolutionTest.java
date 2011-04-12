package net.peruser.test.binding.jena;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

public class RelativeResolutionTest {
	private static Log 		theLog = LogFactory.getLog(RelativeResolutionTest.class);	
	
	public static void main(String args[]) {
		try {
			FileManager		fm = FileManager.get();
			
			// Doing this first allows the relative "file:" URIs (such as "file:../../") to resolve.  Without it, they fail!
			// Model turtleModel = fm.loadModel("app/testapp/rdf/irrelevant_contents.ttl");		
			Model directResult = fm.loadModel("app/testapp/rdf/embedded_relative_URI.rdf");
			theLog.debug("Directly loaded model: \n" + directResult);
			
		} catch (Throwable t) {
			theLog.error("RelativeResolutionTest caught: ", t);
		}		
	}
}                                                                                                                                        
