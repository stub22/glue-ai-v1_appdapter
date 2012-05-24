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

package org.appdapter.api.facade;

import org.appdapter.api.registry.VerySimpleRegistry;
import org.appdapter.core.log.BasicDebugger;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class SubsystemRegistryFuncs extends BasicDebugger {
	/**
	 * Assumes that subRegClaz has a no-args constructor().
	 * @param <SubRegType>
	 * @param rootReg
	 * @param subRegClaz
	 * @param subRegName
	 * @return 
	 */
	public static <SubRegType extends VerySimpleRegistry> SubRegType  findOrMakeSubsystemRegistry(VerySimpleRegistry rootReg, 
				final Class<SubRegType> subRegClaz, final String subRegName) {	
		
		return MakableObjectHelpFuncs.findOrMakeUniqueNamedObjectWithDefCons(rootReg, subRegClaz, subRegName);
	}
	
	public static FacadeRegistry  findOrMakeSubsystemFacadeRegistry(VerySimpleRegistry rootReg, final String subsysRegName) {
		return findOrMakeSubsystemRegistry(rootReg, FacadeRegistry.class, subsysRegName);	
	}
}
