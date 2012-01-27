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
 * @author Stu B. <www.texpedient.com>
 */
public interface SimpleFinder<OT> extends Finder<OT> {
	public static int	MAX_MATCHES = Integer.MAX_VALUE;
	// Return first match, null, or throw, depdending on min/max allowed.
	
	public OT findFirstMatch(Pattern p, int minAllowed, int maxAllowed) throws Exception;
	
	public List<OT> findAllMatches(Pattern p);
	
	public long countMatches(Pattern p);
}
