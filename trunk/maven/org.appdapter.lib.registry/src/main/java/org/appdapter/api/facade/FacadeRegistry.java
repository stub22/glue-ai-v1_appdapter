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
import org.appdapter.registry.basic.BasicRegistry;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class FacadeRegistry extends BasicRegistry {
	

	public <OT> OT  findOrMakeUniqueNamedObject(Class<OT> objClaz, String objName, Maker<OT> maker) {
		return MakableObjectHelpFuncs.findOrMakeUniqueNamedObject(this, objClaz, objName, maker);
	}
	public <OT> OT  findOrMakeUniqueNamedObjectWithDefCons(final Class<OT> objClaz, final String objName) {	
		return MakableObjectHelpFuncs.findOrMakeUniqueNamedObjectWithDefCons(this, objClaz, objName);
	}
	public <FT, FK> String chooseBestLocalFacadeName(FacadeSpec<FT,FK> fs, String optOverrideName) {
		String actualName = fs.getNameString();
		if (optOverrideName != null) {
			actualName = optOverrideName;
		}
		return actualName;
	}
	public <IFT, IFK> IFT  findOrMakeInternalFacade(FacadeSpec<IFT, IFK> fs, String optOverrideName ) {	
		Class<IFT> facadeClaz   = fs.getFacadeClass();
		String actualName = chooseBestLocalFacadeName(fs, optOverrideName);
		return findOrMakeUniqueNamedObjectWithDefCons(facadeClaz, actualName);
	}
	/**
	 * Paired with findExternalFacade, used for objects supplied from outside, mainly from JME3:
	 * 
	 * <ol><li>AssetManager</li>
	 * <li>root node</li>
	 * <li>flat GUI node</li>
	 * </ol>
	*/	
	public <EFT, EFK> void registerExternalFacade(FacadeSpec<EFT, EFK> fs, EFT facade, String optOverrideName) {
		String actualName = chooseBestLocalFacadeName(fs, optOverrideName);
		registerNamedObject(facade, actualName);
	}
	/**
	 * Paired with registerExternalFacade, used for external objects, like JME3 assetManager, rootNode, guiNode.
	 * 
	 * @param <EFT>
	 * @param objClaz
	 * @param objName
	 * @return 
	 */		
	public <EFT, EFK> EFT findExternalFacade(FacadeSpec<EFT, EFK> fs, String optOverrideName)  {
		Class<EFT> facadeClaz = fs.getFacadeClass();
		String actualName = chooseBestLocalFacadeName(fs, optOverrideName);
		EFT result = null;
		try {
			result = findOptionalUniqueNamedObject(facadeClaz, actualName);
		} catch (Throwable t) {
			logError("Problem finding object: " + actualName + " of class: " + facadeClaz);
		}
		return result;
	}			
}
