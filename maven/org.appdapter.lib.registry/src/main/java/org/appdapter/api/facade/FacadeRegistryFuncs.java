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

import org.appdapter.api.registry.Description;
import org.appdapter.api.registry.VerySimpleRegistry;
import org.appdapter.core.log.BasicDebugger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class FacadeRegistryFuncs extends BasicDebugger {
	private final static Logger		theLogger = LoggerFactory.getLogger(FacadeRegistryFuncs.class);
	/**
	 * Used to find or flexibly construct an object that is to be placed in a registry.
	 * @param <OT>
	 * @param objClaz
	 * @param objName
	 * @param maker
	 * @return 
	 */
	public static <OT> OT  findOrMakeUniqueNamedObject(VerySimpleRegistry vsr, Class<OT> objClaz, String objName, Maker<OT> maker) {
		OT result = null;
		try {
			result = vsr.findOptionalUniqueNamedObject(objClaz, objName);
			if (result == null) {
				result = maker.makeObj();
				Description regDesc = maker.getRegistryDesc(result, objName);
				vsr.registerObject(result, regDesc);
			}
		} catch (Throwable t) {
			theLogger.error("findOrMakeUniqueNamedObject got finder or maker exception: ", t);
		}
		return result;
	}	

	/**
	 * Further simplified "findOrMake" method, with longer name!  Uses objClaz.newInstance() as the maker.
	 * 
	 * @param <OT>
	 * @param objClaz
	 * @param objName
	 * @return Found object OR made (and now registered) object OR null on error.
	 */
	public static <OT> OT  findOrMakeUniqueNamedObjectWithDefCons(VerySimpleRegistry vsr, final Class<OT> objClaz, final String objName) {	
		// TODO:  Optimization:   Keep a cache of these DefCons makers, to avoid unnecessary object construction
		// (of the anon-class Makers themselves), which is happening on every call to this method).
		return findOrMakeUniqueNamedObject(vsr, objClaz, objName, new BasicMaker<OT>() {
			@Override public OT makeObj() {
				try {
					theLogger.info("Making new object named " + objName + " using default constructor of " + objClaz);
					return objClaz.newInstance();
				} catch (InstantiationException ie) {
					theLogger.error("findOrMakeUniqueNamedObjectWithDefCons got default constructor exception: ", ie);
					return null;
				} catch (IllegalAccessException iae) {
					theLogger.error("findOrMakeUniqueNamedObjectWithDefCons got default constructor exception: ", iae);
					return null;
				}					
			}
		});
	}
	
	public static <FT, FK> String chooseBestLocalFacadeName(FacadeSpec<FT,FK> fs, String optOverrideName) {
		String actualName = fs.getNameString();
		if (optOverrideName != null) {
			actualName = optOverrideName;
		}
		return actualName;
	}	
}
