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

import java.util.List;

/**
 * Additional Registry methods which simply ther process of looking for a single matching
 * object, or all matching objects, for clients who do not wish to work directly with 
 * Finders and Receivers.
 * 
 * @author Stu B. <www.texpedient.com>
 */
public interface SimpleRegistry extends Registry {

	/**
	 * @return any match found or null, with no uniqueness constraints, no problem mon.
	 */
	public <OT> OT findOptionalFirstMatch(Class<OT> objClaz, Pattern p);
	
	/**
	 * @return exactly one unique match or exception is thrown
	 */

	public <OT> OT findRequiredUniqueMatch(Class<OT> objClaz, Pattern p) throws Exception;
	/**
	 * @return null if nothing, or single unique match, but throws on multi-match
	 */
	
	public <OT> OT findOptionalUniqueMatch(Class<OT> objClaz, Pattern p) throws Exception;
	/**
	 * @return all matches, but throws if result size would be outside of inclusive-bounds [minAllowed, maxAllowed]
	 */
	
	public <OT> List<OT> findAllMatches(Class<OT> objClaz, Pattern p, int minAllowed, int maxAllowed) throws Exception;	
}
