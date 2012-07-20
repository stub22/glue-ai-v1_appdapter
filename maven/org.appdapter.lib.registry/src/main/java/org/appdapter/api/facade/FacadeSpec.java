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

/**
 * @author Stu B. <www.texpedient.com>
 */
public class FacadeSpec<Type, Kind> {

	private final Class<Type> myObjClazz;
	private Kind myKind;
	private	boolean	myExtFlag;

	public FacadeSpec(Kind kind, Class<Type> sClz, boolean extFlag) {
		myKind = kind;
		myObjClazz = sClz;
		myExtFlag = extFlag;
	}

	public Class<Type> getFacadeClass() {
		return myObjClazz;
	}

	public Kind getKind() {
		return myKind;
	}

	public String getNameString() {
		return getKind().toString();
	}
	public boolean isExternal() { 
		return myExtFlag;
	}
	
	public Class determineCredentialClass(Class optOverrideClz, Class userClz) {
		Class credClz = userClz;
		if (optOverrideClz != null) {
			credClz = optOverrideClz;
		} else {
					
		// Use our own class as the default credential for external facades, to usually make Netigso happily return a bundleContext.			
			if (!isExternal()) {
				// Use the internal facade class as the default credential, to usually make Netigso happily return a bundleContext.
				if (myObjClazz != null) {
					credClz = myObjClazz;
				}
			}
		}
		return credClz;
	}
}
