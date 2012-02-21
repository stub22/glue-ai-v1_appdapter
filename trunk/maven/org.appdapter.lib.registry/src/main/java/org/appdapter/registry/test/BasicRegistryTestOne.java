/*
 *  Copyright 2012 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.registry.test;

import org.appdapter.api.registry.VerySimpleRegistry;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.osgi.registry.RegistryServiceFuncs;

/**
 * @author Stu B. <www.texpedient.com>
 *
 */
public class BasicRegistryTestOne extends BasicDebugger {
    public static void main( String[] args ) {
		BasicRegistryTestOne brto = new BasicRegistryTestOne();
		brto.doTest(args);
	}
	public void doTest(String[] args ) { 
		logInfo("START - args=" + args.toString());
       
		
		VerySimpleRegistry vsr = RegistryServiceFuncs.getTheWellKnownRegistry(getClass());
		
		try {
			String name35 =  "thirty five";
			vsr.registerNamedObject(new Long(35), name35);
			Number optNum35 = vsr.findOptionalUniqueNamedObject(Number.class, name35);
			
			logInfo("Found optional number: " + optNum35);
			
			Number reqNum35 = vsr.findRequiredUniqueNamedObject(Number.class, "thirty five");
			
			logInfo("Found required number: " + optNum35);
			
			String optFailed35 = vsr.findOptionalUniqueNamedObject(String.class, name35);
			
			logInfo("Should have found null: " + optFailed35);
			logInfo("Next line should throw.");
			
			String reqFailed35 = vsr.findRequiredUniqueNamedObject(String.class, name35);
			
			logInfo("Oops, got: " + reqFailed35);			

		} catch (Throwable t) { 
			t.printStackTrace();
		}
	
    }
}
