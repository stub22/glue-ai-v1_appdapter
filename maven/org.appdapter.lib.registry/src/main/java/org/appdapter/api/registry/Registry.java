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
package org.appdapter.api.registry;

/**
 * A registry is able to register Object+Description pairs, and generate Finders
 * that can search through the registered pairs.
 * 
 * @author Stu B. <www.texpedient.com>
 */
public interface Registry {
	
	// What is defined behavior if we register two objects for "same" description,
	// or two descriptions for "same" object?
	
	public void registerObject(Object o, Description d);
	
	/**
	 * @return a Finder that can search the Registry for objects of the given class OT.
	 */
	public <OT> Finder<OT> getFinder(Class<OT> objClaz);
}
