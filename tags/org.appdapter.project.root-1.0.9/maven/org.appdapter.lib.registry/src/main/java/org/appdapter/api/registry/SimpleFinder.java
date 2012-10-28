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
 * Additional Finder methods which do not require the user to supply a Receiver.
 * 
 * @author Stu B. <www.texpedient.com>
 */
public interface SimpleFinder<OT> extends Finder<OT> {
	public static int	MAX_MATCHES = Integer.MAX_VALUE;
	/**
	 * Seeks only one match, chosen arbitrarily from available matches, 
	 * but will throw an exception if available number of 
	 * matches for the pattern is not within closed (inclusive) interval 
	 * [minAllowed, maxAllowed].
	 *
	 * @param p pattern which must be matched by the registered object descriptions. 
	 * @param minAllowed the minimum number of matches allowed for this Pattern.
	 * @param maxAllowed the maximum number of matches allowed for this Pattern.
	 * @return first match, null, or throw, depdending on min/max allowed.
	 *  
	 */
	public OT findFirstMatch(Pattern p, int minAllowed, int maxAllowed) throws Exception;
	/**
	 * Find all matches, and return in an arbitrary order, but will throw an exception 
	 * if available number of  matches for the pattern is not within closed (inclusive) 
	 * interval [minAllowed, maxAllowed].
	 * 
	 * @param p pattern which must be matched by the registered object description. * 
	 * @param minAllowed minimum number of matches allowed for this Pattern.
	 * @param maxAllowed maximum number of matches allowed for this Pattern.
	 * @return first match, null, or throw, depdending on min/max allowed.
	 *  
	 */
	public List<OT> findAllMatches(Pattern p, int minAllowed, int maxAllowed) throws Exception;
	/**
	 * Count the number of available matches, but throw an exception if count goes strictly
	 * higher than maxAllowed.
	 * 
	 * @param maxAllowed maximum number of matches allowed for this Pattern.
	 * @return first match, null, or throw, depdending on min/max allowed.
	 *  
	 */
	public long countMatches(Pattern p, int maxAllowed) throws Exception;
}
