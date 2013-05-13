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

package org.appdapter.subreg

import org.appdapter.api.facade.FacadeSpec
import org.appdapter.subreg.FacadeHandle
import org.appdapter.api.facade.FacadeRegistry
import org.appdapter.api.facade.Maker
import org.appdapter.api.facade.SubsystemRegistryFuncs
import org.appdapter.api.registry.VerySimpleRegistry
import org.appdapter.core.log.BasicDebugger
import org.appdapter.osgi.registry.RegistryServiceFuncs

/**
 * @author Stu B. <www.texpedient.com>
 */
class FacadeHandle[FT](val opt : Option[FT]) {
	def isReady() : Boolean = opt.isDefined;
	def getOrElse(f : FT) : FT = opt.getOrElse(f);
	def getOrNull() : Any = opt.orNull(null)
}

abstract class SubsystemHandle(val mySubsysName : String) extends BasicDebugger {
	protected def getRequiredOverRegistry( functionCtx : String) : VerySimpleRegistry

	def getFacadeRegistry() : FacadeRegistry = {
		val vsr = getRequiredOverRegistry("findOrMakeSubsystemFacadeRegistry");
		SubsystemRegistryFuncs.findOrMakeSubsystemFacadeRegistry(vsr, mySubsysName);
	}
	
	def findOrMakeUniqueNamedObject[OT] (objClaz : Class[OT], objName : String, maker : Maker[OT]) : OT = {
		val ssfr = getFacadeRegistry();
		ssfr.findOrMakeUniqueNamedObject(objClaz, objName, maker);
	}	
	def findOrMakeUniqueNamedObjectWithDefCons[OT](objClaz : Class[OT] , objName : String) : OT = {
		val ssfr = getFacadeRegistry();
		ssfr.findOrMakeUniqueNamedObjectWithDefCons(objClaz, objName);
	}
	def findOptionalUniqueNamedObject[OT](objClaz : Class[OT] , objName : String) : Option[OT] = {
		val ssfr = getFacadeRegistry();
		Option(ssfr.findOptionalUniqueNamedObject(objClaz, objName));
	}
	
	def  findOrMakeInternalFacade[IFT, IFK](fs: FacadeSpec[IFT, IFK], optOverrideName : String ) : IFT = {
		val ssfr = getFacadeRegistry();
		ssfr.findOrMakeInternalFacade(fs, optOverrideName);
	}

	def registerExternalFacade[EFT, EFK](fs : FacadeSpec[EFT, EFK], facade : EFT, optOverrideName : String) {
		val ssfr = getFacadeRegistry();
		ssfr.registerExternalFacade(fs, facade, optOverrideName);
	}

	def findExternalFacade[EFT, EFK](fs: FacadeSpec[EFT, EFK], optOverrideName : String) : FacadeHandle[EFT] = {
		val ssfr = getFacadeRegistry();
		val facadeOrNull = ssfr.findExternalFacade(fs, optOverrideName);
		new FacadeHandle(Option(facadeOrNull));
	}	
	
}

class BasicSubsystemHandle(subsysName : String, val credClaz : Class[_]) extends SubsystemHandle(subsysName) {
	private def getVerySimpleRegistry() : VerySimpleRegistry = {
		RegistryServiceFuncs.getTheWellKnownRegistry(credClaz);
	}
	override protected def getRequiredOverRegistry( functionCtx : String) : VerySimpleRegistry = { 
		val vsr = getVerySimpleRegistry(); 
		if (vsr == null) {
			val msg = "getRequiredOverRegistry(" + functionCtx + ") : Somehow got a null OverRegistry";
			logError(msg);
			throw new Exception(msg);
		}
		return vsr;
	}	
}