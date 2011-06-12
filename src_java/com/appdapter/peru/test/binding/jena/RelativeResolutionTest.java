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

package com.appdapter.peru.test.binding.jena;

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
